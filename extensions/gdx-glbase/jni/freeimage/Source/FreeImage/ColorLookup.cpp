// ==========================================================
// X11 and SVG Color name lookup
//
// Design and implementation by
// - Karl-Heinz Bussian (khbussian@moss.de)
//
// This file is part of FreeImage 3
//
// COVERED CODE IS PROVIDED UNDER THIS LICENSE ON AN "AS IS" BASIS, WITHOUT WARRANTY
// OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, WARRANTIES
// THAT THE COVERED CODE IS FREE OF DEFECTS, MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE
// OR NON-INFRINGING. THE ENTIRE RISK AS TO THE QUALITY AND PERFORMANCE OF THE COVERED
// CODE IS WITH YOU. SHOULD ANY COVERED CODE PROVE DEFECTIVE IN ANY RESPECT, YOU (NOT
// THE INITIAL DEVELOPER OR ANY OTHER CONTRIBUTOR) ASSUME THE COST OF ANY NECESSARY
// SERVICING, REPAIR OR CORRECTION. THIS DISCLAIMER OF WARRANTY CONSTITUTES AN ESSENTIAL
// PART OF THIS LICENSE. NO USE OF ANY COVERED CODE IS AUTHORIZED HEREUNDER EXCEPT UNDER
// THIS DISCLAIMER.
//
// Use at your own risk!
//
// ==========================================================

#include "FreeImage.h"
#include "Utilities.h"

// RGB color names  ---------------------------------------------------------

typedef struct tagNamedColor {
        const char *name;     // color name
        BYTE  r;        // red value
        BYTE  g;        // green value
        BYTE  b;        // blue value
} NamedColor;

// --------------------------------------------------------------------------

/**
Helper function : perform a binary search on a color array
@param name Color name
@param color_array Color array
@param n Length of the color array
@return Returns the color index in the array if successful, returns -1 otherwise
*/
static int
binsearch(const char *name, const NamedColor *color_array, int n) {
    int cond, low, mid, high;

    low = 0;
    high = n - 1;
    while (low <= high) {
                mid = (low + high) / 2;
                if ((cond = strcmp(name, color_array[mid].name)) < 0)
                        high = mid - 1;
                else if (cond > 0)
                        low = mid + 1;
                else
                        return mid;
        }
    return -1;
}

/**
Perform a binary search on a color array
@param szColor Color name
@param color_array Color array
@param ncolors Length of the color array
@return Returns the color index in the array if successful, returns -1 otherwise
*/
static int
FreeImage_LookupNamedColor(const char *szColor, const NamedColor *color_array, int ncolors) {
    int i;
    char color[64];

    // make lower case name, squezze white space

    for (i = 0; szColor[i] && i < sizeof(color) - 1; i++) {
        if (isspace(szColor[i]))
            continue;
        if (isupper(szColor[i]))
            color[i] = (char)tolower(szColor[i]);
        else
            color[i] = szColor[i];
    }
    color[i] = 0;

    return (binsearch(color, color_array, ncolors));
}

// ==========================================================
// X11 Color name lookup

/**
 This big list of color names was formed from the file: /usr/X11R6/lib/X11/rgb.txt
 found on a standard Linux installation.
*/

static NamedColor X11ColorMap[] = {
    { "aliceblue",            240, 248, 255 },
    { "antiquewhite",         250, 235, 215 },
    { "antiquewhite1",        255, 239, 219 },
    { "antiquewhite2",        238, 223, 204 },
    { "antiquewhite3",        205, 192, 176 },
    { "antiquewhite4",        139, 131, 120 },
    { "aquamarine",           127, 255, 212 },
    { "aquamarine1",          127, 255, 212 },
    { "aquamarine2",          118, 238, 198 },
    { "aquamarine3",          102, 205, 170 },
    { "aquamarine4",           69, 139, 116 },
    { "azure",                240, 255, 255 },
    { "azure1",               240, 255, 255 },
    { "azure2",               224, 238, 238 },
    { "azure3",               193, 205, 205 },
    { "azure4",               131, 139, 139 },
    { "beige",                245, 245, 220 },
    { "bisque",               255, 228, 196 },
    { "bisque1",              255, 228, 196 },
    { "bisque2",              238, 213, 183 },
    { "bisque3",              205, 183, 158 },
    { "bisque4",              139, 125, 107 },
    { "black",                  0,   0,   0 },
    { "blanchedalmond",       255, 235, 205 },
    { "blue",                   0,   0, 255 },
    { "blue1",                  0,   0, 255 },
    { "blue2",                  0,   0, 238 },
    { "blue3",                  0,   0, 205 },
    { "blue4",                  0,   0, 139 },
    { "blueviolet",           138,  43, 226 },
    { "brown",                165,  42,  42 },
    { "brown1",               255,  64,  64 },
    { "brown2",               238,  59,  59 },
    { "brown3",               205,  51,  51 },
    { "brown4",               139,  35,  35 },
    { "burlywood",            222, 184, 135 },
    { "burlywood1",           255, 211, 155 },
    { "burlywood2",           238, 197, 145 },
    { "burlywood3",           205, 170, 125 },
    { "burlywood4",           139, 115,  85 },
    { "cadetblue",             95, 158, 160 },
    { "cadetblue1",           152, 245, 255 },
    { "cadetblue2",           142, 229, 238 },
    { "cadetblue3",           122, 197, 205 },
    { "cadetblue4",            83, 134, 139 },
    { "chartreuse",           127, 255,   0 },
    { "chartreuse1",          127, 255,   0 },
    { "chartreuse2",          118, 238,   0 },
    { "chartreuse3",          102, 205,   0 },
    { "chartreuse4",           69, 139,   0 },
    { "chocolate",            210, 105,  30 },
    { "chocolate1",           255, 127,  36 },
    { "chocolate2",           238, 118,  33 },
    { "chocolate3",           205, 102,  29 },
    { "chocolate4",           139,  69,  19 },
    { "coral",                255, 127,  80 },
    { "coral1",               255, 114,  86 },
    { "coral2",               238, 106,  80 },
    { "coral3",               205,  91,  69 },
    { "coral4",               139,  62,  47 },
    { "cornflowerblue",       100, 149, 237 },
    { "cornsilk",             255, 248, 220 },
    { "cornsilk1",            255, 248, 220 },
    { "cornsilk2",            238, 232, 205 },
    { "cornsilk3",            205, 200, 177 },
    { "cornsilk4",            139, 136, 120 },
    { "cyan",                   0, 255, 255 },
    { "cyan1",                  0, 255, 255 },
    { "cyan2",                  0, 238, 238 },
    { "cyan3",                  0, 205, 205 },
    { "cyan4",                  0, 139, 139 },
    { "darkblue",               0,   0, 139 },
    { "darkcyan",               0, 139, 139 },
    { "darkgoldenrod",        184, 134,  11 },
    { "darkgoldenrod1",       255, 185,  15 },
    { "darkgoldenrod2",       238, 173,  14 },
    { "darkgoldenrod3",       205, 149,  12 },
    { "darkgoldenrod4",       139, 101,   8 },
    { "darkgreen",              0, 100,   0 },
    { "darkkhaki",            189, 183, 107 },
    { "darkmagenta",          139,   0, 139 },
    { "darkolivegreen",        85, 107,  47 },
    { "darkolivegreen1",      202, 255, 112 },
    { "darkolivegreen2",      188, 238, 104 },
    { "darkolivegreen3",      162, 205,  90 },
    { "darkolivegreen4",      110, 139,  61 },
    { "darkorange",           255, 140,   0 },
    { "darkorange1",          255, 127,   0 },
    { "darkorange2",          238, 118,   0 },
    { "darkorange3",          205, 102,   0 },
    { "darkorange4",          139,  69,   0 },
    { "darkorchid",           153,  50, 204 },
    { "darkorchid1",          191,  62, 255 },
    { "darkorchid2",          178,  58, 238 },
    { "darkorchid3",          154,  50, 205 },
    { "darkorchid4",          104,  34, 139 },
    { "darkred",              139,   0,   0 },
    { "darksalmon",           233, 150, 122 },
    { "darkseagreen",         143, 188, 143 },
    { "darkseagreen1",        193, 255, 193 },
    { "darkseagreen2",        180, 238, 180 },
    { "darkseagreen3",        155, 205, 155 },
    { "darkseagreen4",        105, 139, 105 },
    { "darkslateblue",         72,  61, 139 },
    { "darkslategray",         47,  79,  79 },
    { "darkslategray1",       151, 255, 255 },
    { "darkslategray2",       141, 238, 238 },
    { "darkslategray3",       121, 205, 205 },
    { "darkslategray4",        82, 139, 139 },
    { "darkslategrey",         47,  79,  79 },
    { "darkturquoise",          0, 206, 209 },
    { "darkviolet",           148,   0, 211 },
    { "deeppink",             255,  20, 147 },
    { "deeppink1",            255,  20, 147 },
    { "deeppink2",            238,  18, 137 },
    { "deeppink3",            205,  16, 118 },
    { "deeppink4",            139,  10,  80 },
    { "deepskyblue",            0, 191, 255 },
    { "deepskyblue1",           0, 191, 255 },
    { "deepskyblue2",           0, 178, 238 },
    { "deepskyblue3",           0, 154, 205 },
    { "deepskyblue4",           0, 104, 139 },
    { "dimgray",              105, 105, 105 },
    { "dimgrey",              105, 105, 105 },
    { "dodgerblue",            30, 144, 255 },
    { "dodgerblue1",           30, 144, 255 },
    { "dodgerblue2",           28, 134, 238 },
    { "dodgerblue3",           24, 116, 205 },
    { "dodgerblue4",           16,  78, 139 },
    { "firebrick",            178,  34,  34 },
    { "firebrick1",           255,  48,  48 },
    { "firebrick2",           238,  44,  44 },
    { "firebrick3",           205,  38,  38 },
    { "firebrick4",           139,  26,  26 },
    { "floralwhite",          255, 250, 240 },
    { "forestgreen",          176,  48,  96 },
    { "gainsboro",            220, 220, 220 },
    { "ghostwhite",           248, 248, 255 },
    { "gold",                 255, 215,   0 },
    { "gold1",                255, 215,   0 },
    { "gold2",                238, 201,   0 },
    { "gold3",                205, 173,   0 },
    { "gold4",                139, 117,   0 },
    { "goldenrod",            218, 165,  32 },
    { "goldenrod1",           255, 193,  37 },
    { "goldenrod2",           238, 180,  34 },
    { "goldenrod3",           205, 155,  29 },
    { "goldenrod4",           139, 105,  20 },
    { "gray",                 190, 190, 190 },
    { "green",                  0, 255,   0 },
    { "green1",                 0, 255,   0 },
    { "green2",                 0, 238,   0 },
    { "green3",                 0, 205,   0 },
    { "green4",                 0, 139,   0 },
    { "greenyellow",          173, 255,  47 },
    { "grey",                 190, 190, 190 },
    { "honeydew",             240, 255, 240 },
    { "honeydew1",            240, 255, 240 },
    { "honeydew2",            224, 238, 224 },
    { "honeydew3",            193, 205, 193 },
    { "honeydew4",            131, 139, 131 },
    { "hotpink",              255, 105, 180 },
    { "hotpink1",             255, 110, 180 },
    { "hotpink2",             238, 106, 167 },
    { "hotpink3",             205,  96, 144 },
    { "hotpink4",             139,  58,  98 },
    { "indianred",            205,  92,  92 },
    { "indianred1",           255, 106, 106 },
    { "indianred2",           238,  99,  99 },
    { "indianred3",           205,  85,  85 },
    { "indianred4",           139,  58,  58 },
    { "ivory",                255, 255, 240 },
    { "ivory1",               255, 255, 240 },
    { "ivory2",               238, 238, 224 },
    { "ivory3",               205, 205, 193 },
    { "ivory4",               139, 139, 131 },
    { "khaki",                240, 230, 140 },
    { "khaki1",               255, 246, 143 },
    { "khaki2",               238, 230, 133 },
    { "khaki3",               205, 198, 115 },
    { "khaki4",               139, 134,  78 },
    { "lavender",             230, 230, 250 },
    { "lavenderblush",        255, 240, 245 },
    { "lavenderblush1",       255, 240, 245 },
    { "lavenderblush2",       238, 224, 229 },
    { "lavenderblush3",       205, 193, 197 },
    { "lavenderblush4",       139, 131, 134 },
    { "lawngreen",            124, 252,   0 },
    { "lemonchiffon",         255, 250, 205 },
    { "lemonchiffon1",        255, 250, 205 },
    { "lemonchiffon2",        238, 233, 191 },
    { "lemonchiffon3",        205, 201, 165 },
    { "lemonchiffon4",        139, 137, 112 },
    { "lightblue",            173, 216, 230 },
    { "lightblue1",           191, 239, 255 },
    { "lightblue2",           178, 223, 238 },
    { "lightblue3",           154, 192, 205 },
    { "lightblue4",           104, 131, 139 },
    { "lightcoral",           240, 128, 128 },
    { "lightcyan",            224, 255, 255 },
    { "lightcyan1",           224, 255, 255 },
    { "lightcyan2",           209, 238, 238 },
    { "lightcyan3",           180, 205, 205 },
    { "lightcyan4",           122, 139, 139 },
    { "lightgoldenrod",       238, 221, 130 },
    { "lightgoldenrod1",      255, 236, 139 },
    { "lightgoldenrod2",      238, 220, 130 },
    { "lightgoldenrod3",      205, 190, 112 },
    { "lightgoldenrod4",      139, 129,  76 },
    { "lightgoldenrodyellow", 250, 250, 210 },
    { "lightgray",            211, 211, 211 },
    { "lightgreen",           144, 238, 144 },
    { "lightgrey",            211, 211, 211 },
    { "lightpink",            255, 182, 193 },
    { "lightpink1",           255, 174, 185 },
    { "lightpink2",           238, 162, 173 },
    { "lightpink3",           205, 140, 149 },
    { "lightpink4",           139,  95, 101 },
    { "lightsalmon",          255, 160, 122 },
    { "lightsalmon1",         255, 160, 122 },
    { "lightsalmon2",         238, 149, 114 },
    { "lightsalmon3",         205, 129,  98 },
    { "lightsalmon4",         139,  87,  66 },
    { "lightseagreen",         32, 178, 170 },
    { "lightskyblue",         135, 206, 250 },
    { "lightskyblue1",        176, 226, 255 },
    { "lightskyblue2",        164, 211, 238 },
    { "lightskyblue3",        141, 182, 205 },
    { "lightskyblue4",         96, 123, 139 },
    { "lightslateblue",       132, 112, 255 },
    { "lightslategray",       119, 136, 153 },
    { "lightslategrey",       119, 136, 153 },
    { "lightsteelblue",       176, 196, 222 },
    { "lightsteelblue1",      202, 225, 255 },
    { "lightsteelblue2",      188, 210, 238 },
    { "lightsteelblue3",      162, 181, 205 },
    { "lightsteelblue4",      110, 123, 139 },
    { "lightyellow",          255, 255, 224 },
    { "lightyellow1",         255, 255, 224 },
    { "lightyellow2",         238, 238, 209 },
    { "lightyellow3",         205, 205, 180 },
    { "lightyellow4",         139, 139, 122 },
    { "limegreen",             50, 205,  50 },
    { "linen",                250, 240, 230 },
    { "magenta",              255,   0, 255 },
    { "magenta1",             255,   0, 255 },
    { "magenta2",             238,   0, 238 },
    { "magenta3",             205,   0, 205 },
    { "magenta4",             139,   0, 139 },
    { "maroon",                 0, 255, 255 },
    { "maroon1",              255,  52, 179 },
    { "maroon2",              238,  48, 167 },
    { "maroon3",              205,  41, 144 },
    { "maroon4",              139,  28,  98 },
    { "mediumaquamarine",     102, 205, 170 },
    { "mediumblue",             0,   0, 205 },
    { "mediumorchid",         186,  85, 211 },
    { "mediumorchid1",        224, 102, 255 },
    { "mediumorchid2",        209,  95, 238 },
    { "mediumorchid3",        180,  82, 205 },
    { "mediumorchid4",        122,  55, 139 },
    { "mediumpurple",         147, 112, 219 },
    { "mediumpurple1",        171, 130, 255 },
    { "mediumpurple2",        159, 121, 238 },
    { "mediumpurple3",        137, 104, 205 },
    { "mediumpurple4",         93,  71, 139 },
    { "mediumseagreen",        60, 179, 113 },
    { "mediumslateblue",      123, 104, 238 },
    { "mediumspringgreen",      0, 250, 154 },
    { "mediumturquoise",       72, 209, 204 },
    { "mediumvioletred",      199,  21, 133 },
    { "midnightblue",          25,  25, 112 },
    { "mintcream",            245, 255, 250 },
    { "mistyrose",            255, 228, 225 },
    { "mistyrose1",           255, 228, 225 },
    { "mistyrose2",           238, 213, 210 },
    { "mistyrose3",           205, 183, 181 },
    { "mistyrose4",           139, 125, 123 },
    { "moccasin",             255, 228, 181 },
    { "navajowhite",          255, 222, 173 },
    { "navajowhite1",         255, 222, 173 },
    { "navajowhite2",         238, 207, 161 },
    { "navajowhite3",         205, 179, 139 },
    { "navajowhite4",         139, 121,  94 },
    { "navy",                   0,   0, 128 },
    { "navyblue",               0,   0, 128 },
    { "oldlace",              253, 245, 230 },
    { "olivedrab",            107, 142,  35 },
    { "olivedrab1",           192, 255,  62 },
    { "olivedrab2",           179, 238,  58 },
    { "olivedrab3",           154, 205,  50 },
    { "olivedrab4",           105, 139,  34 },
    { "orange",               255, 165,   0 },
    { "orange1",              255, 165,   0 },
    { "orange2",              238, 154,   0 },
    { "orange3",              205, 133,   0 },
    { "orange4",              139,  90,   0 },
    { "orangered",            255,  69,   0 },
    { "orangered1",           255,  69,   0 },
    { "orangered2",           238,  64,   0 },
    { "orangered3",           205,  55,   0 },
    { "orangered4",           139,  37,   0 },
    { "orchid",               218, 112, 214 },
    { "orchid1",              255, 131, 250 },
    { "orchid2",              238, 122, 233 },
    { "orchid3",              205, 105, 201 },
    { "orchid4",              139,  71, 137 },
    { "palegoldenrod",        238, 232, 170 },
    { "palegreen",            152, 251, 152 },
    { "palegreen1",           154, 255, 154 },
    { "palegreen2",           144, 238, 144 },
    { "palegreen3",           124, 205, 124 },
    { "palegreen4",            84, 139,  84 },
    { "paleturquoise",        175, 238, 238 },
    { "paleturquoise1",       187, 255, 255 },
    { "paleturquoise2",       174, 238, 238 },
    { "paleturquoise3",       150, 205, 205 },
    { "paleturquoise4",       102, 139, 139 },
    { "palevioletred",        219, 112, 147 },
    { "palevioletred1",       255, 130, 171 },
    { "palevioletred2",       238, 121, 159 },
    { "palevioletred3",       205, 104, 137 },
    { "palevioletred4",       139,  71,  93 },
    { "papayawhip",           255, 239, 213 },
    { "peachpuff",            255, 218, 185 },
    { "peachpuff1",           255, 218, 185 },
    { "peachpuff2",           238, 203, 173 },
    { "peachpuff3",           205, 175, 149 },
    { "peachpuff4",           139, 119, 101 },
    { "peru",                 205, 133,  63 },
    { "pink",                 255, 192, 203 },
    { "pink1",                255, 181, 197 },
    { "pink2",                238, 169, 184 },
    { "pink3",                205, 145, 158 },
    { "pink4",                139,  99, 108 },
    { "plum",                 221, 160, 221 },
    { "plum1",                255, 187, 255 },
    { "plum2",                238, 174, 238 },
    { "plum3",                205, 150, 205 },
    { "plum4",                139, 102, 139 },
    { "powderblue",           176, 224, 230 },
    { "purple",               160,  32, 240 },
    { "purple1",              155,  48, 255 },
    { "purple2",              145,  44, 238 },
    { "purple3",              125,  38, 205 },
    { "purple4",               85,  26, 139 },
    { "red",                  255,   0,   0 },
    { "red1",                 255,   0,   0 },
    { "red2",                 238,   0,   0 },
    { "red3",                 205,   0,   0 },
    { "red4",                 139,   0,   0 },
    { "rosybrown",            188, 143, 143 },
    { "rosybrown1",           255, 193, 193 },
    { "rosybrown2",           238, 180, 180 },
    { "rosybrown3",           205, 155, 155 },
    { "rosybrown4",           139, 105, 105 },
    { "royalblue",             65, 105, 225 },
    { "royalblue1",            72, 118, 255 },
    { "royalblue2",            67, 110, 238 },
    { "royalblue3",            58,  95, 205 },
    { "royalblue4",            39,  64, 139 },
    { "saddlebrown",          139,  69,  19 },
    { "salmon",               250, 128, 114 },
    { "salmon1",              255, 140, 105 },
    { "salmon2",              238, 130,  98 },
    { "salmon3",              205, 112,  84 },
    { "salmon4",              139,  76,  57 },
    { "sandybrown",           244, 164,  96 },
    { "seagreen",              46, 139,  87 },
    { "seagreen1",             84, 255, 159 },
    { "seagreen2",             78, 238, 148 },
    { "seagreen3",             67, 205, 128 },
    { "seagreen4",             46, 139,  87 },
    { "seashell",             255, 245, 238 },
    { "seashell1",            255, 245, 238 },
    { "seashell2",            238, 229, 222 },
    { "seashell3",            205, 197, 191 },
    { "seashell4",            139, 134, 130 },
    { "sienna",               160,  82,  45 },
    { "sienna1",              255, 130,  71 },
    { "sienna2",              238, 121,  66 },
    { "sienna3",              205, 104,  57 },
    { "sienna4",              139,  71,  38 },
    { "skyblue",              135, 206, 235 },
    { "skyblue1",             135, 206, 255 },
    { "skyblue2",             126, 192, 238 },
    { "skyblue3",             108, 166, 205 },
    { "skyblue4",              74, 112, 139 },
    { "slateblue",            106,  90, 205 },
    { "slateblue1",           131, 111, 255 },
    { "slateblue2",           122, 103, 238 },
    { "slateblue3",           105,  89, 205 },
    { "slateblue4",            71,  60, 139 },
    { "slategray",            112, 128, 144 },
    { "slategray1",           198, 226, 255 },
    { "slategray2",           185, 211, 238 },
    { "slategray3",           159, 182, 205 },
    { "slategray4",           108, 123, 139 },
    { "slategrey",            112, 128, 144 },
    { "snow",                 255, 250, 250 },
    { "snow1",                255, 250, 250 },
    { "snow2",                238, 233, 233 },
    { "snow3",                205, 201, 201 },
    { "snow4",                139, 137, 137 },
    { "springgreen",            0, 255, 127 },
    { "springgreen1",           0, 255, 127 },
    { "springgreen2",           0, 238, 118 },
    { "springgreen3",           0, 205, 102 },
    { "springgreen4",           0, 139,  69 },
    { "steelblue",             70, 130, 180 },
    { "steelblue1",            99, 184, 255 },
    { "steelblue2",            92, 172, 238 },
    { "steelblue3",            79, 148, 205 },
    { "steelblue4",            54, 100, 139 },
    { "tan",                  210, 180, 140 },
    { "tan1",                 255, 165,  79 },
    { "tan2",                 238, 154,  73 },
    { "tan3",                 205, 133,  63 },
    { "tan4",                 139,  90,  43 },
    { "thistle",              216, 191, 216 },
    { "thistle1",             255, 225, 255 },
    { "thistle2",             238, 210, 238 },
    { "thistle3",             205, 181, 205 },
    { "thistle4",             139, 123, 139 },
    { "tomato",               255,  99,  71 },
    { "tomato1",              255,  99,  71 },
    { "tomato2",              238,  92,  66 },
    { "tomato3",              205,  79,  57 },
    { "tomato4",              139,  54,  38 },
    { "turquoise",             64, 224, 208 },
    { "turquoise1",             0, 245, 255 },
    { "turquoise2",             0, 229, 238 },
    { "turquoise3",             0, 197, 205 },
    { "turquoise4",             0, 134, 139 },
    { "violet",               238, 130, 238 },
    { "violetred",            208,  32, 144 },
    { "violetred1",           255,  62, 150 },
    { "violetred2",           238,  58, 140 },
    { "violetred3",           205,  50, 120 },
    { "violetred4",           139,  34,  82 },
    { "wheat",                245, 222, 179 },
    { "wheat1",               255, 231, 186 },
    { "wheat2",               238, 216, 174 },
    { "wheat3",               205, 186, 150 },
    { "wheat4",               139, 126, 102 },
    { "white",                255, 255, 255 },
    { "whitesmoke",           245, 245, 245 },
    { "yellow",               255, 255,   0 },
    { "yellow1",              255, 255,   0 },
    { "yellow2",              238, 238,   0 },
    { "yellow3",              205, 205,   0 },
    { "yellow4",              139, 139,   0 },
    { "yellowgreen",          154, 205,  50 }
};


BOOL DLL_CALLCONV
FreeImage_LookupX11Color(const char *szColor, BYTE *nRed, BYTE *nGreen, BYTE *nBlue) {
    int i;

    // lookup color
    i = FreeImage_LookupNamedColor(szColor, X11ColorMap, sizeof(X11ColorMap)/sizeof(X11ColorMap[0]));
    if (i >= 0) {
        *nRed   = X11ColorMap[i].r;
        *nGreen = X11ColorMap[i].g;
        *nBlue  = X11ColorMap[i].b;
        return TRUE;
    }

    // not found, try for grey color with attached percent value
    if ( (szColor[0] == 'g' || szColor[0] == 'G') &&
         (szColor[1] == 'r' || szColor[1] == 'R') &&
         (szColor[2] == 'e' || szColor[2] == 'E' || szColor[2] == 'a' || szColor[2] == 'A' ) &&
         (szColor[3] == 'y' || szColor[3] == 'Y' ) )  {

        // grey<num>, or gray<num>, num 1...100
        i = strtol(szColor+4, NULL, 10);
        *nRed   = (BYTE)(255.0/100.0 * i);
        *nGreen = *nRed;
        *nBlue  = *nRed;

        return TRUE;
    }

    // not found at all
    *nRed   = 0;
    *nGreen = 0;
    *nBlue  = 0;

    return FALSE;
}

// ==========================================================
// SVG Color name lookup

/**
 These are the colors defined in the SVG standard (I haven't checked
 the final recommendation for changes)
*/
static NamedColor SVGColorMap[] = {
        { "aliceblue",                  240, 248, 255 },
        { "antiquewhite",               250, 235, 215 },
        { "aqua",                         0, 255, 255 },
        { "aquamarine",                 127, 255, 212 },
        { "azure",                      240, 255, 255 },
        { "beige",                      245, 245, 220 },
        { "bisque",                     255, 228, 196 },
        { "black",                        0,   0,   0 },
        { "blanchedalmond",             255, 235, 205 },
        { "blue",                         0,   0, 255 },
        { "blueviolet",                 138,  43, 226 },
        { "brown",                      165,  42,  42 },
        { "burlywood",                  222, 184, 135 },
        { "cadetblue",                   95, 158, 160 },
        { "chartreuse",                 127, 255,   0 },
        { "chocolate",                  210, 105,  30 },
        { "coral",                      255, 127,  80 },
        { "cornflowerblue",             100, 149, 237 },
        { "cornsilk",                   255, 248, 220 },
        { "crimson",                    220,  20,  60 },
        { "cyan",                         0, 255, 255 },
        { "darkblue",                     0,   0, 139 },
        { "darkcyan",                     0, 139, 139 },
        { "darkgoldenrod",              184, 134,  11 },
        { "darkgray",                   169, 169, 169 },
        { "darkgreen",                    0, 100,   0 },
        { "darkgrey",                   169, 169, 169 },
        { "darkkhaki",                  189, 183, 107 },
        { "darkmagenta",                139,   0, 139 },
        { "darkolivegreen",              85, 107,  47 },
        { "darkorange",                 255, 140,   0 },
        { "darkorchid",                 153,  50, 204 },
        { "darkred",                    139,   0,   0 },
        { "darksalmon",                 233, 150, 122 },
        { "darkseagreen",               143, 188, 143 },
        { "darkslateblue",               72,  61, 139 },
        { "darkslategray",               47,  79,  79 },
        { "darkslategrey",               47,  79,  79 },
        { "darkturquoise",                0, 206, 209 },
        { "darkviolet",                 148,   0, 211 },
        { "deeppink",                   255,  20, 147 },
        { "deepskyblue",                  0, 191, 255 },
        { "dimgray",                    105, 105, 105 },
        { "dimgrey",                    105, 105, 105 },
        { "dodgerblue",                  30, 144, 255 },
        { "firebrick",                  178,  34,  34 },
        { "floralwhite",                255, 250, 240 },
        { "forestgreen",                 34, 139,  34 },
        { "fuchsia",                    255,   0, 255 },
        { "gainsboro",                  220, 220, 220 },
        { "ghostwhite",                 248, 248, 255 },
        { "gold",                       255, 215,   0 },
        { "goldenrod",                  218, 165,  32 },
        { "gray",                       128, 128, 128 },
        { "grey",                       128, 128, 128 },
        { "green",                        0, 128,   0 },
        { "greenyellow",                173, 255,  47 },
        { "honeydew",                   240, 255, 240 },
        { "hotpink",                    255, 105, 180 },
        { "indianred",                  205,  92,  92 },
        { "indigo",                      75,   0, 130 },
        { "ivory",                      255, 255, 240 },
        { "khaki",                      240, 230, 140 },
        { "lavender",                   230, 230, 250 },
        { "lavenderblush",              255, 240, 245 },
        { "lawngreen",                  124, 252,   0 },
        { "lemonchiffon",               255, 250, 205 },
        { "lightblue",                  173, 216, 230 },
        { "lightcoral",                 240, 128, 128 },
        { "lightcyan",                  224, 255, 255 },
        { "lightgoldenrodyellow",       250, 250, 210 },
        { "lightgray",                  211, 211, 211 },
        { "lightgreen",                 144, 238, 144 },
        { "lightgrey",                  211, 211, 211 },
        { "lightpink",                  255, 182, 193 },
        { "lightsalmon",                255, 160, 122 },
        { "lightseagreen",               32, 178, 170 },
        { "lightskyblue",               135, 206, 250 },
        { "lightslategray",             119, 136, 153 },
        { "lightslategrey",             119, 136, 153 },
        { "lightsteelblue",             176, 196, 222 },
        { "lightyellow",                255, 255, 224 },
        { "lime",                         0, 255,   0 },
        { "limegreen",                   50, 205,  50 },
        { "linen",                      250, 240, 230 },
        { "magenta",                    255,   0, 255 },
        { "maroon",                     128,   0,   0 },
        { "mediumaquamarine",           102, 205, 170 },
        { "mediumblue",                   0,   0, 205 },
        { "mediumorchid",               186,  85, 211 },
        { "mediumpurple",               147, 112, 219 },
        { "mediumseagreen",              60, 179, 113 },
        { "mediumslateblue",            123, 104, 238 },
        { "mediumspringgreen",            0, 250, 154 },
        { "mediumturquoise",             72, 209, 204 },
        { "mediumvioletred",            199,  21, 133 },
        { "midnightblue",                25,  25, 112 },
        { "mintcream",                  245, 255, 250 },
        { "mistyrose",                  255, 228, 225 },
        { "moccasin",                   255, 228, 181 },
        { "navajowhite",                255, 222, 173 },
        { "navy",                         0,   0, 128 },
        { "oldlace",                    253, 245, 230 },
        { "olive",                      128, 128,   0 },
        { "olivedrab",                  107, 142,  35 },
        { "orange",                     255, 165,   0 },
        { "orangered",                  255,  69,   0 },
        { "orchid",                     218, 112, 214 },
        { "palegoldenrod",              238, 232, 170 },
        { "palegreen",                  152, 251, 152 },
        { "paleturquoise",              175, 238, 238 },
        { "palevioletred",              219, 112, 147 },
        { "papayawhip",                 255, 239, 213 },
        { "peachpuff",                  255, 218, 185 },
        { "peru",                       205, 133,  63 },
        { "pink",                       255, 192, 203 },
        { "plum",                       221, 160, 221 },
        { "powderblue",                 176, 224, 230 },
        { "purple",                     128,   0, 128 },
        { "red",                        255,   0,   0 },
        { "rosybrown",                  188, 143, 143 },
        { "royalblue",                   65, 105, 225 },
        { "saddlebrown",                139,  69,  19 },
        { "salmon",                     250, 128, 114 },
        { "sandybrown",                 244, 164,  96 },
        { "seagreen",                    46, 139,  87 },
        { "seashell",                   255, 245, 238 },
        { "sienna",                     160,  82,  45 },
        { "silver",                     192, 192, 192 },
        { "skyblue",                    135, 206, 235 },
        { "slateblue",                  106,  90, 205 },
        { "slategray",                  112, 128, 144 },
        { "slategrey",                  112, 128, 144 },
        { "snow",                       255, 250, 250 },
        { "springgreen",                  0, 255, 127 },
        { "steelblue",                   70, 130, 180 },
        { "tan",                        210, 180, 140 },
        { "teal",                         0, 128, 128 },
        { "thistle",                    216, 191, 216 },
        { "tomato",                     255,  99,  71 },
        { "turquoise",                   64, 224, 208 },
        { "violet",                     238, 130, 238 },
        { "wheat",                      245, 222, 179 },
        { "white",                      255, 255, 255 },
        { "whitesmoke",                 245, 245, 245 },
        { "yellow",                     255, 255,   0 },
        { "yellowgreen",                154, 205,  50 }
};


BOOL DLL_CALLCONV
FreeImage_LookupSVGColor(const char *szColor, BYTE *nRed, BYTE *nGreen, BYTE *nBlue) {
    int i;

    // lookup color
    i = FreeImage_LookupNamedColor(szColor, SVGColorMap, sizeof(SVGColorMap)/sizeof(SVGColorMap[0]));
    if (i >= 0) {
        *nRed   = SVGColorMap[i].r;
        *nGreen = SVGColorMap[i].g;
        *nBlue  = SVGColorMap[i].b;
        return TRUE;
    }

    // not found, try for grey color with attached percent value
    if ( (szColor[0] == 'g' || szColor[0] == 'G') &&
         (szColor[1] == 'r' || szColor[1] == 'R') &&
         (szColor[2] == 'e' || szColor[2] == 'E' || szColor[2] == 'a' || szColor[2] == 'A' ) &&
         (szColor[3] == 'y' || szColor[3] == 'Y' ) )  {

        // grey<num>, or gray<num>, num 1...100
        i = strtol(szColor+4, NULL, 10);
        *nRed   = (BYTE)(255.0/100.0 * i);
        *nGreen = *nRed;
        *nBlue  = *nRed;
        return TRUE;
    }

    // not found at all
    *nRed   = 0;
    *nGreen = 0;
    *nBlue  = 0;

    return FALSE;
}

