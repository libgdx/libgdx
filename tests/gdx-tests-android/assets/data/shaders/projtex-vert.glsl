attribute vec4 a_Position;
attribute vec3 a_Normal;

uniform mat4 u_camera;
uniform mat4 u_projector;
uniform mat4 u_model;
uniform mat4 u_modelNormal;
uniform vec3 u_projectorPos;
uniform vec3 u_color;

varying vec3 v_color;
varying vec4 v_texcoords;

void main() {	
	vec4 worldPos = u_model * a_Position;
	vec3 worldNormal = (u_modelNormal * vec4(a_Normal, 1)).xyz;	
	vec3 lightDir = u_projectorPos - worldPos.xyz;
	normalize(lightDir);	
	normalize(worldNormal);
		
	v_color = clamp(u_color * max(0, dot(lightDir, worldNormal)), 0, 1);
	v_texcoords = u_projector * worldPos;	
	gl_Position = u_camera * worldPos;
}