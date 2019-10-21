#!/bin/bash
#
# wrapper for fillholes.m MATLAB script in headless more
# requires MATLAB R2014b
#
# 1st parameter - path to input TIFF volume
# 2nd parameter - path to directory with fillholes.m
# 3rd parameter - path to output TIFF volume
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
    -s|--scripts)
    scripts_path="$2"
    echo scripts file = "$scripts_path"
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

matlab -nodesktop -nodisplay -nosplash -r "addpath('$scripts_path'); fillholes('$input_file', '$output_file'); exit;"
