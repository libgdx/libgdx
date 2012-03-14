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
import static com.allen_sauer.gwt.voices.client.Sound.LoadState.LOAD_STATE_SUPPORT_NOT_KNOWN;

import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.event.dom.client.EndedEvent;
import com.google.gwt.event.dom.client.EndedHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.media.client.Audio;
import com.google.gwt.user.client.ui.RootPanel;

import com.allen_sauer.gwt.voices.client.SoundController.MimeTypeSupport;

/**
 * Sound object representing sounds which can be played back via HTML5 audio.
 */
public class Html5Sound extends AbstractSound {
  // CHECKSTYLE_JAVADOC_OFF

  /**
   * @param mimeType the requested MIME type and optional codec according to RFC 4281
   * @return the level of support for the provided MIME type
   */
  public static MimeTypeSupport getMimeTypeSupport(String mimeType) {
    if (!Audio.isSupported()) {
      return MimeTypeSupport.MIME_TYPE_NOT_SUPPORTED;
    }
    String support = Audio.createIfSupported().getAudioElement().canPlayType(mimeType);
    assert support != null;
    if (AudioElement.CAN_PLAY_PROBABLY.equals(support)) {
      return MimeTypeSupport.MIME_TYPE_SUPPORT_READY;
    }
    if (AudioElement.CAN_PLAY_MAYBE.equals(support)) {
      return MimeTypeSupport.MIME_TYPE_SUPPORT_READY;
    }
    return MimeTypeSupport.MIME_TYPE_SUPPORT_UNKNOWN;
  }

  private Audio audio;

  private EndedHandler endedHandler = new EndedHandler() {
    @Override
    public void onEnded(EndedEvent event) {
      soundHandlerCollection.fireOnPlaybackComplete(Html5Sound.this);
    }
  };

  private HandlerRegistration endedRegistration;

  /**
   * @param mimeType the requested MIME type and optional codec according to RFC 4281
   * @param url the URL of the audio resource
   * @param streaming whether or not to stream the content, although currently ignored
   * @param crossOrigin whether or not the content is to be accessed from a different origin
   */
  public Html5Sound(String mimeType, String url, boolean streaming, boolean crossOrigin) {
    super(mimeType, url, streaming, crossOrigin);

    createAudioElement();

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
        throw new IllegalArgumentException("unexpected MIME type support " + mimeTypeSupport);
      default:
        throw new IllegalArgumentException("unknown MIME type support " + mimeTypeSupport);
    }
  }

  @Override
  public int getBalance() {
    // not implemented
    return SoundController.DEFAULT_BALANCE;
  }

  @Override
  public boolean getLooping() {
    return audio.getAudioElement().isLoop();
  }

  @Override
  public SoundType getSoundType() {
    return SoundType.HTML5;
  }

  @Override
  public int getVolume() {
    return (int) (audio.getAudioElement().getVolume() * 100d);
  }

  @Override
  public boolean play() {
    AudioElement elem = audio.getAudioElement();
    elem.pause();
    try {
      // IE9 has been seen to throw an exception here
      elem.setCurrentTime(0);
    } catch (Exception ignore) {
    }
    if (elem.getCurrentTime() != 0) {
      /*
       * Workaround Chrome's inability to play the same audio twice:
       * http://code.google.com/p/chromium/issues/detail?id=71323
       * http://code.google.com/p/chromium/issues/detail?id=75725
       */
      createAudioElement();
    }
    elem.play();
    // best guess is that the sound played, so return true
    return true;
  }

  @Override
  public void setBalance(int balance) {
    // not implemented
  }

  @Override
  public void setLooping(boolean looping) {
    audio.getAudioElement().setLoop(looping);
  }

  @Override
  public void setVolume(int volume) {
    assert volume >= 0;
    assert volume <= 100;
    audio.getAudioElement().setVolume(volume / 100d);
  }

  @Override
  public void stop() {
    audio.getAudioElement().pause();
  }

  private void createAudioElement() {
    if (endedRegistration != null) {
      endedRegistration.removeHandler();
    }
    if (audio != null) {
      // TODO: remove, once DOM attachment no longer required to sink (bitless) events
      audio.removeFromParent();
    }
    assert Audio.isSupported();
    audio = Audio.createIfSupported();
    assert audio != null;
    AudioElement elem = audio.getAudioElement();
    assert elem != null;

    endedRegistration = audio.addEndedHandler(endedHandler);

    // TODO: remove, once DOM attachment no longer required to sink (bitless) events
    RootPanel.get().add(audio);

    if (isCrossOrigin()) {
      elem.setAttribute("crossOrigin", "anonymous");
    }
    elem.setSrc(getUrl());
  }

}
