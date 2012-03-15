#ifdef GL_ES
#define LOWP lowp
#define MEDP mediump
#define HIGP highp
#else
#define LOWP
#define MEDP
#define HIGP
#endif

attribute vec4 a_position;

uniform mat4 u_projView;

void main() {
	gl_Position = u_projView * a_position;
}