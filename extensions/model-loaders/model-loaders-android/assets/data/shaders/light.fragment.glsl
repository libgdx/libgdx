//#define emissiveColorFlag
//#define specularColorFlag
//#define diffuseColorFlag
//#define rimColorFlag
//#define lightmapTextureFlag
//#define diffuseTextureFlag
//#define specularTextureFlag
//#define translucentFlag
//#define fogColorFlag
#define normalsFlag

#ifdef GL_ES
#define LOWP lowp
#define MED mediump
precision lowp float;
#else
#define MED
#define LOWP
#endif


#ifdef rimColorFlag
uniform vec4 rimColor;
#endif


#ifdef diffuseColorFlag
uniform vec4 diffuseColor;
#endif

#ifdef emissiveColorFlag
uniform vec4 emissiveColor;
#endif

#ifdef specularColorFlag
uniform vec4 specularColor;
#endif

uniform vec3 ambient;
const MED float shininessFactor = 25.0;

#ifdef diffuseTextureFlag
uniform sampler2D diffuseTexture;
#endif

#ifdef lightmapTextureFlag
uniform sampler2D lightmapTexture;
#endif

#ifdef specularTextureFlag
uniform sampler2D specularTexture;
#endif

#ifdef fogColorFlag
uniform vec4 fogColor;
varying float v_fog;
#endif

varying MED vec2 v_texCoords;
varying vec3 v_normal;
varying vec3 v_eye;
varying vec3 v_pos;
varying float v_intensity;

varying vec3 v_lightDir;
varying vec3 v_lightColor;

uniform vec3 camDir;

//wrap light. this is fastest light model
float wrapLight(vec3 nor, vec3 direction){
	return dot(nor, direction) * 0.5 + 0.5;
}	
void main()
{	
	
	float alpha = 1.0;
	
	#ifdef diffuseTextureFlag
		vec4 difTex = texture2D(diffuseTexture, v_texCoords);
		vec3 diffuse = difTex.rgb;	
		#ifdef translucentFlag
			alpha = difTex.a;
		#endif
	#else 
		vec3 diffuse = vec3(1.0);
	#endif
	
	
	#ifdef diffuseColorFlag
		diffuse *= diffuseColor.rgb;
		#ifdef translucentFlag
			alpha *= diffuseColor.a;
		#endif
	#endif 	
		
	//fastest way to calculate inverse of length
  	float invLength = (inversesqrt( dot(v_lightDir, v_lightDir)));
  	vec3 intensity =  v_lightColor * ( v_intensity * invLength);	
	
	#ifdef normalsFlag
		vec3 lightDirection = v_lightDir * invLength;	
	
		vec3 surfaceNormal = normalize( v_normal );
		//lambert phong
    	float lambert = wrapLight(surfaceNormal, lightDirection);
   		vec3 diffuseLight = intensity * lambert;
   		
		//specular blinn
		vec3 fromEye   = normalize(v_eye);	
		vec3 halfAngle = normalize(lightDirection + fromEye);
		vec3 specularLight = diffuseLight * pow( clamp( dot(halfAngle, surfaceNormal), 0.0, 1.0), shininessFactor);
		
		#ifdef specularColorFlag
			specularLight *= specularColor.rgb;
			#ifdef translucentFlag
				alpha += specular * specularColor.a; 
			#endif
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
		light += emissiveColor.rgb;
	#endif
	
	light += ambient * diffuse;
	
	#ifdef normalsFlag
		#ifdef rimColorFlag
		light +=  pow( 1.0 - dot( surfaceNormal, -camDir ), 3.0 ) * rimColor.rgb;
		#endif
	#endif
	
	#ifdef fogColorFlag
	light = mix(light, fogColor.rgb, v_fog);		
		#ifdef translucentFlag
		alpha += v_fog;
		#endif
	#endif
	
	gl_FragColor.rgb = light;
	
	#ifdef translucentFlag
	gl_FragColor.a = alpha;
	#endif
	
}