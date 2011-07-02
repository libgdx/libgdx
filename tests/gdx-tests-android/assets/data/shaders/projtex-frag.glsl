#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_sampler;

varying vec3 v_color;
varying vec4 v_texcoords;

void main() {	
	vec2 texcoords = (v_texcoords.xy / v_texcoords.w + 1.0) * 0.5;
	if(texcoords.x < 0.0 || texcoords.x > 1.0 || texcoords.y < 0.0 || texcoords.y > 1.0)	
		gl_FragColor = vec4(v_color, 1.0);
	else
		gl_FragColor = texture2D(u_sampler, texcoords.st) * vec4(v_color, 1.0);
}