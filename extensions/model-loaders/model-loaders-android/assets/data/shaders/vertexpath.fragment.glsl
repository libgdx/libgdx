//#define lightmapTextureFlag
//#define diffuseTextureFlag
//#define translucentFlag
//#define fogColorFlag

#ifdef GL_ES
#define LOWP lowp
#define MED mediump
precision lowp float;
#else
#define MED
#define LOWP
#endif

#ifdef diffuseTextureFlag
uniform sampler2D diffuseTexture;
#endif

#ifdef lightmapTextureFlag
uniform sampler2D lightmapTexture;
varying vec2 v_texCoords1;
#endif

#ifdef fogColorFlag
uniform vec4 fogColor;
varying float v_fog;
#endif

varying MED vec2 v_texCoords;
varying vec4 v_diffuse;
void main()
{		
	vec4 light = v_diffuse;
	
	#ifdef lightmapTextureFlag
	light *= texture2D(lightmapTexture, v_texCoords1);
	#endif
	
	#ifdef diffuseTextureFlag
	light *= texture2D(diffuseTexture, v_texCoords);
	#endif
	
	#ifdef fogColorFlag
	light.rgb = mix(light.rgb, fogColor.rgb, v_fog);		
		#ifdef translucentFlag
			light.a += v_fog;
		#endif
	#endif
	

	gl_FragColor.rgb = light.rgb;
	#ifdef translucentFlag
	gl_FragColor.a = light.a;
	#endif
}