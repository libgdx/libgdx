#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP  
#endif

uniform vec3 ambient;


uniform sampler2D u_texture0;
uniform sampler2D u_texture1;
//uniform sampler2D u_texture1;

varying vec2 v_texCoords;
varying vec3 v_normal;
varying vec3 v_eye;
varying vec3 v_pos;
varying float v_intensity;

varying vec3 v_lightPos;
varying vec3 v_lightColor;

const float shininessFactor = 10.0;
const float WRAP_AROUND = 0.75; //0 is hard 1 is soft. if this is uniform performance is bad
const float TRESHOLD = 0.2;//prevent color glitches

	
void main()
{	
	
	vec3 tex = texture2D(u_texture0, v_texCoords).rgb;
	vec3 texSpecular = texture2D(u_texture1, v_texCoords).rgb;		
	vec3 surfaceNormal = normalize( v_normal );
	
	//intensity	
  	float dist = length(v_lightPos);
  	vec3 lightDirection = v_lightPos / (dist);// > TRESHOLD ? dist : 1.0); //take comment off if glich is too visible
    
    float angle = dot(surfaceNormal, lightDirection);
    float diffuse = clamp((angle + WRAP_AROUND)/ (1.0 + WRAP_AROUND), 0.0, 1.0 );
   	//float diffuse = max(0.0,dot(surfaceNormal, lightDirection));
    
    
    float intensity = clamp( (v_intensity / dist), 0.0, 1.0 );	
	intensity = (diffuse > 0.0) ? intensity : 0.0;	
	
	//specular blinn
	vec3 fromEye   = normalize(v_eye);	
	vec3 halfAngle = normalize(lightDirection + fromEye);
	float specular = pow(clamp(dot(halfAngle, surfaceNormal), 0.0, 1.0), shininessFactor);
			
	//combine lights
	vec3 light = intensity *( v_lightColor * specular * texSpecular + diffuse * v_lightColor * tex );
	
	gl_FragColor = vec4( light + (ambient * tex), 1.0);
}