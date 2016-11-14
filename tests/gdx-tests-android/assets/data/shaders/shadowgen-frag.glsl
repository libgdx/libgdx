#ifdef GL_ES
precision highp float; 
#endif

varying vec4 v_position;

void main(void)
{
	float normalizedDistance  = v_position.z / v_position.w;
	normalizedDistance = (normalizedDistance + 1.0) / 2.0;
	normalizedDistance += 0.001;

	const vec4 packFactors = vec4(256.0 * 256.0 * 256.0, 256.0 * 256.0, 256.0, 1.0);
	const vec4 bitMask     = vec4(0.0 , 1.0 / 256.0, 1.0 / 256.0, 1.0 / 256.0);
	vec4 packedValue = vec4(fract(packFactors*normalizedDistance));
	packedValue -= packedValue.xxyz * bitMask;
	gl_FragColor  = packedValue;
}