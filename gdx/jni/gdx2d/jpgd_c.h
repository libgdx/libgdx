#ifdef __cplusplus
extern "C" {
#endif

const char *jpgd_failure_reason(void);
unsigned char *jpgd_decompress_jpeg_image_from_memory(const unsigned char *pSrc_data, int src_data_size, int *width, int *height, int *actual_comps, int req_comps);

#ifdef __cplusplus
} //end extern "C"
#endif