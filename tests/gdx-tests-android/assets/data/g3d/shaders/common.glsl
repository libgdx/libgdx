

[FS]
#include ":defines"

[precision]
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

[VS]
#include ":defines"

[nop]
#define nop() {}
 
[defines]
#include ":textureFlag"
#include ":specularFlag"
#include ":cameraPositionFlag"
#include ":separateAmbientFlag"
  
[textureFlag]
#if defined(diffuseTextureFlag) || defined(specularTextureFlag)
	 #define textureFlag
#endif

[specularFlag]
#if defined(specularTextureFlag) || defined(specularColorFlag)
	 #define specularFlag
#endif

[cameraPositionFlag]
#if defined(specularFlag) || defined(fogFlag)
	 #define cameraPositionFlag
#endif
 
[separateAmbientFlag]
#ifdef shadowMapFlag
	 #define separateAmbientFlag
#endif

[tangentVectorsVS]
#include "common.glsl:nop"
#include "g_attributes.glsl:g_normalVS"
#include "g_attributes.glsl:g_binormalVS"
#include "g_attributes.glsl:g_tangentVS"

#if defined(normalFlag) && defined(binormalFlag) && defined(tangentFlag)
#define calculateTangentVectors() nop()
#elif defined(normalFlag) && defined(binormalFlag)
#define calculateTangentVectors() (g_tangent = normalize(cross(g_normal, g_binormal))) 
#elif defined(normalFlag) && defined(tangentFlag)
#define calculateTangentVectors() (g_binormal = normalize(cross(g_normal, g_tangent)))
#elif defined(binormalFlag) && defined(tangentFlag)
#define calculateTangentVectors() (g_normal = normalize(cross(g_binormal, g_tangent)))
#elif defined(normalFlag) || defined(binormalFlag) || defined(tangentFlag)
 	vec3 biggestAngle(const in vec3 base, const in vec3 v1, const in vec3 v2) {
		vec3 c1 = cross(base, v1);
		vec3 c2 = cross(base, v2);
		return (dot(c2, c2) > dot(c1, c1)) ? c2 : c1;
	}
	#if defined(normalFlag)
 	void calculateTangentVectors() {
		g_binormal = normalize(cross(g_normal, biggestAngle(g_normal, vec3(1.0, 0.0, 0.0), vec3(0.0, 1.0, 0.0))));
		g_tangent = normalize(cross(g_normal, g_binormal));
	}
	#elif defined(binormalFlag)
	void calculateTangentVectors() {
		g_tangent = normalize(cross(g_binormal, biggestAngle(g_binormal, vec3(0.0, 0.0, 1.0), vec3(0.0, 1.0, 0.0))));
		g_normal = normalize(cross(g_binormal, g_tangent));
	}
	#elif defined(tangentFlag)
	void calculateTangentVectors() {
		g_binormal = normalize(cross(g_tangent, biggestAngle(g_binormal, vec3(0.0, 0.0, 1.0), vec3(0.0, 1.0, 0.0))));
		g_normal = normalize(cross(g_tangent, g_binormal));
	}
	#endif
#endif
	
[colorDiffuse]
#include "u_uniforms.glsl:u_diffuseTexture"
#include "u_uniforms.glsl:u_diffuseColor"

#if defined(diffuseTextureFlag) && defined(diffuseColorFlag)
#define fetchColorDiffuseTD(texCoord, defaultValue) texture2D(u_diffuseTexture, texCoord) * u_diffuseColor
#elif defined(diffuseTextureFlag)
#define fetchColorDiffuseTD(texCoord, defaultValue) texture2D(u_diffuseTexture, texCoord)
#elif defined(diffuseColorFlag)
#define fetchColorDiffuseTD(texCoord, defaultValue) u_diffuseColor
#else
#define fetchColorDiffuseTD(texCoord, defaultValue) (defaultValue)
#endif

[colorDiffuseFS]
#include ":colorDiffuse"
#include "g_attributes.glsl:g_texCoord0FS" 

#define fetchColorDiffuseD(defaultValue) fetchColorDiffuseTD(g_texCoord0, defaultValue)
#define fetchColorDiffuse() fetchColorDiffuseD(vec4(1.0))

#if defined(diffuseTextureFlag) || defined(diffuseColorFlag)
#define applyColorDiffuse(baseColor) ((baseColor) * fetchColorDiffuse())
#else
#define applyColorDiffuse(baseColor) (baseColor)
#endif
 
[colorSpecular]
#include "u_uniforms.glsl:u_specularTexture"
#include "u_uniforms.glsl:u_specularColor"

#if defined(specularTextureFlag) && defined(specularColorFlag)
#define fetchColorSpecularTD(texCoord, defaultValue) (texture2D(u_specularTexture, texCoord).rgb * u_specularColor.rgb)
#elif defined(specularTextureFlag)
#define fetchColorSpecularTD(texCoord, defaultValue) texture2D(u_specularTexture, texCoord).rgb
#elif defined(specularColorFlag)
#define fetchColorSpecularTD(texCoord, defaultValue) u_specularColor.rgb
#else
#define fetchColorSpecularTD(texCoord, defaultValue) (defaultValue)
#endif
	
[colorSpecularFS]
#include ":colorSpecular"
#include "g_attributes.glsl:g_texCoord0FS" 

#define fetchColorSpecularD(defaultValue) fetchColorSpecularTD(g_texCoord0, defaultValue)
#define fetchColorSpecular() fetchColorSpecularD(vec3(0.0))

#if defined(specularTextureFlag) || defined(specularColorFlag)
#define applyColorSpecular(intensity) ((intensity) * fetchColorSpecular())
#define addColorSpecular(baseColor, intensity)	((baseColor) + applyColorSpecular(intensity))
#else
#define applyColorSpecular(intensity) (vec3(0.0))
#define addColorSpecular(baseColor, intensity)	(baseColor)
#endif
