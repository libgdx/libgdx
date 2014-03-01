#version 300 es

uniform CameraMatrices {
	mat4 view;
	mat4 projection;
	mat4 viewProjection;
	mat4 invProjection;
};

uniform ModelMatrices {
	mat4 modelViewProjection;
};

layout(location = 0)in vec3 inPos;
layout(location = 1)in vec3 inNorm;
layout(location = 2)in vec3 inTan;
layout(location = 3)in vec3 inBiTan;
layout(location = 4)in vec2 inTex;

out mat3 tbnMatrix;
out vec2 texcoord;

void main()
{                                                   
	gl_Position = modelViewProjection * vec4(inPos, 1);
	
	texcoord = inTex;
	
	mat3 normalMat = mat3(view);
	tbnMatrix = mat3(normalMat * inTan,
					 normalMat * inBiTan,
					 normalMat * inNorm);
}                                                   
