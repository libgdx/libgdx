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