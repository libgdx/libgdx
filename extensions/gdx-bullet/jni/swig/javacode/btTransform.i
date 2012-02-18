/*
 *	Extra Java methods for btTransform.
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
  
  /**
   * Gets the basis into the specified Matrix3.
   */
  public void getBasis(Matrix3 out) {
  	out.set(getBasis());
  }
  
  /**
   * Gets the origin into the specified Vector3.
   */
  public void getOrigin(Vector3 out) {
    out.set(getOrigin());
  }
  
  @Override
  public String toString() {
    return getOrigin() + "\n" + getBasis();
  }
%}
