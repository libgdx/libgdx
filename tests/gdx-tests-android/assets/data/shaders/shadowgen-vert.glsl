#ifdef GL_ES
precision highp float; 
#endif

attribute vec3 a_Position; 
varying vec4 v_position;

uniform mat4 u_projTrans;

void main(void) 
{   
   gl_Position =  u_projTrans * vec4(a_Position,1.0) ;
   v_position = gl_Position;   
}