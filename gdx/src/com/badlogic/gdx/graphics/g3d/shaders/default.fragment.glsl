#ifdef GL_ES 
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision mediump float;
#else
#define MED
#define LOWP
#endif

#if defined(specularTextureFlag) || defined(specularColorFlag)
#define specularFlag
#endif

#ifdef normalFlag
varying vec3 v_normal;
#endif

#ifdef colorFlag
varying vec4 v_color;
#endif

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

#ifdef lightingFlag
varying vec3 v_lightDiffuse;
	#ifdef specularFlag
	varying vec3 v_lightSpecular;
	#endif
#endif

void main() {
	#if defined(diffuseTextureFlag) && defined(diffuseColorFlag) && defined(colorFlag)
		vec4 diffuse = texture2D(diffuseTexture, v_texCoords0) * diffuseColor * v_color;
	#elif defined(diffuseTextureFlag) && defined(diffuseColorFlag)
		vec4 diffuse = texture2D(diffuseTexture, v_texCoords0);
	#elif defined(diffuseTextureFlag) && defined(colorFlag)
		vec4 diffuse = texture2D(diffuseTexture, v_texCoords0) * v_color;
	#elif defined(diffuseTextureFlag)
		vec4 diffuse = texture2D(diffuseTexture, v_texCoords0);
	#elif defined(diffuseColorFlag) && defined(colorFlag)
		vec4 diffuse = diffuseColor * v_color;
	#elif defined(diffuseColorFlag)
		vec4 diffuse = diffuseColor;
	#elif defined(colorFlag)
		vec4 diffuse = v_color;
	#else
		vec4 diffuse = vec4(1.0);
	#endif

	#ifdef lightingFlag
		diffuse.rgb *= v_lightDiffuse;
	#endif

	#if !defined(specularFlag) || !defined(lightingFlag)
		gl_FragColor.rgb = diffuse.rgb;
	#else
		#if defined(specularTextureFlag) && defined(specularColorFlag)
			vec3 specular = texture2D(specularTexture, v_texCoords0).rgb * specularColor.rgb * v_lightSpecular;
		#elif defined(specularTextureFlag)
			vec3 specular = texture2D(specularTexture, v_texCoords0).rgb * v_lightSpecular;
		#elif defined(specularColorFlag)
			vec3 specular = specularColor.rgb * v_lightSpecular;
		#elif defined(lightingFlag)
			vec3 specular = v_lightSpecular;
		#endif
		gl_FragColor.rgb = diffuse.rgb + specular.rgb;
	#endif

	#ifdef blendedFlag
		gl_FragColor.a = diffuse.a;
	#endif
}