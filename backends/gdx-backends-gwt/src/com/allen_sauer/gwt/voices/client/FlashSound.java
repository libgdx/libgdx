/*
 * Copyright 2009 Fred Sauer
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.allen_sauer.gwt.voices.client;

import static com.allen_sauer.gwt.voices.client.Sound.LoadState.LOAD_STATE_SUPPORTED_AND_READY;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

import com.allen_sauer.gwt.voices.client.ui.VoicesMovie;

import java.util.ArrayList;

/**
 * <a href= 'http://www.adobe.com/products/flashplayer/'>Adobe Flash Player</a> based
 * sound.
 */
public class FlashSound extends AbstractSound {
  // CHECKSTYLE_JAVADOC_OFF

  private static ArrayList<FlashSound> soundList = new ArrayList<FlashSound>();

  @SuppressWarnings("unused")
  private static void playbackCompleted(final int index) {
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      @Override
      public void execute() {
        soundList.get(index).playbackCompleted();
      }
    });
  }

  @SuppressWarnings("unused")
  private static void soundLoaded(final int index) {
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      @Override
      public void execute() {
        soundList.get(index).soundLoaded();
      }
    });
  }

  private int balance = SoundController.DEFAULT_BALANCE;
  private boolean looping = SoundController.DEFAULT_LOOPING;
  private final int soundNumber;
  private boolean soundRegistered = false;
  private final VoicesMovie voicesMovie;
  private int volume = SoundController.DEFAULT_VOLUME;

  public FlashSound(String mimeType, String url, boolean streaming, boolean crossOrigin,
      VoicesMovie voicesMovie) {
    super(mimeType, url, streaming, crossOrigin);
    this.voicesMovie = voicesMovie;
    soundNumber = soundList.size();
    soundList.add(this);
    if (streaming) {
      setLoadState(LOAD_STATE_SUPPORTED_AND_READY);
    } else {
      registerSound();
    }
  }

  @Override
  public int getBalance() {
    return balance;
  }

  @Override
  public boolean getLooping() {
    return looping;
  }

  public int getSoundNumber() {
    return soundNumber;
  }

  @Override
  public SoundType getSoundType() {
    return SoundType.FLASH;
  }

  @Override
  public int getVolume() {
    return volume;
  }

  @Override
  public boolean play() {
    registerSound();
    if (getLoadState() == LOAD_STATE_SUPPORTED_AND_READY) {
      // true indicates the sound was played
      return voicesMovie.playSound(soundNumber);
    }
    // sound was not played, return false
    return false;
  }

  @Override
  public void setBalance(int balance) {
    assert balance >= -100;
    assert balance <= 100;
    this.balance = balance;
    if (getLoadState() == LOAD_STATE_SUPPORTED_AND_READY) {
      voicesMovie.setBalance(soundNumber, balance);
    }
  }

  @Override
  public void setLooping(boolean looping) {
    this.looping = looping;
    if (getLoadState() == LOAD_STATE_SUPPORTED_AND_READY) {
      voicesMovie.setLooping(soundNumber, looping);
    }
  }

  @Override
  public void setVolume(int volume) {
    assert volume >= 0;
    assert volume <= 100;
    this.volume = volume;
    if (getLoadState() == LOAD_STATE_SUPPORTED_AND_READY) {
      voicesMovie.setVolume(soundNumber, volume);
    }
  }

  @Override
  public void stop() {
    if (getLoadState() == LOAD_STATE_SUPPORTED_AND_READY) {
      voicesMovie.stopSound(soundNumber);
    }
  }

  protected void playbackCompleted() {
    soundHandlerCollection.fireOnPlaybackComplete(this);
  }

  protected void soundLoaded() {
    setLoadState(LOAD_STATE_SUPPORTED_AND_READY);
    if (volume != SoundController.DEFAULT_VOLUME) {
      voicesMovie.setVolume(soundNumber, volume);
    }
    if (balance != SoundController.DEFAULT_BALANCE) {
      voicesMovie.setBalance(soundNumber, balance);
    }
    if (looping != SoundController.DEFAULT_LOOPING) {
      voicesMovie.setLooping(soundNumber, looping);
    }
  }

  private void registerSound() {
    if (!soundRegistered) {
      voicesMovie.registerSound(this);
      soundRegistered = true;
    }
  }
}
