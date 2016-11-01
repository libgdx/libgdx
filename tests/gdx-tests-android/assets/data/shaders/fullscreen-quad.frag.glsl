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

varying MED vec2 v_UV;
uniform sampler2D u_texture;

void main() {
    vec4 sum = texture2D(u_texture, v_UV);
    gl_FragColor = vec4(sum.rgb, 1.0);
}

