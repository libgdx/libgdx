#define LIGHTS_NUM 8
attribute vec4 a_position; 
attribute vec2 a_texCoord0;
attribute vec3 a_normal;

uniform vec3  lightsPos[LIGHTS_NUM];
uniform vec3  lightsCol[LIGHTS_NUM];
uniform float lightsInt[LIGHTS_NUM];
uniform mat4 u_projectionViewMatrix;
uniform mat4 u_modelMatrix;
uniform vec3 ambient;

varying vec2 v_texCoords;
varying vec4 v_diffuse;

const float WRAP_AROUND = 0.75; //0 is hard 1 is soft. if this is uniform performance is bad		
void main()
{	
	v_texCoords = a_texCoord0; 		
	vec4 worldPos = u_modelMatrix * a_position;
	gl_Position = u_projectionViewMatrix * worldPos; 
	vec3 pos  = worldPos.xyz;	
	
	vec3  aggCol = vec3(0.0);	
	for ( int i = 0; i < LIGHTS_NUM; i++ ){	
		vec3 dif  = lightsPos[i] - pos;
		//fastest way to calculate inverse of length				
		float invLen = inversesqrt(dot(dif, dif));				
		vec3 L = dif * invLen;// normalize		
		
		float lambert = clamp((dot(a_normal, L) + WRAP_AROUND) / (1.0 + WRAP_AROUND),0.0, 1.0 );
		float weight   = lightsInt[i] * invLen * lambert;		
		aggCol   += lightsCol[i] * weight;
		
	}
	v_diffuse = vec4(clamp( aggCol + ambient, 0.0,1.0), 1.0);	
}