: The following file renaming is needed if one want to compile all files 
: using the same output directory, e.g. "Debug\" or "Release\". 
:
: Usage:
: copy all src WebP files into src\, then, run this script to rename files
:
: dec\
copy /Y dec\alpha.c dec\dec_alpha.c
copy /Y dec\buffer.c dec\dec_buffer.c
copy /Y dec\frame.c dec\dec_frame.c
copy /Y dec\idec.c dec\dec_idec.c
copy /Y dec\io.c dec\dec_io.c
copy /Y dec\layer.c dec\dec_layer.c
copy /Y dec\quant.c dec\dec_quant.c
copy /Y dec\tree.c dec\dec_tree.c
copy /Y dec\vp8.c dec\dec_vp8.c
copy /Y dec\vp8l.c dec\dec_vp8l.c
copy /Y dec\webp.c dec\dec_webp.c

del /Q dec\alpha.c
del /Q dec\buffer.c
del /Q dec\frame.c
del /Q dec\idec.c
del /Q dec\io.c
del /Q dec\layer.c
del /Q dec\quant.c
del /Q dec\tree.c
del /Q dec\vp8.c
del /Q dec\vp8l.c
del /Q dec\webp.c

: enc\
copy /Y enc\alpha.c enc\enc_alpha.c
copy /Y enc\analysis.c enc\enc_analysis.c
copy /Y enc\backward_references.c enc\enc_backward_references.c
copy /Y enc\config.c enc\enc_config.c
copy /Y enc\cost.c enc\enc_cost.c
copy /Y enc\filter.c enc\enc_filter.c
copy /Y enc\frame.c enc\enc_frame.c
copy /Y enc\histogram.c enc\enc_histogram.c
copy /Y enc\iterator.c enc\enc_iterator.c
copy /Y enc\layer.c enc\enc_layer.c
copy /Y enc\picture.c enc\enc_picture.c
copy /Y enc\quant.c enc\enc_quant.c
copy /Y enc\syntax.c enc\enc_syntax.c
copy /Y enc\tree.c enc\enc_tree.c
copy /Y enc\vp8l.c enc\enc_vp8l.c
copy /Y enc\webpenc.c enc\enc_webpenc.c

del /Q enc\alpha.c
del /Q enc\analysis.c
del /Q enc\backward_references.c
del /Q enc\config.c
del /Q enc\cost.c
del /Q enc\filter.c
del /Q enc\frame.c
del /Q enc\histogram.c
del /Q enc\iterator.c
del /Q enc\layer.c
del /Q enc\picture.c
del /Q enc\quant.c
del /Q enc\syntax.c
del /Q enc\tree.c
del /Q enc\vp8l.c
del /Q enc\webpenc.c

: Makefiles
del /S /Q Makefile.am
del /S /Q *.pc.in

pause -1
