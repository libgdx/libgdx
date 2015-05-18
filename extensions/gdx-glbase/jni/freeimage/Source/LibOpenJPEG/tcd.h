/*
 * Copyright (c) 2002-2007, Communications and Remote Sensing Laboratory, Universite catholique de Louvain (UCL), Belgium
 * Copyright (c) 2002-2007, Professor Benoit Macq
 * Copyright (c) 2001-2003, David Janssens
 * Copyright (c) 2002-2003, Yannick Verschueren
 * Copyright (c) 2003-2007, Francois-Olivier Devaux and Antonin Descampe
 * Copyright (c) 2005, Herve Drolon, FreeImage Team
 * Copyright (c) 2008;2011-2012, Centre National d'Etudes Spatiales (CNES), France 
 * Copyright (c) 2012, CS Systemes d'Information, France
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS `AS IS'
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
#ifndef __TCD_H
#define __TCD_H
/**
@file tcd.h
@brief Implementation of a tile coder/decoder (TCD)

The functions in TCD.C encode or decode each tile independently from
each other. The functions in TCD.C are used by other functions in J2K.C.
*/

/** @defgroup TCD TCD - Implementation of a tile coder/decoder */
/*@{*/

/**
FIXME DOC
*/
typedef struct opj_tcd_seg {
	OPJ_BYTE ** data;
	OPJ_UINT32 dataindex;
	OPJ_UINT32 numpasses;
	OPJ_UINT32 real_num_passes;
	OPJ_UINT32 len;
	OPJ_UINT32 maxpasses;
	OPJ_UINT32 numnewpasses;
	OPJ_UINT32 newlen;
} opj_tcd_seg_t;

/**
FIXME DOC
*/
typedef struct opj_tcd_pass {
	OPJ_UINT32 rate;
	OPJ_FLOAT64 distortiondec;
	OPJ_UINT32 len;
	OPJ_UINT32 term : 1;
} opj_tcd_pass_t;

/**
FIXME DOC
*/
typedef struct opj_tcd_layer {
	OPJ_UINT32 numpasses;		/* Number of passes in the layer */
	OPJ_UINT32 len;				/* len of information */
	OPJ_FLOAT64 disto;			/* add for index (Cfr. Marcela) */
	OPJ_BYTE *data;				/* data */
} opj_tcd_layer_t;

/**
FIXME DOC
*/
typedef struct opj_tcd_cblk_enc {
	OPJ_BYTE* data;					/* Data */
	opj_tcd_layer_t* layers;		/* layer information */
	opj_tcd_pass_t* passes;		/* information about the passes */
	OPJ_INT32 x0, y0, x1, y1;		/* dimension of the code-blocks : left upper corner (x0, y0) right low corner (x1,y1) */
	OPJ_UINT32 numbps;
	OPJ_UINT32 numlenbits;
	OPJ_UINT32 numpasses;			/* number of pass already done for the code-blocks */
	OPJ_UINT32 numpassesinlayers;	/* number of passes in the layer */
	OPJ_UINT32 totalpasses;			/* total number of passes */
} opj_tcd_cblk_enc_t;


typedef struct opj_tcd_cblk_dec {
	OPJ_BYTE * data;				/* Data */
	opj_tcd_seg_t* segs;			/* segments information */
	OPJ_INT32 x0, y0, x1, y1;		/* position of the code-blocks : left upper corner (x0, y0) right low corner (x1,y1) */
	OPJ_UINT32 numbps;
	OPJ_UINT32 numlenbits;
    OPJ_UINT32 data_max_size;		/* Size of allocated data buffer */
	OPJ_UINT32 data_current_size;	/* Size of used data buffer */
	OPJ_UINT32 numnewpasses;		/* number of pass added to the code-blocks */
	OPJ_UINT32 numsegs;				/* number of segments */
	OPJ_UINT32 real_num_segs;
	OPJ_UINT32 m_current_max_segs;
} opj_tcd_cblk_dec_t;

/**
FIXME DOC
*/
typedef struct opj_tcd_precinct {
	OPJ_INT32 x0, y0, x1, y1;		/* dimension of the precinct : left upper corner (x0, y0) right low corner (x1,y1) */
	OPJ_UINT32 cw, ch;				/* number of precinct in width and height */
	union{							/* code-blocks information */
		opj_tcd_cblk_enc_t* enc;
		opj_tcd_cblk_dec_t* dec;
	} cblks;
	OPJ_UINT32 block_size;			/* size taken by cblks (in bytes) */
	opj_tgt_tree_t *incltree;	    /* inclusion tree */
	opj_tgt_tree_t *imsbtree;	    /* IMSB tree */
} opj_tcd_precinct_t;

/**
FIXME DOC
*/
typedef struct opj_tcd_band {
	OPJ_INT32 x0, y0, x1, y1;		/* dimension of the subband : left upper corner (x0, y0) right low corner (x1,y1) */
	OPJ_UINT32 bandno;
	opj_tcd_precinct_t *precincts;	/* precinct information */
	OPJ_UINT32 precincts_data_size;	/* size of data taken by precincts */
	OPJ_INT32 numbps;
	OPJ_FLOAT32 stepsize;
} opj_tcd_band_t;

/**
FIXME DOC
*/
typedef struct opj_tcd_resolution {
	OPJ_INT32 x0, y0, x1, y1;		/* dimension of the resolution level : left upper corner (x0, y0) right low corner (x1,y1) */
	OPJ_UINT32 pw, ph;
	OPJ_UINT32 numbands;			/* number sub-band for the resolution level */
	opj_tcd_band_t bands[3];		/* subband information */
} opj_tcd_resolution_t;

/**
FIXME DOC
*/
typedef struct opj_tcd_tilecomp
{
	OPJ_INT32 x0, y0, x1, y1;				/* dimension of component : left upper corner (x0, y0) right low corner (x1,y1) */
	OPJ_UINT32 numresolutions;				/* number of resolutions level */
	OPJ_UINT32 minimum_num_resolutions;		/* number of resolutions level to decode (at max)*/
	opj_tcd_resolution_t *resolutions;	/* resolutions information */
	OPJ_UINT32 resolutions_size;			/* size of data for resolutions (in bytes) */
	OPJ_INT32 *data;						/* data of the component */
	OPJ_UINT32 data_size;					/* size of the data of the component */
	OPJ_INT32 numpix;						/* add fixed_quality */
} opj_tcd_tilecomp_t;


/**
FIXME DOC
*/
typedef struct opj_tcd_tile {
	OPJ_INT32 x0, y0, x1, y1;		/* dimension of the tile : left upper corner (x0, y0) right low corner (x1,y1) */
	OPJ_UINT32 numcomps;			/* number of components in tile */
	opj_tcd_tilecomp_t *comps;	/* Components information */
	OPJ_INT32 numpix;				/* add fixed_quality */
	OPJ_FLOAT64 distotile;			/* add fixed_quality */
	OPJ_FLOAT64 distolayer[100];	/* add fixed_quality */
	OPJ_UINT32 packno;              /* packet number */
} opj_tcd_tile_t;

/**
FIXME DOC
*/
typedef struct opj_tcd_image
{
	opj_tcd_tile_t *tiles;		/* Tiles information */
}
opj_tcd_image_t;


/**
Tile coder/decoder
*/
typedef struct opj_tcd
{
	/** Position of the tilepart flag in Progression order*/
	OPJ_INT32 tp_pos;
	/** Tile part number*/
	OPJ_UINT32 tp_num;
	/** Current tile part number*/
	OPJ_UINT32 cur_tp_num;
	/** Total number of tileparts of the current tile*/
	OPJ_UINT32 cur_totnum_tp;
	/** Current Packet iterator number */
	OPJ_UINT32 cur_pino;
	/** info on each image tile */
	opj_tcd_image_t *tcd_image;
	/** image header */
	opj_image_t *image;
	/** coding parameters */
	opj_cp_t *cp;
	/** coding/decoding parameters common to all tiles */
	opj_tcp_t *tcp;
	/** current encoded/decoded tile */
	OPJ_UINT32 tcd_tileno;
	/** tell if the tcd is a decoder. */
	OPJ_UINT32 m_is_decoder : 1;
} opj_tcd_t;

/** @name Exported functions */
/*@{*/
/* ----------------------------------------------------------------------- */

/**
Dump the content of a tcd structure
*/
/*void tcd_dump(FILE *fd, opj_tcd_t *tcd, opj_tcd_image_t *img);*/ /* TODO MSD shoul use the new v2 structures */ 

/**
Create a new TCD handle
@param p_is_decoder FIXME DOC
@return Returns a new TCD handle if successful returns NULL otherwise
*/
opj_tcd_t* opj_tcd_create(OPJ_BOOL p_is_decoder);

/**
Destroy a previously created TCD handle
@param tcd TCD handle to destroy
*/
void opj_tcd_destroy(opj_tcd_t *tcd);

/**
 * Initialize the tile coder and may reuse some memory.
 * @param	p_tcd		TCD handle.
 * @param	p_image		raw image.
 * @param	p_cp		coding parameters.
 *
 * @return true if the encoding values could be set (false otherwise).
*/
OPJ_BOOL opj_tcd_init(	opj_tcd_t *p_tcd,
						opj_image_t * p_image,
						opj_cp_t * p_cp );

/**
 * Allocates memory for decoding a specific tile.
 *
 * @param	p_tcd		the tile decoder.
 * @param	p_tile_no	the index of the tile received in sequence. This not necessarily lead to the
 * tile at index p_tile_no.
 *
 * @return	true if the remaining data is sufficient.
 */
OPJ_BOOL opj_tcd_init_decode_tile(opj_tcd_t *p_tcd, OPJ_UINT32 p_tile_no);

void opj_tcd_makelayer_fixed(opj_tcd_t *tcd, OPJ_UINT32 layno, OPJ_UINT32 final);

void opj_tcd_rateallocate_fixed(opj_tcd_t *tcd);

void opj_tcd_makelayer(	opj_tcd_t *tcd,
						OPJ_UINT32 layno,
						OPJ_FLOAT64 thresh,
						OPJ_UINT32 final);

OPJ_BOOL opj_tcd_rateallocate(	opj_tcd_t *tcd,
								OPJ_BYTE *dest,
								OPJ_UINT32 * p_data_written,
								OPJ_UINT32 len,
								opj_codestream_info_t *cstr_info);

/**
 * Gets the maximum tile size that will be taken by the tile once decoded.
 */
OPJ_UINT32 opj_tcd_get_decoded_tile_size (opj_tcd_t *p_tcd );

/**
 * Encodes a tile from the raw image into the given buffer.
 * @param	p_tcd			Tile Coder handle
 * @param	p_tile_no		Index of the tile to encode.
 * @param	p_dest			Destination buffer
 * @param	p_data_written	pointer to an int that is incremented by the number of bytes really written on p_dest
 * @param	p_len			Maximum length of the destination buffer
 * @param	p_cstr_info		Codestream information structure
 * @return  true if the coding is successfull.
*/
OPJ_BOOL opj_tcd_encode_tile(   opj_tcd_t *p_tcd,
							    OPJ_UINT32 p_tile_no,
							    OPJ_BYTE *p_dest,
							    OPJ_UINT32 * p_data_written,
							    OPJ_UINT32 p_len,
							    struct opj_codestream_info *p_cstr_info);


/**
Decode a tile from a buffer into a raw image
@param tcd TCD handle
@param src Source buffer
@param len Length of source buffer
@param tileno Number that identifies one of the tiles to be decoded
@param cstr_info  FIXME DOC
*/
OPJ_BOOL opj_tcd_decode_tile(   opj_tcd_t *tcd,
							    OPJ_BYTE *src,
							    OPJ_UINT32 len,
							    OPJ_UINT32 tileno,
							    opj_codestream_index_t *cstr_info);


/**
 * Copies tile data from the system onto the given memory block.
 */
OPJ_BOOL opj_tcd_update_tile_data (	opj_tcd_t *p_tcd,
								    OPJ_BYTE * p_dest,
								    OPJ_UINT32 p_dest_length );

/**
 *
 */
OPJ_UINT32 opj_tcd_get_encoded_tile_size ( opj_tcd_t *p_tcd );

/**
 * Initialize the tile coder and may reuse some meory.
 *
 * @param	p_tcd		TCD handle.
 * @param	p_tile_no	current tile index to encode.
 *
 * @return true if the encoding values could be set (false otherwise).
*/
OPJ_BOOL opj_tcd_init_encode_tile (	opj_tcd_t *p_tcd,
								    OPJ_UINT32 p_tile_no );

/**
 * Copies tile data from the given memory block onto the system.
 */
OPJ_BOOL opj_tcd_copy_tile_data (opj_tcd_t *p_tcd,
                                 OPJ_BYTE * p_src,
                                 OPJ_UINT32 p_src_length );

/* ----------------------------------------------------------------------- */
/*@}*/

/*@}*/

#endif /* __TCD_H */
