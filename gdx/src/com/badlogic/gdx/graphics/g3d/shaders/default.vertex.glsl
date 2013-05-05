#if defined(diffuseTextureFlag) || defined(specularTextureFlag)
#define textureFlag
#endif

#if defined(specularTextureFlag) || defined(specularColorFlag)
#define specularFlag
#endif

attribute vec3 a_position;
uniform mat4 u_projTrans;

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

#ifdef boneWeight0Flag
#define boneWeightsFlag
attribute vec2 a_boneWeight0;
#endif //boneWeight0Flag

#ifdef boneWeight1Flag
#define boneWeightsFlag
attribute vec2 a_boneWeight1;
#endif //boneWeight1Flag

#ifdef boneWeight2Flag
#define boneWeightsFlag
attribute vec2 a_boneWeight2;
#endif //boneWeight2Flag

#ifdef boneWeight3Flag
#define boneWeightsFlag
attribute vec2 a_boneWeight3;
#endif //boneWeight3Flag

#ifdef boneWeight4Flag
#define boneWeightsFlag
attribute vec2 a_boneWeight4;
#endif //boneWeight4Flag

#ifdef boneWeight5Flag
#define boneWeightsFlag
attribute vec2 a_boneWeight5;
#endif //boneWeight5Flag

#ifdef boneWeight6Flag
#define boneWeightsFlag
attribute vec2 a_boneWeight6;
#endif //boneWeight6Flag

#ifdef boneWeight7Flag
#define boneWeightsFlag
attribute vec2 a_boneWeight7;
#endif //boneWeight7Flag

#if defined(numBones) && defined(boneWeightsFlag)
#if (numBones > 0) 
#define skinningFlag
#endif
#endif

#ifdef skinningFlag
uniform mat4 u_localTrans;
uniform mat4 u_modelTrans;
#else
uniform mat4 u_worldTrans;
#endif //skinningFlag

#if defined(numBones)
#if numBones > 0
uniform mat4 u_bones[numBones];
#endif //numBones
#endif

#ifdef shininessFlag
uniform float u_shininess;
#else
const float u_shininess = 20.0;
#endif // shininessFlag

#ifdef lightingFlag
varying vec3 v_lightDiffuse;

#ifdef ambientLightFlag
uniform vec3 u_ambientLight;
#endif // ambientLightFlag

#ifdef ambientCubemapFlag
uniform vec3 u_ambientCubemap[6];
#endif // ambientCubemapFlag 

#ifdef sphericalHarmonicsFlag
uniform vec3 u_sphericalHarmonics[9];
#endif //sphericalHarmonicsFlag

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
#endif // lightingFlag

void main() {
	#ifdef textureFlag
		v_texCoords0 = a_texCoord0;
	#endif // textureFlag
	
	#ifdef colorFlag
		v_color = a_color;
	#endif // colorFlag
	
	#ifdef skinningFlag
		mat4 skinning;
		#ifdef boneWeight0Flag
			skinning += (a_boneWeight0.y) * u_bones[int(a_boneWeight0.x)];
		#endif //boneWeight0Flag
		#ifdef boneWeight1Flag				
			skinning += (a_boneWeight1.y) * u_bones[int(a_boneWeight1.x)];
		#endif //boneWeight1Flag
		#ifdef boneWeight2Flag		
			skinning += (a_boneWeight2.y) * u_bones[int(a_boneWeight2.x)];
		#endif //boneWeight2Flag
		#ifdef boneWeight3Flag
			skinning += (a_boneWeight3.y) * u_bones[int(a_boneWeight3.x)];
		#endif //boneWeight3Flag
		#ifdef boneWeight4Flag
			skinning += (a_boneWeight4.y) * u_bones[int(a_boneWeight4.x)];
		#endif //boneWeight4Flag
		#ifdef boneWeight5Flag
			skinning += (a_boneWeight5.y) * u_bones[int(a_boneWeight5.x)];
		#endif //boneWeight5Flag
		#ifdef boneWeight6Flag
			skinning += (a_boneWeight6.y) * u_bones[int(a_boneWeight6.x)];
		#endif //boneWeight6Flag
		#ifdef boneWeight7Flag
			skinning += (a_boneWeight7.y) * u_bones[int(a_boneWeight7.x)];
		#endif //boneWeight7Flag
	#endif //skinningFlag

	#ifdef skinningFlag
		vec4 pos = u_modelTrans * ((skinning * u_localTrans) * vec4(a_position, 1.0));
	#else
		vec4 pos = u_worldTrans * vec4(a_position, 1.0);
	#endif
	gl_Position = u_projTrans * pos; // FIXME dont use a temp pos value (<kalle_h> this causes some vertex yittering with positions as low as 300)

	#if defined(normalFlag) && defined(skinningFlag)
		v_normal = normalize((skinning * vec4(a_normal, 0.0)).xyz);
	#elif defined(normalFlag)
		v_normal = normalize(u_normalMatrix * a_normal);
	#endif // normalFlag

	#ifdef lightingFlag
		#ifdef ambientLightFlag
			v_lightDiffuse = u_ambientLight;
		#else
			v_lightDiffuse = vec3(0.0);
		#endif // ambientLightFlag
			
		#ifdef ambientCubemapFlag 		
			vec3 squaredNormal = v_normal * v_normal;
			vec3 isPositive  = step(0.0, v_normal);
			v_lightDiffuse += squaredNormal.x * mix(u_ambientCubemap[0], u_ambientCubemap[1], isPositive.x) +
					squaredNormal.y * mix(u_ambientCubemap[2], u_ambientCubemap[3], isPositive.y) +
					squaredNormal.z * mix(u_ambientCubemap[4], u_ambientCubemap[5], isPositive.z);
		#endif // ambientCubemapFlag

		#ifdef sphericalHarmonicsFlag
			v_lightDiffuse += u_sphericalHarmonics[0];
			v_lightDiffuse += u_sphericalHarmonics[1] * normal.x;
			v_lightDiffuse += u_sphericalHarmonics[2] * normal.y;
			v_lightDiffuse += u_sphericalHarmonics[3] * normal.z;
			v_lightDiffuse += u_sphericalHarmonics[4] * (normal.x * normal.z);
			v_lightDiffuse += u_sphericalHarmonics[5] * (normal.z * normal.y);
			v_lightDiffuse += u_sphericalHarmonics[6] * (normal.y * normal.x);
			v_lightDiffuse += u_sphericalHarmonics[7] * (3.0 * normal.z * normal.z - 1.0);
			v_lightDiffuse += u_sphericalHarmonics[8] * (normal.x * normal.x - normal.y * normal.y);			
		#endif // sphericalHarmonicsFlag
		
		#ifdef specularFlag
			v_lightSpecular = vec3(0.0);
			vec3 viewVec = normalize(u_cameraPosition - pos.xyz);
		#endif // specularFlag
			
		#if defined(numDirectionalLights) && (numDirectionalLights > 0) && defined(normalFlag)
			for (int i = 0; i < numDirectionalLights; i++) {
				vec3 lightDir = -u_dirLights[i].direction;
				float NdotL = clamp(dot(v_normal, lightDir), 0.0, 1.0);
				v_lightDiffuse += u_dirLights[i].color * NdotL;
				#ifdef specularFlag
					float halfDotView = dot(v_normal, normalize(lightDir + viewVec));
					v_lightSpecular += u_dirLights[i].color * clamp(NdotL * pow(halfDotView, u_shininess), 0.0, 1.0);
				#endif // specularFlag
			}
		#endif // numDirectionalLights

		#if defined(numPointLights) && (numPointLights > 0) && defined(normalFlag)
			for (int i = 0; i < numPointLights; i++) {
				vec3 lightDir = u_pointLights[i].position - pos.xyz;
				float dist2 = dot(lightDir, lightDir);
				lightDir *= inversesqrt(dist2);
				float NdotL = clamp(dot(v_normal, lightDir), 0.0, 2.0);
				float falloff = clamp(u_pointLights[i].intensity / (1.0 + dist2), 0.0, 2.0); // FIXME mul intensity on cpu
				v_lightDiffuse += u_pointLights[i].color * (NdotL * falloff);
				#ifdef specularFlag
					float halfDotView = clamp(dot(v_normal, normalize(lightDir + viewVec)), 0.0, 2.0);
					v_lightSpecular += u_pointLights[i].color * clamp(NdotL * pow(halfDotView, u_shininess) * falloff, 0.0, 2.0);
				#endif // specularFlag
			}
		#endif // numPointLights
	#endif // lightingFlag
}