#version 300 es

in vec4 a_position;
in vec2 i_offset;
in vec4 i_color;

out vec4 v_color;

void main () {
    v_color = i_color;
    gl_Position = a_position + vec4(i_offset, 0.0, 0.0);
}
