attribute vec4 a_position;
attribute vec2 a_texCoord;

uniform mat4 u_projTrans;

varying vec2 v_texCoord;
			
void main() {		
	v_texCoord = a_texCoord;
	gl_Position =  u_projTrans * a_position;
} 