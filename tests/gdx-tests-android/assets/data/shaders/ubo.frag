
in vec4 v_color;
in vec2 v_texCoords;

uniform sampler2D u_texture;
uniform u_bufferBlock {
    vec3 colorBuffer;
    vec2 positionBuffer;
};

void main () {
    fragColor = v_color * texture(u_texture, v_texCoords) * vec4(colorBuffer, 1.0);
}