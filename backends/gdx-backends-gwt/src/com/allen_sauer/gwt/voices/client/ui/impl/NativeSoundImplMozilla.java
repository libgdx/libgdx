/*
 * Copyright 2010 Fred Sauer
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.allen_sauer.gwt.voices.client.ui.impl;

/**
 * {@link com.allen_sauer.gwt.voices.client.NativeSound} implementation for Mozilla.
 */
public class NativeSoundImplMozilla extends NativeSoundImplStandard {

  /**
   * Determine whether volume control is supported for the provided MIME type.
   *
   * Returns <code>false</code> if <code>navigator.mimeTypes[mimeType].enabledPlugin.filename</code>
   * ends with <code>wmp.so</code>, indicating the mplayer plugin on Linux, which does not support
   * volume control. Known strings to date are <code>gecko-mediaplayer-wmp.so</code> and
   * <code>mplayerplug-in-wmp.so</code>.
   *
   * @param mimeType the MIME type to test
   * @return true if the MIME type and enabled plugin is believed to provide volume support
   */
  @Override
  protected native boolean mimeTypeSupportsVolume(String mimeType)
  /*-{
    if (mimeType = @com.allen_sauer.gwt.voices.client.Sound::MIME_TYPE_AUDIO_X_MIDI) {
      // No MIDI volume support in Firefox 3.6
      // (Tested on OSX)
      return false;
    }
    var m = navigator.mimeTypes[mimeType];
    if (m != null && m.enabledPlugin != null && m.enabledPlugin.filename.search(/wmp.so$/) != -1) {
      return false;
    }
    return true;
  }-*/;

}
