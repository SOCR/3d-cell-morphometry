// tile2blob.cc
// 
// Separates segmented binary TIF volume into sigle-cell-per-volume collection of TIFs
// Requires TiffLib 4
//
// Compilation:
// g++ -o tile2blob tile2blob.cc -ltiff
// g++ -std=c++0x -o tile2blob tile2blob.cc -ltiff

#include "tiffio.h"
#include <iostream>
#include <cstdlib>
#include <unordered_set>

using namespace std;

int subdivide(int index, TIFF *tif, uint16 dircount, char *prefix) {
	TIFF *out;

	char numstr[21];
	sprintf(numstr, "%03d", index);

	string s1 = (string(prefix));
	string s2 = (string(numstr));
	string s3 = ".tif";
	string s4 = s1 + s2 + s3;

	out = TIFFOpen(s4.c_str(), "w");

	if (!out) {
		fprintf(stderr, "Can't open %s for writing\n", s4.c_str());
		return 1;
	}

	//cout << index << endl;

	uint32 width, length, row, col, bps;
	uint16 nsamples, config, page;
	tdata_t buf;

	TIFFSetDirectory(tif, 0);

	do {
		TIFFGetField(tif, TIFFTAG_IMAGEWIDTH, &width);
		TIFFGetField(tif, TIFFTAG_IMAGELENGTH, &length);
		TIFFGetField(tif, TIFFTAG_BITSPERSAMPLE, &bps);
		TIFFGetField(tif, TIFFTAG_SAMPLESPERPIXEL, &nsamples);
		TIFFGetField(tif, TIFFTAG_PLANARCONFIG, &config);
		TIFFGetField(tif, TIFFTAG_PAGENUMBER, &page);

		TIFFSetField(out, TIFFTAG_IMAGEWIDTH, width);
		TIFFSetField(out, TIFFTAG_IMAGELENGTH, length);
		TIFFSetField(out, TIFFTAG_BITSPERSAMPLE, bps);
		TIFFSetField(out, TIFFTAG_SAMPLESPERPIXEL, nsamples);
		TIFFSetField(out, TIFFTAG_PLANARCONFIG, PLANARCONFIG_CONTIG);
		TIFFSetField(out, TIFFTAG_PHOTOMETRIC, PHOTOMETRIC_MINISBLACK);
		TIFFSetField(out, TIFFTAG_ORIENTATION, ORIENTATION_BOTLEFT);
		TIFFSetField(out, TIFFTAG_SUBFILETYPE, FILETYPE_PAGE);
		TIFFSetField(out, TIFFTAG_PAGENUMBER, page, dircount);

		buf = _TIFFmalloc(TIFFScanlineSize(tif));

		for (row = 0; row < length; row++) {
			TIFFReadScanline(tif, buf, row);

			uint16 *data = (uint16 *)buf;
			//uint32 *data = (uint32 *)buf;

			uint16 black = 0;
			//uint32 black = 0;

			for (col = 0; col < width; col++) {
				uint16 gray = data[col];
				//uint32 gray = data[col];
				if (gray != index) data[col] = black;
			}

			TIFFWriteScanline(out, buf, row);
		}

		TIFFWriteDirectory(out);

		_TIFFfree(buf);

		//page++;

	} while (TIFFReadDirectory(tif));

	TIFFClose(out);

	return 1;
}

int main(int argc, char* argv[]) {
	TIFF *tif = TIFFOpen(argv[1], "r");

	if (tif) {
		int dircount = 0;

		unordered_set<uint16> indexes;
		//unordered_set<uint32> indexes;

		do {
			dircount++;

			uint32 width, length, row, col;
			uint16 nsamples, config;
			tdata_t buf;
			
			TIFFGetField(tif, TIFFTAG_IMAGEWIDTH, &width);
			TIFFGetField(tif, TIFFTAG_IMAGELENGTH, &length);
			TIFFGetField(tif, TIFFTAG_SAMPLESPERPIXEL, &nsamples);
			TIFFGetField(tif, TIFFTAG_PLANARCONFIG, &config);

			buf = _TIFFmalloc(TIFFScanlineSize(tif));

			if (config == PLANARCONFIG_CONTIG) {
				for (row = 0; row < length; row++) {
					if (nsamples == 1) {
						TIFFReadScanline(tif, buf, row);

						uint16 *data = (uint16 *)buf;
						//uint32 *data = (uint32 *)buf;

						for (col = 0; col < width; col++) {
							//uint16 gray = static_cast<uint16*>(buf)[col*nsamples+0];

							uint16 gray = data[col];
							//uint32 gray = data[col];
							indexes.insert(gray);
							//cout << gray << endl;

							//printf("%u\n", data[col]);
						}
						
					} else if (nsamples == 3) {
						//uint16 r = static_cast<uint16*>(buf)[col*nsamples+0];
						//uint16 g = static_cast<uint16*>(buf)[col*nsamples+1];
						//uint16 b = static_cast<uint16*>(buf)[col*nsamples+2];
						//cout << r << " " << g << " " << b << endl;

						cerr << "RGB format not supported" << endl;

						return (EXIT_FAILURE);
					}
				}

				_TIFFfree(buf);

			} else if (config == PLANARCONFIG_SEPARATE) {
				cerr << "Separate planes not supported" << endl;

				return (EXIT_FAILURE);
			}

		} while (TIFFReadDirectory(tif));

		printf("%d directories in %s\n", dircount, argv[1]);

		for (unordered_set<uint16>::iterator a = indexes.begin(); a != indexes.end(); ++a) {
		//for (unordered_set<uint32>::iterator a = indexes.begin(); a != indexes.end(); ++a) {
			//cout << *a << endl;
			subdivide(*a, tif, dircount, argv[2]);
		}

		TIFFClose(tif);
	}

	//exit(0);
}
