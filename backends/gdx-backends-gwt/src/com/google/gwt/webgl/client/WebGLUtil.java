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

package com.google.gwt.webgl.client;

import static com.google.gwt.webgl.client.WebGLRenderingContext.*;

public class WebGLUtil {

	public static float[] createPerspectiveMatrix (int fieldOfViewVertical, float aspectRatio, float minimumClearance,
		float maximumClearance) {
		double fieldOfViewInRad = fieldOfViewVertical * Math.PI / 180.0;
		return new float[] {(float)(Math.tan(fieldOfViewInRad) / aspectRatio), 0, 0, 0, 0,
			(float)(1 / Math.tan(fieldOfViewVertical * Math.PI / 180.0)), 0, 0, 0, 0,
			(minimumClearance + maximumClearance) / (minimumClearance - maximumClearance), -1, 0, 0,
			2 * minimumClearance * maximumClearance / (minimumClearance - maximumClearance), 0};
	}

	public static WebGLProgram createShaderProgram (WebGLRenderingContext gl, String vertexSource, String fragmentSource) {
		WebGLShader vertexShader = getShader(gl, VERTEX_SHADER, vertexSource);
		WebGLShader fragmentShader = getShader(gl, FRAGMENT_SHADER, fragmentSource);

		WebGLProgram shaderProgram = gl.createProgram();
		gl.attachShader(shaderProgram, fragmentShader);
		gl.attachShader(shaderProgram, vertexShader);
		gl.linkProgram(shaderProgram);

		if (!gl.getProgramParameterb(shaderProgram, LINK_STATUS)) {
			throw new RuntimeException("Could not initialize shaders");
		}

		return shaderProgram;
	}

	private static WebGLShader getShader (WebGLRenderingContext gl, int shaderType, String source) {
		WebGLShader shader = gl.createShader(shaderType);
		gl.shaderSource(shader, source);
		gl.compileShader(shader);
		if (!gl.getShaderParameterb(shader, COMPILE_STATUS)) {
			throw new RuntimeException(gl.getShaderInfoLog(shader));
		}
		return shader;
	}
}
