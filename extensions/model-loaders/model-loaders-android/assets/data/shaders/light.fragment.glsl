//#define emissiveColor
//#define specularColor
//#define diffuseColor
//#define bakedLight
#define normals
#ifdef GL_ES
#define LOWP lowp
#define MED mediump
precision mediump float;
#else
#define MED
#define LOWP
#endif

#ifdef diffuseColor
uniform vec3 diffuseCol;
#endif

#ifdef emissiveColor
uniform vec3 emissiveCol;
#endif

#ifdef specularColor
uniform vec3 specularCol;
#endif

uniform vec3 ambient;
const float shininessFactor = 10.0;

uniform sampler2D u_texture0;

varying vec2 v_texCoords;
varying vec3 v_normal;
varying vec3 v_eye;
varying vec3 v_pos;
varying float v_intensity;

varying vec3 v_lightDir;
varying vec3 v_lightColor;

//wrap light. this is fastest light model
float wrapLight(vec3 nor, vec3 direction){
	return dot(nor, direction) * 0.5 + 0.5;
}	
void main()
{		
	vec3 tex = texture2D(u_texture0, v_texCoords).rgb;
		
	//fastest way to calculate inverse of length
  	float invLength = clamp(inversesqrt( dot(v_lightDir, v_lightDir)),0.0, 1.0 );
  	vec3 intensity =  v_lightColor * ( v_intensity);	
	
	#ifdef normals
	vec3 lightDirection = v_lightDir * invLength;	
	
	vec3 surfaceNormal = normalize( v_normal );
	//lambert phong
    float diffuse = wrapLight(surfaceNormal, lightDirection);
   	
	//specular blinn
	vec3 fromEye   = normalize(v_eye);	
	vec3 halfAngle = normalize(lightDirection + fromEye);
	float specular = pow( clamp( dot(halfAngle, surfaceNormal), 0.0, 1.0), shininessFactor);
	//specular = (diffuse > 0.0) ? specular : 0.0;
	float tmp  = specular * (diffuse * 2.0);
	specular = (diffuse > 0.5)  ? specular : tmp;
	
	vec3 diffuseLight = intensity * diffuse * tex;
	#ifdef diffuseColor
		diffuseLight *= diffuseCol;
	#endif 
	
	vec3 specularLight = intensity * specular;
	#ifdef specularColor
		specularLight *= specularCol;
	#endif
	//combine lights
	vec3 light =  specularLight + diffuseLight;
	#else
	vec3 light =  intensity * tex;
	#endif
	
	#ifdef bakedLight
	light *= texture2D(u_texture1, v_texCoords).rgb;	
	#endif
	
	#ifdef emissiveColor
		light += emissiveCol;
	#endif
	
	vec3 ambientLight = (ambient * tex);
	
	gl_FragColor = vec4( ambientLight + light, 1.0);
	//gl_FragColor = texture2D(u_texture1, v_texCoords) * vec4( light + (ambient * tex) , 1.0);
}