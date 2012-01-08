/*
	decode_MMX.s: MMX optimized synth

	copyright ?-2006 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by the mysterious higway (apparently)

 Thomas' words about a note:
 Initially, I found the note "this code comes under GPL" in this file.
 After asking Michael about legal status of the MMX files, he said that he got them without any comment and thus I believe that the GPL comment was made by Michael, since he made mpg123 GPL at some time - and marked some files that way, but not all.
 Based on that thought, I now consider this file along with the other parts of higway's MMX optimization to be licensed under LGPL 2.1 by Michael's decision.
*/

#include "mangle.h"

.text

.globl ASM_NAME(synth_1to1_MMX)
/* int synth_1to1_MMX(real *bandPtr, int channel, short *out, short *buffs, int *bo, float *decwins); */
ASM_NAME(synth_1to1_MMX):
        pushl %ebp
        pushl %edi
        pushl %esi
        pushl %ebx
/* stack: 0=ebx, 4=esi, 8=edi, 12=ebp, 16=back, 20=bandPtr, 24=channel, 28=out, 32=buffs, 36=bo, 40=decwins */
        movl 24(%esp),%ecx
        movl 28(%esp),%edi
        movl $15,%ebx
        movl 36(%esp),%edx
        leal (%edi,%ecx,2),%edi
	decl %ecx
        movl 32(%esp),%esi
        movl (%edx),%eax
        jecxz .L1
        decl %eax
        andl %ebx,%eax
        leal 1088(%esi),%esi
        movl %eax,(%edx)
.L1:
        leal (%esi,%eax,2),%edx
        movl %eax,%ebp
        incl %eax
        pushl 20(%esp)
        andl %ebx,%eax
        leal 544(%esi,%eax,2),%ecx
        incl %ebx
	testl $1, %eax
	jnz .L2
        xchgl %edx,%ecx
	incl %ebp
        leal 544(%esi),%esi
.L2:
        pushl %edx
        pushl %ecx
        call ASM_NAME(dct64_MMX)
        addl $12,%esp
/* stack like before, pushed 3, incremented again */
	leal 1(%ebx), %ecx
        subl %ebp,%ebx
	pushl %eax
	movl 44(%esp),%eax /* decwins */
	leal (%eax,%ebx,2), %edx
	popl %eax
.L3:
        movq  (%edx),%mm0
        pmaddwd (%esi),%mm0
        movq  8(%edx),%mm1
        pmaddwd 8(%esi),%mm1
        movq  16(%edx),%mm2
        pmaddwd 16(%esi),%mm2
        movq  24(%edx),%mm3
        pmaddwd 24(%esi),%mm3
        paddd %mm1,%mm0
        paddd %mm2,%mm0
        paddd %mm3,%mm0
        movq  %mm0,%mm1
        psrlq $32,%mm1
        paddd %mm1,%mm0
        psrad $13,%mm0
        packssdw %mm0,%mm0
        movd %mm0,%eax
	movw %ax, (%edi)

        leal 32(%esi),%esi
        leal 64(%edx),%edx
        leal 4(%edi),%edi
        loop .L3


        subl $64,%esi
        movl $15,%ecx
.L4:
        movq  (%edx),%mm0
        pmaddwd (%esi),%mm0
        movq  8(%edx),%mm1
        pmaddwd 8(%esi),%mm1
        movq  16(%edx),%mm2
        pmaddwd 16(%esi),%mm2
        movq  24(%edx),%mm3
        pmaddwd 24(%esi),%mm3
        paddd %mm1,%mm0
        paddd %mm2,%mm0
        paddd %mm3,%mm0
        movq  %mm0,%mm1
        psrlq $32,%mm1
        paddd %mm0,%mm1
        psrad $13,%mm1
        packssdw %mm1,%mm1
        psubd %mm0,%mm0
        psubsw %mm1,%mm0
        movd %mm0,%eax
	movw %ax,(%edi)

        subl $32,%esi
        addl $64,%edx
        leal 4(%edi),%edi
        loop .L4
	emms
        popl %ebx
        popl %esi
        popl %edi
        popl %ebp
        ret

NONEXEC_STACK
