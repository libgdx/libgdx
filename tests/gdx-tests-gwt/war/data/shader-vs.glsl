attribute vec4 a_position;
attribute vec2 a_texCoord0;

uniform mat4 u_projView; 

varying vec4 v_color;
varying vec2 v_texCoord; 

void main() {
	v_color = vec4(1, 1, 1, 1); 
	v_texCoord = a_texCoord0; 
	gl_Position = u_projView * a_position; 
}