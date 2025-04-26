
package com.badlogic.gdx.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/** Do not use this annotation. It exists only to satisfy IDE configuration. By convention in libgdx everything is implicitly
 * non-null. Marking all packages NonNullByDefault so null analysis knows about our convention could be useful, but it would be
 * somewhat annoying to apply this to every package. */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
@Deprecated
@interface NonNullByDefault {
}
