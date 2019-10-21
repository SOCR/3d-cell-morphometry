#!/bin/bash
#
# wrapper for CurateCn.class in headless more
# requires ImajeJ as ij.jar
#
# 1st parameter - path to input TIFF volume
# 2nd parameter - path to directory with parameters for curation and filtering
# 3rd parameter - path to Java home directory
# 4th parameter - path to directory with CurateCn.class
# 5th parameter - path to output list file with the input file name inside if it passed the curation or empty otherwise
#

while [[ $# > 1 ]]
do
key="$1"

case $key in
    -i|--input)
    input_file="$2"
    echo input file = "$input_file"
    shift # past argument
    ;;
    -p|--params)
    params_file="$2"
    echo params file = "$params_file"
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
    -o|--output)
    output_file="$2"
    echo output file = "$output_file"
    shift # past argument
    ;;
    *)
            # unknown option
    ;;
esac
shift # past argument or value
done

file_temp="${input_file%.*}"
list_file="${file_temp}.lst"
export JAVA_HOME="$java_path"
export PATH="$JAVA_HOME/bin:$PATH"
cmd_to_eval="java -Xmx1024m -cp $bin_path:$bin_path/ij.jar CurateCn $input_file $params_file"
eval $cmd_to_eval

if [ ! -s $list_file ]
then
    echo "" > $output_file
else
     echo "$input_file" > "$output_file"
fi
