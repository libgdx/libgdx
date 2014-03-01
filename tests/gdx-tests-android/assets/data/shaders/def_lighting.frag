#version 300 es
precision highp float;
precision highp sampler2D;

#define MAX_INSTANCES 32

uniform CameraMatrices {
	mat4 view;
	mat4 projection;
	mat4 viewProjection;
	mat4 invProjection;
};

struct LightStaticInfo
{
	vec3 worldPos;
	float radius;
	vec3 color;
};

uniform LightView {
	vec3 viewPos[MAX_INSTANCES];
};

uniform LightParams {
	LightStaticInfo lights[MAX_INSTANCES];
};

uniform sampler2D normalBuffer;
uniform sampler2D depthBuffer;

flat in int instanceID;
in vec4 screenCoord;

out vec4 fragLight;

/*
[ a 0 0 0 ] [x] = [a*x]
[ 0 b 0 0 ] [y] = [b*y]
[ 0 0 0 d ] [z] = [d]
[ 0 0 c e ] [1] = [c*z + e]
probably faster than conventional invProjection multiplication
*/
vec3 getViewPos(vec2 scr, float depth)
{
	vec4 res;
	res.x = invProjection[0][0] * scr.x;
	res.y = invProjection[1][1] * scr.y;
	res.z = invProjection[3][2];
	res.w = invProjection[2][3] * depth + invProjection[3][3];
	return res.xyz / res.w;
}

void main()
{
	// data from uniforms
	LightStaticInfo light = lights[instanceID];
	float radius = light.radius;
	vec3 lightColor = light.color;
	vec3 viewLightOrigin = viewPos[instanceID];

	// sample from gbuffer
	vec2 screenCoord2d = screenCoord.xy / screenCoord.w;
	vec2 texCoord = screenCoord2d * 0.5 + 0.5;
	vec3 storedNormal = texture2D(normalBuffer, texCoord).rgb * 2.0 - 1.0;
	float storedDepth = texture2D(depthBuffer, texCoord).r * 2.0 - 1.0;

	// calculate position in screen space
	vec3 viewPos = getViewPos(screenCoord2d, storedDepth);
	
	// determine light contribution
	vec3 lightDirection = viewLightOrigin - viewPos;
	vec3 lightDirectionNormalized = normalize(lightDirection);
	float nDotL = max(dot(storedNormal, lightDirectionNormalized), 0.0);
	
	float attenuation = clamp(1.0 - length(lightDirection) / radius, 0.0, 1.0);
	
	vec3 lightContribution = lightColor * attenuation * nDotL;
	
	fragLight = vec4(lightContribution, 1);
} 