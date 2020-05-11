#ifdef GL_ES
precision mediump float;
#endif

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

uniform vec3 u_lightColor;
uniform vec3 u_lightDirection;

#ifdef spotLight
	uniform vec3 u_lightPosition;
	uniform float u_lightIntensity;
	uniform float u_lightCutoffAngle;
	uniform float u_lightExponent;
#endif

varying vec3 v_pos;

#if defined(normalFlag)
	varying vec3 v_normal;
#endif // normalFlag


const float u_shininess = 10.0;

uniform sampler2D u_shadowTexture;
uniform vec4 u_uvTransform;
varying vec4 v_shadowMapUv;

float unpack (vec4 colour) {
	const vec4 bitShifts = vec4(1.0 / (256.0 * 256.0 * 256.0),
								1.0 / (256.0 * 256.0),
								1.0 / 256.0,
								1);
	return dot(colour , bitShifts);
}

void main()
{
	const float bias = 0.005;
	vec3 depth = (v_shadowMapUv.xyz / v_shadowMapUv.w)*0.5+0.5;
	vec2 uv = u_uvTransform.xy + depth.xy * u_uvTransform.zw;
	float lenDepthMap = unpack(texture2D(u_shadowTexture, uv));

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
		vec3 lightSpecular = vec3(0.0);
		vec3 lightDiffuse = vec3(0.0);
	#else
		vec3 lightSpecular = vec3(1.0);
		vec3 lightDiffuse = vec3(1.0);
	#endif

	#ifdef directionalLight
		if (depth.x >= 0.0 &&
			depth.x <= 1.0 &&
			depth.y >= 0.0 &&
			depth.y <= 1.0
		) {
			if( depth.z - lenDepthMap > bias ) {
				vec3 lightDir = -u_lightDirection;
				// Diffuse
				float NdotL = clamp(dot(normal, lightDir), 0.0, 1.0);
				lightDiffuse.rgb += u_lightColor * NdotL;

				// Specular
				#ifdef specularTextureFlag
					float halfDotView = clamp(dot(normal, normalize(lightDir + v_viewVec)), 0.0, 2.0);
					lightSpecular += u_lightColor * clamp(NdotL * pow(halfDotView, u_shininess), 0.0, 2.0);
				#endif
			}
		}

	#endif


	// Spot Lights
	#ifdef spotLight
		if (depth.x >= 0.0 &&
			depth.x <= 1.0 &&
			depth.y >= 0.0 &&
			depth.y <= 1.0 &&
			v_shadowMapUv.z >= 0.0
		) {
			if( depth.z - lenDepthMap > bias ) {
				vec3 lightDir = u_lightPosition - v_pos;

				float spotEffect = dot(-normalize(lightDir), normalize(u_lightDirection));
				if ( spotEffect  > cos(radians(u_lightCutoffAngle)) ) {
					spotEffect = max( pow( max( spotEffect, 0.0 ), u_lightExponent ), 0.0 );
					float dist2 = dot(lightDir, lightDir);
					lightDir *= inversesqrt(dist2);
					float NdotL = clamp(dot(normal, lightDir), 0.0, 2.0);
					float falloff = clamp(u_lightIntensity / (1.0 + dist2), 0.0, 2.0);

					// Diffuse
					lightDiffuse += u_lightColor * (NdotL * falloff) * spotEffect;

					// Specular
					#ifdef specularTextureFlag
						float halfDotView = clamp(dot(normal, normalize(lightDir + v_viewVec)), 0.0, 2.0);
						lightSpecular += u_lightColor * clamp(NdotL * pow(halfDotView, u_shininess) * falloff, 0.0, 2.0) * spotEffect;
					#endif
				}
			}
		}
	#endif

	gl_FragColor.rgb = ((diffuse.rgb * lightDiffuse) + (specular * lightSpecular));
}
