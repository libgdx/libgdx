#version 300 es
precision mediump float;

uniform sampler2D albedoBuffer;
uniform sampler2D lightBuffer;

in vec2 texCoord;

out vec4 fragColor;

void main()
{
	vec4 mapAlbedo = texture2D(albedoBuffer, texCoord);
	vec4 mapLight = texture2D(lightBuffer, texCoord);
	
	fragColor = mapAlbedo * mapLight;
}