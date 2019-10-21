// ImageJ macros
// segmentation pre-processing including despeckling and conversion to 8-bit
// Usage in headless mode from bash:
// ImageJ-linux64 --headless --console -macro Farsight.ijm "input_file.tif|output_file.tif"

arg = getArgument();
print("Running segmentation pre-processing:");
index = lastIndexOf(arg, "|");
if (index!=-1) input = substring(arg, 0, index);
if (index!=-1) output = substring(arg, index + 1, lengthOf(arg));
print("Input:");
print(input);
print("Output:");
print(output);
if (File.exists(input)) {
        open(input);
        run("Maximum Average Slice Intensity");
        resetMinAndMax();
        run("Despeckle", "stack");
        run("8-bit");
        run("Grays");
        saveAs("Tiff", output);
        run("Close");
        print("Done.");
} else {
  print("Incorrect argument.");
  eval("script", "System.exit(1);");
}
eval("script", "System.exit(0);");
