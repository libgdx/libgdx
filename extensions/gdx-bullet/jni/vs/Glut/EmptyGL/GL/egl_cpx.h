// todo - implement these as you see fit. 
inline GLint glRenderMode(GLenum  a0) { return GL_RENDER; };	// ymmv. should return previous mode
inline GLenum glGetError() { return GL_NO_ERROR; };
inline GLboolean glIsList(GLuint  a0) {	return	GL_TRUE;	};	
inline GLuint glGenLists(GLsizei  a0) { return (GLuint)a0;	};
inline const GLubyte* glGetString(GLenum  a0) {	return	(const GLubyte *)"egl-xyzzy";	};
inline GLboolean glIsEnabled(GLenum  a0) {	return	GL_TRUE;	};
inline GLboolean glAreTexturesResident(GLsizei  a0,const GLuint *  a1,GLboolean *  a2) {	return	GL_TRUE;	};
inline GLboolean glIsTexture(GLuint  a0) {	return	GL_TRUE;	};
inline void glGetBooleanv(GLenum  a0,GLboolean *  a1) {	*a1 = GL_TRUE;	};
inline void glGetDoublev(GLenum  a0,GLdouble *  a1) {	*a1 = 0.0; };
inline void glGetFloatv(GLenum  a0,GLfloat *  a1) {	*a1 = 0.0f;	};
inline void glGetIntegerv(GLenum  a0,GLint *  a1) { *a1 = 0;	};
