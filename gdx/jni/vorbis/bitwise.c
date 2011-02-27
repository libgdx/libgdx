/********************************************************************
 *                                                                  *
 * THIS FILE IS PART OF THE OggVorbis 'TREMOR' CODEC SOURCE CODE.   *
 *                                                                  *
 * USE, DISTRIBUTION AND REPRODUCTION OF THIS LIBRARY SOURCE IS     *
 * GOVERNED BY A BSD-STYLE SOURCE LICENSE INCLUDED WITH THIS SOURCE *
 * IN 'COPYING'. PLEASE READ THESE TERMS BEFORE DISTRIBUTING.       *
 *                                                                  *
 * THE OggVorbis 'TREMOR' SOURCE CODE IS (C) COPYRIGHT 1994-2002    *
 * BY THE Xiph.Org FOUNDATION http://www.xiph.org/                  *
 *                                                                  *
 ********************************************************************

  function: packing variable sized words into an octet stream

 ********************************************************************/

/* We're 'LSb' endian; if we write a word but read individual bits,
   then we'll read the lsb first */

#include <string.h>
#include <stdlib.h>
#include "ogg.h"

static unsigned long mask[]=
{0x00000000,0x00000001,0x00000003,0x00000007,0x0000000f,
 0x0000001f,0x0000003f,0x0000007f,0x000000ff,0x000001ff,
 0x000003ff,0x000007ff,0x00000fff,0x00001fff,0x00003fff,
 0x00007fff,0x0000ffff,0x0001ffff,0x0003ffff,0x0007ffff,
 0x000fffff,0x001fffff,0x003fffff,0x007fffff,0x00ffffff,
 0x01ffffff,0x03ffffff,0x07ffffff,0x0fffffff,0x1fffffff,
 0x3fffffff,0x7fffffff,0xffffffff };

/* mark read process as having run off the end */
static void _adv_halt(oggpack_buffer *b){
  b->headptr=b->head->buffer->data+b->head->begin+b->head->length;
  b->headend=-1;
  b->headbit=0;
}

/* spans forward, skipping as many bytes as headend is negative; if
   headend is zero, simply finds next byte.  If we're up to the end
   of the buffer, leaves headend at zero.  If we've read past the end,
   halt the decode process. */
static void _span(oggpack_buffer *b){
  while(b->headend<1){
    if(b->head->next){
      b->count+=b->head->length;
      b->head=b->head->next;
      b->headptr=b->head->buffer->data+b->head->begin-b->headend; 
      b->headend+=b->head->length;      
    }else{
      /* we've either met the end of decode, or gone past it. halt
         only if we're past */
      if(b->headend<0 || b->headbit)
        /* read has fallen off the end */
        _adv_halt(b);

      break;
    }
  }
}

void oggpack_readinit(oggpack_buffer *b,ogg_reference *r){
  memset(b,0,sizeof(*b));

  b->tail=b->head=r;
  b->count=0;
  b->headptr=b->head->buffer->data+b->head->begin;
  b->headend=b->head->length;
  _span(b);
}

#define _lookspan()   while(!end){\
                        head=head->next;\
                        if(!head) return -1;\
                        ptr=head->buffer->data + head->begin;\
                        end=head->length;\
                      }

/* Read in bits without advancing the bitptr; bits <= 32 */
long oggpack_look(oggpack_buffer *b,int bits){
  unsigned long m=mask[bits];
  unsigned long ret=-1;

  bits+=b->headbit;

  if(bits >= b->headend<<3){
    int            end=b->headend;
    unsigned char *ptr=b->headptr;
    ogg_reference *head=b->head;

    if(end<0)return -1;
    
    if(bits){
      _lookspan();
      ret=*ptr++>>b->headbit;
      if(bits>8){
        --end;
        _lookspan();
        ret|=*ptr++<<(8-b->headbit);  
        if(bits>16){
          --end;
          _lookspan();
          ret|=*ptr++<<(16-b->headbit);  
          if(bits>24){
            --end;
            _lookspan();
            ret|=*ptr++<<(24-b->headbit);  
            if(bits>32 && b->headbit){
              --end;
              _lookspan();
              ret|=*ptr<<(32-b->headbit);
            }
          }
        }
      }
    }

  }else{

    /* make this a switch jump-table */
    ret=b->headptr[0]>>b->headbit;
    if(bits>8){
      ret|=b->headptr[1]<<(8-b->headbit);  
      if(bits>16){
        ret|=b->headptr[2]<<(16-b->headbit);  
        if(bits>24){
          ret|=b->headptr[3]<<(24-b->headbit);  
          if(bits>32 && b->headbit)
            ret|=b->headptr[4]<<(32-b->headbit);
        }
      }
    }
  }

  ret&=m;
  return ret;
}

/* limited to 32 at a time */
void oggpack_adv(oggpack_buffer *b,int bits){
  bits+=b->headbit;
  b->headbit=bits&7;
  b->headptr+=bits/8;
  if((b->headend-=bits/8)<1)_span(b);
}

/* spans forward and finds next byte.  Never halts */
static void _span_one(oggpack_buffer *b){
  while(b->headend<1){
    if(b->head->next){
      b->count+=b->head->length;
      b->head=b->head->next;
      b->headptr=b->head->buffer->data+b->head->begin; 
      b->headend=b->head->length;      
    }else
      break;
  }
}

static int _halt_one(oggpack_buffer *b){
  if(b->headend<1){
    _adv_halt(b);
    return -1;
  }
  return 0;
}

int oggpack_eop(oggpack_buffer *b){
  if(b->headend<0)return -1;
  return 0;
}

/* bits <= 32 */
long oggpack_read(oggpack_buffer *b,int bits){
  unsigned long m=mask[bits];
  ogg_uint32_t ret=-1;

  bits+=b->headbit;

  if(bits >= b->headend<<3){

    if(b->headend<0)return -1;
    
    if(bits){
      if (_halt_one(b)) return -1;
      ret=*b->headptr>>b->headbit;
      
      if(bits>=8){
        ++b->headptr;
        --b->headend;
        _span_one(b);
        if(bits>8){
          if (_halt_one(b)) return -1;
          ret|=*b->headptr<<(8-b->headbit);   
          
          if(bits>=16){
            ++b->headptr;
            --b->headend;
            _span_one(b);
            if(bits>16){
              if (_halt_one(b)) return -1;
              ret|=*b->headptr<<(16-b->headbit);  
              
              if(bits>=24){
                ++b->headptr;
                --b->headend;
                _span_one(b);
                if(bits>24){
                  if (_halt_one(b)) return -1;
                  ret|=*b->headptr<<(24-b->headbit);
                  
                  if(bits>=32){
                    ++b->headptr;
                    --b->headend;
                    _span_one(b);
                    if(bits>32){
                      if (_halt_one(b)) return -1;
                      if(b->headbit)ret|=*b->headptr<<(32-b->headbit);
                      
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }else{
  
    ret=b->headptr[0]>>b->headbit;
    if(bits>8){
      ret|=b->headptr[1]<<(8-b->headbit);  
      if(bits>16){
        ret|=b->headptr[2]<<(16-b->headbit);  
        if(bits>24){
          ret|=b->headptr[3]<<(24-b->headbit);  
          if(bits>32 && b->headbit){
            ret|=b->headptr[4]<<(32-b->headbit);
          }
        }
      }
    }
    
    b->headptr+=bits/8;
    b->headend-=bits/8;
  }

  ret&=m;
  b->headbit=bits&7;   
  return ret;
}

long oggpack_bytes(oggpack_buffer *b){
  return(b->count+b->headptr-b->head->buffer->data-b->head->begin+
         (b->headbit+7)/8);
}

long oggpack_bits(oggpack_buffer *b){
  return((b->count+b->headptr-b->head->buffer->data-b->head->begin)*8+
         b->headbit);
}

