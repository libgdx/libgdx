#define LIGHTS_NUM 2
attribute vec4 a_position; 
attribute vec2 a_texCoord0;
attribute vec3 a_normal;

uniform vec3  lightsPos[LIGHTS_NUM];
uniform vec3  lightsCol[LIGHTS_NUM];
uniform float lightsInt[LIGHTS_NUM];
uniform vec3 camPos;
uniform mat4 u_projectionViewMatrix;
uniform mat4 u_modelMatrix;

varying vec2 v_texCoords;
varying vec3 v_normal;
varying vec3 v_lightPos;
varying vec3 v_eye;
varying vec3 v_pos;
varying vec3 v_lightColor;
varying float v_intensity;
				
const float WRAP_AROUND = 0.5; //0 is hard 1 is soft. if this is uniform performance is bad		
void main()
{	
	v_texCoords = a_texCoord0; 	
	v_normal    = a_normal;	
	vec4 worldPos = u_modelMatrix * a_position;
	gl_Position = u_projectionViewMatrix * worldPos; 
	vec3 pos  = worldPos.xyz;
	v_pos = pos;
	v_eye = camPos - pos;
	
#if LIGHTS_NUM > 1
	float aggW = 0.0;
	vec3 aggPos = vec3(0.0);
	vec3 aggCol = vec3(0.0);	
	for ( int i = 0; i < LIGHTS_NUM; i++ ){
	
		vec3 dif  = lightsPos[i] - pos;		
		float len = 1.0 / length(dif);
		float W   = lightsInt[i] * len;//or some better fallof
		vec3 L    = dif * len; //normalize
		
		float lambert = dot(a_normal, L);
		W *= clamp(0.0, 1.0, (lambert + WRAP_AROUND) / (1.0 + WRAP_AROUND) );
		//W *= max(0.0, dot(a_normal, L));
				
		aggPos   += L * W;
		len 	 *= W;
		aggCol   += len * lightsCol[i];		
		aggW     += len;
		
	}	
	v_lightPos  = (aggPos / aggW);
	v_lightColor = max(vec3(0.0), (aggCol / aggW));
	v_intensity = aggW;
#else
	v_lightPos  = lightsPos[0];
	v_lightColor = lightsCol[0];
	v_intensity = lightsInt[0];
#endif

}