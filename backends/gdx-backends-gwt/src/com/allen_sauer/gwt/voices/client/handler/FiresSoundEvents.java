/*
 * Copyright 2009 Fred Sauer
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
package com.allen_sauer.gwt.voices.client.handler;

/**
 * Interface for classes wishing to fire sound events.
 */
public interface FiresSoundEvents {
  /**
   * Register a new sound handler.
   * 
   * @param handler sound handler to register
   */
  void addEventHandler(SoundHandler handler);

  /**
   * Remove a previously registered sound handler.
   * 
   * @param handler sound handler to unregister
   */
  void removeEventHandler(SoundHandler handler);
}
