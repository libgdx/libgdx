attribute vec4 a_position;

uniform mat4 u_projTrans;
uniform vec4 u_color;

varying vec4 v_color;
			
void main() {	
	v_color = u_color;
	gl_Position =  u_projTrans * a_position;
} 