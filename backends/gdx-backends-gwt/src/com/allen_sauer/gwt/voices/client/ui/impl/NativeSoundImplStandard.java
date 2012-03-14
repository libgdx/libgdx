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

import com.google.gwt.dom.client.Element;

import com.allen_sauer.gwt.voices.client.SoundController.MimeTypeSupport;

/**
 * {@link com.allen_sauer.gwt.voices.client.NativeSound} implementation for standard browsers.
 */
public abstract class NativeSoundImplStandard extends NativeSoundImpl {
  // CHECKSTYLE_JAVADOC_OFF

  @Override
  public native Element createElement(String url)
  /*-{
    var elem = $doc.createElement("object");
    elem.setAttribute("data", url);
    elem.setAttribute("autostart", "true");
    // setting hidden property prevents MIDI playback
    // elem.setAttribute("hidden", "true");
    return elem;
  }-*/;

  @Override
  public native MimeTypeSupport getMimeTypeSupport(String mimeType)
  /*-{
    var m = navigator.mimeTypes[mimeType];
    // Note, m != null occurs in many browsers for well known MIME types
    // even though the MIME type is not supported without a plug-in
    return (m != null && m.enabledPlugin != null)
        ? @com.allen_sauer.gwt.voices.client.SoundController.MimeTypeSupport::MIME_TYPE_SUPPORT_READY
        : @com.allen_sauer.gwt.voices.client.SoundController.MimeTypeSupport::MIME_TYPE_NOT_SUPPORTED;
  }-*/;

  @Override
  public native void setBalance(Element elem, int balance)
  /*-{
    // did not find any browsers actually supporting this
    elem.setAttribute("balance", "" + balance);
  }-*/;

  @Override
  public native void setVolume(Element elem, int volume)
  /*-{
    elem.setAttribute("volume", "" + volume);
  }-*/;
}
