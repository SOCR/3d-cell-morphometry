#!/bin/bash
#
# wrapper for Farsight.ijm macro in headless more
# requires Fiji (ImageJ2)
#
# 1st parameter - path to input TIFF volume
# 2nd parameter - path to Fiji
# 3rd parameter - path to macros directory with Farsight.ijm
# 4th parameter - path to output TIFF volume
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
    -f|--fiji)
    fiji_path="$2"
    echo Fiji path = "$fiji_path"
    shift # past argument
    ;;
    -m|--macros)
    macros_path="$2"
    echo macros path = "$macros_path"
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

cmd_to_eval="$fiji_path/ImageJ-linux64 --headless --console -macro $macros_path/Farsight.ijm $input_file'|'$output_file"
eval $cmd_to_eval
