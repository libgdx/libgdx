precision mediump float;

in vec4 v_color;
in vec2 v_texCoords;

uniform vec3 u_viewPos;
uniform mat4 u_inverseProjectionMatrix;

uniform sampler2D u_diffuseTexture;
uniform sampler2D u_normalTexture;
uniform sampler2D u_positionTexture;
uniform sampler2D u_depthTexture;

out vec4 fragColor;

struct Light {
    vec3 lightPosition;
    vec3 lightColor;
};

const int NUM_LIGHTS = 10;
uniform Light lights[NUM_LIGHTS];

void main () {
    vec4 diffuse = texture(u_diffuseTexture, v_texCoords);
    vec3 normal = (texture(u_normalTexture, v_texCoords).xyz * 2.0) - 1.0;
    //vec3 position = texture(u_positionTexture, v_texCoords).xyz;
    float specular = diffuse.a;

    float near = 1.0;
    float far = 100.0;

    float depth = texture(u_depthTexture, v_texCoords).x * 2.0 - 1.0;

    vec4 ndc = vec4(v_texCoords * 2.0 - 1.0, depth, 1.0);

    vec4 position = (u_inverseProjectionMatrix * ndc);
    position /= position.w;

    vec3 lighting = diffuse.xyz * 0.3; //ambient
    vec3 viewDir = normalize(u_viewPos - position.xyz);

    vec3 halfview;
    float spec;

    float shiney = 1007.0;
    vec3 globalLightColor = vec3(1.0);
    vec3 spotLightColor = vec3(1.0);


    //spot light
    for (int i = 0; i < NUM_LIGHTS; ++i) {
        vec3 lightDir = normalize(lights[i].lightPosition - position.xyz);
        float dist2 = dot(lightDir, lightDir);
        lightDir *= inversesqrt(dist2);
        float NdotL = clamp(dot(normal, lightDir), 0.0, 1.0);

        float distance = length(lights[i].lightPosition - position.xyz);
        float fallOff = (0.001 * distance) + (0.04 * distance * distance);

        vec3 value = lights[i].lightColor * (NdotL / (1.0 + fallOff));

        vec3 pointdiffuse = value;

        float halfDotView = max(0.0, dot(normal, normalize(lightDir + viewDir)));
        vec3 pointSpec = value * pow(halfDotView, shiney);

        lighting += (pointdiffuse + pointSpec) * diffuse.xyz;
    }



    //global light
    vec3 globalLightDir = normalize(vec3(0.0, 100.0, 0.0) - position.xyz);
    vec3 global = max(dot(normal, globalLightDir), 0.0) * globalLightColor * diffuse.xyz;

    //specular
    halfview = normalize(globalLightDir + viewDir);
    spec = pow(max(dot(normal, halfview), 0.0), shiney);
    vec3 specularglobal = globalLightColor * spec * specular;

    lighting += global + specularglobal;

    fragColor = vec4(lighting, 1.0);
}
