#ifdef GL_ES
precision highp float;
#endif

uniform sampler2D u_sampler;

varying vec3 v_color;
varying vec4 v_texcoords;

void main() {	
	v_texcoords = (v_texcoords / v_texcoords.w + 1.0) * 0.5;
	if(v_texcoords.x < 0 || v_texcoords.x > 1 || v_texcoords.y < 0 || v_texcoords.y > 1)	
		gl_FragColor = vec4(v_color, 1);
	else
		gl_FragColor = texture2D(u_sampler, v_texcoords.st) * vec4(v_color, 1);
}