%module btCompoundShape

%typemap(javaimports) btCompoundShape %{
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
%}

%typemap(javacode) btCompoundShape %{
	protected Array<btCollisionShape> children = new Array<btCollisionShape>();
	
	public void addChildShape(Matrix4 localTransform, btCollisionShape shape, boolean managed) {
		addChildShape(localTransform, shape);
		if (managed)
			children.add(shape);
	}
	
	protected void dispose() {
		for (int i = 0; i < children.size; i++)
			children.get(i).delete();
		children.clear();
	}
%}

%typemap(javadestruct_derived, methodname="delete", methodmodifiers="public synchronized") btCompoundShape %{ {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        gdxBulletJNI.delete_btCompoundShape(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
	dispose();
  }
%}