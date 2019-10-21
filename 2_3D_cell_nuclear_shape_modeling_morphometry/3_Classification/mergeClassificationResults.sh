#!/bin/bash
#
# compiles results of all classified cells into one list
# this is needed to avoid LONI Pipeline bug that resets module infinite cardinality to 1
#
# 1st parameter - path to results of cell classification
# 2th parameter - path to the list of all classified cells in the first cell output, empty list otherwise
#
# author: Alexandr Kalinin, 2017

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
    *)
            # unknown option
    ;;
esac
shift # past argument or value
done

input_file_txt="${input_file##*-}"
input_idx_zeros="${input_file_txt%.*}"
input_file_temp="${input_file%-*}"
input_dir="${input_file%/*}"
input_idx=${input_idx_zeros##+(0)}
tmp_list="${output_file%.*}.txt"

if [[ $input_idx -eq 1 ]]
then
    results=$(ls -d $input_dir/*.txt | grep $input_file_temp) 
    echo results = "$results"
    echo $results | tr " " "\n" > $tmp_list
    echo $tmp_list > $output_file 
else
    echo "" > $output_file
fi
