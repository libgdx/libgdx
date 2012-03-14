#define normals
#define LIGHTS_NUM 4
attribute vec4 a_position; 
attribute vec2 a_texCoord0;

#ifdef normals
attribute vec3 a_normal;
uniform mat3 u_normalMatrix;
#endif

uniform vec3  lightsPos[LIGHTS_NUM];
uniform vec3  lightsCol[LIGHTS_NUM];
uniform float lightsInt[LIGHTS_NUM];
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

				
const float WRAP_AROUND = 1.0; //0 is hard 1 is soft. if this is uniform performance is bad		
void main()
{
#ifdef normals
	v_normal    = u_normalMatrix * a_normal;
#endif

	v_texCoords = a_texCoord0;
	
	vec4 worldPos = u_modelMatrix * a_position;
	gl_Position = u_projectionViewMatrix * worldPos; 
	vec3 pos  = worldPos.xyz;
	v_pos = pos;
	v_eye = camPos - pos;
	
#if LIGHTS_NUM > 1
	//this is good place to calculate dir light?
#ifdef normals
	float aggWeight =  clamp((dot(a_normal, -dirLightDir) + WRAP_AROUND) / (1.0 + WRAP_AROUND),0.0, 1.0 );
	vec3  aggDir = -dirLightDir * aggWeight;
#else
	float aggWeight = 1.0;
	vec3  aggDir = dirLightDir;
#endif
	vec3  aggCol = dirLightCol * aggWeight;	
	for ( int i = 0; i < LIGHTS_NUM; i++ ){
	
		vec3 dif  = lightsPos[i] - pos;
		//fastest way to calculate inverse of length				
		float invLen = inversesqrt(dot(dif, dif));
				
		vec3 L = dif * invLen;// normalize		
		
		#ifdef normals
		float lambert = clamp((dot(a_normal, L) + WRAP_AROUND) / (1.0 + WRAP_AROUND),0.0, 1.0 );		
		float weight   = lightsInt[i] * invLen * lambert;
		#else
		float weight   = lightsInt[i] * invLen;
		#endif
		
		aggDir   += L * weight;
		invLen 	 *= weight;
		aggCol   += invLen * lightsCol[i];		
		aggWeight+= invLen;
		
	}	
	v_lightDir   = aggDir / aggWeight;
	v_lightColor = aggCol / aggWeight;//clamp((aggCol / aggW),0.0, 1.0 );
	v_intensity  = aggWeight;
#else
	v_lightDir   = lightsPos[0];
	v_lightColor = lightsCol[0];
	v_intensity  = lightsInt[0];
#endif
}