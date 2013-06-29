package com.badlogic.gdx.tests.g3d.nodes;

import com.badlogic.gdx.graphics.g3d.shaders.graph.ShaderGraph;
import com.badlogic.gdx.graphics.g3d.shaders.graph.ShaderNode;
import com.badlogic.gdx.graphics.g3d.shaders.graph.ShaderNodeBuilder;
import com.badlogic.gdx.graphics.g3d.shaders.graph.ShaderInput.ShaderInputQualifier;
import com.badlogic.gdx.graphics.g3d.shaders.graph.ShaderNode.ShaderNodeType;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class ShaderGraphTest {
	public static void main (String[] args) {
		ShaderNodeBuilder builder = new ShaderNodeBuilder();
		
		ShaderGraph graph = new ShaderGraph(ShaderNodeType.Vertex);
		
		builder.name("positionAttr").type(ShaderNodeType.Vertex)
				 .input(ShaderProgram.POSITION_ATTRIBUTE, "vec3", ShaderInputQualifier.Attribute)
				 .output("position", "vec4")
				 .code("position = vec4(a_position, 1.0);");
		ShaderNode positionAttr = builder.build();
		graph.addNodeType(positionAttr);
		
		builder.clear();
		builder.name("normalAttr").type(ShaderNodeType.Vertex)
		 		 .input(ShaderProgram.NORMAL_ATTRIBUTE, "vec3", ShaderInputQualifier.Attribute)
		 		 .output("normal", "vec4")
		 		 .code("normal = vec4(a_normal, 0.0);");
		ShaderNode normalAttr = builder.build();
		graph.addNodeType(normalAttr);
		
		builder.clear();
		builder.name("uv0Attr").type(ShaderNodeType.Vertex)
		 		 .input(ShaderProgram.TEXCOORD_ATTRIBUTE + "0", "vec2", ShaderInputQualifier.Attribute)
		 		 .output("uv0", "vec2")
		 		 .code("uv0 = a_texCoord0;");
		ShaderNode uv0Attr = builder.build();
		graph.addNodeType(uv0Attr);
		
		builder.clear();
		builder.name("uv1Attr").type(ShaderNodeType.Vertex)
		 		 .input(ShaderProgram.TEXCOORD_ATTRIBUTE + "1", "vec2", ShaderInputQualifier.Attribute)
		 		 .output("uv1", "vec2")
		 		 .code("uv1 = a_texCoord1;");
		ShaderNode uv1Attr = builder.build();
		graph.addNodeType(uv1Attr);
		
		builder.clear();
		builder.name("transform").type(ShaderNodeType.Vertex)
				 .input("position", "vec4")
				 .input("projectionView", "mat4")
				 .output("transformedPosition", "vec4")
				 .code("position = projectionView * transformedPosition;");
		ShaderNode transform = builder.build();
		graph.addNodeType(transform);
		System.out.println(graph);
	}
}
