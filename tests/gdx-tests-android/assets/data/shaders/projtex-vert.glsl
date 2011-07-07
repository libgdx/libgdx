attribute vec4 a_position;
attribute vec3 a_normal;

uniform mat4 u_camera;
uniform mat4 u_projector;
uniform mat4 u_model;
uniform mat4 u_modelNormal;
uniform vec3 u_projectorPos;
uniform vec3 u_color;

varying vec3 v_color;
varying vec4 v_texcoords;
varying vec3 v_normal;
varying vec3 v_position;

void main() {	
	vec4 worldPos = u_model * a_position;
	vec3 worldNormal = (u_modelNormal * vec4(a_normal, 1)).xyz;	
	
	v_position = worldPos.xyz;
	v_normal = worldNormal.xyz;		
	v_color = u_color;
	v_texcoords = u_projector * worldPos;
	gl_Position = u_camera * worldPos;
}