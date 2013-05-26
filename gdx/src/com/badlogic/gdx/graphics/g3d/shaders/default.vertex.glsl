void main() {
	#ifdef textureFlag
		v_texCoords0 = a_texCoord0;
	#endif // textureFlag
	
	#if defined(colorFlag)
		v_color = a_color;
	#endif // colorFlag
		
	#ifdef blendedFlag
		v_opacity = u_opacity;
		#ifdef alphaTestFlag
			v_alphaTest = u_alphaTest;
		#endif //alphaTestFlag
	#endif // blendedFlag

	#ifdef skinningFlag
		mat4 skinning = mat4(0.0);
		calculateSkinning(skinning);
		vec4 pos = u_worldTrans * skinning * vec4(a_position, 1.0);
	#else
		vec4 pos = u_worldTrans * vec4(a_position, 1.0);
	#endif
	gl_Position = u_projTrans * pos; // FIXME dont use a temp pos value (<kalle_h> this causes some vertex yittering with positions as low as 300)
	
	#if defined(normalFlag)
		#if defined(skinningFlag)
			vec3 normal = normalize((u_worldTrans * skinning * vec4(a_normal, 0.0)).xyz);
		#else
			vec3 normal = normalize(u_normalMatrix * a_normal);
		#endif
		v_normal = normal;
	#endif // normalFlag
		
	#ifdef lightingFlag
		calculateLighting(normal, pos);
	#endif //lightingFlag
}