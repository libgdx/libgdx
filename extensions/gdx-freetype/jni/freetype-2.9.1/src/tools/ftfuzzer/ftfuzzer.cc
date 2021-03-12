// ftfuzzer.cc
//
//   A fuzzing function to test FreeType with libFuzzer.
//
// Copyright 2015-2018 by
// David Turner, Robert Wilhelm, and Werner Lemberg.
//
// This file is part of the FreeType project, and may only be used,
// modified, and distributed under the terms of the FreeType project
// license, LICENSE.TXT.  By continuing to use, modify, or distribute
// this file you indicate that you have read the license and
// understand and accept it fully.


// we use `unique_ptr', `decltype', and other gimmicks defined since C++11
#if __cplusplus < 201103L
#  error "a C++11 compiler is needed"
#endif

#include <archive.h>
#include <archive_entry.h>

#include <assert.h>
#include <stdint.h>

#include <memory>
#include <vector>


  using namespace std;


#include <ft2build.h>

#include FT_FREETYPE_H
#include FT_GLYPH_H
#include FT_CACHE_H
#include FT_CACHE_CHARMAP_H
#include FT_CACHE_IMAGE_H
#include FT_CACHE_SMALL_BITMAPS_H
#include FT_SYNTHESIS_H
#include FT_ADVANCES_H
#include FT_OUTLINE_H
#include FT_BBOX_H
#include FT_MODULE_H
#include FT_DRIVER_H
#include FT_MULTIPLE_MASTERS_H


  static FT_Library  library;
  static int         InitResult;


  struct FT_Global
  {
    FT_Global()
    {
      InitResult = FT_Init_FreeType( &library );
      if ( InitResult )
        return;

      // try to activate Adobe's CFF engine; it might not be the default
      unsigned int  cff_hinting_engine = FT_HINTING_ADOBE;
      FT_Property_Set( library,
                       "cff",
                       "hinting-engine", &cff_hinting_engine );
    }

    ~FT_Global()
    {
      FT_Done_FreeType( library );
    }
  };

  FT_Global  global_ft;


  // We want to select n values at random (without repetition),
  // with 0 < n <= N.  The algorithm is taken from TAoCP, Vol. 2
  // (Algorithm S, selection sampling technique)
  struct Random
  {
    int  n;
    int  N;

    int  t; // total number of values so far
    int  m; // number of selected values so far

    uint32_t  r; // the current pseudo-random number

    Random( int n_,
            int N_ )
    : n( n_ ),
      N( N_ )
    {
      t = 0;
      m = 0;

      // Ideally, this should depend on the input file,
      // for example, taking the sha256 as input;
      // however, this is overkill for fuzzying tests.
      r = 12345;
    }

    int get()
    {
      if ( m >= n )
        return -1;

    Redo:
      // We can't use `rand': different C libraries might provide
      // different implementations of this function.  As a replacement,
      // we use a 32bit version of the `xorshift' algorithm.
      r ^= r << 13;
      r ^= r >> 17;
      r ^= r << 5;

      double  U = double( r ) / UINT32_MAX;

      if ( ( N - t ) * U >= ( n - m ) )
      {
        t++;
        goto Redo;
      }

      t++;
      m++;

      return t;
    }
  };


  static int
  archive_read_entry_data( struct archive   *ar,
                           vector<FT_Byte>  *vw )
  {
    int             r;
    const FT_Byte*  buff;
    size_t          size;
    int64_t         offset;

    for (;;)
    {
      r = archive_read_data_block( ar,
                                   reinterpret_cast<const void**>( &buff ),
                                   &size,
                                   &offset );
      if ( r == ARCHIVE_EOF )
        return ARCHIVE_OK;
      if ( r != ARCHIVE_OK )
        return r;

      vw->insert( vw->end(), buff, buff + size );
    }
  }


  static vector<vector<FT_Byte>>
  parse_data( const uint8_t*  data,
              size_t          size )
  {
    struct archive_entry*    entry;
    int                      r;
    vector<vector<FT_Byte>>  files;

    unique_ptr<struct  archive,
               decltype ( archive_read_free )*>  a( archive_read_new(),
                                                    archive_read_free );

    // activate reading of uncompressed tar archives
    archive_read_support_format_tar( a.get() );

    // the need for `const_cast' was removed with libarchive commit be4d4dd
    if ( !( r = archive_read_open_memory(
                  a.get(),
                  const_cast<void*>(static_cast<const void*>( data ) ),
                  size ) ) )
    {
      unique_ptr<struct  archive,
                 decltype ( archive_read_close )*>  a_open( a.get(),
                                                            archive_read_close );

      // read files contained in archive
      for (;;)
      {
        r = archive_read_next_header( a_open.get(), &entry );
        if ( r == ARCHIVE_EOF )
          break;
        if ( r != ARCHIVE_OK )
          break;

        vector<FT_Byte>  entry_data;
        r = archive_read_entry_data( a.get(), &entry_data );
        if ( r != ARCHIVE_OK )
          break;

        files.push_back( move( entry_data ) );
      }
    }

    if ( files.size() == 0 )
      files.emplace_back( data, data + size );

    return files;
  }


  static void
  setIntermediateAxis( FT_Face  face )
  {
    // only handle Multiple Masters and GX variation fonts
    if ( !FT_HAS_MULTIPLE_MASTERS( face ) )
      return;

    // get variation data for current instance
    FT_MM_Var*  variations_ptr = nullptr;
    if ( FT_Get_MM_Var( face, &variations_ptr ) )
      return;

    unique_ptr<FT_MM_Var,
               decltype ( free )*>  variations( variations_ptr, free );
    vector<FT_Fixed>                coords( variations->num_axis );

    // select an arbitrary instance
    for ( unsigned int  i = 0; i < variations->num_axis; i++ )
      coords[i] = ( variations->axis[i].minimum +
                    variations->axis[i].def     ) / 2;

    if ( FT_Set_Var_Design_Coordinates( face,
                                        FT_UInt( coords.size() ),
                                        coords.data() ) )
      return;
  }


  // the interface function to the libFuzzer library
  extern "C" int
  LLVMFuzzerTestOneInput( const uint8_t*  data,
                          size_t          size_ )
  {
    assert( !InitResult );

    if ( size_ < 1 )
      return 0;

    const vector<vector<FT_Byte>>&  files = parse_data( data, size_ );

    FT_Face         face;
    FT_Int32        load_flags  = FT_LOAD_DEFAULT;
#if 0
    FT_Render_Mode  render_mode = FT_RENDER_MODE_NORMAL;
#endif

    // We use a conservative approach here, at the cost of calling
    // `FT_New_Face' quite often.  The idea is that the fuzzer should be
    // able to try all faces and named instances of a font, expecting that
    // some faces don't work for various reasons, e.g., a broken subfont, or
    // an unsupported NFNT bitmap font in a Mac dfont resource that holds
    // more than a single font.

    // get number of faces
    if ( FT_New_Memory_Face( library,
                             files[0].data(),
                             (FT_Long)files[0].size(),
                             -1,
                             &face ) )
      return 0;
    long  num_faces = face->num_faces;
    FT_Done_Face( face );

    // loop over up to 20 arbitrarily selected faces
    // from index range [0;num-faces-1]
    long  max_face_cnt = num_faces < 20
                           ? num_faces
                           : 20;

    Random  faces_pool( (int)max_face_cnt, (int)num_faces );

    for ( long  face_cnt = 0;
          face_cnt < max_face_cnt;
          face_cnt++ )
    {
      long  face_index = faces_pool.get() - 1;

      // get number of instances
      if ( FT_New_Memory_Face( library,
                               files[0].data(),
                               (FT_Long)files[0].size(),
                               -( face_index + 1 ),
                               &face ) )
        continue;
      long  num_instances = face->style_flags >> 16;
      FT_Done_Face( face );

      // loop over the face without instance (index 0)
      // and up to 20 arbitrarily selected instances
      // from index range [1;num_instances]
      long  max_instance_cnt = num_instances < 20
                                 ? num_instances
                                 : 20;

      Random  instances_pool( (int)max_instance_cnt, (int)num_instances );

      for ( long  instance_cnt = 0;
            instance_cnt <= max_instance_cnt;
            instance_cnt++ )
      {
        long  instance_index = 0;

        if ( !instance_cnt )
        {
          if ( FT_New_Memory_Face( library,
                                   files[0].data(),
                                   (FT_Long)files[0].size(),
                                   face_index,
                                   &face ) )
            continue;
        }
        else
        {
          instance_index = instances_pool.get();

          if ( FT_New_Memory_Face( library,
                                   files[0].data(),
                                   (FT_Long)files[0].size(),
                                   ( instance_index << 16 ) + face_index,
                                   &face ) )
            continue;
        }

        // if we have more than a single input file coming from an archive,
        // attach them (starting with the second file) using the order given
        // in the archive
        for ( size_t  files_index = 1;
              files_index < files.size();
              files_index++ )
        {
          FT_Open_Args  open_args = {};
          open_args.flags         = FT_OPEN_MEMORY;
          open_args.memory_base   = files[files_index].data();
          open_args.memory_size   = (FT_Long)files[files_index].size();

          // the last archive element will be eventually used as the
          // attachment
          FT_Attach_Stream( face, &open_args );
        }

        // loop over an arbitrary size for outlines
        // and up to ten arbitrarily selected bitmap strike sizes
        // from the range [0;num_fixed_sizes - 1]
        int  max_size_cnt = face->num_fixed_sizes < 10
                              ? face->num_fixed_sizes
                              : 10;

        Random sizes_pool( max_size_cnt, face->num_fixed_sizes );

        for ( int  size_cnt = 0;
              size_cnt <= max_size_cnt;
              size_cnt++ )
        {
          FT_Int32  flags = load_flags;

          int  size_index = 0;

          if ( !size_cnt )
          {
            // set up 20pt at 72dpi as an arbitrary size
            if ( FT_Set_Char_Size( face, 20 * 64, 20 * 64, 72, 72 ) )
              continue;
            flags |= FT_LOAD_NO_BITMAP;
          }
          else
          {
            // bitmap strikes are not active for font variations
            if ( instance_index )
              continue;

            size_index = sizes_pool.get() - 1;

            if ( FT_Select_Size( face, size_index ) )
              continue;
            flags |= FT_LOAD_COLOR;
          }

          // test MM interface only for a face without a selected instance
          // and without a selected bitmap strike
          if ( !instance_index && !size_cnt )
            setIntermediateAxis( face );

          // loop over all glyphs
          for ( unsigned int  glyph_index = 0;
                glyph_index < (unsigned int)face->num_glyphs;
                glyph_index++ )
          {
            if ( FT_Load_Glyph( face, glyph_index, flags ) )
              continue;

            // Rendering is the most expensive and the least interesting part.
            //
            // if ( FT_Render_Glyph( face->glyph, render_mode) )
            //   continue;
            // FT_GlyphSlot_Embolden( face->glyph );

#if 0
            FT_Glyph  glyph;
            if ( !FT_Get_Glyph( face->glyph, &glyph ) )
              FT_Done_Glyph( glyph );

            FT_Outline*  outline = &face->glyph->outline;
            FT_Matrix    rot30   = { 0xDDB4, -0x8000, 0x8000, 0xDDB4 };

            FT_Outline_Transform( outline, &rot30 );

            FT_BBox  bbox;
            FT_Outline_Get_BBox( outline, &bbox );
#endif
          }
        }
        FT_Done_Face( face );
      }
    }

    return 0;
  }


// END
