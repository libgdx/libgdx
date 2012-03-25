//#define emissiveColorFlag
//#define specularColorFlag
//#define diffuseColorFlag
//#define lightmapTextureFlag
//#define diffuseTextureFlag
//#define specularTextureFlag
#define normalsFlag

#ifdef GL_ES
#define LOWP lowp
#define MED mediump
precision mediump float;
#else
#define MED
#define LOWP
#endif

#ifdef diffuseColorFlag
uniform vec3 diffuseColor;
#endif

#ifdef emissiveColorFlag
uniform vec3 emissiveColor;
#endif

#ifdef specularColorFlag
uniform vec3 specularColor;
#endif

uniform vec3 ambient;
const float shininessFactor = 10.0;

#ifdef diffuseTextureFlag
uniform sampler2D diffuseTexture;
#endif

#ifdef lightmapTextureFlag
uniform sampler2D lightmapTexture;
#endif

#ifdef specularTextureFlag
uniform sampler2D specularTexture;
#endif

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

	vec3 diffuse = vec3(0.0);
	
	#ifdef diffuseTextureFlag
	diffuse = texture2D(diffuseTexture, v_texCoords).rgb;
	#endif
	
	#ifdef diffuseColorFlag
		diffuse *= diffuseColor;
	#endif 	
		
	//fastest way to calculate inverse of length
  	float invLength = clamp(inversesqrt( dot(v_lightDir, v_lightDir)),0.0, 1.0 );
  	vec3 intensity =  v_lightColor * ( v_intensity * invLength);	
	
	#ifdef normalsFlag
	vec3 lightDirection = v_lightDir * invLength;	
	
	vec3 surfaceNormal = normalize( v_normal );
	//lambert phong
    float lambert = wrapLight(surfaceNormal, lightDirection);
   	
	//specular blinn
	vec3 fromEye   = normalize(v_eye);	
	vec3 halfAngle = normalize(lightDirection + fromEye);
	float specular = pow( clamp( dot(halfAngle, surfaceNormal), 0.0, 1.0), shininessFactor);
	
	//specular = (lambert > 0.0) ? specular : 0.0;
	float tmp  = specular * (lambert * 2.0);
	specular = (lambert > 0.5)  ? specular : tmp;
	
	vec3 diffuseLight = intensity * lambert;
	
	vec3 specularLight = intensity * specular;
	#ifdef specularColorFlag
		specularLight *= specularColor;
	#endif
	
	#ifdef specularTextureFlag
		specularLight *= texture2D(specularTexture, v_texCoords).rgb;
	#endif

	//combine lights
	vec3 light = specularLight + diffuseLight * diffuse;
	#else
	vec3 light = intensity * diffuse ;
	#endif
	
	#ifdef lightmapTextureFlag
	light *= texture2D(lightmapTexture, v_texCoords).rgb;
	#endif
	
	#ifdef emissiveColorFlag
		light += emissiveColor;
	#endif
	
	light += ambient * diffuse;
	
	gl_FragColor = vec4(light, 1.0);
	
}