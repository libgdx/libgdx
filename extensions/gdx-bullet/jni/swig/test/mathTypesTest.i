%feature("director") mathTypesTest;

%inline %{

class mathTypesTest {
public:
	mathTypesTest() { };
	virtual ~mathTypesTest() { };
	
	btVector3 			v1(btVector3 in) { return btVector3(1,2,3); };
	btVector3 * 		v2(btVector3 * in) { return NULL; };
	const btVector3 & 	v3(btVector3 & in) { return btVector3(1,2,3); };
	
	btQuaternion 			q1(btQuaternion in) { return btQuaternion(1,2,3,4); };
	btQuaternion * 			q2(btQuaternion * in) { return NULL; };
	const btQuaternion & 	q3(btQuaternion & in) { return btQuaternion(1,2,3,4); }
	
	btMatrix3x3 			m1(btMatrix3x3 in) { return btMatrix3x3(); };
	btMatrix3x3 * 			m2(btMatrix3x3 * in) { return NULL; };
	const btMatrix3x3 & 	m3(btMatrix3x3 & in) { return btMatrix3x3(); }
	
	virtual void voido(void) = 0;
	
	virtual btVector3 getVector(btVector3 in) = 0;
	virtual btVector3 * getVectorPointer(btVector3 * in) = 0;
	virtual btVector3 & getVectorReference(btVector3 & in) = 0;

	virtual btQuaternion getQuaternion(void) = 0;
	virtual btQuaternion * getQuaternionPointer(void) = 0;
	virtual btQuaternion & getQuaternionReference(void) = 0;
	
	virtual btMatrix3x3 getMatrix(void) = 0;
	virtual btMatrix3x3 * getMatrixPointer(void) = 0;
	virtual btMatrix3x3 & getMatrixReference(void) = 0;
};

%}
