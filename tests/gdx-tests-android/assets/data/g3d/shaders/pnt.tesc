layout (vertices = 3) out;

in vec3 v_normal[];
out vec3 t_normal[];
out vec3 t_phong[];

#define Pi  gl_in[0].gl_Position.xyz
#define Pj  gl_in[1].gl_Position.xyz
#define Pk  gl_in[2].gl_Position.xyz

float PIi(int i, vec3 q)
{
 vec3 q_minus_p = q - gl_in[i].gl_Position.xyz;
 return q[gl_InvocationID] - dot(q_minus_p, v_normal[i])
                           * v_normal[i][gl_InvocationID];
}

#define TESS 3.0

void main() {
	gl_TessLevelOuter[0] = TESS;
	gl_TessLevelOuter[1] = TESS;
	gl_TessLevelOuter[2] = TESS;
	gl_TessLevelInner[0] = TESS;
	gl_out[gl_InvocationID].gl_Position = gl_in[gl_InvocationID].gl_Position;
	t_normal[gl_InvocationID] = v_normal[gl_InvocationID];
	t_phong[gl_InvocationID] = vec3(
		PIi(0,Pj) + PIi(1,Pi),
		PIi(1,Pk) + PIi(2,Pj),
		PIi(2,Pi) + PIi(0,Pk)
	);
}