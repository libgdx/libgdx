/* SpriteBatch shader that uses three extra vertex attributes to support normal-mapped lighting. A single point
light is supported. The secondary (normal map) texture region is assumed to be part of the same texture as the 
primary texture region. See SpriteExtraAttributesTest.java for usage.
*/

attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;
attribute vec2 a_texCoord1;
attribute float a_rotation;
attribute float a_shininess;

uniform mat4 u_projTrans;
uniform vec3 u_lightPosition;

varying vec4 v_color;
varying vec2 v_texCoords0;
varying vec2 v_texCoords1;
varying vec3 v_lightDir;
varying vec3 v_position;
varying float v_shininess;
			
void main() {
	v_color = a_color;
	v_color.a = v_color.a * (255.0/254.0);
	v_texCoords0 = a_texCoord0;
	v_texCoords1 = a_texCoord1;
	v_position = a_position.xyz;
	gl_Position =  u_projTrans * a_position;
	vec2 tangent = vec2(cos(a_rotation), sin(a_rotation));
	vec2 binormal = vec2(-tangent.y, tangent.x);
	v_lightDir = u_lightPosition - v_position;
	v_lightDir = vec3(dot(v_lightDir.xy, tangent), dot(v_lightDir.xy, binormal), v_lightDir.z); // rotate to tangent space
	v_shininess = a_shininess;
}