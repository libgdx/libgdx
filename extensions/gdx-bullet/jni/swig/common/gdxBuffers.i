%define ENABLE_NIO_BUFFER_TYPEMAP(CTYPE, BUFFERTYPE)
%typemap(jni) CTYPE* "jobject"
%typemap(jtype) CTYPE* "BUFFERTYPE"
%typemap(jstype) CTYPE* "BUFFERTYPE"
%typemap(javain, 
	pre="    assert $javainput.isDirect() : \"Buffer must be allocated direct.\";") CTYPE* "$javainput"
%typemap(javaout) CTYPE* {
    return $jnicall;
}
%typemap(in) CTYPE* {
  $1 = (CTYPE*)jenv->GetDirectBufferAddress($input);
  if ($1 == NULL) {
    SWIG_JavaThrowException(jenv, SWIG_JavaRuntimeException, "Unable to get address of direct buffer. Buffer must be allocated direct.");
  }
}
%typemap(memberin) CTYPE* {
  if ($input) {
    $1 = $input;
  } else {
    $1 = 0;
  }
}
%typemap(freearg) CTYPE* ""
%enddef

%define DISABLE_NIO_BUFFER_TYPEMAP(CTYPE)
%clear CTYPE*
%enddef