attribute vec4 a_Position;
attribute vec4 a_Normal;
attribute vec2 a_TexCoord;

uniform mat4 u_projView;

varying vec2 v_texCoords;
varying vec4 v_color;

void main() {
	v_color = vec4(1, 0, 0, 1);
	v_texCoords = a_TexCoord;
	gl_Position = u_projView * a_Position;
}