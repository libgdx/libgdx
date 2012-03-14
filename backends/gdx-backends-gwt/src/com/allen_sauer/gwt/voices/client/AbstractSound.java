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

import com.allen_sauer.gwt.voices.client.handler.SoundHandler;
import com.allen_sauer.gwt.voices.client.handler.SoundHandlerCollection;
import com.allen_sauer.gwt.voices.client.handler.SoundLoadStateChangeEvent;

abstract class AbstractSound implements Sound {
  private static final LoadState INITIAL_LOAD_STATE = LoadState.LOAD_STATE_UNINITIALIZED;
  protected final SoundHandlerCollection soundHandlerCollection = new SoundHandlerCollection();

  private final boolean crossOrigin;
  private LoadState loadState = INITIAL_LOAD_STATE;
  private final String mimeType;
  private final boolean streaming;
  private final String url;

  public AbstractSound(String mimeType, String url, boolean streaming, boolean crossOrigin) {
    this.mimeType = mimeType;
    this.url = url;
    this.streaming = streaming;
    this.crossOrigin = crossOrigin;
  }

  @Override
  public final void addEventHandler(SoundHandler handler) {
    soundHandlerCollection.add(handler);
    if (loadState != INITIAL_LOAD_STATE) {
      handler.onSoundLoadStateChange(new SoundLoadStateChangeEvent(this));
    }
  }

  @Override
  public final LoadState getLoadState() {
    return loadState;
  }

  @Override
  public final String getMimeType() {
    return mimeType;
  }

  @Override
  public abstract SoundType getSoundType();

  @Override
  public final String getUrl() {
    return url;
  }

  public boolean isCrossOrigin() {
    return crossOrigin;
  }

  public final boolean isStreaming() {
    return streaming;
  }

  @Override
  public final void removeEventHandler(SoundHandler handler) {
    soundHandlerCollection.remove(handler);
  }

  public final void setLoadState(LoadState loadState) {
    if (loadState != this.loadState) {
      this.loadState = loadState;
      if (loadState != INITIAL_LOAD_STATE) {
        soundHandlerCollection.fireOnSoundLoadStateChange(this);
      }
    }
  }

  @Override
  public final String toString() {
    return getSoundType() + "(\"" + mimeType + "\", \"" + url + "\", "
        + (isStreaming() ? "streaming" : "not streaming") + ", "
        + (isCrossOrigin() ? "cross origin" : "same origin") + ")";
  }
}
