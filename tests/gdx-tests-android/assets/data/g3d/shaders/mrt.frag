#ifdef GL_ES
precision mediump float;
#endif

#ifdef texturedFlag
uniform sampler2D u_diffuseTexture;
uniform sampler2D u_specularTexture;
uniform sampler2D u_normalTexture;
#endif

in vec3 v_normal;
in vec3 v_position;

#ifdef texturedFlag
in vec3 v_tangent;
in vec3 v_binormal;
in vec2 v_texCoords;
#else
in vec4 v_color;
#endif

layout(location = 0) out vec4 diffuseOut;
layout(location = 1) out vec3 normalOut;
layout(location = 2) out vec3 positionOut;

void main() {
    #ifdef texturedFlag
        vec4 diffuse = texture(u_diffuseTexture, v_texCoords);
        vec4 specular = texture(u_specularTexture, v_texCoords);
        vec3 normal = normalize(2.0 * texture(u_normalTexture, v_texCoords).xyz - 1.0);
    #else
        vec4 diffuse = v_color;
        vec4 specular = vec4(1.0);
    #endif

    diffuseOut.rgb = diffuse.rgb;
    diffuseOut.a = specular.r;

    #ifdef texturedFlag
        vec3 finnormal = normalize((v_tangent * normal.x) + (v_binormal * normal.y) + (v_normal * normal.z));
        normalOut = (finnormal + 1.0) * 0.5;
    #else
        normalOut = (v_normal + 1.0) * 0.5;
    #endif


    positionOut = (v_position + 1.0) * 0.5;
}
