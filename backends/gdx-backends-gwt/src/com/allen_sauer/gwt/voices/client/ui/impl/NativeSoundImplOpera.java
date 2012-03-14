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

import static com.allen_sauer.gwt.voices.client.SoundController.MimeTypeSupport.MIME_TYPE_SUPPORT_READY;

import com.google.gwt.dom.client.Element;

import com.allen_sauer.gwt.voices.client.Sound;
import com.allen_sauer.gwt.voices.client.SoundController.MimeTypeSupport;
import com.allen_sauer.gwt.voices.client.util.StringUtil;

/**
 * {@link com.allen_sauer.gwt.voices.client.NativeSound} implementation for Opera.
 */
public class NativeSoundImplOpera extends NativeSoundImplStandard {
  // CHECKSTYLE_JAVADOC_OFF

  @SuppressWarnings("deprecation")
  private static final String[] SUPPORTED_WITHOUT_PLUGINS_MIME_TYPES = {Sound.MIME_TYPE_AUDIO_X_WAV,};

  @Override
  public MimeTypeSupport getMimeTypeSupport(String mimeType) {
    if (StringUtil.contains(SUPPORTED_WITHOUT_PLUGINS_MIME_TYPES, mimeType)) {
      return MIME_TYPE_SUPPORT_READY;
    }
    return operaGetMimeTypeSupport(mimeType);
  }

  @Override
  public native void preload(Element soundControllerElement, String mimeType, String url) /*-{
                                                                                          new Audio(url);
                                                                                          }-*/;

  private native MimeTypeSupport operaGetMimeTypeSupport(String mimeType) /*-{
                                                                          var m = navigator.mimeTypes[mimeType];
                                                                          // Note, m != null occurs in many browsers for well known MIME types
                                                                          // even though the MIME type is not supported without a plug-in
                                                                          return (m != null && m.description && m.description.length > 0)
                                                                          ? @com.allen_sauer.gwt.voices.client.SoundController.MimeTypeSupport::MIME_TYPE_SUPPORT_READY
                                                                          : @com.allen_sauer.gwt.voices.client.SoundController.MimeTypeSupport::MIME_TYPE_NOT_SUPPORTED;
                                                                          }-*/;
}
