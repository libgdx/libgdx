/*
 * Remap Bullet's void* usage to Java longs, so many "user pointer"
 * methods are useful.
 */
 

%apply unsigned long { void * };

//%typemap(jni) 				void * 	"jlong"
//%typemap(jstype) 			void *	"long"
//%typemap(jtype) 			void *	"long"
//%typemap(in)				void *	"$1 = (void *) $input;"
//%typemap(out)				void *	"$result = (jlong) $1;"
//%typemap(javain)			void *	"$javainput"
//%typemap(javaout)			void *	{ return $jnicall; }
//%typemap(javadirectorin)	void *	"/f* marg *f/ $1"
//%typemap(javadirectorout)	void *	"/f* gram *f/ $javacall"
//%typemap(directorin)		void *	"/f* fump *f/ $input = (jlong) $1;"
//%typemap(directorout)		void *	"/f* pree *f/ $result = (jlong) $input;"
