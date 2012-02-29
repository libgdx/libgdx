#ifdef GL_ES
#define LOWP lowp
#define MED mediump
precision mediump float;
#else
#define MED
#define LOWP
#endif

uniform vec3 ambient;
const float shininessFactor = 15.0;
const float WRAP_AROUND = 1.0; //0 is hard 1 is soft. if this is uniform performance is bad

uniform sampler2D u_texture0;

varying vec2 v_texCoords;
varying vec3 v_normal;
varying vec3 v_eye;
varying vec3 v_pos;
varying float v_intensity;

varying vec3 v_lightDir;
varying vec3 v_lightColor;

const float TRESHOLD = 0.02;//prevent color glitches

	
void main()
{		
	vec3 tex = texture2D(u_texture0, v_texCoords).rgb;
		
	vec3 surfaceNormal = normalize( v_normal );
	
	//fastest way to calculate inverse of length
  	float invLength = inversesqrt( dot(v_lightDir, v_lightDir));
  	vec3 intensity = clamp( (v_lightColor * (v_intensity * invLength)), 0.0, 1.0 );	
	vec3 lightDirection = v_lightDir * invLength;// > TRESHOLD ? invLength : 1.0);    
	
	//lambert phong
    float angle = dot(surfaceNormal, lightDirection);
    float diffuse = clamp((angle + WRAP_AROUND)/ (1.0+WRAP_AROUND), 0.0, 1.0);
   	
	//specular blinn
	vec3 fromEye   = normalize(v_eye);	
	vec3 halfAngle = normalize(lightDirection + fromEye);
	float specular = pow( clamp( dot(halfAngle, surfaceNormal), 0.0, 1.0), shininessFactor);
	specular = (diffuse > 0.0) ? specular : 0.0;
		
	//combine lights
	vec3 light =  intensity *( specular + diffuse *  tex );
	
	gl_FragColor = vec4( light + (ambient * tex), 1.0);
	//gl_FragColor = texture2D(u_texture1, v_texCoords) * vec4( light + (ambient * tex) , 1.0);
}