base = "09_14_SS_T0_Fibro_with_Fibrillarin_63x_4x4";
	
for (grid = 0; grid <= 15; grid++) {
	run("Clear Results");
	
	for (i = 0; i <= 300; i++) {
		c1 = "Z:\\PP_Results\\0000167\\" + base + "_c1_g" + IJ.pad(grid, 3) + "_mask.tif";
		c2 = "Z:\\PP_Results\\0000167\\" + base + "_c2_g" + IJ.pad(grid, 3) + "_connected-" + IJ.pad(i, 3) + ".tif";
		sa = "Z:\\PP_Results\\0000167\\" + base + "_c2_g" + IJ.pad(grid, 3) + "_statistics.txt";
		
		if (File.exists(c1)) {
			if (File.exists(c2)) {
				open(c1);
				open(c2);
				
				imageCalculator("Multiply create 32-bit stack", base + "_c1_g" + IJ.pad(grid, 3) + "_mask.tif",base + "_c2_g" + IJ.pad(grid, 3) + "_connected-" + IJ.pad(i, 3) + ".tif");
				selectWindow("Result of " + base + "_c1_g" + IJ.pad(grid, 3) + "_mask.tif");

				Stack.getStatistics(count, mean, min, max, std);
	
				setResult("Grid", i, IJ.pad(grid, 3));
				setResult("Blob", i, IJ.pad(i, 3));
				setResult("Count", i, count);
				setResult("Mean", i, mean);
				setResult("Min", i, min);
				setResult("Max", i, max);
				setResult("Std", i, std);

				setOption("ShowRowNumbers", false);
	
				updateResults();
	
				selectWindow("Result of " + base + "_c1_g" + IJ.pad(grid, 3) + "_mask.tif");
				close();
				selectWindow(base + "_c1_g" + IJ.pad(grid, 3) + "_mask.tif");
				close();
				selectWindow(base + "_c2_g" + IJ.pad(grid, 3) + "_connected-" + IJ.pad(i, 3) + ".tif");
				close();
			}
		}
	}

	saveAs("Results", sa);
}