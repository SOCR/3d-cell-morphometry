#!/bin/bash
#
# wrapper for calculate_metrics.py
# requires Python with NumPy
#
# 1st parameter - path to input morphometry measures
# 2nd parameter - path to Python binary
# 3rd parameter - path to directory with calculate_metrics.py
# 4th parameter - path to output file with results
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

cmd_to_eval="$python_path/python $sp/calculate_metrics.py $input_file -o $output_file"
eval $cmd_to_eval
