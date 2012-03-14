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

import com.allen_sauer.gwt.voices.client.Sound;
import com.allen_sauer.gwt.voices.client.util.StringUtil;

import java.util.EventObject;

/**
 * Event object when play back of a sound completes.
 */
@SuppressWarnings("serial")
public class PlaybackCompleteEvent extends EventObject {
  /**
   * Constructor, used by {@link SoundHandlerCollection}.
   * 
   * @param source the {@link Sound} object which completed play back
   */
  public PlaybackCompleteEvent(Object source) {
    super(source);
  }

  /**
   * Returns the string <code>"PlaybackCompleteEvent: "</code> followed by
   * <code>sound.toString()</code>, whereby <code>sound</code> is the <code>source</code> of this
   * {@link EventObject}.
   * 
   * @return the string representation of this event
   */
  @Override
  public String toString() {
    Sound sound = (Sound) getSource();
    return StringUtil.getSimpleName(PlaybackCompleteEvent.class) + ": " + sound;
  }
}
