#ifdef GL_ES
#define LOWP lowp
#define MEDP mediump
#define HIGP highp
precision lowp float;
#else
#define LOWP
#define MEDP
#define HIGP
#endif

uniform sampler2D u_diffuse;

varying LOWP float intensity;
varying MEDP vec2 texCoords;

void main() {
	gl_FragColor = intensity * texture2D(u_diffuse, texCoords);
}