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

import java.util.EventListener;

/**
 * Implement this interface to receive events related to sound load states sound playback.
 */
public interface SoundHandler extends EventListener {
  /**
   * Fired when playback of a sound completes.
   * 
   * @param event the completion event
   */
  void onPlaybackComplete(PlaybackCompleteEvent event);

  /**
   * Fired when the load state changes.
   * 
   * @param event the load state completion event
   */
  void onSoundLoadStateChange(SoundLoadStateChangeEvent event);
}
