#!/bin/bash
# first parameter - input file
# second parameter - output file

# masks out Fibrillarin (c2) volume with a binary mask from DAPI (c0) 
# macro outputs file as original base c2 file name + "_mask.tif"

while [[ $# > 1 ]]
do
key="$1"

case $key in
    -c0|--dapi)
    c0="$2"
    echo input file = "$c0"
    shift # past argument
    ;;
    -c2|--fibr)
    c2="$2"
    echo output file = "$c2"
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

# TODO: pass Fiji and macro paths as parameters
../software/Fiji.app/ImageJ-linux64 --headless --console -macro ../software/scripts/MaskOut.ijm "$c0|$c2|$output_file"
