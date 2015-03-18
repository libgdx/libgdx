/*
 * An includable interface file that disables the things that were first
 * enabled in gdxEnableArrays.i.  Lets us turn off the mapping for some
 * types.
 */

%clear btScalar *;
%clear const btScalar *;