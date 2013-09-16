/**
 * passAttribute: only executed when the attribute is provided by the shader, no extra code in fragment needed
 * pushAttribute: always executed, requires pullAttribute in fragment
 */

/**
 * Working with g_attributes, three possibilities:
 * 
 * 1. Vertex shader only
 * - In the vertex shader include the required attribute snippet.
 * - Access the attribute as global variable with the "g_" prefix (e.g. g_position), 
 * 		if the attribute isn't available the variable is set to a default value
 * 
 * 2. Fragment shader only
 * - In the vertex shader include the required attribute snippet.
 * - In the VS main method add the line: "passAttribute();" (e.g. passColor(); or passNormal();)
 * 		If the attribute is available it will passed using a varying, otherwise it's a no-op
 * - In the fragment shader include the required attribute snippet.
 * - Access the attribute as global variable with the "g_" prefix, 
 * 		if the attribute isn't available the variable is set to a default value
 * 	
 * 3. Both vertex and fragment shader
 * - In the vertex shader access the attribute as specified in 1.
 * - When done, call pushAttribute(); e.g. pushColor(); or pushNormal();
 * - In fragment shader include the required attribute snippet.
 * - In the FS main method call pullAttribute(); before using it (e.g. pullColor(); or pullNormal();)  
 */

[VS]
#include ":g_positionVS"
#include ":g_colorVS"
#include ":g_normalVS"
#include ":g_binormalVS"
#include ":g_tangentVS"
#include ":g_texCoord0VS"

[FS]
#include ":g_positionFS"
#include ":g_colorFS"
#include ":g_normalFS"
#include ":g_binormalFS"
#include ":g_tangentFS"
#include ":g_texCoord0FS"

[g_positionVS]
////////////////////////////////////////////////////////////////////////////////////
////////// POSITION ATTRIBUTE - VERTEX
////////////////////////////////////////////////////////////////////////////////////
#include "common.glsl:nop"
#include "a_attributes.glsl:a_position"
 	varying vec4 v_position;
#define pushPositionValue(value) (v_position = (value))
#if defined(positionFlag)
	vec4 g_position = vec4(a_position, 1.0);
	#define passPositionValue(value) pushPositionValue(value)
#else
	vec4 g_position = vec4(0.0, 0.0, 0.0, 1.0);
	#define passPositionValue(value) nop()
#endif
#define passPosition() passPositionValue(g_position)
#define pushPosition() pushPositionValue(g_position)

[g_positionFS]
////////////////////////////////////////////////////////////////////////////////////
////////// POSITION ATTRIBUTE - FRAGMENT
////////////////////////////////////////////////////////////////////////////////////
#include "common.glsl:nop"
varying vec4 v_position;
vec4 g_position = vec4(0.0, 0.0, 0.0, 1.0);
#define pullPosition() (g_position = v_position)
	
[g_colorVS]
////////////////////////////////////////////////////////////////////////////////////
////////// COLOR ATTRIBUTE - VERTEX
///////////////////////////////////////////////////////////////////////////////////
#include "common.glsl:nop"
#include "a_attributes.glsl:a_color"
	varying vec4 v_color;
#define pushColorValue(value) (v_color = (value))
#if defined(colorFlag)
	vec4 g_color = a_color;
	#define passColorValue(value) pushColorValue(value)
#else
	vec4 g_color = vec4(1.0, 1.0, 1.0, 1.0);
	#define passColorValue(value) nop()	
#endif
#define passColor() passColorValue(g_color)
#define pushColor()	pushColorValue(g_color)

[g_colorFS]
////////////////////////////////////////////////////////////////////////////////////
////////// COLOR ATTRIBUTE - FRAGMENT
///////////////////////////////////////////////////////////////////////////////////
#include "common.glsl:nop"
varying vec4 v_color;
vec4 g_color = vec4(1.0, 1.0, 1.0, 1.0);
#define pullColor()	(g_color = v_color)

[g_normalVS]
////////////////////////////////////////////////////////////////////////////////////
////////// NORMAL ATTRIBUTE - VERTEX
///////////////////////////////////////////////////////////////////////////////////
#include "common.glsl:nop"
#include "a_attributes.glsl:a_normal"
	varying vec3 v_normal;
#define pushNormalValue(value) (v_normal = (value))
#if defined(normalFlag)
	vec3 g_normal = a_normal;
	#define passNormalValue(value) pushNormalValue(value)
#else
	vec3 g_normal = vec3(0.0, 0.0, 1.0);
	#define passNormalValue(value) nop() 
#endif
#define passNormal() (passNormalValue(g_normal))
#define pushNormal() (pushNormalValue(g_normal))

[g_normalFS]
////////////////////////////////////////////////////////////////////////////////////
////////// NORMAL ATTRIBUTE - FRAGMENT
///////////////////////////////////////////////////////////////////////////////////
#include "common.glsl:nop"
varying vec3 v_normal;
vec3 g_normal = vec3(0.0, 0.0, 1.0);
#define pullNormal() (g_normal = v_normal)

[g_binormalVS]
////////////////////////////////////////////////////////////////////////////////////
////////// BINORMAL ATTRIBUTE - VERTEX
///////////////////////////////////////////////////////////////////////////////////
#include "common.glsl:nop"
#include "a_attributes.glsl:a_binormal"
	varying vec3 v_binormal;
#define pushBinormalValue(value) (v_binormal = (value))
#if defined(binormalFlag)
	vec3 g_binormal = a_binormal;
	#define passBinormalValue(value) pushBinormalValue(value)
#else
	vec3 g_binormal = vec3(0.0, 1.0, 0.0);
	#define passBinormalValue(value) nop()
#endif // binormalFlag
#define passBinormal() passBinormalValue(g_binormal)
#define pushBinormal() pushBinormalValue(g_binormal)

[g_binormalFS]
////////////////////////////////////////////////////////////////////////////////////
////////// BINORMAL ATTRIBUTE - FRAGMENT
///////////////////////////////////////////////////////////////////////////////////
#include "common.glsl:nop"
varying vec3 v_binormal;
vec3 g_binormal = vec3(0.0, 0.0, 1.0);
#define pullBinormal() (g_binormal = v_binormal)

[g_tangentVS]
////////////////////////////////////////////////////////////////////////////////////
////////// TANGENT ATTRIBUTE - VERTEX
///////////////////////////////////////////////////////////////////////////////////
#include "common.glsl:nop"
#include "a_attributes.glsl:a_tangent"
	varying vec3 v_tangent;
#define pushTangentValue(value) (v_tangent = (value))
#if defined(tangentFlag)
	vec3 g_tangent = a_tangent;
	#define passTangentValue(value) pushTangentValue(value)
#else
	vec3 g_tangent = vec3(1.0, 0.0, 0.0);
	#define passTangentValue(value) nop()
#endif // tangentFlag
#define passTangent() passTangentValue(g_tangent)
#define pushTangent() pushTangentValue(g_tangent)

[g_tangentFS]
////////////////////////////////////////////////////////////////////////////////////
////////// TANGENT ATTRIBUTE - FRAGMENT
///////////////////////////////////////////////////////////////////////////////////
#include "common.glsl:nop"
varying vec3 v_tangent;
vec3 g_tangent = vec3(1.0, 0.0, 0.0);
#define pullTangent() (g_tangent = v_tangent)

[g_texCoord0VS]
////////////////////////////////////////////////////////////////////////////////////
////////// TEXCOORD0 ATTRIBUTE - VERTEX
///////////////////////////////////////////////////////////////////////////////////
#include "common.glsl:nop"
#include "a_attributes.glsl:a_texCoord0"
	varying vec2 v_texCoord0;
#define pushTexCoord0Value(value) (v_texCoord0 = value)
#if defined(texCoord0Flag)
	vec2 g_texCoord0 = a_texCoord0;
	#define passTexCoord0Value(value) pushTexCoord0Value(value)
#else
	vec2 g_texCoord0 = vec2(0.0, 0.0);
	#define passTexCoord0Value(value) nop()	
#endif // texCoord0Flag
#define passTexCoord0() passTexCoord0Value(g_texCoord0)
#define pushTexCoord0() pushTexCoord0Value(g_texCoord0)

[g_texCoord0FS]
////////////////////////////////////////////////////////////////////////////////////
////////// TEXCOORD0 ATTRIBUTE - FRAGMENT
///////////////////////////////////////////////////////////////////////////////////
#include "common.glsl:nop"
varying vec2 v_texCoord0;
vec2 g_texCoord0 = vec2(0.0, 0.0);
#define pullTexCoord0() (g_texCoord0 = v_texCoord0)
