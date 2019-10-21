/*==================================================================================
| tif2nif.c - Utility to convert 3D-TIFF images to NIFTI-1
|-----------------------------------------------------------------------------------
| Adapted from Alex Ade email 11/13/2014 8:49 AM
| 3D TIFF to NIFTI-1 (single file) converter that runs on the command line on Linux.
| The command line flags are:
| — input-file (-i) path to the input TIFF file
| — output-file (-o) path to the output NIFTI-1 file
| — x-scale (-x) numerical x scale factor from the vendor metadata
| — y-scale (-y) numerical y scale factor from the vendor metadata
| — z-scale (-z) numerical z scale factor from the vendor metadata
| 
| The code uses the x, y, and z scale factors to populate the pixdim header array.
| Values are normalized so that x and y are 1.0 and z is scaled appropriately.
| Scale factor values can be entered in scientific notation.
| 
| FFT manipulation uses the libtiff library (http://www.libtiff.org)
| NIFTI manipulation uses the NIH NIFTI library (http://nifti.nimh.nih.gov)
|-----------------------------------------------------------------------------------
| Implemented by David Dilworth
| - Software adapts and follows K&R-C-style from NIFTI "real_easy" example
| - Writes exit or errors to stderr
| -   See exit_message(int exit_code, const char *message);
| - Test platform is ImageJ
| -   ImageJ reads either type of Endian
| -   Correct Endian hasn't been investigated
| - Test files on NAS
| -   /mnt/PP/PP_Results/0000114/Slide5
| -     /Slide5_c0_mask-012.meta, metadata from microscope
| -     /Slide5_c0_mask-012.raw, base image (1024x1024x42x16bits)
| -     /Slide5_c0_mask-012.tif, tiff version of base image
| -     /Slide5_c0_mask-012.nii, nifti version of tiff image
| Possible updates
| - remove or disable printf()
| - convert code to 'safe" calls, e.g fopen_s
| - convert comments to other format, e.g. doxygen
| - convert implementation to C++ objects
|-----------------------------------------------------------------------------------
| g++ -v -> gcc version 4.6.3 20120306 (Red Hat 4.6.3-2) (GCC) 
| TIFF: tiff-3.8.2.zip
| Developed on: Fedora 16, Kernel 3.6.11-4.fc16x86_64
| Compile: "g++ -L/usr/local/lib -o tif2nif tif2nif.c -ltiff"
| David Dilworth, November 24, 2014, v1.0
==================================================================================*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <tiffio.h>

#include "nifti1.h"
#define MIN_HEADER_SIZE 348
#define NII_HEADER_SIZE 352

// tif2nif status values
#define T2N_OK 0					// generic OK result value
#define T2N_PARAM_COUNT -1			// command line parameter count incorrect
#define T2N_PARAM_FORMAT -2			// command line parameter wrong format
#define T2N_TIFF_OPEN -3			// tiff file open failed (called twice)
#define T2N_TIFF_PARSE -4			// tiff parse failed
#define T2N_TIFF_CONFIG -5			// tiff config value incorrect
#define T2N_TIFF_MALLOC -6			// tiff malloc failed
#define T2N_TIFF_SETDIRECTORY -7	// tiff set directory failed
#define T2N_TIFF_READ -8			// tiff read failed
#define T2N_NIFTI_OPEN -9			// nifti file open failed
#define T2N_NIFTI_WRITE -10			// nifti fwrite failed

// parsed command line parameters
char input_tiff_file[FILENAME_MAX];
char output_nifti_file[FILENAME_MAX];
float x_scale;
float y_scale;
float z_scale;

// prints exit code and text message at exit
// - type of message depends exit_code
void exit_message(int exit_code, const char *message)
{
	if(exit_code==T2N_OK)
		fprintf(stderr,"Success, %d, %s\n",exit_code,message);
	else
		fprintf(stderr,"Error, %d, %s\n",exit_code,message);
	exit(exit_code);
}

// parses command line parameters
// - fills global variables
int parse_params(int argc, char *argv[])
{
	int i;
	char key;
	char *param;
	
	*input_tiff_file=0;			// zero variables
	*output_nifti_file=0;
	x_scale=0;
	y_scale=0;
	z_scale=0;
	if(argc != 6){				// check number of parameters
		return T2N_PARAM_COUNT;
        }

	for(i=1;i<argc;i++)			// step through arguments
	{
		param = argv[i];		// get each parameter
		key = param[1];			// skip the - and look at key character
		switch(key)
		{
			case 'i':
			strcpy(input_tiff_file,&param[2]);
			break;
			
			case 'o':
			strcpy(output_nifti_file,&param[2]);
			break;
			
			case 'x':
			x_scale = atof(&param[2]);
			break;
			
			case 'y':
			y_scale = atof(&param[2]);
			break;
			
			case 'z':
			z_scale = atof(&param[2]);
			break;
		}
	}
	if(*input_tiff_file==0 || *output_nifti_file==0)
		return T2N_PARAM_FORMAT;
	if(x_scale==0 || y_scale==0 || z_scale==0)
		return T2N_PARAM_FORMAT;
	return T2N_OK;
}

// converts tiff image to nifti with (x,y,z) scaling
// - based on nifti real_easy example C program
int main(int argc, char *argv[])
{
	// variables for tiff image
	uint32 width, length;
	tsize_t scanlen;
	short bits_persample,samples_perpixel,config;
	uint16 *scanline_buf;
	int dircount;
	int parse;
	
	// variables for nifti image
	float pixdim_x;
	float pixdim_y;
	float pixdim_z;
	nifti_1_header hdr;
	nifti1_extender pad={0,0,0,0};
	FILE *fp_nifti;
	int ret,i;
	short do_nii=1;
	
	// variables for write tiff to nifti
	int j,row,status;
	
	// variables for diagnostic .raw file (option)
	int ret_raw;
	FILE *fp_raw = 0;
	//fp_raw = fopen("file.raw","wb");	// enable to create raw binary image file

	// parse command line and print parameters
	parse = parse_params(argc,argv);
	printf("parse = %d\n",parse);
	printf("input_tiff_file = %s\n",input_tiff_file);
	printf("output_nifti_file = %s\n",output_nifti_file);
	printf("x_scale = %g\n",x_scale);
	printf("y_scale = %g\n",y_scale);
	printf("z_scale = %g\n",z_scale);
	if(parse!=0)
		exit_message(parse,"parameters");

	// compute pixdim[] values from command line
	// - assume x_scale == y_scale
	pixdim_x = 1;
	pixdim_y = 1;
	pixdim_z = z_scale/x_scale;
	
	// open TIFF and get header values
	TIFF *tif = TIFFOpen(input_tiff_file,"rb");
	if (tif==0)
		exit_message(T2N_TIFF_OPEN,"TIFFOpen()");
	if(TIFFGetField(tif,TIFFTAG_IMAGEWIDTH,&width)==0)
		exit_message(T2N_TIFF_PARSE,"TIFFTAG_IMAGEWIDTH");
	if(TIFFGetField(tif,TIFFTAG_IMAGELENGTH,&length)==0)
		exit_message(T2N_TIFF_PARSE,"TIFFTAG_IMAGELENGTH");
	if(TIFFGetField(tif,TIFFTAG_BITSPERSAMPLE,&bits_persample)==0)
		exit_message(T2N_TIFF_PARSE,"TIFFTAG_BITSPERSAMPLE");
	if(TIFFGetField(tif,TIFFTAG_SAMPLESPERPIXEL,&samples_perpixel)==0)
		exit_message(T2N_TIFF_PARSE,"TIFFTAG_SAMPLESPERPIXEL");
	if(TIFFGetField(tif, TIFFTAG_PLANARCONFIG, &config)==0)
		exit_message(T2N_TIFF_PARSE,"TIFFTAG_PLANARCONFIG");
	if(config!=1)
		exit_message(T2N_TIFF_PARSE,"Value != 1");
	scanlen = TIFFScanlineSize(tif);
	if(scanlen <= 0)
		exit_message(T2N_TIFF_CONFIG,"Scanline Length <= 0");
	scanline_buf = (uint16*)_TIFFmalloc(scanlen);
	if(scanline_buf==0)
		exit_message(T2N_TIFF_MALLOC,"TIFFmalloc()");
	
	// count slices in TIFF image
	dircount=0;
	do
		dircount++;
	while
		(TIFFReadDirectory(tif));
    TIFFClose(tif);
        
    // print tiff header values   
    printf("width = %d\n",width);	
	printf("length = %d\n",length);
	printf("samples_perpixel = %d\n",samples_perpixel);
	printf("bits_persample = %d\n",bits_persample);
	printf("config = %d\n",config);	
	printf("scanlen = %d\n",scanlen);
	printf("dircount = %d\n",dircount);
	printf("scanline_buf = 0x%X\n",scanline_buf);

	// prepare nifti header
	// note - do_nii=1 above
	bzero((void *)&hdr, sizeof(hdr));
	hdr.sizeof_hdr = MIN_HEADER_SIZE;
	hdr.dim[0] = 3;     // number of dimensions
	hdr.dim[1] = width;
	hdr.dim[2] = length;
	hdr.dim[3] = dircount;
	hdr.datatype = DT_UINT16;
	hdr.bitpix = bits_persample;
	hdr.pixdim[1] = pixdim_x;
	hdr.pixdim[2] = pixdim_y;
	hdr.pixdim[3] = pixdim_z;
	if (do_nii)
        hdr.vox_offset = (float) NII_HEADER_SIZE;
	else
        hdr.vox_offset = (float)0;
	hdr.xyzt_units = NIFTI_UNITS_UNKNOWN;
	if (do_nii)
		strncpy(hdr.magic, "n+1\0", 4);
	else
	    strncpy(hdr.magic, "ni1\0", 4);
	        
	// write the nifti header
	fp_nifti = fopen(output_nifti_file,"wb");
	if (fp_nifti == NULL)
		exit_message(T2N_NIFTI_OPEN,output_nifti_file);
	ret = fwrite(&hdr, MIN_HEADER_SIZE, 1, fp_nifti);
	if (ret != 1)
		exit_message(T2N_NIFTI_WRITE,"hdr");
		
	// write the nifti pad     
	ret = fwrite(&pad, 4, 1, fp_nifti);
	if (ret != 1)
		exit_message(T2N_NIFTI_WRITE,"pad");
		
	// open tiff and output pixels to nifti	
	tif = TIFFOpen(input_tiff_file,"rb");
	if (tif==0)
		exit_message(T2N_TIFF_OPEN,input_tiff_file);
	for(j=0;j<dircount;j++)										// for all slices
	{
		status = TIFFSetDirectory(tif,j);						// set the slice
		if(status==0)
       		exit_message(T2N_TIFF_READ,"");
		for (row = 0; row < length; row++)						// for all rows
		{
	   		status=TIFFReadScanline(tif, scanline_buf, row, 0);	// read rows as scanlines
       		if(status==-1)
       			exit_message(T2N_TIFF_SETDIRECTORY,"");
           	if(fp_raw)
       			ret_raw= fwrite(scanline_buf,2,length,fp_raw);
       		ret = fwrite(scanline_buf, (size_t)(hdr.bitpix/8), length, fp_nifti);
      		if (ret != length)
				exit_message(T2N_NIFTI_WRITE,"buf");
       	}
	}
	
	// free, close, and exit
	_TIFFfree(scanline_buf);
	TIFFClose(tif);
	fclose(fp_nifti);
	if(fp_raw)
		fclose(fp_raw);
	exit_message(T2N_OK,output_nifti_file);
}
