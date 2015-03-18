
AC_DEFUN([OIS_USE_STLPORT],
[AC_ARG_WITH(stlport, 
             AC_HELP_STRING([--with-stlport=PATH],
                           [the path to STLPort.]),
             ac_cv_use_stlport=$withval,
             ac_cv_use_stlport=no)
 AC_CACHE_CHECK([whether to use STLPort], ac_cv_use_stlport,
                ac_cv_use_stlport=no)
 if test x$ac_cv_use_stlport != xno; then
     STLPORT_CFLAGS="-I$ac_cv_use_stlport/stlport"
     STLPORT_LIBS="-L$ac_cv_use_stlport/lib -lstlport"
 fi
 AC_SUBST(STLPORT_CFLAGS)
 AC_SUBST(STLPORT_LIBS)
])
