#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif

varying LOWP vec4 v_color;
varying vec2 v_texCoords0;
varying vec2 v_texCoords1;
varying vec3 v_lightDir;
varying vec3 v_position;
varying float v_shininess;

uniform vec3 u_camPosition;
uniform sampler2D u_texture;

uniform vec3 u_ambient;
uniform float u_specularStrength;
uniform float u_attenuation;

void main()
{
	float lightDist2 = dot(v_lightDir, v_lightDir);
	normalize(v_lightDir);
	vec3 viewDir = normalize(u_camPosition - v_position);
	vec3 halfDir = normalize(v_lightDir + viewDir);
	LOWP vec4 texture = texture2D (u_texture, v_texCoords0);
	LOWP vec3 normal = texture2D (u_texture, v_texCoords1).xyz * 2.0 - 1.0;
	float att = 1.0 / (1.0 + u_attenuation * lightDist2);
	LOWP vec3 diffuse = vec3(min(1.0, (max(0.0, dot(normal, v_lightDir)) * att + u_ambient))) * texture.rgb;
	float specular = u_specularStrength * pow(max(0.0, dot(normal, halfDir)), v_shininess);
	gl_FragColor = v_color * vec4(diffuse + specular, texture.a);
}