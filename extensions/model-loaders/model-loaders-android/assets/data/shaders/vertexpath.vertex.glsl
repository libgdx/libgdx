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

uniform vec3  lightsPos[LIGHTS_NUM];
uniform vec3  lightsCol[LIGHTS_NUM];
uniform float lightsInt[LIGHTS_NUM];
uniform vec3 dirLightDir;
uniform vec3 dirLightCol;
uniform mat4 u_projectionViewMatrix;
uniform mat4 u_modelMatrix;
uniform vec3 ambient;

varying vec2 v_texCoords;
varying vec4 v_diffuse;

const float WRAP_AROUND = 1.0; //0 is hard 1 is soft. if this is uniform performance is bad		
void main()
{	
	v_texCoords = a_texCoord0; 		
	vec4 worldPos = u_modelMatrix * a_position;
	gl_Position = u_projectionViewMatrix * worldPos; 
	vec3 pos  = worldPos.xyz;	
	
	vec3  aggCol = dirLightCol;
	
	vec3 normal = u_normalMatrix * a_normal; 
	#ifdef normals
	aggCol *= clamp((dot(normal, -dirLightDir) + WRAP_AROUND) / (1.0 + WRAP_AROUND),0.0, 1.0 );
	#endif
		
	for ( int i = 0; i < LIGHTS_NUM; i++ ){	
		vec3 dif  = lightsPos[i] - pos;
		//fastest way to calculate inverse of length				
		float invLen = inversesqrt(dot(dif, dif));				
		vec3 L = dif * invLen;// normalize		
		float weight   = lightsInt[i] * invLen;
		
		#ifdef normals
		float lambert = clamp((dot(normal, L) + WRAP_AROUND) / (1.0 + WRAP_AROUND),0.0, 1.0 );
		weight *= lambert;		
		#endif
		aggCol   += lightsCol[i] * weight;
		
	}
#ifdef diffuseColor
	aggCol *= diffuseCol;
#endif

#ifdef emissiveColor
	aggCol += emissiveCol;
#endif
	
	aggCol += ambient;
	
	v_diffuse = vec4(clamp( aggCol, 0.0,1.0), 1.0);	
}