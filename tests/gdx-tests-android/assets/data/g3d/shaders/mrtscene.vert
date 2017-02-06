in vec4 a_position;
in vec4 a_color;
in vec2 a_texCoord0;

out vec4 v_color;
out vec2 v_texCoords;

void main () {
    v_color = a_color;
    v_color.a = v_color.a * (255.0/254.0);
    v_texCoords = a_texCoord0;

    gl_Position = a_position;
}
