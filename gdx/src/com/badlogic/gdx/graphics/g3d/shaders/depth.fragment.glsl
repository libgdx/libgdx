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

#if defined(diffuseTextureFlag) && defined(blendedFlag)
#define blendedTextureFlag
varying MED vec2 v_texCoords0;
uniform sampler2D u_diffuseTexture;
uniform float u_alphaTest;
#endif

#ifdef PackedDepthFlag
varying HIGH float v_depth;
#endif //PackedDepthFlag

void main() {
	#ifdef blendedTextureFlag
		if (texture2D(u_diffuseTexture, v_texCoords0).a < u_alphaTest)
			discard;
	#endif // blendedTextureFlag
	
	#ifdef PackedDepthFlag
		HIGH float depth = v_depth;
		const HIGH vec4 bias = vec4(1.0 / 255.0, 1.0 / 255.0, 1.0 / 255.0, 0.0);
		HIGH vec4 color = vec4(depth, fract(depth * 255.0), fract(depth * 65025.0), fract(depth * 160581375.0));
		gl_FragColor = color - (color.yzww * bias);
	#endif //PackedDepthFlag
}
