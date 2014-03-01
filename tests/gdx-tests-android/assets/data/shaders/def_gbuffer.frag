#version 300 es
precision highp float;

uniform sampler2D albedoTexture;
uniform sampler2D normalTexture;

in mat3 tbnMatrix;
in vec2 texcoord;

layout(location = 0)out vec4 fragAlbedo;
layout(location = 1)out vec4 fragNormal;

void main()
{
	vec4 mapAlbedo = texture2D(albedoTexture, texcoord);
	vec3 mapNormal = texture2D(normalTexture, texcoord).xyz * 2.0 - 1.0;
	
	vec3 viewNormal = tbnMatrix * mapNormal;                            
	viewNormal = normalize(viewNormal) * 0.5 + 0.5;
	
	fragAlbedo = vec4(mapAlbedo.rgb,1);
	fragNormal = vec4(viewNormal,1);
}