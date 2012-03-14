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

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

import com.allen_sauer.gwt.voices.client.ui.FlashMovie;
import com.allen_sauer.gwt.voices.client.ui.VoicesMovie;
import com.allen_sauer.gwt.voices.client.util.DOMUtil;

/**
 * Main class with which client code interact in order to create {@link Sound}
 * objects, which can be played. In addition, each SoundController defines its
 * own default volume and provides the ability to prioritize Flash based sound.
 *
 * <p>
 * For the time being do not create 16 or more SoundControllers as that would
 * result in 16+ Flash Players, which triggers an Adobe bug, mentioned <a
 * href="http://bugzilla.mozilla.org/show_bug.cgi?id=289873#c41">here</a>.
 */
public class SoundController {
  /**
   * Enumeration for varying levels of MIME type support.
   */
  public enum MimeTypeSupport {
    /**
     * Play back of the MIME type is known to NOT be supported in this browser,
     * based on known capabilities of browsers with the same user agent and
     * installed plugins.
     */
    MIME_TYPE_NOT_SUPPORTED,

    /**
     * Play back of the MIME type is known to be supported in this browser,
     * based on known capabilities of browsers with the same user agent and
     * installed plugins, but this capability has not yet been initialized.
     * Usually this is due to a browser plugin, such as <a href=
     * 'http://www.adobe.com/products/flashplayer/'>Adobe&nbsp;Flash&nbsp;Player
     * < / a > , <a href=
     * 'http://www.apple.com/quicktime/download/'>Apple&nbsp;QuickTime</a> or <a
     * href=
     * 'http://www.microsoft.com/windows/windowsmedia/'>Windows&nbsp;Media&nbsp;Playe
     * r < / a > .
     */
    MIME_TYPE_SUPPORT_NOT_READY,

    /**
     * Play back of the MIME type is known to be supported in this browser,
     * based on known capabilities of browsers with the same user agent and
     * installed plugins.
     */
    MIME_TYPE_SUPPORT_READY,

    /**
     * It is unknown (cannot be determined) whether play back of the MIME type
     * is supported in this browser, based on known capabilities of browsers
     * with the same user agent and installed plugins.
     */
    MIME_TYPE_SUPPORT_UNKNOWN,
  }

  static final int DEFAULT_BALANCE = 0;
  static final boolean DEFAULT_LOOPING = false;
  static final int DEFAULT_VOLUME = 100;

  static {
    setVersion();
  }

  private static native void setVersion()
  /*-{
    $wnd.$GWT_VOICES_VERSION = "@GWT_VOICES_VERSION@";
  }-*/;

  /**
   * Our DOM sound container which is positioned off screen.
   */
  protected final Element soundContainer = DOM.createDiv();
  private int defaultVolume = DEFAULT_VOLUME;
  private SoundType[] preferredSoundTypes;
  private VoicesMovie voicesWrapper;

  /**
   * We avoid using the usual, module relative {@link GWT#getModuleBaseURL()} to
   * force {@literal gwt-voices.swf} to be loaded from the same domain as the
   * host page. This avoids cross origin issues when the page contains a
   * {@code <base href="http://...">} tag. To override the base URL in your
   * code, use {@link SoundController#setGwtVoicesSwfLocation(String)}.
   */
  private String gwtVoicesSwfBaseUrl = GWT.getHostPageBaseURL() + GWT.getModuleName() + "/";

  /**
   * Default constructor to be used by client code.
   */
  public SoundController() {
    initSoundContainer();
  }

  /**
   * Create a new Sound object using the provided MIME type and URL. To enable
   * streaming, or for cross origin content, use {@link #createSound(String, String, boolean, boolean)}.
   *
   * @param mimeType MIME type of the new Sound object
   * @param url location of the new Sound object
   * @return a new Sound object
   */
  public Sound createSound(String mimeType, String url) {
    return createSound(mimeType, url, false, false);
  }

  /**
   * Create a new Sound object using the provided MIME type and URL.
   *
   * @param mimeType MIME type of the new Sound object
   * @param url location of the new Sound object
   * @param streaming whether or not to allow play back to start before sound
   *          has been fully downloaded
   * @param crossOrigin true of the resource is cross origin, false for same origin
   * @return a new Sound object
   */
  public Sound createSound(String mimeType, String url, boolean streaming, boolean crossOrigin) {
    Sound sound = createSoundImpl(mimeType, url, streaming, crossOrigin);
    sound.setVolume(defaultVolume);
    return sound;
  }

  /**
   * Determine the current preferred sound types for new sounds.
   *
   * @return the current preferred sound types
   *
   * @deprecated this method will be removed in a future release
   */
  @Deprecated
  public SoundType[] getPreferredSoundTypes() {
    return preferredSoundTypes;
  }

  /**
   * Set the default volume (range <code>0-100</code>) for new sound.
   *
   * @param defaultVolume the default volume (range <code>0-100</code>) to be
   *          used for new sounds
   */
  public void setDefaultVolume(int defaultVolume) {
    this.defaultVolume = defaultVolume;
  }

  /**
   * Set preferred {@link Sound} class: {@link Html5Sound}, {@link FlashSound},
   * {@link NativeSound}.
   *
   * @param soundTypes the preferred sound types
   *
   * @deprecated this method is a temporary stop-gap, may be retired at any
   *             time, and may be made to do nothing at all without warning
   */
  @Deprecated
  public void setPreferredSoundTypes(SoundType... soundTypes) {
    for (SoundType s : soundTypes) {
      assert s != null;
    }
    preferredSoundTypes = soundTypes;
  }

  /**
   * Provides a way to set the base URL for the {@literal gwt-voices.swf} file.
   * The provided base URL must ends with a trailing {@literal /}.
   *
   * @param gwtVoicesSwfBaseUrl base URL relative to which
   *          {@literal gwt-voices.swf} can be found
   */
  public void setGwtVoicesSwfLocation(String gwtVoicesSwfBaseUrl) {
    assert gwtVoicesSwfBaseUrl.endsWith("/");
    this.gwtVoicesSwfBaseUrl = gwtVoicesSwfBaseUrl;
  }

  /**
   * Lazily instantiate Flash Movie so browser plug-in is not unnecessarily
   * triggered.
   *
   * @return the new movie widget
   */
  protected VoicesMovie getVoicesMovie() {
    if (voicesWrapper == null) {
      voicesWrapper = new VoicesMovie(DOMUtil.getUniqueId(), gwtVoicesSwfBaseUrl);
      DOM.appendChild(soundContainer, voicesWrapper.getElement());
    }
    return voicesWrapper;
  }

  private Sound createSoundImplHtml5(String mimeType, String url, boolean streaming,
      boolean crossOrigin) {
    if (Html5Sound.getMimeTypeSupport(mimeType) == MimeTypeSupport.MIME_TYPE_SUPPORT_READY) {
      return new Html5Sound(mimeType, url, streaming, crossOrigin);
    }
    return null;
  }

  private Sound createSoundImpl(String mimeType, String url, boolean streaming, boolean crossOrigin) {
    Sound sound = null;
    for (SoundType c : preferredSoundTypes) {
      switch(c) {
        case HTML5:
          sound = createSoundImplHtml5(mimeType, url, streaming, crossOrigin);
          break;
        case FLASH:
          sound = createSoundImplFlash(mimeType, url, streaming, crossOrigin);
          break;
        case NATIVE:
          sound = createSoundImplWebAudio(mimeType, url, streaming, crossOrigin);
          break;
        case WEB_AUDIO:
          sound = createSoundImplWebAudio(mimeType, url, streaming, crossOrigin);
          break;
      }
      if (sound != null) {
        return sound;
      }
    }

    // Web Audio rocks
    sound = createSoundImplWebAudio(mimeType, url, streaming, crossOrigin);
    if (sound != null) {
      return sound;
    }

    sound = createSoundImplFlash(mimeType, url, streaming, crossOrigin);
    if (sound != null) {
      return sound;
    }

    sound = createSoundImplHtml5(mimeType, url, streaming, crossOrigin);
    if (sound != null) {
      return sound;
    }

    // Fallback to native browser audio
    sound = new NativeSound(mimeType, url, streaming, crossOrigin, soundContainer);
    return sound;
  }

  private Sound createSoundImplWebAudio(String mimeType, String url, boolean streaming,
      boolean crossOrigin) {
    if (WebAudioSound.getMimeTypeSupport(mimeType) == MimeTypeSupport.MIME_TYPE_SUPPORT_READY) {
      return new WebAudioSound(mimeType, url, streaming, crossOrigin);
    }
    return null;
  }

  private FlashSound createSoundImplFlash(String mimeType, String url, boolean streaming,
      boolean crossOrigin) {
    if (url.startsWith("data:")) {
      return null;
    }
    if (FlashMovie.isExternalInterfaceSupported()) {
      VoicesMovie vm = getVoicesMovie();
      MimeTypeSupport mimeTypeSupport = vm.getMimeTypeSupport(mimeType);
      if (mimeTypeSupport == MimeTypeSupport.MIME_TYPE_SUPPORT_READY
          || mimeTypeSupport == MimeTypeSupport.MIME_TYPE_SUPPORT_NOT_READY) {
        FlashSound sound = new FlashSound(mimeType, url, streaming, crossOrigin, vm);
        return sound;
      }
    }
    return null;
  }

  private void initSoundContainer() {
    String gwtVoices = Window.Location.getParameter(SoundType.QUERY_PARAMETER_NAME);
    if (SoundType.FLASH.getQueryParameterValue().equals(gwtVoices)) {
      setPreferredSoundTypes(SoundType.FLASH);
    } else if (SoundType.HTML5.getQueryParameterValue().equals(gwtVoices)) {
      setPreferredSoundTypes(SoundType.HTML5);
    } else if (SoundType.WEB_AUDIO.getQueryParameterValue().equals(gwtVoices)) {
      setPreferredSoundTypes(SoundType.WEB_AUDIO);
    } else if (SoundType.NATIVE.getQueryParameterValue().equals(gwtVoices)) {
      setPreferredSoundTypes(SoundType.NATIVE);
    } else {
      // For now, prefer Flash over HTML5
      setPreferredSoundTypes(SoundType.WEB_AUDIO, SoundType.FLASH, SoundType.HTML5);
    }

    // place off screen with fixed dimensions and overflow:hidden
    RootPanel.getBodyElement().appendChild(soundContainer);
    Style style = soundContainer.getStyle();
    style.setPosition(Position.ABSOLUTE);
    style.setOverflow(Overflow.HIDDEN);
    style.setLeft(-500, Unit.PX);
    style.setTop(-500, Unit.PX);
    style.setWidth(0, Unit.PX);
    style.setHeight(0, Unit.PX);
  }
}
