#!/bin/bash
#
# wrapper for tile2blob, outputs the list of their paths
# requires TiffLib 4
#
# 1st parameter - path to input TIFF volume
# 2nd parameter - path to original TIFF volume
# 3rd parameter - path to TiffLib
# 4th parameter - path to directory with tile2blob binary
# 4th parameter - path to output list file of separated masks


while [[ $# > 1 ]]
do
key="$1"

case $key in
    -i|--input)
    input_file="$2"
    echo input file = "$input_file"
    shift # past argument
    ;;
     -n|--name)
    orig_name="$2"
    echo orig_name = "$orig_name"
    shift # past argument
    ;;
     -t|--tifflib)
    tifflib_path="$2"
    echo tifflib path = "$tifflib_path"
    shift # past argument
    ;;
     -b|--bin)
    bin_path="$2"
    echo binaries path = "$bin_path"
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

output_dir="${input_file%/*}"
echo output dir = $output_dir
dest_name="${orig_name##*/}"
mask_template="${dest_name%.*}_mask_"
output_template="${output_dir}/${mask_template}"
echo out temp = $output_template

# link tifflib 4
export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:${tifflib_path}/lib"

cmd_to_eval="$bin_path/tile2blob $input_file $output_template"
eval $cmd_to_eval

masks=$(ls -d $output_dir/*.tif | grep $mask_template)
echo $masks | tr " " "\n" > $output_file
