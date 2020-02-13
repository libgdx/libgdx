
package com.badlogic.gdx.utils;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/** An element with this annotation claims that the element may have a {@code null} value. Apart from documentation purposes this
 * annotation is intended to be used by static analysis tools to validate against probable runtime errors or contract violations.
 * @author maltaisn */
@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
public @interface Null {
}
