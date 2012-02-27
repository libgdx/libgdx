/*
 *	Extra Java methods.
 */
 
%module btTransform

%typemap(javacode) btTransform %{

  static {
    new SharedLibraryLoader().load("gdx-bullet");
  }

  /**
   * Sets the values in this transform from the other.
   */
  public void set(btTransform other) {
  	setOrigin(other.getOrigin());
    setBasis(other.getBasis());
  }
  
  @Override
  public String toString() {
    return getOrigin() + "\n" + getBasis();
  }
%}
