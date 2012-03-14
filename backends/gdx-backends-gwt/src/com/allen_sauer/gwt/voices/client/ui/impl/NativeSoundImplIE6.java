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

import static com.allen_sauer.gwt.voices.client.SoundController.MimeTypeSupport.MIME_TYPE_NOT_SUPPORTED;
import static com.allen_sauer.gwt.voices.client.SoundController.MimeTypeSupport.MIME_TYPE_SUPPORT_READY;

import com.google.gwt.dom.client.Element;

import com.allen_sauer.gwt.voices.client.Sound;
import com.allen_sauer.gwt.voices.client.SoundController.MimeTypeSupport;
import com.allen_sauer.gwt.voices.client.util.StringUtil;

import java.util.HashMap;

/**
 * {@link com.allen_sauer.gwt.voices.client.NativeSound} implementation for IE.
 */
public class NativeSoundImplIE6 extends NativeSoundImpl {
  /**
   * List based on <a href='http://support.microsoft.com/kb/297477'>How to apply a background sound
   * to a Web page in FrontPage</a> knowledge base article.
   */
  @SuppressWarnings("deprecation")
  private static final String[] BGSOUND_SUPPORTED_MIME_TYPES = {
      Sound.MIME_TYPE_AUDIO_X_AIFF, Sound.MIME_TYPE_AUDIO_BASIC, Sound.MIME_TYPE_AUDIO_X_MIDI,
      Sound.MIME_TYPE_AUDIO_MPEG, Sound.MIME_TYPE_AUDIO_X_WAV,};

  // CHECKSTYLE_JAVADOC_OFF
  private static final String[] MIME_TYPES_BGSOUND_NO_VOLUME_CONTROL = {Sound.MIME_TYPE_AUDIO_X_MIDI,};

  private static final String[] MIME_TYPES_ONE_AT_ATIME = {Sound.MIME_TYPE_AUDIO_X_MIDI,};

  private static final HashMap<String, Element> oneAtATimeElements = new HashMap<String, Element>();

  @Override
  public native Element createElement(String url)
  /*-{
    var elem = $doc.createElement("bgsound");
    elem.src = url;
    // elem.loop = 1; // -1 = infinitely, 0 = one time, n = number of times
    return elem;
  }-*/;

  @Override
  public MimeTypeSupport getMimeTypeSupport(String mimeType) {
    return StringUtil.contains(BGSOUND_SUPPORTED_MIME_TYPES, mimeType) ? MIME_TYPE_SUPPORT_READY
        : MIME_TYPE_NOT_SUPPORTED;
  }

  @Override
  public boolean play(Element soundControllerElement, Element elem, String mimeType) {
    Element currentElement = oneAtATimeElements.remove(mimeType);
    if (currentElement != null) {
      stop(currentElement);
    }
    if (StringUtil.contains(MIME_TYPES_ONE_AT_ATIME, mimeType)) {
      oneAtATimeElements.put(mimeType, elem);
    }
    return super.play(soundControllerElement, elem, mimeType);
  }

  @Override
  public void preload(Element soundControllerElement, String mimeType, String url) {
    if (!StringUtil.contains(MIME_TYPES_BGSOUND_NO_VOLUME_CONTROL, mimeType)) {
      super.preload(soundControllerElement, mimeType, url);
    }
  }

  /**
   * Best guess at conversion formula from standard -100 .. 100 range to -10000 .. 10000 range used
   * by IE.
   *
   * TODO location documentation for IE
   */
  @Override
  public native void setBalance(Element elem, int balance)
  /*-{
    if (balance == -100) {
      balance = -10000;
    } else if (balance == 100) {
      balance = 10000;
    } else if (balance < 0) {
      balance = 100 - 10000 / (100 + balance);
    } else {
      balance = 10000 / (100 - balance) - 100;
    }
    elem.balance = "" + balance; // -10000 .. 10000
  }-*/;

  /**
   * Best guess at conversion formula from standard 0 .. 100 range to -10000 .. 0 range used by IE.
   *
   * TODO location documentation for IE
   */
  @Override
  public native void setVolume(Element elem, int volume)
  /*-{
    elem.volume = volume == 0 ? -10000 : (-10000 / volume); // -10000 .. 0
  }-*/;
}