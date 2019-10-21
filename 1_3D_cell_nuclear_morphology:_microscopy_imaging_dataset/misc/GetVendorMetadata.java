/**
 *
 * /data/java/jdk1.6.0_23/bin/javac -cp ../lib/loci_tools-4.4.8.jar:../lib/commons-csv.jar:../lib/commons-io-2.4.jar:../lib/commons-cli-1.3.1.jar: GetVendorMetadata.java
 *
 * /data/java/jdk1.6.0_23/bin/java -Xmx512M -Xms512M -cp ../lib/loci_tools-4.4.8.jar:../lib/commons-csv.jar:../lib/commons-io-2.4.jar:../lib/commons-cli-1.3.1.jar: GetVendorMetadata -i /path/to/file.czi -o /path/to/file.meta
 *
 */
import java.io.*;
import java.util.*;
import java.sql.*;

import loci.common.*;
import loci.common.services.*;
import loci.common.xml.*;
import loci.formats.*;
import loci.formats.gui.*;
import loci.formats.in.*;
import loci.formats.meta.*;
import loci.formats.services.*;

import org.apache.commons.csv.*;
import org.apache.commons.cli.*;

/**
 * GetVendorMetadata is a utility class for reading a file
 * and reporting information about it.
 */
public class GetVendorMetadata {

	private static final String NEWLINE = System.getProperty("line.separator");
	private static final String NO_UPGRADE_CHECK = "-no-upgrade";

	private String id = null;
	private boolean doCore = true;
	private boolean doMeta = true;
	private boolean filter = true;
	private boolean thumbs = false;
	private boolean minmax = false;
	private boolean merge = false;
	private boolean stitch = false;
	private boolean group = true;
	private boolean separate = false;
	private boolean expand = false;
	private boolean omexml = false;
	private boolean originalMetadata = true;
	private boolean normalize = false;
	private boolean fastBlit = false;
	private boolean autoscale = false;
	private boolean preload = false;
	private boolean ascii = false;
	private boolean usedFiles = true;
	private boolean omexmlOnly = false;
	private boolean validate = true;
	private boolean flat = true;
	private String omexmlVersion = null;
	private int start = 0;
	private int end = Integer.MAX_VALUE;
	private int series = 0;
	private int resolution = 0;
	private int xCoordinate = 0, yCoordinate = 0, width = 0, height = 0;
	private String swapOrder = null, shuffleOrder = null;
	private String map = null;
	private String format = null;
	private int xmlSpaces = 3;

	private IFormatReader reader;
	private IFormatReader baseReader;
	private MinMaxCalculator minMaxCalc;
	private DimensionSwapper dimSwapper;
	private BufferedImageReader biReader;

	private LinkedHashMap<String, Object> kv = new LinkedHashMap<String, Object>();

	public void setReader(IFormatReader reader) {
		this.reader = reader;
	}

	public void createReader() {
		if (reader != null) return; // reader was set programmatically
		if (format != null) {
			// create reader of a specific format type
			try {
				Class<?> c = Class.forName("loci.formats.in." + format + "Reader");
				reader = (IFormatReader) c.newInstance();
			} catch (ClassNotFoundException exc) {
				//LOGGER.warn("Unknown reader: {}", format);
				//LOGGER.debug("", exc);
			} catch (InstantiationException exc) {
				//LOGGER.warn("Cannot instantiate reader: {}", format);
				//LOGGER.debug("", exc);
			} catch (IllegalAccessException exc) {
				//LOGGER.warn("Cannot access reader: {}", format);
				//LOGGER.debug("", exc);
			}
		}
		if (reader == null) reader = new ImageReader();
		baseReader = reader;
	}

	public void mapLocation() throws IOException {
		if (map != null) Location.mapId(id, map);
		else if (preload) {
			RandomAccessInputStream f = new RandomAccessInputStream(id);
			int len = (int) f.length();
			//LOGGER.info("Caching {} bytes:", len);
			byte[] b = new byte[len];
			int blockSize = 8 * 1024 * 1024; // 8 MB
			int read = 0, left = len;
			while (left > 0) {
				int r = f.read(b, read, blockSize < left ? blockSize : left);
				read += r;
				left -= r;
				float ratio = (float) read / len;
				int p = (int) (100 * ratio);
				//LOGGER.info("\tRead {} bytes ({}% complete)", read, p);
			}
			f.close();
			ByteArrayHandle file = new ByteArrayHandle(b);
			Location.mapFile(id, file);
		}
	}

	public void configureReaderPreInit() throws FormatException, IOException {
		if (omexml) {
			reader.setOriginalMetadataPopulated(originalMetadata);
			try {
				ServiceFactory factory = new ServiceFactory();
				OMEXMLService service = factory.getInstance(OMEXMLService.class);
				reader.setMetadataStore(
				service.createOMEXMLMetadata(null, omexmlVersion));
			} catch (DependencyException de) {
				throw new MissingLibraryException(OMEXMLServiceImpl.NO_OME_XML_MSG, de);
			} catch (ServiceException se) {
				throw new FormatException(se);
			}
		}

		// check file format
		if (reader instanceof ImageReader) {
			// determine format
			ImageReader ir = (ImageReader) reader;
			if (new Location(id).exists()) {
				//LOGGER.info("Checking file format [{}]", ir.getFormat(id));
			}
		} else {
			// verify format
			//LOGGER.info("Checking {} format [{}]", reader.getFormat(),
			//reader.isThisType(id) ? "yes" : "no");
		}

		//LOGGER.info("Initializing reader");
		if (stitch) {
			reader = new FileStitcher(reader, true);
			Location f = new Location(id);
			String pat = null;
			if (!f.exists()) {
				((FileStitcher) reader).setUsingPatternIds(true);
				pat = id;
			} else {
				pat = FilePattern.findPattern(f);
			}
			if (pat != null) id = pat;
		}

		if (expand) reader = new ChannelFiller(reader);
		if (separate) reader = new ChannelSeparator(reader);
		if (merge) reader = new ChannelMerger(reader);

		minMaxCalc = null;

		if (minmax || autoscale) reader = minMaxCalc = new MinMaxCalculator(reader);
		dimSwapper = null;

		if (swapOrder != null || shuffleOrder != null) {
			reader = dimSwapper = new DimensionSwapper(reader);
		}

		reader = biReader = new BufferedImageReader(reader);

		reader.close();
		reader.setNormalized(normalize);
		reader.setMetadataFiltered(filter);
		reader.setGroupFiles(group);
		MetadataOptions metaOptions = new DefaultMetadataOptions(doMeta ?
		MetadataLevel.ALL : MetadataLevel.MINIMUM);
		reader.setMetadataOptions(metaOptions);
		reader.setFlattenedResolutions(flat);
	}

	public void configureReaderPostInit() {
		if (swapOrder != null) dimSwapper.swapDimensions(swapOrder);
		if (shuffleOrder != null) dimSwapper.setOutputOrder(shuffleOrder);
	}

	public void checkWarnings() {
		if (!normalize && (reader.getPixelType() == FormatTools.FLOAT ||
			reader.getPixelType() == FormatTools.DOUBLE)) {

			//LOGGER.warn("");
			//LOGGER.warn("Java does not support " +
			//"display of unnormalized floating point data.");
			//LOGGER.warn("Please use the '-normalize' option " +
			//"to avoid receiving a cryptic exception.");
		}

		if (reader.isRGB() && reader.getRGBChannelCount() > 4) {
			//LOGGER.warn("");
			//LOGGER.warn("Java does not support merging more than 4 channels.");
			//LOGGER.warn("Please use the '-separate' option " +
			//"to avoid losing channels beyond the 4th.");
		}
	}

	public void readCoreMetadata() throws FormatException, IOException {
		if (!doCore) return; // skip core metadata printout

		// read basic metadata
		//LOGGER.info("");
		//LOGGER.info("Reading core metadata");
		//LOGGER.info("{} = {}", stitch ? "File pattern" : "Filename",
		//stitch ? id : reader.getCurrentFile());
		//if (map != null) LOGGER.info("Mapped filename = {}", map);
		if (usedFiles) {
			String[] used = reader.getUsedFiles();
			boolean usedValid = used != null && used.length > 0;
			if (usedValid) {
				for (int u=0; u<used.length; u++) {
					if (used[u] == null) {
						usedValid = false;
						break;
					}
				}
			}
	
			if (!usedValid) {
				//LOGGER.warn("************ invalid used files list ************");
			}
	
			if (used == null) {
				//LOGGER.info("Used files = null");
			} else if (used.length == 0) {
				//LOGGER.info("Used files = []");
			} else if (used.length > 1) {
				//LOGGER.info("Used files:");
				//for (int u=0; u<used.length; u++) LOGGER.info("\t{}", used[u]);
			} else if (!id.equals(used[0])) {
				//LOGGER.info("Used files = [{}]", used[0]);
			}
		}

		int seriesCount = reader.getSeriesCount();
		//LOGGER.info("Series count = {}", seriesCount);
		MetadataStore ms = reader.getMetadataStore();
		MetadataRetrieve mr = ms instanceof MetadataRetrieve ? (MetadataRetrieve) ms : null;
		for (int j=0; j<seriesCount; j++) {
			reader.setSeries(j);

			// read basic metadata for series #i
			int imageCount = reader.getImageCount();
			int resolutions = reader.getResolutionCount();
			boolean rgb = reader.isRGB();
			int sizeX = reader.getSizeX();
			int sizeY = reader.getSizeY();
			int sizeZ = reader.getSizeZ();
			int sizeC = reader.getSizeC();
			int sizeT = reader.getSizeT();
			int pixelType = reader.getPixelType();
			int validBits = reader.getBitsPerPixel();
			int effSizeC = reader.getEffectiveSizeC();
			int rgbChanCount = reader.getRGBChannelCount();
			boolean indexed = reader.isIndexed();
			boolean falseColor = reader.isFalseColor();
			byte[][] table8 = reader.get8BitLookupTable();
			short[][] table16 = reader.get16BitLookupTable();
			int[] cLengths = reader.getChannelDimLengths();
			String[] cTypes = reader.getChannelDimTypes();
			int thumbSizeX = reader.getThumbSizeX();
			int thumbSizeY = reader.getThumbSizeY();
			boolean little = reader.isLittleEndian();
			String dimOrder = reader.getDimensionOrder();
			boolean orderCertain = reader.isOrderCertain();
			boolean thumbnail = reader.isThumbnailSeries();
			boolean interleaved = reader.isInterleaved();
			boolean metadataComplete = reader.isMetadataComplete();

			// output basic metadata for series #i
			String seriesName = mr == null ? null : mr.getImageName(j);
			//LOGGER.info("Series #{}{}{}:",
			//new Object[] {j, seriesName == null ? " " : " -- ",
			//seriesName == null ? "" : seriesName});

			if (flat == false && resolutions > 1) {
				//LOGGER.info("\tResolutions = {}", resolutions);
				for (int i = 0; i < resolutions; i++) {
					reader.setResolution(i);
					//LOGGER.info("\t\tsizeX[{}] = {}", i, reader.getSizeX());
				}
				reader.setResolution(0);
			}
	
			//LOGGER.info("\tImage count = {}", imageCount);
			//LOGGER.info("\tRGB = {} ({}) {}", new Object[] {rgb, rgbChanCount,
			//merge ? "(merged)" : separate ? "(separated)" : ""});
			if (rgb != (rgbChanCount != 1)) {
				//LOGGER.warn("\t************ RGB mismatch ************");
			}
			//LOGGER.info("\tInterleaved = {}", interleaved);
	
			StringBuilder sb = new StringBuilder();
			sb.append("\tIndexed = ");
			sb.append(indexed);
			sb.append(" (");
			sb.append(!falseColor);
			sb.append(" color");
			if (table8 != null) {
				sb.append(", 8-bit LUT: ");
				sb.append(table8.length);
				sb.append(" x ");
				sb.append(table8[0] == null ? "null" : "" + table8[0].length);
			}
			if (table16 != null) {
				sb.append(", 16-bit LUT: ");
				sb.append(table16.length);
				sb.append(" x ");
				sb.append(table16[0] == null ? "null" : "" + table16[0].length);
			}
			sb.append(")");
			//LOGGER.info(sb.toString());
	
			if (table8 != null && table16 != null) {
				//LOGGER.warn("\t************ multiple LUTs ************");
			}
			//LOGGER.info("\tWidth = {}", sizeX);
			//LOGGER.info("\tHeight = {}", sizeY);
			//LOGGER.info("\tSizeZ = {}", sizeZ);
			//LOGGER.info("\tSizeT = {}", sizeT);
	
			sb.setLength(0);
			sb.append("\tSizeC = ");
			sb.append(sizeC);
			if (sizeC != effSizeC) {
				sb.append(" (effectively ");
				sb.append(effSizeC);
				sb.append(")");
			}
			int cProduct = 1;
			if (cLengths.length == 1 && FormatTools.CHANNEL.equals(cTypes[0])) {
				cProduct = cLengths[0];
			} else {
				sb.append(" (");
				for (int i=0; i<cLengths.length; i++) {
					if (i > 0) sb.append(" x ");
					sb.append(cLengths[i]);
					sb.append(" ");
					sb.append(cTypes[i]);
					cProduct *= cLengths[i];
				}
				sb.append(")");
			}
			//LOGGER.info(sb.toString());
	
			if (cLengths.length == 0 || cProduct != sizeC) {
				//LOGGER.warn("\t************ C dimension mismatch ************");
			}
			if (imageCount != sizeZ * effSizeC * sizeT) {
				//LOGGER.info("\t************ ZCT mismatch ************");
			}
			//LOGGER.info("\tThumbnail size = {} x {}", thumbSizeX, thumbSizeY);
			//LOGGER.info("\tEndianness = {}",
			//little ? "intel (little)" : "motorola (big)");
			//LOGGER.info("\tDimension order = {} ({})", dimOrder,
			//orderCertain ? "certain" : "uncertain");
			//LOGGER.info("\tPixel type = {}",
			//FormatTools.getPixelTypeString(pixelType));
			//LOGGER.info("\tValid bits per pixel = {}", validBits);
			//LOGGER.info("\tMetadata complete = {}", metadataComplete);
			//LOGGER.info("\tThumbnail series = {}", thumbnail);
			if (doMeta) {
				//LOGGER.info("\t-----");
				int[] indices;
				if (imageCount > 6) {
					int q = imageCount / 2;
					indices = new int[] {
						0, q - 2, q - 1, q, q + 1, q + 2, imageCount - 1
					};
				} else if (imageCount > 2) {
					indices = new int[] {0, imageCount / 2, imageCount - 1};
				} else if (imageCount > 1) indices = new int[] {0, 1};
				else indices = new int[] {0};
				int[][] zct = new int[indices.length][];
				int[] indices2 = new int[indices.length];
	
				sb.setLength(0);
				for (int i=0; i<indices.length; i++) {
					zct[i] = reader.getZCTCoords(indices[i]);
					indices2[i] = reader.getIndex(zct[i][0], zct[i][1], zct[i][2]);
					sb.append("\tPlane #");
					sb.append(indices[i]);
					sb.append(" <=> Z ");
					sb.append(zct[i][0]);
					sb.append(", C ");
					sb.append(zct[i][1]);
					sb.append(", T ");
					sb.append(zct[i][2]);
					if (indices[i] != indices2[i]) {
						sb.append(" [mismatch: ");
						sb.append(indices2[i]);
						sb.append("]");
						sb.append(NEWLINE);
					} else sb.append(NEWLINE);
				}
				//LOGGER.info(sb.toString());
			}
		}
	}
	
	public void printGlobalMetadata() {
		//LOGGER.info("");
		//LOGGER.info("Reading global metadata");
		Hashtable<String, Object> meta = reader.getGlobalMetadata();
		String[] keys = MetadataTools.keys(meta);
		for (String key : keys) {
			//LOGGER.info("{}: {}", key, meta.get(key));
			//System.out.println(key + " >>>>>>>> " + meta.get(key));
			kv.put(key, meta.get(key));
		}
	}

	public void printOriginalMetadata() {
		String seriesLabel = reader.getSeriesCount() > 1 ?  (" series #" + series) : "";
		//LOGGER.info("");
		//LOGGER.info("Reading{} metadata", seriesLabel);
		Hashtable<String, Object> meta = reader.getSeriesMetadata();
		String[] keys = MetadataTools.keys(meta);
		for (int i=0; i<keys.length; i++) {
			//LOGGER.info("{}: {}", keys[i], meta.get(keys[i]));
			//System.out.println(keys[i] + " >>>>>>>> " + meta.get(keys[i]));
			kv.put(keys[i], meta.get(keys[i]));
		}
	}

	/**
	* A utility method for reading a file from the command line,
	* and displaying the results in a simple display.
	*/
	public LinkedHashMap<String, Object> testRead(String path) throws FormatException, ServiceException, IOException {
		id = path;

		createReader();

		if (id != null) {
			mapLocation();
			configureReaderPreInit();

			// initialize reader
			reader.setId(id);

			configureReaderPostInit();
			checkWarnings();
			readCoreMetadata();
			reader.setSeries(series);
			if (flat == false) reader.setResolution(resolution);

			// read format-specific metadata table
			if (doMeta) {
				printGlobalMetadata();
				printOriginalMetadata();
			}

			reader.close();
		}

		return kv;
	}

	// -- Main method --

	public static void main(String[] args) throws Exception {
		Options options = new Options();
		Option in = new Option("i", true, "Path to the input czi file");
		in.setRequired(true);
		in.setType(String.class);
		options.addOption(in);

      Option out = new Option("o", true, "Path to the output meta file");
      out.setRequired(true);
      out.setType(String.class);
		options.addOption(out);

		CommandLineParser parser = new DefaultParser();

		CSVPrinter csv = null;

		try {
			String input = "";
			String output = "";

			CommandLine line = parser.parse(options, args);

			if (line.hasOption("i")) input = line.getOptionValue("i");

			File input_file = new File(input);
			if (!input_file.isFile()) throw new FileNotFoundException(input + " not found or not a normal file");

			if (line.hasOption("o")) output = line.getOptionValue("o");

			File output_file = new File(output);

			LinkedHashMap<String, Object> tmp = new GetVendorMetadata().testRead(input);

			PrintStream print = new PrintStream(new FileOutputStream(output, true));
			csv = new CSVPrinter(print, CSVFormat.DEFAULT);

			//csv.printRecord("pp_version", "1.1");
			//csv.printRecord("pp_data", PathUtils.getRelativePath(input_file.getCanonicalPath(), output_file.getCanonicalPath(), File.separator));

			for (String key : tmp.keySet()) {
				csv.printRecord(key, tmp.get(key));
			}

			csv.flush();

		} catch (ParseException pe) {
			System.err.println("Error: " + pe.getMessage());

			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("GetVendorMetadata", options, true);

		} catch (FileNotFoundException fe) {
			System.err.println(fe.getMessage());

		} catch (Exception e) {
			System.err.println(e.getMessage());

		} finally {
			if (csv != null) csv.close();
		}
	}
}
