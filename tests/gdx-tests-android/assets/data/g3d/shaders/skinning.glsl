// Use the macro/function applySkinning(x) to apply skinning
// If skinning is not available, it will be a no-op,
// otherwise it will multiply x by the skinning matrix

#include ":skinning"

[bones]
// Declare the bones that are available
#if defined(numBones)
#if numBones > 0
 	uniform mat4 u_bones[numBones];
#endif //numBones
#endif

[skinning]
#include "a_attributes.glsl:a_boneWeights"
#include ":bones"

// If there are bones and there are bone weights, than we can apply skinning
#if defined(numBones) && defined(boneWeightsFlag)
#if (numBones > 0)
	#define skinningFlag
#endif
#endif

#ifdef skinningFlag
	mat4 skinningTransform = mat4(0.0)
	#ifdef boneWeight0Flag
		+ (a_boneWeight0.y) * u_bones[int(a_boneWeight0.x)]
	#endif //boneWeight0Flag
	#ifdef boneWeight1Flag				
		+ (a_boneWeight1.y) * u_bones[int(a_boneWeight1.x)]
	#endif //boneWeight1Flag
	#ifdef boneWeight2Flag		
		+ (a_boneWeight2.y) * u_bones[int(a_boneWeight2.x)]
	#endif //boneWeight2Flag
	#ifdef boneWeight3Flag
		+ (a_boneWeight3.y) * u_bones[int(a_boneWeight3.x)]
	#endif //boneWeight3Flag
	#ifdef boneWeight4Flag
		+ (a_boneWeight4.y) * u_bones[int(a_boneWeight4.x)]
	#endif //boneWeight4Flag
	#ifdef boneWeight5Flag
		+ (a_boneWeight5.y) * u_bones[int(a_boneWeight5.x)]
	#endif //boneWeight5Flag
	#ifdef boneWeight6Flag
		+ (a_boneWeight6.y) * u_bones[int(a_boneWeight6.x)]
	#endif //boneWeight6Flag
	#ifdef boneWeight7Flag
		+ (a_boneWeight7.y) * u_bones[int(a_boneWeight7.x)]
	#endif //boneWeight7Flag
	;
#endif //skinningFlag

#ifdef skinningFlag
	vec3 applySkinning(const in vec3 x) { return (skinningTransform * vec4(x, 0.0)).xyz; }
	vec4 applySkinning(const in vec4 x) { return (skinningTransform * x); }
#else
	#define applySkinning(x) (x)
#endif //skinningFlag
