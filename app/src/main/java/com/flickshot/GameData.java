package com.flickshot;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.widget.Filter;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.achievement.Achievement;
import com.google.android.gms.games.achievement.AchievementBuffer;
import com.google.android.gms.games.achievement.Achievements;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.games.snapshot.Snapshots;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Alex on 4/27/2015.
 */
public class GameData {
    private static final String SNAPSHOT_NAME = "autosave";
    private static final int MAX_RESOLUTION_ATTEMPTS = 8;

    private final ConcurrentHashMap<String,LevelData> data = new ConcurrentHashMap<String,LevelData>();

    SharedPreferences file;

    public GameData(){
    }

    public void read(SharedPreferences sp){
        Map<String,?> values = sp.getAll();
        for(String s: values.keySet()){
                String[] val = s.split(";");
                LevelData dat = getLevelData(val[0]);
                if (val[1].equals("topScore")) {
                    dat.topScore = (Integer) values.get(s);
                } else if (val[1].equals("medals")) {
                    dat.medals = (Integer) values.get(s);
                } else if (val[1].equals("minTime")) {
                    dat.minTime = (Float) values.get(s);
                } else if (val[1].equals("highestCombo")) {
                    dat.highestCombo = (Integer) values.get(s);
                }
        }
        file = sp;
    }

    public void write(){
        SharedPreferences.Editor editor = file.edit();
        if(file!=null) {
            for (String level : data.keySet()) {
                LevelData levelData = data.get(level);
                editor.putInt(level+";topScore",levelData.topScore);
                editor.putInt(level+";medals",levelData.medals);
                editor.putInt(level+";highestCombo",levelData.highestCombo);
                editor.putFloat(level+";minTime",(float)levelData.minTime);
            }
        }
        editor.commit();
        if(FlickShot.googleApiClient!=null && FlickShot.googleApiClient.isConnected())
            saveToDrive();
    }

    public LevelData getLevelData(String level){
        LevelData d = data.get(level);
        if(d==null){
            d = new LevelData();
            d.id = level;
            data.put(level,d);
        }
        return d;
    }

    private void updateFromSnapshot(Snapshot snapshot){
        try {
            ByteArrayInputStream bytesIn = new ByteArrayInputStream(snapshot.getSnapshotContents().readFully());
            ObjectInputStream objectIn = new ObjectInputStream(bytesIn);
            GameDataContainer container = (GameDataContainer)objectIn.readObject();
            for(LevelData l: container.levelData){
                LevelData curr = data.get(l.id);
                if(curr!=null){
                    curr.topScore = Math.max(curr.topScore,l.topScore);
                    curr.highestCombo = Math.max(curr.highestCombo,curr.highestCombo);
                    curr.medals = Math.max(curr.medals,l.medals);
                    if(curr.id.equals("infinite")){
                        curr.minTime = Math.max(curr.minTime,l.minTime);
                    }else{
                        curr.minTime = Math.min(curr.minTime, l.minTime);
                    }
                }else{
                    data.put(l.id,l);
                }
            }
        }catch(Exception ex){}
    }

    public int getMedalCount(){
        int count = 0;
        for(String l:data.keySet()){
            count+=data.get(l).medals;
        }
        return count;
    }

    public String getNext(int i){
        for(String l:data.keySet()){
            if(data.get(l).index == i+1){
                return l;
            }
        }
        return null;
    }

    public void saveToDrive(){
        try {

            final byte[] bytes = getBytes();

            PendingResult<Snapshots.OpenSnapshotResult> result = Games.Snapshots.open(
                    FlickShot.googleApiClient,SNAPSHOT_NAME,true,Snapshots.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED);

            result.setResultCallback(new ResultCallback<Snapshots.OpenSnapshotResult>() {
                @Override
                public void onResult(Snapshots.OpenSnapshotResult result) {
                    int status = result.getStatus().getStatusCode();

                    Snapshot snapshot = result.getSnapshot();
                    snapshot.getSnapshotContents().writeBytes(bytes);

                    Games.Snapshots.commitAndClose(FlickShot.googleApiClient, snapshot, SnapshotMetadataChange.EMPTY_CHANGE);
                }
            });

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void retrieveSaveData(){
        PendingResult<Snapshots.OpenSnapshotResult> result = Games.Snapshots.open(
                FlickShot.googleApiClient,SNAPSHOT_NAME,true);
        result.setResultCallback(new ResultCallback<Snapshots.OpenSnapshotResult>() {
            @Override
            public void onResult(Snapshots.OpenSnapshotResult openSnapshotResult) {
                processOpenSnapshotResult(openSnapshotResult, 0);
            }
        });
    }

    public void processOpenSnapshotResult(Snapshots.OpenSnapshotResult result, int retrys){
        int status = result.getStatus().getStatusCode();


        Snapshot snapshot = result.getSnapshot();
        updateFromSnapshot(snapshot);

        if(status == GamesStatusCodes.STATUS_SNAPSHOT_CONFLICT){
            updateFromSnapshot(result.getConflictingSnapshot());
            if(retrys<MAX_RESOLUTION_ATTEMPTS){
                try {
                    snapshot.getSnapshotContents().writeBytes(getBytes());
                    Snapshots.OpenSnapshotResult r = Games.Snapshots.resolveConflict(FlickShot.googleApiClient, result.getConflictId(), snapshot).await();
                    processOpenSnapshotResult(r,++retrys);
                }catch(Exception ex){}
            }else{
                write();
            }
        }else{
            write();
        }
    }

    private byte[] getBytes() throws IOException{
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        ObjectOutputStream objectsOut = new ObjectOutputStream(bytesOut);

        LevelData[] levels = new LevelData[data.size()];
        int i = 0;
        for (String id : data.keySet()) {
            levels[i++] = data.get(id);
        }
        GameDataContainer container = new GameDataContainer(levels);

        objectsOut.writeObject(container);
        return bytesOut.toByteArray();
    }

    public void checkAchievements(){
        if(!(FlickShot.googleApiClient!=null && FlickShot.googleApiClient.isConnected()))
            return;

        int bronze1=0,bronze2=0,bronze3=0,bronze4=0;
        int silver1=0,silver2=0,silver3=0,silver4=0;
        int gold1=0,gold2=0,gold3=0,gold4=0;

        //count medals for each category
        for(String id:data.keySet()) {
            LevelData level = data.get(id);
            switch (level.group) {
                case 0:
                    switch (level.medals) {
                        case 3:
                            gold1++;
                        case 2:
                            silver1++;
                        case 1:
                            bronze1++;
                    }
                    break;
                case 1:
                    switch (level.medals) {
                        case 3:
                            gold2++;
                        case 2:
                            silver2++;
                        case 1:
                            bronze2++;
                    }
                    break;
                case 2:
                    switch (level.medals) {
                        case 3:
                            gold3++;
                        case 2:
                            silver3++;
                        case 1:
                            bronze3++;
                    }
                    break;
                case 3:
                    switch (level.medals) {
                        case 3:
                            gold4++;
                        case 2:
                            silver4++;
                        case 1:
                            bronze4++;
                    }
                    break;
            }
        }

        Resources r = FlickShot.instance.getResources();
        if(bronze1>0)
            Games.Achievements.setSteps(
                    FlickShot.googleApiClient, r.getString(R.string.achievement_amateur_1),
                    bronze1);


        if(bronze2>0)
            Games.Achievements.setSteps(
                    FlickShot.googleApiClient, r.getString(R.string.achievement_amatuer_2),
                    bronze2);

        if(bronze3>0)
            Games.Achievements.setSteps(
                    FlickShot.googleApiClient, r.getString(R.string.achievement_amatuer_3),
                    bronze3);

        if(bronze4>0)
            Games.Achievements.setSteps(
                    FlickShot.googleApiClient, r.getString(R.string.achievement_amatuer_4),
                    bronze4);

        if(silver1>0)
            Games.Achievements.setSteps(
                    FlickShot.googleApiClient, r.getString(R.string.achievement_adept_1),
                    silver1);

        if(silver2>0)
            Games.Achievements.setSteps(
                    FlickShot.googleApiClient, r.getString(R.string.achievement_adept_2),
                    silver2);

        if(silver3>0)
            Games.Achievements.setSteps(
                    FlickShot.googleApiClient, r.getString(R.string.achievement_adept_3),
                    silver3);

        if(silver4>0)
            Games.Achievements.setSteps(
                    FlickShot.googleApiClient, r.getString(R.string.achievement_adept_4),
                    silver4);

        if(gold1>0)
            Games.Achievements.setSteps(
                    FlickShot.googleApiClient, r.getString(R.string.achievement_expert_1),
                    gold1);
        if(gold2>0)
            Games.Achievements.setSteps(
                    FlickShot.googleApiClient, r.getString(R.string.achievement_expert_2),
                    gold2);

        if(gold3>0)
            Games.Achievements.setSteps(
                    FlickShot.googleApiClient, r.getString(R.string.achievement_expert_3),
                    gold3);

        if(gold4>0)
            Games.Achievements.setSteps(
                    FlickShot.googleApiClient, r.getString(R.string.achievement_expert_4),
                    gold4);


        LevelData infinite = getLevelData("infinite");
        switch(infinite.medals){
            case 3:
                gold1++;
            case 2:
                silver1++;
            case 1:
                bronze1++;
        }

        if(bronze1+bronze2+bronze3+bronze4>0)
            Games.Achievements.setSteps(
                    FlickShot.googleApiClient, r.getString(R.string.achievement_full_amatuer),
                    bronze1+bronze2+bronze3+bronze4);

        if(silver1+silver2+silver3+silver4>0)
            Games.Achievements.setSteps(
                    FlickShot.googleApiClient, r.getString(R.string.achievement_full_adept),
                    silver1+silver2+silver3+silver4);

        if(gold1+gold2+gold3+gold4>0)
            Games.Achievements.setSteps(
                    FlickShot.googleApiClient, r.getString(R.string.achievement_full_expert),
                    gold1+gold2+gold3+gold4);

        if(infinite.topScore/10>0)
            Games.Achievements.setSteps(
                    FlickShot.googleApiClient, r.getString(R.string.achievement_infinite_pro),
                    infinite.topScore/10);

    }

    public static class GameDataContainer implements Serializable{
        public final LevelData[] levelData;

        GameDataContainer(LevelData[] levelData){
            this.levelData = levelData;
        }
    }

    public static class LevelData implements Serializable{
        public int topScore;
        public int medals;
        public int highestCombo;
        public int index;
        public int group;
        public double minTime;
        public String displayName;
        public String id;
    }
}
