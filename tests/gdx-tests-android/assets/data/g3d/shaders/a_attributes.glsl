/**
 * a_attributes.glsl: Include this file at the top of your vertex shader, to have all available attributes declared.
 */

/**
 * Declare the attributes the Shader provides to us, either using the mesh or a default value (it's up to the Shader to decide that).
 * We explicitly declare them only when the flag is set for two reasons:
 * 1. So you can't accidently use an attribute which isn't set by the shader.
 * 2. The shader might choose a different implementation when an attribute is actually used or not. 
 */
#include ":a_position"
#include ":a_color"
#include ":a_normal"
#include ":a_tangent"
#include ":a_binormal"
#include ":a_texCoords"
#include ":a_boneWeights"
 
[a_position]
#ifdef positionFlag
 	attribute vec3 a_position;
#endif //positionFlag

[a_color]
#ifdef colorFlag
 	attribute vec4 a_color;
#endif //colorFlag

[a_normal]
#ifdef normalFlag
 	attribute vec3 a_normal;
#endif //normalFlag
 	
[a_tangent]
#ifdef tangentFlag
 	attribute vec3 a_tangent;
#endif //tangentFlag

[a_binormal]
#ifdef binormalFlag
 	attribute vec3 a_binormal;
#endif //binormalFlag

[a_texCoords]
#include ":a_texCoord0"
#include ":a_texCoord1"
#include ":a_texCoord2"
#include ":a_texCoord3"
#include ":a_texCoord4"
#include ":a_texCoord5"
#include ":a_texCoord6"
#include ":a_texCoord7"

[a_texCoord0]
#ifdef texCoord0Flag
	#ifndef texCoordsFlag
		#define texCoordsFlag
	#endif
 	attribute vec2 a_texCoord0;
#endif

[a_texCoord1]
#ifdef texCoord1Flag
	#ifndef texCoordsFlag
		#define texCoordsFlag
	#endif
 	attribute vec2 a_texCoord1;
#endif

[a_texCoord2]
#ifdef texCoord2Flag
	#ifndef texCoordsFlag
		#define texCoordsFlag
	#endif	
 	attribute vec2 a_texCoord2;
#endif

[a_texCoord3]
#ifdef texCoord3Flag
	#ifndef texCoordsFlag
		#define texCoordsFlag
	#endif
 	attribute vec2 a_texCoord3;
#endif

[a_texCoord4]
#ifdef texCoord4Flag
	#ifndef texCoordsFlag
		#define texCoordsFlag
	#endif
 	attribute vec2 a_texCoord4;
#endif

[a_texCoord5]
#ifdef texCoord5Flag
	#ifndef texCoordsFlag
		#define texCoordsFlag
	#endif
 	attribute vec2 a_texCoord5;
#endif

[a_texCoord6]
#ifdef texCoord6Flag
	#ifndef texCoordsFlag
		#define texCoordsFlag
	#endif
 	attribute vec2 a_texCoord6;
#endif

[a_texCoord7]
#ifdef texCoord7Flag
	#ifndef texCoordsFlag
		#define texCoordsFlag
	#endif
 	attribute vec2 a_texCoord7;
#endif

[a_boneWeights]
#include ":a_boneWeight0"
#include ":a_boneWeight1"
#include ":a_boneWeight2"
#include ":a_boneWeight3"
#include ":a_boneWeight4"
#include ":a_boneWeight5"
#include ":a_boneWeight6"
#include ":a_boneWeight7"

[a_boneWeight0]
#ifdef boneWeight0Flag
	#ifndef boneWeightsFlag
		#define boneWeightsFlag
	#endif
 	attribute vec2 a_boneWeight0;
#endif //boneWeight0Flag

[a_boneWeight1]
#ifdef boneWeight1Flag
	#ifndef boneWeightsFlag
		#define boneWeightsFlag
	#endif
	attribute vec2 a_boneWeight1;
#endif //boneWeight1Flag

[a_boneWeight2]
#ifdef boneWeight2Flag
	#ifndef boneWeightsFlag
		#define boneWeightsFlag
	#endif
	attribute vec2 a_boneWeight2;
#endif //boneWeight2Flag

[a_boneWeight3]
#ifdef boneWeight3Flag
	#ifndef boneWeightsFlag
		#define boneWeightsFlag
	#endif
	attribute vec2 a_boneWeight3;
#endif //boneWeight3Flag

[a_boneWeight4]
#ifdef boneWeight4Flag
	#ifndef boneWeightsFlag
		#define boneWeightsFlag
	#endif
	attribute vec2 a_boneWeight4;
#endif //boneWeight4Flag

[a_boneWeight5]
#ifdef boneWeight5Flag
	#ifndef boneWeightsFlag
		#define boneWeightsFlag
	#endif
	attribute vec2 a_boneWeight5;
#endif //boneWeight5Flag

[a_boneWeight6]
#ifdef boneWeight6Flag
	#ifndef boneWeightsFlag
		#define boneWeightsFlag
	#endif
	attribute vec2 a_boneWeight6;
#endif //boneWeight6Flag

[a_boneWeight7]
#ifdef boneWeight7Flag
	#ifndef boneWeightsFlag
		#define boneWeightsFlag
	#endif
	attribute vec2 a_boneWeight7;
#endif //boneWeight7Flag
