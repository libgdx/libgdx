#define normals
//#define specularColor
//#define emissiveColor
//#define diffuseColor
//#define LIGHTS_NUM 4
attribute vec4 a_position; 
attribute vec2 a_texCoord0;

#ifdef diffuseColor
uniform vec3 diffuseCol;
#endif

#ifdef emissiveColor
uniform vec3 emissiveCol;
#endif

//#ifdef specularColor
//uniform vec3 specularCol;
//#endif

#ifdef normals
attribute vec3 a_normal;
uniform mat3 u_normalMatrix;
#endif

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
	
	#ifdef normals
	vec3 normal = u_normalMatrix * a_normal;	
	aggCol *= wrapLight(normal, -dirLightDir);
	#endif

#if LIGHTS_NUM > 0		
	for ( int i = 0; i < LIGHTS_NUM; i++ ){	
		vec3 dif  = lightsPos[i] - pos;
		//fastest way to calculate inverse of length				
		float invLen = inversesqrt(dot(dif, dif));
		float weight = invLen * lightsInt[i];
				
		#ifdef normals
		vec3 L = invLen * dif;// normalize
		float lambert = wrapLight(normal, L);
		weight *= lambert;		
		#endif
		aggCol += lightsCol[i] * weight;
		
	}
#endif
#ifdef diffuseColor
	aggCol *= diffuseCol;
#endif

#ifdef emissiveColor
	aggCol += emissiveCol;
#endif
	
	aggCol += ambient;
	
	v_diffuse = vec4(clamp( aggCol, 0.0,1.0), 1.0);	
}