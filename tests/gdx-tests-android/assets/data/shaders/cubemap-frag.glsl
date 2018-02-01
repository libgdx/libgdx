#ifdef GL_ES 
precision mediump float;
#endif
 
uniform samplerCube u_environmentCubemap;
 
varying vec4 v_position;
 
void main() {
	vec3 center = vec3(0.0);
	vec3 dir = vec3(v_position) - center;
	gl_FragColor = textureCube(u_environmentCubemap, dir);
}