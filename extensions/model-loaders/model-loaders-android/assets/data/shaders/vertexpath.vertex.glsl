#define normalsFlag
//#define specularColorFlag
//#define emissiveColorFlag
//#define diffuseColorFlag
//#define translucentFlag
//#define LIGHTS_NUM 4
attribute vec4 a_position; 
attribute vec2 a_texCoord0;

#ifdef normalsFlag
attribute vec3 a_normal;
uniform mat3 u_normalMatrix;
#endif

#ifdef diffuseColorFlag
uniform vec4 diffuseColor;
#endif

#ifdef emissiveColorFlag
uniform vec4 emissiveColor;
#endif

//#ifdef specularColor
//uniform vec4 specularCol;
//#endif



#if LIGHTS_NUM > 0
uniform vec3  lightsPos[LIGHTS_NUM];
uniform vec3  lightsCol[LIGHTS_NUM];
uniform float lightsInt[LIGHTS_NUM];
#endif
	
uniform vec3 dirLightDir;
uniform vec3 dirLightCol;
uniform mat4 u_projectionViewMatrix;
uniform mat4 u_modelMatrix;
uniform vec3 ambient;

varying vec2 v_texCoords;
varying vec4 v_diffuse;

//wrap light. this is fastest light model
float wrapLight(vec3 nor, vec3 direction){
	return dot(nor, direction) * 0.5 + 0.5;
}
void main()
{	
	v_texCoords = a_texCoord0; 		
	vec4 worldPos = u_modelMatrix * a_position;
	gl_Position = u_projectionViewMatrix * worldPos; 
	vec3 pos  = worldPos.xyz;	
	
	vec3  aggCol = dirLightCol;
	
	#ifdef normalsFlag
	vec3 normal = u_normalMatrix * a_normal;	
	aggCol *= wrapLight(normal, -dirLightDir);
	#endif

#if LIGHTS_NUM > 0		
	for ( int i = 0; i < LIGHTS_NUM; i++ ){	
		vec3 dif  = lightsPos[i] - pos;
		//fastest way to calculate inverse of length				
		float invLen = inversesqrt(dot(dif, dif));
		float weight = invLen * lightsInt[i];
				
		#ifdef normalsFlag
		vec3 L = invLen * dif;// normalize
		float lambert = wrapLight(normal, L);
		weight *= lambert;		
		#endif
		aggCol += lightsCol[i] * weight;
		
	}
#endif
#ifdef diffuseColorFlag
	aggCol *= diffuseColor.rgb;
#endif

#ifdef emissiveColorFlag
	aggCol += emissiveColor.rgb;
#endif
	
	aggCol += ambient;




#ifdef translucentFlag
	float alpha = diffuseColor.a;	
#else 
	float alpha = 1.0;
#endif


v_diffuse = vec4(clamp( aggCol, 0.0, 1.0), alpha);
}