package com.badlogic.gdx.backends.bindings.metalangle.enums;


import org.moe.natj.general.ann.Generated;

/**
 * Enumerated boolean type
 * 
 * Values other than zero should be considered to be true.  Therefore
 * comparisons should not be made against KHRONOS_TRUE.
 */
@Generated
public final class khronos_boolean_enum_t {
    @Generated
    private khronos_boolean_enum_t() {
    }

    @Generated
    public static final int KHRONOS_FALSE = 0x00000000;
    @Generated
    public static final int KHRONOS_TRUE = 0x00000001;
    @Generated
    public static final int KHRONOS_BOOLEAN_ENUM_FORCE_SIZE = 0x7FFFFFFF;
}