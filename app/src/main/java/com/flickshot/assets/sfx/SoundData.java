package com.flickshot.assets.sfx;

import android.media.SoundPool;

public class SoundData {
	final SoundPool pool;
	final int poolId;
	SoundData(SoundPool pool, int poolId){
		this.pool = pool;
		this.poolId = poolId;
	}
}
