/*
 * Typemaps support for types that may need to be downcast to derived types.
 * Make sure the proxy classes have a "newDerivedObject" implementation
 * (see javacode/* for examples).
 */
 
/* Add more types to the list as needed. */

%typemap(javaout) 	btCollisionShape *, const btCollisionShape * {
    long cPtr = $jnicall;
    return (cPtr == 0) ? null : $*javaclassname.newDerivedObject(cPtr, $owner);
  }
