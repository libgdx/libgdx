/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.badlogic.gdx.i18n;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.utils.TimeUtils;

/** <b>NOTE:</b> This class mimics {@code java.util.ResourceBundle} API but doesn't support multiple class loaders, which is an
 * unnecessary feature for our purpose. Also, this class is intended to be cross-platform, while the support for multiple class
 * loaders requires native calls. That's why we've decided to definitely drop this feature.
 * 
 * <p>
 * {@code ResourceBundle} is an abstract class which is the superclass of classes which provide {@code Locale}-specific resources.
 * A bundle contains a number of named resources, where the names are {@code Strings}. A bundle may have a parent bundle, and when
 * a resource is not found in a bundle, the parent bundle is searched for the resource. If the fallback mechanism reaches the base
 * bundle and still can't find the resource it throws a {@code MissingResourceException}.
 * 
 * <ul>
 * <li>All bundles for the same group of resources share a common base bundle. This base bundle acts as the root and is the last
 * fallback in case none of its children was able to respond to a request.</li>
 * <li>The first level contains changes between different languages. Only the differences between a language and the language of
 * the base bundle need to be handled by a language-specific {@code ResourceBundle}.</li>
 * <li>The second level contains changes between different countries that use the same language. Only the differences between a
 * country and the country of the language bundle need to be handled by a country-specific {@code ResourceBundle}.</li>
 * <li>The third level contains changes that don't have a geographic reason (e.g. changes that where made at some point in time
 * like {@code PREEURO} where the currency of come countries changed. The country bundle would return the current currency (Euro)
 * and the {@code PREEURO} variant bundle would return the old currency (e.g. DM for Germany).</li>
 * </ul>
 * 
 * <strong>Examples</strong>
 * <ul>
 * <li>BaseName (base bundle)
 * <li>BaseName_de (german language bundle)
 * <li>BaseName_fr (french language bundle)
 * <li>BaseName_de_DE (bundle with Germany specific resources in german)
 * <li>BaseName_de_CH (bundle with Switzerland specific resources in german)
 * <li>BaseName_fr_CH (bundle with Switzerland specific resources in french)
 * <li>BaseName_de_DE_PREEURO (bundle with Germany specific resources in german of the time before the Euro)
 * <li>BaseName_fr_FR_PREEURO (bundle with France specific resources in french of the time before the Euro)
 * </ul>
 * 
 * It's also possible to create variants for languages or countries. This can be done by just skipping the country or language
 * abbreviation: BaseName_us__POSIX or BaseName__DE_PREEURO. But it's not allowed to circumvent both language and country:
 * BaseName___VARIANT is illegal.
 * 
 * @see Properties
 * @see PropertyResourceBundle */
public abstract class ResourceBundle {

	/** The parent of this {@code ResourceBundle} that is used if this bundle doesn't include the requested resource. */
	protected ResourceBundle parent;

	/** The locale for this bundle. */
	private Locale locale;

	/** The flag indicating this bundle has expired in the cache. */
	private volatile boolean expired;

	/** The key of this bundle in the cache. */
	private volatile BundleKey bundleKey;

	/** A Set of the keys contained only in this ResourceBundle. The set is cached in this ResourceBundle in order to avoid creating
	 * a new Set each time handleKeySet is called. */
	private volatile Set<String> keySet;

	/** Constant representing a non existent bundle */
	private static final ResourceBundle MISSING_BUNDLE = new ResourceBundle() {
		@Override
		public Enumeration<String> getKeys () {
			return null;
		}

		@Override
		protected Object handleGetObject (String key) {
			return null;
		}

		@Override
		public String toString () {
			return "MISSING_BUNDLE";
		}
	};

	/** The cache is a map from cache keys (with bundle base name, locale, and auxiliary id) to either a resource bundle or
	 * MISSING_BUNDLE wrapped by a BundleReference. */
	private static final ConcurrentMap<BundleKey, BundleReference> cache = new ConcurrentHashMap<BundleKey, BundleReference>(32);

	private static final ReferenceQueue referenceQueue = new ReferenceQueue();

	/** Constructs a new instance of this class. */
	public ResourceBundle () {
		/* empty */
	}

	/** Gets a resource bundle using the specified base name, the default locale, and the default control
	 * {@link PropertyFileControl#INTERNAL_PROPERTIES_ONLY}.
	 * 
	 * @param baseName the base name of the resource bundle
	 * @exception NullPointerException if <code>baseName</code> is <code>null</code>
	 * @exception MissingResourceException if no resource bundle for the specified base name can be found
	 * @return a resource bundle for the given base name and the default locale */
	public static ResourceBundle getBundle (String baseName) {
		return getBundleImpl(baseName, Locale.getDefault(), PropertyFileControl.INTERNAL_PROPERTIES_ONLY);
	}

	/** Gets a resource bundle using the specified base name and locale, and the default control
	 * {@link PropertyFileControl#INTERNAL_PROPERTIES_ONLY}.
	 * 
	 * @param baseName the base name of the resource bundle
	 * @param locale the locale for which a resource bundle is desired
	 * @return a resource bundle for the given base name and locale
	 * @exception java.lang.NullPointerException if <code>baseName</code>, <code>locale</code>, or <code>loader</code> is
	 *               <code>null</code>
	 * @exception MissingResourceException if no resource bundle for the specified base name can be found */
	public static ResourceBundle getBundle (String baseName, Locale locale) {
		return getBundleImpl(baseName, locale, PropertyFileControl.INTERNAL_PROPERTIES_ONLY);
	}

	/** Returns a resource bundle using the specified base name, the default locale and the specified control.
	 * 
	 * @param baseName the base name of the resource bundle
	 * @param control the control which gives information for the resource bundle loading process
	 * @return a resource bundle for the given base name and the default locale
	 * @exception NullPointerException if <code>baseName</code> or <code>control</code> is <code>null</code>
	 * @exception MissingResourceException if no resource bundle for the specified base name can be found
	 * @exception IllegalArgumentException if the given <code>control</code> doesn't perform properly (e.g.,
	 *               <code>control.getCandidateLocales</code> returns null.) */
	public static ResourceBundle getBundle (String baseName, Control control) {
		return getBundleImpl(baseName, Locale.getDefault(), control);
	}

	/** Returns a resource bundle using the specified base name, target locale and control.
	 * 
	 * @param baseName the base name of the resource bundle
	 * @param targetLocale the locale for which a resource bundle is desired
	 * @param control the control which gives information for the resource bundle loading process
	 * @return a resource bundle for the given base name and locale
	 * @exception NullPointerException if <code>baseName</code>, <code>targetLocale</code>, <code>loader</code>, or
	 *               <code>control</code> is <code>null</code>
	 * @exception MissingResourceException if no resource bundle for the specified base name can be found
	 * @exception IllegalArgumentException if the given <code>control</code> doesn't perform properly (e.g.,
	 *               <code>control.getCandidateLocales</code> returns null.) */
	public static ResourceBundle getBundle (String baseName, Locale targetLocale, Control control) {
		return getBundleImpl(baseName, targetLocale, control);
	}

	private static ResourceBundle getBundleImpl (String baseName, Locale locale, Control control) {
		if (locale == null || control == null) throw new NullPointerException();

		// Create a new bundle key. Only its locale will change during the loading process
		BundleKey bundleKey = new BundleKey(baseName, locale, control.getAuxId(baseName));

		// Lookup the bundle
		ResourceBundle bundle = lookupBundle(bundleKey);
		if (isValidBundle(bundle) && hasValidParentChain(bundle)) {
			// Return the valid bundle coming from the cache
			return bundle;
		}

		// Checks the requested formats
		List<String> formats = control.getFormats(baseName);
		if (!isValidList(formats)) {
			throw new IllegalArgumentException("Control with invalid formats");
		}

		// Load the bundle and its parents
		ResourceBundle baseBundle = null;
		Locale targetLocale = locale;
		do {
			// Check the candidate locales
			List<Locale> candidateLocales = control.getCandidateLocales(baseName, targetLocale);
			if (!isValidList(candidateLocales)) {
				throw new IllegalArgumentException("Control with invalid candidate locales");
			}

			bundle = findBundle(bundleKey, candidateLocales, formats, 0, control, baseBundle);

			// Check the loaded bundle (if any)
			if (isValidBundle(bundle)) {
				boolean isBaseBundle = bundle.locale.equals(Locale.ROOT);

				if (!isBaseBundle || bundle.locale.equals(locale)) {
					// Return the bundle for the requested locale
					return bundle;
				}
				if (candidateLocales.size() == 1 && bundle.locale.equals(candidateLocales.get(0))) {
					// Return the bundle for the sole candidate locale
					return bundle;
				}
				if (isBaseBundle && baseBundle == null) {
					// Store the base bundle and keep on processing the remaining fallback locales
					baseBundle = bundle;
				}
			}

			// Set next fallback locale
			targetLocale = control.getFallbackLocale(baseName, targetLocale);

		} while (targetLocale != null);

		if (bundle != null) {
			// Return the base bundle
			return bundle;
		}
		if (baseBundle == null) {
			// No resource bundle found
			throwMissingResourceException(baseName, locale, bundleKey.getThrowable());
		}

		// Return the base bundle
		return baseBundle;
	}

	/** Returns the bundle from the cache; null if the bundle is not found. Be aware that the found bundle may be a MISSING_BUNDLE. */
	private static final ResourceBundle lookupBundle (BundleKey bundleKey) {
		BundleReference bundleReference = cache.get(bundleKey);
		if (bundleReference == null) return null;
		return bundleReference.get();
	}

	/** A bundle is valid if it's neither null nor the MISSING_BUNDLE. */
	private static final boolean isValidBundle (ResourceBundle bundle) {
		return bundle != null && bundle != MISSING_BUNDLE;
	}

	/** Determines whether any of resource bundles in the parent chain, including the leaf, have expired. */
	private static final boolean hasValidParentChain (ResourceBundle bundle) {
		long now = TimeUtils.millis();
		while (bundle != null) {
			if (bundle.expired) return false;
			if (bundle.bundleKey != null) {
				long expirationTime = bundle.bundleKey.expirationTime;
				if (expirationTime >= 0 && expirationTime <= now) return false;
			}
			bundle = bundle.parent;
		}
		return true;
	}

	/** Checks if the given <code>List</code> is not null, not empty, not having null in its elements. */
	private static final boolean isValidList (List list) {
		if (list == null || list.size() == 0) return false;
		int size = list.size();
		for (int i = 0; i < size; i++) {
			if (list.get(i) == null) return false;
		}
		return true;
	}

	private static final ResourceBundle findBundle (BundleKey bundleKey, List<Locale> candidateLocales, List<String> formats,
		int candidateIndex, Control control, ResourceBundle baseBundle) {
		Locale targetLocale = candidateLocales.get(candidateIndex);
		ResourceBundle parent = null;
		if (candidateIndex != candidateLocales.size() - 1) {
			// Find recursively the parent based on the next candidate locale
			parent = findBundle(bundleKey, candidateLocales, formats, candidateIndex + 1, control, baseBundle);
		} else if (baseBundle != null && targetLocale.equals(Locale.ROOT)) {
			return baseBundle;
		}

		// Remove nulled out references from the cache
		Object ref;
		while ((ref = referenceQueue.poll()) != null) {
			cache.remove(((BundleReference)ref).getBundleKey());
		}

		boolean expiredBundle = false;

		// Look up the bundle in the cache
		bundleKey.setLocale(targetLocale);
		ResourceBundle bundle = findBundleInCache(bundleKey, control);
		if (isValidBundle(bundle)) {
			expiredBundle = bundle.expired;
			if (!expiredBundle) {
				if (bundle.parent == parent) {
					return bundle;
				}
				BundleReference bundleRef = cache.get(bundleKey);
				if (bundleRef != null && bundleRef.get() == bundle) {
					cache.remove(bundleKey, bundleRef);
				}
			}
		}

		if (bundle != MISSING_BUNDLE) {
			BundleKey constKey = new BundleKey(bundleKey);

			try {
				bundle = loadBundle(bundleKey, formats, control, expiredBundle);
				if (bundle != null) {
					if (bundle.parent == null) {
						bundle.setParent(parent);
					}
					bundle.locale = targetLocale;
					bundle = putBundleInCache(bundleKey, bundle, control);
					return bundle;
				}

				// Mark the bundle as missing
				putBundleInCache(bundleKey, MISSING_BUNDLE, control);
			} finally {
				if (constKey.getThrowable() instanceof InterruptedException) {
					// Don't swallow the InterruptedException
					Thread.currentThread().interrupt();
				}
			}
		}
		return parent;
	}

	/** Put a new bundle in the cache.
	 * 
	 * @param bundleKey the key for the resource bundle
	 * @param bundle the resource bundle to be put in the cache
	 * @return the ResourceBundle for the given Key; if someone has put the bundle before this call, the one found in the cache is
	 *         returned. */
	private static final ResourceBundle putBundleInCache (BundleKey bundleKey, ResourceBundle bundle, Control control) {
		setExpirationTime(bundleKey, control);
		if (bundleKey.expirationTime != Control.TTL_DONT_CACHE) {
			BundleKey key = new BundleKey(bundleKey);
			BundleReference bundleReference = new BundleReference(bundle, referenceQueue, key);
			bundle.bundleKey = key;

			// Update the cache if the bundle is not there already.
			BundleReference previous = cache.putIfAbsent(key, bundleReference);

			// If someone else has put the same bundle in the cache before
			// us and it has not expired, we should use the one in the cache.
			if (previous != null) {
				ResourceBundle rb = previous.get();
				if (rb != null && !rb.expired) {
					bundle.bundleKey = null;
					bundle = rb;
					bundleReference.clear();
				} else {
					// Replace the expired bundle with the new one.
					cache.put(key, bundleReference);
				}
			}
		}
		return bundle;
	}

	// Tries to load the bundle in the order of given formats.
	// The first non-null bundle is returned.
	private static final ResourceBundle loadBundle (BundleKey bundleKey, List<String> formats, Control control, boolean reload) {
		Locale targetLocale = bundleKey.getLocale();

		ResourceBundle bundle = null;
		int size = formats.size();
		for (int i = 0; i < size; i++) {
			String format = formats.get(i);
			try {
				bundle = control.newBundle(bundleKey.getName(), targetLocale, format, reload);
			} catch (Exception exception) {
				bundleKey.setThrowable(exception);
			}
			if (bundle != null) {
				bundleKey.setFormat(format);
				bundle.locale = targetLocale;
				bundle.expired = false;
				break;
			}
		}

		return bundle;
	}

	/** Finds a bundle in the cache. Any expired bundles are marked as 'expired' and removed from the cache upon return.
	 * 
	 * @param bundleKey the key to look up the cache
	 * @param control the control to be used for the expiration control
	 * @return the cached bundle, or null if the bundle is not found in the cache or its parent has expired.
	 *         <code>bundle.expire</code> is true upon return if the bundle in the cache has expired. */
	private static final ResourceBundle findBundleInCache (BundleKey bundleKey, Control control) {
		// Get the bundle from the cache
		BundleReference bundleReference = cache.get(bundleKey);
		if (bundleReference == null) {
			return null;
		}
		ResourceBundle bundle = bundleReference.get();
		if (bundle == null) {
			return null;
		}

		// Remove the bundle from the cache if its parent has expired.
		if (bundle.parent != null && bundle.parent.expired) {
			bundle.expired = true;
			bundle.bundleKey = null;
			cache.remove(bundleKey, bundleReference);
			return null;
		}

		BundleKey key = bundleReference.getBundleKey();
		long expirationTime = key.expirationTime;
		if (!bundle.expired && expirationTime >= 0 && expirationTime <= TimeUtils.millis()) {
			// its TTL period has expired.

			// Remove the MISSING_BUNDLE from the cache.
			if (bundle == MISSING_BUNDLE) {
				cache.remove(bundleKey, bundleReference);
				return null;
			}

			// Synchronized call to needsReload
			synchronized (bundle) {
				expirationTime = key.expirationTime;
				if (!bundle.expired && expirationTime >= 0 && expirationTime <= TimeUtils.millis()) {
					try {
						bundle.expired = control.needsReload(key.getName(), key.getLocale(), key.getFormat(), bundle, key.loadTime);
					} catch (Exception e) {
						bundleKey.setThrowable(e);
					}
					if (bundle.expired) {
						bundle.bundleKey = null;
						cache.remove(bundleKey, bundleReference);
					} else {
						setExpirationTime(key, control);
					}
				}
			}
		}
		return bundle;
	}

	private static final void throwMissingResourceException (String baseName, Locale locale, Throwable cause) {
		MissingResourceException mre = new MissingResourceException("Can't find bundle for base name " + baseName + ", locale "
			+ locale, baseName + "_" + locale, "");
		if (!(cause instanceof MissingResourceException)) {
			mre.initCause(cause);
		}
		throw mre;
	}

	private static final void setExpirationTime (BundleKey bundleKey, Control control) {
		long ttl = control.getTimeToLive(bundleKey.getName(), bundleKey.getLocale());
		if (ttl >= 0) {
			bundleKey.loadTime = TimeUtils.millis();
			bundleKey.expirationTime = bundleKey.loadTime + ttl;
		} else if (ttl == Control.TTL_DONT_CACHE || ttl == Control.TTL_NO_EXPIRATION_CONTROL) {
			bundleKey.expirationTime = ttl;
		} else {
			throw new IllegalArgumentException("Invalid Control: TTL=" + ttl);
		}
	}

	/** Returns an enumeration of the keys.
	 * 
	 * @return an <code>Enumeration</code> of the keys contained in this <code>ResourceBundle</code> and its parent bundles. */
	public abstract Enumeration<String> getKeys ();

	/** Returns the locale of this resource bundle. This method can be used after a call to getBundle() to determine whether the
	 * resource bundle returned really corresponds to the requested locale or is a fallback.
	 * 
	 * @return the locale of this resource bundle */
	public Locale getLocale () {
		return locale;
	}

	/** Gets an object for the given key from this resource bundle or one of its parents.
	 * 
	 * @param key the key for the desired object
	 * @exception NullPointerException if <code>key</code> is <code>null</code>
	 * @exception MissingResourceException if no object for the given key can be found
	 * @return the object for the given key */
	public final Object getObject (String key) {
		Object result = handleGetObject(key);
		if (result == null) {
			if (parent != null) result = parent.getObject(key);
			if (result == null)
				throw new MissingResourceException("Can't find resource for bundle " + this.getClass().getName() + ", key " + key,
					this.getClass().getName(), key);
		}
		return result;
	}

	/** Gets a string for the given key from this resource bundle or one of its parents. Calling this method is equivalent to
	 * calling <blockquote> <code>(String) {@link #getObject(java.lang.String) getObject}(key)</code>. </blockquote>
	 * 
	 * @param key the key for the desired string
	 * @exception NullPointerException if <code>key</code> is <code>null</code>
	 * @exception MissingResourceException if no object for the given key can be found
	 * @exception ClassCastException if the object found for the given key is not a string
	 * @return the string for the given key
	 * @see #getObject(String) */
	public final String getString (String key) {
		return (String)getObject(key);
	}

	/** Gets a string array for the given key from this resource bundle or one of its parents. Calling this method is equivalent to
	 * calling <blockquote> <code>(String[]) {@link #getObject(java.lang.String) getObject}(key)</code>. </blockquote>
	 * 
	 * @param key the key for the desired string array
	 * @exception NullPointerException if <code>key</code> is <code>null</code>
	 * @exception MissingResourceException if no object for the given key can be found
	 * @exception ClassCastException if the object found for the given key is not a string array
	 * @return the string array for the given key
	 * @see #getObject(String) */
	public final String[] getStringArray (String key) {
		return (String[])getObject(key);
	}

	/** Gets an object for the given key from this resource bundle. Returns null if this resource bundle does not contain an object
	 * for the given key.
	 * 
	 * @param key the key for the desired object
	 * @exception NullPointerException if <code>key</code> is <code>null</code>
	 * @return the object for the given key, or null */
	protected abstract Object handleGetObject (String key);

	/** Sets the parent bundle of this bundle. The parent bundle is searched by {@link #getObject getObject} when this bundle does
	 * not contain a particular resource.
	 * 
	 * @param parent this bundle's parent bundle. */
	protected void setParent (ResourceBundle parent) {
		this.parent = parent;
	}

	/** Removes all resource bundles from the cache.
	 * 
	 * @see Control#getTimeToLive(String,Locale) */
	public static final void clearCache () {
		cache.clear();
	}

	/** Determines whether the given <code>key</code> is contained in this <code>ResourceBundle</code> or its parent bundles.
	 * 
	 * @param key the resource <code>key</code>
	 * @return <code>true</code> if the given <code>key</code> is contained in this <code>ResourceBundle</code> or its parent
	 *         bundles; <code>false</code> otherwise.
	 * @exception NullPointerException if <code>key</code> is <code>null</code> */
	public boolean containsKey (String key) {
		if (key == null) throw new NullPointerException();
		for (ResourceBundle rb = this; rb != null; rb = rb.parent) {
			if (rb.handleKeySet().contains(key)) return true;
		}
		return false;
	}

	/** Returns a <code>Set</code> of the keys contained <em>only</em> in this <code>ResourceBundle</code>.
	 * 
	 * @return a <code>Set</code> of the keys contained only in this <code>ResourceBundle</code> */
	protected Set<String> handleKeySet () {
		if (keySet == null) {
			synchronized (this) {
				if (keySet == null) {
					Set<String> keys = new HashSet<String>();
					Enumeration<String> enumKeys = getKeys();
					while (enumKeys.hasMoreElements()) {
						String key = enumKeys.nextElement();
						if (handleGetObject(key) != null) {
							keys.add(key);
						}
					}
					keySet = keys;
				}
			}
		}
		return keySet;
	}

	/** Returns a <code>Set</code> of all keys contained in this <code>ResourceBundle</code> and its parent bundles.
	 * 
	 * @return a <code>Set</code> of all keys contained in this <code>ResourceBundle</code> and its parent bundles. */
	public Set<String> keySet () {
		Set<String> keys = new HashSet<String>();
		for (ResourceBundle rb = this; rb != null; rb = rb.parent) {
			keys.addAll(rb.handleKeySet());
		}
		return keys;
	}

	/** References to bundles are soft references so that they can be garbage collected when they have no hard references. */
	private static final class BundleReference extends SoftReference<ResourceBundle> {
		private BundleKey bundleKey;

		BundleReference (ResourceBundle referent, ReferenceQueue queue, BundleKey bundleKey) {
			super(referent, queue);
			this.bundleKey = bundleKey;
		}

		public BundleKey getBundleKey () {
			return bundleKey;
		}
	}

	/** Key used for cached resource bundles. The key checks the base name, the locale, and the auxiliary id to determine if the
	 * resource is a match to the requested one. The auxiliary id may be null, but the base name and the locale must have a
	 * non-null value. */
	private static final class BundleKey {

		/*
		 * The actual keys for lookup in Map.
		 */

		private String name;
		private Locale locale;
		private String auxId;

		/*
		 * Additional fields containing information used during the loading process.
		 */

		// bundle format which is necessary for calling Control.needsReload().
		private String format;

		// The time when the bundle has been loaded
		volatile long loadTime;

		// The time when the bundle expires in the cache, or either
		// Control.TTL_DONT_CACHE or Control.TTL_NO_EXPIRATION_CONTROL.
		volatile long expirationTime;

		// Placeholder for an error
		private Throwable throwable;

		// Cached hash code recalculated only if necessary.
		private int hashCode;

		/** Normal constructor. */
		BundleKey (String baseName, Locale locale, String auxId) {
			this.name = baseName;
			this.locale = locale;
			this.auxId = auxId;
			recalculateHashCode();
		}

		/** Copy constructor. */
		BundleKey (BundleKey key) {
			this.name = key.name;
			this.locale = key.locale;
			this.auxId = key.auxId;
			this.format = key.format;
			this.loadTime = key.loadTime;
			this.expirationTime = key.expirationTime;
			this.throwable = null; // Don't copy the reference to the throwable
			this.hashCode = key.hashCode;
		}

		String getName () {
			return name;
		}

		BundleKey setName (String baseName) {
			// Recalculate the hash code only if the base name has actually changed
			if (!this.name.equals(baseName)) {
				this.name = baseName;
				recalculateHashCode();
			}
			return this;
		}

		Locale getLocale () {
			return locale;
		}

		void setLocale (Locale locale) {
			// Recalculate the hash code only if the locale has actually changed
			if (!this.locale.equals(locale)) {
				this.locale = locale;
				recalculateHashCode();
			}
		}

		String getFormat () {
			return format;
		}

		void setFormat (String format) {
			this.format = format;
		}

		Throwable getThrowable () {
			return throwable;
		}

		void setThrowable (Throwable throwable) {
			// Only override the cause if the previous one is an instance of ClassNotFoundException.
			if (this.throwable == null || this.throwable instanceof ClassNotFoundException) {
				this.throwable = throwable;
			}
		}

		@Override
		public boolean equals (Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			BundleKey key = (BundleKey)obj;
			if (hashCode != key.hashCode) return false;
			if (name == null) {
				if (key.name != null) return false;
			} else if (!name.equals(key.name)) return false;
			if (locale == null) {
				if (key.locale != null) return false;
			} else if (!locale.equals(key.locale)) return false;
			if (auxId == null) {
				if (key.auxId != null) return false;
			} else if (!auxId.equals(key.auxId)) return false;
			return true;
		}

		@Override
		public int hashCode () {
			return hashCode;
		}

		private void recalculateHashCode () {
			hashCode = 1;
			hashCode = 31 * hashCode + ((name == null) ? 0 : name.hashCode());
			hashCode = 31 * hashCode + ((locale == null) ? 0 : locale.hashCode());
			hashCode = 31 * hashCode + ((auxId == null) ? 0 : auxId.hashCode());
		}
	}
}
