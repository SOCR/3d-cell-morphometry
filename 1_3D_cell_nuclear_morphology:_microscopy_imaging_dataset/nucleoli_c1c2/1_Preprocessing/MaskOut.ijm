arg = getArgument();
print("Running segmentation pre-processing:");
index = lastIndexOf(arg, "|");
if (index!=-1) input = substring(arg, 0, index);
if (index!=-1) output = substring(arg, index + 1, lengthOf(arg));
index = lastIndexOf(input, "|");
if (index!=-1) c0 = substring(input, 0, index);
if (index!=-1) c2 = substring(input, index + 1, lengthOf(input));
print("c0:");
print(c0);
print("c2:");
print(c2);
print("Output:");
print(output);
index = lastIndexOf(c0, "/");
if (index!=-1) c0name = substring(c0, index + 1, lengthOf(c0));
index = lastIndexOf(c2, "/");
if (index!=-1) c2name = substring(c2, index + 1, lengthOf(c2));

if (File.exists(c0)) {
	if (File.exists(c2)) {
		open(c0);
		open(c2);
		selectWindow(c0name);
		setOption("BlackBackground", false);
		run("Make Binary", "method=Huang background=Default calculate black");
		imageCalculator("Multiply create 32-bit stack", c0name, c2name);
		selectWindow("Result of " + c0name);
		run("Despeckle", "stack");
		run("16-bit");
		saveAs("Tiff", output);
		close();
		selectWindow(c0name);
		close();
		selectWindow(c2name);
		close();
	}
}
