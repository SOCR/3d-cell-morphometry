#!/bin/bash
#
# wrapper for classify_nuclear_masks.py
# requires Python with NumPy and pickled scikit-learn classifier
#
# 1st parameter - path to input morphometry measures
# 2nd parameter - path to dPython binary
# 3rd parameter - path to directory with classify_nuclear_masks.py
# 4th parameter - path to directory with pickled scikit-learn classifier
# 5th parameter - path to output file with output label and probability
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
    -p|--python)
    python_path="$2"
    echo python path = "$python_path"
    shift # past argument
    ;;
    -sp|--sps)
    sp="$2"
    echo scripts file = "$sp"
    shift # past argument
    ;;
    -c|--clf)
    clf_path="$2"
    echo classifier file = "$clf_path"
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
input_idx="${input_file_txt%.*}"
input_file_temp="${input_file%-*}"
input_dir="${input_file%/*}"

cmd_to_eval="$python_path/python $sp/classify_nuclear_masks.py $input_file -clf $clf_path -o $output_file"
eval $cmd_to_eval

