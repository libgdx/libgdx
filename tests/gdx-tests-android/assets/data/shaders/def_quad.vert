#version 300 es

layout(location = 0)in vec4 inPos;

out vec2 texCoord;

void main()
{                                                   
	gl_Position = inPos;
	
	texCoord = inPos.xy * 0.5 + 0.5;
}                                                   
