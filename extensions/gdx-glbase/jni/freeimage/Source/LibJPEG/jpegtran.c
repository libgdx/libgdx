/*
 * jpegtran.c
 *
 * Copyright (C) 1995-2013, Thomas G. Lane, Guido Vollbeding.
 * This file is part of the Independent JPEG Group's software.
 * For conditions of distribution and use, see the accompanying README file.
 *
 * This file contains a command-line user interface for JPEG transcoding.
 * It is very similar to cjpeg.c, and partly to djpeg.c, but provides
 * lossless transcoding between different JPEG file formats.  It also
 * provides some lossless and sort-of-lossless transformations of JPEG data.
 */

#include "cdjpeg.h"		/* Common decls for cjpeg/djpeg applications */
#include "transupp.h"		/* Support routines for jpegtran */
#include "jversion.h"		/* for version message */

#ifdef USE_CCOMMAND		/* command-line reader for Macintosh */
#ifdef __MWERKS__
#include <SIOUX.h>              /* Metrowerks needs this */
#include <console.h>		/* ... and this */
#endif
#ifdef THINK_C
#include <console.h>		/* Think declares it here */
#endif
#endif


/*
 * Argument-parsing code.
 * The switch parser is designed to be useful with DOS-style command line
 * syntax, ie, intermixed switches and file names, where only the switches
 * to the left of a given file name affect processing of that file.
 * The main program in this file doesn't actually use this capability...
 */


static const char * progname;	/* program name for error messages */
static char * outfilename;	/* for -outfile switch */
static char * scaleoption;	/* -scale switch */
static JCOPY_OPTION copyoption;	/* -copy switch */
static jpeg_transform_info transformoption; /* image transformation options */


LOCAL(void)
usage (void)
/* complain about bad command line */
{
  fprintf(stderr, "usage: %s [switches] ", progname);
#ifdef TWO_FILE_COMMANDLINE
  fprintf(stderr, "inputfile outputfile\n");
#else
  fprintf(stderr, "[inputfile]\n");
#endif

  fprintf(stderr, "Switches (names may be abbreviated):\n");
  fprintf(stderr, "  -copy none     Copy no extra markers from source file\n");
  fprintf(stderr, "  -copy comments Copy only comment markers (default)\n");
  fprintf(stderr, "  -copy all      Copy all extra markers\n");
#ifdef ENTROPY_OPT_SUPPORTED
  fprintf(stderr, "  -optimize      Optimize Huffman table (smaller file, but slow compression)\n");
#endif
#ifdef C_PROGRESSIVE_SUPPORTED
  fprintf(stderr, "  -progressive   Create progressive JPEG file\n");
#endif
  fprintf(stderr, "Switches for modifying the image:\n");
#if TRANSFORMS_SUPPORTED
  fprintf(stderr, "  -crop WxH+X+Y  Crop to a rectangular subarea\n");
  fprintf(stderr, "  -flip [horizontal|vertical]  Mirror image (left-right or top-bottom)\n");
  fprintf(stderr, "  -grayscale     Reduce to grayscale (omit color data)\n");
  fprintf(stderr, "  -perfect       Fail if there is non-transformable edge blocks\n");
  fprintf(stderr, "  -rotate [90|180|270]         Rotate image (degrees clockwise)\n");
#endif
  fprintf(stderr, "  -scale M/N     Scale output image by fraction M/N, eg, 1/8\n");
#if TRANSFORMS_SUPPORTED
  fprintf(stderr, "  -transpose     Transpose image\n");
  fprintf(stderr, "  -transverse    Transverse transpose image\n");
  fprintf(stderr, "  -trim          Drop non-transformable edge blocks\n");
  fprintf(stderr, "  -wipe WxH+X+Y  Wipe (gray out) a rectangular subarea\n");
#endif
  fprintf(stderr, "Switches for advanced users:\n");
#ifdef C_ARITH_CODING_SUPPORTED
  fprintf(stderr, "  -arithmetic    Use arithmetic coding\n");
#endif
  fprintf(stderr, "  -restart N     Set restart interval in rows, or in blocks with B\n");
  fprintf(stderr, "  -maxmemory N   Maximum memory to use (in kbytes)\n");
  fprintf(stderr, "  -outfile name  Specify name for output file\n");
  fprintf(stderr, "  -verbose  or  -debug   Emit debug output\n");
  fprintf(stderr, "Switches for wizards:\n");
#ifdef C_MULTISCAN_FILES_SUPPORTED
  fprintf(stderr, "  -scans file    Create multi-scan JPEG per script file\n");
#endif
  exit(EXIT_FAILURE);
}


LOCAL(void)
select_transform (JXFORM_CODE transform)
/* Silly little routine to detect multiple transform options,
 * which we can't handle.
 */
{
#if TRANSFORMS_SUPPORTED
  if (transformoption.transform == JXFORM_NONE ||
      transformoption.transform == transform) {
    transformoption.transform = transform;
  } else {
    fprintf(stderr, "%s: can only do one image transformation at a time\n",
	    progname);
    usage();
  }
#else
  fprintf(stderr, "%s: sorry, image transformation was not compiled\n",
	  progname);
  exit(EXIT_FAILURE);
#endif
}


LOCAL(int)
parse_switches (j_compress_ptr cinfo, int argc, char **argv,
		int last_file_arg_seen, boolean for_real)
/* Parse optional switches.
 * Returns argv[] index of first file-name argument (== argc if none).
 * Any file names with indexes <= last_file_arg_seen are ignored;
 * they have presumably been processed in a previous iteration.
 * (Pass 0 for last_file_arg_seen on the first or only iteration.)
 * for_real is FALSE on the first (dummy) pass; we may skip any expensive
 * processing.
 */
{
  int argn;
  char * arg;
  boolean simple_progressive;
  char * scansarg = NULL;	/* saves -scans parm if any */

  /* Set up default JPEG parameters. */
  simple_progressive = FALSE;
  outfilename = NULL;
  scaleoption = NULL;
  copyoption = JCOPYOPT_DEFAULT;
  transformoption.transform = JXFORM_NONE;
  transformoption.perfect = FALSE;
  transformoption.trim = FALSE;
  transformoption.force_grayscale = FALSE;
  transformoption.crop = FALSE;
  cinfo->err->trace_level = 0;

  /* Scan command line options, adjust parameters */

  for (argn = 1; argn < argc; argn++) {
    arg = argv[argn];
    if (*arg != '-') {
      /* Not a switch, must be a file name argument */
      if (argn <= last_file_arg_seen) {
	outfilename = NULL;	/* -outfile applies to just one input file */
	continue;		/* ignore this name if previously processed */
      }
      break;			/* else done parsing switches */
    }
    arg++;			/* advance past switch marker character */

    if (keymatch(arg, "arithmetic", 1)) {
      /* Use arithmetic coding. */
#ifdef C_ARITH_CODING_SUPPORTED
      cinfo->arith_code = TRUE;
#else
      fprintf(stderr, "%s: sorry, arithmetic coding not supported\n",
	      progname);
      exit(EXIT_FAILURE);
#endif

    } else if (keymatch(arg, "copy", 2)) {
      /* Select which extra markers to copy. */
      if (++argn >= argc)	/* advance to next argument */
	usage();
      if (keymatch(argv[argn], "none", 1)) {
	copyoption = JCOPYOPT_NONE;
      } else if (keymatch(argv[argn], "comments", 1)) {
	copyoption = JCOPYOPT_COMMENTS;
      } else if (keymatch(argv[argn], "all", 1)) {
	copyoption = JCOPYOPT_ALL;
      } else
	usage();

    } else if (keymatch(arg, "crop", 2)) {
      /* Perform lossless cropping. */
#if TRANSFORMS_SUPPORTED
      if (++argn >= argc)	/* advance to next argument */
	usage();
      if (transformoption.crop /* reject multiple crop/wipe requests */ ||
	  ! jtransform_parse_crop_spec(&transformoption, argv[argn])) {
	fprintf(stderr, "%s: bogus -crop argument '%s'\n",
		progname, argv[argn]);
	exit(EXIT_FAILURE);
      }
#else
      select_transform(JXFORM_NONE);	/* force an error */
#endif

    } else if (keymatch(arg, "debug", 1) || keymatch(arg, "verbose", 1)) {
      /* Enable debug printouts. */
      /* On first -d, print version identification */
      static boolean printed_version = FALSE;

      if (! printed_version) {
	fprintf(stderr, "Independent JPEG Group's JPEGTRAN, version %s\n%s\n",
		JVERSION, JCOPYRIGHT);
	printed_version = TRUE;
      }
      cinfo->err->trace_level++;

    } else if (keymatch(arg, "flip", 1)) {
      /* Mirror left-right or top-bottom. */
      if (++argn >= argc)	/* advance to next argument */
	usage();
      if (keymatch(argv[argn], "horizontal", 1))
	select_transform(JXFORM_FLIP_H);
      else if (keymatch(argv[argn], "vertical", 1))
	select_transform(JXFORM_FLIP_V);
      else
	usage();

    } else if (keymatch(arg, "grayscale", 1) || keymatch(arg, "greyscale",1)) {
      /* Force to grayscale. */
#if TRANSFORMS_SUPPORTED
      transformoption.force_grayscale = TRUE;
#else
      select_transform(JXFORM_NONE);	/* force an error */
#endif

    } else if (keymatch(arg, "maxmemory", 3)) {
      /* Maximum memory in Kb (or Mb with 'm'). */
      long lval;
      char ch = 'x';

      if (++argn >= argc)	/* advance to next argument */
	usage();
      if (sscanf(argv[argn], "%ld%c", &lval, &ch) < 1)
	usage();
      if (ch == 'm' || ch == 'M')
	lval *= 1000L;
      cinfo->mem->max_memory_to_use = lval * 1000L;

    } else if (keymatch(arg, "optimize", 1) || keymatch(arg, "optimise", 1)) {
      /* Enable entropy parm optimization. */
#ifdef ENTROPY_OPT_SUPPORTED
      cinfo->optimize_coding = TRUE;
#else
      fprintf(stderr, "%s: sorry, entropy optimization was not compiled\n",
	      progname);
      exit(EXIT_FAILURE);
#endif

    } else if (keymatch(arg, "outfile", 4)) {
      /* Set output file name. */
      if (++argn >= argc)	/* advance to next argument */
	usage();
      outfilename = argv[argn];	/* save it away for later use */

    } else if (keymatch(arg, "perfect", 2)) {
      /* Fail if there is any partial edge MCUs that the transform can't
       * handle. */
      transformoption.perfect = TRUE;

    } else if (keymatch(arg, "progressive", 2)) {
      /* Select simple progressive mode. */
#ifdef C_PROGRESSIVE_SUPPORTED
      simple_progressive = TRUE;
      /* We must postpone execution until num_components is known. */
#else
      fprintf(stderr, "%s: sorry, progressive output was not compiled\n",
	      progname);
      exit(EXIT_FAILURE);
#endif

    } else if (keymatch(arg, "restart", 1)) {
      /* Restart interval in MCU rows (or in MCUs with 'b'). */
      long lval;
      char ch = 'x';

      if (++argn >= argc)	/* advance to next argument */
	usage();
      if (sscanf(argv[argn], "%ld%c", &lval, &ch) < 1)
	usage();
      if (lval < 0 || lval > 65535L)
	usage();
      if (ch == 'b' || ch == 'B') {
	cinfo->restart_interval = (unsigned int) lval;
	cinfo->restart_in_rows = 0; /* else prior '-restart n' overrides me */
      } else {
	cinfo->restart_in_rows = (int) lval;
	/* restart_interval will be computed during startup */
      }

    } else if (keymatch(arg, "rotate", 2)) {
      /* Rotate 90, 180, or 270 degrees (measured clockwise). */
      if (++argn >= argc)	/* advance to next argument */
	usage();
      if (keymatch(argv[argn], "90", 2))
	select_transform(JXFORM_ROT_90);
      else if (keymatch(argv[argn], "180", 3))
	select_transform(JXFORM_ROT_180);
      else if (keymatch(argv[argn], "270", 3))
	select_transform(JXFORM_ROT_270);
      else
	usage();

    } else if (keymatch(arg, "scale", 4)) {
      /* Scale the output image by a fraction M/N. */
      if (++argn >= argc)	/* advance to next argument */
	usage();
      scaleoption = argv[argn];
      /* We must postpone processing until decompression startup. */

    } else if (keymatch(arg, "scans", 1)) {
      /* Set scan script. */
#ifdef C_MULTISCAN_FILES_SUPPORTED
      if (++argn >= argc)	/* advance to next argument */
	usage();
      scansarg = argv[argn];
      /* We must postpone reading the file in case -progressive appears. */
#else
      fprintf(stderr, "%s: sorry, multi-scan output was not compiled\n",
	      progname);
      exit(EXIT_FAILURE);
#endif

    } else if (keymatch(arg, "transpose", 1)) {
      /* Transpose (across UL-to-LR axis). */
      select_transform(JXFORM_TRANSPOSE);

    } else if (keymatch(arg, "transverse", 6)) {
      /* Transverse transpose (across UR-to-LL axis). */
      select_transform(JXFORM_TRANSVERSE);

    } else if (keymatch(arg, "trim", 3)) {
      /* Trim off any partial edge MCUs that the transform can't handle. */
      transformoption.trim = TRUE;

    } else if (keymatch(arg, "wipe", 1)) {
#if TRANSFORMS_SUPPORTED
      if (++argn >= argc)	/* advance to next argument */
	usage();
      if (transformoption.crop /* reject multiple crop/wipe requests */ ||
	  ! jtransform_parse_crop_spec(&transformoption, argv[argn])) {
	fprintf(stderr, "%s: bogus -wipe argument '%s'\n",
		progname, argv[argn]);
	exit(EXIT_FAILURE);
      }
      select_transform(JXFORM_WIPE);
#else
      select_transform(JXFORM_NONE);	/* force an error */
#endif

    } else {
      usage();			/* bogus switch */
    }
  }

  /* Post-switch-scanning cleanup */

  if (for_real) {

#ifdef C_PROGRESSIVE_SUPPORTED
    if (simple_progressive)	/* process -progressive; -scans can override */
      jpeg_simple_progression(cinfo);
#endif

#ifdef C_MULTISCAN_FILES_SUPPORTED
    if (scansarg != NULL)	/* process -scans if it was present */
      if (! read_scan_script(cinfo, scansarg))
	usage();
#endif
  }

  return argn;			/* return index of next arg (file name) */
}


/*
 * The main program.
 */

int
main (int argc, char **argv)
{
  struct jpeg_decompress_struct srcinfo;
  struct jpeg_compress_struct dstinfo;
  struct jpeg_error_mgr jsrcerr, jdsterr;
#ifdef PROGRESS_REPORT
  struct cdjpeg_progress_mgr progress;
#endif
  jvirt_barray_ptr * src_coef_arrays;
  jvirt_barray_ptr * dst_coef_arrays;
  int file_index;
  /* We assume all-in-memory processing and can therefore use only a
   * single file pointer for sequential input and output operation. 
   */
  FILE * fp;

  /* On Mac, fetch a command line. */
#ifdef USE_CCOMMAND
  argc = ccommand(&argv);
#endif

  progname = argv[0];
  if (progname == NULL || progname[0] == 0)
    progname = "jpegtran";	/* in case C library doesn't provide it */

  /* Initialize the JPEG decompression object with default error handling. */
  srcinfo.err = jpeg_std_error(&jsrcerr);
  jpeg_create_decompress(&srcinfo);
  /* Initialize the JPEG compression object with default error handling. */
  dstinfo.err = jpeg_std_error(&jdsterr);
  jpeg_create_compress(&dstinfo);

  /* Now safe to enable signal catcher.
   * Note: we assume only the decompression object will have virtual arrays.
   */
#ifdef NEED_SIGNAL_CATCHER
  enable_signal_catcher((j_common_ptr) &srcinfo);
#endif

  /* Scan command line to find file names.
   * It is convenient to use just one switch-parsing routine, but the switch
   * values read here are mostly ignored; we will rescan the switches after
   * opening the input file.  Also note that most of the switches affect the
   * destination JPEG object, so we parse into that and then copy over what
   * needs to affects the source too.
   */

  file_index = parse_switches(&dstinfo, argc, argv, 0, FALSE);
  jsrcerr.trace_level = jdsterr.trace_level;
  srcinfo.mem->max_memory_to_use = dstinfo.mem->max_memory_to_use;

#ifdef TWO_FILE_COMMANDLINE
  /* Must have either -outfile switch or explicit output file name */
  if (outfilename == NULL) {
    if (file_index != argc-2) {
      fprintf(stderr, "%s: must name one input and one output file\n",
	      progname);
      usage();
    }
    outfilename = argv[file_index+1];
  } else {
    if (file_index != argc-1) {
      fprintf(stderr, "%s: must name one input and one output file\n",
	      progname);
      usage();
    }
  }
#else
  /* Unix style: expect zero or one file name */
  if (file_index < argc-1) {
    fprintf(stderr, "%s: only one input file\n", progname);
    usage();
  }
#endif /* TWO_FILE_COMMANDLINE */

  /* Open the input file. */
  if (file_index < argc) {
    if ((fp = fopen(argv[file_index], READ_BINARY)) == NULL) {
      fprintf(stderr, "%s: can't open %s for reading\n", progname, argv[file_index]);
      exit(EXIT_FAILURE);
    }
  } else {
    /* default input file is stdin */
    fp = read_stdin();
  }

#ifdef PROGRESS_REPORT
  start_progress_monitor((j_common_ptr) &dstinfo, &progress);
#endif

  /* Specify data source for decompression */
  jpeg_stdio_src(&srcinfo, fp);

  /* Enable saving of extra markers that we want to copy */
  jcopy_markers_setup(&srcinfo, copyoption);

  /* Read file header */
  (void) jpeg_read_header(&srcinfo, TRUE);

  /* Adjust default decompression parameters */
  if (scaleoption != NULL)
    if (sscanf(scaleoption, "%u/%u",
	&srcinfo.scale_num, &srcinfo.scale_denom) < 1)
      usage();

  /* Any space needed by a transform option must be requested before
   * jpeg_read_coefficients so that memory allocation will be done right.
   */
#if TRANSFORMS_SUPPORTED
  /* Fail right away if -perfect is given and transformation is not perfect.
   */
  if (!jtransform_request_workspace(&srcinfo, &transformoption)) {
    fprintf(stderr, "%s: transformation is not perfect\n", progname);
    exit(EXIT_FAILURE);
  }
#endif

  /* Read source file as DCT coefficients */
  src_coef_arrays = jpeg_read_coefficients(&srcinfo);

  /* Initialize destination compression parameters from source values */
  jpeg_copy_critical_parameters(&srcinfo, &dstinfo);

  /* Adjust destination parameters if required by transform options;
   * also find out which set of coefficient arrays will hold the output.
   */
#if TRANSFORMS_SUPPORTED
  dst_coef_arrays = jtransform_adjust_parameters(&srcinfo, &dstinfo,
						 src_coef_arrays,
						 &transformoption);
#else
  dst_coef_arrays = src_coef_arrays;
#endif

  /* Close input file, if we opened it.
   * Note: we assume that jpeg_read_coefficients consumed all input
   * until JPEG_REACHED_EOI, and that jpeg_finish_decompress will
   * only consume more while (! cinfo->inputctl->eoi_reached).
   * We cannot call jpeg_finish_decompress here since we still need the
   * virtual arrays allocated from the source object for processing.
   */
  if (fp != stdin)
    fclose(fp);

  /* Open the output file. */
  if (outfilename != NULL) {
    if ((fp = fopen(outfilename, WRITE_BINARY)) == NULL) {
      fprintf(stderr, "%s: can't open %s for writing\n", progname, outfilename);
      exit(EXIT_FAILURE);
    }
  } else {
    /* default output file is stdout */
    fp = write_stdout();
  }

  /* Adjust default compression parameters by re-parsing the options */
  file_index = parse_switches(&dstinfo, argc, argv, 0, TRUE);

  /* Specify data destination for compression */
  jpeg_stdio_dest(&dstinfo, fp);

  /* Start compressor (note no image data is actually written here) */
  jpeg_write_coefficients(&dstinfo, dst_coef_arrays);

  /* Copy to the output file any extra markers that we want to preserve */
  jcopy_markers_execute(&srcinfo, &dstinfo, copyoption);

  /* Execute image transformation, if any */
#if TRANSFORMS_SUPPORTED
  jtransform_execute_transformation(&srcinfo, &dstinfo,
				    src_coef_arrays,
				    &transformoption);
#endif

  /* Finish compression and release memory */
  jpeg_finish_compress(&dstinfo);
  jpeg_destroy_compress(&dstinfo);
  (void) jpeg_finish_decompress(&srcinfo);
  jpeg_destroy_decompress(&srcinfo);

  /* Close output file, if we opened it */
  if (fp != stdout)
    fclose(fp);

#ifdef PROGRESS_REPORT
  end_progress_monitor((j_common_ptr) &dstinfo);
#endif

  /* All done. */
  exit(jsrcerr.num_warnings + jdsterr.num_warnings ?EXIT_WARNING:EXIT_SUCCESS);
  return 0;			/* suppress no-return-value warnings */
}
