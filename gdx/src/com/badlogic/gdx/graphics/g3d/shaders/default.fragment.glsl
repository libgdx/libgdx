#ifdef GL_ES 
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision mediump float;
#else
#define MED
#define LOWP
#endif

varying vec3 v_normal;

#if defined(diffuseTextureFlag) || defined(specularTextureFlag)
varying MED vec2 v_texCoords0;
#endif

#ifdef diffuseColorFlag
uniform vec4 diffuseColor;
#endif

#ifdef diffuseTextureFlag
uniform sampler2D diffuseTexture;
#endif

#ifdef specularColorFlag
uniform vec4 specularColor;
#endif

#ifdef specularTextureFlag
uniform sampler2D specularTexture;
#endif

#if defined(lightsCount)
#define NUM_LIGHTS lightsCount
varying vec3 v_lightLambert;
varying vec3 v_lightSpecular;
varying vec3 v_viewVec;
/* #if (NUM_LIGHTS > 0)
struct Light
{
	vec4 color;
	vec3 position;
	float power;
};
uniform Light lights[NUM_LIGHTS];
#endif */
#endif

void main() {
	#if defined(diffuseTextureFlag) && defined(diffuseColorFlag)
		vec4 diffuse = texture2D(diffuseTexture, v_texCoords0) * diffuseColor;
	#elif defined(diffuseTextureFlag)
		vec4 diffuse = texture2D(diffuseTexture, v_texCoords0);
	#elif defined(diffuseColorFlag)
		vec4 diffuse = diffuseColor;
	#else
		vec4 diffuse = vec4(1.0);
	#endif

	#if defined(specularTextureFlag) && defined(specularColorFlag)
		vec4 specular = texture2D(specularTexture, v_texCoords0) * specularColor;
	#elif defined(specularTextureFlag)
		vec4 specular = texture2D(specularTexture, v_texCoords0);
	#elif defined(specularColorFlag)
		vec4 specular = specularColor;
	#else
		vec4 specular = vec4(0.0);
	#endif
	
	#ifdef NUM_LIGHTS
		diffuse.rgb *= v_lightLambert;
		specular.rgb *= v_lightSpecular;
	#endif
	
	gl_FragColor.rgb = diffuse.rgb + specular.rgb;
}