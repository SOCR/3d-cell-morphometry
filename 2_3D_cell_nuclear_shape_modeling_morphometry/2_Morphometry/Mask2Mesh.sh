#! /bin/bash

# wrapper for Yonggang Shi's tools for creating a mesh from a mask
# this is used to integrate the Mask2Mesh converter into Pipeline

outputdir=`echo $@ | cut -d' ' -f$#`
inputmask=$1
params=`echo $@ | cut -d' ' -f2-$[$#-1]`

mkdir $outputdir

for x in 21 22 23 24 25 26 27 28 29 30 31 32 33 34 41 42 43 44 45 46 47 48 49 50 61 62 63 64 65 66 67 68 81 82 83 84 85 86 87 88 89 90 91 92 101 102 121 122 161 162 163 164 165 166 181 182
do
${LONIAPPS}/yshi/Mask2Mesh_V3 $inputmask $x $params ${outputdir}/${x}.obj
done

exit $?
