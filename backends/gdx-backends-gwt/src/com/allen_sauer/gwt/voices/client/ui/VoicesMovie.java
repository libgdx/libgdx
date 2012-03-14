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
package com.allen_sauer.gwt.voices.client.ui;

import static com.allen_sauer.gwt.voices.client.Sound.LoadState.LOAD_STATE_NOT_SUPPORTED;
import static com.allen_sauer.gwt.voices.client.SoundController.MimeTypeSupport.MIME_TYPE_NOT_SUPPORTED;
import static com.allen_sauer.gwt.voices.client.SoundController.MimeTypeSupport.MIME_TYPE_SUPPORT_NOT_READY;
import static com.allen_sauer.gwt.voices.client.SoundController.MimeTypeSupport.MIME_TYPE_SUPPORT_READY;
import static com.allen_sauer.gwt.voices.client.SoundController.MimeTypeSupport.MIME_TYPE_SUPPORT_UNKNOWN;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

import com.allen_sauer.gwt.voices.client.FlashSound;
import com.allen_sauer.gwt.voices.client.Sound;
import com.allen_sauer.gwt.voices.client.SoundController.MimeTypeSupport;
import com.allen_sauer.gwt.voices.client.util.StringUtil;

import java.util.ArrayList;
import java.util.Iterator;

// CHECKSTYLE_JAVADOC_OFF
public class VoicesMovie extends FlashMovie {

  @SuppressWarnings("deprecation")
  private static final String[] FLASH_SUPPORTED_MIME_TYPES = {
      Sound.MIME_TYPE_AUDIO_MPEG, Sound.MIME_TYPE_AUDIO_MPEG_MP3,};
  private static final String GWT_VOICES_SWF = "gwt-voices.swf";


  private MimeTypeSupport flashSupport = MIME_TYPE_SUPPORT_UNKNOWN;
  private final ArrayList<FlashSound> unitializedSoundList = new ArrayList<FlashSound>();

  public VoicesMovie(String id, String gwtVoicesSwfBaseUrl) {
    super(id, gwtVoicesSwfBaseUrl + GWT_VOICES_SWF);
    installFlashCallbackHooks(id);

    // Flash Player version check for ExternalInterface support
    if (isExternalInterfaceSupported()) {
      flashSupport = MIME_TYPE_SUPPORT_NOT_READY;
    } else {
      flashSupport = MIME_TYPE_NOT_SUPPORTED;
      Scheduler.get().scheduleDeferred(new ScheduledCommand() {
        @Override
        public void execute() {
          movieUnsupported();
        }
      });
    }
  }

  public MimeTypeSupport getMimeTypeSupport(String mimeType) {
    switch (flashSupport) {
      case MIME_TYPE_SUPPORT_READY:
      case MIME_TYPE_SUPPORT_NOT_READY:
        return StringUtil.contains(FLASH_SUPPORTED_MIME_TYPES, mimeType) ? MIME_TYPE_SUPPORT_READY
            : MIME_TYPE_NOT_SUPPORTED;
      case MIME_TYPE_SUPPORT_UNKNOWN:
      case MIME_TYPE_NOT_SUPPORTED:
        return flashSupport;
      default:
        throw new RuntimeException("Unhandled flash support value " + flashSupport);
    }
  }

  public boolean playSound(int id) {
    if (flashSupport == MIME_TYPE_SUPPORT_READY) {
      return callPlaySound(id);
    }
    // the sound was not played, return false
    return false;
  }

  public void registerSound(FlashSound flashSound) {
    if (flashSupport == MIME_TYPE_SUPPORT_READY) {
      doCreateSound(flashSound);
    } else {
      unitializedSoundList.add(flashSound);
    }
  }

  public void setBalance(int id, int balance) {
    assert balance >= -100;
    assert balance <= 100;
    if (flashSupport == MIME_TYPE_SUPPORT_READY) {
      callSetPanning(id, balance / 100f);
    }
  }

  public void setLooping(int id, boolean looping) {
    if (flashSupport == MIME_TYPE_SUPPORT_READY) {
      callSetLooping(id, looping ? Integer.MAX_VALUE : 0);
    }
  }

  public void setVolume(int id, int volume) {
    assert volume >= 0;
    assert volume <= 100;
    if (flashSupport == MIME_TYPE_SUPPORT_READY) {
      callSetVolume(id, volume / 100f);
    }
  }

  public void stopSound(int id) {
    if (flashSupport == MIME_TYPE_SUPPORT_READY) {
      callStopSound(id);
    }
  }

  protected void debug(String text) {
    System.out.println(text);
    if (!GWT.isProdMode()) {
      consoleDebug(text);
    }
  }

  private native void callCreateSound(int id, String soundURL, boolean checkPolicyFile) /*-{
    var elem = this.@com.allen_sauer.gwt.voices.client.ui.FlashMovie::element;
    elem.createSound(id, soundURL, checkPolicyFile);
  }-*/;

  private native boolean callPlaySound(int id) /*-{
    var elem = this.@com.allen_sauer.gwt.voices.client.ui.FlashMovie::element;
    return elem.playSound(id);
  }-*/;

  private native void callSetLooping(int id, int looping) /*-{
    var elem = this.@com.allen_sauer.gwt.voices.client.ui.FlashMovie::element;
    elem.setLooping(id, looping);
  }-*/;

  private native void callSetPanning(int id, float panning) /*-{
    var elem = this.@com.allen_sauer.gwt.voices.client.ui.FlashMovie::element;
    elem.setPanning(id, panning);
  }-*/;

  private native void callSetVolume(int id, float volume) /*-{
    var elem = this.@com.allen_sauer.gwt.voices.client.ui.FlashMovie::element;
    elem.setVolume(id, volume);
  }-*/;

  private native void callStopSound(int id) /*-{
    var elem = this.@com.allen_sauer.gwt.voices.client.ui.FlashMovie::element;
    elem.stopSound(id);
  }-*/;

  private native void consoleDebug(String text) /*-{
    console.log(text);
  }-*/;

  private void doCreateSound(FlashSound flashSound) {
    callCreateSound(flashSound.getSoundNumber(), flashSound.getUrl(), flashSound.isCrossOrigin());
  }

  private native void installFlashCallbackHooks(String id) /*-{
    if ($doc.VoicesMovie === undefined) {
      $doc.VoicesMovie = {};
    }
    var self = this;
    $doc.VoicesMovie[id] = {};

    $doc.VoicesMovie[id].ready = function() {
      self.@com.allen_sauer.gwt.voices.client.ui.VoicesMovie::movieReady()();
    }

    $doc.VoicesMovie[id].soundLoaded = function(id) {
      @com.allen_sauer.gwt.voices.client.FlashSound::soundLoaded(I)(id);
      return true;
    }

    $doc.VoicesMovie[id].playbackCompleted = function(id) {
      @com.allen_sauer.gwt.voices.client.FlashSound::playbackCompleted(I)(id);
    }

    $doc.VoicesMovie[id].log = function(text) {
      self.@com.allen_sauer.gwt.voices.client.ui.VoicesMovie::debug(Ljava/lang/String;)("FLASH[" + id + "]: " + text);
    }
  }-*/;

  private void movieReady() {
    flashSupport = MIME_TYPE_SUPPORT_READY;
    for (Iterator<FlashSound> iterator = unitializedSoundList.iterator(); iterator.hasNext();) {
      FlashSound flashSound = iterator.next();
      doCreateSound(flashSound);
      iterator.remove();
    }
  }

  private void movieUnsupported() {
    for (FlashSound flashSound : unitializedSoundList) {
      flashSound.setLoadState(LOAD_STATE_NOT_SUPPORTED);
      // Flash plug-in may become available later; do not call iterator.remove()
    }
  }

}
