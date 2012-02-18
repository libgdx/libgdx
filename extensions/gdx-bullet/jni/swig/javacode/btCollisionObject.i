/*
 *	Extra Java methods for btTransform.
 */
 
%module btCollisionObject

/* Downcast support */
%typemap(javacode) btCollisionObject %{

  static {
    new SharedLibraryLoader().load("gdx-bullet");
  }
%}