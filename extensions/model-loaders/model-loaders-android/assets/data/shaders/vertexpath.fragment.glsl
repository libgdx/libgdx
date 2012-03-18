//#define lightmapTextureFlag
//#define diffuseTextureFlag
#ifdef GL_ES
#define LOWP lowp
#define MED mediump
precision mediump float;
#else
#define MED
#define LOWP
#endif

#ifdef diffuseTextureFlag
uniform LOWP sampler2D diffuseTexture;
#endif

#ifdef lightmapTextureFlag
uniform LOWP sampler2D lightmapTexture;
#endif

varying vec2 v_texCoords;
varying LOWP vec4 v_diffuse;
void main()
{		
	LOWP vec4 light = v_diffuse;
	
	#ifdef lightmapTextureFlag
	light *= texture2D(lightmapTexture, v_texCoords);
	#endif
	
	#ifdef diffuseTextureFlag
	light *= texture2D(diffuseTexture, v_texCoords);
	#endif
	
	gl_FragColor = light;
}