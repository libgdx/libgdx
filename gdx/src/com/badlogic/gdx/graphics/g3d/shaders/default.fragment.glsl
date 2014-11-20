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

#if (defined(bumpTextureFlag) || (defined(normalTextureFlag) && ((defined(numDirectionalLights) && (numDirectionalLights > 0)) || (defined(numPointLights) && (numPointLights > 0))))) && defined(normalFlag) && defined(binormalFlag) && defined(tangentFlag)
#define fragmentLightingFlag
#endif

#if defined(bumpTextureFlag)
#define cameraPositionFlag
#endif

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
#endif

#ifdef diffuseTextureFlag
varying MED vec2 v_diffuseUV;
#endif

#ifdef specularTextureFlag
varying MED vec2 v_specularUV;
#endif

#if defined(normalTextureFlag) && defined(fragmentLightingFlag)
varying MED vec2 v_normalUV;
#endif

#ifdef bumpTextureFlag
varying MED vec2 v_bumpUV;
#endif

#ifdef bumpScaleFlag
uniform float u_bumpScale;
#else
const float u_bumpScale = 0.05;
#endif

#ifdef bumpBiasFlag
uniform float u_bumpBias;
#else
const float u_bumpBias = 0.5;
#endif

#if defined(tangentFlag) && defined(fragmentLightingFlag)
varying vec3 v_tangent;
#endif

#if defined(binormalFlag) && defined(fragmentLightingFlag)
varying vec3 v_binormal;
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

#ifdef bumpTextureFlag
uniform sampler2D u_bumpTexture;
#endif

#ifdef lightingFlag
varying vec3 v_lightDiffuse;

#if	defined(ambientLightFlag) || defined(ambientCubemapFlag) || defined(sphericalHarmonicsFlag)
#define ambientFlag
#endif //ambientFlag

#ifdef specularFlag
varying vec3 v_lightSpecular;
#if defined(fragmentLightingFlag)
varying vec3 v_viewVec;
#endif
#endif //specularFlag

#ifdef cameraPositionFlag
uniform vec4 u_cameraPosition;
#endif // cameraPositionFlag

#if defined(fragmentLightingFlag)
varying vec4 v_pos;

#ifdef shininessFlag
uniform float u_shininess;
#else
const float u_shininess = 20.0;
#endif // shininessFlag

#if defined(numDirectionalLights) && (numDirectionalLights > 0)
struct DirectionalLight
{
	vec3 color;
	vec3 direction;
};
uniform DirectionalLight u_dirLights[numDirectionalLights];
#endif // numDirectionalLights

#if defined(numPointLights) && (numPointLights > 0)
struct PointLight
{
	vec3 color;
	vec3 position;
};
uniform PointLight u_pointLights[numPointLights];
#endif // numPointLights
#endif // fragmentLightingFlag

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
	#if defined(diffuseTextureFlag)
		vec2 diffuseUV = v_diffuseUV;
	#endif
	#if defined(specularTextureFlag)
		vec2 specularUV = v_specularUV;
	#endif
	#if defined(normalTextureFlag)
		vec2 normalUV = v_normalUV;
	#endif

	#if defined(normalFlag)
		vec3 normal = v_normal;
	#endif

	#if defined(fragmentLightingFlag)
		vec3 tangent = normalize(v_tangent);
		vec3 binormal = normalize(v_binormal);

		mat3 tangentSpace = mat3(tangent, binormal, normal);

		#if defined(bumpTextureFlag)
			vec3 eyeVector = normalize((u_cameraPosition - v_pos).xyz * tangentSpace);
			float height = (texture2D(u_bumpTexture, v_bumpUV.st).r - u_bumpBias) * u_bumpScale;
			vec2 offset = (eyeVector.xy * height);
			#if defined(diffuseTextureFlag)
				diffuseUV += offset;
			#endif
			#if defined(specularTextureFlag)
				specularUV += offset;
			#endif
			#if defined(normalTextureFlag)
				normalUV += offset;
			#endif
		#endif

		#if defined(normalTextureFlag)
			normal = texture2D(u_normalTexture, normalUV).xyz;
			normal.z = normal.z * 2.0 - 1.0;
			normal = tangentSpace * normal;
		#endif

		//gl_FragColor.rgb = normal;
		//gl_FragColor.a = 1.0;
		//return;
	#endif // fragmentLightingFlag

	#if defined(diffuseTextureFlag) && defined(diffuseColorFlag) && defined(colorFlag)
		vec4 diffuse = texture2D(u_diffuseTexture, diffuseUV) * u_diffuseColor * v_color;
	#elif defined(diffuseTextureFlag) && defined(diffuseColorFlag)
		vec4 diffuse = texture2D(u_diffuseTexture, diffuseUV) * u_diffuseColor;
	#elif defined(diffuseTextureFlag) && defined(colorFlag)
		vec4 diffuse = texture2D(u_diffuseTexture, diffuseUV) * v_color;
	#elif defined(diffuseTextureFlag)
		vec4 diffuse = texture2D(u_diffuseTexture, diffuseUV);
	#elif defined(diffuseColorFlag) && defined(colorFlag)
		vec4 diffuse = u_diffuseColor * v_color;
	#elif defined(diffuseColorFlag)
		vec4 diffuse = u_diffuseColor;
	#elif defined(colorFlag)
		vec4 diffuse = v_color;
	#else
		vec4 diffuse = vec4(1.0);
	#endif

	#ifdef lightingFlag
		vec3 lightDiffuse = v_lightDiffuse;
		#ifdef specularFlag
			vec3 lightSpecular = v_lightSpecular;
		#endif

		#if defined(fragmentLightingFlag)
			#ifdef specularFlag
				vec3 viewVec = v_viewVec;
			#endif

			vec4 pos = v_pos;

			#if defined(numDirectionalLights) && (numDirectionalLights > 0) && defined(normalFlag)
				for (int i = 0; i < numDirectionalLights; i++) {
					vec3 lightDir = -u_dirLights[i].direction;
					float NdotL = clamp(dot(normal, lightDir), 0.0, 1.0);
					vec3 value = u_dirLights[i].color * NdotL;
					lightDiffuse += value;
					#ifdef specularFlag
						float halfDotView = max(0.0, dot(normal, normalize(lightDir + viewVec)));
						lightSpecular += value * pow(halfDotView, u_shininess);
					#endif // specularFlag
				}
		    #endif // numDirectionalLights

		    #if defined(numPointLights) && (numPointLights > 0) && defined(normalFlag)
				for (int i = 0; i < numPointLights; i++) {
					vec3 lightDir = u_pointLights[i].position - pos.xyz;
					float dist2 = dot(lightDir, lightDir);
					lightDir *= inversesqrt(dist2);
					float NdotL = clamp(dot(normal, lightDir), 0.0, 1.0);
					vec3 value = u_pointLights[i].color * (NdotL / (1.0 + dist2));
					lightDiffuse += value;
					#ifdef specularFlag
						float halfDotView = max(0.0, dot(normal, normalize(lightDir + viewVec)));
						lightSpecular += value * pow(halfDotView, u_shininess);
					#endif // specularFlag
				}
			#endif // numPointLights
		#endif
	#endif

	#if (!defined(lightingFlag))
		gl_FragColor.rgb = diffuse.rgb;
	#elif (!defined(specularFlag))
		#if defined(ambientFlag) && defined(separateAmbientFlag)
			#ifdef shadowMapFlag
				gl_FragColor.rgb = (diffuse.rgb * (v_ambientLight + getShadow() * lightDiffuse));
				//gl_FragColor.rgb = texture2D(u_shadowTexture, v_shadowMapUv.xy);
			#else
				gl_FragColor.rgb = (diffuse.rgb * (v_ambientLight + lightDiffuse));
			#endif //shadowMapFlag
		#else
			#ifdef shadowMapFlag
				gl_FragColor.rgb = getShadow() * (diffuse.rgb * lightDiffuse);
			#else
				gl_FragColor.rgb = (diffuse.rgb * lightDiffuse);
			#endif //shadowMapFlag
		#endif
	#else
		#if defined(specularTextureFlag) && defined(specularColorFlag)
			vec3 specular = texture2D(u_specularTexture, specularUV).rgb * u_specularColor.rgb * lightSpecular;
		#elif defined(specularTextureFlag)
			vec3 specular = texture2D(u_specularTexture, specularUV).rgb * lightSpecular;
		#elif defined(specularColorFlag)
			vec3 specular = u_specularColor.rgb * lightSpecular;
		#else
			vec3 specular = lightSpecular;
		#endif

		#if defined(ambientFlag) && defined(separateAmbientFlag)
			#ifdef shadowMapFlag
			gl_FragColor.rgb = (diffuse.rgb * (getShadow() * lightDiffuse + v_ambientLight)) + specular;
				//gl_FragColor.rgb = texture2D(u_shadowTexture, v_shadowMapUv.xy);
			#else
				gl_FragColor.rgb = (diffuse.rgb * (lightDiffuse + v_ambientLight)) + specular;
			#endif //shadowMapFlag
		#else
			#ifdef shadowMapFlag
				gl_FragColor.rgb = getShadow() * ((diffuse.rgb * lightDiffuse) + specular);
			#else
				gl_FragColor.rgb = (diffuse.rgb * lightDiffuse) + specular;
			#endif //shadowMapFlag
		#endif
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
