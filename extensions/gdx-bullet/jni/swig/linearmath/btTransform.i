/*
 *	Extra Java methods.
 */
 
%module btTransform

%include "../common/gdxDisableBuffers.i"
%include "../common/gdxEnableArrays.i"

%rename(getBasisConst) btTransform::getBasis() const;
%rename(getOriginConst) btTransform::getOrigin() const;

%{
#include <LinearMath/btTransform.h>
%}
%include "LinearMath/btTransform.h"

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
%include "../common/gdxDisableArrays.i"
%include "../common/gdxEnableBuffers.i"
