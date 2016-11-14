#ifdef GL_ES
precision highp float; 
#endif

varying vec4 v_lightSpacePosition;
varying vec4 v_color;

uniform sampler2D s_shadowMap;

float unpack(vec4 packedZValue)
{	
	const vec4 unpackFactors = vec4( 1.0 / (256.0 * 256.0 * 256.0), 1.0 / (256.0 * 256.0), 1.0 / 256.0, 1.0 );
	return dot(packedZValue,unpackFactors);
}

float getShadowFactor(vec4 lightZ)
{
	vec4 packedZValue = texture2D(s_shadowMap, lightZ.st);
	float unpackedZValue = unpack(packedZValue);
	return float(unpackedZValue > lightZ.z);
}

void main(void) 
{	
	float shadowFactor=1.0;				
	vec4 lightZ = v_lightSpacePosition / v_lightSpacePosition.w;
	lightZ = (lightZ + 1.0) / 2.0;
	shadowFactor = getShadowFactor(lightZ);	
	gl_FragColor = v_color * shadowFactor;
}