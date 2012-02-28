#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP  
#endif

uniform sampler2D u_texture0;

varying vec2 v_texCoords;
varying LOWP vec4 v_diffuse;
void main()
{		
	LOWP vec4 tex = texture2D(u_texture0, v_texCoords);
	gl_FragColor = v_diffuse *  tex;
}