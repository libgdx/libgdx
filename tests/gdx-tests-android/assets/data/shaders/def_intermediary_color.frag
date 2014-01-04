#version 300 es
precision mediump float;

uniform sampler2D colorBuffer;

in vec2 texCoord;

out vec4 fragColor;

void main()
{
	fragColor = texture2D(colorBuffer, texCoord);
}