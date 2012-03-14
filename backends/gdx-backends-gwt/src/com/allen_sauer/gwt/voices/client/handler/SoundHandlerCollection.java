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
package com.allen_sauer.gwt.voices.client.handler;

import java.util.ArrayList;

/**
 * Utility collection class used by AbstractSound.
 */
@SuppressWarnings("serial")
public class SoundHandlerCollection extends ArrayList<SoundHandler> {
  /**
   * Fires when playback of a sound completes.
   * 
   * @param sender the sound which finished playing
   */
  public void fireOnPlaybackComplete(Object sender) {
    PlaybackCompleteEvent event = new PlaybackCompleteEvent(sender);

    for (SoundHandler handler : this) {
      handler.onPlaybackComplete(event);
    }
  }

  /**
   * Fires when a sound's load state changes.
   * 
   * @param sender the sound who's load state changed
   */
  public void fireOnSoundLoadStateChange(Object sender) {
    SoundLoadStateChangeEvent event = new SoundLoadStateChangeEvent(sender);

    for (SoundHandler handler : this) {
      handler.onSoundLoadStateChange(event);
    }
  }
}
