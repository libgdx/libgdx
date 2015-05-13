#ifndef OPJ_CONFIG_PRIVATE_H
#define OPJ_CONFIG_PRIVATE_H

#define OPJ_PACKAGE_VERSION "2.0.0"

/**
Some versions of gcc may have BYTE_ORDER or __BYTE_ORDER defined
If your big endian system isn't being detected, add an OS specific check
*/
#if (defined(BYTE_ORDER) && BYTE_ORDER==BIG_ENDIAN) || \
	(defined(__BYTE_ORDER) && __BYTE_ORDER==__BIG_ENDIAN) || \
	defined(__BIG_ENDIAN__)
#define OPJ_BIG_ENDIAN
#endif /* BYTE_ORDER */

#endif /* OPJ_CONFIG_PRIVATE_H */
