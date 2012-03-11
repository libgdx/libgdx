attribute vec3 a_position;
 
varying vec4 v_position;

uniform mat4 u_projTrans;

void main(void) 
{   
   gl_Position =  u_projTrans * vec4(a_position,1.0) ;
   v_position = gl_Position;   
}