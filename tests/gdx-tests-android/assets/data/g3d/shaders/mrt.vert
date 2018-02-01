in vec3 a_position;
in vec3 a_normal;

#ifdef texturedFlag
in vec2 a_texCoord0;
in vec3 a_tangent;
in vec3 a_binormal;
#else
in vec4 a_color;
#endif

uniform mat4 u_worldTrans;
uniform mat4 u_projViewTrans;
uniform mat3 u_normalMatrix;

out vec3 v_normal;
out vec3 v_position;

#ifdef texturedFlag
out vec3 v_tangent;
out vec3 v_binormal;
out vec2 v_texCoords;
#else
out vec4 v_color;
#endif

void main() {

    v_normal = normalize(u_normalMatrix * a_normal);

    #ifdef texturedFlag
        v_tangent = normalize(u_normalMatrix * a_tangent);
        v_binormal = normalize(u_normalMatrix * a_binormal);
        v_texCoords = a_texCoord0;
    #else
        v_color = a_color;
    #endif

    vec4 position = u_worldTrans * vec4(a_position, 1.0);
    v_position = position.xyz;

    vec4 pos = u_projViewTrans * position;
    gl_Position = pos;
}
