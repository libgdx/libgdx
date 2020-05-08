layout(triangles, equal_spacing, ccw) in;

uniform mat4 u_mvpMatrix;

in vec3 t_normal[];
in vec3 t_phong[];

#define Pi  (gl_in[0].gl_Position.xyz * gl_in[0].gl_Position.w)
#define Pj  (gl_in[1].gl_Position.xyz * gl_in[1].gl_Position.w)
#define Pk  (gl_in[2].gl_Position.xyz * gl_in[2].gl_Position.w)
#define tc1 gl_TessCoord
#define uTessAlpha 0.5

void main() {

	vec3 tc2 = tc1*tc1;
		
	vec3 barPos = gl_TessCoord[0]*Pi
				 + gl_TessCoord[1]*Pj
				 + gl_TessCoord[2]*Pk;
	
	vec3 termIJ = vec3(t_phong[0].x,
			t_phong[1].x,
			t_phong[2].x);
	vec3 termJK = vec3(t_phong[0].y,
			 t_phong[1].y,
			 t_phong[2].y);
	vec3 termIK = vec3(t_phong[0].z,
			 t_phong[1].z,
			 t_phong[2].z);
	 
	vec3 phongPos   = tc2[0]*Pi
					 + tc2[1]*Pj
					 + tc2[2]*Pk
					 + tc1[0]*tc1[1]*termIJ
					 + tc1[1]*tc1[2]*termJK
					 + tc1[2]*tc1[0]*termIK;

	vec3 finalPos = (1.0-uTessAlpha)*barPos + uTessAlpha*phongPos;
	
	gl_Position   = u_mvpMatrix * vec4(finalPos, 1.0);
}