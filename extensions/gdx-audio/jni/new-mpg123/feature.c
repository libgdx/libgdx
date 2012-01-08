#include "mpg123lib_intern.h"

int mpg123_feature(const enum mpg123_feature_set key)
{
	switch(key)
	{
		case MPG123_FEATURE_ABI_UTF8OPEN:
#ifdef WANT_WIN32_UNICODE
		return 1;
#else
		return 0;
#endif /* WANT_WIN32_UNICODE */

		case MPG123_FEATURE_OUTPUT_8BIT:
#ifdef NO_8BIT
		return 0;
#else
		return 1;
#endif /* mpg123_output_8bit */

		case MPG123_FEATURE_OUTPUT_16BIT:
#ifdef NO_16BIT
		return 0;
#else
		return 1;
#endif /* mpg123_output_16bit */

		case MPG123_FEATURE_OUTPUT_32BIT:
#ifdef NO_32BIT
		return 0;
#else
		return 1;
#endif /* mpg123_output_32bit */

		case MPG123_FEATURE_PARSE_ID3V2:
#ifdef NO_ID3V2
		return 0;
#else
		return 1;
#endif /* NO_ID3V2 */

		case MPG123_FEATURE_DECODE_LAYER1:
#ifdef NO_LAYER1
		return 0;
#else
		return 1;
#endif /* NO_LAYER1 */

		case MPG123_FEATURE_DECODE_LAYER2:
#ifdef NO_LAYER2
		return 0;
#else
		return 1;
#endif /* NO_LAYER2 */

		case MPG123_FEATURE_DECODE_LAYER3:
#ifdef NO_LAYER3
		return 0;
#else
		return 1;
#endif /* NO_LAYER3 */

		case MPG123_FEATURE_DECODE_ACCURATE:
#ifdef ACCURATE_ROUNDING
		return 1;
#else
		return 0;
#endif /* ACCURATE_ROUNDING */

		case MPG123_FEATURE_DECODE_DOWNSAMPLE:
#ifdef NO_DOWNSAMPLE
		return 0;
#else
		return 1;
#endif /* NO_DOWNSAMPLE */

		case MPG123_FEATURE_DECODE_NTOM:
#ifdef NO_NTOM
		return 0;
#else
		return 1;
#endif /* NO_NTOM */

		case MPG123_FEATURE_PARSE_ICY:
#ifdef NO_ICY
		return 0;
#else
		return 1;
#endif /* NO_ICY */

		case MPG123_FEATURE_INDEX:
#ifdef FRAME_INDEX
		return 1;
#else
		return 0;
#endif /* FRAME_INDEX */
		case MPG123_FEATURE_TIMEOUT_READ:
#ifdef TIMEOUT_READ
		return 1;
#else
		return 0;
#endif

		default: return 0;
	}
}
