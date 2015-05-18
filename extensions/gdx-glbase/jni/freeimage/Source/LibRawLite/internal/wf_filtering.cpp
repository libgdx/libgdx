/* 
   WF debanding code
   Copyright 2011 by Yan Vladimirovich

   Used in LibRaw with author permission.

LibRaw is free software; you can redistribute it and/or modify
it under the terms of the one of three licenses as you choose:

1. GNU LESSER GENERAL PUBLIC LICENSE version 2.1
   (See file LICENSE.LGPL provided in LibRaw distribution archive for details).

2. COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (CDDL) Version 1.0
   (See file LICENSE.CDDL provided in LibRaw distribution archive for details).

3. LibRaw Software License 27032010
   (See file LICENSE.LibRaw.pdf provided in LibRaw distribution archive for details).

   This file is generated from Dave Coffin's dcraw.c
   dcraw.c -- Dave Coffin's raw photo decoder
   Copyright 1997-2010 by Dave Coffin, dcoffin a cybercom o net

   Look into dcraw homepage (probably http://cybercom.net/~dcoffin/dcraw/)
   for more information
*/


#define P1 imgdata.idata
#define S imgdata.sizes
#define O imgdata.params
#define C imgdata.color
#define T imgdata.thumbnail
#define IO libraw_internal_data.internal_output_params
#define ID libraw_internal_data.internal_data


int LibRaw::wf_remove_banding()
{
#define WF_IMGMODE_BAYER4PLANE 4
#define WF_IMGMODE_BAYER1PLANE 1

#define WF_GREENMODE_IND   0
#define WF_GREENMODE_GX_XG 1
#define WF_GREENMODE_XG_GX 2

#define WF_DEBANDING_OK          0
#define WF_DEBANDING_NOTBAYER2X2 1
#define WF_DEBANDING_TOOSMALL    2

#define WF_GAUSS_PIRAMID_SIZE 4

#define WF_MAXTRESHOLD 65536

#define WF_BAYERSRC(row, col, c) ((ushort(*)[4])imgdata.image)[((row) >> IO.shrink)*S.iwidth + ((col) >> IO.shrink)][c] 
#define WF_BAYERGAU(l, row, col) (gauss_pyramid[l])[((row) >> IO.shrink)*S.iwidth + ((col) >> IO.shrink)] 
#define WF_BAYERDFG(l, row, col) (difwg_pyramid[l])[((row) >> IO.shrink)*S.iwidth + ((col) >> IO.shrink)] 
	
#define MIN(a,b) ((a) < (b) ? (a) : (b))
#define MAX(a,b) ((a) > (b) ? (a) : (b))

#define WF_i_1TO4 for(int i=0; i<4; i++)

	// too small?

	if (S.width<128 || S.height<128)
		return WF_DEBANDING_TOOSMALL;

	// is 2x2 bayer? 

	int bayer2x2flag=-1;

	for(int row_shift=0; row_shift<=8; row_shift+=2)
	{
		for(int col_shift=0; col_shift<=8; col_shift+=2)
		{
			if ((FC(0,0)!=FC(row_shift,   col_shift))   ||
				(FC(1,0)!=FC(row_shift+1, col_shift))   ||
				(FC(0,1)!=FC(row_shift,   col_shift+1)) ||
				(FC(1,1)!=FC(row_shift+1, col_shift+1)))
			{
				bayer2x2flag=0;
			}
		}
	}

	if (bayer2x2flag==0)
		return WF_DEBANDING_NOTBAYER2X2;

	int    x_green_flag = -1;

	int    width_d2,    height_d2;
	int    width_p1_d2, height_p1_d2;

	width_d2  = S.width/2;
	height_d2 = S.height/2;
	
	width_p1_d2  = (S.width+1)/2;
	height_p1_d2 = (S.height+1)/2;

	ushort  val_max_c[4]={0,0,0,0};
	ushort  val_max;

	ushort  dummy_pixel=0;
	ushort *dummy_line;

	dummy_line = (ushort*)calloc(S.width, sizeof(ushort)*4);

	for(int i=0; i<S.width*4; i++)
		dummy_line[i]=0;

	// Searching max value for increasing bit-depth

	for(int row_d2=0; row_d2<height_p1_d2; row_d2++)
	{
		int     row, row_p1;
		ushort *src[4];
		ushort *src_first, *src_plast, *src_last;
		
		row    = row_d2*2;
		row_p1 = row+1;

		WF_i_1TO4 src[i] = &WF_BAYERSRC((i<2)?row:row_p1, i&1, FC((i<2)?row:row_p1, i&1));

		if (row_p1==S.height)
			src[2]=src[3]=dummy_line;

		src_first   = &WF_BAYERSRC(row,   0,               FC(row,   0));
		src_plast   = &WF_BAYERSRC(row,   width_d2*2-2,    FC(row,   0));
		src_last    = &WF_BAYERSRC(row,   width_p1_d2*2-2, FC(row,   0));

		do
		{
			// Do

			WF_i_1TO4 val_max_c[i]=MAX(val_max_c[i], *src[i]);

			// Next 4 pixel or exit

			if     (src[0]<src_plast)
			{
				WF_i_1TO4 src[i]+=8;
			}
			else if(src[0]>src_first && src[0]<src_last)
			{
				WF_i_1TO4 src[i]=i&1?&dummy_pixel:src[i]+8;
			}
			else break;

		}
		while(1);
	}

	val_max=MAX(MAX(val_max_c[0], val_max_c[1]), MAX(val_max_c[2], val_max_c[3]));
	
	// end of searching max value

	if (val_max==0)
		return WF_DEBANDING_OK;
		
	int data_shift;
	int data_mult;
	int val_max_s;

	data_shift = 15;
	val_max_s  = val_max;

	if (val_max_s >= (1 << 8)) { val_max_s >>= 8; data_shift -=  8; }
	if (val_max_s >= (1 << 4)) { val_max_s >>= 4; data_shift -=  4; }
	if (val_max_s >= (1 << 2)) { val_max_s >>= 2; data_shift -=  2; }
	if (val_max_s >= (1 << 1)) {                  data_shift -=  1; }
  
	data_mult = 1<<data_shift;
	val_max <<= data_shift;
		
	// Bit shift

	for(int row_d2=0; row_d2<height_p1_d2; row_d2++)
	{
		int     row, row_p1;
		ushort *src[4];
		ushort *src_first, *src_plast, *src_last;
		
		row    = row_d2*2;
		row_p1 = row+1;

		WF_i_1TO4 src[i] = &WF_BAYERSRC((i<2)?row:row_p1, i&1, FC((i<2)?row:row_p1, i&1));

		if (row_p1==S.height)
			src[2]=src[3]=dummy_line;

		src_first   = &WF_BAYERSRC(row,   0,               FC(row,   0));
		src_plast   = &WF_BAYERSRC(row,   width_d2*2-2,    FC(row,   0));
		src_last    = &WF_BAYERSRC(row,   width_p1_d2*2-2, FC(row,   0));

		do
		{
			// Do

			WF_i_1TO4 (*src[i])<<=data_shift;

			// Next 4 pixel or exit

			if     (src[0]<src_plast)
			{
				WF_i_1TO4 src[i]+=8;
			}
			else if(src[0]>src_first && src[0]<src_last)
			{
				WF_i_1TO4 src[i]=i&1?&dummy_pixel:src[i]+8;
			}
			else break;

		}
		while(1);
	}

	ushort *gauss_pyramid[WF_GAUSS_PIRAMID_SIZE];
	ushort *difwg_pyramid[WF_GAUSS_PIRAMID_SIZE];

	for(int i=0; i<WF_GAUSS_PIRAMID_SIZE; i++)
	{
		gauss_pyramid[i] = (ushort*)calloc(S.width*S.height, sizeof(ushort));
		difwg_pyramid[i] = (ushort*)calloc(S.width*S.height, sizeof(ushort));
	}

	int radius3x3 [4]={3,  3,  3,  0}; // as gau r=24
	int radius3x14[4]={14, 14, 14, 0}; // as gau r=420
	int radius3x45[4]={45, 45, 45, 0}; // as gau r=4140

	// Making 4-level gaussian pyramid

	if (x_green_flag)
	{
		wf_bayer4_green_blur   (0,          imgdata.image,    WF_IMGMODE_BAYER4PLANE, gauss_pyramid[0], WF_IMGMODE_BAYER1PLANE);
		wf_bayer4_igauss_filter(1,          gauss_pyramid[0], WF_IMGMODE_BAYER1PLANE, gauss_pyramid[0], WF_IMGMODE_BAYER1PLANE);
	}
	else
	{
		wf_bayer4_igauss_filter(1,          imgdata.image,    WF_IMGMODE_BAYER4PLANE, gauss_pyramid[0], WF_IMGMODE_BAYER1PLANE);
	}

	wf_bayer4_block_filter (radius3x3,  gauss_pyramid[0], WF_IMGMODE_BAYER1PLANE, gauss_pyramid[1], WF_IMGMODE_BAYER1PLANE); // as gau r=24
	wf_bayer4_block_filter (radius3x14, gauss_pyramid[0], WF_IMGMODE_BAYER1PLANE, gauss_pyramid[2], WF_IMGMODE_BAYER1PLANE); // as gau r=420
	wf_bayer4_block_filter (radius3x45, gauss_pyramid[0], WF_IMGMODE_BAYER1PLANE, gauss_pyramid[3], WF_IMGMODE_BAYER1PLANE); // as gau r=4140 

	
	// Energy multiplyers for laplasyan pyramid

	float dfg_mult[WF_GAUSS_PIRAMID_SIZE]={1.560976, 8.196011, 180.413773, 3601.427246/3.0};

/*	dif_mult[0]=1.0/wf_filter_energy(0, 0,   0,    1);
	dif_mult[1]=1.0/wf_filter_energy(0, 1,   0,   24);
	dif_mult[2]=1.0/wf_filter_energy(0, 24,  0,  420);
	dif_mult[3]=1.0/wf_filter_energy(0, 420, 0, 4140);*/

	float dfg_mulg[WF_GAUSS_PIRAMID_SIZE]={1.235223, 19.813868, 365.148407, 7208.362793/3.0};

/*	dif_mulg[0]=1.0/wf_filter_energy(0, 0,    1,    1);
	dif_mulg[1]=1.0/wf_filter_energy(1, 1,    1,   24);
	dif_mulg[2]=1.0/wf_filter_energy(1, 24,   1,  420);
	dif_mulg[3]=1.0/wf_filter_energy(1, 420,  1, 4140);*/

	float    dfg_mlcc[WF_GAUSS_PIRAMID_SIZE][4];
	long int dfg_dmax[WF_GAUSS_PIRAMID_SIZE][4];

	int green_mode;

	if      ( x_green_flag && (imgdata.idata.cdesc[FC(0, 0)] == imgdata.idata.cdesc[FC(1, 1)]) )
		green_mode = WF_GREENMODE_GX_XG;
	else if ( x_green_flag && (imgdata.idata.cdesc[FC(0, 1)] == imgdata.idata.cdesc[FC(1, 0)]) )
		green_mode = WF_GREENMODE_XG_GX;
	else
		green_mode = WF_GREENMODE_IND;
	
	for(int l=0; l<WF_GAUSS_PIRAMID_SIZE; l++)
	{
		switch (green_mode)
		{
			case WF_GREENMODE_GX_XG:

				dfg_mlcc[l][0]=dfg_mlcc[l][3]=dfg_mulg[l];
				dfg_dmax[l][0]=dfg_dmax[l][3]=65535/dfg_mulg[l];

				dfg_mlcc[l][1]=dfg_mlcc[l][2]=dfg_mult[l];
				dfg_dmax[l][1]=dfg_dmax[l][2]=65535/dfg_mult[l];
				
				break;

			case WF_GREENMODE_XG_GX:

				dfg_mlcc[l][1]=dfg_mlcc[l][2]=dfg_mulg[l];
				dfg_dmax[l][1]=dfg_dmax[l][2]=65535/dfg_mulg[l];

				dfg_mlcc[l][0]=dfg_mlcc[l][3]=dfg_mult[l];
				dfg_dmax[l][0]=dfg_dmax[l][3]=65535/dfg_mult[l];
				
				break;
			
			case WF_GREENMODE_IND:

				dfg_mlcc[l][0]=dfg_mlcc[l][1]=dfg_mlcc[l][2]=dfg_mlcc[l][3]=dfg_mult[l];
				dfg_dmax[l][0]=dfg_dmax[l][1]=dfg_dmax[l][2]=dfg_dmax[l][3]=65535/dfg_mult[l];
				
				break;
		}
	}

	// laplasyan energy

	for(int row_d2=0; row_d2<height_p1_d2; row_d2++)
	{
		int     row, row_p1;
		ushort *src[4];

		ushort *gau[WF_GAUSS_PIRAMID_SIZE][4];
		ushort *dfg[WF_GAUSS_PIRAMID_SIZE][4];

		row    = row_d2*2;
		row_p1 = row+1;

		WF_i_1TO4 src[i] = &WF_BAYERSRC((i<2)?row:row_p1, i&1, FC((i<2)?row:row_p1, i&1));

		if (row_p1==S.height)
			src[2]=src[3]=dummy_line;

		for(int l=0; l<WF_GAUSS_PIRAMID_SIZE; l++)
		{
			WF_i_1TO4 gau[l][i] = &WF_BAYERGAU(l, (i<2)?row:row_p1, i&1);

			WF_i_1TO4 dfg[l][i] = &WF_BAYERDFG(l, (i<2)?row:row_p1, i&1);

			if ((row+1)==S.height)
				dfg[l][2]=dfg[l][3]=gau[l][2]=gau[l][3]=dummy_line;
		}

		ushort *src_first, *src_last, *src_last2;

		src_first   = &WF_BAYERSRC(row,   0,               FC(row,   0));
		src_last    = &WF_BAYERSRC(row,   width_d2*2-2,    FC(row,   0));
		src_last2   = &WF_BAYERSRC(row,   width_p1_d2*2-2, FC(row,   0));

		do
		{
			long int val_gau[4];
			long int val_dif[4];
			long int val_src[4];

			WF_i_1TO4 val_src[i]=*src[i];

			for(int l=0; l<WF_GAUSS_PIRAMID_SIZE; l++)
			{
				WF_i_1TO4 val_gau[i]=*gau[l][i];
				WF_i_1TO4 val_dif[i]=val_src[i]-val_gau[i];
				WF_i_1TO4 val_src[i]=val_gau[i];
				WF_i_1TO4 val_dif[i]*=val_dif[i];

				WF_i_1TO4
					if(val_dif[i]<dfg_dmax[l][i])
					{
						val_dif[i]*=dfg_mlcc[l][i];
						*dfg[l][i] =val_dif[i];
					}
					else
					{
						*dfg[l][i]=65535;
					}
			}

			// Next 4 pixel or exit

			if     (src[0]<src_last)
			{
				WF_i_1TO4 src[i]+=8;

				for (int l=0; l<WF_GAUSS_PIRAMID_SIZE; l++)
					WF_i_1TO4 gau[l][i]+=2;

				for (int l=0; l<WF_GAUSS_PIRAMID_SIZE; l++)
					WF_i_1TO4 dfg[l][i]+=2;
			}
			else if(src[0]>src_first && src[0]<src_last2)
			{
				WF_i_1TO4 src[i]=i&1?&dummy_pixel:src[i]+8;

				for (int l=0; l<WF_GAUSS_PIRAMID_SIZE; l++)
					WF_i_1TO4 gau[l][i]=i&1?&dummy_pixel:gau[l][i]+2;

				for (int l=0; l<WF_GAUSS_PIRAMID_SIZE; l++)
					WF_i_1TO4 dfg[l][i]=i&1?&dummy_pixel:dfg[l][i]+2;
			}
			else break;
		}
		while(1);
	}
	
	int radius2x32 [3]={32, 32, 0};
	int radius2x56 [3]={56, 56, 0};
	int radius2x90 [3]={90, 90, 0};
	int radius2x104[3]={104, 104, 0};

	if (x_green_flag)
	{
		for(int i=0;i<4;i++)
			wf_bayer4_green_blur   (0,           difwg_pyramid[i], WF_IMGMODE_BAYER1PLANE, difwg_pyramid[i], WF_IMGMODE_BAYER1PLANE);
	}

	wf_bayer4_block_filter (radius2x32,  difwg_pyramid[0], WF_IMGMODE_BAYER1PLANE, difwg_pyramid[0], WF_IMGMODE_BAYER1PLANE);
	wf_bayer4_block_filter (radius2x56,  difwg_pyramid[1], WF_IMGMODE_BAYER1PLANE, difwg_pyramid[1], WF_IMGMODE_BAYER1PLANE);
	wf_bayer4_block_filter (radius2x90,  difwg_pyramid[2], WF_IMGMODE_BAYER1PLANE, difwg_pyramid[2], WF_IMGMODE_BAYER1PLANE);
	wf_bayer4_block_filter (radius2x104, difwg_pyramid[3], WF_IMGMODE_BAYER1PLANE, difwg_pyramid[3], WF_IMGMODE_BAYER1PLANE);

	float (*banding_col)[4];
	float (*banding_row)[4];
	float (*banding_col_count)[4];
	float (*banding_row_count)[4];

	banding_col       = (float(*)[4])calloc(height_p1_d2, sizeof(float)*4);
	banding_col_count = (float(*)[4])calloc(height_p1_d2, sizeof(float)*4);

	banding_row       = (float(*)[4])calloc(width_p1_d2, sizeof(float)*4);
	banding_row_count = (float(*)[4])calloc(width_p1_d2, sizeof(float)*4);

	for(int row_d2=0; row_d2<height_p1_d2; row_d2++)
		WF_i_1TO4 banding_col[row_d2][i]=banding_col_count[row_d2][i]=0;

	for(int col_d2=0; col_d2<width_p1_d2; col_d2++)
		WF_i_1TO4 banding_row[col_d2][i]=banding_row_count[col_d2][i]=0;

	long int val_accepted;
	float    treshold[4];

	WF_i_1TO4 treshold[i]=imgdata.params.wf_deband_treshold[FC(i>>1,i&1)];

	val_accepted = val_max-3*MAX(MAX(treshold[0],treshold[1]),MAX(treshold[2],treshold[3]));

	float (*tr_weight)[4];

	tr_weight=(float(*)[4])calloc(WF_MAXTRESHOLD*4, sizeof(float));

	WF_i_1TO4 treshold[i]*=data_mult;

	for(int v=0; v<WF_MAXTRESHOLD; v++)
	{
		for(int i=0; i<4; i++)
		{
			if (v<treshold[i]*treshold[i])
				tr_weight[v][i] = 1.0;
			else if (v*5<6*treshold[i]*treshold[i])
				tr_weight[v][i] = 6.0-5.0*float(v)/(treshold[i]*treshold[i]);
			else
				tr_weight[v][i] = 0.0;
		}
	}

	for(int row_d2=0; row_d2<height_p1_d2; row_d2++)
	{
		int     row, row_p1;
		ushort *src[4];

		ushort *gau[WF_GAUSS_PIRAMID_SIZE][4];
		ushort *dfg[WF_GAUSS_PIRAMID_SIZE][4];

		row    = row_d2*2;
		row_p1 = row+1;

		WF_i_1TO4 src[i] = &WF_BAYERSRC((i<2)?row:row_p1, i&1, FC((i<2)?row:row_p1, i&1));

		if (row_p1==S.height)
			src[2]=src[3]=dummy_line;

		for(int l=0; l<WF_GAUSS_PIRAMID_SIZE; l++)
		{
			WF_i_1TO4 gau[l][i] = &WF_BAYERGAU(l, (i<2)?row:row_p1, i&1);

			WF_i_1TO4 dfg[l][i] = &WF_BAYERDFG(l, (i<2)?row:row_p1, i&1);

			if (row_p1==S.height)
				dfg[l][2]=dfg[l][3]=gau[l][2]=gau[l][3]=dummy_line;
		}

		ushort *src_first, *src_last, *src_last2;

		src_first   = &WF_BAYERSRC(row,   0,               FC(row,   0));
		src_last    = &WF_BAYERSRC(row,   width_d2*2-2,    FC(row,   0));
		src_last2   = &WF_BAYERSRC(row,   width_p1_d2*2-2, FC(row,   0));

		int col_d2 = 0;

		do
		{
			float val_src[4];

			float bsum[4]={0,0,0,0};
			float wsum[4]={0,0,0,0};

			WF_i_1TO4 val_src[i]=*src[i];

			for(int l=0; l<WF_GAUSS_PIRAMID_SIZE; l++)
			{
				float val_dif[4];
				float val_gau[4];
				float wght[4];

				WF_i_1TO4 val_gau[i] =  *gau[l][i];
				WF_i_1TO4 val_dif[i] =  val_src[i]-val_gau[i];
				WF_i_1TO4 val_src[i] =  val_gau[i];

				WF_i_1TO4 wght[i]    =  tr_weight[*dfg[l][i]][i];
				WF_i_1TO4 wsum[i]    += wght[i];
				WF_i_1TO4 bsum[i]    += wght[i]*val_dif[i];
			}

			//WF_i_1TO4 *src[i]=bsum[i];

			WF_i_1TO4 wsum[i]*=wsum[i];

			WF_i_1TO4 banding_col      [row_d2][i] += bsum[i]*wsum[i];
			WF_i_1TO4 banding_col_count[row_d2][i] += wsum[i];

			WF_i_1TO4 banding_row      [col_d2][i] += bsum[i]*wsum[i];
			WF_i_1TO4 banding_row_count[col_d2][i] += wsum[i];

			// Next 4 pixel or exit

			if     (src[0]<src_last)
			{
				WF_i_1TO4 src[i]+=8;

				for (int l=0; l<WF_GAUSS_PIRAMID_SIZE; l++)
					WF_i_1TO4 gau[l][i]+=2;

				for (int l=0; l<WF_GAUSS_PIRAMID_SIZE; l++)
					WF_i_1TO4 dfg[l][i]+=2;
			}
			else if(src[0]>src_first && src[0]<src_last2)
			{
				WF_i_1TO4 src[i]=i&1?&dummy_pixel:src[i]+8;

				for (int l=0; l<WF_GAUSS_PIRAMID_SIZE; l++)
					WF_i_1TO4 gau[l][i]=i&1?&dummy_pixel:gau[l][i]+2;

				for (int l=0; l<WF_GAUSS_PIRAMID_SIZE; l++)
					WF_i_1TO4 dfg[l][i]=i&1?&dummy_pixel:dfg[l][i]+2;
			}
			else break;

			col_d2++;
		}
		while(1);
	}

	float bsum[4], bmean[4];

	int (*banding_col_i)[4];
	int (*banding_row_i)[4];

	banding_col_i = (int(*)[4])calloc(height_p1_d2, sizeof(int)*4);
	banding_row_i = (int(*)[4])calloc(width_p1_d2,  sizeof(int)*4);
	
	// cols

	WF_i_1TO4 bsum[i]=bmean[i]=0;

	for(int row_d2=0; row_d2<height_p1_d2; row_d2++)
		for(int i=0; i<4; i++)
		{
			if (banding_col_count[row_d2][i]>0)
			{
				banding_col[row_d2][i]=banding_col[row_d2][i]/banding_col_count[row_d2][i];
				bsum[i]+=banding_col[row_d2][i];
			}
		}

	WF_i_1TO4 bmean[i]=bsum[i]/(i<2?height_d2:height_p1_d2);
		
	for(int row_d2=0; row_d2<height_p1_d2; row_d2++)
		for(int i=0; i<4; i++)
			banding_col_i[row_d2][i]=int(banding_col[row_d2][i]-bmean[i]);

	// rows

	WF_i_1TO4 bsum[i]=bmean[i]=0;

	for(int col_d2=0; col_d2<width_p1_d2; col_d2++)
		for(int i=0; i<4; i++)
		{
			if (banding_row_count[col_d2][i]>0)
			{
				banding_row[col_d2][i]=(banding_row[col_d2][i]/banding_row_count[col_d2][i]);
				bsum[i]+=banding_row[col_d2][i];
			}
		}

	WF_i_1TO4 bmean[i]=bsum[i]/(i<2?width_d2:width_p1_d2);

	for(int col_d2=0; col_d2<width_p1_d2; col_d2++)
		for(int i=0; i<4; i++)
			if (banding_row_count[col_d2][i]>0)
				banding_row_i[col_d2][i]=int(banding_row[col_d2][i]-bmean[i]);

	for(int row_d2=0; row_d2<height_p1_d2; row_d2++)
	{
		int     row, row_p1;
		ushort *src[4];
		ushort *src_first, *src_plast, *src_last;
		
		row    = row_d2*2;
		row_p1 = row+1;

		WF_i_1TO4 src[i] = &WF_BAYERSRC((i<2)?row:row_p1, i&1, FC((i<2)?row:row_p1, i&1));

		if (row_p1==S.height)
			src[2]=src[3]=dummy_line;

		src_first   = &WF_BAYERSRC(row,   0,               FC(row,   0));
		src_plast   = &WF_BAYERSRC(row,   width_d2*2-2,    FC(row,   0));
		src_last    = &WF_BAYERSRC(row,   width_p1_d2*2-2, FC(row,   0));

		int col_d2=0;

		do
		{
			// Do

			int val_new[4];

			WF_i_1TO4 val_new[i]=*src[i];
			WF_i_1TO4 val_new[i]-=banding_col_i[row_d2][i];
			WF_i_1TO4 val_new[i]-=banding_row_i[col_d2][i];

			for(int i=0; i<4; i++)
			{
				if (*src[i]>=val_accepted)
				{
					val_new[i]=*src[i]>>data_shift;
				}
				else
				{
					if      (val_new[i]>val_max)
						val_new[i]=val_max;
					else if (val_new[i]<0)
						val_new[i]=0;

					val_new[i]>>=data_shift;
				}
			}

			WF_i_1TO4 *src[i]=val_new[i];

			// Next 4 pixel or exit

			if     (src[0]<src_plast)
			{
				WF_i_1TO4 src[i]+=8;
			}
			else if(src[0]>src_first && src[0]<src_last)
			{
				WF_i_1TO4 src[i]=i&1?&dummy_pixel:src[i]+8;
			}
			else break;

			col_d2++;
		}
		while(1);
	}

	free(banding_col_i);
	free(banding_row_i);

	free(tr_weight);
	
	free(banding_col);
	free(banding_col_count);

	free(banding_row);
	free(banding_row_count);

	for(int i=0; i<WF_GAUSS_PIRAMID_SIZE; i++)
	{
		free(gauss_pyramid[i]);
		free(difwg_pyramid[i]);
	}

	free(dummy_line);
        return WF_DEBANDING_OK;
}

double LibRaw::wf_filter_energy(int r1_greenmode, int r1, int r2_greenmode, int r2)
{
	/* 
		This function caclulates energy of laplasyan piramid level.
		Laplasyan level is difference between two 2D gaussian (exactly, binominal) convolutions with radius r1 and r2
		Convolution is done on bayer data, 4 channels, and if (greenmode), additive on green channel.
	
		Not optimized, because now it's used only for precalculations.
	*/


#define WF_MAXFILTERSIZE 10000

	int rmin, rmax;
	int rmin_greenmode, rmax_greenmode;

	if (r1>r2)
	{
		rmax=r1;
		rmin=r2;
		rmax_greenmode=r1_greenmode;
		rmin_greenmode=r2_greenmode;
	}
	else
	{
		rmax=r2;
		rmin=r1;
		rmax_greenmode=r2_greenmode;
		rmin_greenmode=r1_greenmode;
	}


	int rmin_x2_p1, rmax_x2_p1;
	rmin_x2_p1=rmin*2+1;
	rmax_x2_p1=rmax*2+1;

	double gau_kernel_rmin[WF_MAXFILTERSIZE];
	double gau_kernel_rmax[WF_MAXFILTERSIZE];

	for(int i=0; i<rmax_x2_p1; i++)
		gau_kernel_rmin[i]=0;

	gau_kernel_rmin[1]=1.0;
				
	for(int i=2; i<=rmin_x2_p1; i++)
	{
		for(int j=i; j>0; j--)
			gau_kernel_rmin[j]=0.5*(gau_kernel_rmin[j]+gau_kernel_rmin[j-1]);
	}

	for(int i=0; i<=rmax_x2_p1; i++)
		gau_kernel_rmax[i]=gau_kernel_rmin[i];

	for(int i=rmin_x2_p1+1; i<=rmax_x2_p1; i++)
	{
		for(int j=i; j>0; j--)
			gau_kernel_rmax[j]=0.5*(gau_kernel_rmax[j]+gau_kernel_rmax[j-1]);
	}

	double wmin_sum, wmax_sum, energy_sum;

	wmin_sum=0;
	wmax_sum=0;
	energy_sum=0;

	for(int row=-rmax*2-1; row<=rmax*2+1; row++)
	{
		for(int col=-rmax*2-1; col<=rmax*2+1; col++)
		{
			double wght_rmax=0;
			double wght_rmin=0;

#define WF_WMAX(row, col) (((abs(row)<=rmax*2)&&(abs(col)<=rmax*2))?gau_kernel_rmax[abs(row)/2+rmax+1]*gau_kernel_rmax[abs(col)/2+rmax+1]:0)
#define WF_WMIN(row, col) (((abs(row)<=rmin*2)&&(abs(col)<=rmin*2))?gau_kernel_rmin[abs(row)/2+rmin+1]*gau_kernel_rmin[abs(col)/2+rmin+1]:0)

			if ( ((row&1)==0) && ((col&1)==0))
			{
				wght_rmax = WF_WMAX(row, col);
				wght_rmin = WF_WMIN(row, col);
			}

			if (rmax_greenmode)
			{
				if ( ((row&1)==0) && ((col&1)==0))
					wght_rmax = 0.5*wght_rmax;
				else if ( ((row&1)==1) && ((col&1)==1))
				{
					wght_rmax = 0.125*(WF_WMAX(row-1, col-1)+WF_WMAX(row-1, col+1)+WF_WMAX(row+1, col-1)+WF_WMAX(row+1, col+1));
				}
			}

			if (rmin_greenmode)
			{
				if ( ((row&1)==0) && ((col&1)==0))
					wght_rmin = 0.5*wght_rmin;
				else if ( ((row&1)==1) && ((col&1)==1))
				{
					wght_rmin = 0.125*(WF_WMIN(row-1, col-1)+WF_WMIN(row-1, col+1)+WF_WMIN(row+1, col-1)+WF_WMIN(row+1, col+1));
				}
			}

			wmin_sum+=wght_rmin;
			wmax_sum+=wght_rmax;

			energy_sum+=(wght_rmax-wght_rmin)*(wght_rmax-wght_rmin);
		}
		
	}

	return energy_sum;
}

void LibRaw::wf_bayer4_green_blur(int mode, void* src_image, int src_imgmode, void* dst_image, int dst_imgmode)
{
	/* 
		This function filters green (or any "diagonal") channel of bayer4 data with "X" kernel,

		1 1
		 4
		1 1
	*/

#define WF_BAYERSRC4(row, col, c) ((ushort(*)[4])src_image)[((row) >> IO.shrink)*S.iwidth + ((col) >> IO.shrink)][c] 
#define WF_BAYERSRC1(row, col)    ((ushort*)src_image)     [((row) >> IO.shrink)*S.iwidth + ((col) >> IO.shrink)] 
#define WF_BAYERDST4(row, col, c) ((ushort(*)[4])dst_image)[((row) >> IO.shrink)*S.iwidth + ((col) >> IO.shrink)][c] 
#define WF_BAYERDST1(row, col)    ((ushort*)dst_image)     [((row) >> IO.shrink)*S.iwidth + ((col) >> IO.shrink)]

	int green_mode;

	if ( imgdata.idata.cdesc[FC(0, 0)] == imgdata.idata.cdesc[FC(1, 1)] )
		green_mode = WF_GREENMODE_GX_XG;
	else if ( imgdata.idata.cdesc[FC(0, 1)] == imgdata.idata.cdesc[FC(1, 0)] )
		green_mode = WF_GREENMODE_XG_GX;
	else
		green_mode = WF_GREENMODE_IND;

	int src_h_shift, dst_h_shift, src_h_shift_x2;

	if      (src_imgmode == WF_IMGMODE_BAYER1PLANE)
		src_h_shift = 2 >> IO.shrink;
	else if (src_imgmode == WF_IMGMODE_BAYER4PLANE)
		src_h_shift = 8 >> IO.shrink;

	src_h_shift_x2 = src_h_shift*2;

	if      (dst_imgmode == WF_IMGMODE_BAYER1PLANE)
		dst_h_shift = 2 >> IO.shrink;
	else if (dst_imgmode == WF_IMGMODE_BAYER4PLANE)
		dst_h_shift = 8 >> IO.shrink;

	int row, col;

	long int *line_filtered;

	line_filtered = (long int*) calloc(S.width, sizeof(*line_filtered));
	
	ushort *src, *src_c, *src_u1, *src_u2, *src_d1, *src_d2, *dst_c, *src_ca, *dst_ca, *dst_rb; 
	int start_col, start_col_left, row_up, row_dn;

	if ( green_mode != WF_GREENMODE_IND)
	{
		for(row=0; row<S.height; row++)
		{

			if (row == 0)
				row_up = 1;
			else
				row_up = row-1;

			if (row == S.height-1)
				row_dn = S.height-2;
			else
				row_dn = row+1;

			if ( green_mode == WF_GREENMODE_GX_XG )
				start_col = row & 1;
			else
				start_col = ( row+1 ) & 1;

			if ( start_col == 0 )
				start_col_left = 1;
			else
				start_col_left = 0;

			switch (src_imgmode)
			{
				case WF_IMGMODE_BAYER1PLANE:

					src_c  = &WF_BAYERSRC1(row,    start_col);
					src_u1 = &WF_BAYERSRC1(row_up, start_col_left);
					src_d1 = &WF_BAYERSRC1(row_dn, start_col_left);
					src_u2 = &WF_BAYERSRC1(row_up, start_col+1);
					src_d2 = &WF_BAYERSRC1(row_dn, start_col+1);

					break;

				case WF_IMGMODE_BAYER4PLANE:

					src_c  = &WF_BAYERSRC4(row,    start_col,      FC(row,   start_col));
					src_u1 = &WF_BAYERSRC4(row_up, start_col_left, FC(row_up, start_col_left));
					src_d1 = &WF_BAYERSRC4(row_dn, start_col_left, FC(row_dn, start_col_left));
					src_u2 = &WF_BAYERSRC4(row_up, start_col+1,    FC(row_up, start_col+1));
					src_d2 = &WF_BAYERSRC4(row_dn, start_col+1,    FC(row_dn, start_col+1));

					break;
			}

			long int sum_l1, sum_l2;

			sum_l1 = *src_u1 + *src_d1;
			sum_l2 = *src_u2 + *src_d2;			
			
			if (start_col == 0)
			{
				// Edges

				line_filtered[start_col] = sum_l1 + sum_l2 + (*src_c)*4;

				src_u2 += src_h_shift;
				src_d2 += src_h_shift;				

				sum_l2 = *src_u2 + *src_d2;

				src_c += src_h_shift;
				start_col=2;
			}

			int width_m_3 = S.width-3;

			// Main

			for (col=start_col; col<width_m_3; col+=2)
			{
				line_filtered[col] = sum_l1 + sum_l2 + 4*(*src_c);

				src_u1 += src_h_shift_x2;
				src_d1 += src_h_shift_x2;

				sum_l1 = *src_u1 + *src_d1;

				src_c += src_h_shift;
				col+=2;

				line_filtered[col] = sum_l1 + sum_l2 + 4*(*src_c);

				src_u2 += src_h_shift_x2;
				src_d2 += src_h_shift_x2;

				sum_l2 = *src_u2 + *src_d2; 

				src_c += src_h_shift;
			}
			
			// Right edge

			if      (col == S.width-1)
			{
				line_filtered[col] = 2*sum_l1 + 4*(*src_c);
			}
			else if (col == S.width-2)
			{
				line_filtered[col] = sum_l1 + sum_l2 + 4*(*src_c);
			}
			else if (col == S.width-3)
			{
				line_filtered[col] = sum_l1 + sum_l2 + 4*(*src_c);
				
				src_c += src_h_shift;
				col+=2;

				line_filtered[col] = 2*sum_l2 + 4*(*src_c);
			}

			if (row>0)
			{

				if ( green_mode == WF_GREENMODE_GX_XG )
					start_col = ( row+1 ) & 1;
				else
					start_col = row & 1;
				

				switch (dst_imgmode)
				{
					case WF_IMGMODE_BAYER1PLANE:
						dst_c  = &WF_BAYERDST1(row-1,    start_col);
						break;

					case WF_IMGMODE_BAYER4PLANE:
						dst_c  = &WF_BAYERDST4(row-1,    start_col, FC(row-1, start_col));
						break;
				}

				for (col=start_col;  col<S.width; col+=2)
				{
					*dst_c=(line_filtered[col])>>3;
					dst_c+=dst_h_shift;
				}

				if (src_image != dst_image)
				{
					// copy red or blue channel

					if ( green_mode == WF_GREENMODE_GX_XG )
						start_col = row & 1;
					else
						start_col = (row+1) & 1;
					
					switch (src_imgmode)
					{
						case WF_IMGMODE_BAYER1PLANE:
							src     = &WF_BAYERSRC1(row-1, start_col);
							break;

						case WF_IMGMODE_BAYER4PLANE:
							src     = &WF_BAYERSRC4(row-1, start_col, FC(row-1, start_col));
							break;
					}

					switch (dst_imgmode)
					{
						case WF_IMGMODE_BAYER1PLANE:
							dst_rb  = &WF_BAYERDST1(row-1, start_col);
							break;

						case WF_IMGMODE_BAYER4PLANE:
							dst_rb  = &WF_BAYERDST4(row-1, start_col, FC(row-1, start_col));
							break;
					}

					for (col=start_col;  col<S.width; col+=2)
					{
						*dst_rb=*src;
						src   +=src_h_shift;
						dst_rb+=dst_h_shift;
					}
				}
			}
		}

		if ( green_mode == WF_GREENMODE_GX_XG )
			start_col = ( row+1 ) & 1;
		else
			start_col = row & 1;
				

		switch (dst_imgmode)
		{
			case WF_IMGMODE_BAYER1PLANE:
				dst_c  = &WF_BAYERDST1(row-1,    start_col);
				break;

			case WF_IMGMODE_BAYER4PLANE:
				dst_c  = &WF_BAYERDST4(row-1,    start_col, FC(row-1, start_col));
				break;
		}

		for (col=start_col;  col<S.width; col+=2)
		{
			*dst_c=(line_filtered[col])>>3;
			dst_c+=dst_h_shift;
		}

		if (src_image != dst_image)
		{
			// copy red or blue channel
			
			if ( green_mode == WF_GREENMODE_GX_XG )
				start_col = row & 1;
			else
				start_col = (row+1) & 1;
				
			switch (src_imgmode)
			{
				case WF_IMGMODE_BAYER1PLANE:
					src     = &WF_BAYERSRC1(row-1, start_col);
					break;

				case WF_IMGMODE_BAYER4PLANE:
					src     = &WF_BAYERSRC4(row-1, start_col, FC(row-1, start_col));
					break;
			}

			switch (dst_imgmode)
			{
				case WF_IMGMODE_BAYER1PLANE:
					dst_rb  = &WF_BAYERDST1(row-1, start_col);
					break;
				
				case WF_IMGMODE_BAYER4PLANE:
					dst_rb  = &WF_BAYERDST4(row-1, start_col, FC(row-1, start_col));
					break;
			}
			
			for (col=start_col;  col<S.width; col+=2)
			{
				*dst_rb=*src;
				src   +=src_h_shift;
				dst_rb+=dst_h_shift;
			}
		}
	}

	free(line_filtered);
}

void LibRaw::wf_bayer4_igauss_filter(int radius, void* src_image, int src_imgmode, void* dst_image, int dst_imgmode)
{
	/* 
	   This function filter source bayer4 data with gauss (binominal), 4 channels independently.
	*/
	   
#define WF_BAYERSRC4(row, col, c) ((ushort(*)[4])src_image)[((row) >> IO.shrink)*S.iwidth + ((col) >> IO.shrink)][c] 
#define WF_BAYERSRC1(row, col)    ((ushort*)src_image)     [((row) >> IO.shrink)*S.iwidth + ((col) >> IO.shrink)] 
#define WF_BAYERDST4(row, col, c) ((ushort(*)[4])dst_image)[((row) >> IO.shrink)*S.iwidth + ((col) >> IO.shrink)][c] 
#define WF_BAYERDST1(row, col)    ((ushort*)dst_image)     [((row) >> IO.shrink)*S.iwidth + ((col) >> IO.shrink)]

	if (radius <= 0 || radius > 8)
	   return;

	long int (*line_filtered)[4];

	long int gauss_conv_kernel[9][4];

	long int gauss_conv_kernel_c[8][9] = 
	{
		{32768, 16384},
		{24576, 16384, 4096},
		{20480,	15360, 6144, 1024},
		{17920, 14336, 7168, 2048, 256},
		{16128, 13440, 7680, 2880, 640,  64},
		{14784, 12672, 7920, 3520, 1056, 192, 16},
		{13728, 12012, 8008, 4004, 1456, 364, 56,  4},
		{12870, 11440, 8008, 4368, 1820, 560, 120, 16, 1},
	};

	int line_memory_len = (MAX(S.height, S.width)+1)/2+radius*2+1;
    line_filtered       = (long int(*)[4]) calloc(line_memory_len, sizeof(long int[4]));

	int src_h_shift, src_v_shift;
	int dst_h_shift, dst_v_shift;

	if      (src_imgmode == WF_IMGMODE_BAYER1PLANE)
		src_h_shift = 2 >> IO.shrink;
	else if (src_imgmode == WF_IMGMODE_BAYER4PLANE)
		src_h_shift = 8 >> IO.shrink;

	src_v_shift = S.width*src_h_shift;

	if      (dst_imgmode == WF_IMGMODE_BAYER1PLANE)
		dst_h_shift = 2 >> IO.shrink;
	else if (dst_imgmode == WF_IMGMODE_BAYER4PLANE)
		dst_h_shift = 8 >> IO.shrink;

	dst_v_shift = S.width*dst_h_shift;

	int width_d2  = S.width  / 2;
	int height_d2 = S.height / 2;

	int i, j;

	for (j=0; j<=radius; j++)
	{
		for (i=0; i<4; i++)
		{
			gauss_conv_kernel[j][i] = gauss_conv_kernel_c[radius-1][j];
		}
	}

	int row,  col;
	int rowf, colf;

	ushort   *src  [4], *dst[4];
	long int  src_c[4];

	// Horizontal 

	int right_edge[4];

	for (i=0; i<4; i++)
	{
		int padding = i<2 && (S.width & 1 == 1) ? 1 : 0;
		right_edge[i]=width_d2 + radius + padding;
	}
	
	for(row=0; row<S.height; row+=2)
	{
		int row_p1=MIN(row+1, S.height-1);

		switch (src_imgmode)
		{
			case WF_IMGMODE_BAYER1PLANE:

				src[0] = &WF_BAYERSRC1(row,    0);
				src[1] = &WF_BAYERSRC1(row_p1, 0);
				src[2] = &WF_BAYERSRC1(row,    1);
				src[3] = &WF_BAYERSRC1(row_p1, 1);
				break;

			case WF_IMGMODE_BAYER4PLANE:

				src[0] = &WF_BAYERSRC4(row,    0, FC(0,      0));
				src[1] = &WF_BAYERSRC4(row_p1, 0, FC(row_p1, 0));
				src[2] = &WF_BAYERSRC4(row,    1, FC(0,      1));
				src[3] = &WF_BAYERSRC4(row_p1, 1, FC(row_p1, 1));
				break;
		}

		colf = radius;

		for (int j=0; j<line_memory_len; j++)
		{
			for(i=0;i<4;i++)
				line_filtered[j][i]=0;
		}
		
		for(col=0; col<S.width-1; col+=2)
		{

			int col1, col2;

			col1=col2=colf;

			for (i=0; i<4; i++)
				src_c[i]=*src[i];

			for (i=0; i<4; i++)
				line_filtered[colf][i]+=gauss_conv_kernel[0][i]*(src_c[i]);

			for(int j=1; j<=radius; j++)
			{
				col1++;
				col2--;
			
				for (i=0; i<4; i++)
				{
					long int g;

					g = gauss_conv_kernel[j][i]*src_c[i];

					line_filtered[col1][i]+=g;
					line_filtered[col2][i]+=g;
				}
			}

			colf++;

			for (i=0; i<4; i++)
				src[i]+=src_h_shift;
		}

		// width is odd number

		if (col == S.width-1)
		{
			int col1, col2;

			col1=col2=colf;

			for (i=0; i<2; i++)
				src_c[i]=*src[i];

			for (i=0; i<2; i++)
				line_filtered[colf][i]+=gauss_conv_kernel[0][i]*(src_c[i]);

			for(int j=1; j<=radius; j++)
			{
				col1++;
				col2--;
			
				for (i=0; i<2; i++)
				{
					long int g;

					g = gauss_conv_kernel[j][i]*src_c[i];

					line_filtered[col1][i]+=g;
					line_filtered[col2][i]+=g;
				}
			}

			colf++;

			for (i=0; i<2; i++)
				src[i]+=src_h_shift;
		}

		// Edges mirroring

		for(j=0; j<radius; j++)
		{
			for (i=0; i<4; i++)
			{
				line_filtered[radius+j         ][i]+=line_filtered[radius-j-1     ][i];
				line_filtered[right_edge[i]-1-j][i]+=line_filtered[right_edge[i]+j][i];
			}
		}
		
		switch (dst_imgmode)
		{
			case WF_IMGMODE_BAYER1PLANE:
			
				dst[0] = &WF_BAYERDST1(row,    0);
				dst[1] = &WF_BAYERDST1(row_p1, 0);
				dst[2] = &WF_BAYERDST1(row,    1);
				dst[3] = &WF_BAYERDST1(row_p1, 1);
				break;

			case WF_IMGMODE_BAYER4PLANE:
		
				dst[0] = &WF_BAYERDST4(row,    0, FC(0,      0));
				dst[1] = &WF_BAYERDST4(row_p1, 0, FC(row_p1, 0));
				dst[2] = &WF_BAYERDST4(row,    1, FC(0,      1));
				dst[3] = &WF_BAYERDST4(row_p1, 1, FC(row_p1, 1));
				break;
		}

		colf = radius;

		for(col=0; col<S.width-1; col+=2)
		{
			for(i=0; i<4; i++)
			{
				*dst[i]=line_filtered[colf][i]>>16;
				dst[i]+=dst_h_shift;
			}

			colf++;
		}

		if (col == S.width-1)
		{
			for(i=0; i<2; i++)
				*dst[i]=line_filtered[colf][i]>>16;
		}
	}


   	// Vertical

	int lower_edge[4];

	for (i=0; i<4; i++)
	{
		int padding = i<2 && (S.height & 1 == 1) ? 1 : 0;
		lower_edge[i]=height_d2 + radius + padding;
	}


	for(col=0; col<S.width; col+=2)
	{
		int col_p1=MIN(col+1, S.width-1);	

		switch (dst_imgmode)
		{
			case WF_IMGMODE_BAYER1PLANE:

				src[0] = &WF_BAYERDST1(0, col);
				src[1] = &WF_BAYERDST1(0, col_p1);
				src[2] = &WF_BAYERDST1(1, col);
				src[3] = &WF_BAYERDST1(1, col_p1);
				break;

			case WF_IMGMODE_BAYER4PLANE:

				src[0] = &WF_BAYERDST4(0, col,    FC(0,      0));
				src[1] = &WF_BAYERDST4(0, col_p1, FC(0, col_p1));
				src[2] = &WF_BAYERDST4(1, col,    FC(1,      0));
				src[3] = &WF_BAYERDST4(1, col_p1, FC(1, col_p1));
				break;
		}

		rowf = radius;

		for (int j=0; j<line_memory_len; j++)
		{
			for(i=0;i<4;i++)
				line_filtered[j][i]=0;
		}
		
		for(row=0; row<S.height-1; row+=2)
		{

			int row1, row2;

			row1=row2=rowf;

			for (i=0; i<4; i++)
				src_c[i]=*src[i];

			for (i=0; i<4; i++)
				line_filtered[rowf][i]+=gauss_conv_kernel[0][i]*(src_c[i]);

			for(int j=1; j<=radius; j++)
			{
				row1++;
				row2--;
			
				long int g[4];

				for (i=0; i<4; i++)
				{

					g[i] = gauss_conv_kernel[j][i]*src_c[i];

					line_filtered[row1][i]+=g[i];
					line_filtered[row2][i]+=g[i];
				}
			}

			rowf++;

			for (i=0; i<4; i++)
				src[i]+=dst_v_shift;
		}

		// height is odd number

		if (row == S.height-1)
		{
			int row1, row2;

			row1=row2=rowf;

			for (i=0; i<2; i++)
				src_c[i]=*src[i];

			for (i=0; i<2; i++)
				line_filtered[rowf][i]+=gauss_conv_kernel[0][i]*(src_c[i]);

			for(int j=1; j<=radius; j++)
			{
				row1++;
				row2--;
			
				long int g[4];

				for (i=0; i<2; i++)
				{

					g[i] = gauss_conv_kernel[j][i]*src_c[i];

					line_filtered[row1][i]+=g[i];
					line_filtered[row2][i]+=g[i];
				}
			}

			rowf++;

			for (i=0; i<2; i++)
				src[i]+=dst_v_shift;
		}

		// Edge mirroring

		for(int j=0; j<radius; j++)
		{
			for (int i=0; i<4; i++)
			{
				line_filtered[radius+j][i]         +=line_filtered[radius-j-1][i];
				line_filtered[lower_edge[i]-1-j][i]+=line_filtered[lower_edge[i]+j][i];
			}
		}


		switch (dst_imgmode)
		{
			case WF_IMGMODE_BAYER1PLANE:
			
				dst[0] = &WF_BAYERDST1(0, col);
				dst[1] = &WF_BAYERDST1(0, col_p1);
				dst[2] = &WF_BAYERDST1(1, col);
				dst[3] = &WF_BAYERDST1(1, col_p1);
				break;

			case WF_IMGMODE_BAYER4PLANE:
		
				dst[0] = &WF_BAYERDST4(0, col,    FC(0, 0));
				dst[1] = &WF_BAYERDST4(0, col_p1, FC(0, col_p1));
				dst[2] = &WF_BAYERDST4(1, col,    FC(1, 0));
				dst[3] = &WF_BAYERDST4(1, col_p1, FC(1, col_p1));
				break;
		}

		rowf = radius;

		for(row=0; row<S.height-1; row+=2)
		{
			for(i=0; i<4; i++)
			{
				*dst[i]=line_filtered[rowf][i]>>16;
				dst[i]+=dst_v_shift;
			}

			rowf++;
		}

		if (row == S.height-1)
		{
			for(i=0; i<2; i++)
				*dst[i]=line_filtered[rowf][i]>>16;
		}
   }

   free(line_filtered);
}

void LibRaw::wf_bayer4_block_filter(int* radius_list, void* src_image, int src_imgmode, void* dst_image, int dst_imgmode)
{
#define WF_BLOCKFILTER_MAXF 8

#define WF_BAYERSRC4(row,col,c) ((ushort(*)[4])src_image)[((row) >> IO.shrink)*S.iwidth + ((col) >> IO.shrink)][c] 
#define WF_BAYERSRC1(row,col)   ((ushort*)src_image)     [((row) >> IO.shrink)*S.iwidth + ((col) >> IO.shrink)] 
#define WF_BAYERDST4(row,col,c) ((ushort(*)[4])dst_image)[((row) >> IO.shrink)*S.iwidth + ((col) >> IO.shrink)][c] 
#define WF_BAYERDST1(row,col)   ((ushort*)dst_image)     [((row) >> IO.shrink)*S.iwidth + ((col) >> IO.shrink)] 

	int filter_itrtns_num = 0;
	
	int block_radius      [WF_BLOCKFILTER_MAXF]; 
	int block_radius_x2   [WF_BLOCKFILTER_MAXF];
	int block_radius_x2_p1[WF_BLOCKFILTER_MAXF];

	int block_radius_max = 0;
	int block_radius_max_x2;
	int block_radius_max_x2_p1;

	for(int i=0; (i<WF_BLOCKFILTER_MAXF) && (radius_list[i]!=0); i++)
	{
		block_radius      [i]=radius_list[i];
		block_radius_x2   [i]=block_radius[i]*2;
		block_radius_x2_p1[i]=block_radius_x2[i]+1;

		if(block_radius_max<block_radius[i])
			block_radius_max=block_radius[i];

		filter_itrtns_num++;
	}

	long int divider[WF_BLOCKFILTER_MAXF];
	long int div_multiplication;

	div_multiplication=block_radius_x2_p1[0];

	for(int i=1; i<filter_itrtns_num; i++)
	{
		if (div_multiplication*((long int)block_radius_x2_p1[i])<65535)
		{
			div_multiplication*=block_radius_x2_p1[i];
			divider[i-1]=1;
		}
		else
		{
			divider[i-1]=block_radius_x2_p1[i];
		}
	}

	divider[filter_itrtns_num-1]=div_multiplication;

	block_radius_max_x2    = block_radius_max*2;
	block_radius_max_x2_p1 = block_radius_max_x2+1;
	
	int line_memory_len;

	long int (*source_line)[4];
	long int (*line_block_filtered)[4];
 
	line_memory_len = (MAX(S.height, S.width)+1)/2+block_radius_max_x2_p1*2;

	line_block_filtered=(long int(*)[4]) calloc(line_memory_len, sizeof(long int[4]));
	source_line        =(long int(*)[4]) calloc(line_memory_len, sizeof(long int[4]));
	

   	int   src_h_shift, dst_h_shift, src_v_shift, dst_v_shift;
 
	if      (src_imgmode == WF_IMGMODE_BAYER1PLANE)
		src_h_shift = 2 >> IO.shrink;
	else if (src_imgmode == WF_IMGMODE_BAYER4PLANE)
		src_h_shift = 8 >> IO.shrink;

	src_v_shift = S.width*src_h_shift;

	if      (dst_imgmode == WF_IMGMODE_BAYER1PLANE)
		dst_h_shift = 2 >> IO.shrink;
	else if (dst_imgmode == WF_IMGMODE_BAYER4PLANE)
		dst_h_shift = 8 >> IO.shrink;

	dst_v_shift = S.width*dst_h_shift;

	int width_d2     = S.width  / 2;
	int height_d2    = S.height / 2;

	int width_p1_d2  = (S.width+1)  / 2;
	int height_p1_d2 = (S.height+1) / 2;

	ushort   *src[4], *dst[4];

	long int  (*src_plus)[4], (*src_minus)[4];
	long int  block_sum[4];
	
	int row, col;

	int right_edge[4], lower_edge[4];

	for(row=0; row<S.height; row+=2)
	{
		int row_p1=MIN(row+1, S.height-1);

		switch (src_imgmode)
		{
			case WF_IMGMODE_BAYER1PLANE:

				src[0] = &WF_BAYERSRC1(row,    0);
				src[1] = &WF_BAYERSRC1(row_p1, 0);
				src[2] = &WF_BAYERSRC1(row,    1);
				src[3] = &WF_BAYERSRC1(row_p1, 1);
				break;

			case WF_IMGMODE_BAYER4PLANE:

				src[0] = &WF_BAYERSRC4(row,    0, FC(0,      0));
				src[1] = &WF_BAYERSRC4(row_p1, 0, FC(row_p1, 0));
				src[2] = &WF_BAYERSRC4(row,    1, FC(0,      1));
				src[3] = &WF_BAYERSRC4(row_p1, 1, FC(row_p1, 1));
				break;
		}

		for(col=0; col<width_d2; col++)
		{
			for (int i=0; i<4; i++)
			{
				source_line[col][i]=*src[i];
				src[i] += src_h_shift;
			}
		}

		if (S.width & 1 == 1)
		{
			for (int i=0; i<2; i++)
			{		   
				source_line[width_d2][i]=*src[i];
			}

			for (int i=2; i<4; i++)
			{		   
				source_line[width_d2][i]=0;
			}
		}

		for(int f=0; f<filter_itrtns_num; f++)
		{
			src_minus=src_plus=source_line;

			for (int i=0; i<4; i++)
				block_sum[i]=0;

			for(col=0; col<block_radius_x2_p1[f]; col++)
			{
				for (int i=0; i<4; i++)
				{
					block_sum[i]+=(*src_plus)[i];
					line_block_filtered[col][i]=block_sum[i];
				}

				src_plus++;
			}

			for(col=block_radius_x2_p1[f]; col<width_p1_d2; col++)
			{
				for (int i=0; i<4; i++)
				{		   
					block_sum[i]+=(*src_plus)[i];
					block_sum[i]-=(*src_minus)[i];
					line_block_filtered[col][i]=block_sum[i];
				}

				src_plus++;
				src_minus++;
			}

			for(col=width_p1_d2; col<width_p1_d2+block_radius_x2_p1[f]; col++)
			{
				for (int i=0; i<4; i++)
				{
					block_sum[i]-=(*src_minus)[i];
					line_block_filtered[col][i]=block_sum[i];
				}

				src_minus++;
			}

			// Edge mirroring

			for (int i=0; i<4; i++)
			{
				int padding = i<2 && (S.width & 1 == 1) ? 1 : 0;
				right_edge[i]=width_d2 + block_radius[f] + padding;
			}

			for(int j=0; j<block_radius[f]; j++)
			{
				for (int i=0; i<4; i++)
				{
					line_block_filtered[block_radius[f]+j][i]+=line_block_filtered[block_radius[f]-j-1][i];
					line_block_filtered[right_edge[i]-1-j][i]+=line_block_filtered[right_edge[i]+j][i];
				}
			}

			if (divider[f]==1)
			{
				for(col=0; col<width_d2; col++)
				{
					for (int i=0; i<4; i++)
						source_line[col][i]=line_block_filtered[col+block_radius[f]][i];
				}

				if (S.width & 1 == 1)
				{
					for (int i=0; i<2; i++)
						source_line[width_d2][i]=line_block_filtered[width_d2+block_radius[f]][i];

					for (int i=2; i<4; i++)
						source_line[width_d2][i]=0;
				}
			}
			else
			{
				for(col=0; col<width_d2; col++)
				{
					for (int i=0; i<4; i++)
						source_line[col][i]=line_block_filtered[col+block_radius[f]][i]/divider[f];
				}

				if (S.width & 1 == 1)
				{
					for (int i=0; i<2; i++)
						source_line[width_d2][i]=line_block_filtered[width_d2+block_radius[f]][i]/divider[f];

					for (int i=2; i<4; i++)
						source_line[width_d2][i]=0;
				}
			}
		}

		switch (dst_imgmode)
		{
			case WF_IMGMODE_BAYER1PLANE:

				dst[0] = &WF_BAYERDST1(row,    0);
				dst[1] = &WF_BAYERDST1(row_p1, 0);
				dst[2] = &WF_BAYERDST1(row,    1);
				dst[3] = &WF_BAYERDST1(row_p1, 1);
				break;

			case WF_IMGMODE_BAYER4PLANE:

				dst[0] = &WF_BAYERDST4(row,    0, FC(0,      0));
				dst[1] = &WF_BAYERDST4(row_p1, 0, FC(row_p1, 0));
				dst[2] = &WF_BAYERDST4(row,    1, FC(0,      1));
				dst[3] = &WF_BAYERDST4(row_p1, 1, FC(row_p1, 1));
				break;
		}

		for(col=0; col<width_d2; col++)
		{
			for (int i=0; i<4; i++)
			{		   
				*dst[i]=source_line[col][i];
				dst[i]+=dst_h_shift;
			}
		}

		if (S.width & 1 == 1)
		{
			for (int i=0; i<2; i++)
				*dst[i]=source_line[col][i];
		}
	}

	for(col=0; col<S.width; col+=2)
	{
		int col_p1=MIN(col+1, S.width-1);

		switch (dst_imgmode)
		{
			case WF_IMGMODE_BAYER1PLANE:

				src[0] = &WF_BAYERDST1(0, col);
				src[1] = &WF_BAYERDST1(0, col_p1);
				src[2] = &WF_BAYERDST1(1, col);
				src[3] = &WF_BAYERDST1(1, col_p1);
				break;

			case WF_IMGMODE_BAYER4PLANE:

				src[0] = &WF_BAYERDST4(0, col,    FC(0,      0));
				src[1] = &WF_BAYERDST4(0, col_p1, FC(0, col_p1));
				src[2] = &WF_BAYERDST4(1, col,    FC(1,      0));
				src[3] = &WF_BAYERDST4(1, col_p1, FC(1, col_p1));
				break;
		}

		for(row=0; row<height_d2; row++)
		{
			for (int i=0; i<4; i++)
			{
				source_line[row][i]=*src[i];
				src[i] += dst_v_shift;
			}
		}
		
		if (S.height & 1 == 1)
		{
			for (int i=0; i<2; i++)
			{		   
				source_line[height_d2][i]=*src[i];
			}

			for (int i=2; i<4; i++)
			{		   
				source_line[height_d2][i]=0;
			}
		}

		for(int f=0; f<filter_itrtns_num; f++)
		{
			src_minus=src_plus=source_line;

			for (int i=0; i<4; i++)
				block_sum[i]=0;

			for(row=0; row<block_radius_x2_p1[f]; row++)
			{
				for (int i=0; i<4; i++)
				{
					block_sum[i]+=(*src_plus)[i];
					line_block_filtered[row][i]=block_sum[i];
				}

				src_plus++;
			}

			for(row=block_radius_x2_p1[f]; row<height_p1_d2; row++)
			{
				for (int i=0; i<4; i++)
				{		   
					block_sum[i]+=(*src_plus)[i];
					block_sum[i]-=(*src_minus)[i];
					line_block_filtered[row][i]=block_sum[i];
				}

				src_plus++;
				src_minus++;
			}

			for(row=height_p1_d2; row<height_p1_d2+block_radius_x2_p1[f]; row++)
			{
				for (int i=0; i<4; i++)
				{
					block_sum[i]-=(*src_minus)[i];
					line_block_filtered[row][i]=block_sum[i];
				}

				src_minus++;
			}

			// Edge mirroring

			for (int i=0; i<4; i++)
			{
				int padding = (i<2) && (S.height & 1 == 1) ? 1 : 0;
				lower_edge[i]=height_d2 + block_radius[f] + padding;
			}

			for(int j=0; j<block_radius[f]; j++)
			{
				for (int i=0; i<4; i++)
				{
					line_block_filtered[block_radius[f]+j][i]+=line_block_filtered[block_radius[f]-j-1][i];
					line_block_filtered[lower_edge[i]-1-j][i]+=line_block_filtered[lower_edge[i]+j][i];
				}
			}

			if (divider[f]==1)
			{
				for(row=0; row<height_d2; row++)
				{
					for (int i=0; i<4; i++)
						source_line[row][i]=line_block_filtered[row+block_radius[f]][i];
				}

				if (S.height & 1 == 1)
				{
					for (int i=0; i<2; i++)
						source_line[height_d2][i]=line_block_filtered[height_d2+block_radius[f]][i];

					for (int i=2; i<4; i++)
						source_line[height_d2][i]=0;
				}
			}
			else
			{
				for(row=0; row<height_d2; row++)
				{
					for (int i=0; i<4; i++)
						source_line[row][i]=line_block_filtered[row+block_radius[f]][i]/divider[f];
				}

				if (S.height & 1 == 1)
				{
					for (int i=0; i<2; i++)
						source_line[height_d2][i]=line_block_filtered[height_d2+block_radius[f]][i]/divider[f];

					for (int i=2; i<4; i++)
						source_line[height_d2][i]=0;
				}
			}
		}

		switch (dst_imgmode)
		{
			case WF_IMGMODE_BAYER1PLANE:

				dst[0] = &WF_BAYERDST1(0, col);
				dst[1] = &WF_BAYERDST1(0, col_p1);
				dst[2] = &WF_BAYERDST1(1, col);
				dst[3] = &WF_BAYERDST1(1, col_p1);
				break;

			case WF_IMGMODE_BAYER4PLANE:

				dst[0] = &WF_BAYERDST4(0, col,    FC(0,      0));
				dst[1] = &WF_BAYERDST4(0, col_p1, FC(0, col_p1));
				dst[2] = &WF_BAYERDST4(1, col,    FC(1,      0));
				dst[3] = &WF_BAYERDST4(1, col_p1, FC(1, col_p1));
				break;
		}

		for(row=0; row<height_d2; row++)
		{
			for (int i=0; i<4; i++)
			{		   
				*dst[i]=source_line[row][i];
				dst[i]+=dst_v_shift;
			}
		}

		if (S.height & 1 == 1)
		{
			for (int i=0; i<2; i++)
				*dst[i]=source_line[height_d2][i];
		}
	}

	free(line_block_filtered);
	free(source_line);
}
#undef P1
#undef S
#undef O
#undef C
#undef T
#undef IO
#undef ID
