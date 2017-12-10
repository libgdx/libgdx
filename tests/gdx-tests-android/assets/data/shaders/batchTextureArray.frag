#ifdef GL_ES
#define LOWP lowp
precision mediump float;
precision mediump sampler2DArray;
#else
#define LOWP 
#endif

LOWP in vec4 v_color;
in vec3 v_texCoords;
uniform sampler2DArray u_texture;
out vec4 fragColor;

void main()
{
  fragColor = v_color * texture(u_texture, v_texCoords);
}
