/*
 * Copyright 2012 Fred Sauer
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
package com.allen_sauer.gwt.voices.client;

/**
 * Sound type implementations.
 */
public enum SoundType {
  /**
   * Flash Audio.
   */
  FLASH("flash"),

  /**
   * HTML5 Audio.
   */
  HTML5("html5"),

  /**
   * Native browser audio, e.g. <code>&lt;BGSOUND&gt;</code> and <code>&lt;OBJECT&gt;</code>.
   */
  NATIVE("native"),

  /**
   * Modern Web Audio API.
   */
  WEB_AUDIO("webaudio");
  
  /**
   * Query parameter name, which can be used to request a specific audio implementation
   * at runtime.
   */
  public static final String QUERY_PARAMETER_NAME = "gwt-voices";

  private final String queryParameterValue;

  private SoundType(String queryParameterValue) {
    this.queryParameterValue = queryParameterValue;
  }

  /**
   * Get the query parameter value which may be assigned to the {@literal gwt-voices} URL query
   * parameter.
   * 
   * @return value which may be used with the {@literal gwt-voices} URL query parameter
   */
  public String getQueryParameterValue() {
    return queryParameterValue;
  }
}
