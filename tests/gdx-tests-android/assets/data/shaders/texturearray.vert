in vec4 a_position;
in vec3 a_texCoord0;

uniform mat4 u_projViewTrans;
uniform mat4 u_modelView;

out vec4 v_color;
out vec3 v_texCoords;

void main() {
	v_texCoords = a_texCoord0;
	gl_Position =  u_projViewTrans * u_modelView * a_position;
}
