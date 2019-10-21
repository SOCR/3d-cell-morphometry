base = "09_14_SS_T0_Fibro_with_Fibrillarin_63x_4x4";

for (i = 0; i <= 15; i++) {
	//c2 = "Z:\\PP_Results\\0000167\\" + base + "_c2_g" + IJ.pad(i, 3) + "_watershed.tif";
	c2 = "Z:\\PP_Results\\0000167\\" + base + "_c2_g" + IJ.pad(i, 3) + "_filled.tif";
	sa = "Z:\\PP_Results\\0000167\\" + base + "_c2_g" + IJ.pad(i, 3) + "_connected.tif";

	if (File.exists(c2)) {	
		open(c2);
		//setOption("BlackBackground", false);
		//run("Erode", "stack");
		run("Dilate", "stack");
		//run("Invert", "stack");
		run("Find Connected Regions", "allow_diagonal display_one_image regions_for_values_over=100 minimum_number_of_points=1 stop_after=-1");
		saveAs("Tiff", sa);
		close();
		//selectWindow(base + "_c2_g" + IJ.pad(i, 3) + "_watershed.tif");
		selectWindow(base + "_c2_g" + IJ.pad(i, 3) + "_filled.tif");
		close();
	}
}