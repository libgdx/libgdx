#ifdef GL_ES
precision mediump float;
#endif

uniform float u_filter[9];
uniform float2 u_offsets[9];

uniform sampler2D u_texture;

varying vec4 v_color;
varying vec2 v_texCoords;

void main() {			
	int i = 0;
	vec4 sum = vec4(0.0);   
    for(; i < 9; i++) {
		vec4 tmp = texture2D(u_texture, v_texCoords.st + u_offsets[i]);
		sum += tmp * u_filter[i];
	}	
	gl_FragColor = sum;
}