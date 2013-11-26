/*
 * An includable interface file that (re)enables some type mappings that most
 * Bullet types want, but might be disabled for some because it's more trouble
 * than it's worth to map them right.
 */

/* Use Java float[] where Bullet wants btScalar *. */
%apply float[] { btScalar * };
%apply float[] { const btScalar * }; 
