%feature("director") voidPointerTest;

%inline %{

class voidPointerTest {
public:
	voidPointerTest() { };
	virtual ~voidPointerTest() { };
	
	void 	setSomething(void *) { };
	void * 	getSomething() { return NULL; };
	void *	setAndGetSomething(void *) { return NULL; };
	
	virtual void 	v_setSomething(void *) = 0;
	virtual void * 	v_getSomething() = 0;
	virtual void *	v_setAndGetSomething(void *) = 0;
};

%}
