
[VS]
#include "common.glsl:nop"
 	varying vec3 v_shadowMapUv;

#if defined(lightingFlag) && defined(shadowMapFlag)
	uniform mat4 u_shadowMapProjViewTrans;

	vec3 calcShadowMapUV(const in vec4 worldPos) {
		vec4 spos = u_shadowMapProjViewTrans * worldPos;
		v_shadowMapUv.xy = (spos.xy / spos.w) * 0.5 + 0.5;
		v_shadowMapUv.z = min(spos.z * 0.5 + 0.5, 0.998);
	}
	
	#define passShadowMapUV(v) (v_shadowMapUV = v)
#else
	#define calcShadowMapUV(x) vec3(0.0)
	#define passShadowMapUV(v) nop()
#endif
	#define pushShadowMapUV(v) (v_shadowMapUV = v)
	
[FS]
#if defined(lightingFlag) && defined(shadowMapFlag)
 	uniform sampler2D u_shadowTexture;
	uniform float u_shadowPCFOffset;
	varying vec3 v_shadowMapUv;

	float getShadowness(vec2 offset)
	{
	    const vec4 bitShifts = vec4(1.0, 1.0 / 255.0, 1.0 / 65025.0, 1.0 / 16581375.0);
	    return step(v_shadowMapUv.z, dot(texture2D(u_shadowTexture, v_shadowMapUv.xy + offset), bitShifts));//+(1.0/255.0));	
	}

	float getShadow() 
	{
		return (//getShadowness(vec2(0,0)) + 
				getShadowness(vec2(u_shadowPCFOffset, u_shadowPCFOffset)) +
				getShadowness(vec2(-u_shadowPCFOffset, u_shadowPCFOffset)) +
				getShadowness(vec2(u_shadowPCFOffset, -u_shadowPCFOffset)) +
				getShadowness(vec2(-u_shadowPCFOffset, -u_shadowPCFOffset))) * 0.20;
	}
#else
	#define getShadowness(offset) (1.0)
	#define getShadow() (1.0)
#endif
