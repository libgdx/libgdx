package com.badlogic.gdx.backends.lwjgl3.audio;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class OpenALMusicProcessor implements Runnable, Disposable
{
	private Queue<OpenALMusic> musicsToPlay = new ConcurrentLinkedQueue<OpenALMusic>();
	private Queue<OpenALMusic> musicsToStop = new ConcurrentLinkedQueue<OpenALMusic>();
	Queue<OpenALMusic> musicsStopped = new ConcurrentLinkedQueue<OpenALMusic>();
	
	Queue<OpenALMusic> musicsFinished = new ConcurrentLinkedQueue<OpenALMusic>();
	private Array<OpenALMusic> musicsToUpdate = new Array<OpenALMusic>();
	
	private Queue<OpenALMusic> musicsToSeek = new ConcurrentLinkedQueue<OpenALMusic>();
	
	private boolean shouldRun;
	private Thread audioThread;
	private final OpenALLwjgl3Audio audio;
	
	public OpenALMusicProcessor (OpenALLwjgl3Audio audio) {
		this.audio = audio;
		audioThread = new Thread(this);
		shouldRun = true;
		audioThread.start();
	}

	@Override
	public void run () {
		while(shouldRun){
			
			// start playback
			{
				OpenALMusic music;
				while((music = musicsToPlay.poll()) != null){
					if(music.async.prefillBuffers()){
						musicsToUpdate.add(music);
					}else{
						music.async.stop();
						musicsStopped.offer(music);
					}
				}
			}
			
			// seek to position
			{
				OpenALMusic music;
				while((music = musicsToSeek.poll()) != null){
					if(!music.async.seek()){
						musicsToUpdate.removeValue(music, true);
						music.async.stop();
						musicsFinished.offer(music);
					}
					music.seeking = false;
				}
			}
			
			// stream
			for(int i=musicsToUpdate.size-1 ; i>=0 ; i--){
				OpenALMusic music = musicsToUpdate.get(i);
				if(music.disposing){
					musicsToUpdate.removeIndex(i);
					music.async.stop();
					musicsStopped.offer(music);
				}
				else if(music.async.updateBuffers()){
					musicsToUpdate.removeIndex(i);
					music.async.stop();
					musicsFinished.offer(music);
				}
			}
			
			// stop playback
			{
				OpenALMusic music;
				while((music = musicsToStop.poll()) != null){
					musicsToUpdate.removeValue(music, true);
					music.async.stop();
					musicsStopped.offer(music);
				}
			}
			
			try {
				Thread.sleep(100); // TODO no need for fast updates
			} catch (InterruptedException e) {
				// silent fail
			}
		}
	}
	
	@Override
	public void dispose () {
		shouldRun = false;
		try {
			audioThread.join();
		} catch (InterruptedException e) {
			// silently fail
		}
	}

	/**
	 * called from render thread
	 * @param openALMusic
	 */
	void startPlayback (OpenALMusic openALMusic) {
		musicsToPlay.offer(openALMusic);
	}

	public void stopPlayback (OpenALMusic openALMusic) {
		musicsToStop.offer(openALMusic);
	}

	public void seek (OpenALMusic openALMusic) {
		musicsToSeek.offer(openALMusic);
	}

}
