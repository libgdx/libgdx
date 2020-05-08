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

package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderPart;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShaderStage;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class SimpleTesslationShaderTest extends GdxTest {
	ShaderProgram shader;
	Mesh mesh;
	Matrix4 projection = new Matrix4();
	Matrix4 view = new Matrix4();
	Matrix4 model = new Matrix4();
	Matrix4 combined = new Matrix4();
	Vector3 axis = new Vector3(1, 0, 1).nor();
	float angle = 45;

	@Override
	public void create () {
		// @off
		String vertexShader =
			  "attribute vec4 a_position;          \n"
			+ "attribute vec3 a_normal;				\n"
			+ "varying vec3 v_normal;						\n"
			+ "void main()	{								\n"
			+ "	v_normal = a_normal;					\n"
			+ "   gl_Position = a_position;  		\n"
			+ "}";
		
		String tesslationControlShader =
			  "layout (vertices = 3) out;				\n"
			+ ""
			+ "in vec3 v_normal[];						\n"
			+ "out vec3 t_normal[];						\n"
			+ ""
			+ "void main() {"
			+ "	gl_TessLevelOuter[0] = 8.0;			\n"
			+ "	gl_TessLevelOuter[1] = 8.0;			\n"
			+ "	gl_TessLevelOuter[2] = 8.0;			\n"
			+ "	gl_TessLevelInner[0] = 8.0;			\n"
			+ ""
			+ "	gl_out[gl_InvocationID].gl_Position = gl_in[gl_InvocationID].gl_Position;\n"
			+ "	t_normal[gl_InvocationID] = v_normal[gl_InvocationID];\n"
			+ "}";
		
		String tesslationEvaluationShader =
			  "layout(triangles, equal_spacing, ccw) in;\n"
			+ "in vec3 t_normal[];\n"
			+ "out vec3 v_normal;\n"
			+ "void main() {"
			+ "	v_normal = normalize(gl_TessCoord.x * t_normal[0] + gl_TessCoord.y * t_normal[1] + gl_TessCoord.z * t_normal[2]);\n"
			+ "	gl_Position = gl_in[0].gl_Position * gl_TessCoord.x + gl_in[1].gl_Position * gl_TessCoord.y + gl_in[2].gl_Position * gl_TessCoord.z;\n"
			+ "}";

		String geometryShader =
			  "uniform mat4 u_mvpMatrix;                   	\n"
			+ ""
			+ "layout(triangles) in;                        \n"
			+ "layout (line_strip, max_vertices=2) out;     \n"
			+ ""
			+ "in vec3 v_normal[3];									\n"
			+ "out vec4 color;										\n"
			+ ""
			+ "void main() {                                \n"
			+ "	vec3 center = (gl_in[0].gl_Position.xyz + gl_in[1].gl_Position.xyz + gl_in[2].gl_Position.xyz) / 3.0;\n"
			+ "	vec3 normal = normalize(v_normal[0] + v_normal[1] + v_normal[2]);\n"
			+ ""
			+ "	gl_Position = u_mvpMatrix * vec4(center, 1.0);\n"
			+ "	color = vec4(1.0, 0.0, 0.0, 1.0);\n"
			+ "	EmitVertex();\n"
			+ ""
			+ "	gl_Position = u_mvpMatrix * vec4(center + normal * 0.5, 1.0);\n"
			+ "	color = vec4(1.0, 1.0, 0.0, 1.0);\n"
			+ "	EmitVertex();\n"
			+ "}";
		
		String fragmentShader =
			  "#ifdef GL_ES\n"
			+ "precision mediump float;\n"
			+ "#endif\n"
			+ "in vec4 color;					\n"
			+ "void main() {					\n"
			+ "  gl_FragColor = color;		\n"
			+ "}";
		// @on
		
		ShaderStage.geometry.prependCode = Gdx.app.getType().equals(Application.ApplicationType.Desktop) ? "#version 150\n" : "#version 320 es\n";
		ShaderStage.tesslationControl.prependCode = Gdx.app.getType().equals(Application.ApplicationType.Desktop) ? "#version 400\n" : "#version 320 es\n";
		ShaderStage.tesslationEvaluation.prependCode = Gdx.app.getType().equals(Application.ApplicationType.Desktop) ? "#version 400\n" : "#version 320 es\n";

		shader = new ShaderProgram(
			new ShaderPart(ShaderStage.vertex, vertexShader),
			new ShaderPart(ShaderStage.tesslationControl, tesslationControlShader),
			new ShaderPart(ShaderStage.tesslationEvaluation, tesslationEvaluationShader),
			new ShaderPart(ShaderStage.geometry, geometryShader),
			new ShaderPart(ShaderStage.fragment, fragmentShader));
		
		if(!shader.isCompiled()){
			throw new GdxRuntimeException(shader.getLog());
		}
		
		MeshBuilder builder = new MeshBuilder();
		builder.begin(new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal()), GL20.GL_TRIANGLES);
		BoxShapeBuilder.build(builder, 1, 1, 1);
		mesh = builder.end();
		mesh.getVertexAttribute(Usage.Position).alias = "a_position";
		mesh.getVertexAttribute(Usage.Normal).alias = "a_normal";
	}

	@Override
	public void render () {
		angle += Gdx.graphics.getDeltaTime() * 40.0f;
		float aspect = Gdx.graphics.getWidth() / (float)Gdx.graphics.getHeight();
		projection.setToProjection(1.0f, 20.0f, 60.0f, aspect);
		view.idt().trn(0, 0, -2.0f);
		model.setToRotation(axis, angle);
		combined.set(projection).mul(view).mul(model);

		Gdx.gl20.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		shader.bind();
		shader.setUniformMatrix("u_mvpMatrix", combined);
		mesh.render(shader, GL30.GL_PATCHES);

	}
}
