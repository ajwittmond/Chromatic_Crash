package com.flickshot.components.entities.defs.overlays;

import com.flickshot.assets.music.Song;
import com.flickshot.components.entities.CommonEntity;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;

/**
 * Created by Alex on 4/28/2015.
 */
public class TitleMusic extends CommonEntity {
    public static final String ENTITY_NAME = "titleMusic";

    public Song titleMusic;

    public TitleMusic(){
        try{
            titleMusic = new Song("chip_sauce","chip_sauce_loop");
        }catch(Exception ex){
            throw new IllegalStateException("failed to load title music",ex);
        }
        persistant = true;
    }

    public static final EntityStateFactory getFactory(){
        return new EntityStateFactory() {
            @Override
            public EntityState construct() {
                return new TitleMusic();
            }

            @Override
            public Class<? extends EntityState> getType() {
                return TitleMusic.class;
            }
        };
    }
}
