#include "jpgd.h" 

extern "C" {
	unsigned char *jpgd_decompress_jpeg_image_from_memory(const unsigned char *pSrc_data, int src_data_size, int *width, int *height, int *actual_comps, int req_comps) {
		return jpgd::decompress_jpeg_image_from_memory(pSrc_data, src_data_size, width, height, actual_comps, req_comps);
	}
	const char *jpgd_failure_reason(void) {
		return jpgd::failure_reason();
	}
}