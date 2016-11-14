#ifdef GL_ES
precision lowp float;
#endif

uniform sampler2D u_sampler;
uniform vec3 u_projectorPos;

varying vec3 v_color;
varying vec4 v_texcoords;
varying vec3 v_normal;
varying vec3 v_position;

void main() {	
	// phong
	vec3 lightDir = normalize(u_projectorPos - v_position);	
	vec3 normal = normalize(v_normal);
	float dotProduct = dot(normal, lightDir);
	
	dotProduct = max(dotProduct, 0.0);

	vec3 texcoords = (v_texcoords.xyz / v_texcoords.w + 1.0) * 0.5;
	vec4 color;
	float factor = ceil(sign(texcoords.z));
	color = texture2D(u_sampler, texcoords.st) * factor * vec4(v_color, 1.0);
	gl_FragColor = color * dotProduct;
}