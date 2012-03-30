//#define lightmapTextureFlag
//#define diffuseTextureFlag
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
#endif

varying MED vec2 v_texCoords;
varying vec4 v_diffuse;
void main()
{		
	vec4 light = v_diffuse;
	
	#ifdef lightmapTextureFlag
	light *= texture2D(lightmapTexture, v_texCoords);
	#endif
	
	#ifdef diffuseTextureFlag
	light *= texture2D(diffuseTexture, v_texCoords);
	#endif
	
	gl_FragColor = light;
}