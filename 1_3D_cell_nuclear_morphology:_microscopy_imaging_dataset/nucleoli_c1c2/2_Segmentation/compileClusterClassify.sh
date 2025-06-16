export JAVA_HOME="../runtime/jdk1.8.0_111"
export PATH="$JAVA_HOME/bin:$PATH"
bin_path="../runtime"

javac -cp $bin_path:$bin_path/commons-cli-1.2.jar:$bin_path/ij-1.51n.jar:$bin_path/Trainable_Segmentation-3.2.19.jar:$bin_path/weka-dev-3.9.1.jar:$bin_path/imglib2-4.2.1.jar:$bin_path/imglib2-ij-2.0.0-beta-38.jar:$bin_path/imglib2-algorithm-0.8.1.jar:$bin_path/bounce-0.18.jar:$bin_path/ui-behavior-1.3.0.jar:$bin_path/mtj-1.0.4.jar ClusterClassify.java
