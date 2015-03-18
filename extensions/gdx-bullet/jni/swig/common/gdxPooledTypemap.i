// THE MACRO
// NOTE: JTYPE must have a ctor
%define CREATE_POOLED_TYPEMAP(CTYPE, JTYPE, JCLASS, FROMJTOC, FROMCTOJ)
%fragment("gdxBulletHelpers##JTYPE", "header", fragment="gdxPoolMethods##JTYPE") {
	
// Workaround for some strange swig behaviour
#define TOSTRING##JTYPE(X)	"X"

	/* Gets a global ref to the temp class's Return JTYPE.  Do not release this. */ 
	SWIGINTERN inline jobject gdx_getReturn##JTYPE(JNIEnv * jenv) {
	  static jobject ret = NULL;
	  if (ret == NULL) {
	    jclass tempClass = gdx_getTempClass##JTYPE(jenv);
	    jfieldID field = jenv->GetStaticFieldID(tempClass, TOSTRING##JTYPE(static##JTYPE), JCLASS);
	    ret = jenv->NewGlobalRef(jenv->GetStaticObjectField(tempClass, field));
	  }
	  return ret;
	}
	
	/* Sets the data in the Bullet type from the Gdx type. */
	SWIGINTERN inline void gdx_set##CTYPE##From##JTYPE(JNIEnv * jenv, CTYPE & target, jobject source) {
		FROMJTOC(jenv, target, source);
	}

	SWIGINTERN inline void gdx_set##CTYPE##From##JTYPE(JNIEnv * jenv, CTYPE * target, jobject source) {
		gdx_set##CTYPE##From##JTYPE(jenv, *target, source);
	}

	/* Sets the data in the Gdx type from the Bullet type. */
	SWIGINTERN inline void gdx_set##JTYPE##From##CTYPE(JNIEnv * jenv, jobject target, const CTYPE & source) {
		FROMCTOJ(jenv, target, source);
	}

	SWIGINTERN inline void gdx_set##JTYPE##From##CTYPE(JNIEnv * jenv, jobject target, const CTYPE * source) {
		gdx_set##JTYPE##From##CTYPE(jenv, target, *source);
	}

	/*
	 * RAII wrapper to commit changes made to a local CTYPE back to JTYPE
	 */
	class gdxAutoCommit##JTYPE {
	private:
	  JNIEnv * jenv;
	  jobject j##JTYPE;
	  CTYPE & c##CTYPE;
	public:
	  gdxAutoCommit##JTYPE(JNIEnv * jenv, jobject j##JTYPE, CTYPE & c##CTYPE) : 
	    jenv(jenv), j##JTYPE(j##JTYPE), c##CTYPE(c##CTYPE) { };
	  gdxAutoCommit##JTYPE(JNIEnv * jenv, jobject j##JTYPE, CTYPE * c##CTYPE) : 
	    jenv(jenv), j##JTYPE(j##JTYPE), c##CTYPE(*c##CTYPE) { };
	  virtual ~gdxAutoCommit##JTYPE() {
	    gdx_set##JTYPE##From##CTYPE(this->jenv, this->j##JTYPE, this->c##CTYPE);
	  };
	};

	class gdxAutoCommit##CTYPE##AndRelease##JTYPE {
	private:
	  JNIEnv * jenv;
	  jobject j##JTYPE;
	  CTYPE & c##CTYPE;
	  const char * poolName;
	public:
	  gdxAutoCommit##CTYPE##AndRelease##JTYPE(JNIEnv * jenv, jobject j##JTYPE, CTYPE & c##CTYPE, const char *poolName) : 
	    jenv(jenv), j##JTYPE(j##JTYPE), c##CTYPE(c##CTYPE), poolName(poolName) { };
	  gdxAutoCommit##CTYPE##AndRelease##JTYPE(JNIEnv * jenv, jobject j##JTYPE, CTYPE * c##CTYPE, const char *poolName) : 
	    jenv(jenv), j##JTYPE(j##JTYPE), c##CTYPE(*c##CTYPE), poolName(poolName) { };
	  virtual ~gdxAutoCommit##CTYPE##AndRelease##JTYPE() {
	    gdx_set##CTYPE##From##JTYPE(this->jenv, this->c##CTYPE, this->j##JTYPE);
	    gdx_releasePoolObject##JTYPE(this->jenv, this->poolName, this->j##JTYPE);
	  };
	};
}

%pragma(java) modulecode=%{
	/** Temporary JTYPE instance, used by native methods that return a JTYPE instance */
	public final static JTYPE static##JTYPE = new JTYPE();
	/** Pool of JTYPE, used by native (callback) method for the arguments */
	public final static com.badlogic.gdx.utils.Pool<JTYPE> pool##JTYPE = new com.badlogic.gdx.utils.Pool<JTYPE>() {
		@Override
		protected JTYPE newObject() {
			return new JTYPE();
		}
	};
%}
%enddef // CREATE_POOLED_TYPEMAP

%define ENABLE_POOLED_TYPEMAP(CTYPE, JTYPE, JCLASS)
%typemap(jstype) 			CTYPE, CTYPE &, const CTYPE & 	"JTYPE"
%typemap(jtype) 			CTYPE, CTYPE &, const CTYPE & 	"JTYPE"
%typemap(javain)			CTYPE, CTYPE &, const CTYPE &	"$javainput"
%typemap(javadirectorin)	CTYPE, CTYPE &, const CTYPE &	"$1"
%typemap(javadirectorout)	CTYPE, CTYPE &, const CTYPE &	"$javacall"
%typemap(jni) 				CTYPE, CTYPE &, const CTYPE & 	"jobject"

%typemap(in, fragment="gdxBulletHelpers##JTYPE", noblock=1)		CTYPE	{
	gdx_set##CTYPE##From##JTYPE(jenv, $1, $input);
}
%typemap(in, fragment="gdxBulletHelpers##JTYPE", noblock=1)		CTYPE &, const CTYPE &	{
	CTYPE local_$1;
	gdx_set##CTYPE##From##JTYPE(jenv, local_$1, $input);
	$1 = &local_$1;
	gdxAutoCommit##JTYPE auto_commit_$1(jenv, $input, &local_$1);
}
%typemap(directorin, fragment="gdxBulletHelpers##JTYPE", descriptor=JCLASS, noblock=1)	const CTYPE & {
	$input = gdx_takePoolObject##JTYPE(jenv, TOSTRING##JTYPE(pool##JTYPE));
	gdx_set##JTYPE##From##CTYPE(jenv, $input, $1);
	gdxPoolAutoRelease##JTYPE autoRelease_$input(jenv, TOSTRING##JTYPE(pool##JTYPE), $input);
}
%typemap(directorin, fragment="gdxBulletHelpers##JTYPE", descriptor=JCLASS, noblock=1)	CTYPE, CTYPE & {
	$input = gdx_takePoolObject##JTYPE(jenv, TOSTRING##JTYPE(pool##JTYPE));
	gdx_set##JTYPE##From##CTYPE(jenv, $input, $1);
	gdxAutoCommit##CTYPE##AndRelease##JTYPE auto_commit_$1(jenv, $input, &$1, TOSTRING##JTYPE(pool##JTYPE));
}
%typemap(out, fragment="gdxBulletHelpers##JTYPE", noblock=1)		CTYPE, CTYPE &, const CTYPE &	{
	$result = gdx_getReturn##JTYPE(jenv);
	gdx_set##JTYPE##From##CTYPE(jenv, $result, $1);
}
%typemap(javaout)		CTYPE, CTYPE &, const CTYPE &	{
	return $jnicall;
}
%typemap(directorout, fragment="gdxBulletHelpers##JTYPE", descriptor=JCLASS, noblock=1) 	CTYPE {
	gdx_set##CTYPE##From##JTYPE(jenv, $result, $input);
}
/* allocate a local so we don't write to static default */
%typemap(directorout, fragment="gdxBulletHelpers##JTYPE", descriptor=JCLASS, noblock=1) 	CTYPE &, const CTYPE & {
	CTYPE local_$result;
	gdx_set##CTYPE##From##JTYPE(jenv, local_$result, $input);
	$result = &local_$result;
}
%enddef // ENABLE_POOLED_TYPEMAP

%define DISABLE_POOLED_TYPEMAP(CTYPE)
%clear CTYPE;
%clear CTYPE &;
%clear const CTYPE &;
%enddef // DISABLE_POOLED_TYPEMAP