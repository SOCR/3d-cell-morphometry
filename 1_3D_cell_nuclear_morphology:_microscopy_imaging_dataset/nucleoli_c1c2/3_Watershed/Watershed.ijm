base = "09_14_SS_T0_Fibro_with_Fibrillarin_63x_4x4";

for (i = 0; i <= 2; i++) {
	c2 = "Z:\\PP_Results\\0000167\\" + base + "_c2_g" + IJ.pad(i, 3) + "_classified.tif";
	sa = "Z:\\PP_Results\\0000167\\" + base + "_c2_g" + IJ.pad(i, 3) + "_watershed.tif";	
	
	if (File.exists(c2)) {
		open(c2);
		
		//setSlice(nSlices/2.0);
		setSlice(19);
		
		run("Invert", "stack");
		
		setOption("BlackBackground", false);
		
		run("Convert to Mask", "method=Moments background=Default black");
		
		run("Invert", "stack");
		
		//run("Watershed", "stack");
		
		saveAs("Tiff", sa);
		
		run("Close");
	}
}