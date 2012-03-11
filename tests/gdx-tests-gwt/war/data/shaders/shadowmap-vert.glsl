#ifdef GL_ES
precision highp float; 
#endif

attribute vec3 a_position;

uniform vec4 u_color;
uniform mat4 u_projTrans;
uniform mat4 u_lightProjTrans;

varying vec4 v_color;
varying vec4 v_lightSpacePosition;


void main(void) 
{
	v_color = u_color;
	gl_Position = u_projTrans * vec4(a_position,1.0) ;
	v_lightSpacePosition  = u_lightProjTrans * vec4(a_position,1.0) ;
}