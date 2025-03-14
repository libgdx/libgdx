
package com.badlogic.gdx.utils;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/** Compile time "NonNullByDefault" annotation (which libgdx doesn't use) to satisfy Eclipse.
 * @author Nathan Sweet */
@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
public @interface Eclipse2 {
}
