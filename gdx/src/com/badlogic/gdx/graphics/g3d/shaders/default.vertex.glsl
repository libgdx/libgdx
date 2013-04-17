#if defined(diffuseTextureFlag) || defined(specularTextureFlag)
#define textureFlag
#endif

#if defined(specularTextureFlag) || defined(specularColorFlag)
#define specularFlag
#endif

attribute vec3 a_position;
uniform mat4 u_projTrans;
uniform mat4 u_modelTrans;

#ifdef colorFlag
attribute vec4 a_color;
varying vec4 v_color;
#endif // colorFlag

#ifdef normalFlag
attribute vec3 a_normal;
uniform mat3 u_normalMatrix;
varying vec3 v_normal;
#endif // normalFlag

#ifdef textureFlag
attribute vec2 a_texCoord0;
varying vec2 v_texCoords0;
#endif // textureFlag

#ifdef shininessFlag
uniform float shininess;
#else
const float shininess = 20.0;
#endif // shininessFlag

#ifdef lightingFlag
varying vec3 v_lightDiffuse;

#ifdef ambientLightFlag
uniform vec3 ambientLight;
#endif // ambientLightFlag

#ifdef ambientCubemapFlag
uniform vec3 ambientCubemap[6];
#endif // ambientCubemapFlag 

#ifdef specularFlag
varying vec3 v_lightSpecular;
uniform vec3 u_cameraPosition;
#endif // specularFlag

#if defined(numDirectionalLights) && (numDirectionalLights > 0)
struct DirectionalLight
{
	vec3 color;
	vec3 direction;
};
uniform DirectionalLight directionalLights[numDirectionalLights];
#endif // numDirectionalLights

#if defined(numPointLights) && (numPointLights > 0)
struct PointLight
{
	vec3 color;
	vec3 position;
	float intensity;
};
uniform PointLight pointLights[numPointLights];
#endif // numPointLights
#endif // lightingFlag

void main() {
	#ifdef textureFlag
		v_texCoords0 = a_texCoord0;
	#endif // textureFlag
	
	#ifdef colorFlag
		v_color = a_color;
	#endif // colorFlag
	
	vec4 pos = u_modelTrans * vec4(a_position, 1.0);
	gl_Position = u_projTrans * pos;
	
	#ifdef normalFlag
		v_normal = u_normalMatrix * a_normal;
	#endif // normalFlag

	#ifdef lightingFlag
		#ifdef ambientCubemapFlag		
			vec3 squaredNormal = v_normal * v_normal;
			vec3 isPositive  = step(0.0, v_normal);
			#ifdef ambientLightFlag
				v_lightDiffuse = ambientLight + squaredNormal.x * mix(ambientCubemap[0], ambientCubemap[1], isPositive.x) +
					squaredNormal.y * mix(ambientCubemap[2], ambientCubemap[3], isPositive.y) +
					squaredNormal.z * mix(ambientCubemap[4], ambientCubemap[5], isPositive.z);
			#else
				v_lightDiffuse = squaredNormal.x * mix(ambientCubemap[0], ambientCubemap[1], isPositive.x) +
					squaredNormal.y * mix(ambientCubemap[2], ambientCubemap[3], isPositive.y) +
					squaredNormal.z * mix(ambientCubemap[4], ambientCubemap[5], isPositive.z);
			#endif
		#elif defined(ambientLightFlag)
			v_lightDiffuse = ambientLight;
		#else
			v_lightDiffuse = vec3(0.0);
		#endif // ambient
		
		#ifdef specularFlag
			v_lightSpecular = vec3(0.0);
			vec3 viewVec = normalize(u_cameraPosition - pos.xyz);
		#endif // specularFlag
			
		#if defined(numDirectionalLights) && (numDirectionalLights > 0)
			for (int i = 0; i < numDirectionalLights; i++) {
				vec3 lightDir = -directionalLights[i].direction;
				float NdotL = clamp(dot(v_normal, lightDir), 0.0, 1.0);
				v_lightDiffuse += directionalLights[i].color * NdotL;
				#ifdef specularFlag
					float halfDotView = dot(v_normal, normalize(lightDir + viewVec));
					v_lightSpecular += directionalLights[i].color * clamp(NdotL * pow(halfDotView, shininess), 0.0, 1.0);
				#endif // specularFlag
			}
		#endif // numDirectionalLights

		#if defined(numPointLights) && (numPointLights > 0)
			for (int i = 0; i < numPointLights; i++) {
				vec3 lightDir = pointLights[i].position - pos.xyz;
				float distance = length(lightDir);
				lightDir /= distance;
				float NdotL = clamp(dot(v_normal, lightDir), 0.0, 1.0);
				float falloff = clamp(pointLights[i].intensity / (1.0 + distance), 0.0, 1.0);
				v_lightDiffuse += pointLights[i].color * (NdotL * falloff);
				#ifdef specularFlag
					float halfDotView = dot(v_normal, normalize(lightDir + viewVec));
					v_lightSpecular += pointLights[i].color * clamp(NdotL * pow(halfDotView, shininess) * falloff, 0.0, 1.0);
				#endif // specularFlag
			}
		#endif // numPointLights
	#endif // lightingFlag
}