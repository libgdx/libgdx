#version 300 es
precision mediump float;
precision highp sampler2D;

uniform sampler2D depthBuffer;

in vec2 texCoord;

out vec4 fragColor;

void main()
{
	float storedDepth = pow(texture2D(depthBuffer, texCoord).r, 5.0);
	fragColor = vec4(vec3(storedDepth), 1);
}