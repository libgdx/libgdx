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

import android.opengl.GLU;

/**
 * An exception class for OpenGL errors.
 *
 */
@SuppressWarnings("serial")
public class GLException extends RuntimeException {
    public GLException(final int error) {
        super(getErrorString(error));
        mError = error;
    }

    public GLException(final int error, final String string) {
        super(string);
        mError = error;
    }

    private static String getErrorString(int error) {
        String errorString = GLU.gluErrorString(error);
        if ( errorString == null ) {
            errorString = "Unknown error 0x" + Integer.toHexString(error);
        }
        return errorString;
    }

    int getError() {
        return mError;
    }

    private final int mError;
}
