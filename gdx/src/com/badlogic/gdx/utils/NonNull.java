
package com.badlogic.gdx.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/** Do not use this annotation. It exists only to satisfy IDE configuration. Marking everything that cannot be null is tedious and
 * would add enormous clutter. Instead by convention in libgdx everything is implicitly non-null. */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
@Deprecated
@interface NonNull {
}
