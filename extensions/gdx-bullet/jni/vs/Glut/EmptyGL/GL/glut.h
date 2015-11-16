#ifndef	EMPTY_GLUT_H
#define	EMPTY_GLUT_H

#include <GL/gl.h>
#include <GL/glu.h>


#define	GLUT_KEY_F1				0
#define	GLUT_KEY_F2				1
#define	GLUT_KEY_END			2
#define	GLUT_KEY_LEFT			3
#define	GLUT_KEY_RIGHT			4
#define	GLUT_KEY_UP				5
#define	GLUT_KEY_DOWN			6
#define	GLUT_KEY_PAGE_UP		7
#define	GLUT_KEY_PAGE_DOWN		8
#define	GLUT_KEY_HOME			9
#define GLUT_KEY_F3				10
#define GLUT_KEY_F4				11
#define	GLUT_KEY_F5				12
#define GLUT_ACTIVE_SHIFT		13


#define	GLUT_DOUBLE				1
#define	GLUT_RGBA				2
#define	GLUT_DEPTH				4
#define GLUT_STENCIL			8
#define	GLUT_WINDOW_WIDTH		16
#define	GLUT_WINDOW_HEIGHT		32
#define GLUT_RGB				64




inline	void	glutSwapBuffers()	{}
inline	void	glutShowWindow()	{}
inline	void	glutPostRedisplay()	{}
inline	void	glutInit(int *argc, char **argv)	{}
inline	void	glutInitDisplayMode( unsigned int )	{}
inline	void	glutInitWindowPosition(int x, int y)	{}
inline	void	glutInitWindowSize(int x, int y)		{}
inline	int		glutCreateWindow( const char *str)	{return 0;}
inline	void	glutKeyboardFunc( void (*func)(unsigned char, int ,int ) )	{}
inline	void	glutSpecialFunc( void (*func)(int key, int x,int y) )	{}
inline	void	glutSpecialUpFunc( void (*func)(int key, int x,int y) )	{}
inline	void	glutReshapeFunc( void (*func)(int w,int h) )				{}
inline	void	glutDisplayFunc( void (*func)() )					{}
inline	void	glutIdleFunc( void (*func)() )					{}
inline	void	glutMotionFunc( void (*func)(int x,int y) )					{}
inline	void	glutMouseFunc( void (*func)(int button,int state,int x,int y) )	{}
inline	void	glutMainLoop()	{}
inline void	glutSetWindow(int bla) {}

inline	void	glutSolidCube(GLfloat )		{}
inline	void	glutSolidSphere(GLfloat , int a , int b)	{}
inline	void	glutSolidCone(GLfloat ,GLfloat , int a , int b)	{}
inline	int		glutGetModifiers() { return 0;}
inline	void	gluPerspective(float a,float b,float c,float d) {}
inline float	glutGet(int code) { return 0.f;}
#endif
