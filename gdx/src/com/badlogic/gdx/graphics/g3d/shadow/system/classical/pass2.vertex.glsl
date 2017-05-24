#if defined(diffuseTextureFlag) || defined(specularTextureFlag)
#define textureFlag
#endif

#if defined(specularTextureFlag) || defined(specularColorFlag)
#define specularFlag
#endif

#if defined(specularFlag) || defined(fogFlag)
#define cameraPositionFlag
#endif


attribute vec3 a_position;
uniform mat4 u_projViewTrans;


#if defined(colorFlag)
varying vec4 v_color;
attribute vec4 a_color;
#endif // colorFlag

#ifdef normalFlag
attribute vec3 a_normal;
uniform mat3 u_normalMatrix;
varying vec3 v_normal;
#endif // normalFlag

#ifdef textureFlag
attribute vec2 a_texCoord0;
#endif // textureFlag

#ifdef diffuseTextureFlag
uniform vec4 u_diffuseUVTransform;
varying vec2 v_diffuseUV;
#endif

#ifdef normalTextureFlag
uniform vec4 u_normalUVTransform;
varying vec2 v_normalUV;
varying vec3 v_binormal;
varying vec3 v_tangent;
#endif

#ifdef specularTextureFlag
uniform vec4 u_specularUVTransform;
varying vec2 v_specularUV;
varying vec3 v_viewVec;
#endif

uniform mat4 u_worldTrans;


#ifdef lightingFlag
varying vec3 v_lightDiffuse;

#ifdef specularFlag
varying vec3 v_lightSpecular;
#endif // specularFlag

#ifdef cameraPositionFlag
uniform vec4 u_cameraPosition;
#endif // cameraPositionFlag

#endif // lightingFlag

varying vec3 v_pos;

uniform mat4 u_shadowMapProjViewTrans;
varying vec4 v_shadowMapUv;

void main()
{
	vec4 pos = u_worldTrans * vec4(a_position, 1.0);
	v_shadowMapUv = u_shadowMapProjViewTrans * pos;
	v_pos = pos.xyz;

	#ifdef diffuseTextureFlag
		v_diffuseUV = u_diffuseUVTransform.xy + a_texCoord0 * u_diffuseUVTransform.zw;
	#endif //diffuseTextureFlag

	#ifdef specularTextureFlag
		v_specularUV = u_specularUVTransform.xy + a_texCoord0 * u_specularUVTransform.zw;
	#endif //specularTextureFlag


	#if defined(normalTextureFlag)
		v_normalUV = u_normalUVTransform.xy + a_texCoord0 * u_normalUVTransform.zw;
		v_normal = normalize(u_normalMatrix * a_normal);

		vec3 c1 = cross(v_normal, vec3(0.0, 0.0, 1.0));
		vec3 c2 = cross(v_normal, vec3(0.0, 1.0, 0.0));

		if(length(c1)>length(c2)) {
			v_tangent = c1;
		}
		else {
			v_tangent = c2;
		}
		v_tangent = normalize(v_tangent);
		v_binormal = normalize(cross(v_normal, v_tangent));
		#ifdef specularTextureFlag
			v_viewVec = normalize(u_cameraPosition.xyz - pos.xyz);
		#endif
	#elif defined(normalFlag)
		v_normal = normalize(u_normalMatrix * a_normal);
	#endif

	#if defined(colorFlag)
		v_color = a_color;
	#endif // colorFlag

	gl_Position = u_projViewTrans * pos;
}