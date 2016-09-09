%typemap(javabase, notderived="1") SWIGTYPE "BulletBase"

%typemap(javabody) SWIGTYPE %{
	private long swigCPtr;
	
	protected $javaclassname(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new $javaclassname, normally you should not need this constructor it's intended for low-level usage. */ 
	public $javaclassname(long cPtr, boolean cMemoryOwn) {
		this("$javaclassname", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr($javaclassname obj) {
		return (obj == null) ? 0 : obj.swigCPtr;
	}
%}

%typemap(javabody_derived) SWIGTYPE %{
	private long swigCPtr;
	
	protected $javaclassname(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, $imclassname.$javaclazznameSWIGUpcast(cPtr), cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new $javaclassname, normally you should not need this constructor it's intended for low-level usage. */
	public $javaclassname(long cPtr, boolean cMemoryOwn) {
		this("$javaclassname", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset($imclassname.$javaclazznameSWIGUpcast(swigCPtr = cPtr), cMemoryOwn);
	}
	
	public static long getCPtr($javaclassname obj) {
		return (obj == null) ? 0 : obj.swigCPtr;
	}
%}

%typemap(javadestruct, methodname="delete", methodmodifiers="@Override protected synchronized") SWIGTYPE {
		if (swigCPtr != 0) {
			if (swigCMemOwn) {
				swigCMemOwn = false;
				$jnicall;
			}
			swigCPtr = 0;
		}
		super.delete();
	}

%typemap(javadestruct_derived, methodname="delete", methodmodifiers="@Override protected synchronized") SWIGTYPE {
		if (swigCPtr != 0) {
			if (swigCMemOwn) {
				swigCMemOwn = false;
				$jnicall;
			}
			swigCPtr = 0;
		}
		super.delete();
	}

%typemap(javafinalize) SWIGTYPE %{
	@Override
	protected void finalize() throws Throwable {
		if (!destroyed)
			destroy();
		super.finalize();
	}
%}