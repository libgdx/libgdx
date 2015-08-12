#ifdef GL_ES 
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision mediump float;
#else
#define MED
#define LOWP
#define HIGH
#endif

uniform mat4 u_projViewWorldTrans;
attribute vec3 a_position;
//varying HIGH float v_depth;

void main()
{
	vec4 pos = u_projViewWorldTrans * vec4(a_position, 1.0);
	//v_depth = pos.z/pos.w*0.5+0.5;
	gl_Position = pos;
}