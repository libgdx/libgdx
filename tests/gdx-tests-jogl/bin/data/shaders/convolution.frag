#ifdef GL_ES
precision lowp float;
#endif

uniform float u_filter[9];
uniform vec2 u_offsets[9];

uniform sampler2D u_texture;

varying vec4 v_color;
varying vec2 v_texCoords;

void main() {				
	vec4 sum = vec4(0.0);       
	vec4 tmp = texture2D(u_texture, v_texCoords.st + u_offsets[0]);
	sum += tmp * u_filter[0];
	tmp = texture2D(u_texture, v_texCoords.st + u_offsets[1]);
	sum += tmp * u_filter[1];
	tmp = texture2D(u_texture, v_texCoords.st + u_offsets[2]);
	sum += tmp * u_filter[2];
	tmp = texture2D(u_texture, v_texCoords.st + u_offsets[3]);
	sum += tmp * u_filter[3];
	tmp = texture2D(u_texture, v_texCoords.st + u_offsets[4]);
	sum += tmp * u_filter[4];
	tmp = texture2D(u_texture, v_texCoords.st + u_offsets[5]);
	sum += tmp * u_filter[5];
	tmp = texture2D(u_texture, v_texCoords.st + u_offsets[6]);
	sum += tmp * u_filter[6];
	tmp = texture2D(u_texture, v_texCoords.st + u_offsets[7]);
	sum += tmp * u_filter[7];
	tmp = texture2D(u_texture, v_texCoords.st + u_offsets[8]);
	sum += tmp * u_filter[8];
		
	gl_FragColor = sum;
}