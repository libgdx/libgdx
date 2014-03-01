#version 300 es

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

uniform LightParams {
	LightStaticInfo lights[MAX_INSTANCES];
};

layout(location = 0)in vec3 inPos;

flat out int instanceID;
out vec4 screenCoord;

void main()
{   
	LightStaticInfo light = lights[gl_InstanceID];
	float radius = light.radius;
	vec3 worldOffset = light.worldPos;

	gl_Position = viewProjection * vec4(radius * inPos + worldOffset, 1.0);
	
	screenCoord = gl_Position;
	
	instanceID = gl_InstanceID;
}                                                   
