[VS]
#include "g_attributes.glsl:VS"
#include "u_uniforms.glsl"
#include "skinning.glsl"
#include "common.glsl:VS"
#include "common.glsl:tangentVectorsVS"
#include "lights.glsl:VS"
#include "light_gouraud.glsl"
#include "shadowmap.glsl:VS"

void main() {
	g_position = u_worldTrans * applySkinning(g_position);
	gl_Position = u_projViewTrans * g_position;

	applyLights(g_position, 
			normalize(u_cameraPosition.xyz - g_position.xyz), 
			normalize(u_normalMatrix * applySkinning(g_normal)), 
			u_shininess);
	
	// Pass attributes values to the fragment shader
	passLights();
	pushColor();
	pushTexCoord0();
	passFog(calculateFog(g_position, u_cameraPosition));
	passShadowMapUV(calcShadowMapUV(g_position));
}


[FS]
#ifdef GL_ES 
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision mediump float;
#else
#define MED
#define LOWP
#define HIGH
#endif

#include "g_attributes.glsl:FS"
#include "u_uniforms.glsl"
#include "common.glsl:FS"
#include "common.glsl:colorDiffuseFS"
#include "common.glsl:colorSpecularFS"
#include "lights.glsl:FS"
#include "shadowmap.glsl:FS"

#ifdef fogFlag
uniform vec4 u_fogColor;
varying float v_fog;
#endif // fogFlag

void main() {
	pullLights();
	pullColor();
	pullTexCoord0();
	
	vec4 diffuse = applyColorDiffuse(g_color);

	#if (!defined(lightingFlag))  
		gl_FragColor.rgb = diffuse.rgb;
	#elif (!defined(specularFlag))
		#if defined(ambientFlag) && defined(separateAmbientFlag)
			#ifdef shadowMapFlag
				gl_FragColor.rgb = (diffuse.rgb * (g_lightAmbient + getShadow() * g_lightDiffuse));
				//gl_FragColor.rgb = texture2D(u_shadowTexture, v_shadowMapUv.xy);
			#else
				gl_FragColor.rgb = (diffuse.rgb * (g_lightAmbient + g_lightDiffuse));
			#endif //shadowMapFlag
		#else
			#ifdef shadowMapFlag
				gl_FragColor.rgb = getShadow() * (diffuse.rgb * g_lightDiffuse);
			#else
				gl_FragColor.rgb = (diffuse.rgb * g_lightDiffuse);
			#endif //shadowMapFlag
		#endif
	#else
		vec3 specular = applyColorSpecular(g_lightSpecular);
			
		#if defined(ambientFlag) && defined(separateAmbientFlag)
			#ifdef shadowMapFlag
			gl_FragColor.rgb = (diffuse.rgb * (getShadow() * g_lightDiffuse + g_lightAmbient)) + specular;
				//gl_FragColor.rgb = texture2D(u_shadowTexture, v_shadowMapUv.xy);
			#else
				gl_FragColor.rgb = (diffuse.rgb * (g_lightDiffuse + g_LightAmbient)) + specular;
			#endif //shadowMapFlag
		#else
			#ifdef shadowMapFlag
				gl_FragColor.rgb = getShadow() * ((diffuse.rgb * g_lightDiffuse) + specular);
			#else
				gl_FragColor.rgb = (diffuse.rgb * g_lightDiffuse) + specular;
			#endif //shadowMapFlag
		#endif
	#endif //lightingFlag

	#ifdef fogFlag
    	gl_FragColor.rgb = mix(gl_FragColor.rgb, u_fogColor.rgb, v_fog);
    #endif // end fogFlag

	#ifdef blendedFlag
		gl_FragColor.a = diffuse.a * v_opacity;
		#ifdef alphaTestFlag
			if (gl_FragColor.a <= v_alphaTest)
				discard;
		#endif
	#endif

}
