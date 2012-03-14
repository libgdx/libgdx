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
package com.allen_sauer.gwt.voices.client.util;

import com.google.gwt.user.client.Element;

// CHECKSTYLE_JAVADOC_OFF
public class DOMUtil {
  private static int uniqueId = 1000;

  public static native String getNodeName(Element elem)
  /*-{
    return elem.nodeName;
  }-*/;

  public static String getUniqueId() {
    return "gwtVoices" + uniqueId++;
  }
}
