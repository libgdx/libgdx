/*
 * Copyright (c) 2002-2007, Communications and Remote Sensing Laboratory, Universite catholique de Louvain (UCL), Belgium
 * Copyright (c) 2002-2007, Professor Benoit Macq
 * Copyright (c) 2001-2003, David Janssens
 * Copyright (c) 2002-2003, Yannick Verschueren
 * Copyright (c) 2003-2007, Francois-Olivier Devaux and Antonin Descampe
 * Copyright (c) 2005, Herve Drolon, FreeImage Team
 * Copyright (c) 2008, Jerome Fimes, Communications & Systemes <jerome.fimes@c-s.fr>
 * Copyright (c) 2006-2007, Parvatha Elangovan
 * Copyright (c) 2010-2011, Kaori Hagihara
 * Copyright (c) 2011-2012, Centre National d'Etudes Spatiales (CNES), France 
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

#include "opj_includes.h"

#define CINEMA_24_CS 1302083	/*Codestream length for 24fps*/
#define CINEMA_48_CS 651041		/*Codestream length for 48fps*/
#define COMP_24_CS 1041666		/*Maximum size per color component for 2K & 4K @ 24fps*/
#define COMP_48_CS 520833		/*Maximum size per color component for 2K @ 48fps*/

/** @defgroup J2K J2K - JPEG-2000 codestream reader/writer */
/*@{*/

/** @name Local static functions */
/*@{*/

/**
 * Sets up the procedures to do on reading header. Developpers wanting to extend the library can add their own reading procedures.
 */
static void opj_j2k_setup_header_reading (opj_j2k_t *p_j2k);

/**
 * The read header procedure.
 */
static OPJ_BOOL opj_j2k_read_header_procedure(  opj_j2k_t *p_j2k,
                                                opj_stream_private_t *p_stream,
                                                opj_event_mgr_t * p_manager);

/**
 * The default encoding validation procedure without any extension.
 *
 * @param       p_j2k                   the jpeg2000 codec to validate.
 * @param       p_stream                the input stream to validate.
 * @param       p_manager               the user event manager.
 *
 * @return true if the parameters are correct.
 */
static OPJ_BOOL opj_j2k_encoding_validation (   opj_j2k_t * p_j2k,
                                                opj_stream_private_t *p_stream,
                                                opj_event_mgr_t * p_manager );

/**
 * The default decoding validation procedure without any extension.
 *
 * @param       p_j2k                   the jpeg2000 codec to validate.
 * @param       p_stream                                the input stream to validate.
 * @param       p_manager               the user event manager.
 *
 * @return true if the parameters are correct.
 */
static OPJ_BOOL opj_j2k_decoding_validation (   opj_j2k_t * p_j2k,
                                                opj_stream_private_t *p_stream,
                                                opj_event_mgr_t * p_manager );

/**
 * Sets up the validation ,i.e. adds the procedures to lauch to make sure the codec parameters
 * are valid. Developpers wanting to extend the library can add their own validation procedures.
 */
static void opj_j2k_setup_encoding_validation (opj_j2k_t *p_j2k);

/**
 * Sets up the validation ,i.e. adds the procedures to lauch to make sure the codec parameters
 * are valid. Developpers wanting to extend the library can add their own validation procedures.
 */
static void opj_j2k_setup_decoding_validation (opj_j2k_t *p_j2k);

/**
 * Sets up the validation ,i.e. adds the procedures to lauch to make sure the codec parameters
 * are valid. Developpers wanting to extend the library can add their own validation procedures.
 */
static void opj_j2k_setup_end_compress (opj_j2k_t *p_j2k);

/**
 * The mct encoding validation procedure.
 *
 * @param       p_j2k                   the jpeg2000 codec to validate.
 * @param       p_stream                                the input stream to validate.
 * @param       p_manager               the user event manager.
 *
 * @return true if the parameters are correct.
 */
static OPJ_BOOL opj_j2k_mct_validation (opj_j2k_t * p_j2k,
                                        opj_stream_private_t *p_stream,
                                        opj_event_mgr_t * p_manager );

/**
 * Builds the tcd decoder to use to decode tile.
 */
static OPJ_BOOL opj_j2k_build_decoder ( opj_j2k_t * p_j2k,
                                        opj_stream_private_t *p_stream,
                                        opj_event_mgr_t * p_manager );
/**
 * Builds the tcd encoder to use to encode tile.
 */
static OPJ_BOOL opj_j2k_build_encoder ( opj_j2k_t * p_j2k,
                                        opj_stream_private_t *p_stream,
                                        opj_event_mgr_t * p_manager );

/**
 * Creates a tile-coder decoder.
 *
 * @param       p_stream                        the stream to write data to.
 * @param       p_j2k                           J2K codec.
 * @param       p_manager                   the user event manager.
*/
static OPJ_BOOL opj_j2k_create_tcd(     opj_j2k_t *p_j2k,
                                                                    opj_stream_private_t *p_stream,
                                                                    opj_event_mgr_t * p_manager );

/**
 * Excutes the given procedures on the given codec.
 *
 * @param       p_procedure_list        the list of procedures to execute
 * @param       p_j2k                           the jpeg2000 codec to execute the procedures on.
 * @param       p_stream                        the stream to execute the procedures on.
 * @param       p_manager                       the user manager.
 *
 * @return      true                            if all the procedures were successfully executed.
 */
static OPJ_BOOL opj_j2k_exec (  opj_j2k_t * p_j2k,
                            opj_procedure_list_t * p_procedure_list,
                            opj_stream_private_t *p_stream,
                            opj_event_mgr_t * p_manager);

/**
 * Updates the rates of the tcp.
 *
 * @param       p_stream                                the stream to write data to.
 * @param       p_j2k                           J2K codec.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_update_rates(   opj_j2k_t *p_j2k,
                                                                            opj_stream_private_t *p_stream,
                                                                            opj_event_mgr_t * p_manager );

/**
 * Copies the decoding tile parameters onto all the tile parameters.
 * Creates also the tile decoder.
 */
static OPJ_BOOL opj_j2k_copy_default_tcp_and_create_tcd (       opj_j2k_t * p_j2k,
                                                            opj_stream_private_t *p_stream,
                                                            opj_event_mgr_t * p_manager );

/**
 * Destroys the memory associated with the decoding of headers.
 */
static OPJ_BOOL opj_j2k_destroy_header_memory ( opj_j2k_t * p_j2k,
                                                opj_stream_private_t *p_stream,
                                                opj_event_mgr_t * p_manager );

/**
 * Reads the lookup table containing all the marker, status and action, and returns the handler associated
 * with the marker value.
 * @param       p_id            Marker value to look up
 *
 * @return      the handler associated with the id.
*/
static const struct opj_dec_memory_marker_handler * opj_j2k_get_marker_handler (OPJ_UINT32 p_id);

/**
 * Destroys a tile coding parameter structure.
 *
 * @param       p_tcp           the tile coding parameter to destroy.
 */
static void opj_j2k_tcp_destroy (opj_tcp_t *p_tcp);

/**
 * Destroys the data inside a tile coding parameter structure.
 *
 * @param       p_tcp           the tile coding parameter which contain data to destroy.
 */
static void opj_j2k_tcp_data_destroy (opj_tcp_t *p_tcp);

/**
 * Destroys a coding parameter structure.
 *
 * @param       p_cp            the coding parameter to destroy.
 */
static void opj_j2k_cp_destroy (opj_cp_t *p_cp);

/**
 * Writes a SPCod or SPCoc element, i.e. the coding style of a given component of a tile.
 *
 * @param       p_j2k           J2K codec.
 * @param       p_tile_no       FIXME DOC
 * @param       p_comp_no       the component number to output.
 * @param       p_data          FIXME DOC
 * @param       p_header_size   FIXME DOC
 * @param       p_manager       the user event manager.
 *
 * @return FIXME DOC
*/
static OPJ_BOOL opj_j2k_write_SPCod_SPCoc(      opj_j2k_t *p_j2k,
                                                                                    OPJ_UINT32 p_tile_no,
                                                                                    OPJ_UINT32 p_comp_no,
                                                                                    OPJ_BYTE * p_data,
                                                                                    OPJ_UINT32 * p_header_size,
                                                                                    opj_event_mgr_t * p_manager );

/**
 * Gets the size taken by writing a SPCod or SPCoc for the given tile and component.
 *
 * @param       p_j2k                   the J2K codec.
 * @param       p_tile_no               the tile index.
 * @param       p_comp_no               the component being outputted.
 *
 * @return      the number of bytes taken by the SPCod element.
 */
static OPJ_UINT32 opj_j2k_get_SPCod_SPCoc_size (opj_j2k_t *p_j2k,
                                                                                            OPJ_UINT32 p_tile_no,
                                                                                            OPJ_UINT32 p_comp_no );

/**
 * Reads a SPCod or SPCoc element, i.e. the coding style of a given component of a tile.
 * @param       p_j2k           the jpeg2000 codec.
 * @param       compno          FIXME DOC
 * @param       p_header_data   the data contained in the COM box.
 * @param       p_header_size   the size of the data contained in the COM marker.
 * @param       p_manager       the user event manager.
*/
static OPJ_BOOL opj_j2k_read_SPCod_SPCoc(   opj_j2k_t *p_j2k,
                                            OPJ_UINT32 compno,
                                            OPJ_BYTE * p_header_data,
                                            OPJ_UINT32 * p_header_size,
                                            opj_event_mgr_t * p_manager );

/**
 * Gets the size taken by writing SQcd or SQcc element, i.e. the quantization values of a band in the QCD or QCC.
 *
 * @param       p_tile_no               the tile index.
 * @param       p_comp_no               the component being outputted.
 * @param       p_j2k                   the J2K codec.
 *
 * @return      the number of bytes taken by the SPCod element.
 */
static OPJ_UINT32 opj_j2k_get_SQcd_SQcc_size (  opj_j2k_t *p_j2k,
                                                                                    OPJ_UINT32 p_tile_no,
                                                                                    OPJ_UINT32 p_comp_no );

/**
 * Writes a SQcd or SQcc element, i.e. the quantization values of a band in the QCD or QCC.
 *
 * @param       p_tile_no               the tile to output.
 * @param       p_comp_no               the component number to output.
 * @param       p_data                  the data buffer.
 * @param       p_header_size   pointer to the size of the data buffer, it is changed by the function.
 * @param       p_j2k                   J2K codec.
 * @param       p_manager               the user event manager.
 *
*/
static OPJ_BOOL opj_j2k_write_SQcd_SQcc(opj_j2k_t *p_j2k,
                                                                            OPJ_UINT32 p_tile_no,
                                                                            OPJ_UINT32 p_comp_no,
                                                                            OPJ_BYTE * p_data,
                                                                            OPJ_UINT32 * p_header_size,
                                                                            opj_event_mgr_t * p_manager);

/**
 * Updates the Tile Length Marker.
 */
static void opj_j2k_update_tlm ( opj_j2k_t * p_j2k, OPJ_UINT32 p_tile_part_size);

/**
 * Reads a SQcd or SQcc element, i.e. the quantization values of a band in the QCD or QCC.
 *
 * @param       p_j2k           J2K codec.
 * @param       compno          the component number to output.
 * @param       p_header_data   the data buffer.
 * @param       p_header_size   pointer to the size of the data buffer, it is changed by the function.
 * @param       p_manager       the user event manager.
 *
*/
static OPJ_BOOL opj_j2k_read_SQcd_SQcc( opj_j2k_t *p_j2k,
                                        OPJ_UINT32 compno,
                                        OPJ_BYTE * p_header_data,
                                        OPJ_UINT32 * p_header_size,
                                        opj_event_mgr_t * p_manager );

/**
 * Copies the tile component parameters of all the component from the first tile component.
 *
 * @param               p_j2k           the J2k codec.
 */
static void opj_j2k_copy_tile_component_parameters( opj_j2k_t *p_j2k );

/**
 * Copies the tile quantization parameters of all the component from the first tile component.
 *
 * @param               p_j2k           the J2k codec.
 */
static void opj_j2k_copy_tile_quantization_parameters( opj_j2k_t *p_j2k );

/**
 * Reads the tiles.
 */
static OPJ_BOOL opj_j2k_decode_tiles (  opj_j2k_t *p_j2k,
                                        opj_stream_private_t *p_stream,
                                        opj_event_mgr_t * p_manager);

static OPJ_BOOL opj_j2k_pre_write_tile ( opj_j2k_t * p_j2k,
                                                                             OPJ_UINT32 p_tile_index,
                                                                             opj_stream_private_t *p_stream,
                                                                             opj_event_mgr_t * p_manager );

static OPJ_BOOL opj_j2k_update_image_data (opj_tcd_t * p_tcd, OPJ_BYTE * p_data, opj_image_t* p_output_image);

static void opj_j2k_get_tile_data (opj_tcd_t * p_tcd, OPJ_BYTE * p_data);

static OPJ_BOOL opj_j2k_post_write_tile (opj_j2k_t * p_j2k,
                                                                             OPJ_BYTE * p_data,
                                                                             OPJ_UINT32 p_data_size,
                                                                             opj_stream_private_t *p_stream,
                                                                             opj_event_mgr_t * p_manager );

/**
 * Sets up the procedures to do on writing header.
 * Developers wanting to extend the library can add their own writing procedures.
 */
static void opj_j2k_setup_header_writing (opj_j2k_t *p_j2k);

static OPJ_BOOL opj_j2k_write_first_tile_part(  opj_j2k_t *p_j2k,
                                                                                            OPJ_BYTE * p_data,
                                                                                            OPJ_UINT32 * p_data_written,
                                                                                            OPJ_UINT32 p_total_data_size,
                                                                                            opj_stream_private_t *p_stream,
                                                                                            struct opj_event_mgr * p_manager );

static OPJ_BOOL opj_j2k_write_all_tile_parts(   opj_j2k_t *p_j2k,
                                                                                            OPJ_BYTE * p_data,
                                                                                            OPJ_UINT32 * p_data_written,
                                                                                            OPJ_UINT32 p_total_data_size,
                                                                                            opj_stream_private_t *p_stream,
                                                                                            struct opj_event_mgr * p_manager );

/**
 * Gets the offset of the header.
 *
 * @param       p_stream                the stream to write data to.
 * @param       p_j2k                   J2K codec.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_get_end_header( opj_j2k_t *p_j2k,
                                        opj_stream_private_t *p_stream,
                                        opj_event_mgr_t * p_manager );

static OPJ_BOOL opj_j2k_allocate_tile_element_cstr_index(opj_j2k_t *p_j2k);

/*
 * -----------------------------------------------------------------------
 * -----------------------------------------------------------------------
 * -----------------------------------------------------------------------
 */

/**
 * Writes the SOC marker (Start Of Codestream)
 *
 * @param       p_stream                        the stream to write data to.
 * @param       p_j2k                   J2K codec.
 * @param       p_manager       the user event manager.
*/
static OPJ_BOOL opj_j2k_write_soc(      opj_j2k_t *p_j2k,
                                                        opj_stream_private_t *p_stream,
                                                            opj_event_mgr_t * p_manager );

/**
 * Reads a SOC marker (Start of Codestream)
 * @param       p_j2k           the jpeg2000 file codec.
 * @param       p_stream        XXX needs data
 * @param       p_manager       the user event manager.
*/
static OPJ_BOOL opj_j2k_read_soc(   opj_j2k_t *p_j2k,
                                    opj_stream_private_t *p_stream,
                                    opj_event_mgr_t * p_manager );

/**
 * Writes the SIZ marker (image and tile size)
 *
 * @param       p_j2k           J2K codec.
 * @param       p_stream        the stream to write data to.
 * @param       p_manager       the user event manager.
*/
static OPJ_BOOL opj_j2k_write_siz(      opj_j2k_t *p_j2k,
                                                                opj_stream_private_t *p_stream,
                                                                opj_event_mgr_t * p_manager );

/**
 * Reads a SIZ marker (image and tile size)
 * @param       p_j2k           the jpeg2000 file codec.
 * @param       p_header_data   the data contained in the SIZ box.
 * @param       p_header_size   the size of the data contained in the SIZ marker.
 * @param       p_manager       the user event manager.
*/
static OPJ_BOOL opj_j2k_read_siz(opj_j2k_t *p_j2k,
                                 OPJ_BYTE * p_header_data,
                                 OPJ_UINT32 p_header_size,
                                 opj_event_mgr_t * p_manager);

/**
 * Writes the COM marker (comment)
 *
 * @param       p_stream                        the stream to write data to.
 * @param       p_j2k                   J2K codec.
 * @param       p_manager       the user event manager.
*/
static OPJ_BOOL opj_j2k_write_com(      opj_j2k_t *p_j2k,
                                                                        opj_stream_private_t *p_stream,
                                                                        opj_event_mgr_t * p_manager );

/**
 * Reads a COM marker (comments)
 * @param       p_j2k           the jpeg2000 file codec.
 * @param       p_header_data   the data contained in the COM box.
 * @param       p_header_size   the size of the data contained in the COM marker.
 * @param       p_manager       the user event manager.
*/
static OPJ_BOOL opj_j2k_read_com (  opj_j2k_t *p_j2k,
                                    OPJ_BYTE * p_header_data,
                                    OPJ_UINT32 p_header_size,
                                    opj_event_mgr_t * p_manager );
/**
 * Writes the COD marker (Coding style default)
 *
 * @param       p_stream                        the stream to write data to.
 * @param       p_j2k                   J2K codec.
 * @param       p_manager       the user event manager.
*/
static OPJ_BOOL opj_j2k_write_cod(      opj_j2k_t *p_j2k,
                                                                        opj_stream_private_t *p_stream,
                                                                        opj_event_mgr_t * p_manager );

/**
 * Reads a COD marker (Coding Styke defaults)
 * @param       p_header_data   the data contained in the COD box.
 * @param       p_j2k                   the jpeg2000 codec.
 * @param       p_header_size   the size of the data contained in the COD marker.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_read_cod (  opj_j2k_t *p_j2k,
                                    OPJ_BYTE * p_header_data,
                                    OPJ_UINT32 p_header_size,
                                    opj_event_mgr_t * p_manager);

#if 0
/**
 * Writes the COC marker (Coding style component)
 *
 * @param       p_j2k       J2K codec.
 * @param       p_comp_no   the index of the component to output.
 * @param       p_stream    the stream to write data to.
 * @param       p_manager   the user event manager.
*/
static OPJ_BOOL opj_j2k_write_coc(  opj_j2k_t *p_j2k,
                                                                OPJ_UINT32 p_comp_no,
                                                                opj_stream_private_t *p_stream,
                                                                opj_event_mgr_t * p_manager );
#endif

#if 0
/**
 * Writes the COC marker (Coding style component)
 *
 * @param       p_j2k                   J2K codec.
 * @param       p_comp_no               the index of the component to output.
 * @param       p_data          FIXME DOC
 * @param       p_data_written  FIXME DOC
 * @param       p_manager               the user event manager.
*/
static void opj_j2k_write_coc_in_memory(opj_j2k_t *p_j2k,
                                                                            OPJ_UINT32 p_comp_no,
                                                                            OPJ_BYTE * p_data,
                                                                            OPJ_UINT32 * p_data_written,
                                                                            opj_event_mgr_t * p_manager );
#endif

/**
 * Gets the maximum size taken by a coc.
 *
 * @param       p_j2k   the jpeg2000 codec to use.
 */
static OPJ_UINT32 opj_j2k_get_max_coc_size(opj_j2k_t *p_j2k);

/**
 * Reads a COC marker (Coding Style Component)
 * @param       p_header_data   the data contained in the COC box.
 * @param       p_j2k                   the jpeg2000 codec.
 * @param       p_header_size   the size of the data contained in the COC marker.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_read_coc (  opj_j2k_t *p_j2k,
                                    OPJ_BYTE * p_header_data,
                                    OPJ_UINT32 p_header_size,
                                    opj_event_mgr_t * p_manager );

/**
 * Writes the QCD marker (quantization default)
 *
 * @param       p_j2k                   J2K codec.
 * @param       p_stream                the stream to write data to.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_write_qcd(      opj_j2k_t *p_j2k,
                                                                        opj_stream_private_t *p_stream,
                                                                        opj_event_mgr_t * p_manager );

/**
 * Reads a QCD marker (Quantization defaults)
 * @param       p_header_data   the data contained in the QCD box.
 * @param       p_j2k                   the jpeg2000 codec.
 * @param       p_header_size   the size of the data contained in the QCD marker.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_read_qcd (  opj_j2k_t *p_j2k,
                                    OPJ_BYTE * p_header_data,
                                    OPJ_UINT32 p_header_size,
                                    opj_event_mgr_t * p_manager );
#if 0
/**
 * Writes the QCC marker (quantization component)
 *
 * @param       p_comp_no       the index of the component to output.
 * @param       p_stream                the stream to write data to.
 * @param       p_j2k                   J2K codec.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_write_qcc(      opj_j2k_t *p_j2k,
                                                                        OPJ_UINT32 p_comp_no,
                                                                        opj_stream_private_t *p_stream,
                                                                        opj_event_mgr_t * p_manager );
#endif

#if 0
/**
 * Writes the QCC marker (quantization component)
 *
 * @param       p_j2k           J2K codec.
 * @param       p_comp_no       the index of the component to output.
 * @param       p_data          FIXME DOC
 * @param       p_data_written  the stream to write data to.
 * @param       p_manager       the user event manager.
*/
static void opj_j2k_write_qcc_in_memory(opj_j2k_t *p_j2k,
                                                                            OPJ_UINT32 p_comp_no,
                                                                            OPJ_BYTE * p_data,
                                                                            OPJ_UINT32 * p_data_written,
                                                                            opj_event_mgr_t * p_manager );
#endif

/**
 * Gets the maximum size taken by a qcc.
 */
static OPJ_UINT32 opj_j2k_get_max_qcc_size (opj_j2k_t *p_j2k);

/**
 * Reads a QCC marker (Quantization component)
 * @param       p_header_data   the data contained in the QCC box.
 * @param       p_j2k                   the jpeg2000 codec.
 * @param       p_header_size   the size of the data contained in the QCC marker.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_read_qcc(   opj_j2k_t *p_j2k,
                                    OPJ_BYTE * p_header_data,
                                    OPJ_UINT32 p_header_size,
                                    opj_event_mgr_t * p_manager);
/**
 * Writes the POC marker (Progression Order Change)
 *
 * @param       p_stream                                the stream to write data to.
 * @param       p_j2k                           J2K codec.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_write_poc(      opj_j2k_t *p_j2k,
                                                                        opj_stream_private_t *p_stream,
                                                                        opj_event_mgr_t * p_manager );
/**
 * Writes the POC marker (Progression Order Change)
 *
 * @param       p_j2k          J2K codec.
 * @param       p_data         FIXME DOC
 * @param       p_data_written the stream to write data to.
 * @param       p_manager      the user event manager.
 */
static void opj_j2k_write_poc_in_memory(opj_j2k_t *p_j2k,
                                                                            OPJ_BYTE * p_data,
                                                                            OPJ_UINT32 * p_data_written,
                                                                            opj_event_mgr_t * p_manager );
/**
 * Gets the maximum size taken by the writing of a POC.
 */
static OPJ_UINT32 opj_j2k_get_max_poc_size(opj_j2k_t *p_j2k);

/**
 * Reads a POC marker (Progression Order Change)
 *
 * @param       p_header_data   the data contained in the POC box.
 * @param       p_j2k                   the jpeg2000 codec.
 * @param       p_header_size   the size of the data contained in the POC marker.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_read_poc (  opj_j2k_t *p_j2k,
                                    OPJ_BYTE * p_header_data,
                                    OPJ_UINT32 p_header_size,
                                    opj_event_mgr_t * p_manager );

/**
 * Gets the maximum size taken by the toc headers of all the tile parts of any given tile.
 */
static OPJ_UINT32 opj_j2k_get_max_toc_size (opj_j2k_t *p_j2k);

/**
 * Gets the maximum size taken by the headers of the SOT.
 *
 * @param       p_j2k   the jpeg2000 codec to use.
 */
static OPJ_UINT32 opj_j2k_get_specific_header_sizes(opj_j2k_t *p_j2k);

/**
 * Reads a CRG marker (Component registration)
 *
 * @param       p_header_data   the data contained in the TLM box.
 * @param       p_j2k                   the jpeg2000 codec.
 * @param       p_header_size   the size of the data contained in the TLM marker.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_read_crg (  opj_j2k_t *p_j2k,
                                    OPJ_BYTE * p_header_data,
                                    OPJ_UINT32 p_header_size,
                                    opj_event_mgr_t * p_manager );
/**
 * Reads a TLM marker (Tile Length Marker)
 *
 * @param       p_header_data   the data contained in the TLM box.
 * @param       p_j2k                   the jpeg2000 codec.
 * @param       p_header_size   the size of the data contained in the TLM marker.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_read_tlm (  opj_j2k_t *p_j2k,
                                    OPJ_BYTE * p_header_data,
                                    OPJ_UINT32 p_header_size,
                                    opj_event_mgr_t * p_manager);

/**
 * Writes the updated tlm.
 *
 * @param       p_stream                the stream to write data to.
 * @param       p_j2k                   J2K codec.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_write_updated_tlm(      opj_j2k_t *p_j2k,
                                            opj_stream_private_t *p_stream,
                                            opj_event_mgr_t * p_manager );

/**
 * Reads a PLM marker (Packet length, main header marker)
 *
 * @param       p_header_data   the data contained in the TLM box.
 * @param       p_j2k                   the jpeg2000 codec.
 * @param       p_header_size   the size of the data contained in the TLM marker.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_read_plm (  opj_j2k_t *p_j2k,
                                    OPJ_BYTE * p_header_data,
                                    OPJ_UINT32 p_header_size,
                                    opj_event_mgr_t * p_manager);
/**
 * Reads a PLT marker (Packet length, tile-part header)
 *
 * @param       p_header_data   the data contained in the PLT box.
 * @param       p_j2k                   the jpeg2000 codec.
 * @param       p_header_size   the size of the data contained in the PLT marker.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_read_plt (  opj_j2k_t *p_j2k,
                                    OPJ_BYTE * p_header_data,
                                    OPJ_UINT32 p_header_size,
                                    opj_event_mgr_t * p_manager );

#if 0
/**
 * Reads a PPM marker (Packed packet headers, main header)
 *
 * @param       p_header_data   the data contained in the POC box.
 * @param       p_j2k                   the jpeg2000 codec.
 * @param       p_header_size   the size of the data contained in the POC marker.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL j2k_read_ppm_v2 (
                                                opj_j2k_t *p_j2k,
                                                OPJ_BYTE * p_header_data,
                                                OPJ_UINT32 p_header_size,
                                                struct opj_event_mgr * p_manager
                                        );
#endif

static OPJ_BOOL j2k_read_ppm_v3 (
                                                opj_j2k_t *p_j2k,
                                                OPJ_BYTE * p_header_data,
                                                OPJ_UINT32 p_header_size,
                                                opj_event_mgr_t * p_manager );

/**
 * Reads a PPT marker (Packed packet headers, tile-part header)
 *
 * @param       p_header_data   the data contained in the PPT box.
 * @param       p_j2k                   the jpeg2000 codec.
 * @param       p_header_size   the size of the data contained in the PPT marker.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_read_ppt (  opj_j2k_t *p_j2k,
                                    OPJ_BYTE * p_header_data,
                                    OPJ_UINT32 p_header_size,
                                    opj_event_mgr_t * p_manager );
/**
 * Writes the TLM marker (Tile Length Marker)
 *
 * @param       p_stream                                the stream to write data to.
 * @param       p_j2k                           J2K codec.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_write_tlm(      opj_j2k_t *p_j2k,
                                                                        opj_stream_private_t *p_stream,
                                                                        opj_event_mgr_t * p_manager );

/**
 * Writes the SOT marker (Start of tile-part)
 *
 * @param       p_j2k            J2K codec.
 * @param       p_data           FIXME DOC
 * @param       p_data_written   FIXME DOC
 * @param       p_stream         the stream to write data to.
 * @param       p_manager        the user event manager.
*/
static OPJ_BOOL opj_j2k_write_sot(      opj_j2k_t *p_j2k,
                                                                        OPJ_BYTE * p_data,
                                                                        OPJ_UINT32 * p_data_written,
                                                                        const opj_stream_private_t *p_stream,
                                                                        opj_event_mgr_t * p_manager );

/**
 * Reads a PPT marker (Packed packet headers, tile-part header)
 *
 * @param       p_header_data   the data contained in the PPT box.
 * @param       p_j2k                   the jpeg2000 codec.
 * @param       p_header_size   the size of the data contained in the PPT marker.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_read_sot (  opj_j2k_t *p_j2k,
                                    OPJ_BYTE * p_header_data,
                                    OPJ_UINT32 p_header_size,
                                    opj_event_mgr_t * p_manager );
/**
 * Writes the SOD marker (Start of data)
 *
 * @param       p_j2k               J2K codec.
 * @param       p_tile_coder        FIXME DOC
 * @param       p_data              FIXME DOC
 * @param       p_data_written      FIXME DOC
 * @param       p_total_data_size   FIXME DOC
 * @param       p_stream            the stream to write data to.
 * @param       p_manager           the user event manager.
*/
static OPJ_BOOL opj_j2k_write_sod(      opj_j2k_t *p_j2k,
                                                                        opj_tcd_t * p_tile_coder,
                                                                        OPJ_BYTE * p_data,
                                                                        OPJ_UINT32 * p_data_written,
                                                                        OPJ_UINT32 p_total_data_size,
                                                                        const opj_stream_private_t *p_stream,
                                                                        opj_event_mgr_t * p_manager );

/**
 * Reads a SOD marker (Start Of Data)
 *
 * @param       p_j2k                   the jpeg2000 codec.
 * @param       p_stream                FIXME DOC
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_read_sod(   opj_j2k_t *p_j2k,
                                    opj_stream_private_t *p_stream,
                                    opj_event_mgr_t * p_manager );

void opj_j2k_update_tlm (opj_j2k_t * p_j2k, OPJ_UINT32 p_tile_part_size )
{
        opj_write_bytes(p_j2k->m_specific_param.m_encoder.m_tlm_sot_offsets_current,p_j2k->m_current_tile_number,1);            /* PSOT */
        ++p_j2k->m_specific_param.m_encoder.m_tlm_sot_offsets_current;

        opj_write_bytes(p_j2k->m_specific_param.m_encoder.m_tlm_sot_offsets_current,p_tile_part_size,4);                                        /* PSOT */
        p_j2k->m_specific_param.m_encoder.m_tlm_sot_offsets_current += 4;
}

/**
 * Writes the RGN marker (Region Of Interest)
 *
 * @param       p_tile_no               the tile to output
 * @param       p_comp_no               the component to output
 * @param       nb_comps                the number of components
 * @param       p_stream                the stream to write data to.
 * @param       p_j2k                   J2K codec.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_write_rgn(  opj_j2k_t *p_j2k,
                                    OPJ_UINT32 p_tile_no,
                                    OPJ_UINT32 p_comp_no,
                                    OPJ_UINT32 nb_comps,
                                    opj_stream_private_t *p_stream,
                                    opj_event_mgr_t * p_manager );

/**
 * Reads a RGN marker (Region Of Interest)
 *
 * @param       p_header_data   the data contained in the POC box.
 * @param       p_j2k                   the jpeg2000 codec.
 * @param       p_header_size   the size of the data contained in the POC marker.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_read_rgn (opj_j2k_t *p_j2k,
                                  OPJ_BYTE * p_header_data,
                                  OPJ_UINT32 p_header_size,
                                  opj_event_mgr_t * p_manager );

/**
 * Writes the EOC marker (End of Codestream)
 *
 * @param       p_stream                the stream to write data to.
 * @param       p_j2k                   J2K codec.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_write_eoc(      opj_j2k_t *p_j2k,
                                    opj_stream_private_t *p_stream,
                                    opj_event_mgr_t * p_manager );

#if 0
/**
 * Reads a EOC marker (End Of Codestream)
 *
 * @param       p_j2k                   the jpeg2000 codec.
 * @param       p_stream                FIXME DOC
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_read_eoc (      opj_j2k_t *p_j2k,
                                                                opj_stream_private_t *p_stream,
                                                                opj_event_mgr_t * p_manager );
#endif

/**
 * Writes the CBD-MCT-MCC-MCO markers (Multi components transform)
 *
 * @param       p_stream                        the stream to write data to.
 * @param       p_j2k                   J2K codec.
 * @param       p_manager       the user event manager.
*/
static OPJ_BOOL opj_j2k_write_mct_data_group(   opj_j2k_t *p_j2k,
                                                opj_stream_private_t *p_stream,
                                                opj_event_mgr_t * p_manager );

/**
 * Inits the Info
 *
 * @param       p_stream                the stream to write data to.
 * @param       p_j2k                   J2K codec.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_init_info(      opj_j2k_t *p_j2k,
                                    opj_stream_private_t *p_stream,
                                    opj_event_mgr_t * p_manager );

/**
Add main header marker information
@param cstr_index    Codestream information structure
@param type         marker type
@param pos          byte offset of marker segment
@param len          length of marker segment
 */
static OPJ_BOOL opj_j2k_add_mhmarker(opj_codestream_index_t *cstr_index, OPJ_UINT32 type, OPJ_OFF_T pos, OPJ_UINT32 len) ;
/**
Add tile header marker information
@param tileno       tile index number
@param cstr_index   Codestream information structure
@param type         marker type
@param pos          byte offset of marker segment
@param len          length of marker segment
 */
static OPJ_BOOL opj_j2k_add_tlmarker(OPJ_UINT32 tileno, opj_codestream_index_t *cstr_index, OPJ_UINT32 type, OPJ_OFF_T pos, OPJ_UINT32 len);

/**
 * Reads an unknown marker
 *
 * @param       p_j2k                   the jpeg2000 codec.
 * @param       p_stream                the stream object to read from.
 * @param       output_marker           FIXME DOC
 * @param       p_manager               the user event manager.
 *
 * @return      true                    if the marker could be deduced.
*/
static OPJ_BOOL opj_j2k_read_unk( opj_j2k_t *p_j2k,
                                  opj_stream_private_t *p_stream,
                                  OPJ_UINT32 *output_marker,
                                  opj_event_mgr_t * p_manager );

/**
 * Writes the MCT marker (Multiple Component Transform)
 *
 * @param       p_j2k           J2K codec.
 * @param       p_mct_record    FIXME DOC
 * @param       p_stream        the stream to write data to.
 * @param       p_manager       the user event manager.
*/
static OPJ_BOOL opj_j2k_write_mct_record(       opj_j2k_t *p_j2k,
                                                                                    opj_mct_data_t * p_mct_record,
                                            opj_stream_private_t *p_stream,
                                            opj_event_mgr_t * p_manager );

/**
 * Reads a MCT marker (Multiple Component Transform)
 *
 * @param       p_header_data   the data contained in the MCT box.
 * @param       p_j2k                   the jpeg2000 codec.
 * @param       p_header_size   the size of the data contained in the MCT marker.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_read_mct (      opj_j2k_t *p_j2k,
                                                                    OPJ_BYTE * p_header_data,
                                                                    OPJ_UINT32 p_header_size,
                                                                    opj_event_mgr_t * p_manager );

/**
 * Writes the MCC marker (Multiple Component Collection)
 *
 * @param       p_j2k                   J2K codec.
 * @param       p_mcc_record            FIXME DOC
 * @param       p_stream                the stream to write data to.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_write_mcc_record(   opj_j2k_t *p_j2k,
                                            opj_simple_mcc_decorrelation_data_t * p_mcc_record,
                                            opj_stream_private_t *p_stream,
                                            opj_event_mgr_t * p_manager );

/**
 * Reads a MCC marker (Multiple Component Collection)
 *
 * @param       p_header_data   the data contained in the MCC box.
 * @param       p_j2k                   the jpeg2000 codec.
 * @param       p_header_size   the size of the data contained in the MCC marker.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_read_mcc (      opj_j2k_t *p_j2k,
                                                                    OPJ_BYTE * p_header_data,
                                                                    OPJ_UINT32 p_header_size,
                                                                    opj_event_mgr_t * p_manager );

/**
 * Writes the MCO marker (Multiple component transformation ordering)
 *
 * @param       p_stream                                the stream to write data to.
 * @param       p_j2k                           J2K codec.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_write_mco(      opj_j2k_t *p_j2k,
                                    opj_stream_private_t *p_stream,
                                    opj_event_mgr_t * p_manager );

/**
 * Reads a MCO marker (Multiple Component Transform Ordering)
 *
 * @param       p_header_data   the data contained in the MCO box.
 * @param       p_j2k                   the jpeg2000 codec.
 * @param       p_header_size   the size of the data contained in the MCO marker.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_read_mco (      opj_j2k_t *p_j2k,
                                                                    OPJ_BYTE * p_header_data,
                                                                    OPJ_UINT32 p_header_size,
                                                                    opj_event_mgr_t * p_manager );

static OPJ_BOOL opj_j2k_add_mct(opj_tcp_t * p_tcp, opj_image_t * p_image, OPJ_UINT32 p_index);

static void  opj_j2k_read_int16_to_float (const void * p_src_data, void * p_dest_data, OPJ_UINT32 p_nb_elem);
static void  opj_j2k_read_int32_to_float (const void * p_src_data, void * p_dest_data, OPJ_UINT32 p_nb_elem);
static void  opj_j2k_read_float32_to_float (const void * p_src_data, void * p_dest_data, OPJ_UINT32 p_nb_elem);
static void  opj_j2k_read_float64_to_float (const void * p_src_data, void * p_dest_data, OPJ_UINT32 p_nb_elem);

static void  opj_j2k_read_int16_to_int32 (const void * p_src_data, void * p_dest_data, OPJ_UINT32 p_nb_elem);
static void  opj_j2k_read_int32_to_int32 (const void * p_src_data, void * p_dest_data, OPJ_UINT32 p_nb_elem);
static void  opj_j2k_read_float32_to_int32 (const void * p_src_data, void * p_dest_data, OPJ_UINT32 p_nb_elem);
static void  opj_j2k_read_float64_to_int32 (const void * p_src_data, void * p_dest_data, OPJ_UINT32 p_nb_elem);

static void  opj_j2k_write_float_to_int16 (const void * p_src_data, void * p_dest_data, OPJ_UINT32 p_nb_elem);
static void  opj_j2k_write_float_to_int32 (const void * p_src_data, void * p_dest_data, OPJ_UINT32 p_nb_elem);
static void  opj_j2k_write_float_to_float (const void * p_src_data, void * p_dest_data, OPJ_UINT32 p_nb_elem);
static void  opj_j2k_write_float_to_float64 (const void * p_src_data, void * p_dest_data, OPJ_UINT32 p_nb_elem);

/**
 * Ends the encoding, i.e. frees memory.
 *
 * @param       p_stream                the stream to write data to.
 * @param       p_j2k                   J2K codec.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_end_encoding(   opj_j2k_t *p_j2k,
                                                                            opj_stream_private_t *p_stream,
                                                                            opj_event_mgr_t * p_manager );

/**
 * Writes the CBD marker (Component bit depth definition)
 *
 * @param       p_stream                                the stream to write data to.
 * @param       p_j2k                           J2K codec.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_write_cbd(      opj_j2k_t *p_j2k,
                                                                    opj_stream_private_t *p_stream,
                                                                        opj_event_mgr_t * p_manager );

/**
 * Reads a CBD marker (Component bit depth definition)
 * @param       p_header_data   the data contained in the CBD box.
 * @param       p_j2k                   the jpeg2000 codec.
 * @param       p_header_size   the size of the data contained in the CBD marker.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_read_cbd (      opj_j2k_t *p_j2k,
                                                                OPJ_BYTE * p_header_data,
                                                                OPJ_UINT32 p_header_size,
                                                                opj_event_mgr_t * p_manager);

#if 0
/**
 * Writes COC marker for each component.
 *
 * @param       p_stream                the stream to write data to.
 * @param       p_j2k                   J2K codec.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_write_all_coc( opj_j2k_t *p_j2k,
                                                                        opj_stream_private_t *p_stream,
                                                                        opj_event_mgr_t * p_manager );
#endif

#if 0
/**
 * Writes QCC marker for each component.
 *
 * @param       p_stream                the stream to write data to.
 * @param       p_j2k                   J2K codec.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_write_all_qcc( opj_j2k_t *p_j2k,
                                                                        opj_stream_private_t *p_stream,
                                                                        opj_event_mgr_t * p_manager );
#endif

/**
 * Writes regions of interests.
 *
 * @param       p_stream                the stream to write data to.
 * @param       p_j2k                   J2K codec.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_write_regions(  opj_j2k_t *p_j2k,
                                                                        opj_stream_private_t *p_stream,
                                                                        opj_event_mgr_t * p_manager );

/**
 * Writes EPC ????
 *
 * @param       p_stream                the stream to write data to.
 * @param       p_j2k                   J2K codec.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_write_epc(      opj_j2k_t *p_j2k,
                                                                    opj_stream_private_t *p_stream,
                                                                    opj_event_mgr_t * p_manager );

/**
 * Checks the progression order changes values. Tells of the poc given as input are valid.
 * A nice message is outputted at errors.
 *
 * @param       p_pocs                  the progression order changes.
 * @param       p_nb_pocs               the number of progression order changes.
 * @param       p_nb_resolutions        the number of resolutions.
 * @param       numcomps                the number of components
 * @param       numlayers               the number of layers.
 * @param       p_manager               the user event manager.
 *
 * @return      true if the pocs are valid.
 */
static OPJ_BOOL opj_j2k_check_poc_val(  const opj_poc_t *p_pocs,
                                                                            OPJ_UINT32 p_nb_pocs,
                                                                            OPJ_UINT32 p_nb_resolutions,
                                                                            OPJ_UINT32 numcomps,
                                                                            OPJ_UINT32 numlayers,
                                                                            opj_event_mgr_t * p_manager);

/**
 * Gets the number of tile parts used for the given change of progression (if any) and the given tile.
 *
 * @param               cp                      the coding parameters.
 * @param               pino            the offset of the given poc (i.e. its position in the coding parameter).
 * @param               tileno          the given tile.
 *
 * @return              the number of tile parts.
 */
static OPJ_UINT32 opj_j2k_get_num_tp( opj_cp_t *cp, OPJ_UINT32 pino, OPJ_UINT32 tileno);

/**
 * Calculates the total number of tile parts needed by the encoder to
 * encode such an image. If not enough memory is available, then the function return false.
 *
 * @param       p_nb_tiles      pointer that will hold the number of tile parts.
 * @param       cp                      the coding parameters for the image.
 * @param       image           the image to encode.
 * @param       p_j2k                   the p_j2k encoder.
 * @param       p_manager       the user event manager.
 *
 * @return true if the function was successful, false else.
 */
static OPJ_BOOL opj_j2k_calculate_tp(   opj_j2k_t *p_j2k,
                                                                            opj_cp_t *cp,
                                                                            OPJ_UINT32 * p_nb_tiles,
                                                                            opj_image_t *image,
                                                                            opj_event_mgr_t * p_manager);

static void opj_j2k_dump_MH_info(opj_j2k_t* p_j2k, FILE* out_stream);

static void opj_j2k_dump_MH_index(opj_j2k_t* p_j2k, FILE* out_stream);

static opj_codestream_index_t* opj_j2k_create_cstr_index(void);

static OPJ_FLOAT32 opj_j2k_get_tp_stride (opj_tcp_t * p_tcp);

static OPJ_FLOAT32 opj_j2k_get_default_stride (opj_tcp_t * p_tcp);

static int opj_j2k_initialise_4K_poc(opj_poc_t *POC, int numres);

static void opj_j2k_set_cinema_parameters(opj_cparameters_t *parameters, opj_image_t *image, opj_event_mgr_t *p_manager);

static OPJ_BOOL opj_j2k_is_cinema_compliant(opj_image_t *image, OPJ_CINEMA_MODE cinema_mode, opj_event_mgr_t *p_manager);

/*@}*/

/*@}*/

/* ----------------------------------------------------------------------- */
typedef struct j2k_prog_order{
        OPJ_PROG_ORDER enum_prog;
        char str_prog[5];
}j2k_prog_order_t;

j2k_prog_order_t j2k_prog_order_list[] = {
        {OPJ_CPRL, "CPRL"},
        {OPJ_LRCP, "LRCP"},
        {OPJ_PCRL, "PCRL"},
        {OPJ_RLCP, "RLCP"},
        {OPJ_RPCL, "RPCL"},
        {(OPJ_PROG_ORDER)-1, ""}
};

/**
 * FIXME DOC
 */
static const OPJ_UINT32 MCT_ELEMENT_SIZE [] =
{
        2,
        4,
        4,
        8
};

typedef void (* opj_j2k_mct_function) (const void * p_src_data, void * p_dest_data, OPJ_UINT32 p_nb_elem);

const opj_j2k_mct_function j2k_mct_read_functions_to_float [] =
{
        opj_j2k_read_int16_to_float,
        opj_j2k_read_int32_to_float,
        opj_j2k_read_float32_to_float,
        opj_j2k_read_float64_to_float
};

const opj_j2k_mct_function j2k_mct_read_functions_to_int32 [] =
{
        opj_j2k_read_int16_to_int32,
        opj_j2k_read_int32_to_int32,
        opj_j2k_read_float32_to_int32,
        opj_j2k_read_float64_to_int32
};

const opj_j2k_mct_function j2k_mct_write_functions_from_float [] =
{
        opj_j2k_write_float_to_int16,
        opj_j2k_write_float_to_int32,
        opj_j2k_write_float_to_float,
        opj_j2k_write_float_to_float64
};

typedef struct opj_dec_memory_marker_handler
{
        /** marker value */
        OPJ_UINT32 id;
        /** value of the state when the marker can appear */
        OPJ_UINT32 states;
        /** action linked to the marker */
        OPJ_BOOL (*handler) (   opj_j2k_t *p_j2k,
                            OPJ_BYTE * p_header_data,
                            OPJ_UINT32 p_header_size,
                            opj_event_mgr_t * p_manager );
}
opj_dec_memory_marker_handler_t;

const opj_dec_memory_marker_handler_t j2k_memory_marker_handler_tab [] =
{
  {J2K_MS_SOT, J2K_STATE_MH | J2K_STATE_TPHSOT, opj_j2k_read_sot},
  {J2K_MS_COD, J2K_STATE_MH | J2K_STATE_TPH, opj_j2k_read_cod},
  {J2K_MS_COC, J2K_STATE_MH | J2K_STATE_TPH, opj_j2k_read_coc},
  {J2K_MS_RGN, J2K_STATE_MH | J2K_STATE_TPH, opj_j2k_read_rgn},
  {J2K_MS_QCD, J2K_STATE_MH | J2K_STATE_TPH, opj_j2k_read_qcd},
  {J2K_MS_QCC, J2K_STATE_MH | J2K_STATE_TPH, opj_j2k_read_qcc},
  {J2K_MS_POC, J2K_STATE_MH | J2K_STATE_TPH, opj_j2k_read_poc},
  {J2K_MS_SIZ, J2K_STATE_MHSIZ, opj_j2k_read_siz},
  {J2K_MS_TLM, J2K_STATE_MH, opj_j2k_read_tlm},
  {J2K_MS_PLM, J2K_STATE_MH, opj_j2k_read_plm},
  {J2K_MS_PLT, J2K_STATE_TPH, opj_j2k_read_plt},
  {J2K_MS_PPM, J2K_STATE_MH, j2k_read_ppm_v3},
  {J2K_MS_PPT, J2K_STATE_TPH, opj_j2k_read_ppt},
  {J2K_MS_SOP, 0, 0},
  {J2K_MS_CRG, J2K_STATE_MH, opj_j2k_read_crg},
  {J2K_MS_COM, J2K_STATE_MH | J2K_STATE_TPH, opj_j2k_read_com},
  {J2K_MS_MCT, J2K_STATE_MH | J2K_STATE_TPH, opj_j2k_read_mct},
  {J2K_MS_CBD, J2K_STATE_MH , opj_j2k_read_cbd},
  {J2K_MS_MCC, J2K_STATE_MH | J2K_STATE_TPH, opj_j2k_read_mcc},
  {J2K_MS_MCO, J2K_STATE_MH | J2K_STATE_TPH, opj_j2k_read_mco},
#ifdef USE_JPWL
#ifdef TODO_MS /* remove these functions which are not commpatible with the v2 API */
  {J2K_MS_EPC, J2K_STATE_MH | J2K_STATE_TPH, j2k_read_epc},
  {J2K_MS_EPB, J2K_STATE_MH | J2K_STATE_TPH, j2k_read_epb},
  {J2K_MS_ESD, J2K_STATE_MH | J2K_STATE_TPH, j2k_read_esd},
  {J2K_MS_RED, J2K_STATE_MH | J2K_STATE_TPH, j2k_read_red},
#endif
#endif /* USE_JPWL */
#ifdef USE_JPSEC
  {J2K_MS_SEC, J2K_DEC_STATE_MH, j2k_read_sec},
  {J2K_MS_INSEC, 0, j2k_read_insec}
#endif /* USE_JPSEC */
  {J2K_MS_UNK, J2K_STATE_MH | J2K_STATE_TPH, 0}/*opj_j2k_read_unk is directly used*/
};

void  opj_j2k_read_int16_to_float (const void * p_src_data, void * p_dest_data, OPJ_UINT32 p_nb_elem)
{
        OPJ_BYTE * l_src_data = (OPJ_BYTE *) p_src_data;
        OPJ_FLOAT32 * l_dest_data = (OPJ_FLOAT32 *) p_dest_data;
        OPJ_UINT32 i;
        OPJ_UINT32 l_temp;

        for (i=0;i<p_nb_elem;++i) {
                opj_read_bytes(l_src_data,&l_temp,2);

                l_src_data+=sizeof(OPJ_INT16);

                *(l_dest_data++) = (OPJ_FLOAT32) l_temp;
        }
}

void  opj_j2k_read_int32_to_float (const void * p_src_data, void * p_dest_data, OPJ_UINT32 p_nb_elem)
{
        OPJ_BYTE * l_src_data = (OPJ_BYTE *) p_src_data;
        OPJ_FLOAT32 * l_dest_data = (OPJ_FLOAT32 *) p_dest_data;
        OPJ_UINT32 i;
        OPJ_UINT32 l_temp;

        for (i=0;i<p_nb_elem;++i) {
                opj_read_bytes(l_src_data,&l_temp,4);

                l_src_data+=sizeof(OPJ_INT32);

                *(l_dest_data++) = (OPJ_FLOAT32) l_temp;
        }
}

void  opj_j2k_read_float32_to_float (const void * p_src_data, void * p_dest_data, OPJ_UINT32 p_nb_elem)
{
        OPJ_BYTE * l_src_data = (OPJ_BYTE *) p_src_data;
        OPJ_FLOAT32 * l_dest_data = (OPJ_FLOAT32 *) p_dest_data;
        OPJ_UINT32 i;
        OPJ_FLOAT32 l_temp;

        for (i=0;i<p_nb_elem;++i) {
                opj_read_float(l_src_data,&l_temp);

                l_src_data+=sizeof(OPJ_FLOAT32);

                *(l_dest_data++) = l_temp;
        }
}

void  opj_j2k_read_float64_to_float (const void * p_src_data, void * p_dest_data, OPJ_UINT32 p_nb_elem)
{
        OPJ_BYTE * l_src_data = (OPJ_BYTE *) p_src_data;
        OPJ_FLOAT32 * l_dest_data = (OPJ_FLOAT32 *) p_dest_data;
        OPJ_UINT32 i;
        OPJ_FLOAT64 l_temp;

        for (i=0;i<p_nb_elem;++i) {
                opj_read_double(l_src_data,&l_temp);

                l_src_data+=sizeof(OPJ_FLOAT64);

                *(l_dest_data++) = (OPJ_FLOAT32) l_temp;
        }
}

void  opj_j2k_read_int16_to_int32 (const void * p_src_data, void * p_dest_data, OPJ_UINT32 p_nb_elem)
{
        OPJ_BYTE * l_src_data = (OPJ_BYTE *) p_src_data;
        OPJ_INT32 * l_dest_data = (OPJ_INT32 *) p_dest_data;
        OPJ_UINT32 i;
        OPJ_UINT32 l_temp;

        for (i=0;i<p_nb_elem;++i) {
                opj_read_bytes(l_src_data,&l_temp,2);

                l_src_data+=sizeof(OPJ_INT16);

                *(l_dest_data++) = (OPJ_INT32) l_temp;
        }
}

void  opj_j2k_read_int32_to_int32 (const void * p_src_data, void * p_dest_data, OPJ_UINT32 p_nb_elem)
{
        OPJ_BYTE * l_src_data = (OPJ_BYTE *) p_src_data;
        OPJ_INT32 * l_dest_data = (OPJ_INT32 *) p_dest_data;
        OPJ_UINT32 i;
        OPJ_UINT32 l_temp;

        for (i=0;i<p_nb_elem;++i) {
                opj_read_bytes(l_src_data,&l_temp,4);

                l_src_data+=sizeof(OPJ_INT32);

                *(l_dest_data++) = (OPJ_INT32) l_temp;
        }
}

void  opj_j2k_read_float32_to_int32 (const void * p_src_data, void * p_dest_data, OPJ_UINT32 p_nb_elem)
{
        OPJ_BYTE * l_src_data = (OPJ_BYTE *) p_src_data;
        OPJ_INT32 * l_dest_data = (OPJ_INT32 *) p_dest_data;
        OPJ_UINT32 i;
        OPJ_FLOAT32 l_temp;

        for (i=0;i<p_nb_elem;++i) {
                opj_read_float(l_src_data,&l_temp);

                l_src_data+=sizeof(OPJ_FLOAT32);

                *(l_dest_data++) = (OPJ_INT32) l_temp;
        }
}

void  opj_j2k_read_float64_to_int32 (const void * p_src_data, void * p_dest_data, OPJ_UINT32 p_nb_elem)
{
        OPJ_BYTE * l_src_data = (OPJ_BYTE *) p_src_data;
        OPJ_INT32 * l_dest_data = (OPJ_INT32 *) p_dest_data;
        OPJ_UINT32 i;
        OPJ_FLOAT64 l_temp;

        for (i=0;i<p_nb_elem;++i) {
                opj_read_double(l_src_data,&l_temp);

                l_src_data+=sizeof(OPJ_FLOAT64);

                *(l_dest_data++) = (OPJ_INT32) l_temp;
        }
}

void  opj_j2k_write_float_to_int16 (const void * p_src_data, void * p_dest_data, OPJ_UINT32 p_nb_elem)
{
        OPJ_BYTE * l_dest_data = (OPJ_BYTE *) p_dest_data;
        OPJ_FLOAT32 * l_src_data = (OPJ_FLOAT32 *) p_src_data;
        OPJ_UINT32 i;
        OPJ_UINT32 l_temp;

        for (i=0;i<p_nb_elem;++i) {
                l_temp = (OPJ_UINT32) *(l_src_data++);

                opj_write_bytes(l_dest_data,l_temp,sizeof(OPJ_INT16));

                l_dest_data+=sizeof(OPJ_INT16);
        }
}

void opj_j2k_write_float_to_int32 (const void * p_src_data, void * p_dest_data, OPJ_UINT32 p_nb_elem)
{
        OPJ_BYTE * l_dest_data = (OPJ_BYTE *) p_dest_data;
        OPJ_FLOAT32 * l_src_data = (OPJ_FLOAT32 *) p_src_data;
        OPJ_UINT32 i;
        OPJ_UINT32 l_temp;

        for (i=0;i<p_nb_elem;++i) {
                l_temp = (OPJ_UINT32) *(l_src_data++);

                opj_write_bytes(l_dest_data,l_temp,sizeof(OPJ_INT32));

                l_dest_data+=sizeof(OPJ_INT32);
        }
}

void  opj_j2k_write_float_to_float (const void * p_src_data, void * p_dest_data, OPJ_UINT32 p_nb_elem)
{
        OPJ_BYTE * l_dest_data = (OPJ_BYTE *) p_dest_data;
        OPJ_FLOAT32 * l_src_data = (OPJ_FLOAT32 *) p_src_data;
        OPJ_UINT32 i;
        OPJ_FLOAT32 l_temp;

        for (i=0;i<p_nb_elem;++i) {
                l_temp = (OPJ_FLOAT32) *(l_src_data++);

                opj_write_float(l_dest_data,l_temp);

                l_dest_data+=sizeof(OPJ_FLOAT32);
        }
}

void  opj_j2k_write_float_to_float64 (const void * p_src_data, void * p_dest_data, OPJ_UINT32 p_nb_elem)
{
        OPJ_BYTE * l_dest_data = (OPJ_BYTE *) p_dest_data;
        OPJ_FLOAT32 * l_src_data = (OPJ_FLOAT32 *) p_src_data;
        OPJ_UINT32 i;
        OPJ_FLOAT64 l_temp;

        for (i=0;i<p_nb_elem;++i) {
                l_temp = (OPJ_FLOAT64) *(l_src_data++);

                opj_write_double(l_dest_data,l_temp);

                l_dest_data+=sizeof(OPJ_FLOAT64);
        }
}

char *opj_j2k_convert_progression_order(OPJ_PROG_ORDER prg_order){
        j2k_prog_order_t *po;
        for(po = j2k_prog_order_list; po->enum_prog != -1; po++ ){
                if(po->enum_prog == prg_order){
                        return po->str_prog;
                }
        }
        return po->str_prog;
}

OPJ_BOOL opj_j2k_check_poc_val( const opj_poc_t *p_pocs,
                                                        OPJ_UINT32 p_nb_pocs,
                                                        OPJ_UINT32 p_nb_resolutions,
                                                        OPJ_UINT32 p_num_comps,
                                                        OPJ_UINT32 p_num_layers,
                                                        opj_event_mgr_t * p_manager)
{
        OPJ_UINT32* packet_array;
        OPJ_UINT32 index , resno, compno, layno;
        OPJ_UINT32 i;
        OPJ_UINT32 step_c = 1;
        OPJ_UINT32 step_r = p_num_comps * step_c;
        OPJ_UINT32 step_l = p_nb_resolutions * step_r;
        OPJ_BOOL loss = OPJ_FALSE;
        OPJ_UINT32 layno0 = 0;

        packet_array = (OPJ_UINT32*) opj_calloc(step_l * p_num_layers, sizeof(OPJ_UINT32));
        if (packet_array == 00) {
                opj_event_msg(p_manager , EVT_ERROR, "Not enough memory for checking the poc values.\n");
                return OPJ_FALSE;
        }
        memset(packet_array,0,step_l * p_num_layers* sizeof(OPJ_UINT32));

        if (p_nb_pocs == 0) {
        opj_free(packet_array);
                return OPJ_TRUE;
        }

        index = step_r * p_pocs->resno0;
        /* take each resolution for each poc */
        for (resno = p_pocs->resno0 ; resno < p_pocs->resno1 ; ++resno)
        {
                OPJ_UINT32 res_index = index + p_pocs->compno0 * step_c;

                /* take each comp of each resolution for each poc */
                for (compno = p_pocs->compno0 ; compno < p_pocs->compno1 ; ++compno) {
                        OPJ_UINT32 comp_index = res_index + layno0 * step_l;

                        /* and finally take each layer of each res of ... */
                        for (layno = layno0; layno < p_pocs->layno1 ; ++layno) {
                                /*index = step_r * resno + step_c * compno + step_l * layno;*/
                                packet_array[comp_index] = 1;
                                comp_index += step_l;
                        }

                        res_index += step_c;
                }

                index += step_r;
        }
        ++p_pocs;

        /* iterate through all the pocs */
        for (i = 1; i < p_nb_pocs ; ++i) {
                OPJ_UINT32 l_last_layno1 = (p_pocs-1)->layno1 ;

                layno0 = (p_pocs->layno1 > l_last_layno1)? l_last_layno1 : 0;
                index = step_r * p_pocs->resno0;

                /* take each resolution for each poc */
                for (resno = p_pocs->resno0 ; resno < p_pocs->resno1 ; ++resno) {
                        OPJ_UINT32 res_index = index + p_pocs->compno0 * step_c;

                        /* take each comp of each resolution for each poc */
                        for (compno = p_pocs->compno0 ; compno < p_pocs->compno1 ; ++compno) {
                                OPJ_UINT32 comp_index = res_index + layno0 * step_l;

                                /* and finally take each layer of each res of ... */
                                for (layno = layno0; layno < p_pocs->layno1 ; ++layno) {
                                        /*index = step_r * resno + step_c * compno + step_l * layno;*/
                                        packet_array[comp_index] = 1;
                                        comp_index += step_l;
                                }

                                res_index += step_c;
                        }

                        index += step_r;
                }

                ++p_pocs;
        }

        index = 0;
        for (layno = 0; layno < p_num_layers ; ++layno) {
                for (resno = 0; resno < p_nb_resolutions; ++resno) {
                        for (compno = 0; compno < p_num_comps; ++compno) {
                                loss |= (packet_array[index]!=1);
                                /*index = step_r * resno + step_c * compno + step_l * layno;*/
                                index += step_c;
                        }
                }
        }

        if (loss) {
                opj_event_msg(p_manager , EVT_ERROR, "Missing packets possible loss of data\n");
        }

        opj_free(packet_array);

        return !loss;
}

/* ----------------------------------------------------------------------- */

OPJ_UINT32 opj_j2k_get_num_tp(opj_cp_t *cp, OPJ_UINT32 pino, OPJ_UINT32 tileno)
{
        const OPJ_CHAR *prog = 00;
        OPJ_INT32 i;
        OPJ_UINT32 tpnum = 1;
        opj_tcp_t *tcp = 00;
        opj_poc_t * l_current_poc = 00;

        /*  preconditions */
        assert(tileno < (cp->tw * cp->th));
        assert(pino < (cp->tcps[tileno].numpocs + 1));

        /* get the given tile coding parameter */
        tcp = &cp->tcps[tileno];
        assert(tcp != 00);

        l_current_poc = &(tcp->pocs[pino]);
        assert(l_current_poc != 0);

        /* get the progression order as a character string */
        prog = opj_j2k_convert_progression_order(tcp->prg);
        assert(strlen(prog) > 0);

        if (cp->m_specific_param.m_enc.m_tp_on == 1) {
                for (i=0;i<4;++i) {
                        switch (prog[i])
                        {
                                /* component wise */
                                case 'C':
                                        tpnum *= l_current_poc->compE;
                                        break;
                                /* resolution wise */
                                case 'R':
                                        tpnum *= l_current_poc->resE;
                                        break;
                                /* precinct wise */
                                case 'P':
                                        tpnum *= l_current_poc->prcE;
                                        break;
                                /* layer wise */
                                case 'L':
                                        tpnum *= l_current_poc->layE;
                                        break;
                        }
                        /* whould we split here ? */
                        if ( cp->m_specific_param.m_enc.m_tp_flag == prog[i] ) {
                                cp->m_specific_param.m_enc.m_tp_pos=i;
                                break;
                        }
                }
        }
        else {
                tpnum=1;
        }

        return tpnum;
}

OPJ_BOOL opj_j2k_calculate_tp(  opj_j2k_t *p_j2k,
                                                        opj_cp_t *cp,
                                                        OPJ_UINT32 * p_nb_tiles,
                                                        opj_image_t *image,
                                                        opj_event_mgr_t * p_manager
                                )
{
        OPJ_UINT32 pino,tileno;
        OPJ_UINT32 l_nb_tiles;
        opj_tcp_t *tcp;

        /* preconditions */
        assert(p_nb_tiles != 00);
        assert(cp != 00);
        assert(image != 00);
        assert(p_j2k != 00);
        assert(p_manager != 00);

        l_nb_tiles = cp->tw * cp->th;
        * p_nb_tiles = 0;
        tcp = cp->tcps;

        /* INDEX >> */
        /* TODO mergeV2: check this part which use cstr_info */
        /*if (p_j2k->cstr_info) {
                opj_tile_info_t * l_info_tile_ptr = p_j2k->cstr_info->tile;

                for (tileno = 0; tileno < l_nb_tiles; ++tileno) {
                        OPJ_UINT32 cur_totnum_tp = 0;

                        opj_pi_update_encoding_parameters(image,cp,tileno);

                        for (pino = 0; pino <= tcp->numpocs; ++pino)
                        {
                                OPJ_UINT32 tp_num = opj_j2k_get_num_tp(cp,pino,tileno);

                                *p_nb_tiles = *p_nb_tiles + tp_num;

                                cur_totnum_tp += tp_num;
                        }

                        tcp->m_nb_tile_parts = cur_totnum_tp;

                        l_info_tile_ptr->tp = (opj_tp_info_t *) opj_malloc(cur_totnum_tp * sizeof(opj_tp_info_t));
                        if (l_info_tile_ptr->tp == 00) {
                                return OPJ_FALSE;
                        }

                        memset(l_info_tile_ptr->tp,0,cur_totnum_tp * sizeof(opj_tp_info_t));

                        l_info_tile_ptr->num_tps = cur_totnum_tp;

                        ++l_info_tile_ptr;
                        ++tcp;
                }
        }
        else */{
                for (tileno = 0; tileno < l_nb_tiles; ++tileno) {
                        OPJ_UINT32 cur_totnum_tp = 0;

                        opj_pi_update_encoding_parameters(image,cp,tileno);

                        for (pino = 0; pino <= tcp->numpocs; ++pino) {
                                OPJ_UINT32 tp_num = opj_j2k_get_num_tp(cp,pino,tileno);

                                *p_nb_tiles = *p_nb_tiles + tp_num;

                                cur_totnum_tp += tp_num;
                        }
                        tcp->m_nb_tile_parts = cur_totnum_tp;

                        ++tcp;
                }
        }

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_write_soc(     opj_j2k_t *p_j2k,
                                                opj_stream_private_t *p_stream,
                                                    opj_event_mgr_t * p_manager )
{
        /* 2 bytes will be written */
        OPJ_BYTE * l_start_stream = 00;

        /* preconditions */
        assert(p_stream != 00);
        assert(p_j2k != 00);
        assert(p_manager != 00);

        l_start_stream = p_j2k->m_specific_param.m_encoder.m_header_tile_data;

        /* write SOC identifier */
        opj_write_bytes(l_start_stream,J2K_MS_SOC,2);

        if (opj_stream_write_data(p_stream,l_start_stream,2,p_manager) != 2) {
                return OPJ_FALSE;
        }

/* UniPG>> */
#ifdef USE_JPWL
        /* update markers struct */
/*
        OPJ_BOOL res = j2k_add_marker(p_j2k->cstr_info, J2K_MS_SOC, p_stream_tell(p_stream) - 2, 2);
*/
  assert( 0 && "TODO" );
#endif /* USE_JPWL */
/* <<UniPG */

        return OPJ_TRUE;
}

/**
 * Reads a SOC marker (Start of Codestream)
 * @param       p_j2k           the jpeg2000 file codec.
 * @param       p_stream        FIXME DOC
 * @param       p_manager       the user event manager.
*/
static OPJ_BOOL opj_j2k_read_soc(   opj_j2k_t *p_j2k,
                                    opj_stream_private_t *p_stream,
                                    opj_event_mgr_t * p_manager
                                    )
{
        OPJ_BYTE l_data [2];
        OPJ_UINT32 l_marker;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_manager != 00);
        assert(p_stream != 00);

        if (opj_stream_read_data(p_stream,l_data,2,p_manager) != 2) {
                return OPJ_FALSE;
        }

        opj_read_bytes(l_data,&l_marker,2);
        if (l_marker != J2K_MS_SOC) {
                return OPJ_FALSE;
        }

        /* Next marker should be a SIZ marker in the main header */
        p_j2k->m_specific_param.m_decoder.m_state = J2K_STATE_MHSIZ;

        /* FIXME move it in a index structure included in p_j2k*/
        p_j2k->cstr_index->main_head_start = opj_stream_tell(p_stream) - 2;

        opj_event_msg(p_manager, EVT_INFO, "Start to read j2k main header (%d).\n", p_j2k->cstr_index->main_head_start);

        /* Add the marker to the codestream index*/
        if (OPJ_FALSE == opj_j2k_add_mhmarker(p_j2k->cstr_index, J2K_MS_SOC, p_j2k->cstr_index->main_head_start, 2)) {
                opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to add mh marker\n");
                return OPJ_FALSE;
        }
        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_write_siz(     opj_j2k_t *p_j2k,
                                                        opj_stream_private_t *p_stream,
                                                        opj_event_mgr_t * p_manager )
{
        OPJ_UINT32 i;
        OPJ_UINT32 l_size_len;
        OPJ_BYTE * l_current_ptr;
        opj_image_t * l_image = 00;
        opj_cp_t *cp = 00;
        opj_image_comp_t * l_img_comp = 00;

        /* preconditions */
        assert(p_stream != 00);
        assert(p_j2k != 00);
        assert(p_manager != 00);

        l_image = p_j2k->m_private_image;
        cp = &(p_j2k->m_cp);
        l_size_len = 40 + 3 * l_image->numcomps;
        l_img_comp = l_image->comps;

        if (l_size_len > p_j2k->m_specific_param.m_encoder.m_header_tile_data_size) {

                OPJ_BYTE *new_header_tile_data = (OPJ_BYTE *) opj_realloc(p_j2k->m_specific_param.m_encoder.m_header_tile_data, l_size_len);
                if (! new_header_tile_data) {
                        opj_free(p_j2k->m_specific_param.m_encoder.m_header_tile_data);
                        p_j2k->m_specific_param.m_encoder.m_header_tile_data = NULL;
                        p_j2k->m_specific_param.m_encoder.m_header_tile_data_size = 0;
                        opj_event_msg(p_manager, EVT_ERROR, "Not enough memory for the SIZ marker\n");
                        return OPJ_FALSE;
                }
                p_j2k->m_specific_param.m_encoder.m_header_tile_data = new_header_tile_data;
                p_j2k->m_specific_param.m_encoder.m_header_tile_data_size = l_size_len;
        }

        l_current_ptr = p_j2k->m_specific_param.m_encoder.m_header_tile_data;

        /* write SOC identifier */
        opj_write_bytes(l_current_ptr,J2K_MS_SIZ,2);    /* SIZ */
        l_current_ptr+=2;

        opj_write_bytes(l_current_ptr,l_size_len-2,2); /* L_SIZ */
        l_current_ptr+=2;

        opj_write_bytes(l_current_ptr, cp->rsiz, 2);    /* Rsiz (capabilities) */
        l_current_ptr+=2;

        opj_write_bytes(l_current_ptr, l_image->x1, 4); /* Xsiz */
        l_current_ptr+=4;

        opj_write_bytes(l_current_ptr, l_image->y1, 4); /* Ysiz */
        l_current_ptr+=4;

        opj_write_bytes(l_current_ptr, l_image->x0, 4); /* X0siz */
        l_current_ptr+=4;

        opj_write_bytes(l_current_ptr, l_image->y0, 4); /* Y0siz */
        l_current_ptr+=4;

        opj_write_bytes(l_current_ptr, cp->tdx, 4);             /* XTsiz */
        l_current_ptr+=4;

        opj_write_bytes(l_current_ptr, cp->tdy, 4);             /* YTsiz */
        l_current_ptr+=4;

        opj_write_bytes(l_current_ptr, cp->tx0, 4);             /* XT0siz */
        l_current_ptr+=4;

        opj_write_bytes(l_current_ptr, cp->ty0, 4);             /* YT0siz */
        l_current_ptr+=4;

        opj_write_bytes(l_current_ptr, l_image->numcomps, 2);   /* Csiz */
        l_current_ptr+=2;

        for (i = 0; i < l_image->numcomps; ++i) {
                /* TODO here with MCT ? */
                opj_write_bytes(l_current_ptr, l_img_comp->prec - 1 + (l_img_comp->sgnd << 7), 1);      /* Ssiz_i */
                ++l_current_ptr;

                opj_write_bytes(l_current_ptr, l_img_comp->dx, 1);      /* XRsiz_i */
                ++l_current_ptr;

                opj_write_bytes(l_current_ptr, l_img_comp->dy, 1);      /* YRsiz_i */
                ++l_current_ptr;

                ++l_img_comp;
        }

        if (opj_stream_write_data(p_stream,p_j2k->m_specific_param.m_encoder.m_header_tile_data,l_size_len,p_manager) != l_size_len) {
                return OPJ_FALSE;
        }

        return OPJ_TRUE;
}

/**
 * Reads a SIZ marker (image and tile size)
 * @param       p_j2k           the jpeg2000 file codec.
 * @param       p_header_data   the data contained in the SIZ box.
 * @param       p_header_size   the size of the data contained in the SIZ marker.
 * @param       p_manager       the user event manager.
*/
static OPJ_BOOL opj_j2k_read_siz(opj_j2k_t *p_j2k,
                                 OPJ_BYTE * p_header_data,
                                 OPJ_UINT32 p_header_size,
                                 opj_event_mgr_t * p_manager
                                 )
{
        OPJ_UINT32 i;
        OPJ_UINT32 l_nb_comp;
        OPJ_UINT32 l_nb_comp_remain;
        OPJ_UINT32 l_remaining_size;
        OPJ_UINT32 l_nb_tiles;
        OPJ_UINT32 l_tmp;
        opj_image_t *l_image = 00;
        opj_cp_t *l_cp = 00;
        opj_image_comp_t * l_img_comp = 00;
        opj_tcp_t * l_current_tile_param = 00;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_manager != 00);
        assert(p_header_data != 00);

        l_image = p_j2k->m_private_image;
        l_cp = &(p_j2k->m_cp);

        /* minimum size == 39 - 3 (= minimum component parameter) */
        if (p_header_size < 36) {
                opj_event_msg(p_manager, EVT_ERROR, "Error with SIZ marker size\n");
                return OPJ_FALSE;
        }

        l_remaining_size = p_header_size - 36;
        l_nb_comp = l_remaining_size / 3;
        l_nb_comp_remain = l_remaining_size % 3;
        if (l_nb_comp_remain != 0){
                opj_event_msg(p_manager, EVT_ERROR, "Error with SIZ marker size\n");
                return OPJ_FALSE;
        }

        opj_read_bytes(p_header_data,&l_tmp ,2);                                                /* Rsiz (capabilities) */
        p_header_data+=2;
        l_cp->rsiz = (OPJ_RSIZ_CAPABILITIES) l_tmp;
        opj_read_bytes(p_header_data, (OPJ_UINT32*) &l_image->x1, 4);   /* Xsiz */
        p_header_data+=4;
        opj_read_bytes(p_header_data, (OPJ_UINT32*) &l_image->y1, 4);   /* Ysiz */
        p_header_data+=4;
        opj_read_bytes(p_header_data, (OPJ_UINT32*) &l_image->x0, 4);   /* X0siz */
        p_header_data+=4;
        opj_read_bytes(p_header_data, (OPJ_UINT32*) &l_image->y0, 4);   /* Y0siz */
        p_header_data+=4;
        opj_read_bytes(p_header_data, (OPJ_UINT32*) &l_cp->tdx, 4);             /* XTsiz */
        p_header_data+=4;
        opj_read_bytes(p_header_data, (OPJ_UINT32*) &l_cp->tdy, 4);             /* YTsiz */
        p_header_data+=4;
        opj_read_bytes(p_header_data, (OPJ_UINT32*) &l_cp->tx0, 4);             /* XT0siz */
        p_header_data+=4;
        opj_read_bytes(p_header_data, (OPJ_UINT32*) &l_cp->ty0, 4);             /* YT0siz */
        p_header_data+=4;
        opj_read_bytes(p_header_data, (OPJ_UINT32*) &l_tmp, 2);                 /* Csiz */
        p_header_data+=2;
        if (l_tmp < 16385)
                l_image->numcomps = (OPJ_UINT16) l_tmp;
        else {
                opj_event_msg(p_manager, EVT_ERROR, "Error with SIZ marker: number of component is illegal -> %d\n", l_tmp);
                return OPJ_FALSE;
        }

        if (l_image->numcomps != l_nb_comp) {
                opj_event_msg(p_manager, EVT_ERROR, "Error with SIZ marker: number of component is not compatible with the remaining number of parameters ( %d vs %d)\n", l_image->numcomps, l_nb_comp);
                return OPJ_FALSE;
        }

        /* testcase 4035.pdf.SIGSEGV.d8b.3375 */
        if (l_image->x0 > l_image->x1 || l_image->y0 > l_image->y1) {
                opj_event_msg(p_manager, EVT_ERROR, "Error with SIZ marker: negative image size (%d x %d)\n", l_image->x1 - l_image->x0, l_image->y1 - l_image->y0);
                return OPJ_FALSE;
        }
        /* testcase 2539.pdf.SIGFPE.706.1712 (also 3622.pdf.SIGFPE.706.2916 and 4008.pdf.SIGFPE.706.3345 and maybe more) */
        if (!(l_cp->tdx * l_cp->tdy)) {
                opj_event_msg(p_manager, EVT_ERROR, "Error with SIZ marker: invalid tile size (tdx: %d, tdy: %d)\n", l_cp->tdx, l_cp->tdy);
                return OPJ_FALSE;
        }

        /* testcase 1610.pdf.SIGSEGV.59c.681 */
        if (((OPJ_UINT64)l_image->x1) * ((OPJ_UINT64)l_image->y1) != (l_image->x1 * l_image->y1)) {
                opj_event_msg(p_manager, EVT_ERROR, "Prevent buffer overflow (x1: %d, y1: %d)", l_image->x1, l_image->y1);
                return OPJ_FALSE;
        }

#ifdef USE_JPWL
        if (l_cp->correct) {
                /* if JPWL is on, we check whether TX errors have damaged
                  too much the SIZ parameters */
                if (!(l_image->x1 * l_image->y1)) {
                        opj_event_msg(p_manager, EVT_ERROR,
                                "JPWL: bad image size (%d x %d)\n",
                                l_image->x1, l_image->y1);
                        if (!JPWL_ASSUME || JPWL_ASSUME) {
                                opj_event_msg(p_manager, EVT_ERROR, "JPWL: giving up\n");
                                return OPJ_FALSE;
                        }
                }

        /* FIXME check previously in the function so why keep this piece of code ? Need by the norm ?
                if (l_image->numcomps != ((len - 38) / 3)) {
                        opj_event_msg(p_manager, JPWL_ASSUME ? EVT_WARNING : EVT_ERROR,
                                "JPWL: Csiz is %d => space in SIZ only for %d comps.!!!\n",
                                l_image->numcomps, ((len - 38) / 3));
                        if (!JPWL_ASSUME) {
                                opj_event_msg(p_manager, EVT_ERROR, "JPWL: giving up\n");
                                return OPJ_FALSE;
                        }
        */              /* we try to correct */
        /*              opj_event_msg(p_manager, EVT_WARNING, "- trying to adjust this\n");
                        if (l_image->numcomps < ((len - 38) / 3)) {
                                len = 38 + 3 * l_image->numcomps;
                                opj_event_msg(p_manager, EVT_WARNING, "- setting Lsiz to %d => HYPOTHESIS!!!\n",
                                        len);
                        } else {
                                l_image->numcomps = ((len - 38) / 3);
                                opj_event_msg(p_manager, EVT_WARNING, "- setting Csiz to %d => HYPOTHESIS!!!\n",
                                        l_image->numcomps);
                        }
                }
        */

                /* update components number in the jpwl_exp_comps filed */
                l_cp->exp_comps = l_image->numcomps;
        }
#endif /* USE_JPWL */

        /* Allocate the resulting image components */
        l_image->comps = (opj_image_comp_t*) opj_calloc(l_image->numcomps, sizeof(opj_image_comp_t));
        if (l_image->comps == 00){
                l_image->numcomps = 0;
                opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to take in charge SIZ marker\n");
                return OPJ_FALSE;
        }

        memset(l_image->comps,0,l_image->numcomps * sizeof(opj_image_comp_t));
        l_img_comp = l_image->comps;

        /* Read the component information */
        for (i = 0; i < l_image->numcomps; ++i){
                OPJ_UINT32 tmp;
                opj_read_bytes(p_header_data,&tmp,1);   /* Ssiz_i */
                ++p_header_data;
                l_img_comp->prec = (tmp & 0x7f) + 1;
                l_img_comp->sgnd = tmp >> 7;
                opj_read_bytes(p_header_data,&tmp,1);   /* XRsiz_i */
                ++p_header_data;
                l_img_comp->dx = (OPJ_UINT32)tmp; /* should be between 1 and 255 */
                opj_read_bytes(p_header_data,&tmp,1);   /* YRsiz_i */
                ++p_header_data;
                l_img_comp->dy = (OPJ_UINT32)tmp; /* should be between 1 and 255 */
                if( l_img_comp->dx < 1 || l_img_comp->dx > 255 ||
                    l_img_comp->dy < 1 || l_img_comp->dy > 255 ) {
                    opj_event_msg(p_manager, EVT_ERROR,
                                  "Invalid values for comp = %d : dx=%u dy=%u\n (should be between 1 and 255 according the JPEG2000 norm)",
                                  i, l_img_comp->dx, l_img_comp->dy);
                    return OPJ_FALSE;
                }

#ifdef USE_JPWL
                if (l_cp->correct) {
                /* if JPWL is on, we check whether TX errors have damaged
                        too much the SIZ parameters, again */
                        if (!(l_image->comps[i].dx * l_image->comps[i].dy)) {
                                opj_event_msg(p_manager, JPWL_ASSUME ? EVT_WARNING : EVT_ERROR,
                                        "JPWL: bad XRsiz_%d/YRsiz_%d (%d x %d)\n",
                                        i, i, l_image->comps[i].dx, l_image->comps[i].dy);
                                if (!JPWL_ASSUME) {
                                        opj_event_msg(p_manager, EVT_ERROR, "JPWL: giving up\n");
                                        return OPJ_FALSE;
                                }
                                /* we try to correct */
                                opj_event_msg(p_manager, EVT_WARNING, "- trying to adjust them\n");
                                if (!l_image->comps[i].dx) {
                                        l_image->comps[i].dx = 1;
                                        opj_event_msg(p_manager, EVT_WARNING, "- setting XRsiz_%d to %d => HYPOTHESIS!!!\n",
                                                i, l_image->comps[i].dx);
                                }
                                if (!l_image->comps[i].dy) {
                                        l_image->comps[i].dy = 1;
                                        opj_event_msg(p_manager, EVT_WARNING, "- setting YRsiz_%d to %d => HYPOTHESIS!!!\n",
                                                i, l_image->comps[i].dy);
                                }
                        }
                }
#endif /* USE_JPWL */
                l_img_comp->resno_decoded = 0;                                                          /* number of resolution decoded */
                l_img_comp->factor = l_cp->m_specific_param.m_dec.m_reduce; /* reducing factor per component */
                ++l_img_comp;
        }

        /* Compute the number of tiles */
        l_cp->tw = (OPJ_UINT32)opj_int_ceildiv((OPJ_INT32)(l_image->x1 - l_cp->tx0), (OPJ_INT32)l_cp->tdx);
        l_cp->th = (OPJ_UINT32)opj_int_ceildiv((OPJ_INT32)(l_image->y1 - l_cp->ty0), (OPJ_INT32)l_cp->tdy);

        /* Check that the number of tiles is valid */
        if (l_cp->tw == 0 || l_cp->th == 0 || l_cp->tw > 65535 / l_cp->th) {
            opj_event_msg(  p_manager, EVT_ERROR, 
                            "Invalid number of tiles : %u x %u (maximum fixed by jpeg2000 norm is 65535 tiles)\n",
                            l_cp->tw, l_cp->th);
            return OPJ_FALSE;
        }
        l_nb_tiles = l_cp->tw * l_cp->th;

        /* Define the tiles which will be decoded */
        if (p_j2k->m_specific_param.m_decoder.m_discard_tiles) {
                p_j2k->m_specific_param.m_decoder.m_start_tile_x = (p_j2k->m_specific_param.m_decoder.m_start_tile_x - l_cp->tx0) / l_cp->tdx;
                p_j2k->m_specific_param.m_decoder.m_start_tile_y = (p_j2k->m_specific_param.m_decoder.m_start_tile_y - l_cp->ty0) / l_cp->tdy;
                p_j2k->m_specific_param.m_decoder.m_end_tile_x = (OPJ_UINT32)opj_int_ceildiv((OPJ_INT32)(p_j2k->m_specific_param.m_decoder.m_end_tile_x - l_cp->tx0), (OPJ_INT32)l_cp->tdx);
                p_j2k->m_specific_param.m_decoder.m_end_tile_y = (OPJ_UINT32)opj_int_ceildiv((OPJ_INT32)(p_j2k->m_specific_param.m_decoder.m_end_tile_y - l_cp->ty0), (OPJ_INT32)l_cp->tdy);
        }
        else {
                p_j2k->m_specific_param.m_decoder.m_start_tile_x = 0;
                p_j2k->m_specific_param.m_decoder.m_start_tile_y = 0;
                p_j2k->m_specific_param.m_decoder.m_end_tile_x = l_cp->tw;
                p_j2k->m_specific_param.m_decoder.m_end_tile_y = l_cp->th;
        }

#ifdef USE_JPWL
        if (l_cp->correct) {
                /* if JPWL is on, we check whether TX errors have damaged
                  too much the SIZ parameters */
                if ((l_cp->tw < 1) || (l_cp->th < 1) || (l_cp->tw > l_cp->max_tiles) || (l_cp->th > l_cp->max_tiles)) {
                        opj_event_msg(p_manager, JPWL_ASSUME ? EVT_WARNING : EVT_ERROR,
                                "JPWL: bad number of tiles (%d x %d)\n",
                                l_cp->tw, l_cp->th);
                        if (!JPWL_ASSUME) {
                                opj_event_msg(p_manager, EVT_ERROR, "JPWL: giving up\n");
                                return OPJ_FALSE;
                        }
                        /* we try to correct */
                        opj_event_msg(p_manager, EVT_WARNING, "- trying to adjust them\n");
                        if (l_cp->tw < 1) {
                                l_cp->tw= 1;
                                opj_event_msg(p_manager, EVT_WARNING, "- setting %d tiles in x => HYPOTHESIS!!!\n",
                                                l_cp->tw);
                        }
                        if (l_cp->tw > l_cp->max_tiles) {
                                l_cp->tw= 1;
                                opj_event_msg(p_manager, EVT_WARNING, "- too large x, increase expectance of %d\n"
                                        "- setting %d tiles in x => HYPOTHESIS!!!\n",
                                        l_cp->max_tiles, l_cp->tw);
                        }
                        if (l_cp->th < 1) {
                                l_cp->th= 1;
                                opj_event_msg(p_manager, EVT_WARNING, "- setting %d tiles in y => HYPOTHESIS!!!\n",
                                                l_cp->th);
                        }
                        if (l_cp->th > l_cp->max_tiles) {
                                l_cp->th= 1;
                                opj_event_msg(p_manager, EVT_WARNING, "- too large y, increase expectance of %d to continue\n",
                                        "- setting %d tiles in y => HYPOTHESIS!!!\n",
                                        l_cp->max_tiles, l_cp->th);
                        }
                }
        }
#endif /* USE_JPWL */

        /* memory allocations */
        l_cp->tcps = (opj_tcp_t*) opj_calloc(l_nb_tiles, sizeof(opj_tcp_t));
        if (l_cp->tcps == 00) {
                opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to take in charge SIZ marker\n");
                return OPJ_FALSE;
        }
        memset(l_cp->tcps,0,l_nb_tiles*sizeof(opj_tcp_t));

#ifdef USE_JPWL
        if (l_cp->correct) {
                if (!l_cp->tcps) {
                        opj_event_msg(p_manager, JPWL_ASSUME ? EVT_WARNING : EVT_ERROR,
                                "JPWL: could not alloc tcps field of cp\n");
                        if (!JPWL_ASSUME || JPWL_ASSUME) {
                                opj_event_msg(p_manager, EVT_ERROR, "JPWL: giving up\n");
                                return OPJ_FALSE;
                        }
                }
        }
#endif /* USE_JPWL */

        p_j2k->m_specific_param.m_decoder.m_default_tcp->tccps =
                        (opj_tccp_t*) opj_calloc(l_image->numcomps, sizeof(opj_tccp_t));
        if(p_j2k->m_specific_param.m_decoder.m_default_tcp->tccps  == 00) {
                opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to take in charge SIZ marker\n");
                return OPJ_FALSE;
        }
        memset(p_j2k->m_specific_param.m_decoder.m_default_tcp->tccps ,0,l_image->numcomps*sizeof(opj_tccp_t));

        p_j2k->m_specific_param.m_decoder.m_default_tcp->m_mct_records =
                        (opj_mct_data_t*)opj_malloc(OPJ_J2K_MCT_DEFAULT_NB_RECORDS * sizeof(opj_mct_data_t));

        if (! p_j2k->m_specific_param.m_decoder.m_default_tcp->m_mct_records) {
                opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to take in charge SIZ marker\n");
                return OPJ_FALSE;
        }
        memset(p_j2k->m_specific_param.m_decoder.m_default_tcp->m_mct_records,0,OPJ_J2K_MCT_DEFAULT_NB_RECORDS * sizeof(opj_mct_data_t));
        p_j2k->m_specific_param.m_decoder.m_default_tcp->m_nb_max_mct_records = OPJ_J2K_MCT_DEFAULT_NB_RECORDS;

        p_j2k->m_specific_param.m_decoder.m_default_tcp->m_mcc_records =
                        (opj_simple_mcc_decorrelation_data_t*)
                        opj_malloc(OPJ_J2K_MCC_DEFAULT_NB_RECORDS * sizeof(opj_simple_mcc_decorrelation_data_t));

        if (! p_j2k->m_specific_param.m_decoder.m_default_tcp->m_mcc_records) {
                opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to take in charge SIZ marker\n");
                return OPJ_FALSE;
        }
        memset(p_j2k->m_specific_param.m_decoder.m_default_tcp->m_mcc_records,0,OPJ_J2K_MCC_DEFAULT_NB_RECORDS * sizeof(opj_simple_mcc_decorrelation_data_t));
        p_j2k->m_specific_param.m_decoder.m_default_tcp->m_nb_max_mcc_records = OPJ_J2K_MCC_DEFAULT_NB_RECORDS;

        /* set up default dc level shift */
        for (i=0;i<l_image->numcomps;++i) {
                if (! l_image->comps[i].sgnd) {
                        p_j2k->m_specific_param.m_decoder.m_default_tcp->tccps[i].m_dc_level_shift = 1 << (l_image->comps[i].prec - 1);
                }
        }

        l_current_tile_param = l_cp->tcps;
        for     (i = 0; i < l_nb_tiles; ++i) {
                l_current_tile_param->tccps = (opj_tccp_t*) opj_malloc(l_image->numcomps * sizeof(opj_tccp_t));
                if (l_current_tile_param->tccps == 00) {
                        opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to take in charge SIZ marker\n");
                        return OPJ_FALSE;
                }
                memset(l_current_tile_param->tccps,0,l_image->numcomps * sizeof(opj_tccp_t));

                ++l_current_tile_param;
        }

        p_j2k->m_specific_param.m_decoder.m_state =  J2K_STATE_MH; /* FIXME J2K_DEC_STATE_MH; */
        opj_image_comp_header_update(l_image,l_cp);

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_write_com(     opj_j2k_t *p_j2k,
                                                        opj_stream_private_t *p_stream,
                                                        opj_event_mgr_t * p_manager
                            )
{
        OPJ_UINT32 l_comment_size;
        OPJ_UINT32 l_total_com_size;
        const OPJ_CHAR *l_comment;
        OPJ_BYTE * l_current_ptr = 00;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_stream != 00);
        assert(p_manager != 00);

        l_comment = p_j2k->m_cp.comment;
        l_comment_size = (OPJ_UINT32)strlen(l_comment);
        l_total_com_size = l_comment_size + 6;

        if (l_total_com_size > p_j2k->m_specific_param.m_encoder.m_header_tile_data_size) {
                OPJ_BYTE *new_header_tile_data = (OPJ_BYTE *) opj_realloc(p_j2k->m_specific_param.m_encoder.m_header_tile_data, l_total_com_size);
                if (! new_header_tile_data) {
                        opj_free(p_j2k->m_specific_param.m_encoder.m_header_tile_data);
                        p_j2k->m_specific_param.m_encoder.m_header_tile_data = NULL;
                        p_j2k->m_specific_param.m_encoder.m_header_tile_data_size = 0;
                        opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to write the COM marker\n");
                        return OPJ_FALSE;
                }
                p_j2k->m_specific_param.m_encoder.m_header_tile_data = new_header_tile_data;
                p_j2k->m_specific_param.m_encoder.m_header_tile_data_size = l_total_com_size;
        }

        l_current_ptr = p_j2k->m_specific_param.m_encoder.m_header_tile_data;

        opj_write_bytes(l_current_ptr,J2K_MS_COM , 2);  /* COM */
        l_current_ptr+=2;

        opj_write_bytes(l_current_ptr,l_total_com_size - 2 , 2);        /* L_COM */
        l_current_ptr+=2;

        opj_write_bytes(l_current_ptr,1 , 2);   /* General use (IS 8859-15:1999 (Latin) values) */
        l_current_ptr+=2;

        memcpy( l_current_ptr,l_comment,l_comment_size);

        if (opj_stream_write_data(p_stream,p_j2k->m_specific_param.m_encoder.m_header_tile_data,l_total_com_size,p_manager) != l_total_com_size) {
                return OPJ_FALSE;
        }

        return OPJ_TRUE;
}

/**
 * Reads a COM marker (comments)
 * @param       p_j2k           the jpeg2000 file codec.
 * @param       p_header_data   the data contained in the COM box.
 * @param       p_header_size   the size of the data contained in the COM marker.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_read_com (  opj_j2k_t *p_j2k,
                                    OPJ_BYTE * p_header_data,
                                    OPJ_UINT32 p_header_size,
                                    opj_event_mgr_t * p_manager
                                    )
{
        /* preconditions */
        assert(p_j2k != 00);
        assert(p_manager != 00);
        assert(p_header_data != 00);
  (void)p_header_size;

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_write_cod(     opj_j2k_t *p_j2k,
                                                        opj_stream_private_t *p_stream,
                                                        opj_event_mgr_t * p_manager )
{
        opj_cp_t *l_cp = 00;
        opj_tcp_t *l_tcp = 00;
        OPJ_UINT32 l_code_size,l_remaining_size;
        OPJ_BYTE * l_current_data = 00;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_manager != 00);
        assert(p_stream != 00);

        l_cp = &(p_j2k->m_cp);
        l_tcp = &l_cp->tcps[p_j2k->m_current_tile_number];
        l_code_size = 9 + opj_j2k_get_SPCod_SPCoc_size(p_j2k,p_j2k->m_current_tile_number,0);
        l_remaining_size = l_code_size;

        if (l_code_size > p_j2k->m_specific_param.m_encoder.m_header_tile_data_size) {
                OPJ_BYTE *new_header_tile_data = (OPJ_BYTE *) opj_realloc(p_j2k->m_specific_param.m_encoder.m_header_tile_data, l_code_size);
                if (! new_header_tile_data) {
                        opj_free(p_j2k->m_specific_param.m_encoder.m_header_tile_data);
                        p_j2k->m_specific_param.m_encoder.m_header_tile_data = NULL;
                        p_j2k->m_specific_param.m_encoder.m_header_tile_data_size = 0;
                        opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to write COD marker\n");
                        return OPJ_FALSE;
                }
                p_j2k->m_specific_param.m_encoder.m_header_tile_data = new_header_tile_data;
                p_j2k->m_specific_param.m_encoder.m_header_tile_data_size = l_code_size;
        }

        l_current_data = p_j2k->m_specific_param.m_encoder.m_header_tile_data;

        opj_write_bytes(l_current_data,J2K_MS_COD,2);           /* COD */
        l_current_data += 2;

        opj_write_bytes(l_current_data,l_code_size-2,2);        /* L_COD */
        l_current_data += 2;

        opj_write_bytes(l_current_data,l_tcp->csty,1);          /* Scod */
        ++l_current_data;

        opj_write_bytes(l_current_data,l_tcp->prg,1);           /* SGcod (A) */
        ++l_current_data;

        opj_write_bytes(l_current_data,l_tcp->numlayers,2);     /* SGcod (B) */
        l_current_data+=2;

        opj_write_bytes(l_current_data,l_tcp->mct,1);           /* SGcod (C) */
        ++l_current_data;

        l_remaining_size -= 9;

        if (! opj_j2k_write_SPCod_SPCoc(p_j2k,p_j2k->m_current_tile_number,0,l_current_data,&l_remaining_size,p_manager)) {
                opj_event_msg(p_manager, EVT_ERROR, "Error writing COD marker\n");
                return OPJ_FALSE;
        }

        if (l_remaining_size != 0) {
                opj_event_msg(p_manager, EVT_ERROR, "Error writing COD marker\n");
                return OPJ_FALSE;
        }

        if (opj_stream_write_data(p_stream,p_j2k->m_specific_param.m_encoder.m_header_tile_data,l_code_size,p_manager) != l_code_size) {
                return OPJ_FALSE;
        }

        return OPJ_TRUE;
}

/**
 * Reads a COD marker (Coding Styke defaults)
 * @param       p_header_data   the data contained in the COD box.
 * @param       p_j2k                   the jpeg2000 codec.
 * @param       p_header_size   the size of the data contained in the COD marker.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_read_cod (  opj_j2k_t *p_j2k,
                                    OPJ_BYTE * p_header_data,
                                    OPJ_UINT32 p_header_size,
                                    opj_event_mgr_t * p_manager
                                    )
{
        /* loop */
        OPJ_UINT32 i;
        OPJ_UINT32 l_tmp;
        opj_cp_t *l_cp = 00;
        opj_tcp_t *l_tcp = 00;
        opj_image_t *l_image = 00;

        /* preconditions */
        assert(p_header_data != 00);
        assert(p_j2k != 00);
        assert(p_manager != 00);

        l_image = p_j2k->m_private_image;
        l_cp = &(p_j2k->m_cp);

        /* If we are in the first tile-part header of the current tile */
        l_tcp = (p_j2k->m_specific_param.m_decoder.m_state == J2K_STATE_TPH) ?
                                &l_cp->tcps[p_j2k->m_current_tile_number] :
                                p_j2k->m_specific_param.m_decoder.m_default_tcp;

        /* Make sure room is sufficient */
        if (p_header_size < 5) {
                opj_event_msg(p_manager, EVT_ERROR, "Error reading COD marker\n");
                return OPJ_FALSE;
        }

        opj_read_bytes(p_header_data,&l_tcp->csty,1);           /* Scod */
        ++p_header_data;
        opj_read_bytes(p_header_data,&l_tmp,1);                         /* SGcod (A) */
        ++p_header_data;
        l_tcp->prg = (OPJ_PROG_ORDER) l_tmp;
        opj_read_bytes(p_header_data,&l_tcp->numlayers,2);      /* SGcod (B) */
        p_header_data+=2;

        /* If user didn't set a number layer to decode take the max specify in the codestream. */
        if      (l_cp->m_specific_param.m_dec.m_layer) {
                l_tcp->num_layers_to_decode = l_cp->m_specific_param.m_dec.m_layer;
        }
        else {
                l_tcp->num_layers_to_decode = l_tcp->numlayers;
        }

        opj_read_bytes(p_header_data,&l_tcp->mct,1);            /* SGcod (C) */
        ++p_header_data;

        p_header_size -= 5;
        for     (i = 0; i < l_image->numcomps; ++i) {
                l_tcp->tccps[i].csty = l_tcp->csty & J2K_CCP_CSTY_PRT;
        }

        if (! opj_j2k_read_SPCod_SPCoc(p_j2k,0,p_header_data,&p_header_size,p_manager)) {
                opj_event_msg(p_manager, EVT_ERROR, "Error reading COD marker\n");
                return OPJ_FALSE;
        }

        if (p_header_size != 0) {
                opj_event_msg(p_manager, EVT_ERROR, "Error reading COD marker\n");
                return OPJ_FALSE;
        }

        /* Apply the coding style to other components of the current tile or the m_default_tcp*/
        opj_j2k_copy_tile_component_parameters(p_j2k);

        /* Index */
#ifdef WIP_REMOVE_MSD
        if (p_j2k->cstr_info) {
                /*opj_codestream_info_t *l_cstr_info = p_j2k->cstr_info;*/
                p_j2k->cstr_info->prog = l_tcp->prg;
                p_j2k->cstr_info->numlayers = l_tcp->numlayers;
                p_j2k->cstr_info->numdecompos = (OPJ_INT32*) opj_malloc(l_image->numcomps * sizeof(OPJ_UINT32));
                for     (i = 0; i < l_image->numcomps; ++i) {
                        p_j2k->cstr_info->numdecompos[i] = l_tcp->tccps[i].numresolutions - 1;
                }
        }
#endif

        return OPJ_TRUE;
}

#if 0
OPJ_BOOL opj_j2k_write_coc( opj_j2k_t *p_j2k,
                                                OPJ_UINT32 p_comp_no,
                                                opj_stream_private_t *p_stream,
                                                opj_event_mgr_t * p_manager )
{
        OPJ_UINT32 l_coc_size,l_remaining_size;
        OPJ_UINT32 l_comp_room;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_manager != 00);
        assert(p_stream != 00);

        l_comp_room = (p_j2k->m_private_image->numcomps <= 256) ? 1 : 2;

        l_coc_size = 5 + l_comp_room + opj_j2k_get_SPCod_SPCoc_size(p_j2k,p_j2k->m_current_tile_number,p_comp_no);

        if (l_coc_size > p_j2k->m_specific_param.m_encoder.m_header_tile_data_size) {
                OPJ_BYTE *new_header_tile_data;
                /*p_j2k->m_specific_param.m_encoder.m_header_tile_data
                        = (OPJ_BYTE*)opj_realloc(
                                p_j2k->m_specific_param.m_encoder.m_header_tile_data,
                                l_coc_size);*/

                new_header_tile_data = (OPJ_BYTE *) opj_realloc(p_j2k->m_specific_param.m_encoder.m_header_tile_data, l_coc_size);
                if (! new_header_tile_data) {
                        opj_free(p_j2k->m_specific_param.m_encoder.m_header_tile_data);
                        p_j2k->m_specific_param.m_encoder.m_header_tile_data = NULL;
                        p_j2k->m_specific_param.m_encoder.m_header_tile_data_size = 0;
                        opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to write COC marker\n");
                        return OPJ_FALSE;
                }
                p_j2k->m_specific_param.m_encoder.m_header_tile_data = new_header_tile_data;
                p_j2k->m_specific_param.m_encoder.m_header_tile_data_size = l_coc_size;
        }

        opj_j2k_write_coc_in_memory(p_j2k,p_comp_no,p_j2k->m_specific_param.m_encoder.m_header_tile_data,&l_remaining_size,p_manager);

        if (opj_stream_write_data(p_stream,p_j2k->m_specific_param.m_encoder.m_header_tile_data,l_coc_size,p_manager) != l_coc_size) {
                return OPJ_FALSE;
        }

        return OPJ_TRUE;
}
#endif

#if 0
void opj_j2k_write_coc_in_memory(   opj_j2k_t *p_j2k,
                                                OPJ_UINT32 p_comp_no,
                                                OPJ_BYTE * p_data,
                                                OPJ_UINT32 * p_data_written,
                                                opj_event_mgr_t * p_manager
                                    )
{
        opj_cp_t *l_cp = 00;
        opj_tcp_t *l_tcp = 00;
        OPJ_UINT32 l_coc_size,l_remaining_size;
        OPJ_BYTE * l_current_data = 00;
        opj_image_t *l_image = 00;
        OPJ_UINT32 l_comp_room;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_manager != 00);

        l_cp = &(p_j2k->m_cp);
        l_tcp = &l_cp->tcps[p_j2k->m_current_tile_number];
        l_image = p_j2k->m_private_image;
        l_comp_room = (l_image->numcomps <= 256) ? 1 : 2;

        l_coc_size = 5 + l_comp_room + opj_j2k_get_SPCod_SPCoc_size(p_j2k,p_j2k->m_current_tile_number,p_comp_no);
        l_remaining_size = l_coc_size;

        l_current_data = p_data;

        opj_write_bytes(l_current_data,J2K_MS_COC,2);                           /* COC */
        l_current_data += 2;

        opj_write_bytes(l_current_data,l_coc_size-2,2);                         /* L_COC */
        l_current_data += 2;

        opj_write_bytes(l_current_data,p_comp_no, l_comp_room);         /* Ccoc */
        l_current_data+=l_comp_room;

        opj_write_bytes(l_current_data, l_tcp->tccps[p_comp_no].csty, 1);               /* Scoc */
        ++l_current_data;

        l_remaining_size -= (5 + l_comp_room);
        opj_j2k_write_SPCod_SPCoc(p_j2k,p_j2k->m_current_tile_number,0,l_current_data,&l_remaining_size,p_manager);
        * p_data_written = l_coc_size;
}
#endif

OPJ_UINT32 opj_j2k_get_max_coc_size(opj_j2k_t *p_j2k)
{
        OPJ_UINT32 i,j;
        OPJ_UINT32 l_nb_comp;
        OPJ_UINT32 l_nb_tiles;
        OPJ_UINT32 l_max = 0;

        /* preconditions */

        l_nb_tiles = p_j2k->m_cp.tw * p_j2k->m_cp.th ;
        l_nb_comp = p_j2k->m_private_image->numcomps;

        for (i=0;i<l_nb_tiles;++i) {
                for (j=0;j<l_nb_comp;++j) {
                        l_max = opj_uint_max(l_max,opj_j2k_get_SPCod_SPCoc_size(p_j2k,i,j));
                }
        }

        return 6 + l_max;
}

/**
 * Reads a COC marker (Coding Style Component)
 * @param       p_header_data   the data contained in the COC box.
 * @param       p_j2k                   the jpeg2000 codec.
 * @param       p_header_size   the size of the data contained in the COC marker.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_read_coc (  opj_j2k_t *p_j2k,
                                    OPJ_BYTE * p_header_data,
                                    OPJ_UINT32 p_header_size,
                                    opj_event_mgr_t * p_manager
                                    )
{
        opj_cp_t *l_cp = NULL;
        opj_tcp_t *l_tcp = NULL;
        opj_image_t *l_image = NULL;
        OPJ_UINT32 l_comp_room;
        OPJ_UINT32 l_comp_no;

        /* preconditions */
        assert(p_header_data != 00);
        assert(p_j2k != 00);
        assert(p_manager != 00);

        l_cp = &(p_j2k->m_cp);
        l_tcp = (p_j2k->m_specific_param.m_decoder.m_state == J2K_STATE_TPH ) ? /*FIXME J2K_DEC_STATE_TPH*/
                                &l_cp->tcps[p_j2k->m_current_tile_number] :
                                p_j2k->m_specific_param.m_decoder.m_default_tcp;
        l_image = p_j2k->m_private_image;

        l_comp_room = l_image->numcomps <= 256 ? 1 : 2;

        /* make sure room is sufficient*/
        if (p_header_size < l_comp_room + 1) {
                opj_event_msg(p_manager, EVT_ERROR, "Error reading COC marker\n");
                return OPJ_FALSE;
        }
        p_header_size -= l_comp_room + 1;

        opj_read_bytes(p_header_data,&l_comp_no,l_comp_room);                   /* Ccoc */
        p_header_data += l_comp_room;
        if (l_comp_no >= l_image->numcomps) {
                opj_event_msg(p_manager, EVT_ERROR, "Error reading COC marker (bad number of components)\n");
                return OPJ_FALSE;
        }

        opj_read_bytes(p_header_data,&l_tcp->tccps[l_comp_no].csty,1);                  /* Scoc */
        ++p_header_data ;

        if (! opj_j2k_read_SPCod_SPCoc(p_j2k,l_comp_no,p_header_data,&p_header_size,p_manager)) {
                opj_event_msg(p_manager, EVT_ERROR, "Error reading COC marker\n");
                return OPJ_FALSE;
        }

        if (p_header_size != 0) {
                opj_event_msg(p_manager, EVT_ERROR, "Error reading COC marker\n");
                return OPJ_FALSE;
        }
        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_write_qcd(     opj_j2k_t *p_j2k,
                                                        opj_stream_private_t *p_stream,
                                                        opj_event_mgr_t * p_manager
                            )
{
        OPJ_UINT32 l_qcd_size,l_remaining_size;
        OPJ_BYTE * l_current_data = 00;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_manager != 00);
        assert(p_stream != 00);

        l_qcd_size = 4 + opj_j2k_get_SQcd_SQcc_size(p_j2k,p_j2k->m_current_tile_number,0);
        l_remaining_size = l_qcd_size;

        if (l_qcd_size > p_j2k->m_specific_param.m_encoder.m_header_tile_data_size) {
                OPJ_BYTE *new_header_tile_data = (OPJ_BYTE *) opj_realloc(p_j2k->m_specific_param.m_encoder.m_header_tile_data, l_qcd_size);
                if (! new_header_tile_data) {
                        opj_free(p_j2k->m_specific_param.m_encoder.m_header_tile_data);
                        p_j2k->m_specific_param.m_encoder.m_header_tile_data = NULL;
                        p_j2k->m_specific_param.m_encoder.m_header_tile_data_size = 0;
                        opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to write QCD marker\n");
                        return OPJ_FALSE;
                }
                p_j2k->m_specific_param.m_encoder.m_header_tile_data = new_header_tile_data;
                p_j2k->m_specific_param.m_encoder.m_header_tile_data_size = l_qcd_size;
        }

        l_current_data = p_j2k->m_specific_param.m_encoder.m_header_tile_data;

        opj_write_bytes(l_current_data,J2K_MS_QCD,2);           /* QCD */
        l_current_data += 2;

        opj_write_bytes(l_current_data,l_qcd_size-2,2);         /* L_QCD */
        l_current_data += 2;

        l_remaining_size -= 4;

        if (! opj_j2k_write_SQcd_SQcc(p_j2k,p_j2k->m_current_tile_number,0,l_current_data,&l_remaining_size,p_manager)) {
                opj_event_msg(p_manager, EVT_ERROR, "Error writing QCD marker\n");
                return OPJ_FALSE;
        }

        if (l_remaining_size != 0) {
                opj_event_msg(p_manager, EVT_ERROR, "Error writing QCD marker\n");
                return OPJ_FALSE;
        }

        if (opj_stream_write_data(p_stream, p_j2k->m_specific_param.m_encoder.m_header_tile_data,l_qcd_size,p_manager) != l_qcd_size) {
                return OPJ_FALSE;
        }

        return OPJ_TRUE;
}

/**
 * Reads a QCD marker (Quantization defaults)
 * @param       p_header_data   the data contained in the QCD box.
 * @param       p_j2k                   the jpeg2000 codec.
 * @param       p_header_size   the size of the data contained in the QCD marker.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_read_qcd (  opj_j2k_t *p_j2k,
                                    OPJ_BYTE * p_header_data,
                                    OPJ_UINT32 p_header_size,
                                    opj_event_mgr_t * p_manager
                                    )
{
        /* preconditions */
        assert(p_header_data != 00);
        assert(p_j2k != 00);
        assert(p_manager != 00);

        if (! opj_j2k_read_SQcd_SQcc(p_j2k,0,p_header_data,&p_header_size,p_manager)) {
                opj_event_msg(p_manager, EVT_ERROR, "Error reading QCD marker\n");
                return OPJ_FALSE;
        }

        if (p_header_size != 0) {
                opj_event_msg(p_manager, EVT_ERROR, "Error reading QCD marker\n");
                return OPJ_FALSE;
        }

        /* Apply the quantization parameters to other components of the current tile or the m_default_tcp */
        opj_j2k_copy_tile_quantization_parameters(p_j2k);

        return OPJ_TRUE;
}

#if 0
OPJ_BOOL opj_j2k_write_qcc(     opj_j2k_t *p_j2k,
                                                OPJ_UINT32 p_comp_no,
                                                opj_stream_private_t *p_stream,
                                                opj_event_mgr_t * p_manager
                            )
{
        OPJ_UINT32 l_qcc_size,l_remaining_size;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_manager != 00);
        assert(p_stream != 00);

        l_qcc_size = 5 + opj_j2k_get_SQcd_SQcc_size(p_j2k,p_j2k->m_current_tile_number,p_comp_no);
        l_qcc_size += p_j2k->m_private_image->numcomps <= 256 ? 0:1;
        l_remaining_size = l_qcc_size;

        if (l_qcc_size > p_j2k->m_specific_param.m_encoder.m_header_tile_data_size) {
                OPJ_BYTE *new_header_tile_data = (OPJ_BYTE *) opj_realloc(p_j2k->m_specific_param.m_encoder.m_header_tile_data, l_qcc_size);
                if (! new_header_tile_data) {
                        opj_free(p_j2k->m_specific_param.m_encoder.m_header_tile_data);
                        p_j2k->m_specific_param.m_encoder.m_header_tile_data = NULL;
                        p_j2k->m_specific_param.m_encoder.m_header_tile_data_size = 0;
                        opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to write QCC marker\n");
                        return OPJ_FALSE;
                }
                p_j2k->m_specific_param.m_encoder.m_header_tile_data = new_header_tile_data;
                p_j2k->m_specific_param.m_encoder.m_header_tile_data_size = l_qcc_size;
        }

        opj_j2k_write_qcc_in_memory(p_j2k,p_comp_no,p_j2k->m_specific_param.m_encoder.m_header_tile_data,&l_remaining_size,p_manager);

        if (opj_stream_write_data(p_stream,p_j2k->m_specific_param.m_encoder.m_header_tile_data,l_qcc_size,p_manager) != l_qcc_size) {
                return OPJ_FALSE;
        }

        return OPJ_TRUE;
}
#endif

#if 0
void opj_j2k_write_qcc_in_memory(   opj_j2k_t *p_j2k,
                                                                OPJ_UINT32 p_comp_no,
                                                                OPJ_BYTE * p_data,
                                                                OPJ_UINT32 * p_data_written,
                                                                opj_event_mgr_t * p_manager
                                    )
{
        OPJ_UINT32 l_qcc_size,l_remaining_size;
        OPJ_BYTE * l_current_data = 00;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_manager != 00);

        l_qcc_size = 6 + opj_j2k_get_SQcd_SQcc_size(p_j2k,p_j2k->m_current_tile_number,p_comp_no);
        l_remaining_size = l_qcc_size;

        l_current_data = p_data;

        opj_write_bytes(l_current_data,J2K_MS_QCC,2);           /* QCC */
        l_current_data += 2;

        if (p_j2k->m_private_image->numcomps <= 256) {
                --l_qcc_size;

                opj_write_bytes(l_current_data,l_qcc_size-2,2);         /* L_QCC */
                l_current_data += 2;

                opj_write_bytes(l_current_data, p_comp_no, 1);  /* Cqcc */
                ++l_current_data;

                /* in the case only one byte is sufficient the last byte allocated is useless -> still do -6 for available */
                l_remaining_size -= 6;
        }
        else {
                opj_write_bytes(l_current_data,l_qcc_size-2,2);         /* L_QCC */
                l_current_data += 2;

                opj_write_bytes(l_current_data, p_comp_no, 2);  /* Cqcc */
                l_current_data+=2;

                l_remaining_size -= 6;
        }

        opj_j2k_write_SQcd_SQcc(p_j2k,p_j2k->m_current_tile_number,p_comp_no,l_current_data,&l_remaining_size,p_manager);

        *p_data_written = l_qcc_size;
}
#endif

OPJ_UINT32 opj_j2k_get_max_qcc_size (opj_j2k_t *p_j2k)
{
        return opj_j2k_get_max_coc_size(p_j2k);
}

/**
 * Reads a QCC marker (Quantization component)
 * @param       p_header_data   the data contained in the QCC box.
 * @param       p_j2k                   the jpeg2000 codec.
 * @param       p_header_size   the size of the data contained in the QCC marker.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_read_qcc(   opj_j2k_t *p_j2k,
                                    OPJ_BYTE * p_header_data,
                                    OPJ_UINT32 p_header_size,
                                    opj_event_mgr_t * p_manager
                                    )
{
        OPJ_UINT32 l_num_comp,l_comp_no;

        /* preconditions */
        assert(p_header_data != 00);
        assert(p_j2k != 00);
        assert(p_manager != 00);

        l_num_comp = p_j2k->m_private_image->numcomps;

        if (l_num_comp <= 256) {
                if (p_header_size < 1) {
                        opj_event_msg(p_manager, EVT_ERROR, "Error reading QCC marker\n");
                        return OPJ_FALSE;
                }
                opj_read_bytes(p_header_data,&l_comp_no,1);
                ++p_header_data;
                --p_header_size;
        }
        else {
                if (p_header_size < 2) {
                        opj_event_msg(p_manager, EVT_ERROR, "Error reading QCC marker\n");
                        return OPJ_FALSE;
                }
                opj_read_bytes(p_header_data,&l_comp_no,2);
                p_header_data+=2;
                p_header_size-=2;
        }

#ifdef USE_JPWL
        if (p_j2k->m_cp.correct) {

                static OPJ_UINT32 backup_compno = 0;

                /* compno is negative or larger than the number of components!!! */
                if (/*(l_comp_no < 0) ||*/ (l_comp_no >= l_num_comp)) {
                        opj_event_msg(p_manager, EVT_ERROR,
                                "JPWL: bad component number in QCC (%d out of a maximum of %d)\n",
                                l_comp_no, l_num_comp);
                        if (!JPWL_ASSUME) {
                                opj_event_msg(p_manager, EVT_ERROR, "JPWL: giving up\n");
                                return OPJ_FALSE;
                        }
                        /* we try to correct */
                        l_comp_no = backup_compno % l_num_comp;
                        opj_event_msg(p_manager, EVT_WARNING, "- trying to adjust this\n"
                                "- setting component number to %d\n",
                                l_comp_no);
                }

                /* keep your private count of tiles */
                backup_compno++;
        };
#endif /* USE_JPWL */

        if (l_comp_no >= p_j2k->m_private_image->numcomps) {
                opj_event_msg(p_manager, EVT_ERROR,
                              "Invalid component number: %d, regarding the number of components %d\n",
                              l_comp_no, p_j2k->m_private_image->numcomps);
                return OPJ_FALSE;
        }

        if (! opj_j2k_read_SQcd_SQcc(p_j2k,l_comp_no,p_header_data,&p_header_size,p_manager)) {
                opj_event_msg(p_manager, EVT_ERROR, "Error reading QCC marker\n");
                return OPJ_FALSE;
        }

        if (p_header_size != 0) {
                opj_event_msg(p_manager, EVT_ERROR, "Error reading QCC marker\n");
                return OPJ_FALSE;
        }

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_write_poc(     opj_j2k_t *p_j2k,
                                                        opj_stream_private_t *p_stream,
                                                        opj_event_mgr_t * p_manager
                            )
{
        OPJ_UINT32 l_nb_comp;
        OPJ_UINT32 l_nb_poc;
        OPJ_UINT32 l_poc_size;
        OPJ_UINT32 l_written_size = 0;
        opj_tcp_t *l_tcp = 00;
        OPJ_UINT32 l_poc_room;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_manager != 00);
        assert(p_stream != 00);

        l_tcp = &p_j2k->m_cp.tcps[p_j2k->m_current_tile_number];
        l_nb_comp = p_j2k->m_private_image->numcomps;
        l_nb_poc = 1 + l_tcp->numpocs;

        if (l_nb_comp <= 256) {
                l_poc_room = 1;
        }
        else {
                l_poc_room = 2;
        }
        l_poc_size = 4 + (5 + 2 * l_poc_room) * l_nb_poc;

        if (l_poc_size > p_j2k->m_specific_param.m_encoder.m_header_tile_data_size) {
                OPJ_BYTE *new_header_tile_data = (OPJ_BYTE *) opj_realloc(p_j2k->m_specific_param.m_encoder.m_header_tile_data, l_poc_size);
                if (! new_header_tile_data) {
                        opj_free(p_j2k->m_specific_param.m_encoder.m_header_tile_data);
                        p_j2k->m_specific_param.m_encoder.m_header_tile_data = NULL;
                        p_j2k->m_specific_param.m_encoder.m_header_tile_data_size = 0;
                        opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to write POC marker\n");
                        return OPJ_FALSE;
                }
                p_j2k->m_specific_param.m_encoder.m_header_tile_data = new_header_tile_data;
                p_j2k->m_specific_param.m_encoder.m_header_tile_data_size = l_poc_size;
        }

        opj_j2k_write_poc_in_memory(p_j2k,p_j2k->m_specific_param.m_encoder.m_header_tile_data,&l_written_size,p_manager);

        if (opj_stream_write_data(p_stream,p_j2k->m_specific_param.m_encoder.m_header_tile_data,l_poc_size,p_manager) != l_poc_size) {
                return OPJ_FALSE;
        }

        return OPJ_TRUE;
}

void opj_j2k_write_poc_in_memory(   opj_j2k_t *p_j2k,
                                                                OPJ_BYTE * p_data,
                                                                OPJ_UINT32 * p_data_written,
                                                                opj_event_mgr_t * p_manager
                                    )
{
        OPJ_UINT32 i;
        OPJ_BYTE * l_current_data = 00;
        OPJ_UINT32 l_nb_comp;
        OPJ_UINT32 l_nb_poc;
        OPJ_UINT32 l_poc_size;
        opj_image_t *l_image = 00;
        opj_tcp_t *l_tcp = 00;
        opj_tccp_t *l_tccp = 00;
        opj_poc_t *l_current_poc = 00;
        OPJ_UINT32 l_poc_room;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_manager != 00);

        l_tcp = &p_j2k->m_cp.tcps[p_j2k->m_current_tile_number];
        l_tccp = &l_tcp->tccps[0];
        l_image = p_j2k->m_private_image;
        l_nb_comp = l_image->numcomps;
        l_nb_poc = 1 + l_tcp->numpocs;

        if (l_nb_comp <= 256) {
                l_poc_room = 1;
        }
        else {
                l_poc_room = 2;
        }

        l_poc_size = 4 + (5 + 2 * l_poc_room) * l_nb_poc;

        l_current_data = p_data;

        opj_write_bytes(l_current_data,J2K_MS_POC,2);                                   /* POC  */
        l_current_data += 2;

        opj_write_bytes(l_current_data,l_poc_size-2,2);                                 /* Lpoc */
        l_current_data += 2;

        l_current_poc =  l_tcp->pocs;
        for (i = 0; i < l_nb_poc; ++i) {
                opj_write_bytes(l_current_data,l_current_poc->resno0,1);                                /* RSpoc_i */
                ++l_current_data;

                opj_write_bytes(l_current_data,l_current_poc->compno0,l_poc_room);              /* CSpoc_i */
                l_current_data+=l_poc_room;

                opj_write_bytes(l_current_data,l_current_poc->layno1,2);                                /* LYEpoc_i */
                l_current_data+=2;

                opj_write_bytes(l_current_data,l_current_poc->resno1,1);                                /* REpoc_i */
                ++l_current_data;

                opj_write_bytes(l_current_data,l_current_poc->compno1,l_poc_room);              /* CEpoc_i */
                l_current_data+=l_poc_room;

                opj_write_bytes(l_current_data,l_current_poc->prg,1);                                   /* Ppoc_i */
                ++l_current_data;

                /* change the value of the max layer according to the actual number of layers in the file, components and resolutions*/
                l_current_poc->layno1 = (OPJ_UINT32)opj_int_min((OPJ_INT32)l_current_poc->layno1, (OPJ_INT32)l_tcp->numlayers);
                l_current_poc->resno1 = (OPJ_UINT32)opj_int_min((OPJ_INT32)l_current_poc->resno1, (OPJ_INT32)l_tccp->numresolutions);
                l_current_poc->compno1 = (OPJ_UINT32)opj_int_min((OPJ_INT32)l_current_poc->compno1, (OPJ_INT32)l_nb_comp);

                ++l_current_poc;
        }

        *p_data_written = l_poc_size;
}

OPJ_UINT32 opj_j2k_get_max_poc_size(opj_j2k_t *p_j2k)
{
        opj_tcp_t * l_tcp = 00;
        OPJ_UINT32 l_nb_tiles = 0;
        OPJ_UINT32 l_max_poc = 0;
        OPJ_UINT32 i;

        l_tcp = p_j2k->m_cp.tcps;
        l_nb_tiles = p_j2k->m_cp.th * p_j2k->m_cp.tw;

        for (i=0;i<l_nb_tiles;++i) {
                l_max_poc = opj_uint_max(l_max_poc,l_tcp->numpocs);
                ++l_tcp;
        }

        ++l_max_poc;

        return 4 + 9 * l_max_poc;
}

OPJ_UINT32 opj_j2k_get_max_toc_size (opj_j2k_t *p_j2k)
{
        OPJ_UINT32 i;
        OPJ_UINT32 l_nb_tiles;
        OPJ_UINT32 l_max = 0;
        opj_tcp_t * l_tcp = 00;

        l_tcp = p_j2k->m_cp.tcps;
        l_nb_tiles = p_j2k->m_cp.tw * p_j2k->m_cp.th ;

        for (i=0;i<l_nb_tiles;++i) {
                l_max = opj_uint_max(l_max,l_tcp->m_nb_tile_parts);

                ++l_tcp;
        }

        return 12 * l_max;
}

OPJ_UINT32 opj_j2k_get_specific_header_sizes(opj_j2k_t *p_j2k)
{
        OPJ_UINT32 l_nb_bytes = 0;
        OPJ_UINT32 l_nb_comps;
        OPJ_UINT32 l_coc_bytes,l_qcc_bytes;

        l_nb_comps = p_j2k->m_private_image->numcomps - 1;
        l_nb_bytes += opj_j2k_get_max_toc_size(p_j2k);

        if (p_j2k->m_cp.m_specific_param.m_enc.m_cinema == 0) {
                l_coc_bytes = opj_j2k_get_max_coc_size(p_j2k);
                l_nb_bytes += l_nb_comps * l_coc_bytes;

                l_qcc_bytes = opj_j2k_get_max_qcc_size(p_j2k);
                l_nb_bytes += l_nb_comps * l_qcc_bytes;
        }

        l_nb_bytes += opj_j2k_get_max_poc_size(p_j2k);

        /*** DEVELOPER CORNER, Add room for your headers ***/

        return l_nb_bytes;
}

/**
 * Reads a POC marker (Progression Order Change)
 *
 * @param       p_header_data   the data contained in the POC box.
 * @param       p_j2k                   the jpeg2000 codec.
 * @param       p_header_size   the size of the data contained in the POC marker.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_read_poc (  opj_j2k_t *p_j2k,
                                    OPJ_BYTE * p_header_data,
                                    OPJ_UINT32 p_header_size,
                                    opj_event_mgr_t * p_manager
                                    )
{
        OPJ_UINT32 i, l_nb_comp, l_tmp;
        opj_image_t * l_image = 00;
        OPJ_UINT32 l_old_poc_nb, l_current_poc_nb, l_current_poc_remaining;
        OPJ_UINT32 l_chunk_size, l_comp_room;

        opj_cp_t *l_cp = 00;
        opj_tcp_t *l_tcp = 00;
        opj_poc_t *l_current_poc = 00;

        /* preconditions */
        assert(p_header_data != 00);
        assert(p_j2k != 00);
        assert(p_manager != 00);

        l_image = p_j2k->m_private_image;
        l_nb_comp = l_image->numcomps;
        if (l_nb_comp <= 256) {
                l_comp_room = 1;
        }
        else {
                l_comp_room = 2;
        }
        l_chunk_size = 5 + 2 * l_comp_room;
        l_current_poc_nb = p_header_size / l_chunk_size;
        l_current_poc_remaining = p_header_size % l_chunk_size;

        if ((l_current_poc_nb <= 0) || (l_current_poc_remaining != 0)) {
                opj_event_msg(p_manager, EVT_ERROR, "Error reading POC marker\n");
                return OPJ_FALSE;
        }

        l_cp = &(p_j2k->m_cp);
        l_tcp = (p_j2k->m_specific_param.m_decoder.m_state == J2K_STATE_TPH) ?
                                &l_cp->tcps[p_j2k->m_current_tile_number] :
                                p_j2k->m_specific_param.m_decoder.m_default_tcp;
        l_old_poc_nb = l_tcp->POC ? l_tcp->numpocs + 1 : 0;
        l_current_poc_nb += l_old_poc_nb;

        if(l_current_poc_nb >= 32)
          {
          opj_event_msg(p_manager, EVT_ERROR, "Too many POCs %d\n", l_current_poc_nb);
          return OPJ_FALSE;
          }
        assert(l_current_poc_nb < 32);

        /* now poc is in use.*/
        l_tcp->POC = 1;

        l_current_poc = &l_tcp->pocs[l_old_poc_nb];
        for     (i = l_old_poc_nb; i < l_current_poc_nb; ++i) {
                opj_read_bytes(p_header_data,&(l_current_poc->resno0),1);                               /* RSpoc_i */
                ++p_header_data;
                opj_read_bytes(p_header_data,&(l_current_poc->compno0),l_comp_room);    /* CSpoc_i */
                p_header_data+=l_comp_room;
                opj_read_bytes(p_header_data,&(l_current_poc->layno1),2);                               /* LYEpoc_i */
                /* make sure layer end is in acceptable bounds */
                l_current_poc->layno1 = opj_uint_min(l_current_poc->layno1, l_tcp->numlayers);
                p_header_data+=2;
                opj_read_bytes(p_header_data,&(l_current_poc->resno1),1);                               /* REpoc_i */
                ++p_header_data;
                opj_read_bytes(p_header_data,&(l_current_poc->compno1),l_comp_room);    /* CEpoc_i */
                p_header_data+=l_comp_room;
                opj_read_bytes(p_header_data,&l_tmp,1);                                                                 /* Ppoc_i */
                ++p_header_data;
                l_current_poc->prg = (OPJ_PROG_ORDER) l_tmp;
                /* make sure comp is in acceptable bounds */
                l_current_poc->compno1 = opj_uint_min(l_current_poc->compno1, l_nb_comp);
                ++l_current_poc;
        }

        l_tcp->numpocs = l_current_poc_nb - 1;
        return OPJ_TRUE;
}

/**
 * Reads a CRG marker (Component registration)
 *
 * @param       p_header_data   the data contained in the TLM box.
 * @param       p_j2k                   the jpeg2000 codec.
 * @param       p_header_size   the size of the data contained in the TLM marker.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_read_crg (  opj_j2k_t *p_j2k,
                                    OPJ_BYTE * p_header_data,
                                    OPJ_UINT32 p_header_size,
                                    opj_event_mgr_t * p_manager
                                    )
{
        OPJ_UINT32 l_nb_comp;
        /* preconditions */
        assert(p_header_data != 00);
        assert(p_j2k != 00);
        assert(p_manager != 00);

        l_nb_comp = p_j2k->m_private_image->numcomps;

        if (p_header_size != l_nb_comp *4) {
                opj_event_msg(p_manager, EVT_ERROR, "Error reading CRG marker\n");
                return OPJ_FALSE;
        }
        /* Do not care of this at the moment since only local variables are set here */
        /*
        for
                (i = 0; i < l_nb_comp; ++i)
        {
                opj_read_bytes(p_header_data,&l_Xcrg_i,2);                              // Xcrg_i
                p_header_data+=2;
                opj_read_bytes(p_header_data,&l_Ycrg_i,2);                              // Xcrg_i
                p_header_data+=2;
        }
        */
        return OPJ_TRUE;
}

/**
 * Reads a TLM marker (Tile Length Marker)
 *
 * @param       p_header_data   the data contained in the TLM box.
 * @param       p_j2k                   the jpeg2000 codec.
 * @param       p_header_size   the size of the data contained in the TLM marker.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_read_tlm (  opj_j2k_t *p_j2k,
                                    OPJ_BYTE * p_header_data,
                                    OPJ_UINT32 p_header_size,
                                    opj_event_mgr_t * p_manager
                                    )
{
        OPJ_UINT32 l_Ztlm, l_Stlm, l_ST, l_SP, l_tot_num_tp_remaining, l_quotient, l_Ptlm_size;
        /* preconditions */
        assert(p_header_data != 00);
        assert(p_j2k != 00);
        assert(p_manager != 00);

        if (p_header_size < 2) {
                opj_event_msg(p_manager, EVT_ERROR, "Error reading TLM marker\n");
                return OPJ_FALSE;
        }
        p_header_size -= 2;

        opj_read_bytes(p_header_data,&l_Ztlm,1);                                /* Ztlm */
        ++p_header_data;
        opj_read_bytes(p_header_data,&l_Stlm,1);                                /* Stlm */
        ++p_header_data;

        l_ST = ((l_Stlm >> 4) & 0x3);
        l_SP = (l_Stlm >> 6) & 0x1;

        l_Ptlm_size = (l_SP + 1) * 2;
        l_quotient = l_Ptlm_size + l_ST;

        l_tot_num_tp_remaining = p_header_size % l_quotient;

        if (l_tot_num_tp_remaining != 0) {
                opj_event_msg(p_manager, EVT_ERROR, "Error reading TLM marker\n");
                return OPJ_FALSE;
        }
        /* FIXME Do not care of this at the moment since only local variables are set here */
        /*
        for
                (i = 0; i < l_tot_num_tp; ++i)
        {
                opj_read_bytes(p_header_data,&l_Ttlm_i,l_ST);                           // Ttlm_i
                p_header_data += l_ST;
                opj_read_bytes(p_header_data,&l_Ptlm_i,l_Ptlm_size);            // Ptlm_i
                p_header_data += l_Ptlm_size;
        }*/
        return OPJ_TRUE;
}

/**
 * Reads a PLM marker (Packet length, main header marker)
 *
 * @param       p_header_data   the data contained in the TLM box.
 * @param       p_j2k                   the jpeg2000 codec.
 * @param       p_header_size   the size of the data contained in the TLM marker.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_read_plm (  opj_j2k_t *p_j2k,
                                    OPJ_BYTE * p_header_data,
                                    OPJ_UINT32 p_header_size,
                                    opj_event_mgr_t * p_manager
                                    )
{
        /* preconditions */
        assert(p_header_data != 00);
        assert(p_j2k != 00);
        assert(p_manager != 00);

        if (p_header_size < 1) {
                opj_event_msg(p_manager, EVT_ERROR, "Error reading PLM marker\n");
                return OPJ_FALSE;
        }
        /* Do not care of this at the moment since only local variables are set here */
        /*
        opj_read_bytes(p_header_data,&l_Zplm,1);                                        // Zplm
        ++p_header_data;
        --p_header_size;

        while
                (p_header_size > 0)
        {
                opj_read_bytes(p_header_data,&l_Nplm,1);                                // Nplm
                ++p_header_data;
                p_header_size -= (1+l_Nplm);
                if
                        (p_header_size < 0)
                {
                        opj_event_msg(p_manager, EVT_ERROR, "Error reading PLM marker\n");
                        return false;
                }
                for
                        (i = 0; i < l_Nplm; ++i)
                {
                        opj_read_bytes(p_header_data,&l_tmp,1);                         // Iplm_ij
                        ++p_header_data;
                        // take only the last seven bytes
                        l_packet_len |= (l_tmp & 0x7f);
                        if
                                (l_tmp & 0x80)
                        {
                                l_packet_len <<= 7;
                        }
                        else
                        {
                // store packet length and proceed to next packet
                                l_packet_len = 0;
                        }
                }
                if
                        (l_packet_len != 0)
                {
                        opj_event_msg(p_manager, EVT_ERROR, "Error reading PLM marker\n");
                        return false;
                }
        }
        */
        return OPJ_TRUE;
}

/**
 * Reads a PLT marker (Packet length, tile-part header)
 *
 * @param       p_header_data   the data contained in the PLT box.
 * @param       p_j2k                   the jpeg2000 codec.
 * @param       p_header_size   the size of the data contained in the PLT marker.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_read_plt (  opj_j2k_t *p_j2k,
                                    OPJ_BYTE * p_header_data,
                                    OPJ_UINT32 p_header_size,
                                    opj_event_mgr_t * p_manager
                                    )
{
        OPJ_UINT32 l_Zplt, l_tmp, l_packet_len = 0, i;

        /* preconditions */
        assert(p_header_data != 00);
        assert(p_j2k != 00);
        assert(p_manager != 00);

        if (p_header_size < 1) {
                opj_event_msg(p_manager, EVT_ERROR, "Error reading PLT marker\n");
                return OPJ_FALSE;
        }

        opj_read_bytes(p_header_data,&l_Zplt,1);                /* Zplt */
        ++p_header_data;
        --p_header_size;

        for (i = 0; i < p_header_size; ++i) {
                opj_read_bytes(p_header_data,&l_tmp,1);         /* Iplt_ij */
                ++p_header_data;
                /* take only the last seven bytes */
                l_packet_len |= (l_tmp & 0x7f);
                if (l_tmp & 0x80) {
                        l_packet_len <<= 7;
                }
                else {
            /* store packet length and proceed to next packet */
                        l_packet_len = 0;
                }
        }

        if (l_packet_len != 0) {
                opj_event_msg(p_manager, EVT_ERROR, "Error reading PLT marker\n");
                return OPJ_FALSE;
        }

        return OPJ_TRUE;
}

#if 0
OPJ_BOOL j2k_read_ppm_v2 (
                                                opj_j2k_t *p_j2k,
                                                OPJ_BYTE * p_header_data,
                                                OPJ_UINT32 p_header_size,
                                                struct opj_event_mgr * p_manager
                                        )
{

        opj_cp_t *l_cp = 00;
        OPJ_UINT32 l_remaining_data, l_Z_ppm, l_N_ppm;

        /* preconditions */
        assert(p_header_data != 00);
        assert(p_j2k != 00);
        assert(p_manager != 00);

        if (p_header_size < 1) {
                opj_event_msg(p_manager, EVT_ERROR, "Error reading PPM marker\n");
                return OPJ_FALSE;
        }

        l_cp = &(p_j2k->m_cp);
        l_cp->ppm = 1;

        opj_read_bytes(p_header_data,&l_Z_ppm,1);               /* Z_ppm */
        ++p_header_data;
        --p_header_size;

        /* First PPM marker */
        if (l_Z_ppm == 0) {
                if (p_header_size < 4) {
                        opj_event_msg(p_manager, EVT_ERROR, "Error reading PPM marker\n");
                        return OPJ_FALSE;
                }

                opj_read_bytes(p_header_data,&l_N_ppm,4);               /* N_ppm */
                p_header_data+=4;
                p_header_size-=4;

                /* First PPM marker: Initialization */
                l_cp->ppm_len = l_N_ppm;
                l_cp->ppm_data_size = 0;

                l_cp->ppm_buffer = (OPJ_BYTE *) opj_malloc(l_cp->ppm_len);
                if (l_cp->ppm_buffer == 00) {
                        opj_event_msg(p_manager, EVT_ERROR, "Not enough memory reading ppm marker\n");
                        return OPJ_FALSE;
                }
                memset(l_cp->ppm_buffer,0,l_cp->ppm_len);

                l_cp->ppm_data = l_cp->ppm_buffer;
        }

        while (1) {
                if (l_cp->ppm_data_size == l_cp->ppm_len) {
                        if (p_header_size >= 4) {
                                /* read a N_ppm */
                                opj_read_bytes(p_header_data,&l_N_ppm,4);               /* N_ppm */
                                p_header_data+=4;
                                p_header_size-=4;
                                l_cp->ppm_len += l_N_ppm ;

                                OPJ_BYTE *new_ppm_buffer = (OPJ_BYTE *) opj_realloc(l_cp->ppm_buffer, l_cp->ppm_len);
                                if (! new_ppm_buffer) {
                                        opj_free(l_cp->ppm_buffer);
                                        l_cp->ppm_buffer = NULL;
                                        l_cp->ppm_len = 0;
                                        l_cp->ppm_data = NULL;
                                        opj_event_msg(p_manager, EVT_ERROR, "Not enough memory reading ppm marker\n");
                                        return OPJ_FALSE;
                                }
                                l_cp->ppm_buffer = new_ppm_buffer;
                                memset(l_cp->ppm_buffer+l_cp->ppm_data_size,0,l_N_ppm);
                                l_cp->ppm_data = l_cp->ppm_buffer;
                        }
                        else {
                                return OPJ_FALSE;
                        }
                }

                l_remaining_data = l_cp->ppm_len - l_cp->ppm_data_size;

                if (l_remaining_data <= p_header_size) {
                        /* we must store less information than available in the packet */
                        memcpy(l_cp->ppm_buffer + l_cp->ppm_data_size , p_header_data , l_remaining_data);
                        l_cp->ppm_data_size = l_cp->ppm_len;
                        p_header_size -= l_remaining_data;
                        p_header_data += l_remaining_data;
                }
                else {
                        memcpy(l_cp->ppm_buffer + l_cp->ppm_data_size , p_header_data , p_header_size);
                        l_cp->ppm_data_size += p_header_size;
                        p_header_data += p_header_size;
                        p_header_size = 0;
                        break;
                }
        }

        return OPJ_TRUE;
}
#endif

OPJ_BOOL j2k_read_ppm_v3 (
                                                opj_j2k_t *p_j2k,
                                                OPJ_BYTE * p_header_data,
                                                OPJ_UINT32 p_header_size,
                                                struct opj_event_mgr * p_manager
                                        )
{
        opj_cp_t *l_cp = 00;
        OPJ_UINT32 l_remaining_data, l_Z_ppm, l_N_ppm;

        /* preconditions */
        assert(p_header_data != 00);
        assert(p_j2k != 00);
        assert(p_manager != 00);

        /* Minimum size of PPM marker is equal to the size of Zppm element */
        if (p_header_size < 1) {
                opj_event_msg(p_manager, EVT_ERROR, "Error reading PPM marker\n");
                return OPJ_FALSE;
        }

        l_cp = &(p_j2k->m_cp);
        l_cp->ppm = 1;

        opj_read_bytes(p_header_data,&l_Z_ppm,1);               /* Z_ppm */
        ++p_header_data;
        --p_header_size;

        /* First PPM marker */
        if (l_Z_ppm == 0) {
                /* We need now at least the Nppm^0 element */
                if (p_header_size < 4) {
                        opj_event_msg(p_manager, EVT_ERROR, "Error reading PPM marker\n");
                        return OPJ_FALSE;
                }

                opj_read_bytes(p_header_data,&l_N_ppm,4);               /* First N_ppm */
                p_header_data+=4;
                p_header_size-=4;

                /* sanity check: how much bytes is left for Ippm */
                if( p_header_size < l_N_ppm )
                  {
                  opj_event_msg(p_manager, EVT_ERROR, "Not enough bytes (%u) to hold Ippm series (%u), Index (%d)\n", p_header_size, l_N_ppm, l_Z_ppm );
                  opj_free(l_cp->ppm_data);
                  l_cp->ppm_data = NULL;
                  l_cp->ppm_buffer = NULL;
                  l_cp->ppm = 0; /* do not use PPM */
                  return OPJ_TRUE;
                  }

                /* First PPM marker: Initialization */
                l_cp->ppm_len = l_N_ppm;
                l_cp->ppm_data_read = 0;

                l_cp->ppm_data = (OPJ_BYTE *) opj_malloc(l_cp->ppm_len);
                l_cp->ppm_buffer = l_cp->ppm_data;
                if (l_cp->ppm_data == 00) {
                        opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to read ppm marker\n");
                        return OPJ_FALSE;
                }
                memset(l_cp->ppm_data,0,l_cp->ppm_len);

                l_cp->ppm_data_current = l_cp->ppm_data;

                /*l_cp->ppm_data = l_cp->ppm_buffer;*/
        }
        else {
                if (p_header_size < 4) {
                        opj_event_msg(p_manager, EVT_WARNING, "Empty PPM marker\n");
                        return OPJ_TRUE;
                }
                else {
                        /* Uncompleted Ippm series in the previous PPM marker?*/
                        if (l_cp->ppm_data_read < l_cp->ppm_len) {
                                /* Get the place where add the remaining Ippm series*/
                                l_cp->ppm_data_current = &(l_cp->ppm_data[l_cp->ppm_data_read]);
                                l_N_ppm = l_cp->ppm_len - l_cp->ppm_data_read;
                        }
                        else {
                                OPJ_BYTE *new_ppm_data;
                                opj_read_bytes(p_header_data,&l_N_ppm,4);               /* First N_ppm */
                                p_header_data+=4;
                                p_header_size-=4;

                                /* sanity check: how much bytes is left for Ippm */
                                if( p_header_size < l_N_ppm )
                                  {
                                  opj_event_msg(p_manager, EVT_ERROR, "Not enough bytes (%u) to hold Ippm series (%u), Index (%d)\n", p_header_size, l_N_ppm, l_Z_ppm );
                                  opj_free(l_cp->ppm_data);
                                  l_cp->ppm_data = NULL;
                                  l_cp->ppm_buffer = NULL;
                                  l_cp->ppm = 0; /* do not use PPM */
                                  return OPJ_TRUE;
                                  }
                                /* Increase the size of ppm_data to add the new Ippm series*/
                                assert(l_cp->ppm_data == l_cp->ppm_buffer && "We need ppm_data and ppm_buffer to be the same when reallocating");
                                new_ppm_data = (OPJ_BYTE *) opj_realloc(l_cp->ppm_data, l_cp->ppm_len + l_N_ppm);
                                if (! new_ppm_data) {
                                        opj_free(l_cp->ppm_data);
                                        l_cp->ppm_data = NULL;
                                        l_cp->ppm_buffer = NULL;  /* TODO: no need for a new local variable: ppm_buffer and ppm_data are enough */
                                        l_cp->ppm_len = 0;
                                        opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to increase the size of ppm_data to add the new Ippm series\n");
                                        return OPJ_FALSE;
                                }
                                l_cp->ppm_data = new_ppm_data;
                                l_cp->ppm_buffer = l_cp->ppm_data;

                                /* Keep the position of the place where concatenate the new series*/
                                l_cp->ppm_data_current = &(l_cp->ppm_data[l_cp->ppm_len]);
                                l_cp->ppm_len += l_N_ppm;
                        }
                }
        }

        l_remaining_data = p_header_size;

        while (l_remaining_data >= l_N_ppm) {
                /* read a complete Ippm series*/
                memcpy(l_cp->ppm_data_current, p_header_data, l_N_ppm);
                p_header_size -= l_N_ppm;
                p_header_data += l_N_ppm;

                l_cp->ppm_data_read += l_N_ppm; /* Increase the number of data read*/

                if (p_header_size)
                {
                        opj_read_bytes(p_header_data,&l_N_ppm,4);               /* N_ppm^i */
                        p_header_data+=4;
                        p_header_size-=4;
                }
                else {
                        l_remaining_data = p_header_size;
                        break;
                }

                l_remaining_data = p_header_size;

                /* Next Ippm series is a complete series ?*/
                if (l_remaining_data >= l_N_ppm) {
                        OPJ_BYTE *new_ppm_data;
                        /* Increase the size of ppm_data to add the new Ippm series*/
                        assert(l_cp->ppm_data == l_cp->ppm_buffer && "We need ppm_data and ppm_buffer to be the same when reallocating");
                        new_ppm_data = (OPJ_BYTE *) opj_realloc(l_cp->ppm_data, l_cp->ppm_len + l_N_ppm);
                        if (! new_ppm_data) {
                                opj_free(l_cp->ppm_data);
                                l_cp->ppm_data = NULL;
                                l_cp->ppm_buffer = NULL;  /* TODO: no need for a new local variable: ppm_buffer and ppm_data are enough */
                                l_cp->ppm_len = 0;
                                opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to increase the size of ppm_data to add the new (complete) Ippm series\n");
                                return OPJ_FALSE;
                        }
                        l_cp->ppm_data = new_ppm_data;
                        l_cp->ppm_buffer = l_cp->ppm_data;

                        /* Keep the position of the place where concatenate the new series */
                        l_cp->ppm_data_current = &(l_cp->ppm_data[l_cp->ppm_len]);
                        l_cp->ppm_len += l_N_ppm;
                }

        }

        /* Need to read an incomplete Ippm series*/
        if (l_remaining_data) {
                OPJ_BYTE *new_ppm_data;
                assert(l_cp->ppm_data == l_cp->ppm_buffer && "We need ppm_data and ppm_buffer to be the same when reallocating");
                new_ppm_data = (OPJ_BYTE *) opj_realloc(l_cp->ppm_data, l_cp->ppm_len + l_N_ppm);
                if (! new_ppm_data) {
                        opj_free(l_cp->ppm_data);
                        l_cp->ppm_data = NULL;
                        l_cp->ppm_buffer = NULL;  /* TODO: no need for a new local variable: ppm_buffer and ppm_data are enough */
                        l_cp->ppm_len = 0;
                        opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to increase the size of ppm_data to add the new (incomplete) Ippm series\n");
                        return OPJ_FALSE;
                }
                l_cp->ppm_data = new_ppm_data;
                l_cp->ppm_buffer = l_cp->ppm_data;

                /* Keep the position of the place where concatenate the new series*/
                l_cp->ppm_data_current = &(l_cp->ppm_data[l_cp->ppm_len]);
                l_cp->ppm_len += l_N_ppm;

                /* Read incomplete Ippm series*/
                memcpy(l_cp->ppm_data_current, p_header_data, l_remaining_data);
                p_header_size -= l_remaining_data;
                p_header_data += l_remaining_data;

                l_cp->ppm_data_read += l_remaining_data; /* Increase the number of data read*/
        }

#ifdef CLEAN_MSD

                if (l_cp->ppm_data_size == l_cp->ppm_len) {
                        if (p_header_size >= 4) {
                                /* read a N_ppm*/
                                opj_read_bytes(p_header_data,&l_N_ppm,4);               /* N_ppm */
                                p_header_data+=4;
                                p_header_size-=4;
                                l_cp->ppm_len += l_N_ppm ;

                                OPJ_BYTE *new_ppm_buffer = (OPJ_BYTE *) opj_realloc(l_cp->ppm_buffer, l_cp->ppm_len);
                                if (! new_ppm_buffer) {
                                        opj_free(l_cp->ppm_buffer);
                                        l_cp->ppm_buffer = NULL;
                                        l_cp->ppm_len = 0;
                                        opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to read ppm marker\n");
                                        return OPJ_FALSE;
                                }
                                l_cp->ppm_buffer = new_ppm_buffer;
                                memset(l_cp->ppm_buffer+l_cp->ppm_data_size,0,l_N_ppm);

                                l_cp->ppm_data = l_cp->ppm_buffer;
                        }
                        else {
                                return OPJ_FALSE;
                        }
                }

                l_remaining_data = l_cp->ppm_len - l_cp->ppm_data_size;

                if (l_remaining_data <= p_header_size) {
                        /* we must store less information than available in the packet */
                        memcpy(l_cp->ppm_buffer + l_cp->ppm_data_size , p_header_data , l_remaining_data);
                        l_cp->ppm_data_size = l_cp->ppm_len;
                        p_header_size -= l_remaining_data;
                        p_header_data += l_remaining_data;
                }
                else {
                        memcpy(l_cp->ppm_buffer + l_cp->ppm_data_size , p_header_data , p_header_size);
                        l_cp->ppm_data_size += p_header_size;
                        p_header_data += p_header_size;
                        p_header_size = 0;
                        break;
                }
        }
#endif
        return OPJ_TRUE;
}

/**
 * Reads a PPT marker (Packed packet headers, tile-part header)
 *
 * @param       p_header_data   the data contained in the PPT box.
 * @param       p_j2k                   the jpeg2000 codec.
 * @param       p_header_size   the size of the data contained in the PPT marker.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_read_ppt (  opj_j2k_t *p_j2k,
                                    OPJ_BYTE * p_header_data,
                                    OPJ_UINT32 p_header_size,
                                    opj_event_mgr_t * p_manager
                                    )
{
        opj_cp_t *l_cp = 00;
        opj_tcp_t *l_tcp = 00;
        OPJ_UINT32 l_Z_ppt;

        /* preconditions */
        assert(p_header_data != 00);
        assert(p_j2k != 00);
        assert(p_manager != 00);

        /* We need to have the Z_ppt element at minimum */
        if (p_header_size < 1) {
                opj_event_msg(p_manager, EVT_ERROR, "Error reading PPT marker\n");
                return OPJ_FALSE;
        }

        l_cp = &(p_j2k->m_cp);
        if (l_cp->ppm){
                opj_event_msg(p_manager, EVT_ERROR, "Error reading PPT marker: packet header have been previously found in the main header (PPM marker).\n");
                return OPJ_FALSE;
        }

        l_tcp = &(l_cp->tcps[p_j2k->m_current_tile_number]);
        l_tcp->ppt = 1;

        opj_read_bytes(p_header_data,&l_Z_ppt,1);               /* Z_ppt */
        ++p_header_data;
        --p_header_size;

        /* Allocate buffer to read the packet header */
        if (l_Z_ppt == 0) {
                /* First PPT marker */
                l_tcp->ppt_data_size = 0;
                l_tcp->ppt_len = p_header_size;

                opj_free(l_tcp->ppt_buffer);
                l_tcp->ppt_buffer = (OPJ_BYTE *) opj_calloc(l_tcp->ppt_len, sizeof(OPJ_BYTE) );
                if (l_tcp->ppt_buffer == 00) {
                        opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to read PPT marker\n");
                        return OPJ_FALSE;
                }
                l_tcp->ppt_data = l_tcp->ppt_buffer;

                /* memset(l_tcp->ppt_buffer,0,l_tcp->ppt_len); */
        }
        else {
                OPJ_BYTE *new_ppt_buffer;
                l_tcp->ppt_len += p_header_size;

                new_ppt_buffer = (OPJ_BYTE *) opj_realloc(l_tcp->ppt_buffer, l_tcp->ppt_len);
                if (! new_ppt_buffer) {
                        opj_free(l_tcp->ppt_buffer);
                        l_tcp->ppt_buffer = NULL;
                        l_tcp->ppt_len = 0;
                        opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to read PPT marker\n");
                        return OPJ_FALSE;
                }
                l_tcp->ppt_buffer = new_ppt_buffer;
                l_tcp->ppt_data = l_tcp->ppt_buffer;

                memset(l_tcp->ppt_buffer+l_tcp->ppt_data_size,0,p_header_size);
        }

        /* Read packet header from buffer */
        memcpy(l_tcp->ppt_buffer+l_tcp->ppt_data_size,p_header_data,p_header_size);

        l_tcp->ppt_data_size += p_header_size;

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_write_tlm(     opj_j2k_t *p_j2k,
                                                        opj_stream_private_t *p_stream,
                                                        opj_event_mgr_t * p_manager
                            )
{
        OPJ_BYTE * l_current_data = 00;
        OPJ_UINT32 l_tlm_size;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_manager != 00);
        assert(p_stream != 00);

        l_tlm_size = 6 + (5*p_j2k->m_specific_param.m_encoder.m_total_tile_parts);

        if (l_tlm_size > p_j2k->m_specific_param.m_encoder.m_header_tile_data_size) {
                OPJ_BYTE *new_header_tile_data = (OPJ_BYTE *) opj_realloc(p_j2k->m_specific_param.m_encoder.m_header_tile_data, l_tlm_size);
                if (! new_header_tile_data) {
                        opj_free(p_j2k->m_specific_param.m_encoder.m_header_tile_data);
                        p_j2k->m_specific_param.m_encoder.m_header_tile_data = NULL;
                        p_j2k->m_specific_param.m_encoder.m_header_tile_data_size = 0;
                        opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to write TLM marker\n");
                        return OPJ_FALSE;
                }
                p_j2k->m_specific_param.m_encoder.m_header_tile_data = new_header_tile_data;
                p_j2k->m_specific_param.m_encoder.m_header_tile_data_size = l_tlm_size;
        }

        l_current_data = p_j2k->m_specific_param.m_encoder.m_header_tile_data;

        /* change the way data is written to avoid seeking if possible */
        /* TODO */
        p_j2k->m_specific_param.m_encoder.m_tlm_start = opj_stream_tell(p_stream);

        opj_write_bytes(l_current_data,J2K_MS_TLM,2);                                   /* TLM */
        l_current_data += 2;

        opj_write_bytes(l_current_data,l_tlm_size-2,2);                                 /* Lpoc */
        l_current_data += 2;

        opj_write_bytes(l_current_data,0,1);                                                    /* Ztlm=0*/
        ++l_current_data;

        opj_write_bytes(l_current_data,0x50,1);                                                 /* Stlm ST=1(8bits-255 tiles max),SP=1(Ptlm=32bits) */
        ++l_current_data;

        /* do nothing on the 5 * l_j2k->m_specific_param.m_encoder.m_total_tile_parts remaining data */
        if (opj_stream_write_data(p_stream,p_j2k->m_specific_param.m_encoder.m_header_tile_data,l_tlm_size,p_manager) != l_tlm_size) {
                return OPJ_FALSE;
        }

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_write_sot(     opj_j2k_t *p_j2k,
                                                        OPJ_BYTE * p_data,
                                                        OPJ_UINT32 * p_data_written,
                                                        const opj_stream_private_t *p_stream,
                                                        opj_event_mgr_t * p_manager
                            )
{
        /* preconditions */
        assert(p_j2k != 00);
        assert(p_manager != 00);
        assert(p_stream != 00);

        opj_write_bytes(p_data,J2K_MS_SOT,2);                                   /* SOT */
        p_data += 2;

        opj_write_bytes(p_data,10,2);                                                   /* Lsot */
        p_data += 2;

        opj_write_bytes(p_data, p_j2k->m_current_tile_number,2);                        /* Isot */
        p_data += 2;

        /* Psot  */
        p_data += 4;

        opj_write_bytes(p_data, p_j2k->m_specific_param.m_encoder.m_current_tile_part_number,1);                        /* TPsot */
        ++p_data;

        opj_write_bytes(p_data, p_j2k->m_cp.tcps[p_j2k->m_current_tile_number].m_nb_tile_parts,1);                      /* TNsot */
        ++p_data;

        /* UniPG>> */
#ifdef USE_JPWL
        /* update markers struct */
/*
        OPJ_BOOL res = j2k_add_marker(p_j2k->cstr_info, J2K_MS_SOT, p_j2k->sot_start, len + 2);
*/
  assert( 0 && "TODO" );
#endif /* USE_JPWL */

        * p_data_written = 12;

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_read_sot ( opj_j2k_t *p_j2k,
                            OPJ_BYTE * p_header_data,
                            OPJ_UINT32 p_header_size,
                            opj_event_mgr_t * p_manager )
{
        opj_cp_t *l_cp = 00;
        opj_tcp_t *l_tcp = 00;
        OPJ_UINT32 l_tot_len, l_num_parts = 0;
        OPJ_UINT32 l_current_part;
        OPJ_UINT32 l_tile_x,l_tile_y;

        /* preconditions */
        assert(p_header_data != 00);
        assert(p_j2k != 00);
        assert(p_manager != 00);

        /* Size of this marker is fixed = 12 (we have already read marker and its size)*/
        if (p_header_size != 8) {
                opj_event_msg(p_manager, EVT_ERROR, "Error reading SOT marker\n");
                return OPJ_FALSE;
        }

        l_cp = &(p_j2k->m_cp);
        opj_read_bytes(p_header_data,&(p_j2k->m_current_tile_number),2);                /* Isot */
        p_header_data+=2;

        /* testcase 2.pdf.SIGFPE.706.1112 */
        if (p_j2k->m_current_tile_number >= l_cp->tw * l_cp->th) {
                opj_event_msg(p_manager, EVT_ERROR, "Invalid tile number %d\n", p_j2k->m_current_tile_number);
                return OPJ_FALSE;
        }

        l_tcp = &l_cp->tcps[p_j2k->m_current_tile_number];
        l_tile_x = p_j2k->m_current_tile_number % l_cp->tw;
        l_tile_y = p_j2k->m_current_tile_number / l_cp->tw;

#ifdef USE_JPWL
        if (l_cp->correct) {

                OPJ_UINT32 tileno = p_j2k->m_current_tile_number;
                static OPJ_UINT32 backup_tileno = 0;

                /* tileno is negative or larger than the number of tiles!!! */
                if (tileno > (l_cp->tw * l_cp->th)) {
                        opj_event_msg(p_manager, EVT_ERROR,
                                        "JPWL: bad tile number (%d out of a maximum of %d)\n",
                                        tileno, (l_cp->tw * l_cp->th));
                        if (!JPWL_ASSUME) {
                                opj_event_msg(p_manager, EVT_ERROR, "JPWL: giving up\n");
                                return OPJ_FALSE;
                        }
                        /* we try to correct */
                        tileno = backup_tileno;
                        opj_event_msg(p_manager, EVT_WARNING, "- trying to adjust this\n"
                                        "- setting tile number to %d\n",
                                        tileno);
                }

                /* keep your private count of tiles */
                backup_tileno++;
        };
#endif /* USE_JPWL */

        /* look for the tile in the list of already processed tile (in parts). */
        /* Optimization possible here with a more complex data structure and with the removing of tiles */
        /* since the time taken by this function can only grow at the time */

        opj_read_bytes(p_header_data,&l_tot_len,4);             /* Psot */
        p_header_data+=4;

        /* PSot should be equal to zero or >=14 or <= 2^32-1 */
        if ((l_tot_len !=0 ) && (l_tot_len < 14) )
        {
            if (l_tot_len == 12 ) /* MSD: Special case for the PHR data which are read by kakadu*/
            {
                opj_event_msg(p_manager, EVT_WARNING, "Empty SOT marker detected: Psot=%d.\n", l_tot_len);
            }
            else
            {
                opj_event_msg(p_manager, EVT_ERROR, "Psot value is not correct regards to the JPEG2000 norm: %d.\n", l_tot_len);
                return OPJ_FALSE;
            }
        }

#ifdef USE_JPWL
        if (l_cp->correct) {

                /* totlen is negative or larger than the bytes left!!! */
                if (/*(l_tot_len < 0) ||*/ (l_tot_len > p_header_size ) ) { /* FIXME it seems correct; for info in V1 -> (p_stream_numbytesleft(p_stream) + 8))) { */
                        opj_event_msg(p_manager, EVT_ERROR,
                                        "JPWL: bad tile byte size (%d bytes against %d bytes left)\n",
                                        l_tot_len, p_header_size ); /* FIXME it seems correct; for info in V1 -> p_stream_numbytesleft(p_stream) + 8); */
                        if (!JPWL_ASSUME) {
                                opj_event_msg(p_manager, EVT_ERROR, "JPWL: giving up\n");
                                return OPJ_FALSE;
                        }
                        /* we try to correct */
                        l_tot_len = 0;
                        opj_event_msg(p_manager, EVT_WARNING, "- trying to adjust this\n"
                                        "- setting Psot to %d => assuming it is the last tile\n",
                                        l_tot_len);
                }
                };
#endif /* USE_JPWL */

                /* Ref A.4.2: Psot could be equal zero if it is the last tile-part of the codestream.*/
                if (!l_tot_len) {
                        opj_event_msg(p_manager, EVT_INFO, "Psot value of the current tile-part is equal to zero, "
                                        "we assuming it is the last tile-part of the codestream.\n");
                        p_j2k->m_specific_param.m_decoder.m_last_tile_part = 1;
                }

                opj_read_bytes(p_header_data,&l_current_part ,1);       /* TPsot */
                ++p_header_data;

                opj_read_bytes(p_header_data,&l_num_parts ,1);          /* TNsot */
                ++p_header_data;

                if (l_num_parts != 0) { /* Number of tile-part header is provided by this tile-part header */
                        /* Useful to manage the case of textGBR.jp2 file because two values of TNSot are allowed: the correct numbers of
                         * tile-parts for that tile and zero (A.4.2 of 15444-1 : 2002). */
                        if (l_tcp->m_nb_tile_parts) {
                                if (l_current_part >= l_tcp->m_nb_tile_parts){
                                        opj_event_msg(p_manager, EVT_ERROR, "In SOT marker, TPSot (%d) is not valid regards to the current "
                                                        "number of tile-part (%d), giving up\n", l_current_part, l_tcp->m_nb_tile_parts );
                                        p_j2k->m_specific_param.m_decoder.m_last_tile_part = 1;
                                        return OPJ_FALSE;
                                }
                        }
                        if( l_current_part >= l_num_parts ) {
                          /* testcase 451.pdf.SIGSEGV.ce9.3723 */
                          opj_event_msg(p_manager, EVT_ERROR, "In SOT marker, TPSot (%d) is not valid regards to the current "
                            "number of tile-part (header) (%d), giving up\n", l_current_part, l_num_parts );
                          p_j2k->m_specific_param.m_decoder.m_last_tile_part = 1;
                          return OPJ_FALSE;
                        }
                        l_tcp->m_nb_tile_parts = l_num_parts;
                }

                /* If know the number of tile part header we will check if we didn't read the last*/
                if (l_tcp->m_nb_tile_parts) {
                        if (l_tcp->m_nb_tile_parts == (l_current_part+1)) {
                                p_j2k->m_specific_param.m_decoder.m_can_decode = 1; /* Process the last tile-part header*/
                        }
                }

                if (!p_j2k->m_specific_param.m_decoder.m_last_tile_part){
                        /* Keep the size of data to skip after this marker */
                        p_j2k->m_specific_param.m_decoder.m_sot_length = l_tot_len - 12; /* SOT_marker_size = 12 */
                }
                else {
                        /* FIXME: need to be computed from the number of bytes remaining in the codestream */
                        p_j2k->m_specific_param.m_decoder.m_sot_length = 0;
                }

                p_j2k->m_specific_param.m_decoder.m_state = J2K_STATE_TPH;

                /* Check if the current tile is outside the area we want decode or not corresponding to the tile index*/
                if (p_j2k->m_specific_param.m_decoder.m_tile_ind_to_dec == -1) {
                        p_j2k->m_specific_param.m_decoder.m_skip_data =
                                (l_tile_x < p_j2k->m_specific_param.m_decoder.m_start_tile_x)
                                ||      (l_tile_x >= p_j2k->m_specific_param.m_decoder.m_end_tile_x)
                                ||  (l_tile_y < p_j2k->m_specific_param.m_decoder.m_start_tile_y)
                                ||      (l_tile_y >= p_j2k->m_specific_param.m_decoder.m_end_tile_y);
                }
                else {
                        assert( p_j2k->m_specific_param.m_decoder.m_tile_ind_to_dec >= 0 );
                        p_j2k->m_specific_param.m_decoder.m_skip_data =
                                (p_j2k->m_current_tile_number != (OPJ_UINT32)p_j2k->m_specific_param.m_decoder.m_tile_ind_to_dec);
                }

                /* Index */
                if (p_j2k->cstr_index)
                {
                        assert(p_j2k->cstr_index->tile_index != 00);
                        p_j2k->cstr_index->tile_index[p_j2k->m_current_tile_number].tileno = p_j2k->m_current_tile_number;
                        p_j2k->cstr_index->tile_index[p_j2k->m_current_tile_number].current_tpsno = l_current_part;

                        if (l_num_parts != 0){
                                p_j2k->cstr_index->tile_index[p_j2k->m_current_tile_number].nb_tps = l_num_parts;
                                p_j2k->cstr_index->tile_index[p_j2k->m_current_tile_number].current_nb_tps = l_num_parts;

                                if (!p_j2k->cstr_index->tile_index[p_j2k->m_current_tile_number].tp_index) {
                                        p_j2k->cstr_index->tile_index[p_j2k->m_current_tile_number].tp_index =
                                                (opj_tp_index_t*)opj_calloc(l_num_parts, sizeof(opj_tp_index_t));
                                }
                                else {
                                        opj_tp_index_t *new_tp_index = (opj_tp_index_t *) opj_realloc(
                                                        p_j2k->cstr_index->tile_index[p_j2k->m_current_tile_number].tp_index, l_num_parts* sizeof(opj_tp_index_t));
                                        if (! new_tp_index) {
                                                opj_free(p_j2k->cstr_index->tile_index[p_j2k->m_current_tile_number].tp_index);
                                                p_j2k->cstr_index->tile_index[p_j2k->m_current_tile_number].tp_index = NULL;
                                                opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to read PPT marker\n");
                                                return OPJ_FALSE;
                                        }
                                        p_j2k->cstr_index->tile_index[p_j2k->m_current_tile_number].tp_index = new_tp_index;
                                }
                        }
                        else{
                                /*if (!p_j2k->cstr_index->tile_index[p_j2k->m_current_tile_number].tp_index)*/ {

                                        if (!p_j2k->cstr_index->tile_index[p_j2k->m_current_tile_number].tp_index) {
                                                p_j2k->cstr_index->tile_index[p_j2k->m_current_tile_number].current_nb_tps = 10;
                                                p_j2k->cstr_index->tile_index[p_j2k->m_current_tile_number].tp_index =
                                                        (opj_tp_index_t*)opj_calloc( p_j2k->cstr_index->tile_index[p_j2k->m_current_tile_number].current_nb_tps,
                                                                        sizeof(opj_tp_index_t));
                                        }

                                        if ( l_current_part >= p_j2k->cstr_index->tile_index[p_j2k->m_current_tile_number].current_nb_tps ){
                                                opj_tp_index_t *new_tp_index;
                                                p_j2k->cstr_index->tile_index[p_j2k->m_current_tile_number].current_nb_tps = l_current_part + 1;
                                                new_tp_index = (opj_tp_index_t *) opj_realloc(
                                                                p_j2k->cstr_index->tile_index[p_j2k->m_current_tile_number].tp_index,
                                                                p_j2k->cstr_index->tile_index[p_j2k->m_current_tile_number].current_nb_tps * sizeof(opj_tp_index_t));
                                                if (! new_tp_index) {
                                                        opj_free(p_j2k->cstr_index->tile_index[p_j2k->m_current_tile_number].tp_index);
                                                        p_j2k->cstr_index->tile_index[p_j2k->m_current_tile_number].tp_index = NULL;
                                                        p_j2k->cstr_index->tile_index[p_j2k->m_current_tile_number].current_nb_tps = 0;
                                                        opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to read PPT marker\n");
                                                        return OPJ_FALSE;
                                                }
                                                p_j2k->cstr_index->tile_index[p_j2k->m_current_tile_number].tp_index = new_tp_index;
                                        }
                                }

                        }

                }

                /* FIXME move this onto a separate method to call before reading any SOT, remove part about main_end header, use a index struct inside p_j2k */
                /* if (p_j2k->cstr_info) {
                   if (l_tcp->first) {
                   if (tileno == 0) {
                   p_j2k->cstr_info->main_head_end = p_stream_tell(p_stream) - 13;
                   }

                   p_j2k->cstr_info->tile[tileno].tileno = tileno;
                   p_j2k->cstr_info->tile[tileno].start_pos = p_stream_tell(p_stream) - 12;
                   p_j2k->cstr_info->tile[tileno].end_pos = p_j2k->cstr_info->tile[tileno].start_pos + totlen - 1;
                   p_j2k->cstr_info->tile[tileno].num_tps = numparts;

                   if (numparts) {
                   p_j2k->cstr_info->tile[tileno].tp = (opj_tp_info_t *) opj_malloc(numparts * sizeof(opj_tp_info_t));
                   }
                   else {
                   p_j2k->cstr_info->tile[tileno].tp = (opj_tp_info_t *) opj_malloc(10 * sizeof(opj_tp_info_t)); // Fixme (10)
                   }
                   }
                   else {
                   p_j2k->cstr_info->tile[tileno].end_pos += totlen;
                   }

                   p_j2k->cstr_info->tile[tileno].tp[partno].tp_start_pos = p_stream_tell(p_stream) - 12;
                   p_j2k->cstr_info->tile[tileno].tp[partno].tp_end_pos =
                   p_j2k->cstr_info->tile[tileno].tp[partno].tp_start_pos + totlen - 1;
                   }*/
                return OPJ_TRUE;
        }

OPJ_BOOL opj_j2k_write_sod(     opj_j2k_t *p_j2k,
                                                        opj_tcd_t * p_tile_coder,
                                                        OPJ_BYTE * p_data,
                                                        OPJ_UINT32 * p_data_written,
                                                        OPJ_UINT32 p_total_data_size,
                                                        const opj_stream_private_t *p_stream,
                                                        opj_event_mgr_t * p_manager
                            )
{
        opj_codestream_info_t *l_cstr_info = 00;
        OPJ_UINT32 l_remaining_data;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_manager != 00);
        assert(p_stream != 00);

        opj_write_bytes(p_data,J2K_MS_SOD,2);                                   /* SOD */
        p_data += 2;

        /* make room for the EOF marker */
        l_remaining_data =  p_total_data_size - 4;

        /* update tile coder */
        p_tile_coder->tp_num = p_j2k->m_specific_param.m_encoder.m_current_poc_tile_part_number ;
        p_tile_coder->cur_tp_num = p_j2k->m_specific_param.m_encoder.m_current_tile_part_number;

         /* INDEX >> */
        /* TODO mergeV2: check this part which use cstr_info */
        /*l_cstr_info = p_j2k->cstr_info;
        if (l_cstr_info) {
                if (!p_j2k->m_specific_param.m_encoder.m_current_tile_part_number ) {
                        //TODO cstr_info->tile[p_j2k->m_current_tile_number].end_header = p_stream_tell(p_stream) + p_j2k->pos_correction - 1;
                        l_cstr_info->tile[p_j2k->m_current_tile_number].tileno = p_j2k->m_current_tile_number;
                }
                else {*/
                        /*
                        TODO
                        if
                                (cstr_info->tile[p_j2k->m_current_tile_number].packet[cstr_info->packno - 1].end_pos < p_stream_tell(p_stream))
                        {
                                cstr_info->tile[p_j2k->m_current_tile_number].packet[cstr_info->packno].start_pos = p_stream_tell(p_stream);
                        }*/
                /*}*/
                /* UniPG>> */
#ifdef USE_JPWL
                /* update markers struct */
                /*OPJ_BOOL res = j2k_add_marker(p_j2k->cstr_info, J2K_MS_SOD, p_j2k->sod_start, 2);
*/
  assert( 0 && "TODO" );
#endif /* USE_JPWL */
                /* <<UniPG */
        /*}*/
        /* << INDEX */

        if (p_j2k->m_specific_param.m_encoder.m_current_tile_part_number == 0) {
                p_tile_coder->tcd_image->tiles->packno = 0;
                if (l_cstr_info) {
                        l_cstr_info->packno = 0;
                }
        }

        *p_data_written = 0;

        if (! opj_tcd_encode_tile(p_tile_coder, p_j2k->m_current_tile_number, p_data, p_data_written, l_remaining_data , l_cstr_info)) {
                opj_event_msg(p_manager, EVT_ERROR, "Cannot encode tile\n");
                return OPJ_FALSE;
        }

        *p_data_written += 2;

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_read_sod (opj_j2k_t *p_j2k,
                           opj_stream_private_t *p_stream,
                                                   opj_event_mgr_t * p_manager
                           )
{
        OPJ_SIZE_T l_current_read_size;
        opj_codestream_index_t * l_cstr_index = 00;
        OPJ_BYTE ** l_current_data = 00;
        opj_tcp_t * l_tcp = 00;
        OPJ_UINT32 * l_tile_len = 00;
        OPJ_BOOL l_sot_length_pb_detected = OPJ_FALSE;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_manager != 00);
        assert(p_stream != 00);

        l_tcp = &(p_j2k->m_cp.tcps[p_j2k->m_current_tile_number]);

        if (p_j2k->m_specific_param.m_decoder.m_last_tile_part) {
                /* opj_stream_get_number_byte_left returns OPJ_OFF_T
                // but we are in the last tile part,
                // so its result will fit on OPJ_UINT32 unless we find
                // a file with a single tile part of more than 4 GB...*/
                p_j2k->m_specific_param.m_decoder.m_sot_length = (OPJ_UINT32)(opj_stream_get_number_byte_left(p_stream) - 2);
        }
        else {
            /* Check to avoid pass the limit of OPJ_UINT32 */
            if (p_j2k->m_specific_param.m_decoder.m_sot_length >= 2 )
                p_j2k->m_specific_param.m_decoder.m_sot_length -= 2;
            else {
                /* MSD: case commented to support empty SOT marker (PHR data) */
            }
        }

        l_current_data = &(l_tcp->m_data);
        l_tile_len = &l_tcp->m_data_size;

        /* Patch to support new PHR data */
        if (p_j2k->m_specific_param.m_decoder.m_sot_length) {
            if (! *l_current_data) {
                /* LH: oddly enough, in this path, l_tile_len!=0.
                 * TODO: If this was consistant, we could simplify the code to only use realloc(), as realloc(0,...) default to malloc(0,...).
                 */
                *l_current_data = (OPJ_BYTE*) opj_malloc(p_j2k->m_specific_param.m_decoder.m_sot_length);
            }
            else {
                OPJ_BYTE *l_new_current_data = (OPJ_BYTE *) opj_realloc(*l_current_data, *l_tile_len + p_j2k->m_specific_param.m_decoder.m_sot_length);
                if (! l_new_current_data) {
                        opj_free(*l_current_data);
                        /*nothing more is done as l_current_data will be set to null, and just
                          afterward we enter in the error path
                          and the actual tile_len is updated (committed) at the end of the
                          function. */
                }
                *l_current_data = l_new_current_data;
            }
            
            if (*l_current_data == 00) {
                opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to decode tile\n");
                return OPJ_FALSE;
            }
        }
        else {
            l_sot_length_pb_detected = OPJ_TRUE;
        }

        /* Index */
        l_cstr_index = p_j2k->cstr_index;
        if (l_cstr_index) {
                OPJ_OFF_T l_current_pos = opj_stream_tell(p_stream) - 2;

                OPJ_UINT32 l_current_tile_part = l_cstr_index->tile_index[p_j2k->m_current_tile_number].current_tpsno;
                l_cstr_index->tile_index[p_j2k->m_current_tile_number].tp_index[l_current_tile_part].end_header =
                                l_current_pos;
                l_cstr_index->tile_index[p_j2k->m_current_tile_number].tp_index[l_current_tile_part].end_pos =
                                l_current_pos + p_j2k->m_specific_param.m_decoder.m_sot_length + 2;

                if (OPJ_FALSE == opj_j2k_add_tlmarker(p_j2k->m_current_tile_number,
                                        l_cstr_index,
                                        J2K_MS_SOD,
                                        l_current_pos,
                                        p_j2k->m_specific_param.m_decoder.m_sot_length + 2)) {
                        opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to add tl marker\n");
                        return OPJ_FALSE;
                }

                /*l_cstr_index->packno = 0;*/
        }

        /* Patch to support new PHR data */
        if (!l_sot_length_pb_detected) {
            l_current_read_size = opj_stream_read_data(
                        p_stream,
                        *l_current_data + *l_tile_len,
                        p_j2k->m_specific_param.m_decoder.m_sot_length,
                        p_manager);
        }
        else
        {
            l_current_read_size = 0;
        }

        if (l_current_read_size != p_j2k->m_specific_param.m_decoder.m_sot_length) {
                p_j2k->m_specific_param.m_decoder.m_state = J2K_STATE_NEOC;
        }
        else {
                p_j2k->m_specific_param.m_decoder.m_state = J2K_STATE_TPHSOT;
        }

        *l_tile_len += (OPJ_UINT32)l_current_read_size;

        return OPJ_TRUE;
}

 OPJ_BOOL opj_j2k_write_rgn(opj_j2k_t *p_j2k,
                            OPJ_UINT32 p_tile_no,
                            OPJ_UINT32 p_comp_no,
                            OPJ_UINT32 nb_comps,
                            opj_stream_private_t *p_stream,
                            opj_event_mgr_t * p_manager
                            )
{
        OPJ_BYTE * l_current_data = 00;
        OPJ_UINT32 l_rgn_size;
        opj_cp_t *l_cp = 00;
        opj_tcp_t *l_tcp = 00;
        opj_tccp_t *l_tccp = 00;
        OPJ_UINT32 l_comp_room;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_manager != 00);
        assert(p_stream != 00);

        l_cp = &(p_j2k->m_cp);
        l_tcp = &l_cp->tcps[p_tile_no];
        l_tccp = &l_tcp->tccps[p_comp_no];

        if (nb_comps <= 256) {
                l_comp_room = 1;
        }
        else {
                l_comp_room = 2;
        }

        l_rgn_size = 6 + l_comp_room;

        l_current_data = p_j2k->m_specific_param.m_encoder.m_header_tile_data;

        opj_write_bytes(l_current_data,J2K_MS_RGN,2);                                   /* RGN  */
        l_current_data += 2;

        opj_write_bytes(l_current_data,l_rgn_size-2,2);                                 /* Lrgn */
        l_current_data += 2;

        opj_write_bytes(l_current_data,p_comp_no,l_comp_room);                          /* Crgn */
        l_current_data+=l_comp_room;

        opj_write_bytes(l_current_data, 0,1);                                           /* Srgn */
        ++l_current_data;

        opj_write_bytes(l_current_data, (OPJ_UINT32)l_tccp->roishift,1);                            /* SPrgn */
        ++l_current_data;

        if (opj_stream_write_data(p_stream,p_j2k->m_specific_param.m_encoder.m_header_tile_data,l_rgn_size,p_manager) != l_rgn_size) {
                return OPJ_FALSE;
        }

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_write_eoc(     opj_j2k_t *p_j2k,
                            opj_stream_private_t *p_stream,
                            opj_event_mgr_t * p_manager
                            )
{
        /* preconditions */
        assert(p_j2k != 00);
        assert(p_manager != 00);
        assert(p_stream != 00);

        opj_write_bytes(p_j2k->m_specific_param.m_encoder.m_header_tile_data,J2K_MS_EOC,2);                                     /* EOC */

/* UniPG>> */
#ifdef USE_JPWL
        /* update markers struct */
        /*
        OPJ_BOOL res = j2k_add_marker(p_j2k->cstr_info, J2K_MS_EOC, p_stream_tell(p_stream) - 2, 2);
*/
#endif /* USE_JPWL */

        if ( opj_stream_write_data(p_stream,p_j2k->m_specific_param.m_encoder.m_header_tile_data,2,p_manager) != 2) {
                return OPJ_FALSE;
        }

        if ( ! opj_stream_flush(p_stream,p_manager) ) {
                return OPJ_FALSE;
        }

        return OPJ_TRUE;
}

/**
 * Reads a RGN marker (Region Of Interest)
 *
 * @param       p_header_data   the data contained in the POC box.
 * @param       p_j2k                   the jpeg2000 codec.
 * @param       p_header_size   the size of the data contained in the POC marker.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_read_rgn (opj_j2k_t *p_j2k,
                                  OPJ_BYTE * p_header_data,
                                  OPJ_UINT32 p_header_size,
                                  opj_event_mgr_t * p_manager
                                  )
{
        OPJ_UINT32 l_nb_comp;
        opj_image_t * l_image = 00;

        opj_cp_t *l_cp = 00;
        opj_tcp_t *l_tcp = 00;
        OPJ_UINT32 l_comp_room, l_comp_no, l_roi_sty;

        /* preconditions*/
        assert(p_header_data != 00);
        assert(p_j2k != 00);
        assert(p_manager != 00);

        l_image = p_j2k->m_private_image;
        l_nb_comp = l_image->numcomps;

        if (l_nb_comp <= 256) {
                l_comp_room = 1; }
        else {
                l_comp_room = 2; }

        if (p_header_size != 2 + l_comp_room) {
                opj_event_msg(p_manager, EVT_ERROR, "Error reading RGN marker\n");
                return OPJ_FALSE;
        }

        l_cp = &(p_j2k->m_cp);
        l_tcp = (p_j2k->m_specific_param.m_decoder.m_state == J2K_STATE_TPH) ?
                                &l_cp->tcps[p_j2k->m_current_tile_number] :
                                p_j2k->m_specific_param.m_decoder.m_default_tcp;

        opj_read_bytes(p_header_data,&l_comp_no,l_comp_room);           /* Crgn */
        p_header_data+=l_comp_room;
        opj_read_bytes(p_header_data,&l_roi_sty,1);                                     /* Srgn */
        ++p_header_data;

#ifdef USE_JPWL
        if (l_cp->correct) {
                /* totlen is negative or larger than the bytes left!!! */
                if (l_comp_room >= l_nb_comp) {
                        opj_event_msg(p_manager, EVT_ERROR,
                                "JPWL: bad component number in RGN (%d when there are only %d)\n",
                                l_comp_room, l_nb_comp);
                        if (!JPWL_ASSUME || JPWL_ASSUME) {
                                opj_event_msg(p_manager, EVT_ERROR, "JPWL: giving up\n");
                                return OPJ_FALSE;
                        }
                }
        };
#endif /* USE_JPWL */

        /* testcase 3635.pdf.asan.77.2930 */
        if (l_comp_no >= l_nb_comp) {
                opj_event_msg(p_manager, EVT_ERROR,
                        "bad component number in RGN (%d when there are only %d)\n",
                        l_comp_no, l_nb_comp);
                return OPJ_FALSE;
        }

        opj_read_bytes(p_header_data,(OPJ_UINT32 *) (&(l_tcp->tccps[l_comp_no].roishift)),1);   /* SPrgn */
        ++p_header_data;

        return OPJ_TRUE;

}

OPJ_FLOAT32 opj_j2k_get_tp_stride (opj_tcp_t * p_tcp)
{
        return (OPJ_FLOAT32) ((p_tcp->m_nb_tile_parts - 1) * 14);
}

OPJ_FLOAT32 opj_j2k_get_default_stride (opj_tcp_t * p_tcp)
{
    (void)p_tcp;
    return 0;
}

OPJ_BOOL opj_j2k_update_rates(  opj_j2k_t *p_j2k,
                                                            opj_stream_private_t *p_stream,
                                                            opj_event_mgr_t * p_manager )
{
        opj_cp_t * l_cp = 00;
        opj_image_t * l_image = 00;
        opj_tcp_t * l_tcp = 00;
        opj_image_comp_t * l_img_comp = 00;

        OPJ_UINT32 i,j,k;
        OPJ_INT32 l_x0,l_y0,l_x1,l_y1;
        OPJ_FLOAT32 * l_rates = 0;
        OPJ_FLOAT32 l_sot_remove;
        OPJ_UINT32 l_bits_empty, l_size_pixel;
        OPJ_UINT32 l_tile_size = 0;
        OPJ_UINT32 l_last_res;
        OPJ_FLOAT32 (* l_tp_stride_func)(opj_tcp_t *) = 00;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_manager != 00);
        assert(p_stream != 00);

        l_cp = &(p_j2k->m_cp);
        l_image = p_j2k->m_private_image;
        l_tcp = l_cp->tcps;

        l_bits_empty = 8 * l_image->comps->dx * l_image->comps->dy;
        l_size_pixel = l_image->numcomps * l_image->comps->prec;
        l_sot_remove = (OPJ_FLOAT32) opj_stream_tell(p_stream) / (OPJ_FLOAT32)(l_cp->th * l_cp->tw);

        if (l_cp->m_specific_param.m_enc.m_tp_on) {
                l_tp_stride_func = opj_j2k_get_tp_stride;
        }
        else {
                l_tp_stride_func = opj_j2k_get_default_stride;
        }

        for (i=0;i<l_cp->th;++i) {
                for (j=0;j<l_cp->tw;++j) {
                        OPJ_FLOAT32 l_offset = (OPJ_FLOAT32)(*l_tp_stride_func)(l_tcp) / (OPJ_FLOAT32)l_tcp->numlayers;

                        /* 4 borders of the tile rescale on the image if necessary */
                        l_x0 = opj_int_max((OPJ_INT32)(l_cp->tx0 + j * l_cp->tdx), (OPJ_INT32)l_image->x0);
                        l_y0 = opj_int_max((OPJ_INT32)(l_cp->ty0 + i * l_cp->tdy), (OPJ_INT32)l_image->y0);
                        l_x1 = opj_int_min((OPJ_INT32)(l_cp->tx0 + (j + 1) * l_cp->tdx), (OPJ_INT32)l_image->x1);
                        l_y1 = opj_int_min((OPJ_INT32)(l_cp->ty0 + (i + 1) * l_cp->tdy), (OPJ_INT32)l_image->y1);

                        l_rates = l_tcp->rates;

                        /* Modification of the RATE >> */
                        if (*l_rates) {
                                *l_rates =              (( (OPJ_FLOAT32) (l_size_pixel * (OPJ_UINT32)(l_x1 - l_x0) * (OPJ_UINT32)(l_y1 - l_y0)))
                                                                /
                                                                ((*l_rates) * (OPJ_FLOAT32)l_bits_empty)
                                                                )
                                                                -
                                                                l_offset;
                        }

                        ++l_rates;

                        for (k = 1; k < l_tcp->numlayers; ++k) {
                                if (*l_rates) {
                                        *l_rates =              (( (OPJ_FLOAT32) (l_size_pixel * (OPJ_UINT32)(l_x1 - l_x0) * (OPJ_UINT32)(l_y1 - l_y0)))
                                                                        /
                                                                                ((*l_rates) * (OPJ_FLOAT32)l_bits_empty)
                                                                        )
                                                                        -
                                                                        l_offset;
                                }

                                ++l_rates;
                        }

                        ++l_tcp;

                }
        }

        l_tcp = l_cp->tcps;

        for (i=0;i<l_cp->th;++i) {
                for     (j=0;j<l_cp->tw;++j) {
                        l_rates = l_tcp->rates;

                        if (*l_rates) {
                                *l_rates -= l_sot_remove;

                                if (*l_rates < 30) {
                                        *l_rates = 30;
                                }
                        }

                        ++l_rates;

                        l_last_res = l_tcp->numlayers - 1;

                        for (k = 1; k < l_last_res; ++k) {

                                if (*l_rates) {
                                        *l_rates -= l_sot_remove;

                                        if (*l_rates < *(l_rates - 1) + 10) {
                                                *l_rates  = (*(l_rates - 1)) + 20;
                                        }
                                }

                                ++l_rates;
                        }

                        if (*l_rates) {
                                *l_rates -= (l_sot_remove + 2.f);

                                if (*l_rates < *(l_rates - 1) + 10) {
                                        *l_rates  = (*(l_rates - 1)) + 20;
                                }
                        }

                        ++l_tcp;
                }
        }

        l_img_comp = l_image->comps;
        l_tile_size = 0;

        for (i=0;i<l_image->numcomps;++i) {
                l_tile_size += (        opj_uint_ceildiv(l_cp->tdx,l_img_comp->dx)
                                                        *
                                                        opj_uint_ceildiv(l_cp->tdy,l_img_comp->dy)
                                                        *
                                                        l_img_comp->prec
                                                );

                ++l_img_comp;
        }

        l_tile_size = (OPJ_UINT32) (l_tile_size * 0.1625); /* 1.3/8 = 0.1625 */

        l_tile_size += opj_j2k_get_specific_header_sizes(p_j2k);

        p_j2k->m_specific_param.m_encoder.m_encoded_tile_size = l_tile_size;
        p_j2k->m_specific_param.m_encoder.m_encoded_tile_data =
                        (OPJ_BYTE *) opj_malloc(p_j2k->m_specific_param.m_encoder.m_encoded_tile_size);
        if (p_j2k->m_specific_param.m_encoder.m_encoded_tile_data == 00) {
                return OPJ_FALSE;
        }

        if (l_cp->m_specific_param.m_enc.m_cinema) {
                p_j2k->m_specific_param.m_encoder.m_tlm_sot_offsets_buffer =
                                (OPJ_BYTE *) opj_malloc(5*p_j2k->m_specific_param.m_encoder.m_total_tile_parts);
                if (! p_j2k->m_specific_param.m_encoder.m_tlm_sot_offsets_buffer) {
                        return OPJ_FALSE;
                }

                p_j2k->m_specific_param.m_encoder.m_tlm_sot_offsets_current =
                                p_j2k->m_specific_param.m_encoder.m_tlm_sot_offsets_buffer;
        }

        return OPJ_TRUE;
}

#if 0
OPJ_BOOL opj_j2k_read_eoc (     opj_j2k_t *p_j2k,
                                                        opj_stream_private_t *p_stream,
                                                        opj_event_mgr_t * p_manager )
{
        OPJ_UINT32 i;
        opj_tcd_t * l_tcd = 00;
        OPJ_UINT32 l_nb_tiles;
        opj_tcp_t * l_tcp = 00;
        OPJ_BOOL l_success;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_manager != 00);
        assert(p_stream != 00);

        l_nb_tiles = p_j2k->m_cp.th * p_j2k->m_cp.tw;
        l_tcp = p_j2k->m_cp.tcps;

        l_tcd = opj_tcd_create(OPJ_TRUE);
        if (l_tcd == 00) {
                opj_event_msg(p_manager, EVT_ERROR, "Cannot decode tile, memory error\n");
                return OPJ_FALSE;
        }

        for (i = 0; i < l_nb_tiles; ++i) {
                if (l_tcp->m_data) {
                        if (! opj_tcd_init_decode_tile(l_tcd, i)) {
                                opj_tcd_destroy(l_tcd);
                                opj_event_msg(p_manager, EVT_ERROR, "Cannot decode tile, memory error\n");
                                return OPJ_FALSE;
                        }

                        l_success = opj_tcd_decode_tile(l_tcd, l_tcp->m_data, l_tcp->m_data_size, i, p_j2k->cstr_index);
                        /* cleanup */

                        if (! l_success) {
                                p_j2k->m_specific_param.m_decoder.m_state |= J2K_STATE_ERR;
                                break;
                        }
                }

                opj_j2k_tcp_destroy(l_tcp);
                ++l_tcp;
        }

        opj_tcd_destroy(l_tcd);
        return OPJ_TRUE;
}
#endif

OPJ_BOOL opj_j2k_get_end_header(opj_j2k_t *p_j2k,
                                                        struct opj_stream_private *p_stream,
                                                        struct opj_event_mgr * p_manager )
{
        /* preconditions */
        assert(p_j2k != 00);
        assert(p_manager != 00);
        assert(p_stream != 00);

        p_j2k->cstr_index->main_head_end = opj_stream_tell(p_stream);

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_write_mct_data_group(  opj_j2k_t *p_j2k,
                                                                        struct opj_stream_private *p_stream,
                                                                        struct opj_event_mgr * p_manager )
{
        OPJ_UINT32 i;
        opj_simple_mcc_decorrelation_data_t * l_mcc_record;
        opj_mct_data_t * l_mct_record;
        opj_tcp_t * l_tcp;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_stream != 00);
        assert(p_manager != 00);

        if (! opj_j2k_write_cbd(p_j2k,p_stream,p_manager)) {
                return OPJ_FALSE;
        }

        l_tcp = &(p_j2k->m_cp.tcps[p_j2k->m_current_tile_number]);
        l_mct_record = l_tcp->m_mct_records;

        for (i=0;i<l_tcp->m_nb_mct_records;++i) {

                if (! opj_j2k_write_mct_record(p_j2k,l_mct_record,p_stream,p_manager)) {
                        return OPJ_FALSE;
                }

                ++l_mct_record;
        }

        l_mcc_record = l_tcp->m_mcc_records;

        for     (i=0;i<l_tcp->m_nb_mcc_records;++i) {

                if (! opj_j2k_write_mcc_record(p_j2k,l_mcc_record,p_stream,p_manager)) {
                        return OPJ_FALSE;
                }

                ++l_mcc_record;
        }

        if (! opj_j2k_write_mco(p_j2k,p_stream,p_manager)) {
                return OPJ_FALSE;
        }

        return OPJ_TRUE;
}

#if 0
OPJ_BOOL opj_j2k_write_all_coc(opj_j2k_t *p_j2k,
                                                                        struct opj_stream_private *p_stream,
                                                                        struct opj_event_mgr * p_manager )
{
        OPJ_UINT32 compno;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_manager != 00);
        assert(p_stream != 00);

        for (compno = 0; compno < p_j2k->m_private_image->numcomps; ++compno)
        {
                if (! opj_j2k_write_coc(p_j2k,compno,p_stream, p_manager)) {
                        return OPJ_FALSE;
                }
        }

        return OPJ_TRUE;
}
#endif

#if 0
OPJ_BOOL opj_j2k_write_all_qcc(opj_j2k_t *p_j2k,
                                                                        struct opj_stream_private *p_stream,
                                                                        struct opj_event_mgr * p_manager )
{
        OPJ_UINT32 compno;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_manager != 00);
        assert(p_stream != 00);

        for (compno = 0; compno < p_j2k->m_private_image->numcomps; ++compno)
        {
                if (! opj_j2k_write_qcc(p_j2k,compno,p_stream, p_manager)) {
                        return OPJ_FALSE;
                }
        }

        return OPJ_TRUE;
}
#endif


OPJ_BOOL opj_j2k_write_regions( opj_j2k_t *p_j2k,
                                                        struct opj_stream_private *p_stream,
                                                        struct opj_event_mgr * p_manager )
{
        OPJ_UINT32 compno;
        const opj_tccp_t *l_tccp = 00;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_manager != 00);
        assert(p_stream != 00);

        l_tccp = p_j2k->m_cp.tcps->tccps;

        for     (compno = 0; compno < p_j2k->m_private_image->numcomps; ++compno)  {
                if (l_tccp->roishift) {

                        if (! opj_j2k_write_rgn(p_j2k,0,compno,p_j2k->m_private_image->numcomps,p_stream,p_manager)) {
                                return OPJ_FALSE;
                        }
                }

                ++l_tccp;
        }

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_write_epc(     opj_j2k_t *p_j2k,
                                                struct opj_stream_private *p_stream,
                                                struct opj_event_mgr * p_manager )
{
        opj_codestream_index_t * l_cstr_index = 00;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_manager != 00);
        assert(p_stream != 00);

        l_cstr_index = p_j2k->cstr_index;
        if (l_cstr_index) {
                l_cstr_index->codestream_size = (OPJ_UINT64)opj_stream_tell(p_stream);
                /* UniPG>> */
                /* The following adjustment is done to adjust the codestream size */
                /* if SOD is not at 0 in the buffer. Useful in case of JP2, where */
                /* the first bunch of bytes is not in the codestream              */
                l_cstr_index->codestream_size -= (OPJ_UINT64)l_cstr_index->main_head_start;
                /* <<UniPG */
        }

#ifdef USE_JPWL
        /* preparation of JPWL marker segments */
#if 0
        if(cp->epc_on) {

                /* encode according to JPWL */
                jpwl_encode(p_j2k, p_stream, image);

        }
#endif
  assert( 0 && "TODO" );
#endif /* USE_JPWL */

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_read_unk (     opj_j2k_t *p_j2k,
                                                        opj_stream_private_t *p_stream,
                                                        OPJ_UINT32 *output_marker,
                                                        opj_event_mgr_t * p_manager
                                                        )
{
        OPJ_UINT32 l_unknown_marker;
        const opj_dec_memory_marker_handler_t * l_marker_handler;
        OPJ_UINT32 l_size_unk = 2;

        /* preconditions*/
        assert(p_j2k != 00);
        assert(p_manager != 00);
        assert(p_stream != 00);

        opj_event_msg(p_manager, EVT_WARNING, "Unknown marker\n");

        while(1) {
                /* Try to read 2 bytes (the next marker ID) from stream and copy them into the buffer*/
                if (opj_stream_read_data(p_stream,p_j2k->m_specific_param.m_decoder.m_header_data,2,p_manager) != 2) {
                        opj_event_msg(p_manager, EVT_ERROR, "Stream too short\n");
                        return OPJ_FALSE;
                }

                /* read 2 bytes as the new marker ID*/
                opj_read_bytes(p_j2k->m_specific_param.m_decoder.m_header_data,&l_unknown_marker,2);

                if (!(l_unknown_marker < 0xff00)) {

                        /* Get the marker handler from the marker ID*/
                        l_marker_handler = opj_j2k_get_marker_handler(l_unknown_marker);

                        if (!(p_j2k->m_specific_param.m_decoder.m_state & l_marker_handler->states)) {
                                opj_event_msg(p_manager, EVT_ERROR, "Marker is not compliant with its position\n");
                                return OPJ_FALSE;
                        }
                        else {
                                if (l_marker_handler->id != J2K_MS_UNK) {
                                        /* Add the marker to the codestream index*/
                                        if (l_marker_handler->id != J2K_MS_SOT)
                                        {
                                                OPJ_BOOL res = opj_j2k_add_mhmarker(p_j2k->cstr_index, J2K_MS_UNK,
                                                                (OPJ_UINT32) opj_stream_tell(p_stream) - l_size_unk,
                                                                l_size_unk);
                                                if (res == OPJ_FALSE) {
                                                        opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to add mh marker\n");
                                                        return OPJ_FALSE;
                                                }
                                        }
                                        break; /* next marker is known and well located */
                                }
                                else
                                        l_size_unk += 2;
                        }
                }
        }

        *output_marker = l_marker_handler->id ;

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_write_mct_record(      opj_j2k_t *p_j2k,
                                                                opj_mct_data_t * p_mct_record,
                                                                struct opj_stream_private *p_stream,
                                                                struct opj_event_mgr * p_manager )
{
        OPJ_UINT32 l_mct_size;
        OPJ_BYTE * l_current_data = 00;
        OPJ_UINT32 l_tmp;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_manager != 00);
        assert(p_stream != 00);

        l_mct_size = 10 + p_mct_record->m_data_size;

        if (l_mct_size > p_j2k->m_specific_param.m_encoder.m_header_tile_data_size) {
                OPJ_BYTE *new_header_tile_data = (OPJ_BYTE *) opj_realloc(p_j2k->m_specific_param.m_encoder.m_header_tile_data, l_mct_size);
                if (! new_header_tile_data) {
                        opj_free(p_j2k->m_specific_param.m_encoder.m_header_tile_data);
                        p_j2k->m_specific_param.m_encoder.m_header_tile_data = NULL;
                        p_j2k->m_specific_param.m_encoder.m_header_tile_data_size = 0;
                        opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to write MCT marker\n");
                        return OPJ_FALSE;
                }
                p_j2k->m_specific_param.m_encoder.m_header_tile_data = new_header_tile_data;
                p_j2k->m_specific_param.m_encoder.m_header_tile_data_size = l_mct_size;
        }

        l_current_data = p_j2k->m_specific_param.m_encoder.m_header_tile_data;

        opj_write_bytes(l_current_data,J2K_MS_MCT,2);                                   /* MCT */
        l_current_data += 2;

        opj_write_bytes(l_current_data,l_mct_size-2,2);                                 /* Lmct */
        l_current_data += 2;

        opj_write_bytes(l_current_data,0,2);                                                    /* Zmct */
        l_current_data += 2;

        /* only one marker atm */
        l_tmp = (p_mct_record->m_index & 0xff) | (p_mct_record->m_array_type << 8) | (p_mct_record->m_element_type << 10);

        opj_write_bytes(l_current_data,l_tmp,2);
        l_current_data += 2;

        opj_write_bytes(l_current_data,0,2);                                                    /* Ymct */
        l_current_data+=2;

        memcpy(l_current_data,p_mct_record->m_data,p_mct_record->m_data_size);

        if (opj_stream_write_data(p_stream,p_j2k->m_specific_param.m_encoder.m_header_tile_data,l_mct_size,p_manager) != l_mct_size) {
                return OPJ_FALSE;
        }

        return OPJ_TRUE;
}

/**
 * Reads a MCT marker (Multiple Component Transform)
 *
 * @param       p_header_data   the data contained in the MCT box.
 * @param       p_j2k                   the jpeg2000 codec.
 * @param       p_header_size   the size of the data contained in the MCT marker.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_read_mct (      opj_j2k_t *p_j2k,
                                                                    OPJ_BYTE * p_header_data,
                                                                    OPJ_UINT32 p_header_size,
                                                                    opj_event_mgr_t * p_manager
                                    )
{
        OPJ_UINT32 i;
        opj_tcp_t *l_tcp = 00;
        OPJ_UINT32 l_tmp;
        OPJ_UINT32 l_indix;
        opj_mct_data_t * l_mct_data;

        /* preconditions */
        assert(p_header_data != 00);
        assert(p_j2k != 00);

        l_tcp = p_j2k->m_specific_param.m_decoder.m_state == J2K_STATE_TPH ?
                        &p_j2k->m_cp.tcps[p_j2k->m_current_tile_number] :
                        p_j2k->m_specific_param.m_decoder.m_default_tcp;

        if (p_header_size < 2) {
                opj_event_msg(p_manager, EVT_ERROR, "Error reading MCT marker\n");
                return OPJ_FALSE;
        }

        /* first marker */
        opj_read_bytes(p_header_data,&l_tmp,2);                         /* Zmct */
        p_header_data += 2;
        if (l_tmp != 0) {
                opj_event_msg(p_manager, EVT_WARNING, "Cannot take in charge mct data within multiple MCT records\n");
                return OPJ_TRUE;
        }

        if(p_header_size <= 6) {
                opj_event_msg(p_manager, EVT_ERROR, "Error reading MCT marker\n");
                return OPJ_FALSE;
        }

        /* Imct -> no need for other values, take the first, type is double with decorrelation x0000 1101 0000 0000*/
        opj_read_bytes(p_header_data,&l_tmp,2);                         /* Imct */
        p_header_data += 2;

        l_indix = l_tmp & 0xff;
        l_mct_data = l_tcp->m_mct_records;

        for (i=0;i<l_tcp->m_nb_mct_records;++i) {
                if (l_mct_data->m_index == l_indix) {
                        break;
                }
                ++l_mct_data;
        }

        /* NOT FOUND */
        if (i == l_tcp->m_nb_mct_records) {
                if (l_tcp->m_nb_mct_records == l_tcp->m_nb_max_mct_records) {
                        opj_mct_data_t *new_mct_records;
                        l_tcp->m_nb_max_mct_records += OPJ_J2K_MCT_DEFAULT_NB_RECORDS;

                        new_mct_records = (opj_mct_data_t *) opj_realloc(l_tcp->m_mct_records, l_tcp->m_nb_max_mct_records * sizeof(opj_mct_data_t));
                        if (! new_mct_records) {
                                opj_free(l_tcp->m_mct_records);
                                l_tcp->m_mct_records = NULL;
                                l_tcp->m_nb_max_mct_records = 0;
                                l_tcp->m_nb_mct_records = 0;
                                opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to read MCT marker\n");
                                return OPJ_FALSE;
                        }
                        l_tcp->m_mct_records = new_mct_records;
                        l_mct_data = l_tcp->m_mct_records + l_tcp->m_nb_mct_records;
                        memset(l_mct_data ,0,(l_tcp->m_nb_max_mct_records - l_tcp->m_nb_mct_records) * sizeof(opj_mct_data_t));
                }

                l_mct_data = l_tcp->m_mct_records + l_tcp->m_nb_mct_records;
        }

        if (l_mct_data->m_data) {
                opj_free(l_mct_data->m_data);
                l_mct_data->m_data = 00;
        }

        l_mct_data->m_index = l_indix;
        l_mct_data->m_array_type = (J2K_MCT_ARRAY_TYPE)((l_tmp  >> 8) & 3);
        l_mct_data->m_element_type = (J2K_MCT_ELEMENT_TYPE)((l_tmp  >> 10) & 3);

        opj_read_bytes(p_header_data,&l_tmp,2);                         /* Ymct */
        p_header_data+=2;
        if (l_tmp != 0) {
                opj_event_msg(p_manager, EVT_WARNING, "Cannot take in charge multiple MCT markers\n");
                return OPJ_TRUE;
        }

        p_header_size -= 6;

        l_mct_data->m_data = (OPJ_BYTE*)opj_malloc(p_header_size);
        if (! l_mct_data->m_data) {
                opj_event_msg(p_manager, EVT_ERROR, "Error reading MCT marker\n");
                return OPJ_FALSE;
        }
        memcpy(l_mct_data->m_data,p_header_data,p_header_size);

        l_mct_data->m_data_size = p_header_size;
        ++l_tcp->m_nb_mct_records;

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_write_mcc_record(      opj_j2k_t *p_j2k,
                                                                struct opj_simple_mcc_decorrelation_data * p_mcc_record,
                                                                struct opj_stream_private *p_stream,
                                                                struct opj_event_mgr * p_manager )
{
        OPJ_UINT32 i;
        OPJ_UINT32 l_mcc_size;
        OPJ_BYTE * l_current_data = 00;
        OPJ_UINT32 l_nb_bytes_for_comp;
        OPJ_UINT32 l_mask;
        OPJ_UINT32 l_tmcc;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_manager != 00);
        assert(p_stream != 00);

        if (p_mcc_record->m_nb_comps > 255 ) {
        l_nb_bytes_for_comp = 2;
                l_mask = 0x8000;
        }
        else {
                l_nb_bytes_for_comp = 1;
                l_mask = 0;
        }

        l_mcc_size = p_mcc_record->m_nb_comps * 2 * l_nb_bytes_for_comp + 19;
        if (l_mcc_size > p_j2k->m_specific_param.m_encoder.m_header_tile_data_size)
        {
                OPJ_BYTE *new_header_tile_data = (OPJ_BYTE *) opj_realloc(p_j2k->m_specific_param.m_encoder.m_header_tile_data, l_mcc_size);
                if (! new_header_tile_data) {
                        opj_free(p_j2k->m_specific_param.m_encoder.m_header_tile_data);
                        p_j2k->m_specific_param.m_encoder.m_header_tile_data = NULL;
                        p_j2k->m_specific_param.m_encoder.m_header_tile_data_size = 0;
                        opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to write MCC marker\n");
                        return OPJ_FALSE;
                }
                p_j2k->m_specific_param.m_encoder.m_header_tile_data = new_header_tile_data;
                p_j2k->m_specific_param.m_encoder.m_header_tile_data_size = l_mcc_size;
        }

        l_current_data = p_j2k->m_specific_param.m_encoder.m_header_tile_data;

        opj_write_bytes(l_current_data,J2K_MS_MCC,2);                                   /* MCC */
        l_current_data += 2;

        opj_write_bytes(l_current_data,l_mcc_size-2,2);                                 /* Lmcc */
        l_current_data += 2;

        /* first marker */
        opj_write_bytes(l_current_data,0,2);                                    /* Zmcc */
        l_current_data += 2;

        opj_write_bytes(l_current_data,p_mcc_record->m_index,1);                                        /* Imcc -> no need for other values, take the first */
        ++l_current_data;

        /* only one marker atm */
        opj_write_bytes(l_current_data,0,2);                                    /* Ymcc */
        l_current_data+=2;

        opj_write_bytes(l_current_data,1,2);                                    /* Qmcc -> number of collections -> 1 */
        l_current_data+=2;

        opj_write_bytes(l_current_data,0x1,1);                                  /* Xmcci type of component transformation -> array based decorrelation */
        ++l_current_data;

        opj_write_bytes(l_current_data,p_mcc_record->m_nb_comps | l_mask,2);    /* Nmcci number of input components involved and size for each component offset = 8 bits */
        l_current_data+=2;

        for (i=0;i<p_mcc_record->m_nb_comps;++i) {
                opj_write_bytes(l_current_data,i,l_nb_bytes_for_comp);                          /* Cmccij Component offset*/
                l_current_data+=l_nb_bytes_for_comp;
        }

        opj_write_bytes(l_current_data,p_mcc_record->m_nb_comps|l_mask,2);      /* Mmcci number of output components involved and size for each component offset = 8 bits */
        l_current_data+=2;

        for (i=0;i<p_mcc_record->m_nb_comps;++i)
        {
                opj_write_bytes(l_current_data,i,l_nb_bytes_for_comp);                          /* Wmccij Component offset*/
                l_current_data+=l_nb_bytes_for_comp;
        }

        l_tmcc = ((!p_mcc_record->m_is_irreversible)&1)<<16;

        if (p_mcc_record->m_decorrelation_array) {
                l_tmcc |= p_mcc_record->m_decorrelation_array->m_index;
        }

        if (p_mcc_record->m_offset_array) {
                l_tmcc |= ((p_mcc_record->m_offset_array->m_index)<<8);
        }

        opj_write_bytes(l_current_data,l_tmcc,3);       /* Tmcci : use MCT defined as number 1 and irreversible array based. */
        l_current_data+=3;

        if (opj_stream_write_data(p_stream,p_j2k->m_specific_param.m_encoder.m_header_tile_data,l_mcc_size,p_manager) != l_mcc_size) {
                return OPJ_FALSE;
        }

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_read_mcc (     opj_j2k_t *p_j2k,
                                                OPJ_BYTE * p_header_data,
                                                OPJ_UINT32 p_header_size,
                                                opj_event_mgr_t * p_manager )
{
        OPJ_UINT32 i,j;
        OPJ_UINT32 l_tmp;
        OPJ_UINT32 l_indix;
        opj_tcp_t * l_tcp;
        opj_simple_mcc_decorrelation_data_t * l_mcc_record;
        opj_mct_data_t * l_mct_data;
        OPJ_UINT32 l_nb_collections;
        OPJ_UINT32 l_nb_comps;
        OPJ_UINT32 l_nb_bytes_by_comp;

        /* preconditions */
        assert(p_header_data != 00);
        assert(p_j2k != 00);
        assert(p_manager != 00);

        l_tcp = p_j2k->m_specific_param.m_decoder.m_state == J2K_STATE_TPH ?
                        &p_j2k->m_cp.tcps[p_j2k->m_current_tile_number] :
                        p_j2k->m_specific_param.m_decoder.m_default_tcp;

        if (p_header_size < 2) {
                opj_event_msg(p_manager, EVT_ERROR, "Error reading MCC marker\n");
                return OPJ_FALSE;
        }

        /* first marker */
        opj_read_bytes(p_header_data,&l_tmp,2);                         /* Zmcc */
        p_header_data += 2;
        if (l_tmp != 0) {
                opj_event_msg(p_manager, EVT_WARNING, "Cannot take in charge multiple data spanning\n");
                return OPJ_TRUE;
        }

        if (p_header_size < 7) {
                opj_event_msg(p_manager, EVT_ERROR, "Error reading MCC marker\n");
                return OPJ_FALSE;
        }

        opj_read_bytes(p_header_data,&l_indix,1); /* Imcc -> no need for other values, take the first */
        ++p_header_data;

        l_mcc_record = l_tcp->m_mcc_records;

        for(i=0;i<l_tcp->m_nb_mcc_records;++i) {
                if (l_mcc_record->m_index == l_indix) {
                        break;
                }
                ++l_mcc_record;
        }

        /** NOT FOUND */
        if (i == l_tcp->m_nb_mcc_records) {
                if (l_tcp->m_nb_mcc_records == l_tcp->m_nb_max_mcc_records) {
                        opj_simple_mcc_decorrelation_data_t *new_mcc_records;
                        l_tcp->m_nb_max_mcc_records += OPJ_J2K_MCC_DEFAULT_NB_RECORDS;

                        new_mcc_records = (opj_simple_mcc_decorrelation_data_t *) opj_realloc(
                                        l_tcp->m_mcc_records, l_tcp->m_nb_max_mcc_records * sizeof(opj_simple_mcc_decorrelation_data_t));
                        if (! new_mcc_records) {
                                opj_free(l_tcp->m_mcc_records);
                                l_tcp->m_mcc_records = NULL;
                                l_tcp->m_nb_max_mcc_records = 0;
                                l_tcp->m_nb_mcc_records = 0;
                                opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to read MCC marker\n");
                                return OPJ_FALSE;
                        }
                        l_tcp->m_mcc_records = new_mcc_records;
                        l_mcc_record = l_tcp->m_mcc_records + l_tcp->m_nb_mcc_records;
                        memset(l_mcc_record,0,(l_tcp->m_nb_max_mcc_records-l_tcp->m_nb_mcc_records) * sizeof(opj_simple_mcc_decorrelation_data_t));
                }
                l_mcc_record = l_tcp->m_mcc_records + l_tcp->m_nb_mcc_records;
        }
        l_mcc_record->m_index = l_indix;

        /* only one marker atm */
        opj_read_bytes(p_header_data,&l_tmp,2);                         /* Ymcc */
        p_header_data+=2;
        if (l_tmp != 0) {
                opj_event_msg(p_manager, EVT_WARNING, "Cannot take in charge multiple data spanning\n");
                return OPJ_TRUE;
        }

        opj_read_bytes(p_header_data,&l_nb_collections,2);                              /* Qmcc -> number of collections -> 1 */
        p_header_data+=2;

        if (l_nb_collections > 1) {
                opj_event_msg(p_manager, EVT_WARNING, "Cannot take in charge multiple collections\n");
                return OPJ_TRUE;
        }

        p_header_size -= 7;

        for (i=0;i<l_nb_collections;++i) {
                if (p_header_size < 3) {
                        opj_event_msg(p_manager, EVT_ERROR, "Error reading MCC marker\n");
                        return OPJ_FALSE;
                }

                opj_read_bytes(p_header_data,&l_tmp,1); /* Xmcci type of component transformation -> array based decorrelation */
                ++p_header_data;

                if (l_tmp != 1) {
                        opj_event_msg(p_manager, EVT_WARNING, "Cannot take in charge collections other than array decorrelation\n");
                        return OPJ_TRUE;
                }

                opj_read_bytes(p_header_data,&l_nb_comps,2);

                p_header_data+=2;
                p_header_size-=3;

                l_nb_bytes_by_comp = 1 + (l_nb_comps>>15);
                l_mcc_record->m_nb_comps = l_nb_comps & 0x7fff;

                if (p_header_size < (l_nb_bytes_by_comp * l_mcc_record->m_nb_comps + 2)) {
                        opj_event_msg(p_manager, EVT_ERROR, "Error reading MCC marker\n");
                        return OPJ_FALSE;
                }

                p_header_size -= (l_nb_bytes_by_comp * l_mcc_record->m_nb_comps + 2);

                for (j=0;j<l_mcc_record->m_nb_comps;++j) {
                        opj_read_bytes(p_header_data,&l_tmp,l_nb_bytes_by_comp);        /* Cmccij Component offset*/
                        p_header_data+=l_nb_bytes_by_comp;

                        if (l_tmp != j) {
                                opj_event_msg(p_manager, EVT_WARNING, "Cannot take in charge collections with indix shuffle\n");
                                return OPJ_TRUE;
                        }
                }

                opj_read_bytes(p_header_data,&l_nb_comps,2);
                p_header_data+=2;

                l_nb_bytes_by_comp = 1 + (l_nb_comps>>15);
                l_nb_comps &= 0x7fff;

                if (l_nb_comps != l_mcc_record->m_nb_comps) {
                        opj_event_msg(p_manager, EVT_WARNING, "Cannot take in charge collections without same number of indixes\n");
                        return OPJ_TRUE;
                }

                if (p_header_size < (l_nb_bytes_by_comp * l_mcc_record->m_nb_comps + 3)) {
                        opj_event_msg(p_manager, EVT_ERROR, "Error reading MCC marker\n");
                        return OPJ_FALSE;
                }

                p_header_size -= (l_nb_bytes_by_comp * l_mcc_record->m_nb_comps + 3);

                for (j=0;j<l_mcc_record->m_nb_comps;++j) {
                        opj_read_bytes(p_header_data,&l_tmp,l_nb_bytes_by_comp);        /* Wmccij Component offset*/
                        p_header_data+=l_nb_bytes_by_comp;

                        if (l_tmp != j) {
                                opj_event_msg(p_manager, EVT_WARNING, "Cannot take in charge collections with indix shuffle\n");
                                return OPJ_TRUE;
                        }
                }

                opj_read_bytes(p_header_data,&l_tmp,3); /* Wmccij Component offset*/
                p_header_data += 3;

                l_mcc_record->m_is_irreversible = ! ((l_tmp>>16) & 1);
                l_mcc_record->m_decorrelation_array = 00;
                l_mcc_record->m_offset_array = 00;

                l_indix = l_tmp & 0xff;
                if (l_indix != 0) {
                        l_mct_data = l_tcp->m_mct_records;
                        for (j=0;j<l_tcp->m_nb_mct_records;++j) {
                                if (l_mct_data->m_index == l_indix) {
                                        l_mcc_record->m_decorrelation_array = l_mct_data;
                                        break;
                                }
                                ++l_mct_data;
                        }

                        if (l_mcc_record->m_decorrelation_array == 00) {
                                opj_event_msg(p_manager, EVT_ERROR, "Error reading MCC marker\n");
                                return OPJ_FALSE;
                        }
                }

                l_indix = (l_tmp >> 8) & 0xff;
                if (l_indix != 0) {
                        l_mct_data = l_tcp->m_mct_records;
                        for (j=0;j<l_tcp->m_nb_mct_records;++j) {
                                if (l_mct_data->m_index == l_indix) {
                                        l_mcc_record->m_offset_array = l_mct_data;
                                        break;
                                }
                                ++l_mct_data;
                        }

                        if (l_mcc_record->m_offset_array == 00) {
                                opj_event_msg(p_manager, EVT_ERROR, "Error reading MCC marker\n");
                                return OPJ_FALSE;
                        }
                }
        }

        if (p_header_size != 0) {
                opj_event_msg(p_manager, EVT_ERROR, "Error reading MCC marker\n");
                return OPJ_FALSE;
        }

        ++l_tcp->m_nb_mcc_records;

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_write_mco(     opj_j2k_t *p_j2k,
                                                struct opj_stream_private *p_stream,
                                                struct opj_event_mgr * p_manager
                                  )
{
        OPJ_BYTE * l_current_data = 00;
        OPJ_UINT32 l_mco_size;
        opj_tcp_t * l_tcp = 00;
        opj_simple_mcc_decorrelation_data_t * l_mcc_record;
        OPJ_UINT32 i;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_manager != 00);
        assert(p_stream != 00);

        l_tcp =&(p_j2k->m_cp.tcps[p_j2k->m_current_tile_number]);
        l_current_data = p_j2k->m_specific_param.m_encoder.m_header_tile_data;

        l_mco_size = 5 + l_tcp->m_nb_mcc_records;
        if (l_mco_size > p_j2k->m_specific_param.m_encoder.m_header_tile_data_size) {

                OPJ_BYTE *new_header_tile_data = (OPJ_BYTE *) opj_realloc(p_j2k->m_specific_param.m_encoder.m_header_tile_data, l_mco_size);
                if (! new_header_tile_data) {
                        opj_free(p_j2k->m_specific_param.m_encoder.m_header_tile_data);
                        p_j2k->m_specific_param.m_encoder.m_header_tile_data = NULL;
                        p_j2k->m_specific_param.m_encoder.m_header_tile_data_size = 0;
                        opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to write MCO marker\n");
                        return OPJ_FALSE;
                }
                p_j2k->m_specific_param.m_encoder.m_header_tile_data = new_header_tile_data;
                p_j2k->m_specific_param.m_encoder.m_header_tile_data_size = l_mco_size;
        }

        opj_write_bytes(l_current_data,J2K_MS_MCO,2);                   /* MCO */
        l_current_data += 2;

        opj_write_bytes(l_current_data,l_mco_size-2,2);                 /* Lmco */
        l_current_data += 2;

        opj_write_bytes(l_current_data,l_tcp->m_nb_mcc_records,1);      /* Nmco : only one tranform stage*/
        ++l_current_data;

        l_mcc_record = l_tcp->m_mcc_records;
        for     (i=0;i<l_tcp->m_nb_mcc_records;++i) {
                opj_write_bytes(l_current_data,l_mcc_record->m_index,1);/* Imco -> use the mcc indicated by 1*/
                ++l_current_data;

                ++l_mcc_record;
        }

        if (opj_stream_write_data(p_stream,p_j2k->m_specific_param.m_encoder.m_header_tile_data,l_mco_size,p_manager) != l_mco_size) {
                return OPJ_FALSE;
        }

        return OPJ_TRUE;
}

/**
 * Reads a MCO marker (Multiple Component Transform Ordering)
 *
 * @param       p_header_data   the data contained in the MCO box.
 * @param       p_j2k                   the jpeg2000 codec.
 * @param       p_header_size   the size of the data contained in the MCO marker.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_read_mco (      opj_j2k_t *p_j2k,
                                                                    OPJ_BYTE * p_header_data,
                                                                    OPJ_UINT32 p_header_size,
                                                                    opj_event_mgr_t * p_manager
                                    )
{
        OPJ_UINT32 l_tmp, i;
        OPJ_UINT32 l_nb_stages;
        opj_tcp_t * l_tcp;
        opj_tccp_t * l_tccp;
        opj_image_t * l_image;

        /* preconditions */
        assert(p_header_data != 00);
        assert(p_j2k != 00);
        assert(p_manager != 00);

        l_image = p_j2k->m_private_image;
        l_tcp = p_j2k->m_specific_param.m_decoder.m_state == J2K_STATE_TPH ?
                        &p_j2k->m_cp.tcps[p_j2k->m_current_tile_number] :
                        p_j2k->m_specific_param.m_decoder.m_default_tcp;

        if (p_header_size < 1) {
                opj_event_msg(p_manager, EVT_ERROR, "Error reading MCO marker\n");
                return OPJ_FALSE;
        }

        opj_read_bytes(p_header_data,&l_nb_stages,1);                           /* Nmco : only one tranform stage*/
        ++p_header_data;

        if (l_nb_stages > 1) {
                opj_event_msg(p_manager, EVT_WARNING, "Cannot take in charge multiple transformation stages.\n");
                return OPJ_TRUE;
        }

        if (p_header_size != l_nb_stages + 1) {
                opj_event_msg(p_manager, EVT_WARNING, "Error reading MCO marker\n");
                return OPJ_FALSE;
        }

        l_tccp = l_tcp->tccps;

        for (i=0;i<l_image->numcomps;++i) {
                l_tccp->m_dc_level_shift = 0;
                ++l_tccp;
        }

        if (l_tcp->m_mct_decoding_matrix) {
                opj_free(l_tcp->m_mct_decoding_matrix);
                l_tcp->m_mct_decoding_matrix = 00;
        }

        for (i=0;i<l_nb_stages;++i) {
                opj_read_bytes(p_header_data,&l_tmp,1);
                ++p_header_data;

                if (! opj_j2k_add_mct(l_tcp,p_j2k->m_private_image,l_tmp)) {
                        return OPJ_FALSE;
                }
        }

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_add_mct(opj_tcp_t * p_tcp, opj_image_t * p_image, OPJ_UINT32 p_index)
{
        OPJ_UINT32 i;
        opj_simple_mcc_decorrelation_data_t * l_mcc_record;
        opj_mct_data_t * l_deco_array, * l_offset_array;
        OPJ_UINT32 l_data_size,l_mct_size, l_offset_size;
        OPJ_UINT32 l_nb_elem;
        OPJ_UINT32 * l_offset_data, * l_current_offset_data;
        opj_tccp_t * l_tccp;

        /* preconditions */
        assert(p_tcp != 00);

        l_mcc_record = p_tcp->m_mcc_records;

        for (i=0;i<p_tcp->m_nb_mcc_records;++i) {
                if (l_mcc_record->m_index == p_index) {
                        break;
                }
        }

        if (i==p_tcp->m_nb_mcc_records) {
                /** element discarded **/
                return OPJ_TRUE;
        }

        if (l_mcc_record->m_nb_comps != p_image->numcomps) {
                /** do not support number of comps != image */
                return OPJ_TRUE;
        }

        l_deco_array = l_mcc_record->m_decorrelation_array;

        if (l_deco_array) {
                l_data_size = MCT_ELEMENT_SIZE[l_deco_array->m_element_type] * p_image->numcomps * p_image->numcomps;
                if (l_deco_array->m_data_size != l_data_size) {
                        return OPJ_FALSE;
                }

                l_nb_elem = p_image->numcomps * p_image->numcomps;
                l_mct_size = l_nb_elem * (OPJ_UINT32)sizeof(OPJ_FLOAT32);
                p_tcp->m_mct_decoding_matrix = (OPJ_FLOAT32*)opj_malloc(l_mct_size);

                if (! p_tcp->m_mct_decoding_matrix ) {
                        return OPJ_FALSE;
                }

                j2k_mct_read_functions_to_float[l_deco_array->m_element_type](l_deco_array->m_data,p_tcp->m_mct_decoding_matrix,l_nb_elem);
        }

        l_offset_array = l_mcc_record->m_offset_array;

        if (l_offset_array) {
                l_data_size = MCT_ELEMENT_SIZE[l_offset_array->m_element_type] * p_image->numcomps;
                if (l_offset_array->m_data_size != l_data_size) {
                        return OPJ_FALSE;
                }

                l_nb_elem = p_image->numcomps;
                l_offset_size = l_nb_elem * (OPJ_UINT32)sizeof(OPJ_UINT32);
                l_offset_data = (OPJ_UINT32*)opj_malloc(l_offset_size);

                if (! l_offset_data ) {
                        return OPJ_FALSE;
                }

                j2k_mct_read_functions_to_int32[l_offset_array->m_element_type](l_offset_array->m_data,l_offset_data,l_nb_elem);

                l_tccp = p_tcp->tccps;
                l_current_offset_data = l_offset_data;

                for (i=0;i<p_image->numcomps;++i) {
                        l_tccp->m_dc_level_shift = (OPJ_INT32)*(l_current_offset_data++);
                        ++l_tccp;
                }

                opj_free(l_offset_data);
        }

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_write_cbd( opj_j2k_t *p_j2k,
                                                struct opj_stream_private *p_stream,
                                                struct opj_event_mgr * p_manager )
{
        OPJ_UINT32 i;
        OPJ_UINT32 l_cbd_size;
        OPJ_BYTE * l_current_data = 00;
        opj_image_t *l_image = 00;
        opj_image_comp_t * l_comp = 00;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_manager != 00);
        assert(p_stream != 00);

        l_image = p_j2k->m_private_image;
        l_cbd_size = 6 + p_j2k->m_private_image->numcomps;

        if (l_cbd_size > p_j2k->m_specific_param.m_encoder.m_header_tile_data_size) {
                OPJ_BYTE *new_header_tile_data = (OPJ_BYTE *) opj_realloc(p_j2k->m_specific_param.m_encoder.m_header_tile_data, l_cbd_size);
                if (! new_header_tile_data) {
                        opj_free(p_j2k->m_specific_param.m_encoder.m_header_tile_data);
                        p_j2k->m_specific_param.m_encoder.m_header_tile_data = NULL;
                        p_j2k->m_specific_param.m_encoder.m_header_tile_data_size = 0;
                        opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to write CBD marker\n");
                        return OPJ_FALSE;
                }
                p_j2k->m_specific_param.m_encoder.m_header_tile_data = new_header_tile_data;
                p_j2k->m_specific_param.m_encoder.m_header_tile_data_size = l_cbd_size;
        }

        l_current_data = p_j2k->m_specific_param.m_encoder.m_header_tile_data;

        opj_write_bytes(l_current_data,J2K_MS_CBD,2);                   /* CBD */
        l_current_data += 2;

        opj_write_bytes(l_current_data,l_cbd_size-2,2);                 /* L_CBD */
        l_current_data += 2;

        opj_write_bytes(l_current_data,l_image->numcomps, 2);           /* Ncbd */
        l_current_data+=2;

        l_comp = l_image->comps;

        for (i=0;i<l_image->numcomps;++i) {
                opj_write_bytes(l_current_data, (l_comp->sgnd << 7) | (l_comp->prec - 1), 1);           /* Component bit depth */
                ++l_current_data;

                ++l_comp;
        }

        if (opj_stream_write_data(p_stream,p_j2k->m_specific_param.m_encoder.m_header_tile_data,l_cbd_size,p_manager) != l_cbd_size) {
                return OPJ_FALSE;
        }

        return OPJ_TRUE;
}

/**
 * Reads a CBD marker (Component bit depth definition)
 * @param       p_header_data   the data contained in the CBD box.
 * @param       p_j2k                   the jpeg2000 codec.
 * @param       p_header_size   the size of the data contained in the CBD marker.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_read_cbd (      opj_j2k_t *p_j2k,
                                                                OPJ_BYTE * p_header_data,
                                                                OPJ_UINT32 p_header_size,
                                                                opj_event_mgr_t * p_manager
                                    )
{
        OPJ_UINT32 l_nb_comp,l_num_comp;
        OPJ_UINT32 l_comp_def;
        OPJ_UINT32 i;
        opj_image_comp_t * l_comp = 00;

        /* preconditions */
        assert(p_header_data != 00);
        assert(p_j2k != 00);
        assert(p_manager != 00);

        l_num_comp = p_j2k->m_private_image->numcomps;

        if (p_header_size != (p_j2k->m_private_image->numcomps + 2)) {
                opj_event_msg(p_manager, EVT_ERROR, "Crror reading CBD marker\n");
                return OPJ_FALSE;
        }

        opj_read_bytes(p_header_data,&l_nb_comp,2);                             /* Ncbd */
        p_header_data+=2;

        if (l_nb_comp != l_num_comp) {
                opj_event_msg(p_manager, EVT_ERROR, "Crror reading CBD marker\n");
                return OPJ_FALSE;
        }

        l_comp = p_j2k->m_private_image->comps;
        for (i=0;i<l_num_comp;++i) {
                opj_read_bytes(p_header_data,&l_comp_def,1);                    /* Component bit depth */
                ++p_header_data;
        l_comp->sgnd = (l_comp_def>>7) & 1;
                l_comp->prec = (l_comp_def&0x7f) + 1;
                ++l_comp;
        }

        return OPJ_TRUE;
}

/* ----------------------------------------------------------------------- */
/* J2K / JPT decoder interface                                             */
/* ----------------------------------------------------------------------- */

void opj_j2k_setup_decoder(opj_j2k_t *j2k, opj_dparameters_t *parameters)
{
        if(j2k && parameters) {
                j2k->m_cp.m_specific_param.m_dec.m_layer = parameters->cp_layer;
                j2k->m_cp.m_specific_param.m_dec.m_reduce = parameters->cp_reduce;

#ifdef USE_JPWL
                j2k->m_cp.correct = parameters->jpwl_correct;
                j2k->m_cp.exp_comps = parameters->jpwl_exp_comps;
                j2k->m_cp.max_tiles = parameters->jpwl_max_tiles;
#endif /* USE_JPWL */
        }
}

/* ----------------------------------------------------------------------- */
/* J2K encoder interface                                                       */
/* ----------------------------------------------------------------------- */

opj_j2k_t* opj_j2k_create_compress(void)
{
        opj_j2k_t *l_j2k = (opj_j2k_t*) opj_malloc(sizeof(opj_j2k_t));
        if (!l_j2k) {
                return NULL;
        }

        memset(l_j2k,0,sizeof(opj_j2k_t));

        l_j2k->m_is_decoder = 0;
        l_j2k->m_cp.m_is_decoder = 0;

        l_j2k->m_specific_param.m_encoder.m_header_tile_data = (OPJ_BYTE *) opj_malloc(OPJ_J2K_DEFAULT_HEADER_SIZE);
        if (! l_j2k->m_specific_param.m_encoder.m_header_tile_data) {
                opj_j2k_destroy(l_j2k);
                return NULL;
        }

        l_j2k->m_specific_param.m_encoder.m_header_tile_data_size = OPJ_J2K_DEFAULT_HEADER_SIZE;

        /* validation list creation*/
        l_j2k->m_validation_list = opj_procedure_list_create();
        if (! l_j2k->m_validation_list) {
                opj_j2k_destroy(l_j2k);
                return NULL;
        }

        /* execution list creation*/
        l_j2k->m_procedure_list = opj_procedure_list_create();
        if (! l_j2k->m_procedure_list) {
                opj_j2k_destroy(l_j2k);
                return NULL;
        }

        return l_j2k;
}

int opj_j2k_initialise_4K_poc(opj_poc_t *POC, int numres){
    POC[0].tile  = 1;
    POC[0].resno0  = 0;
    POC[0].compno0 = 0;
    POC[0].layno1  = 1;
    POC[0].resno1  = (OPJ_UINT32)(numres-1);
    POC[0].compno1 = 3;
    POC[0].prg1 = OPJ_CPRL;
    POC[1].tile  = 1;
    POC[1].resno0  = (OPJ_UINT32)(numres-1);
    POC[1].compno0 = 0;
    POC[1].layno1  = 1;
    POC[1].resno1  = (OPJ_UINT32)numres;
    POC[1].compno1 = 3;
    POC[1].prg1 = OPJ_CPRL;
    return 2;
}

void opj_j2k_set_cinema_parameters(opj_cparameters_t *parameters, opj_image_t *image, opj_event_mgr_t *p_manager)
{
    /* Configure cinema parameters */
    OPJ_FLOAT32 max_rate = 0;
    OPJ_FLOAT32 temp_rate = 0;
    int i;

    /* profile (Rsiz) */
    switch (parameters->cp_cinema){
    case OPJ_CINEMA2K_24:
    case OPJ_CINEMA2K_48:
        parameters->cp_rsiz = OPJ_CINEMA2K;
        break;
    case OPJ_CINEMA4K_24:
        parameters->cp_rsiz = OPJ_CINEMA4K;
        break;
    case OPJ_OFF:
        assert(0);
        break;
    }

    /* No tiling */
    parameters->tile_size_on = OPJ_FALSE;
    parameters->cp_tdx=1;
    parameters->cp_tdy=1;

    /* One tile part for each component */
    parameters->tp_flag = 'C';
    parameters->tp_on = 1;

    /* Tile and Image shall be at (0,0) */
    parameters->cp_tx0 = 0;
    parameters->cp_ty0 = 0;
    parameters->image_offset_x0 = 0;
    parameters->image_offset_y0 = 0;

    /* Codeblock size= 32*32 */
    parameters->cblockw_init = 32;
    parameters->cblockh_init = 32;

    /* Codeblock style: no mode switch enabled */
    parameters->mode = 0;

    /* No ROI */
    parameters->roi_compno = -1;

    /* No subsampling */
    parameters->subsampling_dx = 1;
    parameters->subsampling_dy = 1;

    /* 9-7 transform */
    parameters->irreversible = 1;

    /* Number of layers */
    if (parameters->tcp_numlayers > 1){
        opj_event_msg(p_manager, EVT_WARNING,
                "JPEG 2000 Profile-3 and 4 (2k/4k dc profile) requires:\n"
                "1 single quality layer"
                "-> Number of layers forced to 1 (rather than %d)\n",
                parameters->tcp_numlayers);
        parameters->tcp_numlayers = 1;
    }

    /* Resolution levels */
    switch (parameters->cp_cinema){
    case OPJ_CINEMA2K_24:
    case OPJ_CINEMA2K_48:
        if(parameters->numresolution > 6){
            opj_event_msg(p_manager, EVT_WARNING,
                    "JPEG 2000 Profile-3 (2k dc profile) requires:\n"
                    "Number of decomposition levels <= 5\n"
                    "-> Number of decomposition levels forced to 5 (rather than %d)\n",
                    parameters->numresolution+1);
            parameters->numresolution = 6;
        }
        break;
    case OPJ_CINEMA4K_24:
        if(parameters->numresolution < 2){
            opj_event_msg(p_manager, EVT_WARNING,
                    "JPEG 2000 Profile-4 (4k dc profile) requires:\n"
                    "Number of decomposition levels >= 1 && <= 6\n"
                    "-> Number of decomposition levels forced to 1 (rather than %d)\n",
                    parameters->numresolution+1);
            parameters->numresolution = 1;
        }else if(parameters->numresolution > 7){
            opj_event_msg(p_manager, EVT_WARNING,
                    "JPEG 2000 Profile-4 (4k dc profile) requires:\n"
                    "Number of decomposition levels >= 1 && <= 6\n"
                    "-> Number of decomposition levels forced to 6 (rather than %d)\n",
                    parameters->numresolution+1);
            parameters->numresolution = 7;
        }
        break;
    default :
        break;
    }

    /* Precincts */
    parameters->csty |= 0x01;
    parameters->res_spec = parameters->numresolution-1;
    for (i = 0; i<parameters->res_spec; i++) {
        parameters->prcw_init[i] = 256;
        parameters->prch_init[i] = 256;
    }

    /* The progression order shall be CPRL */
    parameters->prog_order = OPJ_CPRL;

    /* Progression order changes for 4K, disallowed for 2K */
    if (parameters->cp_cinema == OPJ_CINEMA4K_24) {
        parameters->numpocs = (OPJ_UINT32)opj_j2k_initialise_4K_poc(parameters->POC,parameters->numresolution);
    } else {
        parameters->numpocs = 0;
    }

    /* Limited bit-rate */
    parameters->cp_disto_alloc = 1;
    switch (parameters->cp_cinema){
    case OPJ_CINEMA2K_24:
    case OPJ_CINEMA4K_24:
        max_rate = (OPJ_FLOAT32) (image->numcomps * image->comps[0].w * image->comps[0].h * image->comps[0].prec)/
                (OPJ_FLOAT32)(CINEMA_24_CS * 8 * image->comps[0].dx * image->comps[0].dy);
        if (parameters->tcp_rates[0] == 0){
            parameters->tcp_rates[0] = max_rate;
        }else{
            temp_rate =(OPJ_FLOAT32)(image->numcomps * image->comps[0].w * image->comps[0].h * image->comps[0].prec)/
                    (parameters->tcp_rates[0] * 8 * (OPJ_FLOAT32)image->comps[0].dx * (OPJ_FLOAT32)image->comps[0].dy);
            if (temp_rate > CINEMA_24_CS ){
                opj_event_msg(p_manager, EVT_WARNING,
                        "JPEG 2000 Profile-3 and 4 (2k/4k dc profile) requires:\n"
                        "Maximum 1302083 compressed bytes @ 24fps\n"
                        "-> Specified rate (%3.1f) exceeds this limit. Rate will be forced to %3.1f.\n",
                        parameters->tcp_rates[0], max_rate);
                parameters->tcp_rates[0]= max_rate;
            }else{
                opj_event_msg(p_manager, EVT_WARNING,
                        "JPEG 2000 Profile-3 and 4 (2k/4k dc profile):\n"
                        "INFO : Specified rate (%3.1f) is below the 2k/4k limit @ 24fps.\n",
                        parameters->tcp_rates[0]);
            }
        }
        parameters->max_comp_size = COMP_24_CS;
        break;
    case OPJ_CINEMA2K_48:
        max_rate = ((float) (image->numcomps * image->comps[0].w * image->comps[0].h * image->comps[0].prec))/
                (float)(CINEMA_48_CS * 8 * image->comps[0].dx * image->comps[0].dy);
        if (parameters->tcp_rates[0] == 0){
            parameters->tcp_rates[0] = max_rate;
        }else{
            temp_rate =((float) (image->numcomps * image->comps[0].w * image->comps[0].h * image->comps[0].prec))/
                    (parameters->tcp_rates[0] * 8 * (float)image->comps[0].dx * (float)image->comps[0].dy);
            if (temp_rate > CINEMA_48_CS ){
                opj_event_msg(p_manager, EVT_WARNING,
                        "JPEG 2000 Profile-3 (2k dc profile) requires:\n"
                        "Maximum 651041 compressed bytes @ 48fps\n"
                        "-> Specified rate (%3.1f) exceeds this limit. Rate will be forced to %3.1f.\n",
                        parameters->tcp_rates[0], max_rate);
                parameters->tcp_rates[0]= max_rate;
            }else{
                opj_event_msg(p_manager, EVT_WARNING,
                        "JPEG 2000 Profile-3 (2k dc profile):\n"
                        "INFO : Specified rate (%3.1f) is below the 2k limit @ 48 fps.\n",
                        parameters->tcp_rates[0]);
            }
        }
        parameters->max_comp_size = COMP_48_CS;
        break;
    default:
        break;
    }
}

OPJ_BOOL opj_j2k_is_cinema_compliant(opj_image_t *image, OPJ_CINEMA_MODE cinema_mode, opj_event_mgr_t *p_manager)
{
    OPJ_UINT32 i;

    /* Number of components */
    if (image->numcomps != 3){
        opj_event_msg(p_manager, EVT_WARNING,
                "JPEG 2000 Profile-3 (2k dc profile) requires:\n"
                "3 components"
                "-> Number of components of input image (%d) is not compliant\n"
                "-> Non-profile-3 codestream will be generated\n",
                image->numcomps);
        return OPJ_FALSE;
    }

    /* Bitdepth */
    for (i = 0; i < image->numcomps; i++) {
        if ((image->comps[i].bpp != 12) | (image->comps[i].sgnd)){
            char signed_str[] = "signed";
            char unsigned_str[] = "unsigned";
            char *tmp_str = image->comps[i].sgnd?signed_str:unsigned_str;
            opj_event_msg(p_manager, EVT_WARNING,
                    "JPEG 2000 Profile-3 (2k dc profile) requires:\n"
                    "Precision of each component shall be 12 bits unsigned"
                    "-> At least component %d of input image (%d bits, %s) is not compliant\n"
                    "-> Non-profile-3 codestream will be generated\n",
                    i,image->comps[i].bpp, tmp_str);
            return OPJ_FALSE;
        }
    }

    /* Image size */
    switch (cinema_mode){
    case OPJ_CINEMA2K_24:
    case OPJ_CINEMA2K_48:
        if (((image->comps[0].w > 2048) | (image->comps[0].h > 1080))){
            opj_event_msg(p_manager, EVT_WARNING,
                    "JPEG 2000 Profile-3 (2k dc profile) requires:\n"
                    "width <= 2048 and height <= 1080\n"
                    "-> Input image size %d x %d is not compliant\n"
                    "-> Non-profile-3 codestream will be generated\n",
                    image->comps[0].w,image->comps[0].h);
            return OPJ_FALSE;
        }
        break;
    case OPJ_CINEMA4K_24:
        if (((image->comps[0].w > 4096) | (image->comps[0].h > 2160))){
            opj_event_msg(p_manager, EVT_WARNING,
                    "JPEG 2000 Profile-4 (4k dc profile) requires:\n"
                    "width <= 4096 and height <= 2160\n"
                    "-> Image size %d x %d is not compliant\n"
                    "-> Non-profile-4 codestream will be generated\n",
                    image->comps[0].w,image->comps[0].h);
            return OPJ_FALSE;
        }
        break;
    default :
        break;
    }

    return OPJ_TRUE;
}

void opj_j2k_setup_encoder(     opj_j2k_t *p_j2k,
                                                    opj_cparameters_t *parameters,
                                                    opj_image_t *image,
                                                    opj_event_mgr_t * p_manager)
{
        OPJ_UINT32 i, j, tileno, numpocs_tile;
        opj_cp_t *cp = 00;

        if(!p_j2k || !parameters || ! image) {
                return;
        }

        /* keep a link to cp so that we can destroy it later in j2k_destroy_compress */
        cp = &(p_j2k->m_cp);

        /* set default values for cp */
        cp->tw = 1;
        cp->th = 1;

        /* set cinema parameters if required */
        if (parameters->cp_cinema){
            opj_j2k_set_cinema_parameters(parameters,image,p_manager);
            if (!opj_j2k_is_cinema_compliant(image,parameters->cp_cinema,p_manager)) {
                parameters->cp_rsiz = OPJ_STD_RSIZ;
            }
        }

        /*
        copy user encoding parameters
        */
        cp->m_specific_param.m_enc.m_cinema = parameters->cp_cinema;
        cp->m_specific_param.m_enc.m_max_comp_size = (OPJ_UINT32)parameters->max_comp_size;
        cp->rsiz   = parameters->cp_rsiz;
        cp->m_specific_param.m_enc.m_disto_alloc = (OPJ_UINT32)parameters->cp_disto_alloc & 1u;
        cp->m_specific_param.m_enc.m_fixed_alloc = (OPJ_UINT32)parameters->cp_fixed_alloc & 1u;
        cp->m_specific_param.m_enc.m_fixed_quality = (OPJ_UINT32)parameters->cp_fixed_quality & 1u;

        /* mod fixed_quality */
        if (parameters->cp_fixed_alloc && parameters->cp_matrice) {
                size_t array_size = (size_t)parameters->tcp_numlayers * (size_t)parameters->numresolution * 3 * sizeof(OPJ_INT32);
                cp->m_specific_param.m_enc.m_matrice = (OPJ_INT32 *) opj_malloc(array_size);
                memcpy(cp->m_specific_param.m_enc.m_matrice, parameters->cp_matrice, array_size);
        }

        /* tiles */
        cp->tdx = (OPJ_UINT32)parameters->cp_tdx;
        cp->tdy = (OPJ_UINT32)parameters->cp_tdy;

        /* tile offset */
        cp->tx0 = (OPJ_UINT32)parameters->cp_tx0;
        cp->ty0 = (OPJ_UINT32)parameters->cp_ty0;

        /* comment string */
        if(parameters->cp_comment) {
                cp->comment = (char*)opj_malloc(strlen(parameters->cp_comment) + 1);
                if(cp->comment) {
                        strcpy(cp->comment, parameters->cp_comment);
                }
        }

        /*
        calculate other encoding parameters
        */

        if (parameters->tile_size_on) {
                cp->tw = (OPJ_UINT32)opj_int_ceildiv((OPJ_INT32)(image->x1 - cp->tx0), (OPJ_INT32)cp->tdx);
                cp->th = (OPJ_UINT32)opj_int_ceildiv((OPJ_INT32)(image->y1 - cp->ty0), (OPJ_INT32)cp->tdy);
        } else {
                cp->tdx = image->x1 - cp->tx0;
                cp->tdy = image->y1 - cp->ty0;
        }

        if (parameters->tp_on) {
                cp->m_specific_param.m_enc.m_tp_flag = (OPJ_BYTE)parameters->tp_flag;
                cp->m_specific_param.m_enc.m_tp_on = 1;
        }

#ifdef USE_JPWL
        /*
        calculate JPWL encoding parameters
        */

        if (parameters->jpwl_epc_on) {
                OPJ_INT32 i;

                /* set JPWL on */
                cp->epc_on = OPJ_TRUE;
                cp->info_on = OPJ_FALSE; /* no informative technique */

                /* set EPB on */
                if ((parameters->jpwl_hprot_MH > 0) || (parameters->jpwl_hprot_TPH[0] > 0)) {
                        cp->epb_on = OPJ_TRUE;

                        cp->hprot_MH = parameters->jpwl_hprot_MH;
                        for (i = 0; i < JPWL_MAX_NO_TILESPECS; i++) {
                                cp->hprot_TPH_tileno[i] = parameters->jpwl_hprot_TPH_tileno[i];
                                cp->hprot_TPH[i] = parameters->jpwl_hprot_TPH[i];
                        }
                        /* if tile specs are not specified, copy MH specs */
                        if (cp->hprot_TPH[0] == -1) {
                                cp->hprot_TPH_tileno[0] = 0;
                                cp->hprot_TPH[0] = parameters->jpwl_hprot_MH;
                        }
                        for (i = 0; i < JPWL_MAX_NO_PACKSPECS; i++) {
                                cp->pprot_tileno[i] = parameters->jpwl_pprot_tileno[i];
                                cp->pprot_packno[i] = parameters->jpwl_pprot_packno[i];
                                cp->pprot[i] = parameters->jpwl_pprot[i];
                        }
                }

                /* set ESD writing */
                if ((parameters->jpwl_sens_size == 1) || (parameters->jpwl_sens_size == 2)) {
                        cp->esd_on = OPJ_TRUE;

                        cp->sens_size = parameters->jpwl_sens_size;
                        cp->sens_addr = parameters->jpwl_sens_addr;
                        cp->sens_range = parameters->jpwl_sens_range;

                        cp->sens_MH = parameters->jpwl_sens_MH;
                        for (i = 0; i < JPWL_MAX_NO_TILESPECS; i++) {
                                cp->sens_TPH_tileno[i] = parameters->jpwl_sens_TPH_tileno[i];
                                cp->sens_TPH[i] = parameters->jpwl_sens_TPH[i];
                        }
                }

                /* always set RED writing to false: we are at the encoder */
                cp->red_on = OPJ_FALSE;

        } else {
                cp->epc_on = OPJ_FALSE;
        }
#endif /* USE_JPWL */

        /* initialize the mutiple tiles */
        /* ---------------------------- */
        cp->tcps = (opj_tcp_t*) opj_calloc(cp->tw * cp->th, sizeof(opj_tcp_t));
        if (parameters->numpocs) {
                /* initialisation of POC */
                opj_j2k_check_poc_val(parameters->POC,parameters->numpocs, (OPJ_UINT32)parameters->numresolution, image->numcomps, (OPJ_UINT32)parameters->tcp_numlayers, p_manager);
                /* TODO MSD use the return value*/
        }

        for (tileno = 0; tileno < cp->tw * cp->th; tileno++) {
                opj_tcp_t *tcp = &cp->tcps[tileno];
                tcp->numlayers = (OPJ_UINT32)parameters->tcp_numlayers;

                for (j = 0; j < tcp->numlayers; j++) {
                        if(cp->m_specific_param.m_enc.m_cinema){
                                if (cp->m_specific_param.m_enc.m_fixed_quality) {
                                        tcp->distoratio[j] = parameters->tcp_distoratio[j];
                                }
                                tcp->rates[j] = parameters->tcp_rates[j];
                        }else{
                                if (cp->m_specific_param.m_enc.m_fixed_quality) {       /* add fixed_quality */
                                        tcp->distoratio[j] = parameters->tcp_distoratio[j];
                                } else {
                                        tcp->rates[j] = parameters->tcp_rates[j];
                                }
                        }
                }

                tcp->csty = (OPJ_UINT32)parameters->csty;
                tcp->prg = parameters->prog_order;
                tcp->mct = (OPJ_UINT32)parameters->tcp_mct;

                numpocs_tile = 0;
                tcp->POC = 0;

                if (parameters->numpocs) {
                        /* initialisation of POC */
                        tcp->POC = 1;
                        for (i = 0; i < parameters->numpocs; i++) {
                                if (tileno + 1 == parameters->POC[i].tile )  {
                                        opj_poc_t *tcp_poc = &tcp->pocs[numpocs_tile];

                                        tcp_poc->resno0         = parameters->POC[numpocs_tile].resno0;
                                        tcp_poc->compno0        = parameters->POC[numpocs_tile].compno0;
                                        tcp_poc->layno1         = parameters->POC[numpocs_tile].layno1;
                                        tcp_poc->resno1         = parameters->POC[numpocs_tile].resno1;
                                        tcp_poc->compno1        = parameters->POC[numpocs_tile].compno1;
                                        tcp_poc->prg1           = parameters->POC[numpocs_tile].prg1;
                                        tcp_poc->tile           = parameters->POC[numpocs_tile].tile;

                                        numpocs_tile++;
                                }
                        }

                        tcp->numpocs = numpocs_tile -1 ;
                }else{
                        tcp->numpocs = 0;
                }

                tcp->tccps = (opj_tccp_t*) opj_calloc(image->numcomps, sizeof(opj_tccp_t));

                if (parameters->mct_data) {
                      
                    OPJ_UINT32 lMctSize = image->numcomps * image->numcomps * (OPJ_UINT32)sizeof(OPJ_FLOAT32);
                    OPJ_FLOAT32 * lTmpBuf = (OPJ_FLOAT32*)opj_malloc(lMctSize);
                    OPJ_INT32 * l_dc_shift = (OPJ_INT32 *) ((OPJ_BYTE *) parameters->mct_data + lMctSize);

                    tcp->mct = 2;
                    tcp->m_mct_coding_matrix = (OPJ_FLOAT32*)opj_malloc(lMctSize);
                    memcpy(tcp->m_mct_coding_matrix,parameters->mct_data,lMctSize);
                    memcpy(lTmpBuf,parameters->mct_data,lMctSize);

                    tcp->m_mct_decoding_matrix = (OPJ_FLOAT32*)opj_malloc(lMctSize);
                    assert(opj_matrix_inversion_f(lTmpBuf,(tcp->m_mct_decoding_matrix),image->numcomps));

                    tcp->mct_norms = (OPJ_FLOAT64*)
                                    opj_malloc(image->numcomps * sizeof(OPJ_FLOAT64));

                    opj_calculate_norms(tcp->mct_norms,image->numcomps,tcp->m_mct_decoding_matrix);
                    opj_free(lTmpBuf);

                    for (i = 0; i < image->numcomps; i++) {
                            opj_tccp_t *tccp = &tcp->tccps[i];
                            tccp->m_dc_level_shift = l_dc_shift[i];
                    }

                    opj_j2k_setup_mct_encoding(tcp,image);                        
                }
                else {
                        for (i = 0; i < image->numcomps; i++) {
                                opj_tccp_t *tccp = &tcp->tccps[i];
                                opj_image_comp_t * l_comp = &(image->comps[i]);

                                if (! l_comp->sgnd) {
                                        tccp->m_dc_level_shift = 1 << (l_comp->prec - 1);
                                }
                        }
                }

                for (i = 0; i < image->numcomps; i++) {
                        opj_tccp_t *tccp = &tcp->tccps[i];

                        tccp->csty = parameters->csty & 0x01;   /* 0 => one precinct || 1 => custom precinct  */
                        tccp->numresolutions = (OPJ_UINT32)parameters->numresolution;
                        tccp->cblkw = (OPJ_UINT32)opj_int_floorlog2(parameters->cblockw_init);
                        tccp->cblkh = (OPJ_UINT32)opj_int_floorlog2(parameters->cblockh_init);
                        tccp->cblksty = (OPJ_UINT32)parameters->mode;
                        tccp->qmfbid = parameters->irreversible ? 0 : 1;
                        tccp->qntsty = parameters->irreversible ? J2K_CCP_QNTSTY_SEQNT : J2K_CCP_QNTSTY_NOQNT;
                        tccp->numgbits = 2;

                        if ((OPJ_INT32)i == parameters->roi_compno) {
                                tccp->roishift = parameters->roi_shift;
                        } else {
                                tccp->roishift = 0;
                        }

                                if (parameters->csty & J2K_CCP_CSTY_PRT) {
                                        OPJ_INT32 p = 0, it_res;
                                        assert( tccp->numresolutions > 0 );
                                        for (it_res = (OPJ_INT32)tccp->numresolutions - 1; it_res >= 0; it_res--) {
                                                if (p < parameters->res_spec) {

                                                        if (parameters->prcw_init[p] < 1) {
                                                                tccp->prcw[it_res] = 1;
                                                        } else {
                                                                tccp->prcw[it_res] = (OPJ_UINT32)opj_int_floorlog2(parameters->prcw_init[p]);
                                                        }

                                                        if (parameters->prch_init[p] < 1) {
                                                                tccp->prch[it_res] = 1;
                                                        }else {
                                                                tccp->prch[it_res] = (OPJ_UINT32)opj_int_floorlog2(parameters->prch_init[p]);
                                                        }

                                                } else {
                                                        OPJ_INT32 res_spec = parameters->res_spec;
                                                        OPJ_INT32 size_prcw = 0;
                                                        OPJ_INT32 size_prch = 0;

                                                        assert(res_spec>0); /* issue 189 */
                                                        size_prcw = parameters->prcw_init[res_spec - 1] >> (p - (res_spec - 1));
                                                        size_prch = parameters->prch_init[res_spec - 1] >> (p - (res_spec - 1));


                                                        if (size_prcw < 1) {
                                                                tccp->prcw[it_res] = 1;
                                                        } else {
                                                                tccp->prcw[it_res] = (OPJ_UINT32)opj_int_floorlog2(size_prcw);
                                                        }

                                                        if (size_prch < 1) {
                                                                tccp->prch[it_res] = 1;
                                                        } else {
                                                                tccp->prch[it_res] = (OPJ_UINT32)opj_int_floorlog2(size_prch);
                                                        }
                                                }
                                                p++;
                                                /*printf("\nsize precinct for level %d : %d,%d\n", it_res,tccp->prcw[it_res], tccp->prch[it_res]); */
                                        }       /*end for*/
                                } else {
                                        for (j = 0; j < tccp->numresolutions; j++) {
                                                tccp->prcw[j] = 15;
                                                tccp->prch[j] = 15;
                                        }
                                }

                        opj_dwt_calc_explicit_stepsizes(tccp, image->comps[i].prec);
                }
        }

        if (parameters->mct_data) {
                opj_free(parameters->mct_data);
                parameters->mct_data = 00;
        }
}

static OPJ_BOOL opj_j2k_add_mhmarker(opj_codestream_index_t *cstr_index, OPJ_UINT32 type, OPJ_OFF_T pos, OPJ_UINT32 len)
{
        assert(cstr_index != 00);

        /* expand the list? */
        if ((cstr_index->marknum + 1) > cstr_index->maxmarknum) {
                opj_marker_info_t *new_marker;
                cstr_index->maxmarknum = (OPJ_UINT32)(100 + (OPJ_FLOAT32) cstr_index->maxmarknum);
                new_marker = (opj_marker_info_t *) opj_realloc(cstr_index->marker, cstr_index->maxmarknum *sizeof(opj_marker_info_t));
                if (! new_marker) {
                        opj_free(cstr_index->marker);
                        cstr_index->marker = NULL;
                        cstr_index->maxmarknum = 0;
                        cstr_index->marknum = 0;
                        /* opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to add mh marker\n"); */
                        return OPJ_FALSE;
                }
                cstr_index->marker = new_marker;
        }

        /* add the marker */
        cstr_index->marker[cstr_index->marknum].type = (OPJ_UINT16)type;
        cstr_index->marker[cstr_index->marknum].pos = (OPJ_INT32)pos;
        cstr_index->marker[cstr_index->marknum].len = (OPJ_INT32)len;
        cstr_index->marknum++;
        return OPJ_TRUE;
}

static OPJ_BOOL opj_j2k_add_tlmarker(OPJ_UINT32 tileno, opj_codestream_index_t *cstr_index, OPJ_UINT32 type, OPJ_OFF_T pos, OPJ_UINT32 len)
{
        assert(cstr_index != 00);
        assert(cstr_index->tile_index != 00);

        /* expand the list? */
        if ((cstr_index->tile_index[tileno].marknum + 1) > cstr_index->tile_index[tileno].maxmarknum) {
                opj_marker_info_t *new_marker;
                cstr_index->tile_index[tileno].maxmarknum = (OPJ_UINT32)(100 + (OPJ_FLOAT32) cstr_index->tile_index[tileno].maxmarknum);
                new_marker = (opj_marker_info_t *) opj_realloc(
                                cstr_index->tile_index[tileno].marker,
                                cstr_index->tile_index[tileno].maxmarknum *sizeof(opj_marker_info_t));
                if (! new_marker) {
                        opj_free(cstr_index->tile_index[tileno].marker);
                        cstr_index->tile_index[tileno].marker = NULL;
                        cstr_index->tile_index[tileno].maxmarknum = 0;
                        cstr_index->tile_index[tileno].marknum = 0;
                        /* opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to add tl marker\n"); */
                        return OPJ_FALSE;
                }
                cstr_index->tile_index[tileno].marker = new_marker;
        }

        /* add the marker */
        cstr_index->tile_index[tileno].marker[cstr_index->tile_index[tileno].marknum].type = (OPJ_UINT16)type;
        cstr_index->tile_index[tileno].marker[cstr_index->tile_index[tileno].marknum].pos = (OPJ_INT32)pos;
        cstr_index->tile_index[tileno].marker[cstr_index->tile_index[tileno].marknum].len = (OPJ_INT32)len;
        cstr_index->tile_index[tileno].marknum++;

        if (type == J2K_MS_SOT) {
                OPJ_UINT32 l_current_tile_part = cstr_index->tile_index[tileno].current_tpsno;

                if (cstr_index->tile_index[tileno].tp_index)
                        cstr_index->tile_index[tileno].tp_index[l_current_tile_part].start_pos = pos;

        }
        return OPJ_TRUE;
}

/*
 * -----------------------------------------------------------------------
 * -----------------------------------------------------------------------
 * -----------------------------------------------------------------------
 */

OPJ_BOOL opj_j2k_end_decompress(opj_j2k_t *p_j2k,
                                opj_stream_private_t *p_stream,
                                opj_event_mgr_t * p_manager
                                )
{
    (void)p_j2k;
    (void)p_stream;
    (void)p_manager;
    return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_read_header(   opj_stream_private_t *p_stream,
                                                            opj_j2k_t* p_j2k,
                                                            opj_image_t** p_image,
                                                            opj_event_mgr_t* p_manager )
{
        /* preconditions */
        assert(p_j2k != 00);
        assert(p_stream != 00);
        assert(p_manager != 00);

        /* create an empty image header */
        p_j2k->m_private_image = opj_image_create0();
        if (! p_j2k->m_private_image) {
                return OPJ_FALSE;
        }

        /* customization of the validation */
        opj_j2k_setup_decoding_validation(p_j2k);

        /* validation of the parameters codec */
        if (! opj_j2k_exec(p_j2k, p_j2k->m_validation_list, p_stream,p_manager)) {
                opj_image_destroy(p_j2k->m_private_image);
                p_j2k->m_private_image = NULL;
                return OPJ_FALSE;
        }

        /* customization of the encoding */
        opj_j2k_setup_header_reading(p_j2k);

        /* read header */
        if (! opj_j2k_exec (p_j2k,p_j2k->m_procedure_list,p_stream,p_manager)) {
                opj_image_destroy(p_j2k->m_private_image);
                p_j2k->m_private_image = NULL;
                return OPJ_FALSE;
        }

        *p_image = opj_image_create0();
        if (! (*p_image)) {
                return OPJ_FALSE;
        }

        /* Copy codestream image information to the output image */
        opj_copy_image_header(p_j2k->m_private_image, *p_image);

    /*Allocate and initialize some elements of codestrem index*/
        if (!opj_j2k_allocate_tile_element_cstr_index(p_j2k)){
                return OPJ_FALSE;
        }

        return OPJ_TRUE;
}

void opj_j2k_setup_header_reading (opj_j2k_t *p_j2k)
{
        /* preconditions*/
        assert(p_j2k != 00);

        opj_procedure_list_add_procedure(p_j2k->m_procedure_list,(opj_procedure)opj_j2k_read_header_procedure);

        /* DEVELOPER CORNER, add your custom procedures */
        opj_procedure_list_add_procedure(p_j2k->m_procedure_list,(opj_procedure)opj_j2k_copy_default_tcp_and_create_tcd);

}

void opj_j2k_setup_decoding_validation (opj_j2k_t *p_j2k)
{
        /* preconditions*/
        assert(p_j2k != 00);

        opj_procedure_list_add_procedure(p_j2k->m_validation_list, (opj_procedure)opj_j2k_build_decoder);
        opj_procedure_list_add_procedure(p_j2k->m_validation_list, (opj_procedure)opj_j2k_decoding_validation);
        /* DEVELOPER CORNER, add your custom validation procedure */

}

OPJ_BOOL opj_j2k_mct_validation (       opj_j2k_t * p_j2k,
                                                                opj_stream_private_t *p_stream,
                                                                opj_event_mgr_t * p_manager )
{
        OPJ_BOOL l_is_valid = OPJ_TRUE;
        OPJ_UINT32 i,j;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_stream != 00);
        assert(p_manager != 00);

        if ((p_j2k->m_cp.rsiz & 0x8200) == 0x8200) {
                OPJ_UINT32 l_nb_tiles = p_j2k->m_cp.th * p_j2k->m_cp.tw;
                opj_tcp_t * l_tcp = p_j2k->m_cp.tcps;

                for (i=0;i<l_nb_tiles;++i) {
                        if (l_tcp->mct == 2) {
                                opj_tccp_t * l_tccp = l_tcp->tccps;
                                l_is_valid &= (l_tcp->m_mct_coding_matrix != 00);

                                for (j=0;j<p_j2k->m_private_image->numcomps;++j) {
                                        l_is_valid &= ! (l_tccp->qmfbid & 1);
                                        ++l_tccp;
                                }
                        }
                        ++l_tcp;
                }
        }

        return l_is_valid;
}

OPJ_BOOL opj_j2k_setup_mct_encoding(opj_tcp_t * p_tcp, opj_image_t * p_image)
{
        OPJ_UINT32 i;
        OPJ_UINT32 l_indix = 1;
        opj_mct_data_t * l_mct_deco_data = 00,* l_mct_offset_data = 00;
        opj_simple_mcc_decorrelation_data_t * l_mcc_data;
        OPJ_UINT32 l_mct_size,l_nb_elem;
        OPJ_FLOAT32 * l_data, * l_current_data;
        opj_tccp_t * l_tccp;

        /* preconditions */
        assert(p_tcp != 00);

        if (p_tcp->mct != 2) {
                return OPJ_TRUE;
        }

        if (p_tcp->m_mct_decoding_matrix) {
                if (p_tcp->m_nb_mct_records == p_tcp->m_nb_max_mct_records) {
                        opj_mct_data_t *new_mct_records;
                        p_tcp->m_nb_max_mct_records += OPJ_J2K_MCT_DEFAULT_NB_RECORDS;

                        new_mct_records = (opj_mct_data_t *) opj_realloc(p_tcp->m_mct_records, p_tcp->m_nb_max_mct_records * sizeof(opj_mct_data_t));
                        if (! new_mct_records) {
                                opj_free(p_tcp->m_mct_records);
                                p_tcp->m_mct_records = NULL;
                                p_tcp->m_nb_max_mct_records = 0;
                                p_tcp->m_nb_mct_records = 0;
                                /* opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to setup mct encoding\n"); */
                                return OPJ_FALSE;
                        }
                        p_tcp->m_mct_records = new_mct_records;
                        l_mct_deco_data = p_tcp->m_mct_records + p_tcp->m_nb_mct_records;

                        memset(l_mct_deco_data ,0,(p_tcp->m_nb_max_mct_records - p_tcp->m_nb_mct_records) * sizeof(opj_mct_data_t));
                }
                l_mct_deco_data = p_tcp->m_mct_records + p_tcp->m_nb_mct_records;

                if (l_mct_deco_data->m_data) {
                        opj_free(l_mct_deco_data->m_data);
                        l_mct_deco_data->m_data = 00;
                }

                l_mct_deco_data->m_index = l_indix++;
                l_mct_deco_data->m_array_type = MCT_TYPE_DECORRELATION;
                l_mct_deco_data->m_element_type = MCT_TYPE_FLOAT;
                l_nb_elem = p_image->numcomps * p_image->numcomps;
                l_mct_size = l_nb_elem * MCT_ELEMENT_SIZE[l_mct_deco_data->m_element_type];
                l_mct_deco_data->m_data = (OPJ_BYTE*)opj_malloc(l_mct_size );

                if (! l_mct_deco_data->m_data) {
                        return OPJ_FALSE;
                }

                j2k_mct_write_functions_from_float[l_mct_deco_data->m_element_type](p_tcp->m_mct_decoding_matrix,l_mct_deco_data->m_data,l_nb_elem);

                l_mct_deco_data->m_data_size = l_mct_size;
                ++p_tcp->m_nb_mct_records;
        }

        if (p_tcp->m_nb_mct_records == p_tcp->m_nb_max_mct_records) {
                opj_mct_data_t *new_mct_records;
                p_tcp->m_nb_max_mct_records += OPJ_J2K_MCT_DEFAULT_NB_RECORDS;
                new_mct_records = (opj_mct_data_t *) opj_realloc(p_tcp->m_mct_records, p_tcp->m_nb_max_mct_records * sizeof(opj_mct_data_t));
                if (! new_mct_records) {
                        opj_free(p_tcp->m_mct_records);
                        p_tcp->m_mct_records = NULL;
                        p_tcp->m_nb_max_mct_records = 0;
                        p_tcp->m_nb_mct_records = 0;
                        /* opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to setup mct encoding\n"); */
                        return OPJ_FALSE;
                }
                p_tcp->m_mct_records = new_mct_records;
                l_mct_offset_data = p_tcp->m_mct_records + p_tcp->m_nb_mct_records;

                memset(l_mct_offset_data ,0,(p_tcp->m_nb_max_mct_records - p_tcp->m_nb_mct_records) * sizeof(opj_mct_data_t));

                if (l_mct_deco_data) {
                        l_mct_deco_data = l_mct_offset_data - 1;
                }
        }

        l_mct_offset_data = p_tcp->m_mct_records + p_tcp->m_nb_mct_records;

        if (l_mct_offset_data->m_data) {
                opj_free(l_mct_offset_data->m_data);
                l_mct_offset_data->m_data = 00;
        }

        l_mct_offset_data->m_index = l_indix++;
        l_mct_offset_data->m_array_type = MCT_TYPE_OFFSET;
        l_mct_offset_data->m_element_type = MCT_TYPE_FLOAT;
        l_nb_elem = p_image->numcomps;
        l_mct_size = l_nb_elem * MCT_ELEMENT_SIZE[l_mct_offset_data->m_element_type];
        l_mct_offset_data->m_data = (OPJ_BYTE*)opj_malloc(l_mct_size );

        if (! l_mct_offset_data->m_data) {
                return OPJ_FALSE;
        }

        l_data = (OPJ_FLOAT32*)opj_malloc(l_nb_elem * sizeof(OPJ_FLOAT32));
        if (! l_data) {
                opj_free(l_mct_offset_data->m_data);
                l_mct_offset_data->m_data = 00;
                return OPJ_FALSE;
        }

        l_tccp = p_tcp->tccps;
        l_current_data = l_data;

        for (i=0;i<l_nb_elem;++i) {
                *(l_current_data++) = (OPJ_FLOAT32) (l_tccp->m_dc_level_shift);
                ++l_tccp;
        }

        j2k_mct_write_functions_from_float[l_mct_offset_data->m_element_type](l_data,l_mct_offset_data->m_data,l_nb_elem);

        opj_free(l_data);

        l_mct_offset_data->m_data_size = l_mct_size;

        ++p_tcp->m_nb_mct_records;

        if (p_tcp->m_nb_mcc_records == p_tcp->m_nb_max_mcc_records) {
                opj_simple_mcc_decorrelation_data_t *new_mcc_records;
                p_tcp->m_nb_max_mcc_records += OPJ_J2K_MCT_DEFAULT_NB_RECORDS;
                new_mcc_records = (opj_simple_mcc_decorrelation_data_t *) opj_realloc(
                                p_tcp->m_mcc_records, p_tcp->m_nb_max_mcc_records * sizeof(opj_simple_mcc_decorrelation_data_t));
                if (! new_mcc_records) {
                        opj_free(p_tcp->m_mcc_records);
                        p_tcp->m_mcc_records = NULL;
                        p_tcp->m_nb_max_mcc_records = 0;
                        p_tcp->m_nb_mcc_records = 0;
                        /* opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to setup mct encoding\n"); */
                        return OPJ_FALSE;
                }
                p_tcp->m_mcc_records = new_mcc_records;
                l_mcc_data = p_tcp->m_mcc_records + p_tcp->m_nb_mcc_records;
                memset(l_mcc_data ,0,(p_tcp->m_nb_max_mcc_records - p_tcp->m_nb_mcc_records) * sizeof(opj_simple_mcc_decorrelation_data_t));

        }

        l_mcc_data = p_tcp->m_mcc_records + p_tcp->m_nb_mcc_records;
        l_mcc_data->m_decorrelation_array = l_mct_deco_data;
        l_mcc_data->m_is_irreversible = 1;
        l_mcc_data->m_nb_comps = p_image->numcomps;
        l_mcc_data->m_index = l_indix++;
        l_mcc_data->m_offset_array = l_mct_offset_data;
        ++p_tcp->m_nb_mcc_records;

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_build_decoder (opj_j2k_t * p_j2k,
                                                            opj_stream_private_t *p_stream,
                                                            opj_event_mgr_t * p_manager )
{
        /* add here initialization of cp
           copy paste of setup_decoder */
  (void)p_j2k;
  (void)p_stream;
  (void)p_manager;
        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_build_encoder (opj_j2k_t * p_j2k,
                                                        opj_stream_private_t *p_stream,
                                                        opj_event_mgr_t * p_manager )
{
        /* add here initialization of cp
           copy paste of setup_encoder */
  (void)p_j2k;
  (void)p_stream;
  (void)p_manager;
        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_encoding_validation (  opj_j2k_t * p_j2k,
                                                                            opj_stream_private_t *p_stream,
                                                                            opj_event_mgr_t * p_manager )
{
        OPJ_BOOL l_is_valid = OPJ_TRUE;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_stream != 00);
        assert(p_manager != 00);

        /* STATE checking */
        /* make sure the state is at 0 */
        l_is_valid &= (p_j2k->m_specific_param.m_decoder.m_state == J2K_STATE_NONE);

        /* POINTER validation */
        /* make sure a p_j2k codec is present */
        l_is_valid &= (p_j2k->m_procedure_list != 00);
        /* make sure a validation list is present */
        l_is_valid &= (p_j2k->m_validation_list != 00);

        if ((p_j2k->m_cp.tdx) < (OPJ_UINT32) (1 << p_j2k->m_cp.tcps->tccps->numresolutions)) {
                opj_event_msg(p_manager, EVT_ERROR, "Number of resolutions is too high in comparison to the size of tiles\n");
                return OPJ_FALSE;
        }

        if ((p_j2k->m_cp.tdy) < (OPJ_UINT32) (1 << p_j2k->m_cp.tcps->tccps->numresolutions)) {
                opj_event_msg(p_manager, EVT_ERROR, "Number of resolutions is too high in comparison to the size of tiles\n");
                return OPJ_FALSE;
        }

        /* PARAMETER VALIDATION */
        return l_is_valid;
}

OPJ_BOOL opj_j2k_decoding_validation (  opj_j2k_t *p_j2k,
                                        opj_stream_private_t *p_stream,
                                        opj_event_mgr_t * p_manager
                                        )
{
        OPJ_BOOL l_is_valid = OPJ_TRUE;

        /* preconditions*/
        assert(p_j2k != 00);
        assert(p_stream != 00);
        assert(p_manager != 00);

        /* STATE checking */
        /* make sure the state is at 0 */
#ifdef TODO_MSD
        l_is_valid &= (p_j2k->m_specific_param.m_decoder.m_state == J2K_DEC_STATE_NONE);
#endif
        l_is_valid &= (p_j2k->m_specific_param.m_decoder.m_state == 0x0000);

        /* POINTER validation */
        /* make sure a p_j2k codec is present */
        /* make sure a procedure list is present */
        l_is_valid &= (p_j2k->m_procedure_list != 00);
        /* make sure a validation list is present */
        l_is_valid &= (p_j2k->m_validation_list != 00);

        /* PARAMETER VALIDATION */
        return l_is_valid;
}

OPJ_BOOL opj_j2k_read_header_procedure( opj_j2k_t *p_j2k,
                                                                            opj_stream_private_t *p_stream,
                                                                            opj_event_mgr_t * p_manager)
{
        OPJ_UINT32 l_current_marker;
        OPJ_UINT32 l_marker_size;
        const opj_dec_memory_marker_handler_t * l_marker_handler = 00;

        /* preconditions */
        assert(p_stream != 00);
        assert(p_j2k != 00);
        assert(p_manager != 00);

        /*  We enter in the main header */
        p_j2k->m_specific_param.m_decoder.m_state = J2K_STATE_MHSOC;

        /* Try to read the SOC marker, the codestream must begin with SOC marker */
        if (! opj_j2k_read_soc(p_j2k,p_stream,p_manager)) {
                opj_event_msg(p_manager, EVT_ERROR, "Expected a SOC marker \n");
                return OPJ_FALSE;
        }

        /* Try to read 2 bytes (the next marker ID) from stream and copy them into the buffer */
        if (opj_stream_read_data(p_stream,p_j2k->m_specific_param.m_decoder.m_header_data,2,p_manager) != 2) {
                opj_event_msg(p_manager, EVT_ERROR, "Stream too short\n");
                return OPJ_FALSE;
        }

        /* Read 2 bytes as the new marker ID */
        opj_read_bytes(p_j2k->m_specific_param.m_decoder.m_header_data,&l_current_marker,2);

        /* Try to read until the SOT is detected */
        while (l_current_marker != J2K_MS_SOT) {

                /* Check if the current marker ID is valid */
                if (l_current_marker < 0xff00) {
                        opj_event_msg(p_manager, EVT_ERROR, "We expected read a marker ID (0xff--) instead of %.8x\n", l_current_marker);
                        return OPJ_FALSE;
                }

                /* Get the marker handler from the marker ID */
                l_marker_handler = opj_j2k_get_marker_handler(l_current_marker);

                /* Manage case where marker is unknown */
                if (l_marker_handler->id == J2K_MS_UNK) {
                        if (! opj_j2k_read_unk(p_j2k, p_stream, &l_current_marker, p_manager)){
                                opj_event_msg(p_manager, EVT_ERROR, "Unknow marker have been detected and generated error.\n");
                                return OPJ_FALSE;
                        }

                        if (l_current_marker == J2K_MS_SOT)
                                break; /* SOT marker is detected main header is completely read */
                        else    /* Get the marker handler from the marker ID */
                                l_marker_handler = opj_j2k_get_marker_handler(l_current_marker);
                }

                /* Check if the marker is known and if it is the right place to find it */
                if (! (p_j2k->m_specific_param.m_decoder.m_state & l_marker_handler->states) ) {
                        opj_event_msg(p_manager, EVT_ERROR, "Marker is not compliant with its position\n");
                        return OPJ_FALSE;
                }

                /* Try to read 2 bytes (the marker size) from stream and copy them into the buffer */
                if (opj_stream_read_data(p_stream,p_j2k->m_specific_param.m_decoder.m_header_data,2,p_manager) != 2) {
                        opj_event_msg(p_manager, EVT_ERROR, "Stream too short\n");
                        return OPJ_FALSE;
                }

                /* read 2 bytes as the marker size */
                opj_read_bytes(p_j2k->m_specific_param.m_decoder.m_header_data,&l_marker_size,2);
                l_marker_size -= 2; /* Subtract the size of the marker ID already read */

                /* Check if the marker size is compatible with the header data size */
                if (l_marker_size > p_j2k->m_specific_param.m_decoder.m_header_data_size) {
                        OPJ_BYTE *new_header_data = (OPJ_BYTE *) opj_realloc(p_j2k->m_specific_param.m_decoder.m_header_data, l_marker_size);
                        if (! new_header_data) {
                                opj_free(p_j2k->m_specific_param.m_decoder.m_header_data);
                                p_j2k->m_specific_param.m_decoder.m_header_data = NULL;
                                p_j2k->m_specific_param.m_decoder.m_header_data_size = 0;
                                opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to read header\n");
                                return OPJ_FALSE;
                        }
                        p_j2k->m_specific_param.m_decoder.m_header_data = new_header_data;
                        p_j2k->m_specific_param.m_decoder.m_header_data_size = l_marker_size;
                }

                /* Try to read the rest of the marker segment from stream and copy them into the buffer */
                if (opj_stream_read_data(p_stream,p_j2k->m_specific_param.m_decoder.m_header_data,l_marker_size,p_manager) != l_marker_size) {
                        opj_event_msg(p_manager, EVT_ERROR, "Stream too short\n");
                        return OPJ_FALSE;
                }

                /* Read the marker segment with the correct marker handler */
                if (! (*(l_marker_handler->handler))(p_j2k,p_j2k->m_specific_param.m_decoder.m_header_data,l_marker_size,p_manager)) {
                        opj_event_msg(p_manager, EVT_ERROR, "Marker handler function failed to read the marker segment\n");
                        return OPJ_FALSE;
                }

                /* Add the marker to the codestream index*/
                if (OPJ_FALSE == opj_j2k_add_mhmarker(
                                        p_j2k->cstr_index,
                                        l_marker_handler->id,
                                        (OPJ_UINT32) opj_stream_tell(p_stream) - l_marker_size - 4,
                                        l_marker_size + 4 )) {
                        opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to add mh marker\n");
                        return OPJ_FALSE;
                }

                /* Try to read 2 bytes (the next marker ID) from stream and copy them into the buffer */
                if (opj_stream_read_data(p_stream,p_j2k->m_specific_param.m_decoder.m_header_data,2,p_manager) != 2) {
                        opj_event_msg(p_manager, EVT_ERROR, "Stream too short\n");
                        return OPJ_FALSE;
                }

                /* read 2 bytes as the new marker ID */
                opj_read_bytes(p_j2k->m_specific_param.m_decoder.m_header_data,&l_current_marker,2);
        }

        opj_event_msg(p_manager, EVT_INFO, "Main header has been correctly decoded.\n");

        /* Position of the last element if the main header */
        p_j2k->cstr_index->main_head_end = (OPJ_UINT32) opj_stream_tell(p_stream) - 2;

        /* Next step: read a tile-part header */
        p_j2k->m_specific_param.m_decoder.m_state = J2K_STATE_TPHSOT;

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_exec ( opj_j2k_t * p_j2k,
                                        opj_procedure_list_t * p_procedure_list,
                                        opj_stream_private_t *p_stream,
                                        opj_event_mgr_t * p_manager )
{
        OPJ_BOOL (** l_procedure) (opj_j2k_t * ,opj_stream_private_t *,opj_event_mgr_t *) = 00;
        OPJ_BOOL l_result = OPJ_TRUE;
        OPJ_UINT32 l_nb_proc, i;

        /* preconditions*/
        assert(p_procedure_list != 00);
        assert(p_j2k != 00);
        assert(p_stream != 00);
        assert(p_manager != 00);

        l_nb_proc = opj_procedure_list_get_nb_procedures(p_procedure_list);
        l_procedure = (OPJ_BOOL (**) (opj_j2k_t * ,opj_stream_private_t *,opj_event_mgr_t *)) opj_procedure_list_get_first_procedure(p_procedure_list);

        for     (i=0;i<l_nb_proc;++i) {
                l_result = l_result && ((*l_procedure) (p_j2k,p_stream,p_manager));
                ++l_procedure;
        }

        /* and clear the procedure list at the end.*/
        opj_procedure_list_clear(p_procedure_list);
        return l_result;
}

/* FIXME DOC*/
static OPJ_BOOL opj_j2k_copy_default_tcp_and_create_tcd (       opj_j2k_t * p_j2k,
                                                            opj_stream_private_t *p_stream,
                                                            opj_event_mgr_t * p_manager
                                                            )
{
        opj_tcp_t * l_tcp = 00;
        opj_tcp_t * l_default_tcp = 00;
        OPJ_UINT32 l_nb_tiles;
        OPJ_UINT32 i,j;
        opj_tccp_t *l_current_tccp = 00;
        OPJ_UINT32 l_tccp_size;
        OPJ_UINT32 l_mct_size;
        opj_image_t * l_image;
        OPJ_UINT32 l_mcc_records_size,l_mct_records_size;
        opj_mct_data_t * l_src_mct_rec, *l_dest_mct_rec;
        opj_simple_mcc_decorrelation_data_t * l_src_mcc_rec, *l_dest_mcc_rec;
        OPJ_UINT32 l_offset;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_stream != 00);
        assert(p_manager != 00);

        l_image = p_j2k->m_private_image;
        l_nb_tiles = p_j2k->m_cp.th * p_j2k->m_cp.tw;
        l_tcp = p_j2k->m_cp.tcps;
        l_tccp_size = l_image->numcomps * (OPJ_UINT32)sizeof(opj_tccp_t);
        l_default_tcp = p_j2k->m_specific_param.m_decoder.m_default_tcp;
        l_mct_size = l_image->numcomps * l_image->numcomps * (OPJ_UINT32)sizeof(OPJ_FLOAT32);

        /* For each tile */
        for (i=0; i<l_nb_tiles; ++i) {
                /* keep the tile-compo coding parameters pointer of the current tile coding parameters*/
                l_current_tccp = l_tcp->tccps;
                /*Copy default coding parameters into the current tile coding parameters*/
                memcpy(l_tcp, l_default_tcp, sizeof(opj_tcp_t));
                /* Initialize some values of the current tile coding parameters*/
                l_tcp->ppt = 0;
                l_tcp->ppt_data = 00;
                /* Reconnect the tile-compo coding parameters pointer to the current tile coding parameters*/
                l_tcp->tccps = l_current_tccp;

                /* Get the mct_decoding_matrix of the dflt_tile_cp and copy them into the current tile cp*/
                if (l_default_tcp->m_mct_decoding_matrix) {
                        l_tcp->m_mct_decoding_matrix = (OPJ_FLOAT32*)opj_malloc(l_mct_size);
                        if (! l_tcp->m_mct_decoding_matrix ) {
                                return OPJ_FALSE;
                        }
                        memcpy(l_tcp->m_mct_decoding_matrix,l_default_tcp->m_mct_decoding_matrix,l_mct_size);
                }

                /* Get the mct_record of the dflt_tile_cp and copy them into the current tile cp*/
                l_mct_records_size = l_default_tcp->m_nb_max_mct_records * (OPJ_UINT32)sizeof(opj_mct_data_t);
                l_tcp->m_mct_records = (opj_mct_data_t*)opj_malloc(l_mct_records_size);
                if (! l_tcp->m_mct_records) {
                        return OPJ_FALSE;
                }
                memcpy(l_tcp->m_mct_records, l_default_tcp->m_mct_records,l_mct_records_size);

                /* Copy the mct record data from dflt_tile_cp to the current tile*/
                l_src_mct_rec = l_default_tcp->m_mct_records;
                l_dest_mct_rec = l_tcp->m_mct_records;

                for (j=0;j<l_default_tcp->m_nb_mct_records;++j) {

                        if (l_src_mct_rec->m_data) {

                                l_dest_mct_rec->m_data = (OPJ_BYTE*) opj_malloc(l_src_mct_rec->m_data_size);
                                if(! l_dest_mct_rec->m_data) {
                                        return OPJ_FALSE;
                                }
                                memcpy(l_dest_mct_rec->m_data,l_src_mct_rec->m_data,l_src_mct_rec->m_data_size);
                        }

                        ++l_src_mct_rec;
                        ++l_dest_mct_rec;
                }

                /* Get the mcc_record of the dflt_tile_cp and copy them into the current tile cp*/
                l_mcc_records_size = l_default_tcp->m_nb_max_mcc_records * (OPJ_UINT32)sizeof(opj_simple_mcc_decorrelation_data_t);
                l_tcp->m_mcc_records = (opj_simple_mcc_decorrelation_data_t*) opj_malloc(l_mcc_records_size);
                if (! l_tcp->m_mcc_records) {
                        return OPJ_FALSE;
                }
                memcpy(l_tcp->m_mcc_records,l_default_tcp->m_mcc_records,l_mcc_records_size);

                /* Copy the mcc record data from dflt_tile_cp to the current tile*/
                l_src_mcc_rec = l_default_tcp->m_mcc_records;
                l_dest_mcc_rec = l_tcp->m_mcc_records;

                for (j=0;j<l_default_tcp->m_nb_max_mcc_records;++j) {

                        if (l_src_mcc_rec->m_decorrelation_array) {
                                l_offset = (OPJ_UINT32)(l_src_mcc_rec->m_decorrelation_array - l_default_tcp->m_mct_records);
                                l_dest_mcc_rec->m_decorrelation_array = l_tcp->m_mct_records + l_offset;
                        }

                        if (l_src_mcc_rec->m_offset_array) {
                                l_offset = (OPJ_UINT32)(l_src_mcc_rec->m_offset_array - l_default_tcp->m_mct_records);
                                l_dest_mcc_rec->m_offset_array = l_tcp->m_mct_records + l_offset;
                        }

                        ++l_src_mcc_rec;
                        ++l_dest_mcc_rec;
                }

                /* Copy all the dflt_tile_compo_cp to the current tile cp */
                memcpy(l_current_tccp,l_default_tcp->tccps,l_tccp_size);

                /* Move to next tile cp*/
                ++l_tcp;
        }

        /* Create the current tile decoder*/
        p_j2k->m_tcd = (opj_tcd_t*)opj_tcd_create(OPJ_TRUE); /* FIXME why a cast ? */
        if (! p_j2k->m_tcd ) {
                return OPJ_FALSE;
        }

        if ( !opj_tcd_init(p_j2k->m_tcd, l_image, &(p_j2k->m_cp)) ) {
                opj_tcd_destroy(p_j2k->m_tcd);
                p_j2k->m_tcd = 00;
                opj_event_msg(p_manager, EVT_ERROR, "Cannot decode tile, memory error\n");
                return OPJ_FALSE;
        }

        return OPJ_TRUE;
}

const opj_dec_memory_marker_handler_t * opj_j2k_get_marker_handler (OPJ_UINT32 p_id)
{
        const opj_dec_memory_marker_handler_t *e;
        for (e = j2k_memory_marker_handler_tab; e->id != 0; ++e) {
                if (e->id == p_id) {
                        break; /* we find a handler corresponding to the marker ID*/
                }
        }
        return e;
}

void opj_j2k_destroy (opj_j2k_t *p_j2k)
{
        if (p_j2k == 00) {
                return;
        }

        if (p_j2k->m_is_decoder) {

                if (p_j2k->m_specific_param.m_decoder.m_default_tcp != 00) {
                        opj_j2k_tcp_destroy(p_j2k->m_specific_param.m_decoder.m_default_tcp);
                        opj_free(p_j2k->m_specific_param.m_decoder.m_default_tcp);
                        p_j2k->m_specific_param.m_decoder.m_default_tcp = 00;
                }

                if (p_j2k->m_specific_param.m_decoder.m_header_data != 00) {
                        opj_free(p_j2k->m_specific_param.m_decoder.m_header_data);
                        p_j2k->m_specific_param.m_decoder.m_header_data = 00;
                        p_j2k->m_specific_param.m_decoder.m_header_data_size = 0;
                }
        }
        else {

                if (p_j2k->m_specific_param.m_encoder.m_encoded_tile_data) {
                        opj_free(p_j2k->m_specific_param.m_encoder.m_encoded_tile_data);
                        p_j2k->m_specific_param.m_encoder.m_encoded_tile_data = 00;
                }

                if (p_j2k->m_specific_param.m_encoder.m_tlm_sot_offsets_buffer) {
                        opj_free(p_j2k->m_specific_param.m_encoder.m_tlm_sot_offsets_buffer);
                        p_j2k->m_specific_param.m_encoder.m_tlm_sot_offsets_buffer = 00;
                        p_j2k->m_specific_param.m_encoder.m_tlm_sot_offsets_current = 00;
                }

                if (p_j2k->m_specific_param.m_encoder.m_header_tile_data) {
                        opj_free(p_j2k->m_specific_param.m_encoder.m_header_tile_data);
                        p_j2k->m_specific_param.m_encoder.m_header_tile_data = 00;
                        p_j2k->m_specific_param.m_encoder.m_header_tile_data_size = 0;
                }
        }

        opj_tcd_destroy(p_j2k->m_tcd);

        opj_j2k_cp_destroy(&(p_j2k->m_cp));
        memset(&(p_j2k->m_cp),0,sizeof(opj_cp_t));

        opj_procedure_list_destroy(p_j2k->m_procedure_list);
        p_j2k->m_procedure_list = 00;

        opj_procedure_list_destroy(p_j2k->m_validation_list);
        p_j2k->m_procedure_list = 00;

        j2k_destroy_cstr_index(p_j2k->cstr_index);
        p_j2k->cstr_index = NULL;

        opj_image_destroy(p_j2k->m_private_image);
        p_j2k->m_private_image = NULL;

        opj_image_destroy(p_j2k->m_output_image);
        p_j2k->m_output_image = NULL;

        opj_free(p_j2k);
}

void j2k_destroy_cstr_index (opj_codestream_index_t *p_cstr_ind)
{
        if (p_cstr_ind) {

                if (p_cstr_ind->marker) {
                        opj_free(p_cstr_ind->marker);
                        p_cstr_ind->marker = NULL;
                }

                if (p_cstr_ind->tile_index) {
                        OPJ_UINT32 it_tile = 0;

                        for (it_tile=0; it_tile < p_cstr_ind->nb_of_tiles; it_tile++) {

                                if(p_cstr_ind->tile_index[it_tile].packet_index) {
                                        opj_free(p_cstr_ind->tile_index[it_tile].packet_index);
                                        p_cstr_ind->tile_index[it_tile].packet_index = NULL;
                                }

                                if(p_cstr_ind->tile_index[it_tile].tp_index){
                                        opj_free(p_cstr_ind->tile_index[it_tile].tp_index);
                                        p_cstr_ind->tile_index[it_tile].tp_index = NULL;
                                }

                                if(p_cstr_ind->tile_index[it_tile].marker){
                                        opj_free(p_cstr_ind->tile_index[it_tile].marker);
                                        p_cstr_ind->tile_index[it_tile].marker = NULL;

                                }
                        }

                        opj_free( p_cstr_ind->tile_index);
                        p_cstr_ind->tile_index = NULL;
                }

                opj_free(p_cstr_ind);
        }
}

void opj_j2k_tcp_destroy (opj_tcp_t *p_tcp)
{
        if (p_tcp == 00) {
                return;
        }

        if (p_tcp->ppt_buffer != 00) {
                opj_free(p_tcp->ppt_buffer);
                p_tcp->ppt_buffer = 00;
        }

        if (p_tcp->tccps != 00) {
                opj_free(p_tcp->tccps);
                p_tcp->tccps = 00;
        }

        if (p_tcp->m_mct_coding_matrix != 00) {
                opj_free(p_tcp->m_mct_coding_matrix);
                p_tcp->m_mct_coding_matrix = 00;
        }

        if (p_tcp->m_mct_decoding_matrix != 00) {
                opj_free(p_tcp->m_mct_decoding_matrix);
                p_tcp->m_mct_decoding_matrix = 00;
        }

        if (p_tcp->m_mcc_records) {
                opj_free(p_tcp->m_mcc_records);
                p_tcp->m_mcc_records = 00;
                p_tcp->m_nb_max_mcc_records = 0;
                p_tcp->m_nb_mcc_records = 0;
        }

        if (p_tcp->m_mct_records) {
                opj_mct_data_t * l_mct_data = p_tcp->m_mct_records;
                OPJ_UINT32 i;

                for (i=0;i<p_tcp->m_nb_mct_records;++i) {
                        if (l_mct_data->m_data) {
                                opj_free(l_mct_data->m_data);
                                l_mct_data->m_data = 00;
                        }

                        ++l_mct_data;
                }

                opj_free(p_tcp->m_mct_records);
                p_tcp->m_mct_records = 00;
        }

        if (p_tcp->mct_norms != 00) {
                opj_free(p_tcp->mct_norms);
                p_tcp->mct_norms = 00;
        }

        opj_j2k_tcp_data_destroy(p_tcp);

}

void opj_j2k_tcp_data_destroy (opj_tcp_t *p_tcp)
{
        if (p_tcp->m_data) {
                opj_free(p_tcp->m_data);
                p_tcp->m_data = NULL;
                p_tcp->m_data_size = 0;
        }
}

void opj_j2k_cp_destroy (opj_cp_t *p_cp)
{
        OPJ_UINT32 l_nb_tiles;
        opj_tcp_t * l_current_tile = 00;
        OPJ_UINT32 i;

        if (p_cp == 00)
        {
                return;
        }
        if (p_cp->tcps != 00)
        {
                l_current_tile = p_cp->tcps;
                l_nb_tiles = p_cp->th * p_cp->tw;

                for (i = 0; i < l_nb_tiles; ++i)
                {
                        opj_j2k_tcp_destroy(l_current_tile);
                        ++l_current_tile;
                }
                opj_free(p_cp->tcps);
                p_cp->tcps = 00;
        }
        opj_free(p_cp->ppm_buffer);
        p_cp->ppm_buffer = 00;
        p_cp->ppm_data = NULL; /* ppm_data belongs to the allocated buffer pointed by ppm_buffer */
        opj_free(p_cp->comment);
        p_cp->comment = 00;
        if (! p_cp->m_is_decoder)
        {
                opj_free(p_cp->m_specific_param.m_enc.m_matrice);
                p_cp->m_specific_param.m_enc.m_matrice = 00;
        }
}

OPJ_BOOL opj_j2k_read_tile_header(      opj_j2k_t * p_j2k,
                                                                    OPJ_UINT32 * p_tile_index,
                                                                    OPJ_UINT32 * p_data_size,
                                                                    OPJ_INT32 * p_tile_x0, OPJ_INT32 * p_tile_y0,
                                                                    OPJ_INT32 * p_tile_x1, OPJ_INT32 * p_tile_y1,
                                                                    OPJ_UINT32 * p_nb_comps,
                                                                    OPJ_BOOL * p_go_on,
                                                                    opj_stream_private_t *p_stream,
                                                                    opj_event_mgr_t * p_manager )
{
        OPJ_UINT32 l_current_marker = J2K_MS_SOT;
        OPJ_UINT32 l_marker_size;
        const opj_dec_memory_marker_handler_t * l_marker_handler = 00;
        opj_tcp_t * l_tcp = NULL;
        OPJ_UINT32 l_nb_tiles;

        /* preconditions */
        assert(p_stream != 00);
        assert(p_j2k != 00);
        assert(p_manager != 00);

        /* Reach the End Of Codestream ?*/
        if (p_j2k->m_specific_param.m_decoder.m_state == J2K_STATE_EOC){
                l_current_marker = J2K_MS_EOC;
        }
        /* We need to encounter a SOT marker (a new tile-part header) */
        else if (p_j2k->m_specific_param.m_decoder.m_state != J2K_STATE_TPHSOT){
                return OPJ_FALSE;
        }

        /* Read into the codestream until reach the EOC or ! can_decode ??? FIXME */
        while ( (!p_j2k->m_specific_param.m_decoder.m_can_decode) && (l_current_marker != J2K_MS_EOC) ) {

                /* Try to read until the Start Of Data is detected */
                while (l_current_marker != J2K_MS_SOD) {
                    
                    if(opj_stream_get_number_byte_left(p_stream) == 0)
                    {
                        p_j2k->m_specific_param.m_decoder.m_state = J2K_STATE_NEOC;
                        break;
                    }

                        /* Try to read 2 bytes (the marker size) from stream and copy them into the buffer */
                        if (opj_stream_read_data(p_stream,p_j2k->m_specific_param.m_decoder.m_header_data,2,p_manager) != 2) {
                                opj_event_msg(p_manager, EVT_ERROR, "Stream too short\n");
                                return OPJ_FALSE;
                        }

                        /* Read 2 bytes from the buffer as the marker size */
                        opj_read_bytes(p_j2k->m_specific_param.m_decoder.m_header_data,&l_marker_size,2);

                        /* cf. https://code.google.com/p/openjpeg/issues/detail?id=226 */
                        if (l_current_marker == 0x8080 && opj_stream_get_number_byte_left(p_stream) == 0) {
                                p_j2k->m_specific_param.m_decoder.m_state = J2K_STATE_NEOC;
                                break;
                        }

                        /* Why this condition? FIXME */
                        if (p_j2k->m_specific_param.m_decoder.m_state & J2K_STATE_TPH){
                                p_j2k->m_specific_param.m_decoder.m_sot_length -= (l_marker_size + 2);
                        }
                        l_marker_size -= 2; /* Subtract the size of the marker ID already read */

                        /* Get the marker handler from the marker ID */
                        l_marker_handler = opj_j2k_get_marker_handler(l_current_marker);

                        /* Check if the marker is known and if it is the right place to find it */
                        if (! (p_j2k->m_specific_param.m_decoder.m_state & l_marker_handler->states) ) {
                                opj_event_msg(p_manager, EVT_ERROR, "Marker is not compliant with its position\n");
                                return OPJ_FALSE;
                        }
/* FIXME manage case of unknown marker as in the main header ? */

                        /* Check if the marker size is compatible with the header data size */
                        if (l_marker_size > p_j2k->m_specific_param.m_decoder.m_header_data_size) {
                                OPJ_BYTE *new_header_data = (OPJ_BYTE *) opj_realloc(p_j2k->m_specific_param.m_decoder.m_header_data, l_marker_size);
                                if (! new_header_data) {
                                        opj_free(p_j2k->m_specific_param.m_decoder.m_header_data);
                                        p_j2k->m_specific_param.m_decoder.m_header_data = NULL;
                                        p_j2k->m_specific_param.m_decoder.m_header_data_size = 0;
                                        opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to read header\n");
                                        return OPJ_FALSE;
                                }
                                p_j2k->m_specific_param.m_decoder.m_header_data = new_header_data;
                                p_j2k->m_specific_param.m_decoder.m_header_data_size = l_marker_size;
                        }

                        /* Try to read the rest of the marker segment from stream and copy them into the buffer */
                        if (opj_stream_read_data(p_stream,p_j2k->m_specific_param.m_decoder.m_header_data,l_marker_size,p_manager) != l_marker_size) {
                                opj_event_msg(p_manager, EVT_ERROR, "Stream too short\n");
                                return OPJ_FALSE;
                        }

                        if (!l_marker_handler->handler) {
                                /* See issue #175 */
                                opj_event_msg(p_manager, EVT_ERROR, "Not sure how that happened.\n");
                                return OPJ_FALSE;
                        }
                        /* Read the marker segment with the correct marker handler */
                        if (! (*(l_marker_handler->handler))(p_j2k,p_j2k->m_specific_param.m_decoder.m_header_data,l_marker_size,p_manager)) {
                                opj_event_msg(p_manager, EVT_ERROR, "Fail to read the current marker segment (%#x)\n", l_current_marker);
                                return OPJ_FALSE;
                        }

                        /* Add the marker to the codestream index*/
                        if (OPJ_FALSE == opj_j2k_add_tlmarker(p_j2k->m_current_tile_number,
                                                p_j2k->cstr_index,
                                                l_marker_handler->id,
                                                (OPJ_UINT32) opj_stream_tell(p_stream) - l_marker_size - 4,
                                                l_marker_size + 4 )) {
                                opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to add tl marker\n");
                                return OPJ_FALSE;
                        }

                        /* Keep the position of the last SOT marker read */
                        if ( l_marker_handler->id == J2K_MS_SOT ) {
                                OPJ_UINT32 sot_pos = (OPJ_UINT32) opj_stream_tell(p_stream) - l_marker_size - 4 ;
                                if (sot_pos > p_j2k->m_specific_param.m_decoder.m_last_sot_read_pos)
                                {
                                        p_j2k->m_specific_param.m_decoder.m_last_sot_read_pos = sot_pos;
                                }
                        }

                        if (p_j2k->m_specific_param.m_decoder.m_skip_data) {
                                /* Skip the rest of the tile part header*/
                                if (opj_stream_skip(p_stream,p_j2k->m_specific_param.m_decoder.m_sot_length,p_manager) != p_j2k->m_specific_param.m_decoder.m_sot_length) {
                                        opj_event_msg(p_manager, EVT_ERROR, "Stream too short\n");
                                        return OPJ_FALSE;
                                }
                                l_current_marker = J2K_MS_SOD; /* Normally we reached a SOD */
                        }
                        else {
                                /* Try to read 2 bytes (the next marker ID) from stream and copy them into the buffer*/
                                if (opj_stream_read_data(p_stream,p_j2k->m_specific_param.m_decoder.m_header_data,2,p_manager) != 2) {
                                        opj_event_msg(p_manager, EVT_ERROR, "Stream too short\n");
                                        return OPJ_FALSE;
                                }
                                /* Read 2 bytes from the buffer as the new marker ID */
                                opj_read_bytes(p_j2k->m_specific_param.m_decoder.m_header_data,&l_current_marker,2);
                        }
                }
                if(opj_stream_get_number_byte_left(p_stream) == 0
                    && p_j2k->m_specific_param.m_decoder.m_state == J2K_STATE_NEOC)
                    break;

                /* If we didn't skip data before, we need to read the SOD marker*/
                if (! p_j2k->m_specific_param.m_decoder.m_skip_data) {
                        /* Try to read the SOD marker and skip data ? FIXME */
                        if (! opj_j2k_read_sod(p_j2k, p_stream, p_manager)) {
                                return OPJ_FALSE;
                        }

                        if (! p_j2k->m_specific_param.m_decoder.m_can_decode){
                                /* Try to read 2 bytes (the next marker ID) from stream and copy them into the buffer */
                                if (opj_stream_read_data(p_stream,p_j2k->m_specific_param.m_decoder.m_header_data,2,p_manager) != 2) {
                                        opj_event_msg(p_manager, EVT_ERROR, "Stream too short\n");
                                        return OPJ_FALSE;
                                }

                                /* Read 2 bytes from buffer as the new marker ID */
                                opj_read_bytes(p_j2k->m_specific_param.m_decoder.m_header_data,&l_current_marker,2);
                        }
                }
                else {
                        /* Indicate we will try to read a new tile-part header*/
                        p_j2k->m_specific_param.m_decoder.m_skip_data = 0;
                        p_j2k->m_specific_param.m_decoder.m_can_decode = 0;
                        p_j2k->m_specific_param.m_decoder.m_state = J2K_STATE_TPHSOT;

                        /* Try to read 2 bytes (the next marker ID) from stream and copy them into the buffer */
                        if (opj_stream_read_data(p_stream,p_j2k->m_specific_param.m_decoder.m_header_data,2,p_manager) != 2) {
                                opj_event_msg(p_manager, EVT_ERROR, "Stream too short\n");
                                return OPJ_FALSE;
                        }

                        /* Read 2 bytes from buffer as the new marker ID */
                        opj_read_bytes(p_j2k->m_specific_param.m_decoder.m_header_data,&l_current_marker,2);
                }
        }

        /* Current marker is the EOC marker ?*/
        if (l_current_marker == J2K_MS_EOC) {
                if (p_j2k->m_specific_param.m_decoder.m_state != J2K_STATE_EOC ){
                        p_j2k->m_current_tile_number = 0;
                        p_j2k->m_specific_param.m_decoder.m_state = J2K_STATE_EOC;
                }
        }

        /* FIXME DOC ???*/
        if ( ! p_j2k->m_specific_param.m_decoder.m_can_decode) {
                l_tcp = p_j2k->m_cp.tcps + p_j2k->m_current_tile_number;
                l_nb_tiles = p_j2k->m_cp.th * p_j2k->m_cp.tw;

                while( (p_j2k->m_current_tile_number < l_nb_tiles) && (l_tcp->m_data == 00) ) {
                        ++p_j2k->m_current_tile_number;
                        ++l_tcp;
                }

                if (p_j2k->m_current_tile_number == l_nb_tiles) {
                        *p_go_on = OPJ_FALSE;
                        return OPJ_TRUE;
                }
        }

        /*FIXME ???*/
        if (! opj_tcd_init_decode_tile(p_j2k->m_tcd, p_j2k->m_current_tile_number)) {
                opj_event_msg(p_manager, EVT_ERROR, "Cannot decode tile, memory error\n");
                return OPJ_FALSE;
        }

        opj_event_msg(p_manager, EVT_INFO, "Header of tile %d / %d has been read.\n",
                        p_j2k->m_current_tile_number, (p_j2k->m_cp.th * p_j2k->m_cp.tw) - 1);

        *p_tile_index = p_j2k->m_current_tile_number;
        *p_go_on = OPJ_TRUE;
        *p_data_size = opj_tcd_get_decoded_tile_size(p_j2k->m_tcd);
        *p_tile_x0 = p_j2k->m_tcd->tcd_image->tiles->x0;
        *p_tile_y0 = p_j2k->m_tcd->tcd_image->tiles->y0;
        *p_tile_x1 = p_j2k->m_tcd->tcd_image->tiles->x1;
        *p_tile_y1 = p_j2k->m_tcd->tcd_image->tiles->y1;
        *p_nb_comps = p_j2k->m_tcd->tcd_image->tiles->numcomps;

         p_j2k->m_specific_param.m_decoder.m_state |= 0x0080;/* FIXME J2K_DEC_STATE_DATA;*/

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_decode_tile (  opj_j2k_t * p_j2k,
                                                        OPJ_UINT32 p_tile_index,
                                                        OPJ_BYTE * p_data,
                                                        OPJ_UINT32 p_data_size,
                                                        opj_stream_private_t *p_stream,
                                                        opj_event_mgr_t * p_manager )
{
        OPJ_UINT32 l_current_marker;
        OPJ_BYTE l_data [2];
        opj_tcp_t * l_tcp;

        /* preconditions */
        assert(p_stream != 00);
        assert(p_j2k != 00);
        assert(p_manager != 00);

        if ( !(p_j2k->m_specific_param.m_decoder.m_state & 0x0080/*FIXME J2K_DEC_STATE_DATA*/)
                || (p_tile_index != p_j2k->m_current_tile_number) ) {
                return OPJ_FALSE;
        }

        l_tcp = &(p_j2k->m_cp.tcps[p_tile_index]);
        if (! l_tcp->m_data) {
                opj_j2k_tcp_destroy(l_tcp);
                return OPJ_FALSE;
        }

        if (! opj_tcd_decode_tile(      p_j2k->m_tcd,
                                                                l_tcp->m_data,
                                                                l_tcp->m_data_size,
                                                                p_tile_index,
                                                                p_j2k->cstr_index) ) {
                opj_j2k_tcp_destroy(l_tcp);
                p_j2k->m_specific_param.m_decoder.m_state |= 0x8000;/*FIXME J2K_DEC_STATE_ERR;*/
                opj_event_msg(p_manager, EVT_ERROR, "Failed to decode.\n");
                return OPJ_FALSE;
        }

        if (! opj_tcd_update_tile_data(p_j2k->m_tcd,p_data,p_data_size)) {
                return OPJ_FALSE;
        }

        /* To avoid to destroy the tcp which can be useful when we try to decode a tile decoded before (cf j2k_random_tile_access)
         * we destroy just the data which will be re-read in read_tile_header*/
        /*opj_j2k_tcp_destroy(l_tcp);
        p_j2k->m_tcd->tcp = 0;*/
        opj_j2k_tcp_data_destroy(l_tcp);

        p_j2k->m_specific_param.m_decoder.m_can_decode = 0;
        p_j2k->m_specific_param.m_decoder.m_state &= (~ (0x0080u));/* FIXME J2K_DEC_STATE_DATA);*/

        if(opj_stream_get_number_byte_left(p_stream) == 0 
            && p_j2k->m_specific_param.m_decoder.m_state == J2K_STATE_NEOC){
            return OPJ_TRUE;
        }

        if (p_j2k->m_specific_param.m_decoder.m_state != 0x0100){ /*FIXME J2K_DEC_STATE_EOC)*/
                if (opj_stream_read_data(p_stream,l_data,2,p_manager) != 2) {
                        opj_event_msg(p_manager, EVT_ERROR, "Stream too short\n");
                        return OPJ_FALSE;
                }

                opj_read_bytes(l_data,&l_current_marker,2);

                if (l_current_marker == J2K_MS_EOC) {
                        p_j2k->m_current_tile_number = 0;
                        p_j2k->m_specific_param.m_decoder.m_state =  0x0100;/*FIXME J2K_DEC_STATE_EOC;*/
                }
                else if (l_current_marker != J2K_MS_SOT)
                {
                        opj_event_msg(p_manager, EVT_ERROR, "Stream too short, expected SOT\n");
                        
                        if(opj_stream_get_number_byte_left(p_stream) == 0) {
                            p_j2k->m_specific_param.m_decoder.m_state = J2K_STATE_NEOC;
                            return OPJ_TRUE;
                        }
                        return OPJ_FALSE;
                }
        }

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_update_image_data (opj_tcd_t * p_tcd, OPJ_BYTE * p_data, opj_image_t* p_output_image)
{
        OPJ_UINT32 i,j,k = 0;
        OPJ_UINT32 l_width_src,l_height_src;
        OPJ_UINT32 l_width_dest,l_height_dest;
        OPJ_INT32 l_offset_x0_src, l_offset_y0_src, l_offset_x1_src, l_offset_y1_src;
        OPJ_INT32 l_start_offset_src, l_line_offset_src, l_end_offset_src ;
        OPJ_UINT32 l_start_x_dest , l_start_y_dest;
        OPJ_UINT32 l_x0_dest, l_y0_dest, l_x1_dest, l_y1_dest;
        OPJ_INT32 l_start_offset_dest, l_line_offset_dest;

        opj_image_comp_t * l_img_comp_src = 00;
        opj_image_comp_t * l_img_comp_dest = 00;

        opj_tcd_tilecomp_t * l_tilec = 00;
        opj_image_t * l_image_src = 00;
        OPJ_UINT32 l_size_comp, l_remaining;
        OPJ_INT32 * l_dest_ptr;
        opj_tcd_resolution_t* l_res= 00;

        l_tilec = p_tcd->tcd_image->tiles->comps;
        l_image_src = p_tcd->image;
        l_img_comp_src = l_image_src->comps;

        l_img_comp_dest = p_output_image->comps;

        for (i=0; i<l_image_src->numcomps; i++) {

                /* Allocate output component buffer if necessary */
                if (!l_img_comp_dest->data) {

                        l_img_comp_dest->data = (OPJ_INT32*) opj_calloc(l_img_comp_dest->w * l_img_comp_dest->h, sizeof(OPJ_INT32));
                        if (! l_img_comp_dest->data) {
                                return OPJ_FALSE;
                        }
                }

                /* Copy info from decoded comp image to output image */
                l_img_comp_dest->resno_decoded = l_img_comp_src->resno_decoded;

                /*-----*/
                /* Compute the precision of the output buffer */
                l_size_comp = l_img_comp_src->prec >> 3; /*(/ 8)*/
                l_remaining = l_img_comp_src->prec & 7;  /* (%8) */
                l_res = l_tilec->resolutions + l_img_comp_src->resno_decoded;

                if (l_remaining) {
                        ++l_size_comp;
                }

                if (l_size_comp == 3) {
                        l_size_comp = 4;
                }
                /*-----*/

                /* Current tile component size*/
                /*if (i == 0) {
                fprintf(stdout, "SRC: l_res_x0=%d, l_res_x1=%d, l_res_y0=%d, l_res_y1=%d\n",
                                l_res->x0, l_res->x1, l_res->y0, l_res->y1);
                }*/

                l_width_src = (OPJ_UINT32)(l_res->x1 - l_res->x0);
                l_height_src = (OPJ_UINT32)(l_res->y1 - l_res->y0);

                /* Border of the current output component*/
                l_x0_dest = (OPJ_UINT32)opj_int_ceildivpow2((OPJ_INT32)l_img_comp_dest->x0, (OPJ_INT32)l_img_comp_dest->factor);
                l_y0_dest = (OPJ_UINT32)opj_int_ceildivpow2((OPJ_INT32)l_img_comp_dest->y0, (OPJ_INT32)l_img_comp_dest->factor);
                l_x1_dest = l_x0_dest + l_img_comp_dest->w;
                l_y1_dest = l_y0_dest + l_img_comp_dest->h;

                /*if (i == 0) {
                fprintf(stdout, "DEST: l_x0_dest=%d, l_x1_dest=%d, l_y0_dest=%d, l_y1_dest=%d (%d)\n",
                                l_x0_dest, l_x1_dest, l_y0_dest, l_y1_dest, l_img_comp_dest->factor );
                }*/

                /*-----*/
                /* Compute the area (l_offset_x0_src, l_offset_y0_src, l_offset_x1_src, l_offset_y1_src)
                 * of the input buffer (decoded tile component) which will be move
                 * in the output buffer. Compute the area of the output buffer (l_start_x_dest,
                 * l_start_y_dest, l_width_dest, l_height_dest)  which will be modified
                 * by this input area.
                 * */
                assert( l_res->x0 >= 0);
                assert( l_res->x1 >= 0);
                if ( l_x0_dest < (OPJ_UINT32)l_res->x0 ) {
                        l_start_x_dest = (OPJ_UINT32)l_res->x0 - l_x0_dest;
                        l_offset_x0_src = 0;

                        if ( l_x1_dest >= (OPJ_UINT32)l_res->x1 ) {
                                l_width_dest = l_width_src;
                                l_offset_x1_src = 0;
                        }
                        else {
                                l_width_dest = l_x1_dest - (OPJ_UINT32)l_res->x0 ;
                                l_offset_x1_src = (OPJ_INT32)(l_width_src - l_width_dest);
                        }
                }
                else {
                        l_start_x_dest = 0 ;
                        l_offset_x0_src = (OPJ_INT32)l_x0_dest - l_res->x0;

                        if ( l_x1_dest >= (OPJ_UINT32)l_res->x1 ) {
                                l_width_dest = l_width_src - (OPJ_UINT32)l_offset_x0_src;
                                l_offset_x1_src = 0;
                        }
                        else {
                                l_width_dest = l_img_comp_dest->w ;
                                l_offset_x1_src = l_res->x1 - (OPJ_INT32)l_x1_dest;
                        }
                }

                if ( l_y0_dest < (OPJ_UINT32)l_res->y0 ) {
                        l_start_y_dest = (OPJ_UINT32)l_res->y0 - l_y0_dest;
                        l_offset_y0_src = 0;

                        if ( l_y1_dest >= (OPJ_UINT32)l_res->y1 ) {
                                l_height_dest = l_height_src;
                                l_offset_y1_src = 0;
                        }
                        else {
                                l_height_dest = l_y1_dest - (OPJ_UINT32)l_res->y0 ;
                                l_offset_y1_src =  (OPJ_INT32)(l_height_src - l_height_dest);
                        }
                }
                else {
                        l_start_y_dest = 0 ;
                        l_offset_y0_src = (OPJ_INT32)l_y0_dest - l_res->y0;

                        if ( l_y1_dest >= (OPJ_UINT32)l_res->y1 ) {
                                l_height_dest = l_height_src - (OPJ_UINT32)l_offset_y0_src;
                                l_offset_y1_src = 0;
                        }
                        else {
                                l_height_dest = l_img_comp_dest->h ;
                                l_offset_y1_src = l_res->y1 - (OPJ_INT32)l_y1_dest;
                        }
                }

                if( (l_offset_x0_src < 0 ) || (l_offset_y0_src < 0 ) || (l_offset_x1_src < 0 ) || (l_offset_y1_src < 0 ) ){
                        return OPJ_FALSE;
                }
                /* testcase 2977.pdf.asan.67.2198 */
                if ((OPJ_INT32)l_width_dest < 0 || (OPJ_INT32)l_height_dest < 0) {
                        return OPJ_FALSE;
                }
                /*-----*/

                /* Compute the input buffer offset */
                l_start_offset_src = l_offset_x0_src + l_offset_y0_src * (OPJ_INT32)l_width_src;
                l_line_offset_src = l_offset_x1_src + l_offset_x0_src;
                l_end_offset_src = l_offset_y1_src * (OPJ_INT32)l_width_src - l_offset_x0_src;

                /* Compute the output buffer offset */
                l_start_offset_dest = (OPJ_INT32)(l_start_x_dest + l_start_y_dest * l_img_comp_dest->w);
                l_line_offset_dest = (OPJ_INT32)(l_img_comp_dest->w - l_width_dest);

                /* Move the output buffer to the first place where we will write*/
                l_dest_ptr = l_img_comp_dest->data + l_start_offset_dest;

                /*if (i == 0) {
                        fprintf(stdout, "COMPO[%d]:\n",i);
                        fprintf(stdout, "SRC: l_start_x_src=%d, l_start_y_src=%d, l_width_src=%d, l_height_src=%d\n"
                                        "\t tile offset:%d, %d, %d, %d\n"
                                        "\t buffer offset: %d; %d, %d\n",
                                        l_res->x0, l_res->y0, l_width_src, l_height_src,
                                        l_offset_x0_src, l_offset_y0_src, l_offset_x1_src, l_offset_y1_src,
                                        l_start_offset_src, l_line_offset_src, l_end_offset_src);

                        fprintf(stdout, "DEST: l_start_x_dest=%d, l_start_y_dest=%d, l_width_dest=%d, l_height_dest=%d\n"
                                        "\t start offset: %d, line offset= %d\n",
                                        l_start_x_dest, l_start_y_dest, l_width_dest, l_height_dest, l_start_offset_dest, l_line_offset_dest);
                }*/

                switch (l_size_comp) {
                        case 1:
                                {
                                        OPJ_CHAR * l_src_ptr = (OPJ_CHAR*) p_data;
                                        l_src_ptr += l_start_offset_src; /* Move to the first place where we will read*/

                                        if (l_img_comp_src->sgnd) {
                                                for (j = 0 ; j < l_height_dest ; ++j) {
                                                        for ( k = 0 ; k < l_width_dest ; ++k) {
                                                                *(l_dest_ptr++) = (OPJ_INT32) (*(l_src_ptr++)); /* Copy only the data needed for the output image */
                                                        }

                                                        l_dest_ptr+= l_line_offset_dest; /* Move to the next place where we will write */
                                                        l_src_ptr += l_line_offset_src ; /* Move to the next place where we will read */
                                                }
                                        }
                                        else {
                                                for ( j = 0 ; j < l_height_dest ; ++j ) {
                                                        for ( k = 0 ; k < l_width_dest ; ++k) {
                                                                *(l_dest_ptr++) = (OPJ_INT32) ((*(l_src_ptr++))&0xff);
                                                        }

                                                        l_dest_ptr+= l_line_offset_dest;
                                                        l_src_ptr += l_line_offset_src;
                                                }
                                        }

                                        l_src_ptr += l_end_offset_src; /* Move to the end of this component-part of the input buffer */
                                        p_data = (OPJ_BYTE*) l_src_ptr; /* Keep the current position for the next component-part */
                                }
                                break;
                        case 2:
                                {
                                        OPJ_INT16 * l_src_ptr = (OPJ_INT16 *) p_data;
                                        l_src_ptr += l_start_offset_src;

                                        if (l_img_comp_src->sgnd) {
                                                for (j=0;j<l_height_dest;++j) {
                                                        for (k=0;k<l_width_dest;++k) {
                                                                *(l_dest_ptr++) = *(l_src_ptr++);
                                                        }

                                                        l_dest_ptr+= l_line_offset_dest;
                                                        l_src_ptr += l_line_offset_src ;
                                                }
                                        }
                                        else {
                                                for (j=0;j<l_height_dest;++j) {
                                                        for (k=0;k<l_width_dest;++k) {
                                                                *(l_dest_ptr++) = (*(l_src_ptr++))&0xffff;
                                                        }

                                                        l_dest_ptr+= l_line_offset_dest;
                                                        l_src_ptr += l_line_offset_src ;
                                                }
                                        }

                                        l_src_ptr += l_end_offset_src;
                                        p_data = (OPJ_BYTE*) l_src_ptr;
                                }
                                break;
                        case 4:
                                {
                                        OPJ_INT32 * l_src_ptr = (OPJ_INT32 *) p_data;
                                        l_src_ptr += l_start_offset_src;

                                        for (j=0;j<l_height_dest;++j) {
                                                for (k=0;k<l_width_dest;++k) {
                                                        *(l_dest_ptr++) = (*(l_src_ptr++));
                                                }

                                                l_dest_ptr+= l_line_offset_dest;
                                                l_src_ptr += l_line_offset_src ;
                                        }

                                        l_src_ptr += l_end_offset_src;
                                        p_data = (OPJ_BYTE*) l_src_ptr;
                                }
                                break;
                }

                ++l_img_comp_dest;
                ++l_img_comp_src;
                ++l_tilec;
        }

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_set_decode_area(       opj_j2k_t *p_j2k,
                                                                    opj_image_t* p_image,
                                                                    OPJ_INT32 p_start_x, OPJ_INT32 p_start_y,
                                                                    OPJ_INT32 p_end_x, OPJ_INT32 p_end_y,
                                                                    opj_event_mgr_t * p_manager )
{
        opj_cp_t * l_cp = &(p_j2k->m_cp);
        opj_image_t * l_image = p_j2k->m_private_image;

        OPJ_UINT32 it_comp;
        OPJ_INT32 l_comp_x1, l_comp_y1;
        opj_image_comp_t* l_img_comp = NULL;

        /* Check if we are read the main header */
        if (p_j2k->m_specific_param.m_decoder.m_state != J2K_STATE_TPHSOT) { /* FIXME J2K_DEC_STATE_TPHSOT)*/
                opj_event_msg(p_manager, EVT_ERROR, "Need to decode the main header before begin to decode the remaining codestream");
                return OPJ_FALSE;
        }

        if ( !p_start_x && !p_start_y && !p_end_x && !p_end_y){
                opj_event_msg(p_manager, EVT_INFO, "No decoded area parameters, set the decoded area to the whole image\n");

                p_j2k->m_specific_param.m_decoder.m_start_tile_x = 0;
                p_j2k->m_specific_param.m_decoder.m_start_tile_y = 0;
                p_j2k->m_specific_param.m_decoder.m_end_tile_x = l_cp->tw;
                p_j2k->m_specific_param.m_decoder.m_end_tile_y = l_cp->th;

                return OPJ_TRUE;
        }

        /* ----- */
        /* Check if the positions provided by the user are correct */

        /* Left */
        assert(p_start_x >= 0 );
        assert(p_start_y >= 0 );

        if ((OPJ_UINT32)p_start_x > l_image->x1 ) {
                opj_event_msg(p_manager, EVT_ERROR,
                        "Left position of the decoded area (region_x0=%d) is outside the image area (Xsiz=%d).\n",
                        p_start_x, l_image->x1);
                return OPJ_FALSE;
        }
        else if ((OPJ_UINT32)p_start_x < l_image->x0){
                opj_event_msg(p_manager, EVT_WARNING,
                                "Left position of the decoded area (region_x0=%d) is outside the image area (XOsiz=%d).\n",
                                p_start_x, l_image->x0);
                p_j2k->m_specific_param.m_decoder.m_start_tile_x = 0;
                p_image->x0 = l_image->x0;
        }
        else {
                p_j2k->m_specific_param.m_decoder.m_start_tile_x = ((OPJ_UINT32)p_start_x - l_cp->tx0) / l_cp->tdx;
                p_image->x0 = (OPJ_UINT32)p_start_x;
        }

        /* Up */
        if ((OPJ_UINT32)p_start_y > l_image->y1){
                opj_event_msg(p_manager, EVT_ERROR,
                                "Up position of the decoded area (region_y0=%d) is outside the image area (Ysiz=%d).\n",
                                p_start_y, l_image->y1);
                return OPJ_FALSE;
        }
        else if ((OPJ_UINT32)p_start_y < l_image->y0){
                opj_event_msg(p_manager, EVT_WARNING,
                                "Up position of the decoded area (region_y0=%d) is outside the image area (YOsiz=%d).\n",
                                p_start_y, l_image->y0);
                p_j2k->m_specific_param.m_decoder.m_start_tile_y = 0;
                p_image->y0 = l_image->y0;
        }
        else {
                p_j2k->m_specific_param.m_decoder.m_start_tile_y = ((OPJ_UINT32)p_start_y - l_cp->ty0) / l_cp->tdy;
                p_image->y0 = (OPJ_UINT32)p_start_y;
        }

        /* Right */
        assert((OPJ_UINT32)p_end_x > 0);
        assert((OPJ_UINT32)p_end_y > 0);
        if ((OPJ_UINT32)p_end_x < l_image->x0) {
                opj_event_msg(p_manager, EVT_ERROR,
                        "Right position of the decoded area (region_x1=%d) is outside the image area (XOsiz=%d).\n",
                        p_end_x, l_image->x0);
                return OPJ_FALSE;
        }
        else if ((OPJ_UINT32)p_end_x > l_image->x1) {
                opj_event_msg(p_manager, EVT_WARNING,
                        "Right position of the decoded area (region_x1=%d) is outside the image area (Xsiz=%d).\n",
                        p_end_x, l_image->x1);
                p_j2k->m_specific_param.m_decoder.m_end_tile_x = l_cp->tw;
                p_image->x1 = l_image->x1;
        }
        else {
                p_j2k->m_specific_param.m_decoder.m_end_tile_x = (OPJ_UINT32)opj_int_ceildiv(p_end_x - (OPJ_INT32)l_cp->tx0, (OPJ_INT32)l_cp->tdx);
                p_image->x1 = (OPJ_UINT32)p_end_x;
        }

        /* Bottom */
        if ((OPJ_UINT32)p_end_y < l_image->y0) {
                opj_event_msg(p_manager, EVT_ERROR,
                        "Bottom position of the decoded area (region_y1=%d) is outside the image area (YOsiz=%d).\n",
                        p_end_y, l_image->y0);
                return OPJ_FALSE;
        }
        if ((OPJ_UINT32)p_end_y > l_image->y1){
                opj_event_msg(p_manager, EVT_WARNING,
                        "Bottom position of the decoded area (region_y1=%d) is outside the image area (Ysiz=%d).\n",
                        p_end_y, l_image->y1);
                p_j2k->m_specific_param.m_decoder.m_end_tile_y = l_cp->th;
                p_image->y1 = l_image->y1;
        }
        else{
                p_j2k->m_specific_param.m_decoder.m_end_tile_y = (OPJ_UINT32)opj_int_ceildiv(p_end_y - (OPJ_INT32)l_cp->ty0, (OPJ_INT32)l_cp->tdy);
                p_image->y1 = (OPJ_UINT32)p_end_y;
        }
        /* ----- */

        p_j2k->m_specific_param.m_decoder.m_discard_tiles = 1;

        l_img_comp = p_image->comps;
        for (it_comp=0; it_comp < p_image->numcomps; ++it_comp)
        {
                OPJ_INT32 l_h,l_w;

                l_img_comp->x0 = (OPJ_UINT32)opj_int_ceildiv((OPJ_INT32)p_image->x0, (OPJ_INT32)l_img_comp->dx);
                l_img_comp->y0 = (OPJ_UINT32)opj_int_ceildiv((OPJ_INT32)p_image->y0, (OPJ_INT32)l_img_comp->dy);
                l_comp_x1 = opj_int_ceildiv((OPJ_INT32)p_image->x1, (OPJ_INT32)l_img_comp->dx);
                l_comp_y1 = opj_int_ceildiv((OPJ_INT32)p_image->y1, (OPJ_INT32)l_img_comp->dy);

                l_w = opj_int_ceildivpow2(l_comp_x1, (OPJ_INT32)l_img_comp->factor)
                                - opj_int_ceildivpow2((OPJ_INT32)l_img_comp->x0, (OPJ_INT32)l_img_comp->factor);
                if (l_w < 0){
                        opj_event_msg(p_manager, EVT_ERROR,
                                "Size x of the decoded component image is incorrect (comp[%d].w=%d).\n",
                                it_comp, l_w);
                        return OPJ_FALSE;
                }
                l_img_comp->w = (OPJ_UINT32)l_w;

                l_h = opj_int_ceildivpow2(l_comp_y1, (OPJ_INT32)l_img_comp->factor)
                                - opj_int_ceildivpow2((OPJ_INT32)l_img_comp->y0, (OPJ_INT32)l_img_comp->factor);
                if (l_h < 0){
                        opj_event_msg(p_manager, EVT_ERROR,
                                "Size y of the decoded component image is incorrect (comp[%d].h=%d).\n",
                                it_comp, l_h);
                        return OPJ_FALSE;
                }
                l_img_comp->h = (OPJ_UINT32)l_h;

                l_img_comp++;
        }

        opj_event_msg( p_manager, EVT_INFO,"Setting decoding area to %d,%d,%d,%d\n",
                        p_image->x0, p_image->y0, p_image->x1, p_image->y1);

        return OPJ_TRUE;
}

opj_j2k_t* opj_j2k_create_decompress(void)
{
        opj_j2k_t *l_j2k = (opj_j2k_t*) opj_malloc(sizeof(opj_j2k_t));
        if (!l_j2k) {
                return 00;
        }
        memset(l_j2k,0,sizeof(opj_j2k_t));

        l_j2k->m_is_decoder = 1;
        l_j2k->m_cp.m_is_decoder = 1;

        l_j2k->m_specific_param.m_decoder.m_default_tcp = (opj_tcp_t*) opj_malloc(sizeof(opj_tcp_t));
        if (!l_j2k->m_specific_param.m_decoder.m_default_tcp) {
                opj_j2k_destroy(l_j2k);
                return 00;
        }
        memset(l_j2k->m_specific_param.m_decoder.m_default_tcp,0,sizeof(opj_tcp_t));

        l_j2k->m_specific_param.m_decoder.m_header_data = (OPJ_BYTE *) opj_malloc(OPJ_J2K_DEFAULT_HEADER_SIZE);
        if (! l_j2k->m_specific_param.m_decoder.m_header_data) {
                opj_j2k_destroy(l_j2k);
                return 00;
        }

        l_j2k->m_specific_param.m_decoder.m_header_data_size = OPJ_J2K_DEFAULT_HEADER_SIZE;

        l_j2k->m_specific_param.m_decoder.m_tile_ind_to_dec = -1 ;

        l_j2k->m_specific_param.m_decoder.m_last_sot_read_pos = 0 ;

        /* codestream index creation */
        l_j2k->cstr_index = opj_j2k_create_cstr_index();

                        /*(opj_codestream_index_t*) opj_malloc(sizeof(opj_codestream_index_t));
        if (!l_j2k->cstr_index){
                opj_j2k_destroy(l_j2k);
                return NULL;
        }

        l_j2k->cstr_index->marker = (opj_marker_info_t*) opj_malloc(100 * sizeof(opj_marker_info_t));
*/

        /* validation list creation */
        l_j2k->m_validation_list = opj_procedure_list_create();
        if (! l_j2k->m_validation_list) {
                opj_j2k_destroy(l_j2k);
                return 00;
        }

        /* execution list creation */
        l_j2k->m_procedure_list = opj_procedure_list_create();
        if (! l_j2k->m_procedure_list) {
                opj_j2k_destroy(l_j2k);
                return 00;
        }

        return l_j2k;
}

opj_codestream_index_t* opj_j2k_create_cstr_index(void)
{
        opj_codestream_index_t* cstr_index = (opj_codestream_index_t*)
                        opj_calloc(1,sizeof(opj_codestream_index_t));
        if (!cstr_index)
                return NULL;

        cstr_index->maxmarknum = 100;
        cstr_index->marknum = 0;
        cstr_index->marker = (opj_marker_info_t*)
                        opj_calloc(cstr_index->maxmarknum, sizeof(opj_marker_info_t));
        if (!cstr_index-> marker)
                return NULL;

        cstr_index->tile_index = NULL;

        return cstr_index;
}

OPJ_UINT32 opj_j2k_get_SPCod_SPCoc_size (       opj_j2k_t *p_j2k,
                                                                                OPJ_UINT32 p_tile_no,
                                                                                OPJ_UINT32 p_comp_no )
{
        opj_cp_t *l_cp = 00;
        opj_tcp_t *l_tcp = 00;
        opj_tccp_t *l_tccp = 00;

        /* preconditions */
        assert(p_j2k != 00);

        l_cp = &(p_j2k->m_cp);
        l_tcp = &l_cp->tcps[p_tile_no];
        l_tccp = &l_tcp->tccps[p_comp_no];

        /* preconditions again */
        assert(p_tile_no < (l_cp->tw * l_cp->th));
        assert(p_comp_no < p_j2k->m_private_image->numcomps);

        if (l_tccp->csty & J2K_CCP_CSTY_PRT) {
                return 5 + l_tccp->numresolutions;
        }
        else {
                return 5;
        }
}

OPJ_BOOL opj_j2k_write_SPCod_SPCoc(     opj_j2k_t *p_j2k,
                                                                    OPJ_UINT32 p_tile_no,
                                                                    OPJ_UINT32 p_comp_no,
                                                                    OPJ_BYTE * p_data,
                                                                    OPJ_UINT32 * p_header_size,
                                                                    struct opj_event_mgr * p_manager )
{
        OPJ_UINT32 i;
        opj_cp_t *l_cp = 00;
        opj_tcp_t *l_tcp = 00;
        opj_tccp_t *l_tccp = 00;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_header_size != 00);
        assert(p_manager != 00);
        assert(p_data != 00);

        l_cp = &(p_j2k->m_cp);
        l_tcp = &l_cp->tcps[p_tile_no];
        l_tccp = &l_tcp->tccps[p_comp_no];

        /* preconditions again */
        assert(p_tile_no < (l_cp->tw * l_cp->th));
        assert(p_comp_no <(p_j2k->m_private_image->numcomps));

        if (*p_header_size < 5) {
                opj_event_msg(p_manager, EVT_ERROR, "Error writing SPCod SPCoc element\n");
                return OPJ_FALSE;
        }

        opj_write_bytes(p_data,l_tccp->numresolutions - 1, 1);  /* SPcoc (D) */
        ++p_data;

        opj_write_bytes(p_data,l_tccp->cblkw - 2, 1);                   /* SPcoc (E) */
        ++p_data;

        opj_write_bytes(p_data,l_tccp->cblkh - 2, 1);                   /* SPcoc (F) */
        ++p_data;

        opj_write_bytes(p_data,l_tccp->cblksty, 1);                             /* SPcoc (G) */
        ++p_data;

        opj_write_bytes(p_data,l_tccp->qmfbid, 1);                              /* SPcoc (H) */
        ++p_data;

        *p_header_size = *p_header_size - 5;

        if (l_tccp->csty & J2K_CCP_CSTY_PRT) {

                if (*p_header_size < l_tccp->numresolutions) {
                        opj_event_msg(p_manager, EVT_ERROR, "Error writing SPCod SPCoc element\n");
                        return OPJ_FALSE;
                }

                for (i = 0; i < l_tccp->numresolutions; ++i) {
                        opj_write_bytes(p_data,l_tccp->prcw[i] + (l_tccp->prch[i] << 4), 1);    /* SPcoc (I_i) */
                        ++p_data;
                }

                *p_header_size = *p_header_size - l_tccp->numresolutions;
        }

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_read_SPCod_SPCoc(  opj_j2k_t *p_j2k,
                                                                OPJ_UINT32 compno,
                                                                OPJ_BYTE * p_header_data,
                                                                OPJ_UINT32 * p_header_size,
                                                                opj_event_mgr_t * p_manager)
{
        OPJ_UINT32 i, l_tmp;
        opj_cp_t *l_cp = NULL;
        opj_tcp_t *l_tcp = NULL;
        opj_tccp_t *l_tccp = NULL;
        OPJ_BYTE * l_current_ptr = NULL;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_manager != 00);
        assert(p_header_data != 00);

        l_cp = &(p_j2k->m_cp);
        l_tcp = (p_j2k->m_specific_param.m_decoder.m_state == J2K_STATE_TPH) ?
                                &l_cp->tcps[p_j2k->m_current_tile_number] :
                                p_j2k->m_specific_param.m_decoder.m_default_tcp;

        /* precondition again */
        assert(compno < p_j2k->m_private_image->numcomps);

        l_tccp = &l_tcp->tccps[compno];
        l_current_ptr = p_header_data;

        /* make sure room is sufficient */
        if (*p_header_size < 5) {
                opj_event_msg(p_manager, EVT_ERROR, "Error reading SPCod SPCoc element\n");
                return OPJ_FALSE;
        }

        opj_read_bytes(l_current_ptr, &l_tccp->numresolutions ,1);              /* SPcox (D) */
        ++l_tccp->numresolutions;                                                                               /* tccp->numresolutions = read() + 1 */
        if (l_tccp->numresolutions > OPJ_J2K_MAXRLVLS) {
                opj_event_msg(p_manager, EVT_ERROR,
                              "Invalid value for numresolutions : %d, max value is set in openjpeg.h at %d\n",
                              l_tccp->numresolutions, OPJ_J2K_MAXRLVLS);
                return OPJ_FALSE;
        }
        ++l_current_ptr;

        /* If user wants to remove more resolutions than the codestream contains, return error */
        if (l_cp->m_specific_param.m_dec.m_reduce >= l_tccp->numresolutions) {
                opj_event_msg(p_manager, EVT_ERROR, "Error decoding component %d.\nThe number of resolutions to remove is higher than the number "
                                        "of resolutions of this component\nModify the cp_reduce parameter.\n\n", compno);
                p_j2k->m_specific_param.m_decoder.m_state |= 0x8000;/* FIXME J2K_DEC_STATE_ERR;*/
                return OPJ_FALSE;
        }

        opj_read_bytes(l_current_ptr,&l_tccp->cblkw ,1);                /* SPcoc (E) */
        ++l_current_ptr;
        l_tccp->cblkw += 2;

        opj_read_bytes(l_current_ptr,&l_tccp->cblkh ,1);                /* SPcoc (F) */
        ++l_current_ptr;
        l_tccp->cblkh += 2;

        opj_read_bytes(l_current_ptr,&l_tccp->cblksty ,1);              /* SPcoc (G) */
        ++l_current_ptr;

        opj_read_bytes(l_current_ptr,&l_tccp->qmfbid ,1);               /* SPcoc (H) */
        ++l_current_ptr;

        *p_header_size = *p_header_size - 5;

        /* use custom precinct size ? */
        if (l_tccp->csty & J2K_CCP_CSTY_PRT) {
                if (*p_header_size < l_tccp->numresolutions) {
                        opj_event_msg(p_manager, EVT_ERROR, "Error reading SPCod SPCoc element\n");
                        return OPJ_FALSE;
                }

                for     (i = 0; i < l_tccp->numresolutions; ++i) {
                        opj_read_bytes(l_current_ptr,&l_tmp ,1);                /* SPcoc (I_i) */
                        ++l_current_ptr;
                        l_tccp->prcw[i] = l_tmp & 0xf;
                        l_tccp->prch[i] = l_tmp >> 4;
                }

                *p_header_size = *p_header_size - l_tccp->numresolutions;
        }
        else {
                /* set default size for the precinct width and height */
                for     (i = 0; i < l_tccp->numresolutions; ++i) {
                        l_tccp->prcw[i] = 15;
                        l_tccp->prch[i] = 15;
                }
        }

#ifdef WIP_REMOVE_MSD
        /* INDEX >> */
        if (p_j2k->cstr_info && compno == 0) {
                OPJ_UINT32 l_data_size = l_tccp->numresolutions * sizeof(OPJ_UINT32);

                p_j2k->cstr_info->tile[p_j2k->m_current_tile_number].tccp_info[compno].cblkh = l_tccp->cblkh;
                p_j2k->cstr_info->tile[p_j2k->m_current_tile_number].tccp_info[compno].cblkw = l_tccp->cblkw;
                p_j2k->cstr_info->tile[p_j2k->m_current_tile_number].tccp_info[compno].numresolutions = l_tccp->numresolutions;
                p_j2k->cstr_info->tile[p_j2k->m_current_tile_number].tccp_info[compno].cblksty = l_tccp->cblksty;
                p_j2k->cstr_info->tile[p_j2k->m_current_tile_number].tccp_info[compno].qmfbid = l_tccp->qmfbid;

                memcpy(p_j2k->cstr_info->tile[p_j2k->m_current_tile_number].pdx,l_tccp->prcw, l_data_size);
                memcpy(p_j2k->cstr_info->tile[p_j2k->m_current_tile_number].pdy,l_tccp->prch, l_data_size);
        }
        /* << INDEX */
#endif

        return OPJ_TRUE;
}

void opj_j2k_copy_tile_component_parameters( opj_j2k_t *p_j2k )
{
        /* loop */
        OPJ_UINT32 i;
        opj_cp_t *l_cp = NULL;
        opj_tcp_t *l_tcp = NULL;
        opj_tccp_t *l_ref_tccp = NULL, *l_copied_tccp = NULL;
        OPJ_UINT32 l_prc_size;

        /* preconditions */
        assert(p_j2k != 00);

        l_cp = &(p_j2k->m_cp);
        l_tcp = (p_j2k->m_specific_param.m_decoder.m_state == J2K_STATE_TPH) ? /* FIXME J2K_DEC_STATE_TPH*/
                                &l_cp->tcps[p_j2k->m_current_tile_number] :
                                p_j2k->m_specific_param.m_decoder.m_default_tcp;

        l_ref_tccp = &l_tcp->tccps[0];
        l_copied_tccp = l_ref_tccp + 1;
        l_prc_size = l_ref_tccp->numresolutions * (OPJ_UINT32)sizeof(OPJ_UINT32);

        for     (i=1; i<p_j2k->m_private_image->numcomps; ++i) {
                l_copied_tccp->numresolutions = l_ref_tccp->numresolutions;
                l_copied_tccp->cblkw = l_ref_tccp->cblkw;
                l_copied_tccp->cblkh = l_ref_tccp->cblkh;
                l_copied_tccp->cblksty = l_ref_tccp->cblksty;
                l_copied_tccp->qmfbid = l_ref_tccp->qmfbid;
                memcpy(l_copied_tccp->prcw,l_ref_tccp->prcw,l_prc_size);
                memcpy(l_copied_tccp->prch,l_ref_tccp->prch,l_prc_size);
                ++l_copied_tccp;
        }
}

OPJ_UINT32 opj_j2k_get_SQcd_SQcc_size ( opj_j2k_t *p_j2k,
                                                                        OPJ_UINT32 p_tile_no,
                                                                        OPJ_UINT32 p_comp_no )
{
        OPJ_UINT32 l_num_bands;

        opj_cp_t *l_cp = 00;
        opj_tcp_t *l_tcp = 00;
        opj_tccp_t *l_tccp = 00;

        /* preconditions */
        assert(p_j2k != 00);

        l_cp = &(p_j2k->m_cp);
        l_tcp = &l_cp->tcps[p_tile_no];
        l_tccp = &l_tcp->tccps[p_comp_no];

        /* preconditions again */
        assert(p_tile_no < l_cp->tw * l_cp->th);
        assert(p_comp_no < p_j2k->m_private_image->numcomps);

        l_num_bands = (l_tccp->qntsty == J2K_CCP_QNTSTY_SIQNT) ? 1 : (l_tccp->numresolutions * 3 - 2);

        if (l_tccp->qntsty == J2K_CCP_QNTSTY_NOQNT)  {
                return 1 + l_num_bands;
        }
        else {
                return 1 + 2*l_num_bands;
        }
}

OPJ_BOOL opj_j2k_write_SQcd_SQcc(       opj_j2k_t *p_j2k,
                                                                OPJ_UINT32 p_tile_no,
                                                                OPJ_UINT32 p_comp_no,
                                                                OPJ_BYTE * p_data,
                                                                OPJ_UINT32 * p_header_size,
                                                                struct opj_event_mgr * p_manager )
{
        OPJ_UINT32 l_header_size;
        OPJ_UINT32 l_band_no, l_num_bands;
        OPJ_UINT32 l_expn,l_mant;

        opj_cp_t *l_cp = 00;
        opj_tcp_t *l_tcp = 00;
        opj_tccp_t *l_tccp = 00;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_header_size != 00);
        assert(p_manager != 00);
        assert(p_data != 00);

        l_cp = &(p_j2k->m_cp);
        l_tcp = &l_cp->tcps[p_tile_no];
        l_tccp = &l_tcp->tccps[p_comp_no];

        /* preconditions again */
        assert(p_tile_no < l_cp->tw * l_cp->th);
        assert(p_comp_no <p_j2k->m_private_image->numcomps);

        l_num_bands = (l_tccp->qntsty == J2K_CCP_QNTSTY_SIQNT) ? 1 : (l_tccp->numresolutions * 3 - 2);

        if (l_tccp->qntsty == J2K_CCP_QNTSTY_NOQNT)  {
                l_header_size = 1 + l_num_bands;

                if (*p_header_size < l_header_size) {
                        opj_event_msg(p_manager, EVT_ERROR, "Error writing SQcd SQcc element\n");
                        return OPJ_FALSE;
                }

                opj_write_bytes(p_data,l_tccp->qntsty + (l_tccp->numgbits << 5), 1);    /* Sqcx */
                ++p_data;

                for (l_band_no = 0; l_band_no < l_num_bands; ++l_band_no) {
                        l_expn = (OPJ_UINT32)l_tccp->stepsizes[l_band_no].expn;
                        opj_write_bytes(p_data, l_expn << 3, 1);        /* SPqcx_i */
                        ++p_data;
                }
        }
        else {
                l_header_size = 1 + 2*l_num_bands;

                if (*p_header_size < l_header_size) {
                        opj_event_msg(p_manager, EVT_ERROR, "Error writing SQcd SQcc element\n");
                        return OPJ_FALSE;
                }

                opj_write_bytes(p_data,l_tccp->qntsty + (l_tccp->numgbits << 5), 1);    /* Sqcx */
                ++p_data;

                for (l_band_no = 0; l_band_no < l_num_bands; ++l_band_no) {
                        l_expn = (OPJ_UINT32)l_tccp->stepsizes[l_band_no].expn;
                        l_mant = (OPJ_UINT32)l_tccp->stepsizes[l_band_no].mant;

                        opj_write_bytes(p_data, (l_expn << 11) + l_mant, 2);    /* SPqcx_i */
                        p_data += 2;
                }
        }

        *p_header_size = *p_header_size - l_header_size;

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_read_SQcd_SQcc(opj_j2k_t *p_j2k,
                                                            OPJ_UINT32 p_comp_no,
                                                            OPJ_BYTE* p_header_data,
                                                            OPJ_UINT32 * p_header_size,
                                                            opj_event_mgr_t * p_manager
                                                            )
{
        /* loop*/
        OPJ_UINT32 l_band_no;
        opj_cp_t *l_cp = 00;
        opj_tcp_t *l_tcp = 00;
        opj_tccp_t *l_tccp = 00;
        OPJ_BYTE * l_current_ptr = 00;
        OPJ_UINT32 l_tmp, l_num_band;

        /* preconditions*/
        assert(p_j2k != 00);
        assert(p_manager != 00);
        assert(p_header_data != 00);

        l_cp = &(p_j2k->m_cp);
        /* come from tile part header or main header ?*/
        l_tcp = (p_j2k->m_specific_param.m_decoder.m_state == J2K_STATE_TPH) ? /*FIXME J2K_DEC_STATE_TPH*/
                                &l_cp->tcps[p_j2k->m_current_tile_number] :
                                p_j2k->m_specific_param.m_decoder.m_default_tcp;

        /* precondition again*/
        assert(p_comp_no <  p_j2k->m_private_image->numcomps);

        l_tccp = &l_tcp->tccps[p_comp_no];
        l_current_ptr = p_header_data;

        if (*p_header_size < 1) {
                opj_event_msg(p_manager, EVT_ERROR, "Error reading SQcd or SQcc element\n");
                return OPJ_FALSE;
        }
        *p_header_size -= 1;

        opj_read_bytes(l_current_ptr, &l_tmp ,1);                       /* Sqcx */
        ++l_current_ptr;

        l_tccp->qntsty = l_tmp & 0x1f;
        l_tccp->numgbits = l_tmp >> 5;
        if (l_tccp->qntsty == J2K_CCP_QNTSTY_SIQNT) {
        l_num_band = 1;
        }
        else {
                l_num_band = (l_tccp->qntsty == J2K_CCP_QNTSTY_NOQNT) ?
                        (*p_header_size) :
                        (*p_header_size) / 2;

                if( l_num_band > OPJ_J2K_MAXBANDS ) {
                        opj_event_msg(p_manager, EVT_WARNING, "While reading CCP_QNTSTY element inside QCD or QCC marker segment, "
                                "number of subbands (%d) is greater to OPJ_J2K_MAXBANDS (%d). So we limit the number of elements stored to "
                                "OPJ_J2K_MAXBANDS (%d) and skip the rest. \n", l_num_band, OPJ_J2K_MAXBANDS, OPJ_J2K_MAXBANDS);
                        /*return OPJ_FALSE;*/
                }
        }

#ifdef USE_JPWL
        if (l_cp->correct) {

                /* if JPWL is on, we check whether there are too many subbands */
                if (/*(l_num_band < 0) ||*/ (l_num_band >= OPJ_J2K_MAXBANDS)) {
                        opj_event_msg(p_manager, JPWL_ASSUME ? EVT_WARNING : EVT_ERROR,
                                "JPWL: bad number of subbands in Sqcx (%d)\n",
                                l_num_band);
                        if (!JPWL_ASSUME) {
                                opj_event_msg(p_manager, EVT_ERROR, "JPWL: giving up\n");
                                return OPJ_FALSE;
                        }
                        /* we try to correct */
                        l_num_band = 1;
                        opj_event_msg(p_manager, EVT_WARNING, "- trying to adjust them\n"
                                "- setting number of bands to %d => HYPOTHESIS!!!\n",
                                l_num_band);
                };

        };
#endif /* USE_JPWL */

        if (l_tccp->qntsty == J2K_CCP_QNTSTY_NOQNT) {
                for     (l_band_no = 0; l_band_no < l_num_band; l_band_no++) {
                        opj_read_bytes(l_current_ptr, &l_tmp ,1);                       /* SPqcx_i */
                        ++l_current_ptr;
                        if (l_band_no < OPJ_J2K_MAXBANDS){
                                l_tccp->stepsizes[l_band_no].expn = (OPJ_INT32)(l_tmp >> 3);
                                l_tccp->stepsizes[l_band_no].mant = 0;
                        }
                }
                *p_header_size = *p_header_size - l_num_band;
        }
        else {
                for     (l_band_no = 0; l_band_no < l_num_band; l_band_no++) {
                        opj_read_bytes(l_current_ptr, &l_tmp ,2);                       /* SPqcx_i */
                        l_current_ptr+=2;
                        if (l_band_no < OPJ_J2K_MAXBANDS){
                                l_tccp->stepsizes[l_band_no].expn = (OPJ_INT32)(l_tmp >> 11);
                                l_tccp->stepsizes[l_band_no].mant = l_tmp & 0x7ff;
                        }
                }
                *p_header_size = *p_header_size - 2*l_num_band;
        }

        /* Add Antonin : if scalar_derived -> compute other stepsizes */
        if (l_tccp->qntsty == J2K_CCP_QNTSTY_SIQNT) {
                for (l_band_no = 1; l_band_no < OPJ_J2K_MAXBANDS; l_band_no++) {
                        l_tccp->stepsizes[l_band_no].expn =
                                ((OPJ_INT32)(l_tccp->stepsizes[0].expn) - (OPJ_INT32)((l_band_no - 1) / 3) > 0) ?
                                        (OPJ_INT32)(l_tccp->stepsizes[0].expn) - (OPJ_INT32)((l_band_no - 1) / 3) : 0;
                        l_tccp->stepsizes[l_band_no].mant = l_tccp->stepsizes[0].mant;
                }
        }

        return OPJ_TRUE;
}

void opj_j2k_copy_tile_quantization_parameters( opj_j2k_t *p_j2k )
{
        OPJ_UINT32 i;
        opj_cp_t *l_cp = NULL;
        opj_tcp_t *l_tcp = NULL;
        opj_tccp_t *l_ref_tccp = NULL;
        opj_tccp_t *l_copied_tccp = NULL;
        OPJ_UINT32 l_size;

        /* preconditions */
        assert(p_j2k != 00);

        l_cp = &(p_j2k->m_cp);
        l_tcp = p_j2k->m_specific_param.m_decoder.m_state == J2K_STATE_TPH ?
                        &l_cp->tcps[p_j2k->m_current_tile_number] :
                        p_j2k->m_specific_param.m_decoder.m_default_tcp;

        l_ref_tccp = &l_tcp->tccps[0];
        l_copied_tccp = l_ref_tccp + 1;
        l_size = OPJ_J2K_MAXBANDS * sizeof(opj_stepsize_t);

        for     (i=1;i<p_j2k->m_private_image->numcomps;++i) {
                l_copied_tccp->qntsty = l_ref_tccp->qntsty;
                l_copied_tccp->numgbits = l_ref_tccp->numgbits;
                memcpy(l_copied_tccp->stepsizes,l_ref_tccp->stepsizes,l_size);
                ++l_copied_tccp;
        }
}

static void opj_j2k_dump_tile_info( opj_tcp_t * l_default_tile,OPJ_INT32 numcomps,FILE* out_stream)
{
        if (l_default_tile)
        {
                OPJ_INT32 compno;

                fprintf(out_stream, "\t default tile {\n");
                fprintf(out_stream, "\t\t csty=%#x\n", l_default_tile->csty);
                fprintf(out_stream, "\t\t prg=%#x\n", l_default_tile->prg);
                fprintf(out_stream, "\t\t numlayers=%d\n", l_default_tile->numlayers);
                fprintf(out_stream, "\t\t mct=%x\n", l_default_tile->mct);

                for (compno = 0; compno < numcomps; compno++) {
                        opj_tccp_t *l_tccp = &(l_default_tile->tccps[compno]);
                        OPJ_UINT32 resno;
      OPJ_INT32 bandno, numbands;

                        /* coding style*/
                        fprintf(out_stream, "\t\t comp %d {\n", compno);
                        fprintf(out_stream, "\t\t\t csty=%#x\n", l_tccp->csty);
                        fprintf(out_stream, "\t\t\t numresolutions=%d\n", l_tccp->numresolutions);
                        fprintf(out_stream, "\t\t\t cblkw=2^%d\n", l_tccp->cblkw);
                        fprintf(out_stream, "\t\t\t cblkh=2^%d\n", l_tccp->cblkh);
                        fprintf(out_stream, "\t\t\t cblksty=%#x\n", l_tccp->cblksty);
                        fprintf(out_stream, "\t\t\t qmfbid=%d\n", l_tccp->qmfbid);

                        fprintf(out_stream, "\t\t\t preccintsize (w,h)=");
                        for (resno = 0; resno < l_tccp->numresolutions; resno++) {
                                fprintf(out_stream, "(%d,%d) ", l_tccp->prcw[resno], l_tccp->prch[resno]);
                        }
                        fprintf(out_stream, "\n");

                        /* quantization style*/
                        fprintf(out_stream, "\t\t\t qntsty=%d\n", l_tccp->qntsty);
                        fprintf(out_stream, "\t\t\t numgbits=%d\n", l_tccp->numgbits);
                        fprintf(out_stream, "\t\t\t stepsizes (m,e)=");
                        numbands = (l_tccp->qntsty == J2K_CCP_QNTSTY_SIQNT) ? 1 : (OPJ_INT32)l_tccp->numresolutions * 3 - 2;
                        for (bandno = 0; bandno < numbands; bandno++) {
                                fprintf(out_stream, "(%d,%d) ", l_tccp->stepsizes[bandno].mant,
                                        l_tccp->stepsizes[bandno].expn);
                        }
                        fprintf(out_stream, "\n");

                        /* RGN value*/
                        fprintf(out_stream, "\t\t\t roishift=%d\n", l_tccp->roishift);

                        fprintf(out_stream, "\t\t }\n");
                } /*end of component of default tile*/
                fprintf(out_stream, "\t }\n"); /*end of default tile*/
            }
}

void j2k_dump (opj_j2k_t* p_j2k, OPJ_INT32 flag, FILE* out_stream)
{
        /* Check if the flag is compatible with j2k file*/
        if ( (flag & OPJ_JP2_INFO) || (flag & OPJ_JP2_IND)){
                fprintf(out_stream, "Wrong flag\n");
                return;
        }

        /* Dump the image_header */
        if (flag & OPJ_IMG_INFO){
                if (p_j2k->m_private_image)
                        j2k_dump_image_header(p_j2k->m_private_image, 0, out_stream);
        }

        /* Dump the codestream info from main header */
        if (flag & OPJ_J2K_MH_INFO){
                opj_j2k_dump_MH_info(p_j2k, out_stream);
        }
        /* Dump all tile/codestream info */
        if (flag & OPJ_J2K_TCH_INFO){
          OPJ_UINT32 l_nb_tiles = p_j2k->m_cp.th * p_j2k->m_cp.tw;
          OPJ_UINT32 i;
          opj_tcp_t * l_tcp = p_j2k->m_cp.tcps;
          for (i=0;i<l_nb_tiles;++i) {
            opj_j2k_dump_tile_info( l_tcp,(OPJ_INT32)p_j2k->m_private_image->numcomps, out_stream);
            ++l_tcp;
          }
        }

        /* Dump the codestream info of the current tile */
        if (flag & OPJ_J2K_TH_INFO){

        }

        /* Dump the codestream index from main header */
        if (flag & OPJ_J2K_MH_IND){
                opj_j2k_dump_MH_index(p_j2k, out_stream);
        }

        /* Dump the codestream index of the current tile */
        if (flag & OPJ_J2K_TH_IND){

        }

}

void opj_j2k_dump_MH_index(opj_j2k_t* p_j2k, FILE* out_stream)
{
        opj_codestream_index_t* cstr_index = p_j2k->cstr_index;
        OPJ_UINT32 it_marker, it_tile, it_tile_part;

        fprintf(out_stream, "Codestream index from main header: {\n");

        fprintf(out_stream, "\t Main header start position=%" PRIi64 "\n"
                                    "\t Main header end position=%" PRIi64 "\n",
                        cstr_index->main_head_start, cstr_index->main_head_end);

        fprintf(out_stream, "\t Marker list: {\n");

        if (cstr_index->marker){
                for (it_marker=0; it_marker < cstr_index->marknum ; it_marker++){
                        fprintf(out_stream, "\t\t type=%#x, pos=%" PRIi64 ", len=%d\n",
                                        cstr_index->marker[it_marker].type,
                                        cstr_index->marker[it_marker].pos,
                                        cstr_index->marker[it_marker].len );
                }
        }

        fprintf(out_stream, "\t }\n");

        if (cstr_index->tile_index){

        /* Simple test to avoid to write empty information*/
        OPJ_UINT32 l_acc_nb_of_tile_part = 0;
        for (it_tile=0; it_tile < cstr_index->nb_of_tiles ; it_tile++){
                        l_acc_nb_of_tile_part += cstr_index->tile_index[it_tile].nb_tps;
        }

        if (l_acc_nb_of_tile_part)
        {
            fprintf(out_stream, "\t Tile index: {\n");

                    for (it_tile=0; it_tile < cstr_index->nb_of_tiles ; it_tile++){
                            OPJ_UINT32 nb_of_tile_part = cstr_index->tile_index[it_tile].nb_tps;

                            fprintf(out_stream, "\t\t nb of tile-part in tile [%d]=%d\n", it_tile, nb_of_tile_part);

                            if (cstr_index->tile_index[it_tile].tp_index){
                                    for (it_tile_part =0; it_tile_part < nb_of_tile_part; it_tile_part++){
                                            fprintf(out_stream, "\t\t\t tile-part[%d]: star_pos=%" PRIi64 ", end_header=%" PRIi64 ", end_pos=%" PRIi64 ".\n",
                                                            it_tile_part,
                                                            cstr_index->tile_index[it_tile].tp_index[it_tile_part].start_pos,
                                                            cstr_index->tile_index[it_tile].tp_index[it_tile_part].end_header,
                                                            cstr_index->tile_index[it_tile].tp_index[it_tile_part].end_pos);
                                    }
                            }

                            if (cstr_index->tile_index[it_tile].marker){
                                    for (it_marker=0; it_marker < cstr_index->tile_index[it_tile].marknum ; it_marker++){
                                            fprintf(out_stream, "\t\t type=%#x, pos=%" PRIi64 ", len=%d\n",
                                                            cstr_index->tile_index[it_tile].marker[it_marker].type,
                                                            cstr_index->tile_index[it_tile].marker[it_marker].pos,
                                                            cstr_index->tile_index[it_tile].marker[it_marker].len );
                                    }
                            }
                    }
                    fprintf(out_stream,"\t }\n");
        }
        }

        fprintf(out_stream,"}\n");

}


void opj_j2k_dump_MH_info(opj_j2k_t* p_j2k, FILE* out_stream)
{

        fprintf(out_stream, "Codestream info from main header: {\n");

        fprintf(out_stream, "\t tx0=%d, ty0=%d\n", p_j2k->m_cp.tx0, p_j2k->m_cp.ty0);
        fprintf(out_stream, "\t tdx=%d, tdy=%d\n", p_j2k->m_cp.tdx, p_j2k->m_cp.tdy);
        fprintf(out_stream, "\t tw=%d, th=%d\n", p_j2k->m_cp.tw, p_j2k->m_cp.th);
        opj_j2k_dump_tile_info(p_j2k->m_specific_param.m_decoder.m_default_tcp,(OPJ_INT32)p_j2k->m_private_image->numcomps, out_stream);
        fprintf(out_stream, "}\n");
}

void j2k_dump_image_header(opj_image_t* img_header, OPJ_BOOL dev_dump_flag, FILE* out_stream)
{
        char tab[2];

        if (dev_dump_flag){
                fprintf(stdout, "[DEV] Dump an image_header struct {\n");
                tab[0] = '\0';
        }
        else {
                fprintf(out_stream, "Image info {\n");
                tab[0] = '\t';tab[1] = '\0';
        }

        fprintf(out_stream, "%s x0=%d, y0=%d\n", tab, img_header->x0, img_header->y0);
        fprintf(out_stream,     "%s x1=%d, y1=%d\n", tab, img_header->x1, img_header->y1);
        fprintf(out_stream, "%s numcomps=%d\n", tab, img_header->numcomps);

        if (img_header->comps){
                OPJ_UINT32 compno;
                for (compno = 0; compno < img_header->numcomps; compno++) {
                        fprintf(out_stream, "%s\t component %d {\n", tab, compno);
                        j2k_dump_image_comp_header(&(img_header->comps[compno]), dev_dump_flag, out_stream);
                        fprintf(out_stream,"%s}\n",tab);
                }
        }

        fprintf(out_stream, "}\n");
}

void j2k_dump_image_comp_header(opj_image_comp_t* comp_header, OPJ_BOOL dev_dump_flag, FILE* out_stream)
{
        char tab[3];

        if (dev_dump_flag){
                fprintf(stdout, "[DEV] Dump an image_comp_header struct {\n");
                tab[0] = '\0';
        }       else {
                tab[0] = '\t';tab[1] = '\t';tab[2] = '\0';
        }

        fprintf(out_stream, "%s dx=%d, dy=%d\n", tab, comp_header->dx, comp_header->dy);
        fprintf(out_stream, "%s prec=%d\n", tab, comp_header->prec);
        fprintf(out_stream, "%s sgnd=%d\n", tab, comp_header->sgnd);

        if (dev_dump_flag)
                fprintf(out_stream, "}\n");
}

opj_codestream_info_v2_t* j2k_get_cstr_info(opj_j2k_t* p_j2k)
{
        OPJ_UINT32 compno;
        OPJ_UINT32 numcomps = p_j2k->m_private_image->numcomps;
        opj_tcp_t *l_default_tile;
        opj_codestream_info_v2_t* cstr_info = (opj_codestream_info_v2_t*) opj_calloc(1,sizeof(opj_codestream_info_v2_t));
		if (!cstr_info)
			return NULL;

        cstr_info->nbcomps = p_j2k->m_private_image->numcomps;

        cstr_info->tx0 = p_j2k->m_cp.tx0;
        cstr_info->ty0 = p_j2k->m_cp.ty0;
        cstr_info->tdx = p_j2k->m_cp.tdx;
        cstr_info->tdy = p_j2k->m_cp.tdy;
        cstr_info->tw = p_j2k->m_cp.tw;
        cstr_info->th = p_j2k->m_cp.th;

        cstr_info->tile_info = NULL; /* Not fill from the main header*/

        l_default_tile = p_j2k->m_specific_param.m_decoder.m_default_tcp;

        cstr_info->m_default_tile_info.csty = l_default_tile->csty;
        cstr_info->m_default_tile_info.prg = l_default_tile->prg;
        cstr_info->m_default_tile_info.numlayers = l_default_tile->numlayers;
        cstr_info->m_default_tile_info.mct = l_default_tile->mct;

        cstr_info->m_default_tile_info.tccp_info = (opj_tccp_info_t*) opj_calloc(cstr_info->nbcomps, sizeof(opj_tccp_info_t));
		if (!cstr_info->m_default_tile_info.tccp_info)
		{
			opj_destroy_cstr_info(&cstr_info);
			return NULL;
		}

        for (compno = 0; compno < numcomps; compno++) {
                opj_tccp_t *l_tccp = &(l_default_tile->tccps[compno]);
                opj_tccp_info_t *l_tccp_info = &(cstr_info->m_default_tile_info.tccp_info[compno]);
                OPJ_INT32 bandno, numbands;

                /* coding style*/
                l_tccp_info->csty = l_tccp->csty;
                l_tccp_info->numresolutions = l_tccp->numresolutions;
                l_tccp_info->cblkw = l_tccp->cblkw;
                l_tccp_info->cblkh = l_tccp->cblkh;
                l_tccp_info->cblksty = l_tccp->cblksty;
                l_tccp_info->qmfbid = l_tccp->qmfbid;
                if (l_tccp->numresolutions < OPJ_J2K_MAXRLVLS)
                {
                        memcpy(l_tccp_info->prch, l_tccp->prch, l_tccp->numresolutions);
                        memcpy(l_tccp_info->prcw, l_tccp->prcw, l_tccp->numresolutions);
                }

                /* quantization style*/
                l_tccp_info->qntsty = l_tccp->qntsty;
                l_tccp_info->numgbits = l_tccp->numgbits;

                numbands = (l_tccp->qntsty == J2K_CCP_QNTSTY_SIQNT) ? 1 : (OPJ_INT32)l_tccp->numresolutions * 3 - 2;
                if (numbands < OPJ_J2K_MAXBANDS) {
                        for (bandno = 0; bandno < numbands; bandno++) {
                                l_tccp_info->stepsizes_mant[bandno] = (OPJ_UINT32)l_tccp->stepsizes[bandno].mant;
                                l_tccp_info->stepsizes_expn[bandno] = (OPJ_UINT32)l_tccp->stepsizes[bandno].expn;
                        }
                }

                /* RGN value*/
                l_tccp_info->roishift = l_tccp->roishift;
        }

        return cstr_info;
}

opj_codestream_index_t* j2k_get_cstr_index(opj_j2k_t* p_j2k)
{
        opj_codestream_index_t* l_cstr_index = (opj_codestream_index_t*)
                        opj_calloc(1,sizeof(opj_codestream_index_t));
        if (!l_cstr_index)
                return NULL;

        l_cstr_index->main_head_start = p_j2k->cstr_index->main_head_start;
        l_cstr_index->main_head_end = p_j2k->cstr_index->main_head_end;
        l_cstr_index->codestream_size = p_j2k->cstr_index->codestream_size;

        l_cstr_index->marknum = p_j2k->cstr_index->marknum;
        l_cstr_index->marker = (opj_marker_info_t*)opj_malloc(l_cstr_index->marknum*sizeof(opj_marker_info_t));
        if (!l_cstr_index->marker){
                opj_free( l_cstr_index);
                return NULL;
        }

        if (p_j2k->cstr_index->marker)
                memcpy(l_cstr_index->marker, p_j2k->cstr_index->marker, l_cstr_index->marknum * sizeof(opj_marker_info_t) );
        else{
                opj_free(l_cstr_index->marker);
                l_cstr_index->marker = NULL;
        }

        l_cstr_index->nb_of_tiles = p_j2k->cstr_index->nb_of_tiles;
        l_cstr_index->tile_index = (opj_tile_index_t*)opj_calloc(l_cstr_index->nb_of_tiles, sizeof(opj_tile_index_t) );
        if (!l_cstr_index->tile_index){
                opj_free( l_cstr_index->marker);
                opj_free( l_cstr_index);
                return NULL;
        }

        if (!p_j2k->cstr_index->tile_index){
                opj_free(l_cstr_index->tile_index);
                l_cstr_index->tile_index = NULL;
        }
        else {
                OPJ_UINT32 it_tile = 0;
                for (it_tile = 0; it_tile < l_cstr_index->nb_of_tiles; it_tile++ ){

                        /* Tile Marker*/
                        l_cstr_index->tile_index[it_tile].marknum = p_j2k->cstr_index->tile_index[it_tile].marknum;

                        l_cstr_index->tile_index[it_tile].marker =
                                (opj_marker_info_t*)opj_malloc(l_cstr_index->tile_index[it_tile].marknum*sizeof(opj_marker_info_t));

                        if (!l_cstr_index->tile_index[it_tile].marker) {
                                OPJ_UINT32 it_tile_free;

                                for (it_tile_free=0; it_tile_free < it_tile; it_tile_free++){
                                        opj_free(l_cstr_index->tile_index[it_tile_free].marker);
                                }

                                opj_free( l_cstr_index->tile_index);
                                opj_free( l_cstr_index->marker);
                                opj_free( l_cstr_index);
                                return NULL;
                        }

                        if (p_j2k->cstr_index->tile_index[it_tile].marker)
                                memcpy( l_cstr_index->tile_index[it_tile].marker,
                                                p_j2k->cstr_index->tile_index[it_tile].marker,
                                                l_cstr_index->tile_index[it_tile].marknum * sizeof(opj_marker_info_t) );
                        else{
                                opj_free(l_cstr_index->tile_index[it_tile].marker);
                                l_cstr_index->tile_index[it_tile].marker = NULL;
                        }

                        /* Tile part index*/
                        l_cstr_index->tile_index[it_tile].nb_tps = p_j2k->cstr_index->tile_index[it_tile].nb_tps;

                        l_cstr_index->tile_index[it_tile].tp_index =
                                (opj_tp_index_t*)opj_malloc(l_cstr_index->tile_index[it_tile].nb_tps*sizeof(opj_tp_index_t));

                        if(!l_cstr_index->tile_index[it_tile].tp_index){
                                OPJ_UINT32 it_tile_free;

                                for (it_tile_free=0; it_tile_free < it_tile; it_tile_free++){
                                        opj_free(l_cstr_index->tile_index[it_tile_free].marker);
                                        opj_free(l_cstr_index->tile_index[it_tile_free].tp_index);
                                }

                                opj_free( l_cstr_index->tile_index);
                                opj_free( l_cstr_index->marker);
                                opj_free( l_cstr_index);
                                return NULL;
                        }

                        if (p_j2k->cstr_index->tile_index[it_tile].tp_index){
                                memcpy( l_cstr_index->tile_index[it_tile].tp_index,
                                                p_j2k->cstr_index->tile_index[it_tile].tp_index,
                                                l_cstr_index->tile_index[it_tile].nb_tps * sizeof(opj_tp_index_t) );
                        }
                        else{
                                opj_free(l_cstr_index->tile_index[it_tile].tp_index);
                                l_cstr_index->tile_index[it_tile].tp_index = NULL;
                        }

                        /* Packet index (NOT USED)*/
                        l_cstr_index->tile_index[it_tile].nb_packet = 0;
                        l_cstr_index->tile_index[it_tile].packet_index = NULL;

                }
        }

        return l_cstr_index;
}

OPJ_BOOL opj_j2k_allocate_tile_element_cstr_index(opj_j2k_t *p_j2k)
{
        OPJ_UINT32 it_tile=0;

        p_j2k->cstr_index->nb_of_tiles = p_j2k->m_cp.tw * p_j2k->m_cp.th;
        p_j2k->cstr_index->tile_index = (opj_tile_index_t*)opj_calloc(p_j2k->cstr_index->nb_of_tiles, sizeof(opj_tile_index_t));
        if (!p_j2k->cstr_index->tile_index)
                return OPJ_FALSE;

        for (it_tile=0; it_tile < p_j2k->cstr_index->nb_of_tiles; it_tile++){
                p_j2k->cstr_index->tile_index[it_tile].maxmarknum = 100;
                p_j2k->cstr_index->tile_index[it_tile].marknum = 0;
                p_j2k->cstr_index->tile_index[it_tile].marker = (opj_marker_info_t*)
                                opj_calloc(p_j2k->cstr_index->tile_index[it_tile].maxmarknum, sizeof(opj_marker_info_t));
                if (!p_j2k->cstr_index->tile_index[it_tile].marker)
                        return OPJ_FALSE;
        }

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_decode_tiles ( opj_j2k_t *p_j2k,
                                                            opj_stream_private_t *p_stream,
                                                            opj_event_mgr_t * p_manager)
{
        OPJ_BOOL l_go_on = OPJ_TRUE;
        OPJ_UINT32 l_current_tile_no;
        OPJ_UINT32 l_data_size,l_max_data_size;
        OPJ_INT32 l_tile_x0,l_tile_y0,l_tile_x1,l_tile_y1;
        OPJ_UINT32 l_nb_comps;
        OPJ_BYTE * l_current_data;
        OPJ_UINT32 nr_tiles = 0;

        l_current_data = (OPJ_BYTE*)opj_malloc(1000);
        if (! l_current_data) {
                opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to decode tiles\n");
                return OPJ_FALSE;
        }
        l_max_data_size = 1000;

        while (OPJ_TRUE) {
                if (! opj_j2k_read_tile_header( p_j2k,
                                        &l_current_tile_no,
                                        &l_data_size,
                                        &l_tile_x0, &l_tile_y0,
                                        &l_tile_x1, &l_tile_y1,
                                        &l_nb_comps,
                                        &l_go_on,
                                        p_stream,
                                        p_manager)) {
                        opj_free(l_current_data);
                        return OPJ_FALSE;
                }

                if (! l_go_on) {
                        break;
                }

                if (l_data_size > l_max_data_size) {
                        OPJ_BYTE *l_new_current_data = (OPJ_BYTE *) opj_realloc(l_current_data, l_data_size);
                        if (! l_new_current_data) {
                                opj_free(l_current_data);
                                opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to decode tile %d/%d\n", l_current_tile_no +1, p_j2k->m_cp.th * p_j2k->m_cp.tw);
                                return OPJ_FALSE;
                        }
                        l_current_data = l_new_current_data;
                        l_max_data_size = l_data_size;
                }

                if (! opj_j2k_decode_tile(p_j2k,l_current_tile_no,l_current_data,l_data_size,p_stream,p_manager)) {
                        opj_free(l_current_data);
                        opj_event_msg(p_manager, EVT_ERROR, "Failed to decode tile %d/%d\n", l_current_tile_no +1, p_j2k->m_cp.th * p_j2k->m_cp.tw);
                        return OPJ_FALSE;
                }
                opj_event_msg(p_manager, EVT_INFO, "Tile %d/%d has been decoded.\n", l_current_tile_no +1, p_j2k->m_cp.th * p_j2k->m_cp.tw);

                if (! opj_j2k_update_image_data(p_j2k->m_tcd,l_current_data, p_j2k->m_output_image)) {
                        opj_free(l_current_data);
                        return OPJ_FALSE;
                }
                opj_event_msg(p_manager, EVT_INFO, "Image data has been updated with tile %d.\n\n", l_current_tile_no + 1);
                
                if(opj_stream_get_number_byte_left(p_stream) == 0  
                    && p_j2k->m_specific_param.m_decoder.m_state == J2K_STATE_NEOC)
                    break;
                if(++nr_tiles ==  p_j2k->m_cp.th * p_j2k->m_cp.tw) 
                    break;
        }

        opj_free(l_current_data);

        return OPJ_TRUE;
}

/**
 * Sets up the procedures to do on decoding data. Developpers wanting to extend the library can add their own reading procedures.
 */
static void opj_j2k_setup_decoding (opj_j2k_t *p_j2k)
{
        /* preconditions*/
        assert(p_j2k != 00);

        opj_procedure_list_add_procedure(p_j2k->m_procedure_list,(opj_procedure)opj_j2k_decode_tiles);
        /* DEVELOPER CORNER, add your custom procedures */

}

/*
 * Read and decode one tile.
 */
static OPJ_BOOL opj_j2k_decode_one_tile (       opj_j2k_t *p_j2k,
                                                                            opj_stream_private_t *p_stream,
                                                                            opj_event_mgr_t * p_manager)
{
        OPJ_BOOL l_go_on = OPJ_TRUE;
        OPJ_UINT32 l_current_tile_no;
        OPJ_UINT32 l_tile_no_to_dec;
        OPJ_UINT32 l_data_size,l_max_data_size;
        OPJ_INT32 l_tile_x0,l_tile_y0,l_tile_x1,l_tile_y1;
        OPJ_UINT32 l_nb_comps;
        OPJ_BYTE * l_current_data;

        l_current_data = (OPJ_BYTE*)opj_malloc(1000);
        if (! l_current_data) {
                opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to decode one tile\n");
                return OPJ_FALSE;
        }
        l_max_data_size = 1000;

        /*Allocate and initialize some elements of codestrem index if not already done*/
        if( !p_j2k->cstr_index->tile_index)
        {
                if (!opj_j2k_allocate_tile_element_cstr_index(p_j2k)){
                        opj_free(l_current_data);
                        return OPJ_FALSE;
                }
        }
        /* Move into the codestream to the first SOT used to decode the desired tile */
        l_tile_no_to_dec = (OPJ_UINT32)p_j2k->m_specific_param.m_decoder.m_tile_ind_to_dec;
        if (p_j2k->cstr_index->tile_index)
                if(p_j2k->cstr_index->tile_index->tp_index)
                {
                        if ( ! p_j2k->cstr_index->tile_index[l_tile_no_to_dec].nb_tps) {
                                /* the index for this tile has not been built,
                                 *  so move to the last SOT read */
                                if ( !(opj_stream_read_seek(p_stream, p_j2k->m_specific_param.m_decoder.m_last_sot_read_pos+2, p_manager)) ){
                                        opj_event_msg(p_manager, EVT_ERROR, "Problem with seek function\n");
                        opj_free(l_current_data);
                                        return OPJ_FALSE;
                                }
                        }
                        else{
                                if ( !(opj_stream_read_seek(p_stream, p_j2k->cstr_index->tile_index[l_tile_no_to_dec].tp_index[0].start_pos+2, p_manager)) ) {
                                        opj_event_msg(p_manager, EVT_ERROR, "Problem with seek function\n");
                        opj_free(l_current_data);
                                        return OPJ_FALSE;
                                }
                        }
                        /* Special case if we have previously read the EOC marker (if the previous tile getted is the last ) */
                        if(p_j2k->m_specific_param.m_decoder.m_state == J2K_STATE_EOC)
                                p_j2k->m_specific_param.m_decoder.m_state = J2K_STATE_TPHSOT;
                }

        while (OPJ_TRUE) {
                if (! opj_j2k_read_tile_header( p_j2k,
                                        &l_current_tile_no,
                                        &l_data_size,
                                        &l_tile_x0, &l_tile_y0,
                                        &l_tile_x1, &l_tile_y1,
                                        &l_nb_comps,
                                        &l_go_on,
                                        p_stream,
                                        p_manager)) {
                        opj_free(l_current_data);
                        return OPJ_FALSE;
                }

                if (! l_go_on) {
                        break;
                }

                if (l_data_size > l_max_data_size) {
                        OPJ_BYTE *l_new_current_data = (OPJ_BYTE *) opj_realloc(l_current_data, l_data_size);
                        if (! l_new_current_data) {
                                opj_free(l_current_data);
                                l_current_data = NULL;
                                /* TODO: LH: why tile numbering policy used in messages differs from
                                   the one used in opj_j2k_decode_tiles() ? */
                                opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to decode tile %d/%d\n", l_current_tile_no, (p_j2k->m_cp.th * p_j2k->m_cp.tw) - 1);
                                return OPJ_FALSE;
                        }
                        l_current_data = l_new_current_data;
                        l_max_data_size = l_data_size;
                }

                if (! opj_j2k_decode_tile(p_j2k,l_current_tile_no,l_current_data,l_data_size,p_stream,p_manager)) {
                        opj_free(l_current_data);
                        return OPJ_FALSE;
                }
                opj_event_msg(p_manager, EVT_INFO, "Tile %d/%d has been decoded.\n", l_current_tile_no, (p_j2k->m_cp.th * p_j2k->m_cp.tw) - 1);

                if (! opj_j2k_update_image_data(p_j2k->m_tcd,l_current_data, p_j2k->m_output_image)) {
                        opj_free(l_current_data);
                        return OPJ_FALSE;
                }
                opj_event_msg(p_manager, EVT_INFO, "Image data has been updated with tile %d.\n\n", l_current_tile_no);

                if(l_current_tile_no == l_tile_no_to_dec)
                {
                        /* move into the codestream to the the first SOT (FIXME or not move?)*/
                        if (!(opj_stream_read_seek(p_stream, p_j2k->cstr_index->main_head_end + 2, p_manager) ) ) {
                                opj_event_msg(p_manager, EVT_ERROR, "Problem with seek function\n");
                                return OPJ_FALSE;
                        }
                        break;
                }
                else {
                        opj_event_msg(p_manager, EVT_WARNING, "Tile read, decode and updated is not the desired (%d vs %d).\n", l_current_tile_no, l_tile_no_to_dec);
                }

        }

        opj_free(l_current_data);

        return OPJ_TRUE;
}

/**
 * Sets up the procedures to do on decoding one tile. Developpers wanting to extend the library can add their own reading procedures.
 */
static void opj_j2k_setup_decoding_tile (opj_j2k_t *p_j2k)
{
        /* preconditions*/
        assert(p_j2k != 00);

        opj_procedure_list_add_procedure(p_j2k->m_procedure_list,(opj_procedure)opj_j2k_decode_one_tile);
        /* DEVELOPER CORNER, add your custom procedures */

}

OPJ_BOOL opj_j2k_decode(opj_j2k_t * p_j2k,
                                                opj_stream_private_t * p_stream,
                                                opj_image_t * p_image,
                                                opj_event_mgr_t * p_manager)
{
        OPJ_UINT32 compno;

        if (!p_image)
                return OPJ_FALSE;

        p_j2k->m_output_image = opj_image_create0();
        if (! (p_j2k->m_output_image)) {
                return OPJ_FALSE;
        }
        opj_copy_image_header(p_image, p_j2k->m_output_image);

        /* customization of the decoding */
        opj_j2k_setup_decoding(p_j2k);

        /* Decode the codestream */
        if (! opj_j2k_exec (p_j2k,p_j2k->m_procedure_list,p_stream,p_manager)) {
                opj_image_destroy(p_j2k->m_private_image);
                p_j2k->m_private_image = NULL;
                return OPJ_FALSE;
        }

        /* Move data and copy one information from codec to output image*/
        for (compno = 0; compno < p_image->numcomps; compno++) {
                p_image->comps[compno].resno_decoded = p_j2k->m_output_image->comps[compno].resno_decoded;
                p_image->comps[compno].data = p_j2k->m_output_image->comps[compno].data;
#if 0
                char fn[256];
                sprintf( fn, "/tmp/%d.raw", compno );
                FILE *debug = fopen( fn, "wb" );
                fwrite( p_image->comps[compno].data, sizeof(OPJ_INT32), p_image->comps[compno].w * p_image->comps[compno].h, debug );
                fclose( debug );
#endif
                p_j2k->m_output_image->comps[compno].data = NULL;
        }

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_get_tile(      opj_j2k_t *p_j2k,
                                                    opj_stream_private_t *p_stream,
                                                    opj_image_t* p_image,
                                                    opj_event_mgr_t * p_manager,
                                                    OPJ_UINT32 tile_index )
{
        OPJ_UINT32 compno;
        OPJ_UINT32 l_tile_x, l_tile_y;
        opj_image_comp_t* l_img_comp;

        if (!p_image) {
                opj_event_msg(p_manager, EVT_ERROR, "We need an image previously created.\n");
                return OPJ_FALSE;
        }

        if ( /*(tile_index < 0) &&*/ (tile_index >= p_j2k->m_cp.tw * p_j2k->m_cp.th) ){
                opj_event_msg(p_manager, EVT_ERROR, "Tile index provided by the user is incorrect %d (max = %d) \n", tile_index, (p_j2k->m_cp.tw * p_j2k->m_cp.th) - 1);
                return OPJ_FALSE;
        }

        /* Compute the dimension of the desired tile*/
        l_tile_x = tile_index % p_j2k->m_cp.tw;
        l_tile_y = tile_index / p_j2k->m_cp.tw;

        p_image->x0 = l_tile_x * p_j2k->m_cp.tdx + p_j2k->m_cp.tx0;
        if (p_image->x0 < p_j2k->m_private_image->x0)
                p_image->x0 = p_j2k->m_private_image->x0;
        p_image->x1 = (l_tile_x + 1) * p_j2k->m_cp.tdx + p_j2k->m_cp.tx0;
        if (p_image->x1 > p_j2k->m_private_image->x1)
                p_image->x1 = p_j2k->m_private_image->x1;

        p_image->y0 = l_tile_y * p_j2k->m_cp.tdy + p_j2k->m_cp.ty0;
        if (p_image->y0 < p_j2k->m_private_image->y0)
                p_image->y0 = p_j2k->m_private_image->y0;
        p_image->y1 = (l_tile_y + 1) * p_j2k->m_cp.tdy + p_j2k->m_cp.ty0;
        if (p_image->y1 > p_j2k->m_private_image->y1)
                p_image->y1 = p_j2k->m_private_image->y1;

        l_img_comp = p_image->comps;
        for (compno=0; compno < p_image->numcomps; ++compno)
        {
                OPJ_INT32 l_comp_x1, l_comp_y1;

                l_img_comp->factor = p_j2k->m_private_image->comps[compno].factor;

                l_img_comp->x0 = (OPJ_UINT32)opj_int_ceildiv((OPJ_INT32)p_image->x0, (OPJ_INT32)l_img_comp->dx);
                l_img_comp->y0 = (OPJ_UINT32)opj_int_ceildiv((OPJ_INT32)p_image->y0, (OPJ_INT32)l_img_comp->dy);
                l_comp_x1 = opj_int_ceildiv((OPJ_INT32)p_image->x1, (OPJ_INT32)l_img_comp->dx);
                l_comp_y1 = opj_int_ceildiv((OPJ_INT32)p_image->y1, (OPJ_INT32)l_img_comp->dy);

                l_img_comp->w = (OPJ_UINT32)(opj_int_ceildivpow2(l_comp_x1, (OPJ_INT32)l_img_comp->factor) - opj_int_ceildivpow2((OPJ_INT32)l_img_comp->x0, (OPJ_INT32)l_img_comp->factor));
                l_img_comp->h = (OPJ_UINT32)(opj_int_ceildivpow2(l_comp_y1, (OPJ_INT32)l_img_comp->factor) - opj_int_ceildivpow2((OPJ_INT32)l_img_comp->y0, (OPJ_INT32)l_img_comp->factor));

                l_img_comp++;
        }

        /* Destroy the previous output image*/
        if (p_j2k->m_output_image)
                opj_image_destroy(p_j2k->m_output_image);

        /* Create the ouput image from the information previously computed*/
        p_j2k->m_output_image = opj_image_create0();
        if (! (p_j2k->m_output_image)) {
                return OPJ_FALSE;
        }
        opj_copy_image_header(p_image, p_j2k->m_output_image);

        p_j2k->m_specific_param.m_decoder.m_tile_ind_to_dec = (OPJ_INT32)tile_index;

        /* customization of the decoding */
        opj_j2k_setup_decoding_tile(p_j2k);

        /* Decode the codestream */
        if (! opj_j2k_exec (p_j2k,p_j2k->m_procedure_list,p_stream,p_manager)) {
                opj_image_destroy(p_j2k->m_private_image);
                p_j2k->m_private_image = NULL;
                return OPJ_FALSE;
        }

        /* Move data and copy one information from codec to output image*/
        for (compno = 0; compno < p_image->numcomps; compno++) {
                p_image->comps[compno].resno_decoded = p_j2k->m_output_image->comps[compno].resno_decoded;

                if (p_image->comps[compno].data)
                        opj_free(p_image->comps[compno].data);

                p_image->comps[compno].data = p_j2k->m_output_image->comps[compno].data;

                p_j2k->m_output_image->comps[compno].data = NULL;
        }

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_set_decoded_resolution_factor(opj_j2k_t *p_j2k,
                                               OPJ_UINT32 res_factor,
                                               opj_event_mgr_t * p_manager)
{
        OPJ_UINT32 it_comp;

        p_j2k->m_cp.m_specific_param.m_dec.m_reduce = res_factor;

        if (p_j2k->m_private_image) {
                if (p_j2k->m_private_image->comps) {
                        if (p_j2k->m_specific_param.m_decoder.m_default_tcp) {
                                if (p_j2k->m_specific_param.m_decoder.m_default_tcp->tccps) {
                                        for (it_comp = 0 ; it_comp < p_j2k->m_private_image->numcomps; it_comp++) {
                                                OPJ_UINT32 max_res = p_j2k->m_specific_param.m_decoder.m_default_tcp->tccps[it_comp].numresolutions;
                                                if ( res_factor >= max_res){
                                                        opj_event_msg(p_manager, EVT_ERROR, "Resolution factor is greater than the maximum resolution in the component.\n");
                                                        return OPJ_FALSE;
                                                }
                                                p_j2k->m_private_image->comps[it_comp].factor = res_factor;
                                        }
                                        return OPJ_TRUE;
                                }
                        }
                }
        }

        return OPJ_FALSE;
}

OPJ_BOOL opj_j2k_encode(opj_j2k_t * p_j2k,
                        opj_stream_private_t *p_stream,
                        opj_event_mgr_t * p_manager )
{
        OPJ_UINT32 i;
        OPJ_UINT32 l_nb_tiles;
        OPJ_UINT32 l_max_tile_size, l_current_tile_size;
        OPJ_BYTE * l_current_data;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_stream != 00);
        assert(p_manager != 00);

        l_current_data = (OPJ_BYTE*)opj_malloc(1000);
        if (! l_current_data) {
                opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to encode all tiles\n");
                return OPJ_FALSE;
        }
        l_max_tile_size = 1000;

        l_nb_tiles = p_j2k->m_cp.th * p_j2k->m_cp.tw;
        for (i=0;i<l_nb_tiles;++i) {
                if (! opj_j2k_pre_write_tile(p_j2k,i,p_stream,p_manager)) {
                        opj_free(l_current_data);
                        return OPJ_FALSE;
                }

                l_current_tile_size = opj_tcd_get_encoded_tile_size(p_j2k->m_tcd);
                if (l_current_tile_size > l_max_tile_size) {
                        OPJ_BYTE *l_new_current_data = (OPJ_BYTE *) opj_realloc(l_current_data, l_current_tile_size);
                        if (! l_new_current_data) {
                                opj_free(l_current_data);
                                opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to encode all tiles\n");
                                return OPJ_FALSE;
                        }
                        l_current_data = l_new_current_data;
                        l_max_tile_size = l_current_tile_size;
                }

                opj_j2k_get_tile_data(p_j2k->m_tcd,l_current_data);

                if (! opj_j2k_post_write_tile (p_j2k,l_current_data,l_current_tile_size,p_stream,p_manager)) {
                        return OPJ_FALSE;
                }
        }

        opj_free(l_current_data);
        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_end_compress(  opj_j2k_t *p_j2k,
                                                        opj_stream_private_t *p_stream,
                                                        opj_event_mgr_t * p_manager)
{
        /* customization of the encoding */
        opj_j2k_setup_end_compress(p_j2k);

        if (! opj_j2k_exec (p_j2k, p_j2k->m_procedure_list, p_stream, p_manager))
        {
                return OPJ_FALSE;
        }

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_start_compress(opj_j2k_t *p_j2k,
                                                            opj_stream_private_t *p_stream,
                                                            opj_image_t * p_image,
                                                            opj_event_mgr_t * p_manager)
{
        /* preconditions */
        assert(p_j2k != 00);
        assert(p_stream != 00);
        assert(p_manager != 00);

        p_j2k->m_private_image = opj_image_create0();
        opj_copy_image_header(p_image, p_j2k->m_private_image);

        /* TODO_MSD: Find a better way */
        if (p_image->comps) {
                OPJ_UINT32 it_comp;
                for (it_comp = 0 ; it_comp < p_image->numcomps; it_comp++) {
                        if (p_image->comps[it_comp].data) {
                                p_j2k->m_private_image->comps[it_comp].data =p_image->comps[it_comp].data;
                                p_image->comps[it_comp].data = NULL;

                        }
                }
        }

        /* customization of the validation */
        opj_j2k_setup_encoding_validation (p_j2k);

        /* validation of the parameters codec */
        if (! opj_j2k_exec(p_j2k,p_j2k->m_validation_list,p_stream,p_manager)) {
                return OPJ_FALSE;
        }

        /* customization of the encoding */
        opj_j2k_setup_header_writing(p_j2k);

        /* write header */
        if (! opj_j2k_exec (p_j2k,p_j2k->m_procedure_list,p_stream,p_manager)) {
                return OPJ_FALSE;
        }

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_pre_write_tile (       opj_j2k_t * p_j2k,
                                                                OPJ_UINT32 p_tile_index,
                                                                opj_stream_private_t *p_stream,
                                                                opj_event_mgr_t * p_manager )
{
  (void)p_stream;
        if (p_tile_index != p_j2k->m_current_tile_number) {
                opj_event_msg(p_manager, EVT_ERROR, "The given tile index does not match." );
                return OPJ_FALSE;
        }

        opj_event_msg(p_manager, EVT_INFO, "tile number %d / %d\n", p_j2k->m_current_tile_number + 1, p_j2k->m_cp.tw * p_j2k->m_cp.th);

        p_j2k->m_specific_param.m_encoder.m_current_tile_part_number = 0;
        p_j2k->m_tcd->cur_totnum_tp = p_j2k->m_cp.tcps[p_tile_index].m_nb_tile_parts;
        p_j2k->m_specific_param.m_encoder.m_current_poc_tile_part_number = 0;

        /* initialisation before tile encoding  */
        if (! opj_tcd_init_encode_tile(p_j2k->m_tcd, p_j2k->m_current_tile_number)) {
                return OPJ_FALSE;
        }

        return OPJ_TRUE;
}

void opj_j2k_get_tile_data (opj_tcd_t * p_tcd, OPJ_BYTE * p_data)
{
        OPJ_UINT32 i,j,k = 0;
        OPJ_UINT32 l_width,l_height,l_stride, l_offset_x,l_offset_y, l_image_width;
        opj_image_comp_t * l_img_comp = 00;
        opj_tcd_tilecomp_t * l_tilec = 00;
        opj_image_t * l_image = 00;
        OPJ_UINT32 l_size_comp, l_remaining;
        OPJ_INT32 * l_src_ptr;
        l_tilec = p_tcd->tcd_image->tiles->comps;
        l_image = p_tcd->image;
        l_img_comp = l_image->comps;

        for (i=0;i<p_tcd->image->numcomps;++i) {
                l_size_comp = l_img_comp->prec >> 3; /* (/8) */
                l_remaining = l_img_comp->prec & 7;  /* (%8) */
                if (l_remaining) {
                        ++l_size_comp;
                }

                if (l_size_comp == 3) {
                        l_size_comp = 4;
                }

                l_width  = (OPJ_UINT32)(l_tilec->x1 - l_tilec->x0);
                l_height = (OPJ_UINT32)(l_tilec->y1 - l_tilec->y0);
                l_offset_x = (OPJ_UINT32)opj_int_ceildiv((OPJ_INT32)l_image->x0, (OPJ_INT32)l_img_comp->dx);
                l_offset_y = (OPJ_UINT32)opj_int_ceildiv((OPJ_INT32)l_image->y0, (OPJ_INT32)l_img_comp->dy);
                l_image_width = (OPJ_UINT32)opj_int_ceildiv((OPJ_INT32)l_image->x1 - (OPJ_INT32)l_image->x0, (OPJ_INT32)l_img_comp->dx);
                l_stride = l_image_width - l_width;
                l_src_ptr = l_img_comp->data + ((OPJ_UINT32)l_tilec->x0 - l_offset_x) + ((OPJ_UINT32)l_tilec->y0 - l_offset_y) * l_image_width;

                switch (l_size_comp) {
                        case 1:
                                {
                                        OPJ_CHAR * l_dest_ptr = (OPJ_CHAR*) p_data;
                                        if (l_img_comp->sgnd) {
                                                for     (j=0;j<l_height;++j) {
                                                        for (k=0;k<l_width;++k) {
                                                                *(l_dest_ptr) = (OPJ_CHAR) (*l_src_ptr);
                                                                ++l_dest_ptr;
                                                                ++l_src_ptr;
                                                        }
                                                        l_src_ptr += l_stride;
                                                }
                                        }
                                        else {
                                                for (j=0;j<l_height;++j) {
                                                        for (k=0;k<l_width;++k) {
                                                                *(l_dest_ptr) = (OPJ_CHAR)((*l_src_ptr)&0xff);
                                                                ++l_dest_ptr;
                                                                ++l_src_ptr;
                                                        }
                                                        l_src_ptr += l_stride;
                                                }
                                        }

                                        p_data = (OPJ_BYTE*) l_dest_ptr;
                                }
                                break;
                        case 2:
                                {
                                        OPJ_INT16 * l_dest_ptr = (OPJ_INT16 *) p_data;
                                        if (l_img_comp->sgnd) {
                                                for (j=0;j<l_height;++j) {
                                                        for (k=0;k<l_width;++k) {
                                                                *(l_dest_ptr++) = (OPJ_INT16) (*(l_src_ptr++));
                                                        }
                                                        l_src_ptr += l_stride;
                                                }
                                        }
                                        else {
                                                for (j=0;j<l_height;++j) {
                                                        for (k=0;k<l_width;++k) {
                                                                *(l_dest_ptr++) = (OPJ_INT16)((*(l_src_ptr++)) & 0xffff);
                                                        }
                                                        l_src_ptr += l_stride;
                                                }
                                        }

                                        p_data = (OPJ_BYTE*) l_dest_ptr;
                                }
                                break;
                        case 4:
                                {
                                        OPJ_INT32 * l_dest_ptr = (OPJ_INT32 *) p_data;
                                        for (j=0;j<l_height;++j) {
                                                for (k=0;k<l_width;++k) {
                                                        *(l_dest_ptr++) = *(l_src_ptr++);
                                                }
                                                l_src_ptr += l_stride;
                                        }

                                        p_data = (OPJ_BYTE*) l_dest_ptr;
                                }
                                break;
                }

                ++l_img_comp;
                ++l_tilec;
        }
}

OPJ_BOOL opj_j2k_post_write_tile (      opj_j2k_t * p_j2k,
                                                                OPJ_BYTE * p_data,
                                                                OPJ_UINT32 p_data_size,
                                                                opj_stream_private_t *p_stream,
                                                                opj_event_mgr_t * p_manager )
{
        opj_tcd_t * l_tcd = 00;
        OPJ_UINT32 l_nb_bytes_written;
        OPJ_BYTE * l_current_data = 00;
        OPJ_UINT32 l_tile_size = 0;
        OPJ_UINT32 l_available_data;

        /* preconditions */
        assert(p_j2k->m_specific_param.m_encoder.m_encoded_tile_data);

        l_tcd = p_j2k->m_tcd;
        
        l_tile_size = p_j2k->m_specific_param.m_encoder.m_encoded_tile_size;
        l_available_data = l_tile_size;
        l_current_data = p_j2k->m_specific_param.m_encoder.m_encoded_tile_data;

        if (! opj_tcd_copy_tile_data(l_tcd,p_data,p_data_size)) {
                opj_event_msg(p_manager, EVT_ERROR, "Size mismatch between tile data and sent data." );
                return OPJ_FALSE;
        }

        l_nb_bytes_written = 0;
        if (! opj_j2k_write_first_tile_part(p_j2k,l_current_data,&l_nb_bytes_written,l_available_data,p_stream,p_manager)) {
                return OPJ_FALSE;
        }
        l_current_data += l_nb_bytes_written;
        l_available_data -= l_nb_bytes_written;

        l_nb_bytes_written = 0;
        if (! opj_j2k_write_all_tile_parts(p_j2k,l_current_data,&l_nb_bytes_written,l_available_data,p_stream,p_manager)) {
                return OPJ_FALSE;
        }

        l_available_data -= l_nb_bytes_written;
        l_nb_bytes_written = l_tile_size - l_available_data;

        if ( opj_stream_write_data(     p_stream,
                                                                p_j2k->m_specific_param.m_encoder.m_encoded_tile_data,
                                                                l_nb_bytes_written,p_manager) != l_nb_bytes_written) {
                return OPJ_FALSE;
        }

        ++p_j2k->m_current_tile_number;

        return OPJ_TRUE;
}

void opj_j2k_setup_end_compress (opj_j2k_t *p_j2k)
{
        /* preconditions */
        assert(p_j2k != 00);

        /* DEVELOPER CORNER, insert your custom procedures */
        opj_procedure_list_add_procedure(p_j2k->m_procedure_list,(opj_procedure)opj_j2k_write_eoc );

        if (p_j2k->m_cp.m_specific_param.m_enc.m_cinema) {
                opj_procedure_list_add_procedure(p_j2k->m_procedure_list,(opj_procedure)opj_j2k_write_updated_tlm);
        }

        opj_procedure_list_add_procedure(p_j2k->m_procedure_list,(opj_procedure)opj_j2k_write_epc );
        opj_procedure_list_add_procedure(p_j2k->m_procedure_list,(opj_procedure)opj_j2k_end_encoding );
        opj_procedure_list_add_procedure(p_j2k->m_procedure_list,(opj_procedure)opj_j2k_destroy_header_memory);
}

void opj_j2k_setup_encoding_validation (opj_j2k_t *p_j2k)
{
        /* preconditions */
        assert(p_j2k != 00);

        opj_procedure_list_add_procedure(p_j2k->m_validation_list, (opj_procedure)opj_j2k_build_encoder);
        opj_procedure_list_add_procedure(p_j2k->m_validation_list, (opj_procedure)opj_j2k_encoding_validation);

        /* DEVELOPER CORNER, add your custom validation procedure */
        opj_procedure_list_add_procedure(p_j2k->m_validation_list, (opj_procedure)opj_j2k_mct_validation);
}

void opj_j2k_setup_header_writing (opj_j2k_t *p_j2k)
{
        /* preconditions */
        assert(p_j2k != 00);

        opj_procedure_list_add_procedure(p_j2k->m_procedure_list,(opj_procedure)opj_j2k_init_info );
        opj_procedure_list_add_procedure(p_j2k->m_procedure_list,(opj_procedure)opj_j2k_write_soc );
        opj_procedure_list_add_procedure(p_j2k->m_procedure_list,(opj_procedure)opj_j2k_write_siz );
        opj_procedure_list_add_procedure(p_j2k->m_procedure_list,(opj_procedure)opj_j2k_write_cod );
        opj_procedure_list_add_procedure(p_j2k->m_procedure_list,(opj_procedure)opj_j2k_write_qcd );

        if (p_j2k->m_cp.m_specific_param.m_enc.m_cinema) {
                /* No need for COC or QCC, QCD and COD are used
                opj_procedure_list_add_procedure(p_j2k->m_procedure_list,(opj_procedure)opj_j2k_write_all_coc );
                opj_procedure_list_add_procedure(p_j2k->m_procedure_list,(opj_procedure)opj_j2k_write_all_qcc );
                */
                opj_procedure_list_add_procedure(p_j2k->m_procedure_list,(opj_procedure)opj_j2k_write_tlm );

                if (p_j2k->m_cp.m_specific_param.m_enc.m_cinema == OPJ_CINEMA4K_24) {
                        opj_procedure_list_add_procedure(p_j2k->m_procedure_list,(opj_procedure)opj_j2k_write_poc );
                }
        }

        opj_procedure_list_add_procedure(p_j2k->m_procedure_list,(opj_procedure)opj_j2k_write_regions);

        if (p_j2k->m_cp.comment != 00)  {
                opj_procedure_list_add_procedure(p_j2k->m_procedure_list,(opj_procedure)opj_j2k_write_com);
        }

        /* DEVELOPER CORNER, insert your custom procedures */
        if (p_j2k->m_cp.rsiz & OPJ_MCT) {
                opj_procedure_list_add_procedure(p_j2k->m_procedure_list,(opj_procedure)opj_j2k_write_mct_data_group );
        }
        /* End of Developer Corner */

        if (p_j2k->cstr_index) {
                opj_procedure_list_add_procedure(p_j2k->m_procedure_list,(opj_procedure)opj_j2k_get_end_header );
        }

        opj_procedure_list_add_procedure(p_j2k->m_procedure_list,(opj_procedure)opj_j2k_create_tcd);
        opj_procedure_list_add_procedure(p_j2k->m_procedure_list,(opj_procedure)opj_j2k_update_rates);
}

OPJ_BOOL opj_j2k_write_first_tile_part (opj_j2k_t *p_j2k,
                                                                        OPJ_BYTE * p_data,
                                                                        OPJ_UINT32 * p_data_written,
                                                                        OPJ_UINT32 p_total_data_size,
                                                                        opj_stream_private_t *p_stream,
                                                                        struct opj_event_mgr * p_manager )
{
        OPJ_UINT32 l_nb_bytes_written = 0;
        OPJ_UINT32 l_current_nb_bytes_written;
        OPJ_BYTE * l_begin_data = 00;

        opj_tcd_t * l_tcd = 00;
        opj_cp_t * l_cp = 00;

        l_tcd = p_j2k->m_tcd;
        l_cp = &(p_j2k->m_cp);

        l_tcd->cur_pino = 0;

        /*Get number of tile parts*/
        p_j2k->m_specific_param.m_encoder.m_current_poc_tile_part_number = 0;

        /* INDEX >> */
        /* << INDEX */

        l_current_nb_bytes_written = 0;
        l_begin_data = p_data;
        if (! opj_j2k_write_sot(p_j2k,p_data,&l_current_nb_bytes_written,p_stream,p_manager))
        {
                return OPJ_FALSE;
        }

        l_nb_bytes_written += l_current_nb_bytes_written;
        p_data += l_current_nb_bytes_written;
        p_total_data_size -= l_current_nb_bytes_written;

        if (l_cp->m_specific_param.m_enc.m_cinema == 0) {
#if 0
                for (compno = 1; compno < p_j2k->m_private_image->numcomps; compno++) {
                        l_current_nb_bytes_written = 0;
                        opj_j2k_write_coc_in_memory(p_j2k,compno,p_data,&l_current_nb_bytes_written,p_manager);
                        l_nb_bytes_written += l_current_nb_bytes_written;
                        p_data += l_current_nb_bytes_written;
                        p_total_data_size -= l_current_nb_bytes_written;

                        l_current_nb_bytes_written = 0;
                        opj_j2k_write_qcc_in_memory(p_j2k,compno,p_data,&l_current_nb_bytes_written,p_manager);
                        l_nb_bytes_written += l_current_nb_bytes_written;
                        p_data += l_current_nb_bytes_written;
                        p_total_data_size -= l_current_nb_bytes_written;
                }
#endif

                if (l_cp->tcps[p_j2k->m_current_tile_number].numpocs) {
                        l_current_nb_bytes_written = 0;
                        opj_j2k_write_poc_in_memory(p_j2k,p_data,&l_current_nb_bytes_written,p_manager);
                        l_nb_bytes_written += l_current_nb_bytes_written;
                        p_data += l_current_nb_bytes_written;
                        p_total_data_size -= l_current_nb_bytes_written;
                }
        }

        l_current_nb_bytes_written = 0;
        if (! opj_j2k_write_sod(p_j2k,l_tcd,p_data,&l_current_nb_bytes_written,p_total_data_size,p_stream,p_manager)) {
                return OPJ_FALSE;
        }

        l_nb_bytes_written += l_current_nb_bytes_written;
        * p_data_written = l_nb_bytes_written;

        /* Writing Psot in SOT marker */
        opj_write_bytes(l_begin_data + 6,l_nb_bytes_written,4);                                 /* PSOT */

        if (l_cp->m_specific_param.m_enc.m_cinema){
                opj_j2k_update_tlm(p_j2k,l_nb_bytes_written);
        }

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_write_all_tile_parts(  opj_j2k_t *p_j2k,
                                                                        OPJ_BYTE * p_data,
                                                                        OPJ_UINT32 * p_data_written,
                                                                        OPJ_UINT32 p_total_data_size,
                                                                        opj_stream_private_t *p_stream,
                                                                        struct opj_event_mgr * p_manager
                                                                )
{
        OPJ_UINT32 tilepartno=0;
        OPJ_UINT32 l_nb_bytes_written = 0;
        OPJ_UINT32 l_current_nb_bytes_written;
        OPJ_UINT32 l_part_tile_size;
        OPJ_UINT32 tot_num_tp;
        OPJ_UINT32 pino;

        OPJ_BYTE * l_begin_data;
        opj_tcp_t *l_tcp = 00;
        opj_tcd_t * l_tcd = 00;
        opj_cp_t * l_cp = 00;

        l_tcd = p_j2k->m_tcd;
        l_cp = &(p_j2k->m_cp);
        l_tcp = l_cp->tcps + p_j2k->m_current_tile_number;

        /*Get number of tile parts*/
        tot_num_tp = opj_j2k_get_num_tp(l_cp,0,p_j2k->m_current_tile_number);

        /* start writing remaining tile parts */
        ++p_j2k->m_specific_param.m_encoder.m_current_tile_part_number;
        for (tilepartno = 1; tilepartno < tot_num_tp ; ++tilepartno) {
                p_j2k->m_specific_param.m_encoder.m_current_poc_tile_part_number = tilepartno;
                l_current_nb_bytes_written = 0;
                l_part_tile_size = 0;
                l_begin_data = p_data;

                if (! opj_j2k_write_sot(p_j2k,p_data,&l_current_nb_bytes_written,p_stream,p_manager)) {
                        return OPJ_FALSE;
                }

                l_nb_bytes_written += l_current_nb_bytes_written;
                p_data += l_current_nb_bytes_written;
                p_total_data_size -= l_current_nb_bytes_written;
                l_part_tile_size += l_current_nb_bytes_written;

                l_current_nb_bytes_written = 0;
                if (! opj_j2k_write_sod(p_j2k,l_tcd,p_data,&l_current_nb_bytes_written,p_total_data_size,p_stream,p_manager)) {
                        return OPJ_FALSE;
                }

                p_data += l_current_nb_bytes_written;
                l_nb_bytes_written += l_current_nb_bytes_written;
                p_total_data_size -= l_current_nb_bytes_written;
                l_part_tile_size += l_current_nb_bytes_written;

                /* Writing Psot in SOT marker */
                opj_write_bytes(l_begin_data + 6,l_part_tile_size,4);                                   /* PSOT */

                if (l_cp->m_specific_param.m_enc.m_cinema) {
                        opj_j2k_update_tlm(p_j2k,l_part_tile_size);
                }

                ++p_j2k->m_specific_param.m_encoder.m_current_tile_part_number;
        }

        for (pino = 1; pino <= l_tcp->numpocs; ++pino) {
                l_tcd->cur_pino = pino;

                /*Get number of tile parts*/
                tot_num_tp = opj_j2k_get_num_tp(l_cp,pino,p_j2k->m_current_tile_number);
                for (tilepartno = 0; tilepartno < tot_num_tp ; ++tilepartno) {
                        p_j2k->m_specific_param.m_encoder.m_current_poc_tile_part_number = tilepartno;
                        l_current_nb_bytes_written = 0;
                        l_part_tile_size = 0;
                        l_begin_data = p_data;

                        if (! opj_j2k_write_sot(p_j2k,p_data,&l_current_nb_bytes_written,p_stream,p_manager)) {
                                return OPJ_FALSE;
                        }

                        l_nb_bytes_written += l_current_nb_bytes_written;
                        p_data += l_current_nb_bytes_written;
                        p_total_data_size -= l_current_nb_bytes_written;
                        l_part_tile_size += l_current_nb_bytes_written;

                        l_current_nb_bytes_written = 0;

                        if (! opj_j2k_write_sod(p_j2k,l_tcd,p_data,&l_current_nb_bytes_written,p_total_data_size,p_stream,p_manager)) {
                                return OPJ_FALSE;
                        }

                        l_nb_bytes_written += l_current_nb_bytes_written;
                        p_data += l_current_nb_bytes_written;
                        p_total_data_size -= l_current_nb_bytes_written;
                        l_part_tile_size += l_current_nb_bytes_written;

                        /* Writing Psot in SOT marker */
                        opj_write_bytes(l_begin_data + 6,l_part_tile_size,4);                                   /* PSOT */

                        if (l_cp->m_specific_param.m_enc.m_cinema) {
                                opj_j2k_update_tlm(p_j2k,l_part_tile_size);
                        }

                        ++p_j2k->m_specific_param.m_encoder.m_current_tile_part_number;
                }
        }

        *p_data_written = l_nb_bytes_written;

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_write_updated_tlm( opj_j2k_t *p_j2k,
                                                                    struct opj_stream_private *p_stream,
                                                                    struct opj_event_mgr * p_manager )
{
        OPJ_UINT32 l_tlm_size;
        OPJ_OFF_T l_tlm_position, l_current_position;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_manager != 00);
        assert(p_stream != 00);

        l_tlm_size = 5 * p_j2k->m_specific_param.m_encoder.m_total_tile_parts;
        l_tlm_position = 6 + p_j2k->m_specific_param.m_encoder.m_tlm_start;
        l_current_position = opj_stream_tell(p_stream);

        if (! opj_stream_seek(p_stream,l_tlm_position,p_manager)) {
                return OPJ_FALSE;
        }

        if (opj_stream_write_data(p_stream,p_j2k->m_specific_param.m_encoder.m_tlm_sot_offsets_buffer,l_tlm_size,p_manager) != l_tlm_size) {
                return OPJ_FALSE;
        }

        if (! opj_stream_seek(p_stream,l_current_position,p_manager)) {
                return OPJ_FALSE;
        }

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_end_encoding(  opj_j2k_t *p_j2k,
                                                        struct opj_stream_private *p_stream,
                                                        struct opj_event_mgr * p_manager )
{
        /* preconditions */
        assert(p_j2k != 00);
        assert(p_manager != 00);
        assert(p_stream != 00);

        opj_tcd_destroy(p_j2k->m_tcd);
        p_j2k->m_tcd = 00;

        if (p_j2k->m_specific_param.m_encoder.m_tlm_sot_offsets_buffer) {
                opj_free(p_j2k->m_specific_param.m_encoder.m_tlm_sot_offsets_buffer);
                p_j2k->m_specific_param.m_encoder.m_tlm_sot_offsets_buffer = 0;
                p_j2k->m_specific_param.m_encoder.m_tlm_sot_offsets_current = 0;
        }

        if (p_j2k->m_specific_param.m_encoder.m_encoded_tile_data) {
                opj_free(p_j2k->m_specific_param.m_encoder.m_encoded_tile_data);
                p_j2k->m_specific_param.m_encoder.m_encoded_tile_data = 0;
        }

        p_j2k->m_specific_param.m_encoder.m_encoded_tile_size = 0;

        return OPJ_TRUE;
}

/**
 * Destroys the memory associated with the decoding of headers.
 */
static OPJ_BOOL opj_j2k_destroy_header_memory ( opj_j2k_t * p_j2k,
                                                opj_stream_private_t *p_stream,
                                                opj_event_mgr_t * p_manager
                                                )
{
        /* preconditions */
        assert(p_j2k != 00);
        assert(p_stream != 00);
        assert(p_manager != 00);

        if (p_j2k->m_specific_param.m_encoder.m_header_tile_data) {
                opj_free(p_j2k->m_specific_param.m_encoder.m_header_tile_data);
                p_j2k->m_specific_param.m_encoder.m_header_tile_data = 0;
        }

        p_j2k->m_specific_param.m_encoder.m_header_tile_data_size = 0;

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_init_info(     opj_j2k_t *p_j2k,
                                                struct opj_stream_private *p_stream,
                                                struct opj_event_mgr * p_manager )
{
        opj_codestream_info_t * l_cstr_info = 00;

        /* preconditions */
        assert(p_j2k != 00);
        assert(p_manager != 00);
        assert(p_stream != 00);
  (void)l_cstr_info;

        /* TODO mergeV2: check this part which use cstr_info */
        /*l_cstr_info = p_j2k->cstr_info;

        if (l_cstr_info)  {
                OPJ_UINT32 compno;
                l_cstr_info->tile = (opj_tile_info_t *) opj_malloc(p_j2k->m_cp.tw * p_j2k->m_cp.th * sizeof(opj_tile_info_t));

                l_cstr_info->image_w = p_j2k->m_image->x1 - p_j2k->m_image->x0;
                l_cstr_info->image_h = p_j2k->m_image->y1 - p_j2k->m_image->y0;

                l_cstr_info->prog = (&p_j2k->m_cp.tcps[0])->prg;

                l_cstr_info->tw = p_j2k->m_cp.tw;
                l_cstr_info->th = p_j2k->m_cp.th;

                l_cstr_info->tile_x = p_j2k->m_cp.tdx;*/        /* new version parser */
                /*l_cstr_info->tile_y = p_j2k->m_cp.tdy;*/      /* new version parser */
                /*l_cstr_info->tile_Ox = p_j2k->m_cp.tx0;*/     /* new version parser */
                /*l_cstr_info->tile_Oy = p_j2k->m_cp.ty0;*/     /* new version parser */

                /*l_cstr_info->numcomps = p_j2k->m_image->numcomps;

                l_cstr_info->numlayers = (&p_j2k->m_cp.tcps[0])->numlayers;

                l_cstr_info->numdecompos = (OPJ_INT32*) opj_malloc(p_j2k->m_image->numcomps * sizeof(OPJ_INT32));

                for (compno=0; compno < p_j2k->m_image->numcomps; compno++) {
                        l_cstr_info->numdecompos[compno] = (&p_j2k->m_cp.tcps[0])->tccps->numresolutions - 1;
                }

                l_cstr_info->D_max = 0.0;       */      /* ADD Marcela */

                /*l_cstr_info->main_head_start = opj_stream_tell(p_stream);*/ /* position of SOC */

                /*l_cstr_info->maxmarknum = 100;
                l_cstr_info->marker = (opj_marker_info_t *) opj_malloc(l_cstr_info->maxmarknum * sizeof(opj_marker_info_t));
                l_cstr_info->marknum = 0;
        }*/

        return opj_j2k_calculate_tp(p_j2k,&(p_j2k->m_cp),&p_j2k->m_specific_param.m_encoder.m_total_tile_parts,p_j2k->m_private_image,p_manager);
}

/**
 * Creates a tile-coder decoder.
 *
 * @param       p_stream                the stream to write data to.
 * @param       p_j2k                   J2K codec.
 * @param       p_manager               the user event manager.
*/
static OPJ_BOOL opj_j2k_create_tcd(     opj_j2k_t *p_j2k,
                                                                    opj_stream_private_t *p_stream,
                                                                    opj_event_mgr_t * p_manager
                                    )
{
        /* preconditions */
        assert(p_j2k != 00);
        assert(p_manager != 00);
        assert(p_stream != 00);

        p_j2k->m_tcd = opj_tcd_create(OPJ_FALSE);

        if (! p_j2k->m_tcd) {
                opj_event_msg(p_manager, EVT_ERROR, "Not enough memory to create Tile Coder\n");
                return OPJ_FALSE;
        }

        if (!opj_tcd_init(p_j2k->m_tcd,p_j2k->m_private_image,&p_j2k->m_cp)) {
                opj_tcd_destroy(p_j2k->m_tcd);
                p_j2k->m_tcd = 00;
                return OPJ_FALSE;
        }

        return OPJ_TRUE;
}

OPJ_BOOL opj_j2k_write_tile (opj_j2k_t * p_j2k,
                                                 OPJ_UINT32 p_tile_index,
                                                 OPJ_BYTE * p_data,
                                                 OPJ_UINT32 p_data_size,
                                                 opj_stream_private_t *p_stream,
                                                 opj_event_mgr_t * p_manager )
{
        if (! opj_j2k_pre_write_tile(p_j2k,p_tile_index,p_stream,p_manager)) {
                opj_event_msg(p_manager, EVT_ERROR, "Error while opj_j2k_pre_write_tile with tile index = %d\n", p_tile_index);
                return OPJ_FALSE;
        }
        else {
                if (! opj_j2k_post_write_tile(p_j2k,p_data,p_data_size,p_stream,p_manager)) {
                        opj_event_msg(p_manager, EVT_ERROR, "Error while opj_j2k_post_write_tile with tile index = %d\n", p_tile_index);
                        return OPJ_FALSE;
                }
        }

        return OPJ_TRUE;
}
