package com.flickshot.components.entities;

import android.content.res.XmlResourceParser;

import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.Entity;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.defs.managers.WaveEnd;
import com.flickshot.components.timeline.Timeline;
import com.flickshot.config.Config;
import com.flickshot.util.Action;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Alex on 4/13/2015.
 */
public class InfiniteModeWaveFactory {
    private static final ArrayList<EventConfig> configs = new ArrayList<EventConfig>();
    private static final ArrayList<EventConfig> bonusConfigs = new ArrayList<EventConfig>();

    public static void init(XmlResourceParser xrp){
        try {
            int event = xrp.next();
            while(event!=XmlResourceParser.START_TAG || !xrp.getName().equals("InfiniteModeConfig")){
                event = xrp.next();
            }
            for(;event!=XmlResourceParser.END_TAG ||  !xrp.getName().equals("InfiniteModeConfig"); event = xrp.next()){
                if(event == XmlResourceParser.START_TAG && xrp.getName().equals("Event")){
                    EventConfig ec = readEvent(xrp);
                    if(ec.bonus)
                        bonusConfigs.add(ec);
                    else
                        configs.add(ec);
                }
            }
        }catch(Exception ex){
            throw new IllegalStateException("Error reading infinite mode config at line:"+xrp.getLineNumber() +" column:"+xrp.getColumnNumber(),ex);
        }
    }

    private static EventConfig readEvent(XmlResourceParser xrp) throws Exception{
        int minWave = (int)getDoubleValue(xrp.getAttributeValue(null,"minWave"),0);
        int maxWave = (int)getDoubleValue(xrp.getAttributeValue(null,"maxWave"),Integer.MAX_VALUE);
        boolean bonus = "true".equalsIgnoreCase(xrp.getAttributeValue(null,"bonus"));
        String addString =xrp.getAttributeValue(null, "addString");
        if(addString==null)
            addString = "";

        ArrayList<EntityConfig> entities = new ArrayList<EntityConfig>();
        for(int event = xrp.next();event!=XmlResourceParser.END_TAG || !xrp.getName().equals("Event");){
            if(event == XmlResourceParser.START_TAG) {
                entities.add(readEntity(xrp));
                event = xrp.getEventType();
                continue;
            }
            event = xrp.next();
        }

        return new EventConfig(entities.toArray(new EntityConfig[entities.size()]),addString, minWave, maxWave, bonus);
    }

    private static double getDoubleValue(String s, double defaultVal){
        try {
            return Double.parseDouble(s);
        }catch(Exception ex){
            return defaultVal;
        }
    }

    private static EntityConfig readEntity(XmlResourceParser xrp) throws Exception{
        final String name = xrp.getName();
        final double x,y,t;
        final Config config;

        Entity e = Entities.getEntity(name);
        config = e.factory.getConfig();


        x = getDoubleValue(xrp.getAttributeValue(null,"x"),0);
        y = getDoubleValue(xrp.getAttributeValue(null,"y"),0);
        t = getDoubleValue(xrp.getAttributeValue(null,"t"),0);

//        System.err.println(name +" config obtained");
//        System.err.println(name + " x:"+x +" y:"+y+" t:"+t);

        for(int event = xrp.next();(event!= XmlResourceParser.START_TAG || xrp.getName().equals("config")) &&
                (event!= XmlResourceParser.END_TAG || !xrp.getName().equals(name)); ){
            if(event==XmlResourceParser.START_TAG && xrp.getName().equals("config")){
                if(config==null)
                    throw new IllegalStateException("no config for entity: \""+name+"\"");
                config.configure("config",xrp);
                event=xrp.getEventType();
//                System.out.println(name+" config configured");
            }
            event=xrp.next();
        }
        return new EntityConfig(name,x,y,t,config);
    }

    private static final ArrayList<EventConfig> misc = new ArrayList<EventConfig>(4);

    private static void generateWave(Timeline tln, int wave, ArrayList<EventConfig> configs){
        double t = tln.getTime()+1;

        misc.clear();


        do{
            EventConfig e = getRandConfig(configs, wave);
            boolean add=true;
            if(misc.size()>0) {
                for (int i = 0; i < misc.size(); i++) {
                    EventConfig temp = misc.get(i);
                    if (temp == e) {
                        add = false;
                        break;
                    } else {
                        for (int j = 0; j < e.addString.length(); j++) {
                            if (temp.addString.indexOf(e.addString.charAt(j)) >= 0) {
                                add = false;
                                break;
                            }
                        }
                    }
                }
            }
            if(add==true)
                misc.add(e);
        }while(misc.size()<4 && Math.random()<0.8+(0.2*(1.0-Math.pow((1.0/(wave)),1/2))));


//        System.out.println("t="+tln.getTime());
        for(int i = 0; i<misc.size(); i++){
            EventConfig ec= misc.get(i);
//            System.out.println("EntityConfigs "+ec.configs.length);
            for(int j = 0; j<ec.configs.length; j++){
                final EntityConfig c = ec.configs[j];
//                System.out.println("Spawn "+c.name+" at t=t+"+c.t+" or "+(t+c.t));
                tln.addAction(t+c.t,new Action(){
                    @Override
                    public void doAction() {
//                        System.out.println("Spawning: "+c.name);
                        EntityState e = Entities.newInstance(c.name,c.x,c.y);
                        e.configure(c.config);
                    }
                });
            }
        }

//        System.out.println("WaveEnd at t="+(t+5));
        tln.addAction(t+5,new Action(){

            @Override
            public void doAction() {
//                System.out.println("Spawn wave end");
                Entities.newInstance(WaveEnd.class,0,0);
            }
        });
    }

    public static void generateWave(Timeline t, int wave){
        generateWave(t,wave,configs);
    }

    public static void generateBonusWave(Timeline t, int wave){
        generateWave(t,wave,bonusConfigs);
    }

    private static EventConfig getRandConfig(ArrayList<EventConfig> list,int wave){
        EventConfig c;
        do{
            c = list.get((int)Math.floor(list.size()*Math.random()));
        }while(wave<=c.minWave || wave>=c.maxWave);
        return c;
    }


    private static final class EventConfig{
        EntityConfig[] configs;
        String addString;
        int minWave;
        int maxWave;
        boolean bonus;

        EventConfig(EntityConfig[] configs,String addString, int minWave, int maxWave,boolean bonus){
            this.configs = configs;
            this.addString = addString;
            this.minWave = minWave;
            this.maxWave = maxWave;
            this.bonus = bonus;
        }

    }

    private static final class EntityConfig{
        final String name;
        final double x,y,t;
        final Config config;
        EntityConfig(String name,double x,double y, double t,Config config){
            this.name = name;
            this.x=x;
            this.y=y;
            this.t=t;
            this.config = config;
        }
    }


}
