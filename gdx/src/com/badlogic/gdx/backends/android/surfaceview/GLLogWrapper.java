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

package com.badlogic.gdx.backends.android.surfaceview;

import java.io.IOException;
import java.io.Writer;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import javax.microedition.khronos.opengles.GL;

/**
 * A wrapper that logs all GL calls (and results) in human-readable form.
 *
 */
class GLLogWrapper extends GLWrapperBase {
    private static final int FORMAT_INT = 0;
    private static final int FORMAT_FLOAT = 1;
    private static final int FORMAT_FIXED = 2;

    public GLLogWrapper(GL gl, Writer log, boolean logArgumentNames) {
        super(gl);
        mLog = log;
        mLogArgumentNames = logArgumentNames;
    }

    private void checkError() {
        int glError;
        if ((glError = mgl.glGetError()) != 0) {
            String errorMessage = "glError: " + Integer.toString(glError);
            logLine(errorMessage);
        }
    }

    private void logLine(String message) {
        log(message + '\n');
    }

    private void log(String message) {
        try {
            mLog.write(message);
        } catch (IOException e) {
            // Ignore exception, keep on trying
        }
    }

    private void begin(String name) {
        log(name + '(');
        mArgCount = 0;
    }

    private void arg(String name, String value) {
        if (mArgCount++ > 0) {
            log(", ");
        }
        if (mLogArgumentNames) {
            log(name + "=");
        }
        log(value);
    }

    private void end() {
        log(");\n");
        flush();
    }

    private void flush() {
        try {
            mLog.flush();
        } catch (IOException e) {
            mLog = null;
        }
    }

    private void arg(String name, boolean value) {
        arg(name, Boolean.toString(value));
    }

    private void arg(String name, int value) {
        arg(name, Integer.toString(value));
    }

    private void arg(String name, float value) {
        arg(name, Float.toString(value));
    }

    private void returns(String result) {
        log(") returns " + result + ";\n");
        flush();
    }

    private void returns(int result) {
        returns(Integer.toString(result));
    }

    private void arg(String name, int n, int[] arr, int offset) {
        arg(name, toString(n, FORMAT_INT, arr, offset));
    }

    private void arg(String name, int n, short[] arr, int offset) {
        arg(name, toString(n, arr, offset));
    }

    private void arg(String name, int n, float[] arr, int offset) {
        arg(name, toString(n, arr, offset));
    }

    private void formattedAppend(StringBuilder buf, int value, int format) {
        switch (format) {
        case FORMAT_INT:
            buf.append(value);
            break;
        case FORMAT_FLOAT:
            buf.append(Float.intBitsToFloat(value));
            break;
        case FORMAT_FIXED:
            buf.append(value / 65536.0f);
            break;
        }
    }

    private String toString(int n, int format, int[] arr, int offset) {
        StringBuilder buf = new StringBuilder();
        buf.append("{\n");
        int arrLen = arr.length;
        for (int i = 0; i < n; i++) {
            int index = offset + i;
            buf.append(" [" + index + "] = ");
            if (index < 0 || index >= arrLen) {
                buf.append("out of bounds");
            } else {
                formattedAppend(buf, arr[index], format);
            }
            buf.append('\n');
        }
        buf.append("}");
        return buf.toString();
    }

    private String toString(int n, short[] arr, int offset) {
        StringBuilder buf = new StringBuilder();
        buf.append("{\n");
        int arrLen = arr.length;
        for (int i = 0; i < n; i++) {
            int index = offset + i;
            buf.append(" [" + index + "] = ");
            if (index < 0 || index >= arrLen) {
                buf.append("out of bounds");
            } else {
                buf.append(arr[index]);
            }
            buf.append('\n');
        }
        buf.append("}");
        return buf.toString();
    }

    private String toString(int n, float[] arr, int offset) {
        StringBuilder buf = new StringBuilder();
        buf.append("{\n");
        int arrLen = arr.length;
        for (int i = 0; i < n; i++) {
            int index = offset + i;
            buf.append("[" + index + "] = ");
            if (index < 0 || index >= arrLen) {
                buf.append("out of bounds");
            } else {
                buf.append(arr[index]);
            }
            buf.append('\n');
        }
        buf.append("}");
        return buf.toString();
    }

    private String toString(int n, FloatBuffer buf) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        for (int i = 0; i < n; i++) {
            builder.append(" [" + i + "] = " + buf.get(i) + '\n');
        }
        builder.append("}");
        return builder.toString();
    }

    private String toString(int n, int format, IntBuffer buf) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        for (int i = 0; i < n; i++) {
            builder.append(" [" + i + "] = ");
            formattedAppend(builder, buf.get(i), format);
            builder.append('\n');
        }
        builder.append("}");
        return builder.toString();
    }

    private String toString(int n, ShortBuffer buf) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        for (int i = 0; i < n; i++) {
            builder.append(" [" + i + "] = " + buf.get(i) + '\n');
        }
        builder.append("}");
        return builder.toString();
    }

    private void arg(String name, int n, FloatBuffer buf) {
        arg(name, toString(n, buf));
    }

    private void arg(String name, int n, IntBuffer buf) {
        arg(name, toString(n, FORMAT_INT, buf));
    }

    private void arg(String name, int n, ShortBuffer buf) {
        arg(name, toString(n, buf));
    }

    private void argPointer(int size, int type, int stride, Buffer pointer) {
        arg("size", size);
        arg("type", getPointerTypeName(type));
        arg("stride", stride);
        arg("pointer", pointer.toString());
    }

    private static String getHex(int value) {
        return "0x" + Integer.toHexString(value);
    }

    public static String getErrorString(int error) {
        switch (error) {
        case GL_NO_ERROR:
            return "GL_NO_ERROR";
        case GL_INVALID_ENUM:
            return "GL_INVALID_ENUM";
        case GL_INVALID_VALUE:
            return "GL_INVALID_VALUE";
        case GL_INVALID_OPERATION:
            return "GL_INVALID_OPERATION";
        case GL_STACK_OVERFLOW:
            return "GL_STACK_OVERFLOW";
        case GL_STACK_UNDERFLOW:
            return "GL_STACK_UNDERFLOW";
        case GL_OUT_OF_MEMORY:
            return "GL_OUT_OF_MEMORY";
        default:
            return getHex(error);
        }
    }

    private String getClearBufferMask(int mask) {
        StringBuilder b = new StringBuilder();
        if ((mask & GL_DEPTH_BUFFER_BIT) != 0) {
            b.append("GL_DEPTH_BUFFER_BIT");
            mask &= ~GL_DEPTH_BUFFER_BIT;
        }
        if ((mask & GL_STENCIL_BUFFER_BIT) != 0) {
            if (b.length() > 0) {
                b.append(" | ");
            }
            b.append("GL_STENCIL_BUFFER_BIT");
            mask &= ~GL_STENCIL_BUFFER_BIT;
        }
        if ((mask & GL_COLOR_BUFFER_BIT) != 0) {
            if (b.length() > 0) {
                b.append(" | ");
            }
            b.append("GL_COLOR_BUFFER_BIT");
            mask &= ~GL_COLOR_BUFFER_BIT;
        }
        if (mask != 0) {
            if (b.length() > 0) {
                b.append(" | ");
            }
            b.append(getHex(mask));
        }
        return b.toString();
    }

    private String getFactor(int factor) {
        switch(factor) {
        case GL_ZERO:
            return "GL_ZERO";
        case GL_ONE:
            return "GL_ONE";
        case GL_SRC_COLOR:
            return "GL_SRC_COLOR";
        case GL_ONE_MINUS_SRC_COLOR:
            return "GL_ONE_MINUS_SRC_COLOR";
        case GL_DST_COLOR:
            return "GL_DST_COLOR";
        case GL_ONE_MINUS_DST_COLOR:
            return "GL_ONE_MINUS_DST_COLOR";
        case GL_SRC_ALPHA:
            return "GL_SRC_ALPHA";
        case GL_ONE_MINUS_SRC_ALPHA:
            return "GL_ONE_MINUS_SRC_ALPHA";
        case GL_DST_ALPHA:
            return "GL_DST_ALPHA";
        case GL_ONE_MINUS_DST_ALPHA:
            return "GL_ONE_MINUS_DST_ALPHA";
        case GL_SRC_ALPHA_SATURATE:
            return "GL_SRC_ALPHA_SATURATE";

        default:
            return getHex(factor);
        }
    }

    private String getShadeModel(int model) {
        switch(model) {
        case GL_FLAT:
            return "GL_FLAT";
        case GL_SMOOTH:
            return "GL_SMOOTH";
        default:
            return getHex(model);
        }
    }

    private String getTextureTarget(int target) {
        switch (target) {
        case GL_TEXTURE_2D:
            return "GL_TEXTURE_2D";
        default:
            return getHex(target);
        }
    }

    private String getTextureEnvTarget(int target) {
        switch (target) {
        case GL_TEXTURE_ENV:
            return "GL_TEXTURE_ENV";
        default:
            return getHex(target);
        }
    }

    private String getTextureEnvPName(int pname) {
        switch (pname) {
        case GL_TEXTURE_ENV_MODE:
            return "GL_TEXTURE_ENV_MODE";
        case GL_TEXTURE_ENV_COLOR:
            return "GL_TEXTURE_ENV_COLOR";
        default:
            return getHex(pname);
        }
    }

    private int getTextureEnvParamCount(int pname) {
        switch (pname) {
        case GL_TEXTURE_ENV_MODE:
            return 1;
        case GL_TEXTURE_ENV_COLOR:
            return 4;
        default:
            return 0;
        }
    }

    private String getTextureEnvParamName(float param) {
        int iparam = (int) param;
        if (param == (float) iparam) {
            switch (iparam) {
            case GL_REPLACE:
                return "GL_REPLACE";
            case GL_MODULATE:
                return "GL_MODULATE";
            case GL_DECAL:
                return "GL_DECAL";
            case GL_BLEND:
                return "GL_BLEND";
            case GL_ADD:
                return "GL_ADD";
            case GL_COMBINE:
                return "GL_COMBINE";
            default:
                return getHex(iparam);
            }
        }
        return Float.toString(param);
    }

    private String getMatrixMode(int matrixMode) {
        switch (matrixMode) {
        case GL_MODELVIEW:
            return "GL_MODELVIEW";
        case GL_PROJECTION:
            return "GL_PROJECTION";
        case GL_TEXTURE:
            return "GL_TEXTURE";
        default:
            return getHex(matrixMode);
        }
    }

    private String getClientState(int clientState) {
        switch (clientState) {
        case GL_COLOR_ARRAY:
            return "GL_COLOR_ARRAY";
        case GL_VERTEX_ARRAY:
            return "GL_VERTEX_ARRAY";
        case GL_NORMAL_ARRAY:
            return "GL_NORMAL_ARRAY";
        case GL_TEXTURE_COORD_ARRAY:
            return "GL_TEXTURE_COORD_ARRAY";
        default:
            return getHex(clientState);
        }
    }

    private String getCap(int cap) {
        switch (cap) {
        case GL_FOG:
            return "GL_FOG";
        case GL_LIGHTING:
            return "GL_LIGHTING";
        case GL_TEXTURE_2D:
            return "GL_TEXTURE_2D";
        case GL_CULL_FACE:
            return "GL_CULL_FACE";
        case GL_ALPHA_TEST:
            return "GL_ALPHA_TEST";
        case GL_BLEND:
            return "GL_BLEND";
        case GL_COLOR_LOGIC_OP:
            return "GL_COLOR_LOGIC_OP";
        case GL_DITHER:
            return "GL_DITHER";
        case GL_STENCIL_TEST:
            return "GL_STENCIL_TEST";
        case GL_DEPTH_TEST:
            return "GL_DEPTH_TEST";
        case GL_LIGHT0:
            return "GL_LIGHT0";
        case GL_LIGHT1:
            return "GL_LIGHT1";
        case GL_LIGHT2:
            return "GL_LIGHT2";
        case GL_LIGHT3:
            return "GL_LIGHT3";
        case GL_LIGHT4:
            return "GL_LIGHT4";
        case GL_LIGHT5:
            return "GL_LIGHT5";
        case GL_LIGHT6:
            return "GL_LIGHT6";
        case GL_LIGHT7:
            return "GL_LIGHT7";
        case GL_POINT_SMOOTH:
            return "GL_POINT_SMOOTH";
        case GL_LINE_SMOOTH:
            return "GL_LINE_SMOOTH";
        case GL_COLOR_MATERIAL:
            return "GL_COLOR_MATERIAL";
        case GL_NORMALIZE:
            return "GL_NORMALIZE";
        case GL_RESCALE_NORMAL:
            return "GL_RESCALE_NORMAL";
        case GL_VERTEX_ARRAY:
            return "GL_VERTEX_ARRAY";
        case GL_NORMAL_ARRAY:
            return "GL_NORMAL_ARRAY";
        case GL_COLOR_ARRAY:
            return "GL_COLOR_ARRAY";
        case GL_TEXTURE_COORD_ARRAY:
            return "GL_TEXTURE_COORD_ARRAY";
        case GL_MULTISAMPLE:
            return "GL_MULTISAMPLE";
        case GL_SAMPLE_ALPHA_TO_COVERAGE:
            return "GL_SAMPLE_ALPHA_TO_COVERAGE";
        case GL_SAMPLE_ALPHA_TO_ONE:
            return "GL_SAMPLE_ALPHA_TO_ONE";
        case GL_SAMPLE_COVERAGE:
            return "GL_SAMPLE_COVERAGE";
        case GL_SCISSOR_TEST:
            return "GL_SCISSOR_TEST";
        default:
            return getHex(cap);
        }
    }

    private String getTexturePName(int pname) {
        switch (pname) {
        case GL_TEXTURE_MAG_FILTER:
            return "GL_TEXTURE_MAG_FILTER";
        case GL_TEXTURE_MIN_FILTER:
            return "GL_TEXTURE_MIN_FILTER";
        case GL_TEXTURE_WRAP_S:
            return "GL_TEXTURE_WRAP_S";
        case GL_TEXTURE_WRAP_T:
            return "GL_TEXTURE_WRAP_T";
        case GL_GENERATE_MIPMAP:
            return "GL_GENERATE_MIPMAP";
        case GL_TEXTURE_CROP_RECT_OES:
            return "GL_TEXTURE_CROP_RECT_OES";
        default:
            return getHex(pname);
        }
    }

    private String getTextureParamName(float param) {
        int iparam = (int) param;
        if (param == (float) iparam) {
            switch (iparam) {
            case GL_CLAMP_TO_EDGE:
                return "GL_CLAMP_TO_EDGE";
            case GL_REPEAT:
                return "GL_REPEAT";
            case GL_NEAREST:
                return "GL_NEAREST";
            case GL_LINEAR:
                return "GL_LINEAR";
            case GL_NEAREST_MIPMAP_NEAREST:
                return "GL_NEAREST_MIPMAP_NEAREST";
            case GL_LINEAR_MIPMAP_NEAREST:
                return "GL_LINEAR_MIPMAP_NEAREST";
            case GL_NEAREST_MIPMAP_LINEAR:
                return "GL_NEAREST_MIPMAP_LINEAR";
            case GL_LINEAR_MIPMAP_LINEAR:
                return "GL_LINEAR_MIPMAP_LINEAR";
            default:
                return getHex(iparam);
            }
        }
        return Float.toString(param);
    }

    private String getFogPName(int pname) {
        switch (pname) {
        case GL_FOG_DENSITY:
            return "GL_FOG_DENSITY";
        case GL_FOG_START:
            return "GL_FOG_START";
        case GL_FOG_END:
            return "GL_FOG_END";
        case GL_FOG_MODE:
            return "GL_FOG_MODE";
        case GL_FOG_COLOR:
            return "GL_FOG_COLOR";
        default:
            return getHex(pname);
        }
    }

    private int getFogParamCount(int pname) {
        switch (pname) {
        case GL_FOG_DENSITY:
            return 1;
        case GL_FOG_START:
            return 1;
        case GL_FOG_END:
            return 1;
        case GL_FOG_MODE:
            return 1;
        case GL_FOG_COLOR:
            return 4;
        default:
            return 0;
        }
    }

    private String getBeginMode(int mode) {
        switch (mode) {
        case GL_POINTS:
            return "GL_POINTS";
        case GL_LINES:
            return "GL_LINES";
        case GL_LINE_LOOP:
            return "GL_LINE_LOOP";
        case GL_LINE_STRIP:
            return "GL_LINE_STRIP";
        case GL_TRIANGLES:
            return "GL_TRIANGLES";
        case GL_TRIANGLE_STRIP:
            return "GL_TRIANGLE_STRIP";
        case GL_TRIANGLE_FAN:
            return "GL_TRIANGLE_FAN";
        default:
            return getHex(mode);
        }
    }

    private String getIndexType(int type) {
        switch (type) {
        case GL_UNSIGNED_SHORT:
            return "GL_UNSIGNED_SHORT";
        case GL_UNSIGNED_BYTE:
            return "GL_UNSIGNED_BYTE";
        default:
            return getHex(type);
        }
    }

    private String getIntegerStateName(int pname) {
        switch (pname) {
        case GL_ALPHA_BITS:
            return "GL_ALPHA_BITS";
        case GL_ALIASED_LINE_WIDTH_RANGE:
            return "GL_ALIASED_LINE_WIDTH_RANGE";
        case GL_ALIASED_POINT_SIZE_RANGE:
            return "GL_ALIASED_POINT_SIZE_RANGE";
        case GL_BLUE_BITS:
            return "GL_BLUE_BITS";
        case GL_COMPRESSED_TEXTURE_FORMATS:
            return "GL_COMPRESSED_TEXTURE_FORMATS";
        case GL_DEPTH_BITS:
            return "GL_DEPTH_BITS";
        case GL_GREEN_BITS:
            return "GL_GREEN_BITS";
        case GL_MAX_ELEMENTS_INDICES:
            return "GL_MAX_ELEMENTS_INDICES";
        case GL_MAX_ELEMENTS_VERTICES:
            return "GL_MAX_ELEMENTS_VERTICES";
        case GL_MAX_LIGHTS:
            return "GL_MAX_LIGHTS";
        case GL_MAX_TEXTURE_SIZE:
            return "GL_MAX_TEXTURE_SIZE";
        case GL_MAX_VIEWPORT_DIMS:
            return "GL_MAX_VIEWPORT_DIMS";
        case GL_MAX_MODELVIEW_STACK_DEPTH:
            return "GL_MAX_MODELVIEW_STACK_DEPTH";
        case GL_MAX_PROJECTION_STACK_DEPTH:
            return "GL_MAX_PROJECTION_STACK_DEPTH";
        case GL_MAX_TEXTURE_STACK_DEPTH:
            return "GL_MAX_TEXTURE_STACK_DEPTH";
        case GL_MAX_TEXTURE_UNITS:
            return "GL_MAX_TEXTURE_UNITS";
        case GL_NUM_COMPRESSED_TEXTURE_FORMATS:
            return "GL_NUM_COMPRESSED_TEXTURE_FORMATS";
        case GL_RED_BITS:
            return "GL_RED_BITS";
        case GL_SMOOTH_LINE_WIDTH_RANGE:
            return "GL_SMOOTH_LINE_WIDTH_RANGE";
        case GL_SMOOTH_POINT_SIZE_RANGE:
            return "GL_SMOOTH_POINT_SIZE_RANGE";
        case GL_STENCIL_BITS:
            return "GL_STENCIL_BITS";
        case GL_SUBPIXEL_BITS:
            return "GL_SUBPIXEL_BITS";

        case GL_MODELVIEW_MATRIX_FLOAT_AS_INT_BITS_OES:
            return "GL_MODELVIEW_MATRIX_FLOAT_AS_INT_BITS_OES";
        case GL_PROJECTION_MATRIX_FLOAT_AS_INT_BITS_OES:
            return "GL_PROJECTION_MATRIX_FLOAT_AS_INT_BITS_OES";
        case GL_TEXTURE_MATRIX_FLOAT_AS_INT_BITS_OES:
            return "GL_TEXTURE_MATRIX_FLOAT_AS_INT_BITS_OES";

        default:
            return getHex(pname);
        }
    }

    private int getIntegerStateSize(int pname) {
        switch (pname) {
        case GL_ALPHA_BITS:
            return 1;
        case GL_ALIASED_LINE_WIDTH_RANGE:
            return 2;
        case GL_ALIASED_POINT_SIZE_RANGE:
            return 2;
        case GL_BLUE_BITS:
            return 1;
        case GL_COMPRESSED_TEXTURE_FORMATS:
            // Have to ask the implementation for the size
        {
            int[] buffer = new int[1];
            mgl.glGetIntegerv(GL_NUM_COMPRESSED_TEXTURE_FORMATS, buffer, 0);
            return buffer[0];
        }
        case GL_DEPTH_BITS:
            return 1;
        case GL_GREEN_BITS:
            return 1;
        case GL_MAX_ELEMENTS_INDICES:
            return 1;
        case GL_MAX_ELEMENTS_VERTICES:
            return 1;
        case GL_MAX_LIGHTS:
            return 1;
        case GL_MAX_TEXTURE_SIZE:
            return 1;
        case GL_MAX_VIEWPORT_DIMS:
            return 2;
        case GL_MAX_MODELVIEW_STACK_DEPTH:
            return 1;
        case GL_MAX_PROJECTION_STACK_DEPTH:
            return 1;
        case GL_MAX_TEXTURE_STACK_DEPTH:
            return 1;
        case GL_MAX_TEXTURE_UNITS:
            return 1;
        case GL_NUM_COMPRESSED_TEXTURE_FORMATS:
            return 1;
        case GL_RED_BITS:
            return 1;
        case GL_SMOOTH_LINE_WIDTH_RANGE:
            return 2;
        case GL_SMOOTH_POINT_SIZE_RANGE:
            return 2;
        case GL_STENCIL_BITS:
            return 1;
        case GL_SUBPIXEL_BITS:
            return 1;

        case GL_MODELVIEW_MATRIX_FLOAT_AS_INT_BITS_OES:
        case GL_PROJECTION_MATRIX_FLOAT_AS_INT_BITS_OES:
        case GL_TEXTURE_MATRIX_FLOAT_AS_INT_BITS_OES:
            return 16;

        default:
            return 0;
        }
    }

    private int getIntegerStateFormat(int pname) {
        switch (pname) {
        case GL_MODELVIEW_MATRIX_FLOAT_AS_INT_BITS_OES:
        case GL_PROJECTION_MATRIX_FLOAT_AS_INT_BITS_OES:
        case GL_TEXTURE_MATRIX_FLOAT_AS_INT_BITS_OES:
            return FORMAT_FLOAT;

        default:
            return FORMAT_INT;
        }
    }

    private String getHintTarget(int target) {
        switch (target) {
        case GL_FOG_HINT:
            return "GL_FOG_HINT";
        case GL_LINE_SMOOTH_HINT:
            return "GL_LINE_SMOOTH_HINT";
        case GL_PERSPECTIVE_CORRECTION_HINT:
            return "GL_PERSPECTIVE_CORRECTION_HINT";
        case GL_POINT_SMOOTH_HINT:
            return "GL_POINT_SMOOTH_HINT";
        case GL_POLYGON_SMOOTH_HINT:
            return "GL_POLYGON_SMOOTH_HINT";
        case GL_GENERATE_MIPMAP_HINT:
            return "GL_GENERATE_MIPMAP_HINT";
        default:
            return getHex(target);
        }
    }

    private String getHintMode(int mode) {
        switch (mode) {
        case GL_FASTEST:
            return "GL_FASTEST";
        case GL_NICEST:
            return "GL_NICEST";
        case GL_DONT_CARE:
            return "GL_DONT_CARE";
        default:
            return getHex(mode);
        }
    }

    private String getFaceName(int face) {
        switch (face) {
        case GL_FRONT_AND_BACK:
            return "GL_FRONT_AND_BACK";
        default:
            return getHex(face);
        }
    }

    private String getMaterialPName(int pname) {
        switch (pname) {
        case GL_AMBIENT:
            return "GL_AMBIENT";
        case GL_DIFFUSE:
            return "GL_DIFFUSE";
        case GL_SPECULAR:
            return "GL_SPECULAR";
        case GL_EMISSION:
            return "GL_EMISSION";
        case GL_SHININESS:
            return "GL_SHININESS";
        case GL_AMBIENT_AND_DIFFUSE:
            return "GL_AMBIENT_AND_DIFFUSE";
        default:
            return getHex(pname);
        }
    }

    private int getMaterialParamCount(int pname) {
        switch (pname) {
        case GL_AMBIENT:
            return 4;
        case GL_DIFFUSE:
            return 4;
        case GL_SPECULAR:
            return 4;
        case GL_EMISSION:
            return 4;
        case GL_SHININESS:
            return 1;
        case GL_AMBIENT_AND_DIFFUSE:
            return 4;
        default:
            return 0;
        }
    }

    private String getLightName(int light) {
        if (light >= GL_LIGHT0 && light <= GL_LIGHT7) {
            return "GL_LIGHT" + Integer.toString(light);
        }
        return getHex(light);
    }

    private String getLightPName(int pname) {
        switch (pname) {
        case GL_AMBIENT:
            return "GL_AMBIENT";
        case GL_DIFFUSE:
            return "GL_DIFFUSE";
        case GL_SPECULAR:
            return "GL_SPECULAR";
        case GL_POSITION:
            return "GL_POSITION";
        case GL_SPOT_DIRECTION:
            return "GL_SPOT_DIRECTION";
        case GL_SPOT_EXPONENT:
            return "GL_SPOT_EXPONENT";
        case GL_SPOT_CUTOFF:
            return "GL_SPOT_CUTOFF";
        case GL_CONSTANT_ATTENUATION:
            return "GL_CONSTANT_ATTENUATION";
        case GL_LINEAR_ATTENUATION:
            return "GL_LINEAR_ATTENUATION";
        case GL_QUADRATIC_ATTENUATION:
            return "GL_QUADRATIC_ATTENUATION";
        default:
            return getHex(pname);
        }
    }

    private int getLightParamCount(int pname) {
        switch (pname) {
        case GL_AMBIENT:
            return 4;
        case GL_DIFFUSE:
            return 4;
        case GL_SPECULAR:
            return 4;
        case GL_POSITION:
            return 4;
        case GL_SPOT_DIRECTION:
            return 3;
        case GL_SPOT_EXPONENT:
            return 1;
        case GL_SPOT_CUTOFF:
            return 1;
        case GL_CONSTANT_ATTENUATION:
            return 1;
        case GL_LINEAR_ATTENUATION:
            return 1;
        case GL_QUADRATIC_ATTENUATION:
            return 1;
        default:
            return 0;
        }
    }

    private String getLightModelPName(int pname) {
        switch (pname) {
        case GL_LIGHT_MODEL_AMBIENT:
            return "GL_LIGHT_MODEL_AMBIENT";
        case GL_LIGHT_MODEL_TWO_SIDE:
            return "GL_LIGHT_MODEL_TWO_SIDE";
        default:
            return getHex(pname);
        }
    }

    private int getLightModelParamCount(int pname) {
        switch (pname) {
        case GL_LIGHT_MODEL_AMBIENT:
            return 4;
        case GL_LIGHT_MODEL_TWO_SIDE:
            return 1;
        default:
            return 0;
        }
    }

    private String getPointerTypeName(int type) {
        switch (type) {
        case GL_BYTE:
            return "GL_BYTE";
        case GL_UNSIGNED_BYTE:
            return "GL_UNSIGNED_BYTE";
        case GL_SHORT:
            return "GL_SHORT";
        case GL_FIXED:
            return "GL_FIXED";
        case GL_FLOAT:
            return "GL_FLOAT";
        default:
            return getHex(type);
        }
    }

    private ByteBuffer toByteBuffer(int byteCount, Buffer input) {
        ByteBuffer result = null;
        boolean convertWholeBuffer = (byteCount < 0);
        if (input instanceof ByteBuffer) {
            ByteBuffer input2 = (ByteBuffer) input;
            if (convertWholeBuffer) {
                byteCount = input2.limit();
            }
            result = ByteBuffer.allocate(byteCount).order(input2.order());
            int position = input2.position();
            for (int i = 0; i < byteCount; i++) {
                result.put(input2.get());
            }
            input2.position(position);
        } else if (input instanceof CharBuffer) {
            CharBuffer input2 = (CharBuffer) input;
            if (convertWholeBuffer) {
                byteCount = input2.limit() * 2;
            }
            result = ByteBuffer.allocate(byteCount).order(input2.order());
            CharBuffer result2 = result.asCharBuffer();
            int position = input2.position();
            for (int i = 0; i < byteCount / 2; i++) {
                result2.put(input2.get());
            }
            input2.position(position);
        } else if (input instanceof ShortBuffer) {
            ShortBuffer input2 = (ShortBuffer) input;
            if (convertWholeBuffer) {
                byteCount = input2.limit() * 2;
            }
            result = ByteBuffer.allocate(byteCount).order(input2.order());
            ShortBuffer result2 = result.asShortBuffer();
            int position = input2.position();
            for (int i = 0; i < byteCount / 2; i++) {
                result2.put(input2.get());
            }
            input2.position(position);
        } else if (input instanceof IntBuffer) {
            IntBuffer input2 = (IntBuffer) input;
            if (convertWholeBuffer) {
                byteCount = input2.limit() * 4;
            }
            result = ByteBuffer.allocate(byteCount).order(input2.order());
            IntBuffer result2 = result.asIntBuffer();
            int position = input2.position();
            for (int i = 0; i < byteCount / 4; i++) {
                result2.put(input2.get());
            }
            input2.position(position);
        } else if (input instanceof FloatBuffer) {
            FloatBuffer input2 = (FloatBuffer) input;
            if (convertWholeBuffer) {
                byteCount = input2.limit() * 4;
            }
            result = ByteBuffer.allocate(byteCount).order(input2.order());
            FloatBuffer result2 = result.asFloatBuffer();
            int position = input2.position();
            for (int i = 0; i < byteCount / 4; i++) {
                result2.put(input2.get());
            }
            input2.position(position);
        } else if (input instanceof DoubleBuffer) {
            DoubleBuffer input2 = (DoubleBuffer) input;
            if (convertWholeBuffer) {
                byteCount = input2.limit() * 8;
            }
            result = ByteBuffer.allocate(byteCount).order(input2.order());
            DoubleBuffer result2 = result.asDoubleBuffer();
            int position = input2.position();
            for (int i = 0; i < byteCount / 8; i++) {
                result2.put(input2.get());
            }
            input2.position(position);
        } else if (input instanceof LongBuffer) {
            LongBuffer input2 = (LongBuffer) input;
            if (convertWholeBuffer) {
                byteCount = input2.limit() * 8;
            }
            result = ByteBuffer.allocate(byteCount).order(input2.order());
            LongBuffer result2 = result.asLongBuffer();
            int position = input2.position();
            for (int i = 0; i < byteCount / 8; i++) {
                result2.put(input2.get());
            }
            input2.position(position);
        } else {
            throw new RuntimeException("Unimplemented Buffer subclass.");
        }
        result.rewind();
        // The OpenGL API will interpret the result in hardware byte order,
        // so we better do that as well:
        result.order(ByteOrder.nativeOrder());
        return result;
    }

    private char[] toCharIndices(int count, int type, Buffer indices) {
        char[] result = new char[count];
        switch (type) {
        case GL_UNSIGNED_BYTE: {
            ByteBuffer byteBuffer = toByteBuffer(count, indices);
            byte[] array = byteBuffer.array();
            int offset = byteBuffer.arrayOffset();
            for (int i = 0; i < count; i++) {
                result[i] = (char) (0xff & array[offset + i]);
            }
        }
            break;
        case GL_UNSIGNED_SHORT: {
            CharBuffer charBuffer;
            if (indices instanceof CharBuffer) {
                charBuffer = (CharBuffer) indices;
            } else {
                ByteBuffer byteBuffer = toByteBuffer(count * 2, indices);
                charBuffer = byteBuffer.asCharBuffer();
            }
            int oldPosition = charBuffer.position();
            charBuffer.position(0);
            charBuffer.get(result);
            charBuffer.position(oldPosition);
        }
            break;
        default:
            // Don't throw an exception, because we don't want logging to
            // change the behavior.
            break;
        }
        return result;
    }

    private void doArrayElement(StringBuilder builder, boolean enabled,
            String name, PointerInfo pointer, int index) {
        if (!enabled) {
            return;
        }
        builder.append(" ");
        builder.append(name + ":{");
        if (pointer == null) {
            builder.append("undefined");
            return;
        }
        if (pointer.mStride < 0) {
            builder.append("invalid stride");
            return;
        }

        int stride = pointer.getStride();
        ByteBuffer byteBuffer = pointer.mTempByteBuffer;
        int size = pointer.mSize;
        int type = pointer.mType;
        int sizeofType = pointer.sizeof(type);
        int byteOffset = stride * index;
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            switch (type) {
            case GL_BYTE: {
                byte d = byteBuffer.get(byteOffset);
                builder.append(Integer.toString(d));
            }
                break;
            case GL_UNSIGNED_BYTE: {
                byte d = byteBuffer.get(byteOffset);
                builder.append(Integer.toString(0xff & d));
            }
                break;
            case GL_SHORT: {
                ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
                short d = shortBuffer.get(byteOffset / 2);
                builder.append(Integer.toString(d));
            }
                break;
            case GL_FIXED: {
                IntBuffer intBuffer = byteBuffer.asIntBuffer();
                int d = intBuffer.get(byteOffset / 4);
                builder.append(Integer.toString(d));
            }
                break;
            case GL_FLOAT: {
                FloatBuffer intBuffer = byteBuffer.asFloatBuffer();
                float d = intBuffer.get(byteOffset / 4);
                builder.append(Float.toString(d));
            }
                break;
            default:
                builder.append("?");
                break;
            }
            byteOffset += sizeofType;
        }
        builder.append("}");
    }

    private void doElement(StringBuilder builder, int ordinal, int vertexIndex) {
        builder.append(" [" + ordinal + " : " + vertexIndex + "] =");
        doArrayElement(builder, mVertexArrayEnabled, "v", mVertexPointer,
                vertexIndex);
        doArrayElement(builder, mNormalArrayEnabled, "n", mNormalPointer,
                vertexIndex);
        doArrayElement(builder, mColorArrayEnabled, "c", mColorPointer,
                vertexIndex);
        doArrayElement(builder, mTextureCoordArrayEnabled, "t",
                mTexCoordPointer, vertexIndex);
        builder.append("\n");
        // Vertex
        // Normal
        // Color
        // TexCoord
    }

    private void bindArrays() {
        if (mColorArrayEnabled)
            mColorPointer.bindByteBuffer();
        if (mNormalArrayEnabled)
            mNormalPointer.bindByteBuffer();
        if (mTextureCoordArrayEnabled)
            mTexCoordPointer.bindByteBuffer();
        if (mVertexArrayEnabled)
            mVertexPointer.bindByteBuffer();
    }

    private void unbindArrays() {
        if (mColorArrayEnabled)
            mColorPointer.unbindByteBuffer();
        if (mNormalArrayEnabled)
            mNormalPointer.unbindByteBuffer();
        if (mTextureCoordArrayEnabled)
            mTexCoordPointer.unbindByteBuffer();
        if (mVertexArrayEnabled)
            mVertexPointer.unbindByteBuffer();
    }

    private void startLogIndices() {
        mStringBuilder = new StringBuilder();
        mStringBuilder.append("\n");
        bindArrays();
    }

    private void endLogIndices() {
        log(mStringBuilder.toString());
        unbindArrays();
    }

    // ---------------------------------------------------------------------
    // GL10 methods:

    public void glActiveTexture(int texture) {
        begin("glActiveTexture");
        arg("texture", texture);
        end();
        mgl.glActiveTexture(texture);
        checkError();
    }

    public void glAlphaFunc(int func, float ref) {
        begin("glAlphaFunc");
        arg("func", func);
        arg("ref", ref);
        end();
        mgl.glAlphaFunc(func, ref);
        checkError();
    }

    public void glAlphaFuncx(int func, int ref) {
        begin("glAlphaFuncx");
        arg("func", func);
        arg("ref", ref);
        end();
        mgl.glAlphaFuncx(func, ref);
        checkError();
    }

    public void glBindTexture(int target, int texture) {
        begin("glBindTexture");
        arg("target", getTextureTarget(target));
        arg("texture", texture);
        end();
        mgl.glBindTexture(target, texture);
        checkError();
    }

    public void glBlendFunc(int sfactor, int dfactor) {
        begin("glBlendFunc");
        arg("sfactor", getFactor(sfactor));
        arg("dfactor", getFactor(dfactor));
        end();

        mgl.glBlendFunc(sfactor, dfactor);
        checkError();
    }

    public void glClear(int mask) {
        begin("glClear");
        arg("mask", getClearBufferMask(mask));
        end();

        mgl.glClear(mask);
        checkError();
    }

    public void glClearColor(float red, float green, float blue, float alpha) {
        begin("glClearColor");
        arg("red", red);
        arg("green", green);
        arg("blue", blue);
        arg("alpha", alpha);
        end();

        mgl.glClearColor(red, green, blue, alpha);
        checkError();
    }

    public void glClearColorx(int red, int green, int blue, int alpha) {
        begin("glClearColor");
        arg("red", red);
        arg("green", green);
        arg("blue", blue);
        arg("alpha", alpha);
        end();

        mgl.glClearColorx(red, green, blue, alpha);
        checkError();
    }

    public void glClearDepthf(float depth) {
        begin("glClearDepthf");
        arg("depth", depth);
        end();

        mgl.glClearDepthf(depth);
        checkError();
    }

    public void glClearDepthx(int depth) {
        begin("glClearDepthx");
        arg("depth", depth);
        end();

        mgl.glClearDepthx(depth);
        checkError();
    }

    public void glClearStencil(int s) {
        begin("glClearStencil");
        arg("s", s);
        end();

        mgl.glClearStencil(s);
        checkError();
    }

    public void glClientActiveTexture(int texture) {
        begin("glClientActiveTexture");
        arg("texture", texture);
        end();

        mgl.glClientActiveTexture(texture);
        checkError();
    }

    public void glColor4f(float red, float green, float blue, float alpha) {
        begin("glColor4f");
        arg("red", red);
        arg("green", green);
        arg("blue", blue);
        arg("alpha", alpha);
        end();

        mgl.glColor4f(red, green, blue, alpha);
        checkError();
    }

    public void glColor4x(int red, int green, int blue, int alpha) {
        begin("glColor4x");
        arg("red", red);
        arg("green", green);
        arg("blue", blue);
        arg("alpha", alpha);
        end();

        mgl.glColor4x(red, green, blue, alpha);
        checkError();
    }

    public void glColorMask(boolean red, boolean green, boolean blue,
            boolean alpha) {
        begin("glColorMask");
        arg("red", red);
        arg("green", green);
        arg("blue", blue);
        arg("alpha", alpha);
        end();

        mgl.glColorMask(red, green, blue, alpha);
        checkError();
    }

    public void glColorPointer(int size, int type, int stride, Buffer pointer) {
        begin("glColorPointer");
        argPointer(size, type, stride, pointer);
        end();
        mColorPointer = new PointerInfo(size, type, stride, pointer);

        mgl.glColorPointer(size, type, stride, pointer);
        checkError();
    }

    public void glCompressedTexImage2D(int target, int level,
            int internalformat, int width, int height, int border,
            int imageSize, Buffer data) {
        begin("glCompressedTexImage2D");
        arg("target", getTextureTarget(target));
        arg("level", level);
        arg("internalformat", internalformat);
        arg("width", width);
        arg("height", height);
        arg("border", border);
        arg("imageSize", imageSize);
        arg("data", data.toString());
        end();

        mgl.glCompressedTexImage2D(target, level, internalformat, width,
                height, border, imageSize, data);
        checkError();
    }

    public void glCompressedTexSubImage2D(int target, int level, int xoffset,
            int yoffset, int width, int height, int format, int imageSize,
            Buffer data) {
        begin("glCompressedTexSubImage2D");
        arg("target", getTextureTarget(target));
        arg("level", level);
        arg("xoffset", xoffset);
        arg("yoffset", yoffset);
        arg("width", width);
        arg("height", height);
        arg("format", format);
        arg("imageSize", imageSize);
        arg("data", data.toString());
        end();

        mgl.glCompressedTexSubImage2D(target, level, xoffset, yoffset, width,
                height, format, imageSize, data);
        checkError();
    }

    public void glCopyTexImage2D(int target, int level, int internalformat,
            int x, int y, int width, int height, int border) {
        begin("glCopyTexImage2D");
        arg("target", getTextureTarget(target));
        arg("level", level);
        arg("internalformat", internalformat);
        arg("x", x);
        arg("y", y);
        arg("width", width);
        arg("height", height);
        arg("border", border);
        end();

        mgl.glCopyTexImage2D(target, level, internalformat, x, y, width,
                height, border);
        checkError();
    }

    public void glCopyTexSubImage2D(int target, int level, int xoffset,
            int yoffset, int x, int y, int width, int height) {
        begin("glCopyTexSubImage2D");
        arg("target", getTextureTarget(target));
        arg("level", level);
        arg("xoffset", xoffset);
        arg("yoffset", yoffset);
        arg("x", x);
        arg("y", y);
        arg("width", width);
        arg("height", height);
        end();

        mgl.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width,
                height);
        checkError();
    }

    public void glCullFace(int mode) {
        begin("glCullFace");
        arg("mode", mode);
        end();

        mgl.glCullFace(mode);
        checkError();
    }

    public void glDeleteTextures(int n, int[] textures, int offset) {
        begin("glDeleteTextures");
        arg("n", n);
        arg("textures", n, textures, offset);
        arg("offset", offset);
        end();

        mgl.glDeleteTextures(n, textures, offset);
        checkError();
    }

    public void glDeleteTextures(int n, IntBuffer textures) {
        begin("glDeleteTextures");
        arg("n", n);
        arg("textures", n, textures);
        end();

        mgl.glDeleteTextures(n, textures);
        checkError();
    }

    public void glDepthFunc(int func) {
        begin("glDepthFunc");
        arg("func", func);
        end();

        mgl.glDepthFunc(func);
        checkError();
    }

    public void glDepthMask(boolean flag) {
        begin("glDepthMask");
        arg("flag", flag);
        end();

        mgl.glDepthMask(flag);
        checkError();
    }

    public void glDepthRangef(float near, float far) {
        begin("glDepthRangef");
        arg("near", near);
        arg("far", far);
        end();

        mgl.glDepthRangef(near, far);
        checkError();
    }

    public void glDepthRangex(int near, int far) {
        begin("glDepthRangex");
        arg("near", near);
        arg("far", far);
        end();

        mgl.glDepthRangex(near, far);
        checkError();
    }

    public void glDisable(int cap) {
        begin("glDisable");
        arg("cap", getCap(cap));
        end();

        mgl.glDisable(cap);
        checkError();
    }

    public void glDisableClientState(int array) {
        begin("glDisableClientState");
        arg("array", getClientState(array));
        end();

        switch (array) {
        case GL_COLOR_ARRAY:
            mColorArrayEnabled = false;
            break;
        case GL_NORMAL_ARRAY:
            mNormalArrayEnabled = false;
            break;
        case GL_TEXTURE_COORD_ARRAY:
            mTextureCoordArrayEnabled = false;
            break;
        case GL_VERTEX_ARRAY:
            mVertexArrayEnabled = false;
            break;
        }
        mgl.glDisableClientState(array);
        checkError();
    }

    public void glDrawArrays(int mode, int first, int count) {
        begin("glDrawArrays");
        arg("mode", mode);
        arg("first", first);
        arg("count", count);
        startLogIndices();
        for (int i = 0; i < count; i++) {
            doElement(mStringBuilder, i, first + i);
        }
        endLogIndices();
        end();

        mgl.glDrawArrays(mode, first, count);
        checkError();
    }

    public void glDrawElements(int mode, int count, int type, Buffer indices) {
        begin("glDrawElements");
        arg("mode", getBeginMode(mode));
        arg("count", count);
        arg("type", getIndexType(type));
        char[] indexArray = toCharIndices(count, type, indices);
        int indexArrayLength = indexArray.length;
        startLogIndices();
        for (int i = 0; i < indexArrayLength; i++) {
            doElement(mStringBuilder, i, indexArray[i]);
        }
        endLogIndices();
        end();

        mgl.glDrawElements(mode, count, type, indices);
        checkError();
    }

    public void glEnable(int cap) {
        begin("glEnable");
        arg("cap", getCap(cap));
        end();

        mgl.glEnable(cap);
        checkError();
    }

    public void glEnableClientState(int array) {
        begin("glEnableClientState");
        arg("array", getClientState(array));
        end();

        switch (array) {
        case GL_COLOR_ARRAY:
            mColorArrayEnabled = true;
            break;
        case GL_NORMAL_ARRAY:
            mNormalArrayEnabled = true;
            break;
        case GL_TEXTURE_COORD_ARRAY:
            mTextureCoordArrayEnabled = true;
            break;
        case GL_VERTEX_ARRAY:
            mVertexArrayEnabled = true;
            break;
        }
        mgl.glEnableClientState(array);
        checkError();
    }

    public void glFinish() {
        begin("glFinish");
        end();

        mgl.glFinish();
        checkError();
    }

    public void glFlush() {
        begin("glFlush");
        end();

        mgl.glFlush();
        checkError();
    }

    public void glFogf(int pname, float param) {
        begin("glFogf");
        arg("pname", pname);
        arg("param", param);
        end();

        mgl.glFogf(pname, param);
        checkError();
    }

    public void glFogfv(int pname, float[] params, int offset) {
        begin("glFogfv");
        arg("pname", getFogPName(pname));
        arg("params", getFogParamCount(pname), params, offset);
        arg("offset", offset);
        end();

        mgl.glFogfv(pname, params, offset);
        checkError();
    }

    public void glFogfv(int pname, FloatBuffer params) {
        begin("glFogfv");
        arg("pname", getFogPName(pname));
        arg("params", getFogParamCount(pname), params);
        end();

        mgl.glFogfv(pname, params);
        checkError();
    }

    public void glFogx(int pname, int param) {
        begin("glFogx");
        arg("pname", getFogPName(pname));
        arg("param", param);
        end();

        mgl.glFogx(pname, param);
        checkError();
    }

    public void glFogxv(int pname, int[] params, int offset) {
        begin("glFogxv");
        arg("pname", getFogPName(pname));
        arg("params", getFogParamCount(pname), params, offset);
        arg("offset", offset);
        end();

        mgl.glFogxv(pname, params, offset);
        checkError();
    }

    public void glFogxv(int pname, IntBuffer params) {
        begin("glFogxv");
        arg("pname", getFogPName(pname));
        arg("params", getFogParamCount(pname), params);
        end();

        mgl.glFogxv(pname, params);
        checkError();
    }

    public void glFrontFace(int mode) {
        begin("glFrontFace");
        arg("mode", mode);
        end();

        mgl.glFrontFace(mode);
        checkError();
    }

    public void glFrustumf(float left, float right, float bottom, float top,
            float near, float far) {
        begin("glFrustumf");
        arg("left", left);
        arg("right", right);
        arg("bottom", bottom);
        arg("top", top);
        arg("near", near);
        arg("far", far);
        end();

        mgl.glFrustumf(left, right, bottom, top, near, far);
        checkError();
    }

    public void glFrustumx(int left, int right, int bottom, int top, int near,
            int far) {
        begin("glFrustumx");
        arg("left", left);
        arg("right", right);
        arg("bottom", bottom);
        arg("top", top);
        arg("near", near);
        arg("far", far);
        end();

        mgl.glFrustumx(left, right, bottom, top, near, far);
        checkError();
    }

    public void glGenTextures(int n, int[] textures, int offset) {
        begin("glGenTextures");
        arg("n", n);
        arg("textures", Arrays.toString(textures));
        arg("offset", offset);

        mgl.glGenTextures(n, textures, offset);

        returns(toString(n, FORMAT_INT, textures, offset));

        checkError();
    }

    public void glGenTextures(int n, IntBuffer textures) {
        begin("glGenTextures");
        arg("n", n);
        arg("textures", textures.toString());

        mgl.glGenTextures(n, textures);

        returns(toString(n, FORMAT_INT, textures));

        checkError();
    }

    public int glGetError() {
        begin("glGetError");

        int result = mgl.glGetError();

        returns(result);

        return result;
    }

    public void glGetIntegerv(int pname, int[] params, int offset) {
        begin("glGetIntegerv");
        arg("pname", getIntegerStateName(pname));
        arg("params", Arrays.toString(params));
        arg("offset", offset);

        mgl.glGetIntegerv(pname, params, offset);

        returns(toString(getIntegerStateSize(pname),
                getIntegerStateFormat(pname), params, offset));

        checkError();
    }

    public void glGetIntegerv(int pname, IntBuffer params) {
        begin("glGetIntegerv");
        arg("pname", getIntegerStateName(pname));
        arg("params", params.toString());

        mgl.glGetIntegerv(pname, params);

        returns(toString(getIntegerStateSize(pname),
                getIntegerStateFormat(pname), params));

        checkError();
    }

    public String glGetString(int name) {
        begin("glGetString");
        arg("name", name);

        String result = mgl.glGetString(name);

        returns(result);

        checkError();
        return result;
    }

    public void glHint(int target, int mode) {
        begin("glHint");
        arg("target", getHintTarget(target));
        arg("mode", getHintMode(mode));
        end();

        mgl.glHint(target, mode);
        checkError();
    }

    public void glLightModelf(int pname, float param) {
        begin("glLightModelf");
        arg("pname", getLightModelPName(pname));
        arg("param", param);
        end();

        mgl.glLightModelf(pname, param);
        checkError();
    }

    public void glLightModelfv(int pname, float[] params, int offset) {
        begin("glLightModelfv");
        arg("pname", getLightModelPName(pname));
        arg("params", getLightModelParamCount(pname), params, offset);
        arg("offset", offset);
        end();

        mgl.glLightModelfv(pname, params, offset);
        checkError();
    }

    public void glLightModelfv(int pname, FloatBuffer params) {
        begin("glLightModelfv");
        arg("pname", getLightModelPName(pname));
        arg("params", getLightModelParamCount(pname), params);
        end();

        mgl.glLightModelfv(pname, params);
        checkError();
    }

    public void glLightModelx(int pname, int param) {
        begin("glLightModelx");
        arg("pname", getLightModelPName(pname));
        arg("param", param);
        end();

        mgl.glLightModelx(pname, param);
        checkError();
    }

    public void glLightModelxv(int pname, int[] params, int offset) {
        begin("glLightModelxv");
        arg("pname", getLightModelPName(pname));
        arg("params", getLightModelParamCount(pname), params, offset);
        arg("offset", offset);
        end();

        mgl.glLightModelxv(pname, params, offset);
        checkError();
    }

    public void glLightModelxv(int pname, IntBuffer params) {
        begin("glLightModelfv");
        arg("pname", getLightModelPName(pname));
        arg("params", getLightModelParamCount(pname), params);
        end();

        mgl.glLightModelxv(pname, params);
        checkError();
    }

    public void glLightf(int light, int pname, float param) {
        begin("glLightf");
        arg("light", getLightName(light));
        arg("pname", getLightPName(pname));
        arg("param", param);
        end();

        mgl.glLightf(light, pname, param);
        checkError();
    }

    public void glLightfv(int light, int pname, float[] params, int offset) {
        begin("glLightfv");
        arg("light", getLightName(light));
        arg("pname", getLightPName(pname));
        arg("params", getLightParamCount(pname), params, offset);
        arg("offset", offset);
        end();

        mgl.glLightfv(light, pname, params, offset);
        checkError();
    }

    public void glLightfv(int light, int pname, FloatBuffer params) {
        begin("glLightfv");
        arg("light", getLightName(light));
        arg("pname", getLightPName(pname));
        arg("params", getLightParamCount(pname), params);
        end();

        mgl.glLightfv(light, pname, params);
        checkError();
    }

    public void glLightx(int light, int pname, int param) {
        begin("glLightx");
        arg("light", getLightName(light));
        arg("pname", getLightPName(pname));
        arg("param", param);
        end();

        mgl.glLightx(light, pname, param);
        checkError();
    }

    public void glLightxv(int light, int pname, int[] params, int offset) {
        begin("glLightxv");
        arg("light", getLightName(light));
        arg("pname", getLightPName(pname));
        arg("params", getLightParamCount(pname), params, offset);
        arg("offset", offset);
        end();

        mgl.glLightxv(light, pname, params, offset);
        checkError();
    }

    public void glLightxv(int light, int pname, IntBuffer params) {
        begin("glLightxv");
        arg("light", getLightName(light));
        arg("pname", getLightPName(pname));
        arg("params", getLightParamCount(pname), params);
        end();

        mgl.glLightxv(light, pname, params);
        checkError();
    }

    public void glLineWidth(float width) {
        begin("glLineWidth");
        arg("width", width);
        end();

        mgl.glLineWidth(width);
        checkError();
    }

    public void glLineWidthx(int width) {
        begin("glLineWidthx");
        arg("width", width);
        end();

        mgl.glLineWidthx(width);
        checkError();
    }

    public void glLoadIdentity() {
        begin("glLoadIdentity");
        end();

        mgl.glLoadIdentity();
        checkError();
    }

    public void glLoadMatrixf(float[] m, int offset) {
        begin("glLoadMatrixf");
        arg("m", 16, m, offset);
        arg("offset", offset);
        end();

        mgl.glLoadMatrixf(m, offset);
        checkError();
    }

    public void glLoadMatrixf(FloatBuffer m) {
        begin("glLoadMatrixf");
        arg("m", 16, m);
        end();

        mgl.glLoadMatrixf(m);
        checkError();
    }

    public void glLoadMatrixx(int[] m, int offset) {
        begin("glLoadMatrixx");
        arg("m", 16, m, offset);
        arg("offset", offset);
        end();

        mgl.glLoadMatrixx(m, offset);
        checkError();
    }

    public void glLoadMatrixx(IntBuffer m) {
        begin("glLoadMatrixx");
        arg("m", 16, m);
        end();

        mgl.glLoadMatrixx(m);
        checkError();
    }

    public void glLogicOp(int opcode) {
        begin("glLogicOp");
        arg("opcode", opcode);
        end();

        mgl.glLogicOp(opcode);
        checkError();
    }

    public void glMaterialf(int face, int pname, float param) {
        begin("glMaterialf");
        arg("face", getFaceName(face));
        arg("pname", getMaterialPName(pname));
        arg("param", param);
        end();

        mgl.glMaterialf(face, pname, param);
        checkError();
    }

    public void glMaterialfv(int face, int pname, float[] params, int offset) {
        begin("glMaterialfv");
        arg("face", getFaceName(face));
        arg("pname", getMaterialPName(pname));
        arg("params", getMaterialParamCount(pname), params, offset);
        arg("offset", offset);
        end();

        mgl.glMaterialfv(face, pname, params, offset);
        checkError();
    }

    public void glMaterialfv(int face, int pname, FloatBuffer params) {
        begin("glMaterialfv");
        arg("face", getFaceName(face));
        arg("pname", getMaterialPName(pname));
        arg("params", getMaterialParamCount(pname), params);
        end();

        mgl.glMaterialfv(face, pname, params);
        checkError();
    }

    public void glMaterialx(int face, int pname, int param) {
        begin("glMaterialx");
        arg("face", getFaceName(face));
        arg("pname", getMaterialPName(pname));
        arg("param", param);
        end();

        mgl.glMaterialx(face, pname, param);
        checkError();
    }

    public void glMaterialxv(int face, int pname, int[] params, int offset) {
        begin("glMaterialxv");
        arg("face", getFaceName(face));
        arg("pname", getMaterialPName(pname));
        arg("params", getMaterialParamCount(pname), params, offset);
        arg("offset", offset);
        end();

        mgl.glMaterialxv(face, pname, params, offset);
        checkError();
    }

    public void glMaterialxv(int face, int pname, IntBuffer params) {
        begin("glMaterialxv");
        arg("face", getFaceName(face));
        arg("pname", getMaterialPName(pname));
        arg("params", getMaterialParamCount(pname), params);
        end();

        mgl.glMaterialxv(face, pname, params);
        checkError();
    }

    public void glMatrixMode(int mode) {
        begin("glMatrixMode");
        arg("mode", getMatrixMode(mode));
        end();

        mgl.glMatrixMode(mode);
        checkError();
    }

    public void glMultMatrixf(float[] m, int offset) {
        begin("glMultMatrixf");
        arg("m", 16, m, offset);
        arg("offset", offset);
        end();

        mgl.glMultMatrixf(m, offset);
        checkError();
    }

    public void glMultMatrixf(FloatBuffer m) {
        begin("glMultMatrixf");
        arg("m", 16, m);
        end();

        mgl.glMultMatrixf(m);
        checkError();
    }

    public void glMultMatrixx(int[] m, int offset) {
        begin("glMultMatrixx");
        arg("m", 16, m, offset);
        arg("offset", offset);
        end();

        mgl.glMultMatrixx(m, offset);
        checkError();
    }

    public void glMultMatrixx(IntBuffer m) {
        begin("glMultMatrixx");
        arg("m", 16, m);
        end();

        mgl.glMultMatrixx(m);
        checkError();
    }

    public void glMultiTexCoord4f(int target, float s, float t, float r, float q) {
        begin("glMultiTexCoord4f");
        arg("target", target);
        arg("s", s);
        arg("t", t);
        arg("r", r);
        arg("q", q);
        end();

        mgl.glMultiTexCoord4f(target, s, t, r, q);
        checkError();
    }

    public void glMultiTexCoord4x(int target, int s, int t, int r, int q) {
        begin("glMultiTexCoord4x");
        arg("target", target);
        arg("s", s);
        arg("t", t);
        arg("r", r);
        arg("q", q);
        end();

        mgl.glMultiTexCoord4x(target, s, t, r, q);
        checkError();
    }

    public void glNormal3f(float nx, float ny, float nz) {
        begin("glNormal3f");
        arg("nx", nx);
        arg("ny", ny);
        arg("nz", nz);
        end();

        mgl.glNormal3f(nx, ny, nz);
        checkError();
    }

    public void glNormal3x(int nx, int ny, int nz) {
        begin("glNormal3x");
        arg("nx", nx);
        arg("ny", ny);
        arg("nz", nz);
        end();

        mgl.glNormal3x(nx, ny, nz);
        checkError();
    }

    public void glNormalPointer(int type, int stride, Buffer pointer) {
        begin("glNormalPointer");
        arg("type", type);
        arg("stride", stride);
        arg("pointer", pointer.toString());
        end();
        mNormalPointer = new PointerInfo(3, type, stride, pointer);

        mgl.glNormalPointer(type, stride, pointer);
        checkError();
    }

    public void glOrthof(float left, float right, float bottom, float top,
            float near, float far) {
        begin("glOrthof");
        arg("left", left);
        arg("right", right);
        arg("bottom", bottom);
        arg("top", top);
        arg("near", near);
        arg("far", far);
        end();

        mgl.glOrthof(left, right, bottom, top, near, far);
        checkError();
    }

    public void glOrthox(int left, int right, int bottom, int top, int near,
            int far) {
        begin("glOrthox");
        arg("left", left);
        arg("right", right);
        arg("bottom", bottom);
        arg("top", top);
        arg("near", near);
        arg("far", far);
        end();

        mgl.glOrthox(left, right, bottom, top, near, far);
        checkError();
    }

    public void glPixelStorei(int pname, int param) {
        begin("glPixelStorei");
        arg("pname", pname);
        arg("param", param);
        end();

        mgl.glPixelStorei(pname, param);
        checkError();
    }

    public void glPointSize(float size) {
        begin("glPointSize");
        arg("size", size);
        end();

        mgl.glPointSize(size);
        checkError();
    }

    public void glPointSizex(int size) {
        begin("glPointSizex");
        arg("size", size);
        end();

        mgl.glPointSizex(size);
        checkError();
    }

    public void glPolygonOffset(float factor, float units) {
        begin("glPolygonOffset");
        arg("factor", factor);
        arg("units", units);
        end();
        mgl.glPolygonOffset(factor, units);
        checkError();
    }

    public void glPolygonOffsetx(int factor, int units) {
        begin("glPolygonOffsetx");
        arg("factor", factor);
        arg("units", units);
        end();

        mgl.glPolygonOffsetx(factor, units);
        checkError();
    }

    public void glPopMatrix() {
        begin("glPopMatrix");
        end();

        mgl.glPopMatrix();
        checkError();
    }

    public void glPushMatrix() {
        begin("glPushMatrix");
        end();

        mgl.glPushMatrix();
        checkError();
    }

    public void glReadPixels(int x, int y, int width, int height, int format,
            int type, Buffer pixels) {
        begin("glReadPixels");
        arg("x", x);
        arg("y", y);
        arg("width", width);
        arg("height", height);
        arg("format", format);
        arg("type", type);
        arg("pixels", pixels.toString());
        end();

        mgl.glReadPixels(x, y, width, height, format, type, pixels);
        checkError();
    }

    public void glRotatef(float angle, float x, float y, float z) {
        begin("glRotatef");
        arg("angle", angle);
        arg("x", x);
        arg("y", y);
        arg("z", z);
        end();

        mgl.glRotatef(angle, x, y, z);
        checkError();
    }

    public void glRotatex(int angle, int x, int y, int z) {
        begin("glRotatex");
        arg("angle", angle);
        arg("x", x);
        arg("y", y);
        arg("z", z);
        end();

        mgl.glRotatex(angle, x, y, z);
        checkError();
    }

    public void glSampleCoverage(float value, boolean invert) {
        begin("glSampleCoveragex");
        arg("value", value);
        arg("invert", invert);
        end();

        mgl.glSampleCoverage(value, invert);
        checkError();
    }

    public void glSampleCoveragex(int value, boolean invert) {
        begin("glSampleCoveragex");
        arg("value", value);
        arg("invert", invert);
        end();

        mgl.glSampleCoveragex(value, invert);
        checkError();
    }

    public void glScalef(float x, float y, float z) {
        begin("glScalef");
        arg("x", x);
        arg("y", y);
        arg("z", z);
        end();

        mgl.glScalef(x, y, z);
        checkError();
    }

    public void glScalex(int x, int y, int z) {
        begin("glScalex");
        arg("x", x);
        arg("y", y);
        arg("z", z);
        end();

        mgl.glScalex(x, y, z);
        checkError();
    }

    public void glScissor(int x, int y, int width, int height) {
        begin("glScissor");
        arg("x", x);
        arg("y", y);
        arg("width", width);
        arg("height", height);
        end();

        mgl.glScissor(x, y, width, height);
        checkError();
    }

    public void glShadeModel(int mode) {
        begin("glShadeModel");
        arg("mode", getShadeModel(mode));
        end();

        mgl.glShadeModel(mode);
        checkError();
    }

    public void glStencilFunc(int func, int ref, int mask) {
        begin("glStencilFunc");
        arg("func", func);
        arg("ref", ref);
        arg("mask", mask);
        end();

        mgl.glStencilFunc(func, ref, mask);
        checkError();
    }

    public void glStencilMask(int mask) {
        begin("glStencilMask");
        arg("mask", mask);
        end();

        mgl.glStencilMask(mask);
        checkError();
    }

    public void glStencilOp(int fail, int zfail, int zpass) {
        begin("glStencilOp");
        arg("fail", fail);
        arg("zfail", zfail);
        arg("zpass", zpass);
        end();

        mgl.glStencilOp(fail, zfail, zpass);
        checkError();
    }

    public void glTexCoordPointer(int size, int type, int stride, Buffer pointer) {
        begin("glTexCoordPointer");
        argPointer(size, type, stride, pointer);
        end();
        mTexCoordPointer = new PointerInfo(size, type, stride, pointer);

        mgl.glTexCoordPointer(size, type, stride, pointer);
        checkError();
    }

    public void glTexEnvf(int target, int pname, float param) {
        begin("glTexEnvf");
        arg("target", getTextureEnvTarget(target));
        arg("pname", getTextureEnvPName(pname));
        arg("param", getTextureEnvParamName(param));
        end();

        mgl.glTexEnvf(target, pname, param);
        checkError();
    }

    public void glTexEnvfv(int target, int pname, float[] params, int offset) {
        begin("glTexEnvfv");
        arg("target", getTextureEnvTarget(target));
        arg("pname", getTextureEnvPName(pname));
        arg("params", getTextureEnvParamCount(pname), params, offset);
        arg("offset", offset);
        end();

        mgl.glTexEnvfv(target, pname, params, offset);
        checkError();
    }

    public void glTexEnvfv(int target, int pname, FloatBuffer params) {
        begin("glTexEnvfv");
        arg("target", getTextureEnvTarget(target));
        arg("pname", getTextureEnvPName(pname));
        arg("params", getTextureEnvParamCount(pname), params);
        end();

        mgl.glTexEnvfv(target, pname, params);
        checkError();
    }

    public void glTexEnvx(int target, int pname, int param) {
        begin("glTexEnvx");
        arg("target", getTextureEnvTarget(target));
        arg("pname", getTextureEnvPName(pname));
        arg("param", param);
        end();

        mgl.glTexEnvx(target, pname, param);
        checkError();
    }

    public void glTexEnvxv(int target, int pname, int[] params, int offset) {
        begin("glTexEnvxv");
        arg("target", getTextureEnvTarget(target));
        arg("pname", getTextureEnvPName(pname));
        arg("params", getTextureEnvParamCount(pname), params, offset);
        arg("offset", offset);
        end();

        mgl.glTexEnvxv(target, pname, params, offset);
        checkError();
    }

    public void glTexEnvxv(int target, int pname, IntBuffer params) {
        begin("glTexEnvxv");
        arg("target", getTextureEnvTarget(target));
        arg("pname", getTextureEnvPName(pname));
        arg("params", getTextureEnvParamCount(pname), params);
        end();

        mgl.glTexEnvxv(target, pname, params);
        checkError();
    }

    public void glTexImage2D(int target, int level, int internalformat,
            int width, int height, int border, int format, int type,
            Buffer pixels) {
        begin("glTexImage2D");
        arg("target", target);
        arg("level", level);
        arg("internalformat", internalformat);
        arg("width", width);
        arg("height", height);
        arg("border", border);
        arg("format", format);
        arg("type", type);
        arg("pixels", pixels.toString());
        end();

        mgl.glTexImage2D(target, level, internalformat, width, height, border,
                format, type, pixels);
        checkError();
    }

    public void glTexParameterf(int target, int pname, float param) {
        begin("glTexParameterf");
        arg("target", getTextureTarget(target));
        arg("pname", getTexturePName(pname));
        arg("param", getTextureParamName(param));
        end();

        mgl.glTexParameterf(target, pname, param);
        checkError();
    }

    public void glTexParameterx(int target, int pname, int param) {
        begin("glTexParameterx");
        arg("target", getTextureTarget(target));
        arg("pname", getTexturePName(pname));
        arg("param", param);
        end();

        mgl.glTexParameterx(target, pname, param);
        checkError();
    }

    public void glTexParameteriv(int target, int pname, int[] params, int offset) {
        begin("glTexParameteriv");
        arg("target", getTextureTarget(target));
        arg("pname", getTexturePName(pname));
        arg("params", 4, params, offset);
        end();

        mgl11.glTexParameteriv(target, pname, params, offset);
        checkError();
    }

    public void glTexParameteriv(int target, int pname, IntBuffer params) {
        begin("glTexParameteriv");
        arg("target", getTextureTarget(target));
        arg("pname", getTexturePName(pname));
        arg("params", 4, params);
        end();

        mgl11.glTexParameteriv(target, pname, params);
        checkError();
    }

    public void glTexSubImage2D(int target, int level, int xoffset,
            int yoffset, int width, int height, int format, int type,
            Buffer pixels) {
        begin("glTexSubImage2D");
        arg("target", getTextureTarget(target));
        arg("level", level);
        arg("xoffset", xoffset);
        arg("yoffset", yoffset);
        arg("width", width);
        arg("height", height);
        arg("format", format);
        arg("type", type);
        arg("pixels", pixels.toString());
        end();
        mgl.glTexSubImage2D(target, level, xoffset, yoffset, width, height,
                format, type, pixels);
        checkError();
    }

    public void glTranslatef(float x, float y, float z) {
        begin("glTranslatef");
        arg("x", x);
        arg("y", y);
        arg("z", z);
        end();
        mgl.glTranslatef(x, y, z);
        checkError();
    }

    public void glTranslatex(int x, int y, int z) {
        begin("glTranslatex");
        arg("x", x);
        arg("y", y);
        arg("z", z);
        end();
        mgl.glTranslatex(x, y, z);
        checkError();
    }

    public void glVertexPointer(int size, int type, int stride, Buffer pointer) {
        begin("glVertexPointer");
        argPointer(size, type, stride, pointer);
        end();
        mVertexPointer = new PointerInfo(size, type, stride, pointer);
        mgl.glVertexPointer(size, type, stride, pointer);
        checkError();
    }

    public void glViewport(int x, int y, int width, int height) {
        begin("glViewport");
        arg("x", x);
        arg("y", y);
        arg("width", width);
        arg("height", height);
        end();
        mgl.glViewport(x, y, width, height);
        checkError();
    }

    public void glClipPlanef(int plane, float[] equation, int offset) {
        begin("glClipPlanef");
        arg("plane", plane);
        arg("equation", 4, equation, offset);
        arg("offset", offset);
        end();
        mgl11.glClipPlanef(plane, equation, offset);
        checkError();
    }

    public void glClipPlanef(int plane, FloatBuffer equation) {
        begin("glClipPlanef");
        arg("plane", plane);
        arg("equation", 4, equation);
        end();
        mgl11.glClipPlanef(plane, equation);
        checkError();
    }

    public void glClipPlanex(int plane, int[] equation, int offset) {
        begin("glClipPlanex");
        arg("plane", plane);
        arg("equation", 4, equation, offset);
        arg("offset", offset);
        end();
        mgl11.glClipPlanex(plane, equation, offset);
        checkError();
    }

    public void glClipPlanex(int plane, IntBuffer equation) {
        begin("glClipPlanef");
        arg("plane", plane);
        arg("equation", 4, equation);
        end();
        mgl11.glClipPlanex(plane, equation);
        checkError();
    }

    // Draw Texture Extension

    public void glDrawTexfOES(float x, float y, float z,
        float width, float height) {
        begin("glDrawTexfOES");
        arg("x", x);
        arg("y", y);
        arg("z", z);
        arg("width", width);
        arg("height", height);
        end();
        mgl11Ext.glDrawTexfOES(x, y, z, width, height);
        checkError();
    }

    public void glDrawTexfvOES(float[] coords, int offset) {
        begin("glDrawTexfvOES");
        arg("coords", 5, coords, offset);
        arg("offset", offset);
        end();
        mgl11Ext.glDrawTexfvOES(coords, offset);
        checkError();
    }

    public void glDrawTexfvOES(FloatBuffer coords) {
        begin("glDrawTexfvOES");
        arg("coords", 5, coords);
        end();
        mgl11Ext.glDrawTexfvOES(coords);
        checkError();
    }

    public void glDrawTexiOES(int x, int y, int z, int width, int height) {
        begin("glDrawTexiOES");
        arg("x", x);
        arg("y", y);
        arg("z", z);
        arg("width", width);
        arg("height", height);
        end();
        mgl11Ext.glDrawTexiOES(x, y, z, width, height);
        checkError();
    }

    public void glDrawTexivOES(int[] coords, int offset) {
        begin("glDrawTexivOES");
        arg("coords", 5, coords, offset);
        arg("offset", offset);
        end();
        mgl11Ext.glDrawTexivOES(coords, offset);
        checkError();
    }

    public void glDrawTexivOES(IntBuffer coords) {
        begin("glDrawTexivOES");
        arg("coords", 5, coords);
        end();
        mgl11Ext.glDrawTexivOES(coords);
        checkError();
    }

    public void glDrawTexsOES(short x, short y, short z,
        short width, short height) {
        begin("glDrawTexsOES");
        arg("x", x);
        arg("y", y);
        arg("z", z);
        arg("width", width);
        arg("height", height);
        end();
        mgl11Ext.glDrawTexsOES(x, y, z, width, height);
        checkError();
    }

    public void glDrawTexsvOES(short[] coords, int offset) {
        begin("glDrawTexsvOES");
        arg("coords", 5, coords, offset);
        arg("offset", offset);
        end();
        mgl11Ext.glDrawTexsvOES(coords, offset);
        checkError();
    }

    public void glDrawTexsvOES(ShortBuffer coords) {
        begin("glDrawTexsvOES");
        arg("coords", 5, coords);
        end();
        mgl11Ext.glDrawTexsvOES(coords);
        checkError();
    }

    public void glDrawTexxOES(int x, int y, int z, int width, int height) {
        begin("glDrawTexxOES");
        arg("x", x);
        arg("y", y);
        arg("z", z);
        arg("width", width);
        arg("height", height);
        end();
        mgl11Ext.glDrawTexxOES(x, y, z, width, height);
        checkError();
    }

    public void glDrawTexxvOES(int[] coords, int offset) {
        begin("glDrawTexxvOES");
        arg("coords", 5, coords, offset);
        arg("offset", offset);
        end();
        mgl11Ext.glDrawTexxvOES(coords, offset);
        checkError();
    }

    public void glDrawTexxvOES(IntBuffer coords) {
        begin("glDrawTexxvOES");
        arg("coords", 5, coords);
        end();
        mgl11Ext.glDrawTexxvOES(coords);
        checkError();
    }

    public int glQueryMatrixxOES(int[] mantissa, int mantissaOffset,
        int[] exponent, int exponentOffset) {
        begin("glQueryMatrixxOES");
        arg("mantissa", Arrays.toString(mantissa));
        arg("exponent", Arrays.toString(exponent));
        end();
        int valid = mgl10Ext.glQueryMatrixxOES(mantissa, mantissaOffset,
            exponent, exponentOffset);
        returns(toString(16, FORMAT_FIXED, mantissa, mantissaOffset));
        returns(toString(16, FORMAT_INT, exponent, exponentOffset));
        checkError();
        return valid;
    }

    public int glQueryMatrixxOES(IntBuffer mantissa, IntBuffer exponent) {
        begin("glQueryMatrixxOES");
        arg("mantissa", mantissa.toString());
        arg("exponent", exponent.toString());
        end();
        int valid = mgl10Ext.glQueryMatrixxOES(mantissa, exponent);
        returns(toString(16, FORMAT_FIXED, mantissa));
        returns(toString(16, FORMAT_INT, exponent));
        checkError();
        return valid;
    }

    // Unsupported GL11 methods

    public void glBindBuffer(int target, int buffer) {
        throw new UnsupportedOperationException();
    }

    public void glBufferData(int target, int size, Buffer data, int usage) {
        throw new UnsupportedOperationException();
    }

    public void glBufferSubData(int target, int offset, int size, Buffer data) {
        throw new UnsupportedOperationException();
    }

    public void glColor4ub(byte red, byte green, byte blue, byte alpha) {
        throw new UnsupportedOperationException();
    }

    public void glDeleteBuffers(int n, int[] buffers, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glDeleteBuffers(int n, IntBuffer buffers) {
        throw new UnsupportedOperationException();
    }

    public void glGenBuffers(int n, int[] buffers, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glGenBuffers(int n, IntBuffer buffers) {
        throw new UnsupportedOperationException();
    }

    public void glGetBooleanv(int pname, boolean[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glGetBooleanv(int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    public void glGetBufferParameteriv(int target, int pname, int[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glGetBufferParameteriv(int target, int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    public void glGetClipPlanef(int pname, float[] eqn, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glGetClipPlanef(int pname, FloatBuffer eqn) {
        throw new UnsupportedOperationException();
    }

    public void glGetClipPlanex(int pname, int[] eqn, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glGetClipPlanex(int pname, IntBuffer eqn) {
        throw new UnsupportedOperationException();
    }

    public void glGetFixedv(int pname, int[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glGetFixedv(int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    public void glGetFloatv(int pname, float[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glGetFloatv(int pname, FloatBuffer params) {
        throw new UnsupportedOperationException();
    }

    public void glGetLightfv(int light, int pname, float[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glGetLightfv(int light, int pname, FloatBuffer params) {
        throw new UnsupportedOperationException();
    }

    public void glGetLightxv(int light, int pname, int[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glGetLightxv(int light, int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    public void glGetMaterialfv(int face, int pname, float[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glGetMaterialfv(int face, int pname, FloatBuffer params) {
        throw new UnsupportedOperationException();
    }

    public void glGetMaterialxv(int face, int pname, int[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glGetMaterialxv(int face, int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    public void glGetTexEnviv(int env, int pname, int[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glGetTexEnviv(int env, int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    public void glGetTexEnvxv(int env, int pname, int[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glGetTexEnvxv(int env, int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    public void glGetTexParameterfv(int target, int pname, float[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glGetTexParameterfv(int target, int pname, FloatBuffer params) {
        throw new UnsupportedOperationException();
    }

    public void glGetTexParameteriv(int target, int pname, int[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glGetTexParameteriv(int target, int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    public void glGetTexParameterxv(int target, int pname, int[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glGetTexParameterxv(int target, int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    public boolean glIsBuffer(int buffer) {
        throw new UnsupportedOperationException();
    }

    public boolean glIsEnabled(int cap) {
        throw new UnsupportedOperationException();
    }

    public boolean glIsTexture(int texture) {
        throw new UnsupportedOperationException();
    }

    public void glPointParameterf(int pname, float param) {
        throw new UnsupportedOperationException();
    }

    public void glPointParameterfv(int pname, float[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glPointParameterfv(int pname, FloatBuffer params) {
        throw new UnsupportedOperationException();
    }

    public void glPointParameterx(int pname, int param) {
        throw new UnsupportedOperationException();
    }

    public void glPointParameterxv(int pname, int[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glPointParameterxv(int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    public void glPointSizePointerOES(int type, int stride, Buffer pointer) {
        throw new UnsupportedOperationException();
    }

    public void glTexEnvi(int target, int pname, int param) {
        throw new UnsupportedOperationException();
    }

    public void glTexEnviv(int target, int pname, int[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glTexEnviv(int target, int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    public void glTexParameterfv(int target, int pname, float[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glTexParameterfv(int target, int pname, FloatBuffer params) {
        throw new UnsupportedOperationException();
    }

    public void glTexParameteri(int target, int pname, int param) {
        throw new UnsupportedOperationException();
    }

    public void glTexParameterxv(int target, int pname, int[] params, int offset) {
        throw new UnsupportedOperationException();
    }

    public void glTexParameterxv(int target, int pname, IntBuffer params) {
        throw new UnsupportedOperationException();
    }

    private class PointerInfo {
        /**
         * The number of coordinates per vertex. 1..4
         */
        public int mSize;
        /**
         * The type of each coordinate.
         */
        public int mType;
        /**
         * The byte offset between consecutive vertices. 0 means mSize *
         * sizeof(mType)
         */
        public int mStride;
        public Buffer mPointer;
        public ByteBuffer mTempByteBuffer; // Only valid during glDrawXXX calls

        public PointerInfo(int size, int type, int stride, Buffer pointer) {
            mSize = size;
            mType = type;
            mStride = stride;
            mPointer = pointer;
        }

        public int sizeof(int type) {
            switch (type) {
            case GL_UNSIGNED_BYTE:
                return 1;
            case GL_BYTE:
                return 1;
            case GL_SHORT:
                return 2;
            case GL_FIXED:
                return 4;
            case GL_FLOAT:
                return 4;
            default:
                return 0;
            }
        }

        public int getStride() {
            return mStride > 0 ? mStride : sizeof(mType) * mSize;
        }

        public void bindByteBuffer() {
            mTempByteBuffer = toByteBuffer(-1, mPointer);
        }

        public void unbindByteBuffer() {
            mTempByteBuffer = null;
        }
    }

    private Writer mLog;
    private boolean mLogArgumentNames;
    private int mArgCount;

    private PointerInfo mColorPointer;
    private PointerInfo mNormalPointer;
    private PointerInfo mTexCoordPointer;
    private PointerInfo mVertexPointer;

    boolean mColorArrayEnabled;
    boolean mNormalArrayEnabled;
    boolean mTextureCoordArrayEnabled;
    boolean mVertexArrayEnabled;

    StringBuilder mStringBuilder;
}