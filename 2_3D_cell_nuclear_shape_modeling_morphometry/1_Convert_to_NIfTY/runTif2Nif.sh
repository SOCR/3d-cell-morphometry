#!/bin/bash
#
# wrapper for tif2nif
# requires TiffLib
#
# 1st parameter - path to input TIFF volume
# 2nd parameter - path to metadata file
# 3rd parameter - path to TiffLib
# 4th parameter - path to directory with tif2nif binary
# 5th parameter - path to output NIfTI volume
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
    -m|--meta)
    meta_file="$2"
    echo meta file = "$meta_file"
    shift # past argument
    ;;
    -t|--tifflib)
    tifflib="$2"
    echo tifflib path = "$tifflib"
    shift # past argument
    ;;
    -b|--bin)
    bin_path="$2"
    echo binary path = "$bin_path"
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

x=$(grep "ScalingX" $meta_file)
x="${x##*|}"
x="${x##*,}"
y=$(grep "ScalingY" $meta_file)
y="${y##*|}"
y="${y##*,}"
z=$(grep "ScalingZ" $meta_file)
z="${z##*|}"
z="${z##*,}"

# link tifflib 4
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$tifflib
params="-i$input_file -o$output_file -x$x -y$y -z$z"
cmd_to_eval="$bin_path/tif2nif $params"
eval $cmd_to_eval
