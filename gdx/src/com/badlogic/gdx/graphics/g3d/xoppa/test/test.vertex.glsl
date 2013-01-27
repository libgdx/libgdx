#if defined(diffuseTextureFlag) || defined(specularTextureFlag)
#define textureFlag
#endif

attribute vec3 a_position;
#ifdef textureFlag
attribute vec2 a_texCoord0;
varying vec2 v_texCoords0;
#endif

#if defined(lightsCount) && (lightsCount > 0)
#define NUM_LIGHTS lightsCount
struct Light
{
	vec4 color;
	vec3 position;
	float power;
};
uniform Light lights[NUM_LIGHTS];
#endif

uniform mat4 u_projTrans;
uniform mat4 u_modelTrans;

void main() {
	#ifdef textureFlag
		v_texCoords0 = a_texCoord0;
	#endif
	gl_Position = u_projTrans * (u_modelTrans * vec4(a_position, 1.0));
}