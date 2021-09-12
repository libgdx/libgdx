/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.backends.gwt;

import com.google.gwt.core.client.JsArrayString;

import static com.badlogic.gdx.backends.gwt.GwtUtils.toStringArray;

/** Implementation of the <a href="https://w3c.github.io/webappsec-feature-policy/#featurepolicy">Feature Policy Interface</a> */
class GwtFeaturePolicy {

	static native boolean isSupported () /*-{
		return "featurePolicy" in $wnd.document;
	}-*/;

	static native boolean allowsFeature (String feature) /*-{
		if (!@com.badlogic.gdx.backends.gwt.GwtFeaturePolicy::isSupported()()) return true;
		return $wnd.document.featurePolicy.allowsFeature(feature);
	}-*/;

	static native boolean allowsFeature (String feature, String origin) /*-{
		if (!@com.badlogic.gdx.backends.gwt.GwtFeaturePolicy::isSupported()()) return true;
		return $wnd.document.featurePolicy.allowsFeature(feature, origin);
	}-*/;

	static private native JsArrayString JSfeatures () /*-{
		return $wnd.document.featurePolicy.features();
	}-*/;

	static String[] features () {
		if (GwtFeaturePolicy.isSupported())
			return toStringArray(JSfeatures());
		else
			return null;
	}

	static private native JsArrayString JSallowedFeatures () /*-{
		return $wnd.document.featurePolicy.allowedFeatures();
	}-*/;

	static String[] allowedFeatures () {
		if (GwtFeaturePolicy.isSupported())
			return toStringArray(JSallowedFeatures());
		else
			return null;
	}

	static private native JsArrayString JSgetAllowlistForFeature (String feature) /*-{
		return $wnd.document.featurePolicy.getAllowlistForFeature(feature);
	}-*/;

	static String[] getAllowlistForFeature (String feature) {
		if (GwtFeaturePolicy.isSupported())
			return toStringArray(JSgetAllowlistForFeature(feature));
		else
			return null;
	}
}
