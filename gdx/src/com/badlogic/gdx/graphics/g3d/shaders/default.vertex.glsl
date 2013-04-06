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

#ifdef shininessFlag
uniform float shininess;
#else
const float shininess = 20.0;
#endif

#if defined(lightsCount)
#define NUM_LIGHTS lightsCount

varying vec3 v_lightDiffuse;
varying vec3 v_lightSpecular;
uniform vec4 ambient;
uniform vec3 u_cameraPosition;
varying vec3 v_viewVec;

#if (NUM_LIGHTS > 0)
#define NONE 		0
#define AMBIENT 	1
#define POINT		3
#define DIRECTIONAL	5
#define SPOT		7

struct Light
{
	int type;
	vec4 color;
	vec3 position;
	vec3 attenuation;
	vec3 direction;
	float angle;
	float exponent;
};
uniform Light lights[NUM_LIGHTS];
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
	v_lightDiffuse = ambient.rgb;
	v_lightSpecular = vec3(0.0);
	#if (NUM_LIGHTS > 0)
	v_normal = u_normalMatrix * a_normal;
	v_viewVec = normalize(u_cameraPosition - pos.xyz);
		
	for (int i = 0; i < NUM_LIGHTS; i++) {
		if (lights[i].type != NONE) {
			vec3 lightVec = lights[i].type == DIRECTIONAL ? -lights[i].direction : normalize(lights[i].position - pos.xyz);
			float diff = dot(v_normal, lightVec);
			
			if (diff > 0.0) {
				float spot = 1.0;
				if (lights[i].type == SPOT) {
					spot = max(-dot(lightVec, lights[i].direction), 0.0);
					float fade = clamp((lights[i].angle - spot)/(-lights[i].angle*0.05), 0.0, 1.0); // FIXME make inner angle variable
					spot = pow(spot * fade, lights[i].exponent);
				}
				
				vec3 r = -normalize(reflect(lightVec, v_normal));
				float spec = pow(max(dot(r, v_viewVec), 0.0), shininess);
				
				float weight = 1.0;
				if (lights[i].type != DIRECTIONAL) {
					float d = distance(pos.xyz, lights[i].position);
					weight = spot / (lights[i].attenuation.x + lights[i].attenuation.y * d + lights[i].attenuation.z * d * d);
				}
				
				vec3 fc = lights[i].color.rgb * weight;
				v_lightDiffuse += diff * fc;
				v_lightSpecular += spec * fc;
			}
		}
	}
	#endif
	#endif
}