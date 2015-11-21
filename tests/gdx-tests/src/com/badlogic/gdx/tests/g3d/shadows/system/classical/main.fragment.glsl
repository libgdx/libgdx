#ifdef GL_ES
precision mediump float;
#endif

const float u_shininess = 10.0;

#if defined(diffuseTextureFlag) || defined(specularTextureFlag)
#define textureFlag
#endif

#if defined(specularTextureFlag) || defined(specularColorFlag)
#define specularFlag
#endif

#if defined(specularFlag) || defined(fogFlag)
#define cameraPositionFlag
#endif

#if defined(colorFlag)
varying vec4 v_color;
#endif


#ifdef diffuseTextureFlag
uniform sampler2D u_diffuseTexture;
varying vec2 v_diffuseUV;
#endif

#ifdef specularTextureFlag
uniform sampler2D u_specularTexture;
varying vec2 v_specularUV;
varying vec3 v_viewVec;
#endif

#ifdef normalTextureFlag
uniform sampler2D u_normalTexture;
varying vec2 v_normalUV;
varying vec3 v_binormal;
varying vec3 v_tangent;
#endif

#ifdef diffuseColorFlag
uniform vec4 u_diffuseColor;
#endif

#ifdef numDirectionalLights
#if numDirectionalLights > 0
struct DirectionalLight
{
	vec3 color;
	vec3 direction;
	float intensity;
};
uniform DirectionalLight u_dirLights[numDirectionalLights];

#endif
#endif // numDirectionalLights

#ifdef numPointLights
#if numPointLights > 0

struct PointLight
{
	vec3 color;
	vec3 position;
	float intensity;
};
uniform PointLight u_pointLights[numPointLights];

#endif
#endif // numPointLights

#ifdef numSpotLights
#if numSpotLights > 0
struct SpotLight
{
	vec3 color;
	vec3 position;
	vec3 direction;
	float intensity;
	float cutoffAngle;
	float exponent;
};
uniform SpotLight u_spotLights[numSpotLights];

#endif
#endif // numSpotLights

varying vec3 v_pos;

#if defined(normalFlag)
	varying vec3 v_normal;
#endif // normalFlag


uniform sampler2D u_shadowTexture;
uniform vec2 u_resolution;

vec3 getShadow() {
	vec2 c = gl_FragCoord.xy;
	c.x /= u_resolution.x;
	c.y /= u_resolution.y;
	return texture2D(u_shadowTexture, c).rgb;
}

// Clamp each color pass to respect the shadow clamping
// Pass2 clamps each light
void main() {

	#if defined(normalTextureFlag)
		vec3 normal = normalize(texture2D(u_normalTexture, v_normalUV).rgb * 2.0 - 1.0);
		normal = normalize((v_tangent * normal.x) + (v_binormal * normal.y) + (v_normal * normal.z));
	#elif defined(normalFlag)
		vec3 normal = v_normal;
	#endif

	#if defined(specularTextureFlag)
		vec3 specular = texture2D(u_specularTexture, v_specularUV).rgb;
	#else
		vec3 specular = vec3(0.0);
	#endif

	#if defined(diffuseTextureFlag) && defined(diffuseColorFlag) && defined(colorFlag)
		vec4 diffuse = texture2D(u_diffuseTexture, v_diffuseUV) * u_diffuseColor * v_color;
	#elif defined(diffuseTextureFlag) && defined(diffuseColorFlag)
		vec4 diffuse = texture2D(u_diffuseTexture, v_diffuseUV) * u_diffuseColor;
	#elif defined(diffuseTextureFlag) && defined(colorFlag)
		vec4 diffuse = texture2D(u_diffuseTexture, v_diffuseUV) * v_color;
	#elif defined(diffuseTextureFlag)
		vec4 diffuse = texture2D(u_diffuseTexture, v_diffuseUV);
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
		vec3 finalColor = vec3(0.0);
		vec3 tmpColor = vec3(0.0);
		const float bias = 0.01;
	#else
		vec3 finalColor = diffuse.rgb + specular;
	#endif

	// Directional Lights
	#ifdef numDirectionalLights
	#if numDirectionalLights > 0
		for (int i = 0; i < numDirectionalLights; i++) {
			tmpColor = vec3(0.0);
			vec3 lightDir = -u_dirLights[i].direction;

			// Diffuse
			float NdotL = clamp(dot(normal, lightDir), 0.0, 1.0);
			tmpColor += diffuse.rgb * u_dirLights[i].color * NdotL;

			// Specular
			#ifdef specularTextureFlag
				float halfDotView = clamp(dot(normal, normalize(lightDir + v_viewVec)), 0.0, 2.0);
				tmpColor += specular* u_dirLights[i].color * clamp(NdotL * pow(halfDotView, u_shininess), 0.0, 2.0);
			#endif

			finalColor += clamp(tmpColor, 0.0, 1.0);
		}
	#endif
	#endif // numDirectionalLights


	// Spot Lights
	#ifdef numSpotLights
	#if numSpotLights > 0
		for (int i = 0; i < numSpotLights; i++) {
			tmpColor = vec3(0.0);
			vec3 lightDir = u_spotLights[i].position - v_pos;
			float spotEffect = dot(-normalize(lightDir), normalize(u_spotLights[i].direction));

			if ( spotEffect  > cos(radians(u_spotLights[i].cutoffAngle)) ) {
				spotEffect = max( pow( max( spotEffect, 0.0 ), u_spotLights[i].exponent ), 0.0 );
				float dist2 = dot(lightDir, lightDir);
				lightDir *= inversesqrt(dist2);
				float NdotL = clamp(dot(normal, lightDir), 0.0, 2.0);
				float falloff = clamp(u_spotLights[i].intensity / (1.0 + dist2), 0.0, 2.0);

				// Diffuse
				tmpColor += diffuse.rgb * u_spotLights[i].color * (NdotL * falloff) * spotEffect;

				// Specular
				#ifdef specularTextureFlag
					float halfDotView = clamp(dot(normal, normalize(lightDir + v_viewVec)), 0.0, 2.0);
					tmpColor += specular * u_spotLights[i].color * clamp(NdotL * pow(halfDotView, u_shininess) * falloff, 0.0, 2.0) * spotEffect;
				#endif
			}

			finalColor += clamp(tmpColor, 0.0, 1.0);
		}
	#endif
	#endif // numSpotLights

	// Point Lights
	#ifdef numPointLights
	#if numPointLights > 0
		for (int i = 0; i < numPointLights; i++) {
			tmpColor = vec3(0.0);
			vec3 lightDir = u_pointLights[i].position - v_pos;
			float dist2 = dot(lightDir, lightDir);
			lightDir *= inversesqrt(dist2);
			float NdotL = clamp(dot(normal, lightDir), 0.0, 2.0);
			float falloff = clamp(u_pointLights[i].intensity / (1.0 + dist2), 0.0, 2.0);

			// Diffuse
			tmpColor += diffuse.rgb * u_pointLights[i].color * (NdotL * falloff);

			// Specular
			#ifdef specularTextureFlag
				float halfDotView = clamp(dot(normal, normalize(lightDir + v_viewVec)), 0.0, 2.0);
				tmpColor += specular * u_pointLights[i].color * clamp(NdotL * pow(halfDotView, u_shininess) * falloff, 0.0, 2.0);
			#endif

			finalColor += clamp(tmpColor, 0.0, 1.0);
		}
	#endif
	#endif // numPointLights

	gl_FragColor.rgb = finalColor - getShadow();
}
