#ifdef GL_ES
precision mediump float;
#endif
in vec4 v_col;
layout(location = 0) out vec4 colorOut;
layout(location = 1) out vec4 redOut;

void main() {
	colorOut = v_col;
	redOut = vec4(1.0, 0.0, 0.0, 1.0);
}
