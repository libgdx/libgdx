in vec4 a_position;
in vec2 a_texCoord0;

in vec2 i_offset;

uniform mat4 u_projTrans;

out vec2 v_texCoord0;

void main () {
    v_texCoord0 = a_texCoord0;

    vec4 pos = a_position;
    pos.xy += i_offset;

    gl_Position = u_projTrans * pos;
}
