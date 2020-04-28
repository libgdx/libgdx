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

package com.badlogic.gdx;

import java.util.HashMap;

/**
 * A lazy load dependencies manager, the dependency will be created only when the inject method is called.
 * Very efficient in saving memory and managing the loading time
 *
 * <br><br>
 *
 * <p>Java 8
 * <pre> DependenciesInjector.map(MySuperClass.class, MyImplantation::new);</pre>
 *
 * <br>
 * <p>Java 7
 * <pre>
 * DependenciesInjector.map(MySuperClass.class, new LazyLoader&#60;MySuperClass&#62;() {
 *     &#64;Override
 *     public MySuperClass load() {
 *         return new MyImplantation();
 *     }
 * });
 * }
 * </pre>
 *
 *
 * <p>And then to inject it use:
 *
 * <p><code>MySuperClass mySuperClass = DependenciesInjector.inject(MySuperClass.class);</code>
 */
public final class DependenciesInjector {

    private static final HashMap<Class<?>, LazyLoader<?>> MAP = new HashMap<>();

    /**
     * Adds a mapping for a clz instance
     *
     * @param clz        the class to map
     * @param lazyLoader a lazy loader callback to be called when the class is being injected
     */
    public static <T> void map(Class<? super T> clz, LazyLoader<T> lazyLoader) {
        MAP.put(clz, lazyLoader);
    }

    /**
     * Inject the provided clz, will throw an exception if it wasn't mapped before
     *
     * @param clz the class to inject
     * @return a new instance of clz
     */
    public static <T> T inject(Class<T> clz) {
        @SuppressWarnings("unchecked")
        LazyLoader<T> loader = (LazyLoader<T>) MAP.get(clz);
        if (loader == null) {
            throw new Error("Mapping for " + clz.getName() + " not found");
        }
        return loader.load();
    }

    public interface LazyLoader<T> {
        T load();
    }
}
