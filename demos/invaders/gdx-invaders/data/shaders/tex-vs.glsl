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
attribute MEDP vec2 a_texCoord0;

uniform mat4 u_projView;

varying MEDP vec2 texCoords;

void main() {
	texCoords = a_texCoord0;
	gl_Position = u_projView * a_position;
}