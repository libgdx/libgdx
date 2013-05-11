#ifdef GL_ES 
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision mediump float;
#else
#define MED
#define LOWP
#endif

#if defined(normalTextureFlag)
#define phongFlag
#endif

#if defined(specularTextureFlag) || defined(specularColorFlag)
#define specularFlag
#endif

#ifdef normalFlag

varying vec3 v_normal;

#if defined(binormalFlag) || defined(tangentFlag) || defined(normalTextureFlag)
varying vec3 v_binormal;
varying vec3 v_tangent;
#endif //binormalFlag || tangentFlag

#endif //normalFlag

#if defined(colorFlag)
varying vec4 v_color;
#endif

#if defined(diffuseTextureFlag) || defined(specularTextureFlag) || defined(normalTextureFlag)
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

varying vec3 v_lightDiffuse;

#ifdef phongFlag

varying vec3 v_viewVec;
varying vec3 v_pos;

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
	float intensity;
};
uniform PointLight u_pointLights[numPointLights];
#endif // numPointLights

#else //phongFlag

#ifdef specularFlag
varying vec3 v_lightSpecular;
#endif //specularFlag

#endif //phongFlag	
#endif //lightingFlag
	

void main() {
	#if defined(normalFlag) && defined(normalTextureFlag)
		vec3 normal = normalize(2.0 + texture2D(u_normalTexture, v_texCoords0).xyz - 1.0);
		normal = normalize((v_tangent * normal.x) + (v_binormal * normal.y) + (v_normal * normal.z));
	#elif defined(normalFlag) 
		vec3 normal = v_normal;
	#elif defined(normalTextureFlag)
		vec3 normal = normalize(texture2D(u_normalTexture, v_texCoords0).xyz);
	#endif // normalFlag
		
	#if defined(diffuseTextureFlag) && defined(diffuseColorFlag) && defined(colorFlag)
		vec4 diffuse = texture2D(u_diffuseTexture, v_texCoords0) * u_diffuseColor * v_color;
	#elif defined(diffuseTextureFlag) && defined(diffuseColorFlag)
		vec4 diffuse = texture2D(u_diffuseTexture, v_texCoords0);
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

	#if !defined(specularFlag) || !defined(lightingFlag)
		gl_FragColor.rgb = diffuse.rgb;
		
	#elif defined(phongFlag)
		vec3 lightDiffuse = v_lightDiffuse;
		
		#ifdef specularFlag
			vec3 lightSpecular = vec3(0.0);
			#if defined(specularTextureFlag) && defined(specularColorFlag)
				vec3 specular = texture2D(u_specularTexture, v_texCoords0).rgb * u_specularColor.rgb;
			#elif defined(specularTextureFlag)
				vec3 specular = texture2D(u_specularTexture, v_texCoords0).rgb;
			#elif defined(specularColorFlag)
				vec3 specular = u_specularColor.rgb;
			#else //if defined(lightingFlag)
				vec3 specular = vec3(0.0);
			#endif
		#endif
			
		#if defined(numDirectionalLights) && (numDirectionalLights > 0) && (defined(normalFlag) || defined(normalTextureFlag))
			for (int i = 0; i < numDirectionalLights; i++) {
				vec3 lightDir = -u_dirLights[i].direction;
				float NdotL = clamp(dot(normal, lightDir), 0.0, 1.0);
				lightDiffuse.rgb += u_dirLights[i].color * NdotL;
				#ifdef specularFlag
					float halfDotView = dot(normal, normalize(lightDir + v_viewVec));
					lightSpecular += u_dirLights[i].color * clamp(NdotL * pow(halfDotView, u_shininess), 0.0, 1.0);
				#endif // specularFlag
			}
		#endif // numDirectionalLights
			
		#if defined(numPointLights) && (numPointLights > 0) && (defined(normalFlag) || defined(normalTextureFlag))
			for (int i = 0; i < numPointLights; i++) {
				vec3 lightDir = u_pointLights[i].position - v_pos;
				float dist2 = dot(lightDir, lightDir);
				lightDir *= inversesqrt(dist2);
				float NdotL = clamp(dot(normal, lightDir), 0.0, 2.0);
				float falloff = clamp(u_pointLights[i].intensity / (1.0 + dist2), 0.0, 2.0); // FIXME mul intensity on cpu
				lightDiffuse += u_pointLights[i].color * (NdotL * falloff);
				#ifdef specularFlag
					float halfDotView = clamp(dot(normal, normalize(lightDir + v_viewVec)), 0.0, 2.0);
					lightSpecular += u_pointLights[i].color * clamp(NdotL * pow(halfDotView, u_shininess) * falloff, 0.0, 2.0);
				#endif // specularFlag
			}
		#endif // numPointLights
		
		gl_FragColor.rgb = (diffuse.rgb * lightDiffuse) + (specular * lightSpecular);
		
	#else //!phongFlag
		#if defined(specularTextureFlag) && defined(specularColorFlag)
			vec3 specular = texture2D(u_specularTexture, v_texCoords0).rgb * u_specularColor.rgb * v_lightSpecular;
		#elif defined(specularTextureFlag)
			vec3 specular = texture2D(u_specularTexture, v_texCoords0).rgb * v_lightSpecular;
		#elif defined(specularColorFlag)
			vec3 specular = u_specularColor.rgb * v_lightSpecular;
		#else //if defined(lightingFlag)
			vec3 specular = v_lightSpecular;
		#endif
		gl_FragColor.rgb = diffuse.rgb * v_lightDiffuse + specular;
	#endif //phongFlag

	#ifdef blendedFlag
		gl_FragColor.a = diffuse.a;
	#endif
}