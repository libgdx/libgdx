#ifdef GL_ES
precision mediump float;
#endif

uniform float u_far;
varying vec4 v_color;

void main() {	
 	float z = 1.0 - (gl_FragCoord.z / gl_FragCoord.w) / u_far;
	gl_FragColor = vec4(z, z, z, 1.0);	
}