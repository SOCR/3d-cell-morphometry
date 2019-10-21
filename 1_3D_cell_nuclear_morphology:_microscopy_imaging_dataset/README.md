# [SOCR](http://socr.umich.edu/) 3D Cell Morphometry Project
## 1. 3D cell nuclear morphology: microscopy imaging dataset

### Source code, models, and documentation for 3D image pre-processing, per-channel segmentation, and curation

Inputs: 3D TIFF images (volumes) in each of 3 channels
(available for downloading on the [project data webpage](http://www.socr.umich.edu/projects/3d-cell-morphometry/data.html)).

#### Nuclei 3D pre-processing, segmentation, and curation (DAPI, c0):

1. [Convert each volume](./nuclei_c0/1_Preprocessing) to 8-bit greyscale and apply despeckling using [ImageJ](https://imagej.nih.gov/ij/)
2. [Segment each volume](./nuclei_c0/2_Segmentation) in 3D using
[Farsight toolkit's Nuclear Segmentation](http://farsight-toolkit.org/wiki/Nuclear_Segmentation) algorithm
3. [Fill holes](./nuclei_c0/3_Fill_holes) in derived nuclear masks using [MATLAB](https://www.mathworks.com/products/matlab.html)
4. [Separate nuclear masks](./nuclei_c0/4_Separate_masks) into individual volumes (implemented using [LibTIFF](http://www.libtiff.org/))
5. [Filter out nuclear masks](./nuclei_c0/5_Mask_curation) that do not pass min and/or max thresholds for voxel count (mask volume) and compactness

#### Nucleoli 3D pre-processing, segmentation, and curation (both fibrillarin, c1, and EtBr, c2):

1. [Mask out background](./nucleoli_c1c2/1_Preprocessing) in each volume, convert it to 16-bit greyscale and apply despeckling
using [ImageJ](https://imagej.nih.gov/ij/)
2. [Segment each volume](./nucleoli_c1c2/2_Segmentation) in 3D using
[Trainable Weka Segmentation (Fiji)](http://imagej.net/Trainable_Weka_Segmentation)
3. [Separate binary blobs](./nucleoli_c1c2/3_Watershed) by the watershed algorithm and find connected components in [ImageJ](https://imagej.nih.gov/ij/)
4. [Separate nuclear masks](./nuclei_c0/4_Separate_masks) into individual volumes (implemented using [LibTIFF](http://www.libtiff.org/)) – same as for nuclei
5. [Run co-localization method](./nucleoli_c1c2/5_Colocalization) to confirm c2 masks with c1 masks using [ImageJ](https://imagej.nih.gov/ij/)
6. [Filter out c2 masks](./nuclei_c0/5_Mask_curation) that do not pass min and/or max thresholds for voxel count (mask volume) and compactness– same as for nuclei, with [own config](./nucleoli_c1c2/6_Mask_curation)

Outputs: individual c0 and c2 binary masks in TIFF format
(also available for downloading on the [project data webpage](http://www.socr.umich.edu/projects/3d-cell-morphometry/data.html)).