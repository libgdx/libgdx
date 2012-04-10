#define normalsFlag
//#define LIGHTS_NUM 4
attribute vec4 a_position; 
attribute vec2 a_texCoord0;

#ifdef normalsFlag
attribute vec3 a_normal;
uniform mat3 u_normalMatrix;
#endif

#if LIGHTS_NUM > 0
uniform vec3  lightsPos[LIGHTS_NUM];
uniform vec3  lightsCol[LIGHTS_NUM];
uniform float lightsInt[LIGHTS_NUM];
#endif
uniform vec3 camPos;
uniform vec3 dirLightDir;
uniform vec3 dirLightCol;
uniform mat4 u_projectionViewMatrix;
uniform mat4 u_modelMatrix;

varying vec2 v_texCoords;
varying vec3 v_normal;
varying vec3 v_lightDir;
varying vec3 v_eye;
varying vec3 v_pos;
varying vec3 v_lightColor;
varying float v_intensity;
				
//wrap light. this is fastest light model
float wrapLight(vec3 nor, vec3 direction){
	return dot(nor, direction) * 0.5 + 0.5;
}
void main()
{
#ifdef normalsFlag
	v_normal    = u_normalMatrix * a_normal;
#endif

	v_texCoords = a_texCoord0;
	
	vec4 worldPos = u_modelMatrix * a_position;
	gl_Position = u_projectionViewMatrix * worldPos; 
	vec3 pos  = worldPos.xyz;
	v_pos = pos;
	v_eye = camPos - pos;
	

#if LIGHTS_NUM > 0

	#ifdef normals
	float aggWeight =  wrapLight(v_normal, -dirLightDir);
	vec3  aggDir = -dirLightDir * aggWeight;
	#else
	float aggWeight = 1.0;
	vec3  aggDir = -dirLightDir;
	#endif
	vec3  aggCol = dirLightCol * aggWeight;

		
	for ( int i = 0; i < LIGHTS_NUM; i++ ){
	
		vec3 dif  = lightsPos[i] - pos;
		//fastest way to calculate inverse of length				
		float invLen = inversesqrt(dot(dif, dif));
				
		vec3 L = dif * invLen;// normalize		
		
		#ifdef normalsFlag
		float lambert = wrapLight(v_normal, L);		
		float weight  = lightsInt[i] * invLen * lambert;
		#else
		float weight  = lightsInt[i] * invLen;
		#endif
		
		aggDir   += L * weight;		
		aggCol   += lightsCol[i] * weight;		
		aggWeight+= weight;
		
	}	
	v_lightDir   = aggDir / aggWeight;
	v_lightColor = clamp((aggCol / aggWeight),0.0, 1.0 );
	v_intensity  = aggWeight;
#else
	v_lightDir   = -dirLightDir;
	v_lightColor = dirLightCol;
	v_intensity  = 1.0;
#endif
}