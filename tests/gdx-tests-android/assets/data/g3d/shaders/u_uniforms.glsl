
// Uniforms which are always available
#include ":u_projViewTrans"
#include ":u_worldTrans"
#include ":u_cameraPosition"
#include ":u_normalMatrix"
// Other uniforms
#include ":u_opacity"
#include ":u_alphaTest"
#include ":u_shininess"

#include ":u_diffuseColor"
#include ":u_diffuseTexture"
#include ":u_specularColor"
#include ":u_specularTexture"
#include ":u_normalTexture"
 
[u_projViewTrans]
 	uniform mat4 u_projViewTrans;

[u_worldTrans]
	uniform mat4 u_worldTrans;

[u_cameraPosition]
	uniform vec4 u_cameraPosition;

[u_normalMatrix]
	uniform mat3 u_normalMatrix;

[u_opacity]
#ifdef blendedFlag
 	uniform float u_opacity;
#else
	const float u_opacity = 1.0;
#endif

[u_alphaTest]
#ifdef alphaTestFlag
 	uniform float u_alphaTest;
#else
	const float u_alphaTest = 0.0;
#endif

[u_shininess]
#ifdef shininessFlag
 	uniform float u_shininess;
#else
	const float u_shininess = 20.0;
#endif

[u_diffuseColor]
#ifdef diffuseColorFlag
 	uniform vec4 u_diffuseColor;
#endif

[u_diffuseTexture]
#ifdef diffuseTextureFlag
	uniform sampler2D u_diffuseTexture;
#endif

[u_specularColor]
#ifdef specularColorFlag
	uniform vec4 u_specularColor;
#endif

[u_specularTexture]
#ifdef specularTextureFlag
 	uniform sampler2D u_specularTexture;
#endif

[u_normalTexture]
#ifdef normalTextureFlag
 	uniform sampler2D u_normalTexture;
#endif
