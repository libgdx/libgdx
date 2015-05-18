/* Library for accessing X3F Files 
----------------------------------------------------------------
BSD-style License
----------------------------------------------------------------

* Copyright (c) 2010, Roland Karlsson (roland@proxel.se)
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*     * Redistributions of source code must retain the above copyright
*       notice, this list of conditions and the following disclaimer.
*     * Redistributions in binary form must reproduce the above copyright
*       notice, this list of conditions and the following disclaimer in the
*       documentation and/or other materials provided with the distribution.
*     * Neither the name of the organization nor the
*       names of its contributors may be used to endorse or promote products
*       derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY ROLAND KARLSSON ''AS IS'' AND ANY
* EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL ROLAND KARLSSON BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

  /* From X3F_IO.H */
#if defined(_WIN32)
	#if defined _MSC_VER
		typedef   signed __int8   int8_t;
		typedef unsigned __int8   uint8_t;
		typedef   signed __int16  int16_t;
		typedef unsigned __int16  uint16_t;
		typedef   signed __int32  int32_t;
		typedef unsigned __int32  uint32_t;
		typedef   signed __int64  int64_t;
		typedef unsigned __int64  uint64_t;
	#else
		#include <stdint.h>
	#endif // _WIN32
	#include <sys/types.h>
#else
#include <inttypes.h>
#endif

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <math.h>
#include <stdio.h>
#include "../libraw/libraw_datastream.h"

#define SIZE_UNIQUE_IDENTIFIER 16
#define SIZE_WHITE_BALANCE 32
#define NUM_EXT_DATA 32

#define X3F_VERSION(MAJ,MIN) (uint32_t)(((MAJ)<<16) + MIN)
#define X3F_VERSION_2_0 X3F_VERSION(2,0)
#define X3F_VERSION_2_1 X3F_VERSION(2,1)

/* Main file identifier */
#define X3F_FOVb (uint32_t)(0x62564f46)
/* Directory identifier */
#define X3F_SECd (uint32_t)(0x64434553)
/* Property section identifiers */
#define X3F_PROP (uint32_t)(0x504f5250)
#define X3F_SECp (uint32_t)(0x70434553)
/* Image section identifiers */
#define X3F_IMAG (uint32_t)(0x46414d49)
#define X3F_IMA2 (uint32_t)(0x32414d49)
#define X3F_SECi (uint32_t)(0x69434553)
/* CAMF identifiers */
#define X3F_CAMF (uint32_t)(0x464d4143)
#define X3F_SECc (uint32_t)(0x63434553)
/* CAMF entry identifiers */
#define X3F_CMbP (uint32_t)(0x50624d43)
#define X3F_CMbT (uint32_t)(0x54624d43)
#define X3F_CMbM (uint32_t)(0x4d624d43)
#define X3F_CMb  (uint32_t)(0x00624d43)

/* TODO: bad name */
#define X3F_IMAGE_RAW_TRUE_SD1      (uint32_t)(0x0001001e)

/* TODO: bad name */
#define X3F_IMAGE_RAW_HUFFMAN_X530  (uint32_t)(0x00030005)

#define X3F_IMAGE_RAW_TRUE          (uint32_t)(0x0003001e)
#define X3F_IMAGE_RAW_HUFFMAN_10BIT (uint32_t)(0x00030006)
#define X3F_IMAGE_THUMB_PLAIN       (uint32_t)(0x00020003)
#define X3F_IMAGE_THUMB_HUFFMAN     (uint32_t)(0x0002000b)
#define X3F_IMAGE_THUMB_JPEG        (uint32_t)(0x00020012)

#define X3F_IMAGE_HEADER_SIZE 28
#define X3F_CAMF_HEADER_SIZE 28
#define X3F_PROPERTY_LIST_HEADER_SIZE 24

typedef uint16_t utf16_t;

typedef int bool_t;

typedef enum x3f_extended_types_e {
  X3F_EXT_TYPE_NONE=0,
  X3F_EXT_TYPE_EXPOSURE_ADJUST=1,
  X3F_EXT_TYPE_CONTRAST_ADJUST=2,
  X3F_EXT_TYPE_SHADOW_ADJUST=3,
  X3F_EXT_TYPE_HIGHLIGHT_ADJUST=4,
  X3F_EXT_TYPE_SATURATION_ADJUST=5,
  X3F_EXT_TYPE_SHARPNESS_ADJUST=6,
  X3F_EXT_TYPE_RED_ADJUST=7,
  X3F_EXT_TYPE_GREEN_ADJUST=8,
  X3F_EXT_TYPE_BLUE_ADJUST=9,
  X3F_EXT_TYPE_FILL_LIGHT_ADJUST=10
} x3f_extended_types_t;

typedef struct x3f_property_s {
  /* Read from file */
  uint32_t name_offset;
  uint32_t value_offset;

  /* Computed */
  utf16_t *name;		/* 0x00 terminated UTF 16 */
  utf16_t *value;               /* 0x00 terminated UTF 16 */
} x3f_property_t;

typedef struct x3f_property_table_s {
  uint32_t size;
  x3f_property_t *element;
} x3f_property_table_t;

typedef struct x3f_property_list_s {
  /* 2.0 Fields */
  uint32_t num_properties;
  uint32_t character_format;
  uint32_t reserved;
  uint32_t total_length;

  x3f_property_table_t property_table;

  unsigned char *data;

  uint32_t data_size;

} x3f_property_list_t;

typedef struct x3f_table8_s {
  uint32_t size;
  uint8_t *element;
} x3f_table8_t;

typedef struct x3f_table16_s {
  uint32_t size;
  uint16_t *element;
} x3f_table16_t;

typedef struct x3f_table32_s {
  uint32_t size;
  uint32_t *element;
} x3f_table32_t;

#define UNDEFINED_LEAF 0xffffffff

typedef struct x3f_huffnode_s {
  struct x3f_huffnode_s *branch[2];
  uint32_t leaf;
} x3f_huffnode_t;

typedef struct x3f_hufftree_s {
  uint32_t free_node_index; /* Free node index in huffman tree array */
  x3f_huffnode_t *nodes;    /* Coding tree */
} x3f_hufftree_t;

typedef struct x3f_true_huffman_element_s {
  uint8_t code_size;
  uint8_t code;
} x3f_true_huffman_element_t;

typedef struct x3f_true_huffman_s {
  uint32_t size;
  x3f_true_huffman_element_t *element;
} x3f_true_huffman_t;

/* TODO: is this a constant? */
#define TRUE_PLANES 3

typedef struct x3f_true_s {
  uint16_t seed[3];		/* Always 512,512,512 */
  uint16_t unknown;		/* Always 0 */
  x3f_true_huffman_t table;	/* Huffman table - zero
				   terminated. size is the number of
				   leaves plus 1.*/

  x3f_table32_t plane_size;	/* Size of the 3 planes */
  uint8_t *plane_address[TRUE_PLANES]; /* computed offset to the planes */
  x3f_hufftree_t tree;		/* Coding tree */
  x3f_table16_t x3rgb16;        /* 3x16 bit X3-RGB data */
} x3f_true_t;

typedef struct x3f_huffman_s {
  x3f_table16_t mapping;   /* Value Mapping = X3F lossy compression */
  x3f_table32_t table;          /* Coding Table */
  x3f_hufftree_t tree;		/* Coding tree */
  x3f_table32_t row_offsets;    /* Row offsets */
  x3f_table8_t rgb8;            /* 3x8 bit RGB data */
  x3f_table16_t x3rgb16;        /* 3x16 bit X3-RGB data */
} x3f_huffman_t;

typedef struct x3f_image_data_s {
  /* 2.0 Fields */
  /* ------------------------------------------------------------------ */
  /* Known combinations of type and format are:
     1-6, 2-3, 2-11, 2-18, 3-6 */
  uint32_t type;                /* 1 = RAW X3 (SD1)
                                   2 = thumbnail or maybe just RGB
                                   3 = RAW X3 */
  uint32_t format;              /* 3 = 3x8 bit pixmap
                                   6 = 3x10 bit huffman with map table
                                   11 = 3x8 bit huffman
                                   18 = JPEG */
  uint32_t type_format;         /* type<<16 + format */
  /* ------------------------------------------------------------------ */
  uint32_t columns;             /* width / row size in pixels */
  uint32_t rows;                /* height */
  uint32_t row_stride;          /* row size in bytes */

  x3f_huffman_t *huffman;       /* Huffman help data */

  x3f_true_t *tru;		/* TRUE coding help data */

  unsigned char *data;                   /* Take from file if NULL. Otherwise,
                                   this is the actual data bytes in
                                   the file. */
  uint32_t data_size;

} x3f_image_data_t;

typedef struct camf_entry_s {
  uint32_t id;
  uint32_t version;
  uint32_t entry_size;
  uint32_t name_offset;
  uint32_t value_offset;
  void *entry;			/* pointer into decoded data */

  /* computed values */
  uint8_t *name_address;
  void *value_address;
} camf_entry_t;

typedef struct camf_entry_table_s {
  uint32_t size;
  camf_entry_t *element;
} camf_entry_table_t;

typedef struct x3f_camf_typeN_s {
  uint32_t val0;
  uint32_t val1;
  uint32_t val2;
  uint32_t val3;
} x3f_camf_typeN_t;

typedef struct x3f_camf_type2_s {
  uint32_t reserved;
  uint32_t infotype;
  uint32_t infotype_version;
  uint32_t crypt_key;
} x3f_camf_type2_t;

typedef struct x3f_camf_type4_s {
  uint32_t reserved;
  uint32_t decode_bias;
  uint32_t block_size;
  uint32_t block_count;
} x3f_camf_type4_t;

typedef struct x3f_camf_s {

  /* Header info */
  uint32_t type;
  union {
    x3f_camf_typeN_t tN;
    x3f_camf_type2_t t2;
    x3f_camf_type4_t t4;
  };

  /* The encrypted raw data */
  unsigned char *data;
  uint32_t data_size;

  /* Help data for type 4 Huffman compression */
  x3f_true_huffman_t table;
  x3f_hufftree_t tree;
  uint8_t *decoding_start;

  /* The decrypted data */
  void *decoded_data;
  uint32_t decoded_data_size;

  /* Pointers into the decrypted data */
  camf_entry_table_t entry_table;
} x3f_camf_t;

typedef struct x3f_directory_entry_header_s {
  uint32_t identifier;        /* Should be 'SECp', 'SECi;, */
  uint32_t version;           /* 0x00020001 is version 2.1  */
  union {
    x3f_property_list_t property_list;
    x3f_image_data_t image_data;
    x3f_camf_t camf;
  } data_subsection;
} x3f_directory_entry_header_t;

typedef struct x3f_directory_entry_s {
  struct {
    uint32_t offset;
    uint32_t size;
  } input,output;

  uint32_t type;

  x3f_directory_entry_header_t header;
} x3f_directory_entry_t;

typedef struct x3f_directory_section_s {
  uint32_t identifier;          /* Should be 'SECd' */
  uint32_t version;             /* 0x00020001 is version 2.1  */

  /* 2.0 Fields */
  uint32_t num_directory_entries;
  x3f_directory_entry_t *directory_entry;
} x3f_directory_section_t;

typedef struct x3f_header_s {
  /* 2.0 Fields */
  uint32_t identifier;          /* Should be 'FOVb' */
  uint32_t version;             /* 0x00020001 means 2.1 */
  uint8_t unique_identifier[SIZE_UNIQUE_IDENTIFIER];
  uint32_t mark_bits;
  uint32_t columns;             /* Columns and rows ... */
  uint32_t rows;                /* ... before rotation */
  uint32_t rotation;            /* 0, 90, 180, 270 */

  /* Added for 2.1 and 2.2 */
  uint8_t white_balance[SIZE_WHITE_BALANCE];
  uint8_t extended_types[NUM_EXT_DATA]; /* x3f_extended_types_t */
  uint32_t extended_data[NUM_EXT_DATA];
} x3f_header_t;

typedef struct x3f_info_s {
  char *error;
  struct {
    LibRaw_abstract_datastream *file;                 /* Use if more data is needed */
  } input;
} x3f_info_t;

typedef struct x3f_s {
  x3f_info_t info;
  x3f_header_t header;
  x3f_directory_section_t directory_section;
} x3f_t;

typedef enum x3f_return_e {
  X3F_OK=0,
  X3F_ARGUMENT_ERROR=1,
  X3F_INFILE_ERROR=2,
  X3F_OUTFILE_ERROR=3,
  X3F_INTERNAL_ERROR=4
} x3f_return_t;

extern x3f_t *x3f_new_from_file(LibRaw_abstract_datastream *infile);

extern x3f_return_t x3f_delete(x3f_t *x3f);

extern x3f_directory_entry_t *x3f_get_raw(x3f_t *x3f);

extern x3f_directory_entry_t *x3f_get_thumb_plain(x3f_t *x3f);

extern x3f_directory_entry_t *x3f_get_thumb_huffman(x3f_t *x3f);

extern x3f_directory_entry_t *x3f_get_thumb_jpeg(x3f_t *x3f);

extern x3f_directory_entry_t *x3f_get_camf(x3f_t *x3f);

extern x3f_directory_entry_t *x3f_get_prop(x3f_t *x3f);

extern x3f_return_t x3f_load_data(x3f_t *x3f, x3f_directory_entry_t *DE);

extern x3f_return_t x3f_load_image_block(x3f_t *x3f, x3f_directory_entry_t *DE);

extern x3f_return_t x3f_swap_images(x3f_t *x3f_template, x3f_t *x3f_images);

/* --------------------------------------------------------------------- */
/* Huffman Decode Macros                                                 */
/* --------------------------------------------------------------------- */

#define HUF_TREE_MAX_LENGTH 27
#define HUF_TREE_MAX_NODES(_leaves) ((HUF_TREE_MAX_LENGTH+1)*(_leaves))
#define HUF_TREE_GET_LENGTH(_v) (((_v)>>27)&0x1f)
#define HUF_TREE_GET_CODE(_v) ((_v)&0x07ffffff)

/* --------------------------------------------------------------------- */
/* Reading and writing - assuming little endian in the file              */
/* --------------------------------------------------------------------- */

static int x3f_get1(LibRaw_abstract_datastream *f)
{
  /* Little endian file */
  return f->get_char(); 
}

static int  x3f_sget2 (uchar *s)
{
        return s[0] | s[1] << 8;
}

static int x3f_get2(LibRaw_abstract_datastream *f)
{
    uchar str[2] = { 0xff,0xff };
    f->read (str, 1, 2);
    return x3f_sget2(str);
}

unsigned x3f_sget4 (uchar *s)
{
       return s[0] | s[1] << 8 | s[2] << 16 | s[3] << 24;
}

unsigned x3f_get4(LibRaw_abstract_datastream *f)
{
    uchar str[4] = { 0xff,0xff,0xff,0xff };
    f->read (str, 1, 4);
    return x3f_sget4(str);
}

#define FREE(P) do { free(P); (P) = NULL; } while (0)

#define PUT_GET_N(_buffer,_size,_file,_func)			\
  do								\
    {								\
      int _left = _size;					\
      while (_left != 0) {					\
	int _cur = _file->_func(_buffer,1,_left);		\
	if (_cur == 0) {					\
	  fprintf(stderr, "Failure to access file\n");		\
	  throw LIBRAW_EXCEPTION_IO_CORRUPT;			\
	}							\
	_left -= _cur;						\
      }								\
    } while(0)

#define GET1(_v) do {(_v) = x3f_get1(I->input.file);} while (0)
#define GET2(_v) do {(_v) = x3f_get2(I->input.file);} while (0)
#define GET4(_v) do {(_v) = x3f_get4(I->input.file);} while (0)
#define GETN(_v,_s) PUT_GET_N(_v,_s,I->input.file,read)

#define GET_TABLE(_TYPE,_T, _GETX, _NUM)                                \
  do {									\
    int _i;								\
    (_T).size = (_NUM);							\
    (_T).element = (_TYPE *)realloc((_T).element,			\
				   (_NUM)*sizeof((_T).element[0]));	\
    for (_i = 0; _i < (_T).size; _i++)					\
      _GETX((_T).element[_i]);						\
  } while (0)



#define GET_TRUE_HUFF_TABLE(_T)						\
  do {									\
    int _i;								\
    (_T).element = NULL;						\
    for (_i = 0; ; _i++) {						\
      (_T).size = _i + 1;						\
      (_T).element = (void *)realloc((_T).element,			\
				     (_i + 1)*sizeof((_T).element[0]));	\
      GET1((_T).element[_i].code_size);					\
      GET1((_T).element[_i].code);					\
      if ((_T).element[_i].code_size == 0) break;			\
    }									\
  } while (0)


#if 0
/* --------------------------------------------------------------------- */
/* Converting - ingeger vs memory - assuming little endian in the memory */
/* --------------------------------------------------------------------- */

static void x3f_convert2(void *to, void *from)
{
  uint8_t *f = (uint8_t *)from;
  uint16_t *t = (uint16_t *)to;

  /* Little endian memory */
  *t = (uint16_t)((*(f+0)<<0) + (*(f+1)<<8));
}

static void x3f_convert4(void *to, void *from)
{
  uint8_t *f = (uint8_t *)from;
  uint16_t *t = (uint16_t *)to;

  /* Little endian memory */
  *t = (uint32_t)((*(f+0)<<0) + (*(f+1)<<8) + (*(f+2)<<16) + (*(f+3)<<24)); 
}

#define CONV2(_v, _p) do {x3f_conv2(_p, _v);} while (0)
#define CONV4(_v, _p) do {x3f_conv4(_p, _v);} while (0)
#endif


/* --------------------------------------------------------------------- */
/* Allocating Huffman tree help data                                   */
/* --------------------------------------------------------------------- */

static void cleanup_huffman_tree(x3f_hufftree_t *HTP)
{
  free(HTP->nodes);
}

static void new_huffman_tree(x3f_hufftree_t *HTP, int bits)
{
  int leaves = 1<<bits;

  HTP->free_node_index = 0;
  HTP->nodes = (x3f_huffnode_t *)
    calloc(1, HUF_TREE_MAX_NODES(leaves)*sizeof(x3f_huffnode_t));
}

/* --------------------------------------------------------------------- */
/* Allocating TRUE engine RAW help data                                  */
/* --------------------------------------------------------------------- */

static void cleanup_true(x3f_true_t **TRUP)
{
  x3f_true_t *TRU = *TRUP;

  if (TRU == NULL) return;

  FREE(TRU->table.element);
  FREE(TRU->plane_size.element);
  cleanup_huffman_tree(&TRU->tree);
  FREE(TRU->x3rgb16.element);

  FREE(TRU);

  *TRUP = NULL;
}

static x3f_true_t *new_true(x3f_true_t **TRUP)
{
  x3f_true_t *TRU = (x3f_true_t *)calloc(1, sizeof(x3f_true_t));

  cleanup_true(TRUP);

  TRU->table.size = 0;
  TRU->table.element = NULL;
  TRU->plane_size.size = 0;
  TRU->plane_size.element = NULL;
  TRU->tree.nodes = NULL;
  TRU->x3rgb16.size = 0;
  TRU->x3rgb16.element = NULL;

  *TRUP = TRU;

  return TRU;
}

/* --------------------------------------------------------------------- */
/* Allocating Huffman engine help data                                   */
/* --------------------------------------------------------------------- */

static void cleanup_huffman(x3f_huffman_t **HUFP)
{
  x3f_huffman_t *HUF = *HUFP;

  if (HUF == NULL) return;

  FREE(HUF->mapping.element);
  FREE(HUF->table.element);
  cleanup_huffman_tree(&HUF->tree);
  FREE(HUF->row_offsets.element);
  FREE(HUF->rgb8.element);
  FREE(HUF->x3rgb16.element);
  FREE(HUF);

  *HUFP = NULL;
}

static x3f_huffman_t *new_huffman(x3f_huffman_t **HUFP)
{
  x3f_huffman_t *HUF = (x3f_huffman_t *)calloc(1, sizeof(x3f_huffman_t));

  cleanup_huffman(HUFP);

  /* Set all not read data block pointers to NULL */
  HUF->mapping.size = 0;
  HUF->mapping.element = NULL;
  HUF->table.size = 0;
  HUF->table.element = NULL;
  HUF->tree.nodes = NULL;
  HUF->row_offsets.size = 0;
  HUF->row_offsets.element = NULL;
  HUF->rgb8.size = 0;
  HUF->rgb8.element = NULL;
  HUF->x3rgb16.size = 0;
  HUF->x3rgb16.element = NULL;

  *HUFP = HUF;

  return HUF;
}


/* --------------------------------------------------------------------- */
/* Creating a new x3f structure from file                                */
/* --------------------------------------------------------------------- */

/* extern */ x3f_t *x3f_new_from_file(LibRaw_abstract_datastream *infile)
{
  x3f_t *x3f = (x3f_t *)calloc(1, sizeof(x3f_t));
  x3f_info_t *I = NULL;
  x3f_header_t *H = NULL;
  x3f_directory_section_t *DS = NULL;
  int i, d;

  I = &x3f->info;
  I->error = NULL;
  I->input.file = infile;

  if (infile == NULL) {
    I->error = (char*)"No infile";
    return x3f;
  }

  /* Read file header */
  H = &x3f->header;
  infile->seek(0, SEEK_SET);
  GET4(H->identifier);
  if (H->identifier != X3F_FOVb) {
#ifdef DCRAW_VERBOSE
    fprintf(stderr, "Faulty file type\n");
#endif
    x3f_delete(x3f);
    return NULL;
  }

  GET4(H->version);
  GETN(H->unique_identifier, SIZE_UNIQUE_IDENTIFIER);
  GET4(H->mark_bits);
  GET4(H->columns);
  GET4(H->rows);
  GET4(H->rotation);
  if (H->version > X3F_VERSION_2_0) {
    GETN(H->white_balance, SIZE_WHITE_BALANCE);
    GETN(H->extended_types, NUM_EXT_DATA);
    for (i=0; i<NUM_EXT_DATA; i++)
      GET4(H->extended_data[i]);
  }

  /* Go to the beginning of the directory */
  infile->seek(-4, SEEK_END);
  infile->seek(x3f_get4(infile), SEEK_SET);

  /* Read the directory header */
  DS = &x3f->directory_section;
  GET4(DS->identifier);
  GET4(DS->version);
  GET4(DS->num_directory_entries);

  if (DS->num_directory_entries > 0) {
    size_t size = DS->num_directory_entries * sizeof(x3f_directory_entry_t);
    DS->directory_entry = (x3f_directory_entry_t *)calloc(1, size);
  }

  /* Traverse the directory */
  for (d=0; d<DS->num_directory_entries; d++) { 
    x3f_directory_entry_t *DE = &DS->directory_entry[d];
    x3f_directory_entry_header_t *DEH = &DE->header;
    uint32_t save_dir_pos;

    /* Read the directory entry info */
    GET4(DE->input.offset);
    GET4(DE->input.size);

    GET4(DE->type);

    /* Save current pos and go to the entry */
    save_dir_pos = infile->tell();
    infile->seek(DE->input.offset, SEEK_SET);

    /* Read the type independent part of the entry header */
    DEH = &DE->header;
    GET4(DEH->identifier);
    GET4(DEH->version);

    /* NOTE - the tests below could be made on DE->type instead */

    if (DEH->identifier == X3F_SECp) {
      x3f_property_list_t *PL = &DEH->data_subsection.property_list;

      /* Read the property part of the header */
      GET4(PL->num_properties);
      GET4(PL->character_format);
      GET4(PL->reserved);
      GET4(PL->total_length);

      /* Set all not read data block pointers to NULL */
      PL->data = NULL;
      PL->data_size = 0;
    }

    if (DEH->identifier == X3F_SECi) {
      x3f_image_data_t *ID = &DEH->data_subsection.image_data;

      /* Read the image part of the header */
      GET4(ID->type);
      GET4(ID->format);
      ID->type_format = (ID->type << 16) + (ID->format);
      GET4(ID->columns);
      GET4(ID->rows);
      GET4(ID->row_stride);

      /* Set all not read data block pointers to NULL */
      ID->huffman = NULL;

      ID->data = NULL;
      ID->data_size = 0;
    }

    if (DEH->identifier == X3F_SECc) {
      x3f_camf_t *CAMF = &DEH->data_subsection.camf;

      /* Read the CAMF part of the header */
      GET4(CAMF->type);
      GET4(CAMF->tN.val0);
      GET4(CAMF->tN.val1);
      GET4(CAMF->tN.val2);
      GET4(CAMF->tN.val3);

      /* Set all not read data block pointers to NULL */
      CAMF->data = NULL;
      CAMF->data_size = 0;

      /* Set all not allocated help pointers to NULL */
      CAMF->table.element = NULL;
      CAMF->table.size = 0;
      CAMF->tree.nodes = NULL;
      CAMF->decoded_data = NULL;
      CAMF->decoded_data_size = 0;
      CAMF->entry_table.element = NULL;
      CAMF->entry_table.size = 0;
    }

    /* Reset the file pointer back to the directory */
    infile->seek( save_dir_pos, SEEK_SET);
  }

  return x3f;
}


static char x3f_id_buf[5] = {0,0,0,0,0};

static char *x3f_id(uint32_t id)
{
  x3f_id_buf[0] = (id>>0) & 0xff; 
  x3f_id_buf[1] = (id>>8) & 0xff; 
  x3f_id_buf[2] = (id>>16) & 0xff; 
  x3f_id_buf[3] = (id>>24) & 0xff; 

  return x3f_id_buf;
}


static uint32_t row_offsets_size(x3f_huffman_t *HUF)
{
  return HUF->row_offsets.size * sizeof(HUF->row_offsets.element[0]);
}



/* --------------------------------------------------------------------- */
/* Clean up an x3f structure                                             */
/* --------------------------------------------------------------------- */

/* extern */ x3f_return_t x3f_delete(x3f_t *x3f)
{
  x3f_directory_section_t *DS;
  int d;

  if (x3f == NULL)
    return X3F_ARGUMENT_ERROR;

  DS = &x3f->directory_section;

  for (d=0; d<DS->num_directory_entries; d++) { 
    x3f_directory_entry_t *DE = &DS->directory_entry[d];
    x3f_directory_entry_header_t *DEH = &DE->header;

    if (DEH->identifier == X3F_SECp) {
      x3f_property_list_t *PL = &DEH->data_subsection.property_list;

      FREE(PL->property_table.element);
      FREE(PL->data);
    }

    if (DEH->identifier == X3F_SECi) {
      x3f_image_data_t *ID = &DEH->data_subsection.image_data;

      cleanup_huffman(&ID->huffman);
      cleanup_true(&ID->tru);
      FREE(ID->data);
    }

    if (DEH->identifier == X3F_SECc) {
      x3f_camf_t *CAMF = &DEH->data_subsection.camf;

      FREE(CAMF->data);
      FREE(CAMF->table.element);
      cleanup_huffman_tree(&CAMF->tree);
      FREE(CAMF->decoded_data);
      FREE(CAMF->entry_table.element);
    }
  }

  FREE(DS->directory_entry);
  FREE(x3f);
  return X3F_OK;
}


/* --------------------------------------------------------------------- */
/* Getting a reference to a directory entry                              */
/* --------------------------------------------------------------------- */

/* TODO: all those only get the first instance */

static x3f_directory_entry_t *x3f_get(x3f_t *x3f,
                                      uint32_t type,
                                      uint32_t image_type)
{
  x3f_directory_section_t *DS;
  int d;

  if (x3f == NULL) return NULL;

  DS = &x3f->directory_section;

  for (d=0; d<DS->num_directory_entries; d++) { 
    x3f_directory_entry_t *DE = &DS->directory_entry[d];
    x3f_directory_entry_header_t *DEH = &DE->header;
    
    if (DEH->identifier == type) {
      switch (DEH->identifier) {
      case X3F_SECi:
        {
          x3f_image_data_t *ID = &DEH->data_subsection.image_data;

          if (ID->type_format == image_type)
            return DE;
        }
        break;
      default:
        return DE;
      }
    }
  }

  return NULL;
}

/* extern */ x3f_directory_entry_t *x3f_get_raw(x3f_t *x3f)
{
  x3f_directory_entry_t *DE;

  if ((DE = x3f_get(x3f, X3F_SECi, X3F_IMAGE_RAW_HUFFMAN_X530)) != NULL)
    return DE;

  if ((DE = x3f_get(x3f, X3F_SECi, X3F_IMAGE_RAW_HUFFMAN_10BIT)) != NULL)
    return DE;

  if ((DE = x3f_get(x3f, X3F_SECi, X3F_IMAGE_RAW_TRUE)) != NULL)
    return DE;

  if ((DE = x3f_get(x3f, X3F_SECi, X3F_IMAGE_RAW_TRUE_SD1)) != NULL)
    return DE;

  return NULL;
}

/* extern */ x3f_directory_entry_t *x3f_get_thumb_plain(x3f_t *x3f)
{
  return x3f_get(x3f, X3F_SECi, X3F_IMAGE_THUMB_PLAIN);
}

/* extern */ x3f_directory_entry_t *x3f_get_thumb_huffman(x3f_t *x3f)
{
  return x3f_get(x3f, X3F_SECi, X3F_IMAGE_THUMB_HUFFMAN);
}

/* extern */ x3f_directory_entry_t *x3f_get_thumb_jpeg(x3f_t *x3f)
{
  return x3f_get(x3f, X3F_SECi, X3F_IMAGE_THUMB_JPEG);
}

/* extern */ x3f_directory_entry_t *x3f_get_camf(x3f_t *x3f)
{
  return x3f_get(x3f, X3F_SECc, 0);
}

/* extern */ x3f_directory_entry_t *x3f_get_prop(x3f_t *x3f)
{
  return x3f_get(x3f, X3F_SECp, 0);
}

/* For some obscure reason, the bit numbering is weird. It is
   generally some kind of "big endian" style - e.g. the bit 7 is the
   first in a byte and bit 31 first in a 4 byte int. For patterns in
   the huffman pattern table, bit 27 is the first bit and bit 26 the
   next one. */

#define PATTERN_BIT_POS(_len, _bit) ((_len) - (_bit) - 1)
#define MEMORY_BIT_POS(_bit) PATTERN_BIT_POS(8, _bit)


/* --------------------------------------------------------------------- */
/* Huffman Decode                                                        */
/* --------------------------------------------------------------------- */

/* Make the huffman tree */


static x3f_huffnode_t *new_node(x3f_hufftree_t *tree)
{
  x3f_huffnode_t *t = &tree->nodes[tree->free_node_index];

  t->branch[0] = NULL;
  t->branch[1] = NULL;
  t->leaf = UNDEFINED_LEAF;

  tree->free_node_index++;

  return t;
}

static void add_code_to_tree(x3f_hufftree_t *tree,
                             int length, uint32_t code, uint32_t value)
{
  int i;

  x3f_huffnode_t *t = tree->nodes;

  for (i=0; i<length; i++) {
    int pos = PATTERN_BIT_POS(length, i);
    int bit = (code>>pos)&1;
    x3f_huffnode_t *t_next = t->branch[bit];

    if (t_next == NULL)
      t_next = t->branch[bit] = new_node(tree);

    t = t_next;
  }

  t->leaf = value;
}

static void populate_true_huffman_tree(x3f_hufftree_t *tree,
				       x3f_true_huffman_t *table)
{
  int i;

  new_node(tree);

  for (i=0; i<table->size; i++) {
    x3f_true_huffman_element_t *element = &table->element[i];
    uint32_t length = element->code_size;
    
    if (length != 0) {
      /* add_code_to_tree wants the code right adjusted */
      uint32_t code = ((element->code) >> (8 - length)) & 0xff;
      uint32_t value = i;

      add_code_to_tree(tree, length, code, value);

    }
  }
}

static void populate_huffman_tree(x3f_hufftree_t *tree,
				  x3f_table32_t *table,
				  x3f_table16_t *mapping)
{
  int i;

  new_node(tree);

  for (i=0; i<table->size; i++) {
    uint32_t element = table->element[i];

    if (element != 0) {
      uint32_t length = HUF_TREE_GET_LENGTH(element);
      uint32_t code = HUF_TREE_GET_CODE(element); 
      uint32_t value;

      /* If we have a valid mapping table - then the value from the
         mapping table shall be used. Otherwise we use the current
         index in the table as value. */
      if (table->size == mapping->size)
        value = mapping->element[i];
      else
        value = i;

      add_code_to_tree(tree, length, code, value);

    }
  }
}

/* Help machinery for reading bits in a memory */

typedef struct bit_state_s {
  uint8_t *next_address;
  uint8_t bit_offset;
  uint8_t bits[8];
} bit_state_t;

void set_bit_state(bit_state_t *BS, uint8_t *address)
{
  BS->next_address = address;
  BS->bit_offset = 8;
}

static uint8_t get_bit(bit_state_t *BS)
{
  if (BS->bit_offset == 8) {
    uint8_t byte = *BS->next_address;
    int i;

    for (i=7; i>= 0; i--) {
      BS->bits[i] = byte&1;
      byte = byte >> 1;
    }
    BS->next_address++;
    BS->bit_offset = 0;
  }

  return BS->bits[BS->bit_offset++];
}

/* Decode use the TRUE algorithm */

static int32_t get_true_diff(bit_state_t *BS, x3f_hufftree_t *HTP)
{
  int32_t diff;
  x3f_huffnode_t *node = &HTP->nodes[0];
  uint8_t bits;

  while (node->branch[0] != NULL || node->branch[1] != NULL) {
    uint8_t bit = get_bit(BS);
    x3f_huffnode_t *new_node = node->branch[bit];

    node = new_node;
    if (node == NULL) {
#ifdef DCRAW_VERBOSE
      fprintf(stderr, "Huffman coding got unexpected bit\n");
#endif
      return 0;
    }
  }

  bits = node->leaf;

  if (bits == 0)
    diff = 0;
  else {
    uint8_t first_bit = get_bit(BS);
    int i;

    diff = first_bit;

    for (i=1; i<bits; i++)
      diff = (diff << 1) + get_bit(BS); 
    
    if (first_bit == 0)
      diff -= (1<<bits) - 1;
  }

  return diff;
}

/* This code (that decodes one of the X3F color planes, really is a
   decoding of a compression algorithm suited for Bayer CFA data. In
   Bayer CFA the data is divided into 2x2 squares that represents
   (R,G1,G2,B) data. Those four positions are (in this compression)
   treated as one data stream each, where you store the differences to
   previous data in the stream. The reason for this is, of course,
   that the date is more often than not near to the next data in a
   stream that represents the same color. */

/* TODO: write more about the compression */

static void true_decode_one_color(x3f_image_data_t *ID, int color)
{
  x3f_true_t *TRU = ID->tru;
  uint32_t seed = TRU->seed[color]; /* TODO : Is this correct ? */
  int row;

  x3f_hufftree_t *tree = &TRU->tree;
  bit_state_t BS;

  int32_t row_start_acc[2][2];
  uint32_t rows = ID->rows;
  uint32_t cols = ID->columns;

  uint16_t *dst = TRU->x3rgb16.element + color;

  set_bit_state(&BS, TRU->plane_address[color]);

  row_start_acc[0][0] = seed;
  row_start_acc[0][1] = seed;
  row_start_acc[1][0] = seed;
  row_start_acc[1][1] = seed;

  for (row = 0; row < rows; row++) {
    int col;
    bool_t odd_row = row&1;
    int32_t acc[2];

    for (col = 0; col < cols; col++) {
      bool_t odd_col = col&1;
      int32_t diff = get_true_diff(&BS, tree);
      int32_t prev = col < 2 ?
	row_start_acc[odd_row][odd_col] :
	acc[odd_col];
      int32_t value = prev + diff;

      acc[odd_col] = value;
      if (col < 2)
	row_start_acc[odd_row][odd_col] = value;

      *dst = value;
      dst += 3;
    }
  }
}

static void true_decode(x3f_info_t *I,
			x3f_directory_entry_t *DE)
{
  x3f_directory_entry_header_t *DEH = &DE->header;
  x3f_image_data_t *ID = &DEH->data_subsection.image_data;
  int color;

  for (color = 0; color < 3; color++) {
    true_decode_one_color(ID, color);
  }
}

/* Decode use the huffman tree */

static int32_t get_huffman_diff(bit_state_t *BS, x3f_hufftree_t *HTP)
{
  int32_t diff;
  x3f_huffnode_t *node = &HTP->nodes[0];

  while (node->branch[0] != NULL || node->branch[1] != NULL) {
    uint8_t bit = get_bit(BS);
    x3f_huffnode_t *new_node = node->branch[bit];

    node = new_node;
    if (node == NULL) {
#ifdef DCRAW_VERBOSE
      fprintf(stderr, "Huffman coding got unexpected bit\n");
#endif
      return 0;
    }
  }

  diff = node->leaf;

  return diff;
}

static void huffman_decode_row(x3f_info_t *I,
                               x3f_directory_entry_t *DE,
                               int bits,
                               int row)
{
  x3f_directory_entry_header_t *DEH = &DE->header;
  x3f_image_data_t *ID = &DEH->data_subsection.image_data;
  x3f_huffman_t *HUF = ID->huffman;

  uint16_t c[3] = {0,0,0}, c_fix[3];
  int col;
  bit_state_t BS;

  set_bit_state(&BS, ID->data + HUF->row_offsets.element[row]);

  for (col = 0; col < ID->columns; col++) {
    int color;

    for (color = 0; color < 3; color++) {
      c[color] += get_huffman_diff(&BS, &HUF->tree);

      switch (ID->type_format) {
      case X3F_IMAGE_RAW_HUFFMAN_X530:
      case X3F_IMAGE_RAW_HUFFMAN_10BIT:
        c_fix[color] = (int16_t)c[color] > 0 ? c[color] : 0;

        HUF->x3rgb16.element[3*(row*ID->columns + col) + color] = c_fix[color]; 
        break;
      case X3F_IMAGE_THUMB_HUFFMAN:
        c_fix[color] = (int8_t)c[color] > 0 ? c[color] : 0;

        HUF->rgb8.element[3*(row*ID->columns + col) + color] = c_fix[color]; 
        break;
      default:
#ifdef DCRAW_VERBOSE
        fprintf(stderr, "Unknown huffman image type\n");
#endif
        ;
      }
    }
  }
}

static void huffman_decode(x3f_info_t *I,
                           x3f_directory_entry_t *DE,
                           int bits)
{
  x3f_directory_entry_header_t *DEH = &DE->header;
  x3f_image_data_t *ID = &DEH->data_subsection.image_data;

  int row;

  for (row = 0; row < ID->rows; row++)
    huffman_decode_row(I, DE, bits, row);
}


static int32_t get_simple_diff(x3f_huffman_t *HUF, uint16_t index)
{
  if (HUF->mapping.size == 0)
    return index;
  else
    return HUF->mapping.element[index];
}

static void simple_decode_row(x3f_info_t *I,
                              x3f_directory_entry_t *DE,
                              int bits,
                              int row,
                              int row_stride)
{
  x3f_directory_entry_header_t *DEH = &DE->header;
  x3f_image_data_t *ID = &DEH->data_subsection.image_data;
  x3f_huffman_t *HUF = ID->huffman;

  uint32_t *data = (uint32_t *)(ID->data + row*row_stride); 

  uint16_t c[3] = {0,0,0}, c_fix[3];
  int col;

  uint32_t mask = 0;

  switch (bits) {
  case 8:
    mask = 0x0ff;
    break;
  case 9:
    mask = 0x1ff;
    break;
  case 10:
    mask = 0x3ff;
    break;
  case 11:
    mask = 0x7ff;
    break;
  case 12:
    mask = 0xfff;
    break;
  default:
#ifdef DCRAW_VERBOSE
    fprintf(stderr, "Unknown number of bits: %d\n", bits);
#endif
    mask = 0;
    break;
  }

  for (col = 0; col < ID->columns; col++) {
    int color;
    uint32_t val = data[col];

    for (color = 0; color < 3; color++) {
      c[color] += get_simple_diff(HUF, (val>>(color*bits))&mask);

      switch (ID->type_format) {
      case X3F_IMAGE_RAW_HUFFMAN_X530:
      case X3F_IMAGE_RAW_HUFFMAN_10BIT:
        c_fix[color] = (int16_t)c[color] > 0 ? c[color] : 0;

        HUF->x3rgb16.element[3*(row*ID->columns + col) + color] = c_fix[color]; 
        break;
      case X3F_IMAGE_THUMB_HUFFMAN:
        c_fix[color] = (int8_t)c[color] > 0 ? c[color] : 0;

        HUF->rgb8.element[3*(row*ID->columns + col) + color] = c_fix[color]; 
        break;
      default:
#ifdef DCRAW_VERBOSE
        fprintf(stderr, "Unknown huffman image type\n");
#endif
        ;
      }
    }
  }
}

static void simple_decode(x3f_info_t *I,
                          x3f_directory_entry_t *DE,
                          int bits,
                          int row_stride)
{
  x3f_directory_entry_header_t *DEH = &DE->header;
  x3f_image_data_t *ID = &DEH->data_subsection.image_data;

  int row;

  for (row = 0; row < ID->rows; row++)
    simple_decode_row(I, DE, bits, row, row_stride);
}

/* --------------------------------------------------------------------- */
/* Loading the data in a directory entry                                 */
/* --------------------------------------------------------------------- */

/* First you set the offset to where to start reading the data ... */

static void read_data_set_offset(x3f_info_t *I,
                                 x3f_directory_entry_t *DE,
                                 uint32_t header_size)
{
  uint32_t i_off = DE->input.offset + header_size;

  I->input.file->seek(i_off, SEEK_SET);
}

/* ... then you read the data, block for block */

static uint32_t read_data_block(unsigned char **data,
                                x3f_info_t *I,
                                x3f_directory_entry_t *DE,
                                uint32_t footer)
{
  uint32_t size =
    DE->input.size + DE->input.offset - I->input.file->tell() - footer;

  *data = (unsigned char *)malloc(size);

  GETN(*data, size);

  return size;
}

static void x3f_load_image_verbatim(x3f_info_t *I, x3f_directory_entry_t *DE)
{
  x3f_directory_entry_header_t *DEH = &DE->header;
  x3f_image_data_t *ID = &DEH->data_subsection.image_data;

  ID->data_size = read_data_block(&ID->data, I, DE, 0);
}

static void x3f_load_property_list(x3f_info_t *I, x3f_directory_entry_t *DE)
{
  x3f_directory_entry_header_t *DEH = &DE->header;
  x3f_property_list_t *PL = &DEH->data_subsection.property_list;

  int i;

  read_data_set_offset(I, DE, X3F_PROPERTY_LIST_HEADER_SIZE);

  PL->property_table.size = PL->num_properties;
  PL->property_table.element = (x3f_property_t *)realloc(PL->property_table.element,
                                               PL->num_properties*sizeof(PL->property_table.element[0]));
  for (i = 0; i < PL->property_table.size; i++) {
      GET4(PL->property_table.element[i].name_offset);
      GET4(PL->property_table.element[i].value_offset);
    }

  PL->data_size = read_data_block(&PL->data, I, DE, 0);

  for (i=0; i<PL->num_properties; i++) {
    x3f_property_t *P = &PL->property_table.element[i];
 
    P->name = ((utf16_t *)PL->data + P->name_offset);
    P->value = ((utf16_t *)PL->data + P->value_offset);
  }
}

static void x3f_load_true(x3f_info_t *I,
			  x3f_directory_entry_t *DE)
{
  x3f_directory_entry_header_t *DEH = &DE->header;
  x3f_image_data_t *ID = &DEH->data_subsection.image_data;
  x3f_true_t *TRU = new_true(&ID->tru);
  int i;

  /* Read TRUE header data */
  GET2(TRU->seed[0]);		/* TODO : should it always be 512 ?? */
  GET2(TRU->seed[1]);		/* TODO : should it always be 512 ?? */
  GET2(TRU->seed[2]);		/* TODO : should it always be 512 ?? */
  GET2(TRU->unknown);		/* TODO : should it always be zero ?? */

  TRU->table.element = NULL;
  for (i = 0; ; i++) {
    TRU->table.size = i + 1;
    TRU->table.element = (x3f_true_huffman_element_t*)realloc(TRU->table.element,
                                         (i + 1)*sizeof(TRU->table.element[0]));
    GET1(TRU->table.element[i].code_size);
    GET1(TRU->table.element[i].code);
    if (TRU->table.element[i].code_size == 0) break;
  }

  GET_TABLE(uint32_t,TRU->plane_size, GET4, TRUE_PLANES);

  /* Read image data */
  ID->data_size = read_data_block(&ID->data, I, DE, 0);

  /* TODO: can it be fewer than 8 bits? Maybe taken from TRU->table? */  
  new_huffman_tree(&TRU->tree, 8);

  populate_true_huffman_tree(&TRU->tree, &TRU->table);


  /* TODO : we here assume 3 planes - thats not neccessarily right */
  TRU->plane_address[0] = ID->data;
  for (i=1; i<3; i++)
    TRU->plane_address[i] = 
      TRU->plane_address[i-1] +
      (((TRU->plane_size.element[i-1] + 15) / 16) * 16); 

  TRU->x3rgb16.size = ID->columns * ID->rows * 3;
  TRU->x3rgb16.element =
    (uint16_t *)malloc(sizeof(uint16_t)*TRU->x3rgb16.size);
  true_decode(I, DE);
}

static void x3f_load_huffman_compressed(x3f_info_t *I,
                                        x3f_directory_entry_t *DE,
                                        int bits,
                                        int use_map_table)
{
  x3f_directory_entry_header_t *DEH = &DE->header;
  x3f_image_data_t *ID = &DEH->data_subsection.image_data;
  x3f_huffman_t *HUF = ID->huffman;
  int table_size = 1<<bits;
  int row_offsets_size = ID->rows * sizeof(HUF->row_offsets.element[0]);

  GET_TABLE(uint32_t,HUF->table, GET4, table_size);

  ID->data_size = read_data_block(&ID->data, I, DE, row_offsets_size);

  GET_TABLE(uint32_t,HUF->row_offsets, GET4, ID->rows);

  new_huffman_tree(&HUF->tree, bits);
  populate_huffman_tree(&HUF->tree, &HUF->table, &HUF->mapping);

  huffman_decode(I, DE, bits);
}

static void x3f_load_huffman_not_compressed(x3f_info_t *I,
                                            x3f_directory_entry_t *DE,
                                            int bits,
                                            int use_map_table,
                                            int row_stride)
{
  x3f_directory_entry_header_t *DEH = &DE->header;
  x3f_image_data_t *ID = &DEH->data_subsection.image_data;

  ID->data_size = read_data_block(&ID->data, I, DE, 0);

  simple_decode(I, DE, bits, row_stride);
}

static void x3f_load_huffman(x3f_info_t *I,
                             x3f_directory_entry_t *DE,
                             int bits,
                             int use_map_table,
                             int row_stride)
{
  x3f_directory_entry_header_t *DEH = &DE->header;
  x3f_image_data_t *ID = &DEH->data_subsection.image_data;
  x3f_huffman_t *HUF = new_huffman(&ID->huffman);

  if (use_map_table) {
    int table_size = 1<<bits;

    GET_TABLE(uint16_t,HUF->mapping, GET2, table_size); 
  }

  switch (ID->type_format) {
  case X3F_IMAGE_RAW_HUFFMAN_X530:
  case X3F_IMAGE_RAW_HUFFMAN_10BIT:
    HUF->x3rgb16.size = ID->columns * ID->rows * 3;
    HUF->x3rgb16.element =
      (uint16_t *)malloc(sizeof(uint16_t)*HUF->x3rgb16.size);
    break;
  case X3F_IMAGE_THUMB_HUFFMAN:
    HUF->rgb8.size = ID->columns * ID->rows * 3;
    HUF->rgb8.element =
      (uint8_t *)malloc(sizeof(uint8_t)*HUF->rgb8.size);
    break;
  default:
#ifdef DCRAW_VERBOSE
    fprintf(stderr, "Unknown huffman image type\n");
#endif
    ;
  }

  if (row_stride == 0)
    return x3f_load_huffman_compressed(I, DE, bits, use_map_table);
  else
    return x3f_load_huffman_not_compressed(I, DE, bits, use_map_table, row_stride);
}

static void x3f_load_pixmap(x3f_info_t *I, x3f_directory_entry_t *DE)
{
  x3f_load_image_verbatim(I, DE);
}

static void x3f_load_jpeg(x3f_info_t *I, x3f_directory_entry_t *DE)
{
  x3f_load_image_verbatim(I, DE);
}

static void x3f_load_image(x3f_info_t *I, x3f_directory_entry_t *DE)
{
  x3f_directory_entry_header_t *DEH = &DE->header;
  x3f_image_data_t *ID = &DEH->data_subsection.image_data;

  read_data_set_offset(I, DE, X3F_IMAGE_HEADER_SIZE);
  
  switch (ID->type_format) {
  case X3F_IMAGE_RAW_TRUE:
  case X3F_IMAGE_RAW_TRUE_SD1:
    x3f_load_true(I, DE);
    break;
  case X3F_IMAGE_RAW_HUFFMAN_X530:
  case X3F_IMAGE_RAW_HUFFMAN_10BIT:
    x3f_load_huffman(I, DE, 10, 1, ID->row_stride);
    break;
  case X3F_IMAGE_THUMB_PLAIN:
    x3f_load_pixmap(I, DE);
    break;
  case X3F_IMAGE_THUMB_HUFFMAN:
    x3f_load_huffman(I, DE, 8, 0, ID->row_stride);
    break;
  case X3F_IMAGE_THUMB_JPEG:
    x3f_load_jpeg(I, DE);
    break;
  default:
#ifdef DCRAW_VERBOSE
    fprintf(stderr, "Unknown image type\n");
#endif
    ;
  }
}

static void x3f_load_camf_decode_type2(x3f_camf_t *CAMF)
{
  uint32_t key = CAMF->t2.crypt_key;
  int i;

  CAMF->decoded_data_size = CAMF->data_size;
  CAMF->decoded_data = malloc(CAMF->decoded_data_size);

  for (i=0; i<CAMF->data_size; i++) {
    uint8_t old, _new;
    uint32_t tmp;

    old = ((uint8_t *)CAMF->data)[i];
    key = (key * 1597 + 51749) % 244944;
    tmp = (uint32_t)(key * ((int64_t)301593171) >> 24);
    _new = (uint8_t)(old ^ (uint8_t)(((((key << 8) - tmp) >> 1) + tmp) >> 17));
    ((uint8_t *)CAMF->decoded_data)[i] = _new;
  }
}


/* NOTE: the unpacking in this code is in big respects identical to
   true_decode_one_color(). The difference is in the output you
   build. It might be possible to make some parts shared. NOTE ALSO:
   This means that the meta data is obfuscated using an image
   compression algorithm. */

static void camf_decode_type4(x3f_camf_t *CAMF)
{
  uint32_t seed = CAMF->t4.decode_bias;
  int row;

  uint8_t *dst;
  bool_t odd_dst = 0;

  x3f_hufftree_t *tree = &CAMF->tree;
  bit_state_t BS;

  int32_t row_start_acc[2][2];
  uint32_t rows = CAMF->t4.block_count;
  uint32_t cols = CAMF->t4.block_size;

  CAMF->decoded_data_size = (cols * rows * 3) / 2;
  CAMF->decoded_data = malloc(CAMF->decoded_data_size);

  dst = (uint8_t *)CAMF->decoded_data;

  set_bit_state(&BS, CAMF->decoding_start);

  row_start_acc[0][0] = seed;
  row_start_acc[0][1] = seed;
  row_start_acc[1][0] = seed;
  row_start_acc[1][1] = seed;

  for (row = 0; row < rows; row++) {
    int col;
    bool_t odd_row = row&1;
    int32_t acc[2];

    for (col = 0; col < cols; col++) {
      bool_t odd_col = col&1;
      int32_t diff = get_true_diff(&BS, tree);
      int32_t prev = col < 2 ?
	row_start_acc[odd_row][odd_col] :
	acc[odd_col];
      int32_t value = prev + diff;

      acc[odd_col] = value;
      if (col < 2)
	row_start_acc[odd_row][odd_col] = value;

      switch(odd_dst) {
      case 0:
	*dst++  = (uint8_t)((value>>4)&0xff);
	*dst    = (uint8_t)((value<<4)&0xf0);
	break;
      case 1:
	*dst++ |= (uint8_t)((value>>8)&0x0f);
	*dst++  = (uint8_t)((value<<0)&0xff);
	break;
      }

      odd_dst = !odd_dst;

    } /* end col */
  } /* end row */
}

static void x3f_load_camf_decode_type4(x3f_camf_t *CAMF)
{
  int i;
  uint8_t *p;
  x3f_true_huffman_element_t *element = NULL;

  for (i=0, p = CAMF->data; *p != 0; i++) {
    /* TODO: Is this too expensive ??*/
    element =
      (x3f_true_huffman_element_t *)realloc(element, (i+1)*sizeof(*element));

    element[i].code_size = *p++;
    element[i].code = *p++;
  }

  CAMF->table.size = i;
  CAMF->table.element = element;

  /* TODO: where does thes value 32 come from? */
#define CAMF_T4_DATA_OFFSET 32
  CAMF->decoding_start = (uint8_t *)CAMF->data + CAMF_T4_DATA_OFFSET;

  /* TODO: can it be fewer than 8 bits? Maybe taken from TRU->table? */  
  new_huffman_tree(&CAMF->tree, 8);

  populate_true_huffman_tree(&CAMF->tree, &CAMF->table);


  camf_decode_type4(CAMF);
}

static void x3f_setup_camf_entries(x3f_camf_t *CAMF)
{
  uint8_t *p = (uint8_t *)CAMF->decoded_data;
  uint8_t *end = p + CAMF->decoded_data_size;
  camf_entry_t *table = NULL;
  int i;

  for (i=0; p < end; i++) {
    uint32_t *p4 = (uint32_t *)p;

    if ((*p4 & 0xffffff) != X3F_CMb) {
      /* TODO: whats this all about ? Is it OK to just terminate if
	 you find an invalid entry ? */
#ifdef DCRAW_VERBOSE
      fprintf(stderr, "Unknown CAMF entry %x\n", *p4);
#endif
      break;
    }

    /* TODO: lots of realloc - may be inefficient */
    table = (camf_entry_t *)realloc(table, (i+1)*sizeof(camf_entry_t));

    table[i].id = *p4++;
    table[i].version = *p4++;
    table[i].entry_size = *p4++;
    table[i].name_offset = *p4++;
    table[i].value_offset = *p4++;

    table[i].entry = p;

    table[i].name_address = p + table[i].name_offset; 
    table[i].value_address = p + table[i].value_offset; 

    p += table[i].entry_size;
  }

  CAMF->entry_table.size = i;
  CAMF->entry_table.element = table;
}

static void x3f_load_camf(x3f_info_t *I, x3f_directory_entry_t *DE)
{
  x3f_directory_entry_header_t *DEH = &DE->header;
  x3f_camf_t *CAMF = &DEH->data_subsection.camf;

  read_data_set_offset(I, DE, X3F_CAMF_HEADER_SIZE);

  CAMF->data_size = read_data_block(&CAMF->data, I, DE, 0);

  switch (CAMF->type) {
  case 2:			/* Older SD9-SD14 */
    x3f_load_camf_decode_type2(CAMF);
    break;
  case 4:			/* TRUE DP1-... */
    x3f_load_camf_decode_type4(CAMF);
    break;
  default:
#ifdef DCRAW_VERBOSE
    fprintf(stderr, "Unknown CAMF type\n");
#endif
    ;
  }

  if (CAMF->decoded_data != NULL)
    x3f_setup_camf_entries(CAMF);
#ifdef DCRAW_VERBOSE
  else
    fprintf(stderr, "No decoded CAMF data\n");
#endif
}

/* extern */ x3f_return_t x3f_load_data(x3f_t *x3f, x3f_directory_entry_t *DE)
{
  x3f_info_t *I = &x3f->info;

  if (DE == NULL)
    return X3F_ARGUMENT_ERROR;

  switch (DE->header.identifier) {
  case X3F_SECp:
    x3f_load_property_list(I, DE);
    break;
  case X3F_SECi:
    x3f_load_image(I, DE);
    break;
  case X3F_SECc:
    x3f_load_camf(I, DE);
    break;
  default:
#ifdef DCRAW_VERBOSE
    fprintf(stderr, "Unknown directory entry type\n");
#endif
    return X3F_INTERNAL_ERROR;
  }

  return X3F_OK;
}

/* extern */ x3f_return_t x3f_load_image_block(x3f_t *x3f, x3f_directory_entry_t *DE)
{
  x3f_info_t *I = &x3f->info;

  if (DE == NULL)
    return X3F_ARGUMENT_ERROR;

  switch (DE->header.identifier) {
  case X3F_SECi:
    read_data_set_offset(I, DE, X3F_IMAGE_HEADER_SIZE);
    x3f_load_image_verbatim(I, DE);
    break;
  default:
#ifdef DCRAW_VERBOSE
    fprintf(stderr, "Unknown image directory entry type\n");
#endif
    return X3F_INTERNAL_ERROR;
  }

  return X3F_OK;
}


/* extern */ x3f_return_t x3f_dump_raw_data(x3f_t *x3f,
                                            char *outfilename)
{
  x3f_directory_entry_t *DE = x3f_get_raw(x3f);

  if (DE == NULL) {
    return X3F_ARGUMENT_ERROR;
  } else {
    x3f_directory_entry_header_t *DEH = &DE->header;
    x3f_image_data_t *ID = &DEH->data_subsection.image_data;
    void *data = ID->data;

    if (data == NULL) {
      return X3F_INTERNAL_ERROR;
    } else {
      FILE *f_out = fopen(outfilename, "wb");

      if (f_out == NULL) {
        return X3F_OUTFILE_ERROR;
      } else {
        fwrite(data, 1, DE->input.size, f_out);
        fclose(f_out);
      }
    }
  }

  return X3F_OK;
}

