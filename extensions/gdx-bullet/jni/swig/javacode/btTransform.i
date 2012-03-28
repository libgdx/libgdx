/*
 *	Extra Java methods.
 */
 
%module btTransform

%typemap(javacode) btTransform %{

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
