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
package com.allen_sauer.gwt.voices.client;

import static com.allen_sauer.gwt.voices.client.Sound.LoadState.LOAD_STATE_NOT_SUPPORTED;
import static com.allen_sauer.gwt.voices.client.Sound.LoadState.LOAD_STATE_SUPPORTED_MAYBE_READY;
import static com.allen_sauer.gwt.voices.client.Sound.LoadState.LOAD_STATE_SUPPORTED_NOT_READY;
import static com.allen_sauer.gwt.voices.client.Sound.LoadState.LOAD_STATE_SUPPORT_NOT_KNOWN;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;

import com.allen_sauer.gwt.voices.client.SoundController.MimeTypeSupport;
import com.allen_sauer.gwt.voices.client.ui.impl.NativeSoundImpl;

/**
 * Sound object representing sounds which can be played back natively by the browser, i.e. without
 * additional plugins.
 */
public class NativeSound extends AbstractSound {
  // CHECKSTYLE_JAVADOC_OFF

  protected static NativeSoundImpl impl;
  static {
    impl = (NativeSoundImpl) GWT.create(NativeSoundImpl.class);
  }

  public static MimeTypeSupport getMimeTypeSupport(String mimeType) {
    return impl.getMimeTypeSupport(mimeType);
  }

  private int balance = SoundController.DEFAULT_BALANCE;

  private Element element;

  private final String mimeType;

  private final Element soundControllerElement;

  private int volume = SoundController.DEFAULT_VOLUME;

  public NativeSound(String mimeType, String url, boolean streaming, boolean crossOrigin,
      Element soundControllerElement) {
    super(mimeType, url, streaming, crossOrigin);

    this.soundControllerElement = soundControllerElement;
    this.mimeType = mimeType;
    // TODO: determine whether requests can be cross origin
    impl.preload(soundControllerElement, mimeType, url);
    element = impl.createElement(url);

    MimeTypeSupport mimeTypeSupport = getMimeTypeSupport(mimeType);
    switch (mimeTypeSupport) {
      case MIME_TYPE_SUPPORT_READY:
        setLoadState(LOAD_STATE_SUPPORTED_MAYBE_READY);
        break;
      case MIME_TYPE_NOT_SUPPORTED:
        setLoadState(LOAD_STATE_NOT_SUPPORTED);
        break;
      case MIME_TYPE_SUPPORT_UNKNOWN:
        setLoadState(LOAD_STATE_SUPPORT_NOT_KNOWN);
        break;
      case MIME_TYPE_SUPPORT_NOT_READY:
        setLoadState(LOAD_STATE_SUPPORTED_NOT_READY);
        break;
      default:
        throw new IllegalArgumentException("unknown MIME type support " + mimeTypeSupport);
    }
  }

  @Override
  public int getBalance() {
    return balance;
  }

  @Override
  public boolean getLooping() {
    return impl.getLooping(element);
  }

  @Override
  public SoundType getSoundType() {
    return SoundType.NATIVE;
  }

  @Override
  public int getVolume() {
    return volume;
  }

  @Override
  public boolean play() {
    return impl.play(soundControllerElement, element, mimeType);
  }

  @Override
  public void setBalance(int balance) {
    assert balance >= -100;
    assert balance <= 100;
    this.balance = balance;
    impl.setBalance(element, balance);
  }

  @Override
  public void setLooping(boolean looping) {
    impl.setLooping(element, looping);
  }

  @Override
  public void setVolume(int volume) {
    assert volume >= 0;
    assert volume <= 100;
    this.volume = volume;
    impl.setVolume(element, volume);
  }

  @Override
  public void stop() {
    impl.stop(element);
  }

}
