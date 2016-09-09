/** Managed objects are java objects of which the reference is maintained by the wrapper and the instance is reused
 * by using a LongMap lookup with the native pointer.
 * 
 * Use managed objects for classes which are likely to be created by the user and are expected to live long. 
 * @author Xoppa */
%define CREATE_MANAGED_OBJECT(_TYPE)

%typemap(javaout) 	_TYPE *, const _TYPE *, _TYPE * const & {
	return _TYPE.getInstance($jnicall, $owner);
}

%typemap(javadirectorin) _TYPE *, const _TYPE *, _TYPE * const &	"_TYPE.getInstance($1, false)"
%enddef // CREATE_MANAGED_OBJECT