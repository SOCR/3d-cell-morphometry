/**
 *
 * /data/java/jdk1.6.0_23/bin/javac -cp ../lib/loci_tools-4.4.8.jar:../lib/ome-io-4.4.8.jar:../lib/scifio-5.0.0b1.jar:../lib/ij-1.49s.jar:../lib/commons-csv.jar:../lib/commons-io-2.4.jar:../lib/commons-cli-1.3.1.jar: CropAndConvertVendorData.java
 *
 * /data/java/jdk1.6.0_23/bin/java -Xmx512M -Xms512M -cp ../lib/loci_tools-4.4.8.jar:../lib/ome-io-4.4.8.jar:../lib/scifio-5.0.0b1.jar:../lib/ij-1.49s.jar:../lib/commons-csv/commons-csv.jar:../lib/commons-io-2.4/commons-io-2.4.jar:../lib/commons-cli-1.3.1.jar: CropAndConvertVendorData -i /path/to/file.czi -o /dir/to/output
 *
 */
import java.awt.image.IndexColorModel;
import java.io.*;
import java.util.*;

import loci.common.*;
import loci.common.services.*;
import loci.formats.*;
import loci.formats.in.OMETiffReader;
import loci.formats.meta.*;
import loci.formats.out.TiffWriter;
import loci.formats.ome.OMEXMLMetadataRoot;
import loci.formats.services.*;
import loci.formats.tiff.IFD;

import ome.xml.model.Image;
import ome.xml.model.enums.PixelType;
import ome.xml.model.primitives.PositiveInteger;

import ij.ImagePlus;
import ij.IJ;
import ij.Prefs;
import ij.io.Opener;
import ij.io.FileInfo;

import org.apache.commons.cli.*;

/**
 * CropAndConvertVendorData is a utility class for converting a file between 
 * formats and cropping.
 */
public final class CropAndConvertVendorData {
	private String in = null, out = null;
	private String map = null;
	private String compression = null;
	private boolean separate = true;
	private boolean bigtiff = false, group = true;
	private boolean autoscale = false;
	private int series = -1;
	private int firstPlane = 0;
	private int lastPlane = Integer.MAX_VALUE;
	private int channel = -1, zSection = -1, timepoint = -1;
	private int xCoordinate = 0, yCoordinate = 0, width = 0, height = 0;

	private IFormatReader reader;
	private MinMaxCalculator minMax;

	static private LinkedHashMap<String, String> outset = new LinkedHashMap<String, String>();

	static private int totalGridCount = 0;
	static private int totalChannelCount = 0;

	private CropAndConvertVendorData() {
	}

	public void testConvert(String input, String output) throws FormatException, IOException {
		long start = System.currentTimeMillis();

		reader = new ImageReader();

		if (separate) reader = new ChannelSeparator(reader);

		reader.setGroupFiles(group);
		reader.setMetadataFiltered(true);
		reader.setOriginalMetadataPopulated(true);

		OMEXMLService service = null;

		try {
			ServiceFactory factory = new ServiceFactory();
			service = factory.getInstance(OMEXMLService.class);
			reader.setMetadataStore(service.createOMEXMLMetadata());
		} catch (DependencyException de) {
			throw new MissingLibraryException(OMEXMLServiceImpl.NO_OME_XML_MSG, de);
		} catch (ServiceException se) {
			throw new FormatException(se);
		}

		in = input;

		try {
			reader.setId(in);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

		int cw = reader.getSizeX();
		int ch = reader.getSizeY();
		int bw = 1024;
		int bh = 1024;
		int tw = (int)Math.ceil((double)cw/(double)bw);
		int th = (int)Math.ceil((double)ch/(double)bh);

		totalChannelCount = reader.getEffectiveSizeC();

		int cnt = 0;

		for (int ai = 0; ai < th; ai++) {
			for (int aj = 0; aj < tw; aj++) {
				totalGridCount++;

				IFormatWriter writer = new ImageWriter();

				xCoordinate = aj*bw;
				yCoordinate = ai*bh;
				width = (aj+1 == tw) ? (cw%bw == 0) ? bw : cw%bw : bw;
				height = (ai+1 == th) ? (ch%bh == 0) ? bh : ch%bh : bh;

				File f = new File(in);
				int ind = f.getName().lastIndexOf(".");

				File fout = new File(output, f.getName().substring(0, ind) + "_c%c_g" + String.format("%03d", cnt) + ".tif");
				String out = fout.getCanonicalPath();

				MetadataStore store = reader.getMetadataStore();

				MetadataTools.populatePixels(store, reader, false, false);

				boolean dimensionsSet = true;

				if (width == 0 || height == 0) {
					width = reader.getSizeX();
					height = reader.getSizeY();
					dimensionsSet = false;
				}

				if (store instanceof MetadataRetrieve) {
					if (series >= 0) {
						try {
							String xml = service.getOMEXML(service.asRetrieve(store));
							OMEXMLMetadataRoot root = (OMEXMLMetadataRoot) store.getRoot();
							Image exportImage = root.getImage(series);

							IMetadata meta = service.createOMEXMLMetadata(xml);
							OMEXMLMetadataRoot newRoot = (OMEXMLMetadataRoot) meta.getRoot();
							while (newRoot.sizeOfImageList() > 0) {
								newRoot.removeImage(newRoot.getImage(0));
							}

							newRoot.addImage(exportImage);
							meta.setRoot(newRoot);

							meta.setPixelsSizeX(new PositiveInteger(width), 0);
							meta.setPixelsSizeY(new PositiveInteger(height), 0);

							if (channel >= 0) {
								meta.setPixelsSizeC(new PositiveInteger(1), 0);
							}

							if (zSection >= 0) {
								meta.setPixelsSizeZ(new PositiveInteger(1), 0);
							}

							if (timepoint >= 0) {
								meta.setPixelsSizeT(new PositiveInteger(1), 0);
							}

							writer.setMetadataRetrieve((MetadataRetrieve) meta);
						} catch (ServiceException e) {
							throw new FormatException(e);
						}
					} else {
						for (int i=0; i<reader.getSeriesCount(); i++) {
							if (width != reader.getSizeX() || height != reader.getSizeY()) {
								store.setPixelsSizeX(new PositiveInteger(width), 0);
								store.setPixelsSizeY(new PositiveInteger(height), 0);
							}

							if (autoscale) {
								store.setPixelsType(PixelType.UINT8, i);
							}

							if (channel >= 0) {
								store.setPixelsSizeC(new PositiveInteger(1), 0);
							}

							if (zSection >= 0) {
								store.setPixelsSizeZ(new PositiveInteger(1), 0);
							}

							if (timepoint >= 0) {
								store.setPixelsSizeT(new PositiveInteger(1), 0);
							}
						}

						writer.setMetadataRetrieve((MetadataRetrieve) store);
					}
				}

				writer.setWriteSequentially(true);

				if (writer instanceof TiffWriter) {
					((TiffWriter) writer).setBigTiff(bigtiff);
				} else if (writer instanceof ImageWriter) {
					IFormatWriter w = ((ImageWriter) writer).getWriter(out);
					if (w instanceof TiffWriter) {
						((TiffWriter) w).setBigTiff(bigtiff);
					}
				}

				String format = writer.getFormat();
				long mid = System.currentTimeMillis();

				int total = 0;
				int num = writer.canDoStacks() ? reader.getSeriesCount() : 1;
				long read = 0, write = 0;
				int first = series == -1 ? 0 : series;
				int last = series == -1 ? num : series + 1;
				long timeLastLogged = System.currentTimeMillis();

				for (int q=first; q<last; q++) {
					reader.setSeries(q);

					if (!dimensionsSet) {
						width = reader.getSizeX();
						height = reader.getSizeY();
					}

					int writerSeries = series == -1 ? q : 0;
					writer.setSeries(writerSeries);
					writer.setInterleaved(reader.isInterleaved() && !autoscale);
					writer.setValidBitsPerPixel(reader.getBitsPerPixel());
					int numImages = writer.canDoStacks() ? reader.getImageCount() : 1;

					int startPlane = Math.max(0, firstPlane);
					int endPlane = Math.min(numImages, lastPlane);
					numImages = endPlane - startPlane;

					if (channel >= 0) {
						numImages /= reader.getEffectiveSizeC();
					}

					if (zSection >= 0) {
						numImages /= reader.getSizeZ();
					}

					if (timepoint >= 0) {
						numImages /= reader.getSizeT();
					}

					total += numImages;

					int count = 0;
					for (int i=startPlane; i<endPlane; i++) {
						int[] coords = reader.getZCTCoords(i);
	
						outset.put(FormatTools.getFilename(q, i, reader, out), Integer.toString(coords[1]));

						if ((zSection >= 0 && coords[0] != zSection) || (channel >= 0 && coords[1] != channel) || (timepoint >= 0 && coords[2] != timepoint)) {
							continue;
						}

						writer.setId(FormatTools.getFilename(q, i, reader, out));
						if (compression != null) writer.setCompression(compression);

						long s = System.currentTimeMillis();
						long m = convertPlane(writer, i, startPlane);
						long e = System.currentTimeMillis();
						read += m - s;
						write += e - m;

						// log number of planes processed every second or so
						if (count == numImages - 1 || (e - timeLastLogged) / 1000 > 0) {
							int current = (count - startPlane) + 1;
							int percent = 100 * current / numImages;
							StringBuilder sb = new StringBuilder();
							sb.append("\t");
							int numSeries = last - first;
	
							if (numSeries > 1) {
								sb.append("Series ");
								sb.append(q);
								sb.append(": converted ");
							} else sb.append("Converted ");
	
							timeLastLogged = e;
						}
						count++;
					}
				}

				writer.close();

				long end = System.currentTimeMillis();

				// output timing results
				float sec = (end - start) / 1000f;
				long initial = mid - start;
				float readAvg = (float) read / total;
				float writeAvg = (float) write / total;

				cnt++;
			}
		}
	}

	private long convertPlane(IFormatWriter writer, int index, int startPlane) throws FormatException, IOException {

		if (DataTools.safeMultiply64(width, height) >=
			DataTools.safeMultiply64(4096, 4096)) {

			// this is a "big image", so we will attempt to convert it one tile
			// at a time

			if ((writer instanceof TiffWriter) || ((writer instanceof ImageWriter) && (((ImageWriter) writer).getWriter(out) instanceof TiffWriter))) {
				return convertTilePlane(writer, index, startPlane);
			}
		}

		byte[] buf =
			reader.openBytes(index, xCoordinate, yCoordinate, width, height);

		autoscalePlane(buf, index);
		applyLUT(writer);
		long m = System.currentTimeMillis();
		writer.saveBytes(index - startPlane, buf);

		return m;
	}

	private long convertTilePlane(IFormatWriter writer, int index, int startPlane) throws FormatException, IOException {
		int w = reader.getOptimalTileWidth();
		int h = reader.getOptimalTileHeight();
		int nXTiles = width / w;
		int nYTiles = height / h;

		if (nXTiles * w != width) {
			nXTiles++;
		}

		if (nYTiles * h != height) {
			nYTiles++;
		}

		IFD ifd = new IFD();
		ifd.put(IFD.TILE_WIDTH, w);
		ifd.put(IFD.TILE_LENGTH, h);

		Long m = null;
		for (int y=0; y<nYTiles; y++) {
			for (int x=0; x<nXTiles; x++) {
				int tileX = xCoordinate + x * w;
				int tileY = yCoordinate + y * h;
				int tileWidth = x < nXTiles - 1 ? w : width % w;
				int tileHeight = y < nYTiles - 1 ? h : height % h;
				byte[] buf =
					reader.openBytes(index, tileX, tileY, tileWidth, tileHeight);

				autoscalePlane(buf, index);
				applyLUT(writer);
				if (m == null) {
					m = System.currentTimeMillis();
				}

				if (writer instanceof TiffWriter) {
					((TiffWriter) writer).saveBytes(index - startPlane, buf,
						ifd, tileX, tileY, tileWidth, tileHeight);
				} else if (writer instanceof ImageWriter) {
					IFormatWriter baseWriter = ((ImageWriter) writer).getWriter(out);
					if (baseWriter instanceof TiffWriter) {
						((TiffWriter) baseWriter).saveBytes(index - startPlane, buf,
							ifd, tileX, tileY, tileWidth, tileHeight);
					}
				}
			}
		}

		return m;
	}

	private void autoscalePlane(byte[] buf, int index) throws FormatException, IOException {
		if (autoscale) {
			Double min = null;
			Double max = null;

			Double[] planeMin = minMax.getPlaneMinimum(index);
			Double[] planeMax = minMax.getPlaneMaximum(index);

			if (planeMin != null && planeMax != null) {
				min = planeMin[0];
				max = planeMax[0];

				for (int j=1; j<planeMin.length; j++) {
					if (planeMin[j].doubleValue() < min.doubleValue()) {
						min = planeMin[j];
					}
					if (planeMax[j].doubleValue() < max.doubleValue()) {
						max = planeMax[j];
					}
				}
			}

			int pixelType = reader.getPixelType();
			int bpp = FormatTools.getBytesPerPixel(pixelType);
			boolean floatingPoint = FormatTools.isFloatingPoint(pixelType);
			Object pix = DataTools.makeDataArray(buf, bpp, floatingPoint,
			reader.isLittleEndian());
			byte[][] b = ImageTools.make24Bits(pix, width, height,
				reader.isInterleaved(), false, min, max);

			int channelCount = reader.getRGBChannelCount();
			int copyComponents = Math.min(channelCount, b.length);

			buf = new byte[channelCount * b[0].length];
			for (int j=0; j<copyComponents; j++) {
				System.arraycopy(b[j], 0, buf, b[0].length * j, b[0].length);
			}
		}
	}

	private void applyLUT(IFormatWriter writer) throws FormatException, IOException {
		byte[][] lut = reader.get8BitLookupTable();
		if (lut != null) {
			IndexColorModel model = new IndexColorModel(8, lut[0].length,
			lut[0], lut[1], lut[2]);
			writer.setColorModel(model);
		}
	}

	public static void main(String[] args) throws FormatException, IOException {
		Options options = new Options();

		Option in = new Option("i", true, "Path to the input czi file");
		in.setRequired(true);
		in.setType(String.class);
		options.addOption(in);

		Option out = new Option("o", true, "Path to the output directory");
		out.setRequired(true);
		out.setType(String.class);
		options.addOption(out);

		CommandLineParser parser = new DefaultParser();

		try {
			String input = "";
			String output = "";

			CommandLine line = parser.parse(options, args);

			if (line.hasOption("i")) input = line.getOptionValue("i");

			File input_file = new File(input);
			if (!input_file.isFile()) throw new FileNotFoundException(input + " not found or not a normal file");

			if (line.hasOption("o")) output = line.getOptionValue("o");

			//File output_file = new File(output);
			//if (!output_file.isDirectory()) throw new FileNotFoundException(output + " not found or not a directory");

			new CropAndConvertVendorData().testConvert(input, output);

		} catch (ParseException pe) {
			System.err.println("Error: " + pe.getMessage());
					
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("GetVendorMetadata", options, true);
											
		} catch (FileNotFoundException fe) {
			System.err.println(fe.getMessage());
											
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
}
