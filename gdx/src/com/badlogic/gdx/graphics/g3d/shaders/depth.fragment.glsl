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

#ifdef PackedDepthFlag
varying HIGH float v_depth;
#endif //PackedDepthFlag

void main() {
	#ifdef PackedDepthFlag
		HIGH float depth = v_depth;
		const HIGH vec4 bias = vec4(1.0 / 255.0, 1.0 / 255.0, 1.0 / 255.0, 0.0);
		HIGH vec4 color = vec4(depth, fract(depth * 255.0), fract(depth * 65025.0), fract(depth * 160581375.0));
		gl_FragColor = color - (color.yzww * bias);
	#endif //PackedDepthFlag
}
