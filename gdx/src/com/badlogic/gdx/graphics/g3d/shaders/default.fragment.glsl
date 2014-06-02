#ifdef GL_ES 
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision mediump float;
#else
#define MED
#define LOWP
#define HIGH
#endif

#if defined(specularTextureFlag) || defined(specularColorFlag)
#define specularFlag
#endif


varying vec3 v_eyePoint;  //point to cam

#ifdef normalFlag
varying vec3 v_normal;
#endif //normalFlag

#if defined(colorFlag)
varying vec4 v_color;
#endif

#ifdef blendedFlag
varying float v_opacity;
#ifdef alphaTestFlag
varying float v_alphaTest;
#endif //alphaTestFlag
#endif //blendedFlag

#if defined(diffuseTextureFlag) || defined(specularTextureFlag)
#define textureFlag
varying MED vec2 v_texCoords0;
#endif

#ifdef diffuseColorFlag
uniform vec4 u_diffuseColor;
#endif

#ifdef diffuseTextureFlag
uniform sampler2D u_diffuseTexture;
#endif

#ifdef specularColorFlag
uniform vec4 u_specularColor;
#endif

#ifdef specularTextureFlag
uniform sampler2D u_specularTexture;
#endif

#ifdef normalTextureFlag
uniform sampler2D u_normalTexture;
#endif

#ifdef lightingFlag

//here come the SpotLight
struct SpotLight
{
	vec3 color;
	vec3 position;
	vec3 direction;
	float constantAttenuation;
	float linearAttenuation;
	float quadraticAttenuation;
	float cutOff;
	float exponent;
};
uniform SpotLight u_spotLights[numSpotLights];

struct SpotLightInterpolated
{
	vec3 color;
	vec3 position;
	vec3 direction;
	float dist;
};
varying SpotLightInterpolated v_spotLights[numSpotLights];

#if	defined(ambientLightFlag) || defined(ambientCubemapFlag) || defined(sphericalHarmonicsFlag)
#define ambientFlag
#endif //ambientFlag

#ifdef specularFlag
//varying vec3 v_lightSpecular;
varying vec3 viewVec;
#endif //specularFlag

#ifdef shadowMapFlag
uniform sampler2D u_shadowTexture;
uniform float u_shadowPCFOffset;
varying vec3 v_shadowMapUv;
#define separateAmbientFlag

float getShadowness(vec2 offset)
{
    const vec4 bitShifts = vec4(1.0, 1.0 / 255.0, 1.0 / 65025.0, 1.0 / 160581375.0);
    return step(v_shadowMapUv.z, dot(texture2D(u_shadowTexture, v_shadowMapUv.xy + offset), bitShifts));//+(1.0/255.0));	
}

float getShadow() 
{
	return (//getShadowness(vec2(0,0)) + 
			getShadowness(vec2(u_shadowPCFOffset, u_shadowPCFOffset)) +
			getShadowness(vec2(-u_shadowPCFOffset, u_shadowPCFOffset)) +
			getShadowness(vec2(u_shadowPCFOffset, -u_shadowPCFOffset)) +
			getShadowness(vec2(-u_shadowPCFOffset, -u_shadowPCFOffset))) * 0.25;
}
#endif //shadowMapFlag

#if defined(ambientFlag) && defined(separateAmbientFlag)
varying vec3 v_ambientLight;
#endif //separateAmbientFlag

#endif //lightingFlag

#ifdef fogFlag
uniform vec4 u_fogColor;
varying float v_fog;
#endif // fogFlag

void main() {
	#if defined(normalFlag) 
		vec3 normal = v_normal;
	#endif // normalFlag
		
	#if defined(diffuseTextureFlag) && defined(diffuseColorFlag) && defined(colorFlag)
		vec4 diffuse = texture2D(u_diffuseTexture, v_texCoords0) * u_diffuseColor * v_color;
	#elif defined(diffuseTextureFlag) && defined(diffuseColorFlag)
		vec4 diffuse = texture2D(u_diffuseTexture, v_texCoords0) * u_diffuseColor;
	#elif defined(diffuseTextureFlag) && defined(colorFlag)
		vec4 diffuse = texture2D(u_diffuseTexture, v_texCoords0) * v_color;
	#elif defined(diffuseTextureFlag)
		vec4 diffuse = texture2D(u_diffuseTexture, v_texCoords0);
	#elif defined(diffuseColorFlag) && defined(colorFlag)
		vec4 diffuse = u_diffuseColor * v_color;
	#elif defined(diffuseColorFlag)
		vec4 diffuse = u_diffuseColor;
	#elif defined(colorFlag)
		vec4 diffuse = v_color;
	#else
		vec4 diffuse = vec4(1.0);
	#endif

	#if (!defined(lightingFlag))  
		gl_FragColor.rgb = diffuse.rgb;
	#else
		//light diffuse and specular
		vec3 v_lightDiffuse;
		vec3 v_lightSpecular = vec3(0.0);
		
		//#ifdef specularFlag
		//	v_lightSpecular = vec3(0.0);
		//#endif // specularFlag
		
		#if defined(numSpotLights) && (numSpotLights > 0) && defined(normalFlag)
            for (int i = 0; i < numSpotLights; i++) {
								
                float NdotL = clamp(dot(normal, v_spotLights[i].direction), 0.0, 1.0);
                vec3 value = v_spotLights[i].color * (NdotL / (u_spotLights[i].constantAttenuation + u_spotLights[i].linearAttenuation * sqrt(v_spotLights[i].dist) + u_spotLights[i].quadraticAttenuation * v_spotLights[i].dist));
                
                float spotFactor = max(dot(-v_spotLights[i].direction, u_spotLights[i].direction), 0.0);
                if(spotFactor > cos(u_spotLights[i].cutOff)) {
                    //is inside the cone
                    value *= spotFactor * u_spotLights[i].exponent;
                    
                	v_lightDiffuse += value;
                }

                #ifdef specularFlag
                    float halfDotView = max(0.0, dot(normal, normalize(lightDir + viewVec)));
                    v_lightSpecular += value * pow(halfDotView, u_shininess);
                #endif // specularFlag
				
            }
        #endif // numSpotLights
		
		#if (!defined(specularFlag))
			#if defined(ambientFlag) && defined(separateAmbientFlag)
				#ifdef shadowMapFlag
					gl_FragColor.rgb = (diffuse.rgb * (v_ambientLight + getShadow() * v_lightDiffuse));
					//gl_FragColor.rgb = texture2D(u_shadowTexture, v_shadowMapUv.xy);
				#else
					gl_FragColor.rgb = (diffuse.rgb * (v_ambientLight + v_lightDiffuse));
				#endif //shadowMapFlag
			#else
				#ifdef shadowMapFlag
					gl_FragColor.rgb = getShadow() * (diffuse.rgb * v_lightDiffuse);
				#else
					gl_FragColor.rgb = (diffuse.rgb * v_lightDiffuse);
				#endif //shadowMapFlag
			#endif
		#else //specular
			#if defined(specularTextureFlag) && defined(specularColorFlag)
				vec3 specular = texture2D(u_specularTexture, v_texCoords0).rgb * u_specularColor.rgb * v_lightSpecular;
			#elif defined(specularTextureFlag)
				vec3 specular = texture2D(u_specularTexture, v_texCoords0).rgb * v_lightSpecular;
			#elif defined(specularColorFlag)
				vec3 specular = u_specularColor.rgb * v_lightSpecular;
			#else
				vec3 specular = v_lightSpecular;
			#endif
				
			#if defined(ambientFlag) && defined(separateAmbientFlag)
				#ifdef shadowMapFlag
				gl_FragColor.rgb = (diffuse.rgb * (getShadow() * v_lightDiffuse + v_ambientLight)) + specular;
					//gl_FragColor.rgb = texture2D(u_shadowTexture, v_shadowMapUv.xy);
				#else
					gl_FragColor.rgb = (diffuse.rgb * (v_lightDiffuse + v_ambientLight)) + specular;
				#endif //shadowMapFlag
			#else
				#ifdef shadowMapFlag
					gl_FragColor.rgb = getShadow() * ((diffuse.rgb * v_lightDiffuse) + specular);
				#else
					gl_FragColor.rgb = (diffuse.rgb * v_lightDiffuse) + specular;
				#endif //shadowMapFlag
			#endif
		#endif //specular Flag
	#endif //lightingFlag

	#ifdef fogFlag
		gl_FragColor.rgb = mix(gl_FragColor.rgb, u_fogColor.rgb, v_fog);
	#endif // end fogFlag

	#ifdef blendedFlag
		gl_FragColor.a = diffuse.a * v_opacity;
		#ifdef alphaTestFlag
			if (gl_FragColor.a <= v_alphaTest)
				discard;
		#endif
	#else
		gl_FragColor.a = 1.0;
	#endif

}
