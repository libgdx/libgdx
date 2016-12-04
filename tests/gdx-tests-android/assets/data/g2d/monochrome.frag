#ifdef GL_ES
#define LOWP lowp
	precision mediump float;
#else
#define LOWP 
#endif

varying LOWP vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
     
void main()
{
	vec4 texture = texture2D(u_texture, v_texCoords);
	float lum = dot(vec3(0.3, 0.59, 0.11), texture.rgb);
	gl_FragColor = v_color * vec4(vec3(lum), texture.a);
}