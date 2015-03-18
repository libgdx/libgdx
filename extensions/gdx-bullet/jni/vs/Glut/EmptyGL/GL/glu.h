#ifndef	EMPTY_GLU
#define	EMPTY_GLU

inline	void	gluOrtho2D( int a,int b, int c ,int d ) { } ;
inline	void	gluLookAt( 
				GLfloat a,GLfloat b, GLfloat c, 
				GLfloat d,GLfloat e, GLfloat f, 
				GLfloat g,GLfloat h, GLfloat i ) { };

#define	GLU_FILL	1
#define	GLU_SMOOTH	2

typedef	int	GLUquadric;
typedef	GLUquadric	GLUquadricObj;

inline	GLUquadric		*gluNewQuadric()									{  return (GLUquadric *)1;	};
inline	void			gluQuadricDrawStyle( GLUquadric *o, int mode)	{};
inline	void			gluQuadricNormals( GLUquadric *o, int mode)		{};
inline	void			gluDeleteQuadric( GLUquadric *q)					{};
inline	void			 gluDisk (GLUquadric* quad, GLdouble inner, GLdouble outer, GLint slices, GLint loops) {};
inline	void			gluCylinder (GLUquadric* quad, GLdouble base, GLdouble top, GLdouble height, GLint slices, GLint stacks) {};
inline int 			gluBuild2DMipmaps (GLenum      target, GLint       components, GLint       width, GLint       height, GLenum      format, GLenum      type,  const void  *data) { return 0;}
#endif
