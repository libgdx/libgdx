/* -*- C -*-
 * File: libraw_bytebuffer.h
   Copyright 2008-2013 LibRaw LLC (info@libraw.org)

 *
 * Created: Fri Aug 12 14:41:45 2011


LibRaw is free software; you can redistribute it and/or modify
it under the terms of the one of three licenses as you choose:

1. GNU LESSER GENERAL PUBLIC LICENSE version 2.1
   (See file LICENSE.LGPL provided in LibRaw distribution archive for details).

2. COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (CDDL) Version 1.0
   (See file LICENSE.CDDL provided in LibRaw distribution archive for details).

3. LibRaw Software License 27032010
   (See file LICENSE.LibRaw.pdf provided in LibRaw distribution archive for details).

 */
class LibRaw_byte_buffer
{
  public:
    LibRaw_byte_buffer(unsigned sz=0);
    void set_buffer(void *bb, unsigned int sz);
    virtual ~LibRaw_byte_buffer();
    // fast inlines
    int get_byte() { if(offt>=size) return EOF; return buf[offt++];}
    void unseek2() { if(offt>=2) offt-=2;}
    void *get_buffer() { return buf; }
    int get_ljpeg_byte() {
        if(offt>=size) return 0;
        unsigned char val = buf[offt++];
        if(val!=0xFF || offt >=size || buf[offt++]==0)
            return val;
        offt -=2;
        return 0;
    }

  private:
    unsigned char *buf;
    unsigned int  size,offt, do_free;
};

class LibRaw_bit_buffer
{
    unsigned bitbuf;
    int vbits, rst;
  public:
    LibRaw_bit_buffer() : bitbuf(0),vbits(0),rst(0) {}

        void reset() {  bitbuf=vbits=rst=0;}
#ifndef LIBRAW_LIBRARY_BUILD
        void fill_lj(LibRaw_byte_buffer* buf,int nbits);
        unsigned _getbits_lj(LibRaw_byte_buffer* buf, int nbits);
        unsigned _gethuff_lj(LibRaw_byte_buffer* buf, int nbits, unsigned short* huff);
        void fill(LibRaw_byte_buffer* buf,int nbits,int zer0_ff);
        unsigned _getbits(LibRaw_byte_buffer* buf, int nbits,int zer0_ff);
        unsigned _gethuff(LibRaw_byte_buffer* buf, int nbits, unsigned short* huff, int zer0_ff);
#else
        void fill_lj(LibRaw_byte_buffer* buf,int nbits)
        {
            unsigned c1,c2,c3;
            if(rst || nbits < vbits) return;
            int m = vbits >> 3;
            switch(m)
                {
                case 2:	
                    c1 = buf->get_ljpeg_byte();
                    bitbuf = (bitbuf <<8) | (c1);
                    vbits+=8;
                    break;
                case 1:
                    c1 = buf->get_ljpeg_byte();
                    c2 = buf->get_ljpeg_byte();
                    bitbuf = (bitbuf <<16) | (c1<<8) | c2;
                    vbits+=16;		
                    break;
                case 0:
                    c1 = buf->get_ljpeg_byte();
                    c2 = buf->get_ljpeg_byte();
                    c3 = buf->get_ljpeg_byte();
                    bitbuf = (bitbuf <<24) | (c1<<16) | (c2<<8)|c3;
                    vbits+=24;
                    break;
                }
        }

        unsigned _getbits_lj(LibRaw_byte_buffer* buf, int nbits)
        {
            unsigned c;
            if(nbits==0 || vbits < 0) return 0;
            fill_lj(buf,nbits);
            c = bitbuf << (32-vbits) >> (32-nbits);
            vbits-=nbits;
            if(vbits<0)throw LIBRAW_EXCEPTION_IO_EOF;
            return c;
        }
        unsigned _gethuff_lj(LibRaw_byte_buffer* buf, int nbits, unsigned short* huff)
        {
            unsigned c;
            if(nbits==0 || vbits < 0) return 0;
            fill_lj(buf,nbits);
            c = bitbuf << (32-vbits) >> (32-nbits);
            vbits -= huff[c] >> 8;
            c = (uchar) huff[c];
            if(vbits<0)throw LIBRAW_EXCEPTION_IO_EOF;
            return c;
        }
        void fill(LibRaw_byte_buffer* buf,int nbits,int zer0_ff)
        {
            unsigned c;
            while (!rst && vbits < nbits && (c = buf->get_byte()) != EOF &&
                   !(rst = zer0_ff && c == 0xff && buf->get_byte())) {
                bitbuf = (bitbuf << 8) + (uchar) c;
                vbits += 8;
            }
        }
        unsigned _getbits(LibRaw_byte_buffer* buf, int nbits,int zer0_ff)
        {
            unsigned c;
            if(nbits==0 || vbits < 0) return 0;
            fill(buf,nbits,zer0_ff);
            c = bitbuf << (32-vbits) >> (32-nbits);
            vbits-=nbits;
            if(vbits<0)throw LIBRAW_EXCEPTION_IO_EOF;
            return c;
        }
        unsigned _gethuff(LibRaw_byte_buffer* buf, int nbits, unsigned short* huff, int zer0_ff)
        {
            unsigned c;
            if(nbits==0 || vbits < 0) return 0;
            fill(buf,nbits,zer0_ff);
            c = bitbuf << (32-vbits) >> (32-nbits);
            vbits -= huff[c] >> 8;
            c = (uchar) huff[c];
            if(vbits<0)throw LIBRAW_EXCEPTION_IO_EOF;
            return c;
        }
#endif
};
