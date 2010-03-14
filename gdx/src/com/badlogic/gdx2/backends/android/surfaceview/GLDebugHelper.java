/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.badlogic.gdx2.backends.android.surfaceview;

import java.io.Writer;

import javax.microedition.khronos.egl.EGL;
import javax.microedition.khronos.opengles.GL;

/**
 * A helper class for debugging OpenGL ES applications.
 *
 * Wraps the supplied GL interface with a new GL interface that adds support for
 * error checking and logging.
 *
 */
public class GLDebugHelper {

    /**
     * Wrap an existing GL interface in a new GL interface that adds support for
     * error checking and/or logging.
     * <p>
     * Wrapping means that the GL instance that is passed in to this method is
     * wrapped inside a new GL instance that optionally performs additional
     * operations before and after calling the wrapped GL instance.
     * <p>
     * Error checking means that the wrapper will automatically call
     * glError after each GL operation,
     * and throw a GLException if an error occurs. (By design, calling glError
     * itself will not cause an exception to be thrown.) Enabling error checking
     * is an alternative to manually calling glError after every GL operation.
     * <p>
     * Logging means writing a text representation of each GL method call to
     * a log.
     * <p>
     * @param gl the existing GL interface. Must implement GL and GL10. May
     * optionally implement GL11 as well.
     * @param configFlags A bitmask of error checking flags.
     * @param log - null to disable logging, non-null to enable logging.
     * @return the wrapped GL instance.
     */

    /**
     * Check glError() after every call.
     */
    public static final int CONFIG_CHECK_GL_ERROR = (1 << 0);

    /**
     * Check if all calls are on the same thread.
     */
    public static final int CONFIG_CHECK_THREAD = (1 << 1);

    /**
     * Print argument names when logging GL Calls.
     */
    public static final int CONFIG_LOG_ARGUMENT_NAMES = (1 << 2);

    /**
     * The Error number used in the GLException that is thrown if
     * CONFIG_CHECK_THREAD is enabled and you call OpenGL ES on the
     * a different thread.
     */
    public static final int ERROR_WRONG_THREAD = 0x7000;

    public static GL wrap(GL gl, int configFlags, Writer log) {
        if ( configFlags != 0 ) {
            gl = new GLErrorWrapper(gl, configFlags);
        }
        if ( log != null ) {
            boolean logArgumentNames =
                (CONFIG_LOG_ARGUMENT_NAMES & configFlags) != 0;
            gl = new GLLogWrapper(gl, log, logArgumentNames);
        }
        return gl;
    }

    /**
     * Wrap an existing EGL interface in a new EGL interface that adds
     * support for error checking and/or logging.
     * @param egl the existing GL interface. Must implement EGL and EGL10. May
     * optionally implement EGL11 as well.
     * @param configFlags A bitmask of error checking flags.
     * @param log - null to disable logging, non-null to enable logging.
     * @return the wrapped EGL interface.
     */
    public static EGL wrap(EGL egl, int configFlags, Writer log) {
        if (log != null) {
            egl = new EGLLogWrapper(egl, configFlags, log);
        }
        return egl;
    }
}
