#!/bin/bash
# first parameter - input TIF volume
# second parameter - output segmented TIF volume

# takes one binary mask and passes it through curation and filtering protocol
# and outputs .lst file with the input file name inside if it passed the curation or empty otherwise

while [[ $# > 1 ]]
do
key="$1"

case $key in
    -i|--input)
    input_file="$2"
    echo input file = "$input_file"
    shift # past argument
    ;;
    -o|--output)
    output_file="$2"
    echo output file = "$output_file"
    shift # past argument
    ;;
    -m|--model)
    model_file="$2"
    echo model file = "$model_file"
    shift # past argument
    ;;
    -j|--java)
    java_path="$2"
    echo Java home = "$java_path"
    shift # past argument
    ;;
    -b|--bin)
    bin_path="$2"
    echo bin dir = "$bin_path"
    shift # past argument
    ;;
    -nt|--num_threads)
    num_threads="$2"
    echo number of threads = "$num_threads"
    shift # past argument
    ;;
    *)
            # unknown option
    ;;
esac
shift # past argument or value
done

export JAVA_HOME="$java_path"
export PATH="$JAVA_HOME/bin:$PATH"
#ulimit -u 64
cmd_to_eval="java -server -Xmx22G -Xms4G -cp $bin_path:$bin_path/commons-cli-1.2.jar:$bin_path/ij-1.51n.jar:$bin_path/Trainable_Segmentation-3.2.19.jar:$bin_path/weka-dev-3.9.1.jar:$bin_path/imglib2-4.2.1.jar:$bin_path/imglib2-ij-2.0.0-beta-38.jar:$bin_path/imglib2-algorithm-0.8.1.jar:$bin_path/bounce-0.18.jar:$bin_path/ui-behavior-1.3.0.jar:$bin_path/mtj-1.0.4.jar: ClusterClassify -m $model_file -i $input_file -o $output_file -nt $num_threads -3d"
eval $cmd_to_eval
