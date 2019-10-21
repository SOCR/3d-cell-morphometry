import trainableSegmentation.*;
import trainableSegmentation.WekaSegmentation;
import trainableSegmentation.FeatureStackArray;
import ij.*;
import org.apache.commons.cli.*;
import java.util.*;

public class ClusterClassify {
	public static void main(String[] args) throws Exception {
		Options options = new Options();

		options.addOption("3d", false, "use 3d features");
                options.addOption("nt", true, "number of threads");
		options.addOption("m", true, "path to classifier model");
		options.addOption("i", true, "path to input file");
		options.addOption("o", true, "path to output file");

		CommandLineParser parser = new BasicParser();

		String model = null;
		String input = null;
		String output = null;
		boolean use_3d = false;
                int num_threads = 16;

		try {
			CommandLine cmd = parser.parse(options, args);

			if (cmd.hasOption("3d")) {
				use_3d = true;
			}

			if (cmd.hasOption("nt")) {
                                System.out.println(cmd.getOptionValue("nt"));
				num_threads = Integer.parseInt(cmd.getOptionValue("nt"));
                        }

			if (cmd.hasOption("m")) {
				model = cmd.getOptionValue("m");
			}

			if (cmd.hasOption("i")) {
				input = cmd.getOptionValue("i");
			}

			if (cmd.hasOption("o")) {
				output = cmd.getOptionValue("o");
			}

			if (model == null || input == null || output == null) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("ClusterClassify", options);
				System.exit(-1);
			}

                        Prefs.setThreads(num_threads);

			System.out.println("Open Image");
			ImagePlus ip = IJ.openImage(input);
			System.out.println(ip.toString());

			System.out.println("Create Segmentor");
			//WekaSegmentation ws = new WekaSegmentation(ip);
			//WekaSegmentation ws = new WekaSegmentation();
			//WekaSegmentation ws = new WekaSegmentation(true);
			WekaSegmentation ws = new WekaSegmentation(use_3d);
			//ws.setTrainingImage(ip);

			System.out.println("Load Classifier");
			ws.loadClassifier(model);

			//ws.loadNewImage(ip);

			System.out.println("Apply Classifier");
			//ws.applyClassifier(false);
			ImagePlus ret = ws.applyClassifier(ip, 0, false);

			System.out.println("Read header from " + model +
				" (number of attributes = " + ws.getTrainHeader().numAttributes() +
				")");

			System.out.println("Save Image");
			//ImagePlus ret = ws.getClassifiedImage();
			System.out.println(ret.toString());
			IJ.save(ret, output);

			ret.close();
			ip.close();

		} catch(ParseException e) {
			System.err.println(e.getMessage());

			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("ClusterClassify", options);
		}
	}
}
