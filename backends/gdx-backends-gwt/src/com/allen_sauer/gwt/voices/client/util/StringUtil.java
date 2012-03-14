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

// CHECKSTYLE_JAVADOC_OFF
public class StringUtil {
  public static boolean contains(String[] arr, String text) {
    for (String element : arr) {
      if (element.equals(text)) {
        return true;
      }
    }
    return false;
  }

  public static String getSimpleName(Class<?> clazz) {
    String name = clazz.getName();
    return name.substring(name.lastIndexOf('.') + 1);
  }
}
