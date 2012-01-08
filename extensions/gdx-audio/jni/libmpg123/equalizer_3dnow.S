/*
	equalizer_3dnow: 3DNow! optimized do_equalizer()

	copyright ?-2006 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by KIMURA Takuhiro
*/

#include "mangle.h"

.text
	ALIGN4
.globl ASM_NAME(do_equalizer_3dnow)
/*	.type	 ASM_NAME(do_equalizer_3dnow),@function */
/* void do_equalizer(real *bandPtr,int channel, real equalizer[2][32]); */
ASM_NAME(do_equalizer_3dnow):
	pushl %esi
	pushl %ebx
	/* bandPtr */
	movl 12(%esp),%ebx
	/* channel */
	movl 16(%esp),%ecx
	xorl %edx,%edx
	/* equalizer */
	movl 20(%esp),%esi
	sall $7,%ecx
	ALIGN4
.L9:
	movq (%ebx,%edx),%mm0
	pfmul (%esi,%ecx),%mm0

	movq 8(%ebx,%edx),%mm1
	pfmul 8(%esi,%ecx),%mm1
	movq %mm0,(%ebx,%edx)
	
	movq 16(%ebx,%edx),%mm0
	pfmul 16(%esi,%ecx),%mm0
	movq %mm1,8(%ebx,%edx)
	
	movq 24(%ebx,%edx),%mm1
	pfmul 24(%esi,%ecx),%mm1
	movq %mm0,16(%ebx,%edx)

	movq 32(%ebx,%edx),%mm0
	pfmul 32(%esi,%ecx),%mm0
	movq %mm1,24(%ebx,%edx)

	movq 40(%ebx,%edx),%mm1
	pfmul 40(%esi,%ecx),%mm1
	movq %mm0,32(%ebx,%edx)
	
	movq 48(%ebx,%edx),%mm0
	pfmul 48(%esi,%ecx),%mm0
	movq %mm1,40(%ebx,%edx)
	
	movq 56(%ebx,%edx),%mm1
	pfmul 56(%esi,%ecx),%mm1
	movq %mm0,48(%ebx,%edx)
	movq %mm1,56(%ebx,%edx)
	
	addl $64,%edx
	addl $32,%ecx
	cmpl $124,%edx
	jle .L9
	ALIGN4
	popl %ebx
	popl %esi
	ret

NONEXEC_STACK
