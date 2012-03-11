/*
 * Copyright 2010 Google Inc.
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
package com.google.gwt.corp.localstorage;


import java.io.IOException;

import com.google.gwt.core.client.JavaScriptException;

public class LocalStorage {

  public static String getItem(String key) throws IOException {
    try {
      return getItemImpl(key);
    } catch (JavaScriptException e) {
      throw new IOException("" + e);
    }
  }

  public static String key(int index) throws IOException {
    try {
      return keyImpl(index);
    } catch (JavaScriptException e) {
      throw new IOException("" + e);
    }
  }

  public static int length() throws IOException {
    try {
      return lengthImpl();
    } catch (JavaScriptException e) {
      throw new IOException("" + e);
    }
  }

  public static void removeItem(String key) throws IOException {
    try {
      removeItemImpl(key);
    } catch (JavaScriptException e) {
      throw new IOException("" + e);
    }
  }

  public static void setItem(String key, String value) throws IOException {
    try {
      setItemImpl(key, value);
    } catch (JavaScriptException e) {
      throw new IOException("" + e);
    }
  }

  private native static String getItemImpl(String key) /*-{
    return $wnd.localStorage.getItem(key);
  }-*/;

  private native static String keyImpl(int index) /*-{
    return $wnd.localStorage.key(index);
  }-*/;

  public native static int lengthImpl() /*-{
    return $wnd.localStorage.length;
  }-*/;

  public native static void setItemImpl(String key, String value) /*-{
    $wnd.localStorage.setItem(key, value);
  }-*/;

  public native static void removeItemImpl(String key) /*-{
		$wnd.localStorage.removeItem(key);
	}-*/;
}
