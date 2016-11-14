#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D s_texture;
varying vec2 v_texCoord;

void main() {			
	gl_FragColor = texture2D(s_texture, v_texCoord);
} 