/* -*- C++ -*-
 * File: libraw_alloc.h
 * Copyright 2008-2013 LibRaw LLC (info@libraw.org)
 * Created: Sat Mar  22, 2008 
 *
 * LibRaw C++ interface
 *
LibRaw is free software; you can redistribute it and/or modify
it under the terms of the one of three licenses as you choose:

1. GNU LESSER GENERAL PUBLIC LICENSE version 2.1
   (See file LICENSE.LGPL provided in LibRaw distribution archive for details).

2. COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (CDDL) Version 1.0
   (See file LICENSE.CDDL provided in LibRaw distribution archive for details).

3. LibRaw Software License 27032010
   (See file LICENSE.LibRaw.pdf provided in LibRaw distribution archive for details).

 */

#ifndef __LIBRAW_ALLOC_H
#define __LIBRAW_ALLOC_H

#include <stdlib.h>
#include <string.h>

#ifdef __cplusplus

#define LIBRAW_MSIZE 32

class DllDef libraw_memmgr
{
  public:
    libraw_memmgr()
        {
            memset(mems,0,sizeof(mems));
            calloc_cnt=0;
        }
    void *malloc(size_t sz)
        {
            void *ptr = ::malloc(sz);
            mem_ptr(ptr);
            return ptr;
        }
    void *calloc(size_t n, size_t sz)
        {
            void *ptr =  ::calloc(n,sz);
            mem_ptr(ptr);
            return ptr;
        }
    void *realloc(void *ptr,size_t newsz)
        {
            void *ret = ::realloc(ptr,newsz);
            forget_ptr(ptr);
            mem_ptr(ret);
            return ret;
        }
    void  free(void *ptr)
    {
        forget_ptr(ptr);
        ::free(ptr);
    }
    void cleanup(void)
    {
        for(int i = 0; i< LIBRAW_MSIZE; i++)
            if(mems[i])
                {
                    free(mems[i]);
                    mems[i] = NULL;
                }
    }

  private:
    void *mems[LIBRAW_MSIZE];
    int calloc_cnt;
    void mem_ptr(void *ptr)
    {
        if(ptr)
            for(int i=0;i < LIBRAW_MSIZE; i++)
                if(!mems[i])
                    {
                        mems[i] = ptr;
                        break;
                    }
    }
    void forget_ptr(void *ptr)
    {
        if(ptr)
            for(int i=0;i < LIBRAW_MSIZE; i++)
                if(mems[i] == ptr)
                    mems[i] = NULL;
    }

};

#endif /* C++ */

#endif
