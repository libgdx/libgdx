#if defined(diffuseTextureFlag) || defined(specularTextureFlag)
#define textureFlag
#endif

attribute vec3 a_position;

attribute vec3 a_normal;

uniform mat3 u_normalMatrix;
varying vec3 v_normal;

#ifdef textureFlag
attribute vec2 a_texCoord0;
varying vec2 v_texCoords0;
#endif

#if defined(lightsCount) 
#if (lightsCount > 0)
#define NUM_LIGHTS lightsCount
struct Light
{
	vec4 color;
	vec3 position;
	float power;
};
uniform Light lights[NUM_LIGHTS];

varying vec3 v_lightLambert;
#endif
#endif

uniform mat4 u_projTrans;
uniform mat4 u_modelTrans;

void main() {
	#ifdef textureFlag
		v_texCoords0 = a_texCoord0;
	#endif
		
	vec4 pos = u_modelTrans * vec4(a_position, 1.0);
	gl_Position = u_projTrans * pos;

	#ifdef NUM_LIGHTS
	v_normal = u_normalMatrix * a_normal;
	
	vec3 aggDir = vec3(0.0);
	float aggWeight = 0.0;
	vec3 aggCol = vec3(0.0);
	
	for (int i = 0; i < NUM_LIGHTS; i++) {
		if (lights[i].power > 0.0) {
			vec3 diff = lights[i].position - vec3(pos);
			
			float invLen = inversesqrt(dot(diff, diff));
			vec3 intensity = lights[i].color.rgb * invLen;
			
			float lambert = dot(normalize(v_normal), diff * invLen) * 0.5 + 0.5;
			float weight = lights[i].power * invLen * lambert; 

			aggCol += vec3(lights[i].color) * weight;
		}
	}
	v_lightLambert = clamp(aggCol, 0.0, 1.0);
	#endif
}