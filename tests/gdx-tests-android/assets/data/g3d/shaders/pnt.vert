attribute vec4 a_position;
attribute vec3 a_normal;

varying vec3 v_normal;

void main()	{
	v_normal = a_normal;
	gl_Position = a_position;
}
