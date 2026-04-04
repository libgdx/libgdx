precision mediump float;

in vec2 v_texCoord0;

uniform sampler2D u_texture;

out vec4 FragColor;


void main () {
    FragColor = texture(u_texture, v_texCoord0);
}
