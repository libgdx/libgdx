%define JAVA_CRITICAL_ARRAYS_TYPEMAPS(CTYPE, JTYPE, JNITYPE, JFUNCNAME, JNIDESC)

%typemap(jni) CTYPE[ANY], CTYPE[]               %{JNITYPE##Array%}
%typemap(jtype) CTYPE[ANY], CTYPE[]             %{JTYPE[]%}
%typemap(jstype) CTYPE[ANY], CTYPE[]            %{JTYPE[]%}

%typemap(in) CTYPE[]
%{ $1 = (CTYPE *)jenv->GetPrimitiveArrayCritical($input, 0); %}
%typemap(in) CTYPE[ANY]
%{  if ($input && JCALL1(GetArrayLength, jenv, $input) != $1_size) {
    SWIG_JavaThrowException(jenv, SWIG_JavaIndexOutOfBoundsException, "incorrect array size");
    return $null;
  }
  $1 = (CTYPE *)jenv->GetPrimitiveArrayCritical($input, 0); %}
%typemap(argout) CTYPE[ANY], CTYPE[]; 
%{ /*SWIG_JavaArrayArgout##JFUNCNAME(jenv, jarr$argnum, (CTYPE *)$1, $input);*/ %}
%typemap(out) CTYPE[ANY]
%{ /*$result = SWIG_JavaArrayOut##JFUNCNAME(jenv, (CTYPE *)$1, $1_dim0);*/ %}
%typemap(out) CTYPE[] 
%{ /*$result = SWIG_JavaArrayOut##JFUNCNAME(jenv, (CTYPE *)$1, FillMeInAsSizeCannotBeDeterminedAutomatically);*/ %}
%typemap(freearg) CTYPE[ANY], CTYPE[]
%{ jenv->ReleasePrimitiveArrayCritical($input, (CTYPE *)$1, 0); %}

%typemap(javain) CTYPE[ANY], CTYPE[] "$javainput"
%typemap(javaout) CTYPE[ANY], CTYPE[] {
    return $jnicall;
}

%typemap(memberin) CTYPE[ANY], CTYPE[];
%typemap(globalin) CTYPE[ANY], CTYPE[];
%enddef