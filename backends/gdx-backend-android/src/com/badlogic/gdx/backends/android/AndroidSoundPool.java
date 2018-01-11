package com.badlogic.gdx.backends.android;

import android.media.SoundPool;
import com.badlogic.gdx.Gdx;
import java.util.HashMap;

public class AndroidSoundPool extends SoundPool {

    private HashMap<Integer,SoundInfo> cacheMap = new HashMap<Integer,SoundInfo>();
    /**
     * @param maxStreams
     * @param streamType
     * @param srcQuality
     * @deprecated
     */
    public AndroidSoundPool(int maxStreams, int streamType, int srcQuality) {
        super(maxStreams, streamType, srcQuality);

        setOnLoadCompleteListener(new OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                if (cacheMap.containsKey(i)){
                    SoundInfo soundInfo = cacheMap.get(i);
                    play(soundInfo.soundID,soundInfo.leftVolume,soundInfo.rightVolume,soundInfo.priority,soundInfo.loop,soundInfo.rate);
                    cacheMap.remove(i);
                }
            }
        });
    }

    public void clearCache(){
        cacheMap.clear();
    }

    public  int playSound(int soundID, float leftVolume, float rightVolume, int priority, int loop, float rate) {
        int streamId =   play( soundID, leftVolume, rightVolume, priority, loop, rate);

        if (streamId == 0){
            cacheMap.put(soundID,new SoundInfo(soundID, leftVolume, rightVolume, priority, loop, rate));
        }

        return streamId;
    }

    class SoundInfo {
        int soundID;float leftVolume;float rightVolume;int priority;int loop;float rate;
        SoundInfo(int soundID, float leftVolume, float rightVolume, int priority, int loop, float rate){
            this.soundID = soundID;
            this.leftVolume = leftVolume;
            this.rightVolume = rightVolume;
            this.priority = priority;
            this.loop = loop;
            this.rate = rate;
        }
    }

}
