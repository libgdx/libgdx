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


#ifdef billboard
//Billboard particles
varying vec4 v_color;
varying MED vec2 v_texCoords0;

uniform sampler2D u_diffuseTexture;
uniform float u_opacity;

void main() {

	vec4 diffuse = texture2D(u_diffuseTexture, v_texCoords0) * v_color;
	gl_FragColor.rgb = diffuse.rgb;
	gl_FragColor.a = diffuse.a * u_opacity;
}
#else
//Point particles
varying vec4 v_color;
varying mat2 v_rotation;
varying MED vec2 v_texCoords0;
varying vec2 v_uvRegionCenter;

uniform float u_opacity;
uniform sampler2D u_diffuseTexture;
uniform vec2 u_regionSize;

void main() {
	
	vec2 uv = v_texCoords0.xy + gl_PointCoord*u_regionSize - v_uvRegionCenter;
	vec2 texCoord = v_rotation * uv  +v_uvRegionCenter;
	vec4 diffuse = texture2D(u_diffuseTexture, texCoord) * v_color;
	gl_FragColor.rgb = diffuse.rgb;
	gl_FragColor.a = diffuse.a * u_opacity;
}

#endif