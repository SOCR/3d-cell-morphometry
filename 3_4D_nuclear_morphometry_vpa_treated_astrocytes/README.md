# [SOCR](http://socr.umich.edu/) 3D Cell Morphometry Project
## 3. 4D nuclear morphometry of VPA-treated astrocytes

Kalinin, A.A., Hou, X., Ade, A.S., Fon, G.V., Meixner, W., Higgins, G.A., Sexton, J.Z., Wan, X., Dinov, I.D., O’Meara, M.J. and Athey, B.D. 2021. Valproic acid-induced changes of 4D nuclear morphology in astrocyte cells. _Molecular Biology of the Cell_, 32(18), pp.1624-1633. [doi:10.1091/mbc.E20-08-0502](https://doi.org/10.1091/mbc.E20-08-0502)

### Description of image acquisition, processing and analysis steps

#### Image acquisition

Normal and VPA-treated astrocyte cells were labeled with DAPI (4′,6-diamidino-2-phenylindole), a common stain for the nuclear DNA. 3D imaging employed a Zeiss LSM 710 laser scanning confocal microscope with a 63× PLAN/apochromat 1.4 NA DIC objective. Each original 3D volume was then resliced into a 1024×1024×Z lattice (Z = {30,50}), where regional subvolumes facilitate the alignment with the native tile size of the microscope. For every subvolume, accompanying vendor metadata was extracted from the original data.

#### Image deconvolution

Theoretical 3D point spread functions for each individual image volume were modeled using the Richards and Wolf algorithm from the PSFGenerator plugin for Fiji [1]. We then used estimated point spread functions and imaging metadata to apply Lucy-Richardson deconvolution (10 iterations) to the original 3D image volumes using the DeconvolutionLab2 software [2].

#### Image segmentation

Segmentation of astrocyte nuclei from 3D deconvolved images used the Nuclear Segmentation algorithm from Farsight toolkit [3] as described in our previous study [3D cell nuclear morphology: microscopy imaging dataset](../1_3D_cell_nuclear_morphology%3A_microscopy_imaging_dataset#nuclei-3d-pre-processing-segmentation-and-curation-dapi-c0), with exactly the same image pre-/post-processing steps and all segmentation parameters.

#### Morphometric feature extraction

Feature extraction included measuring descriptors of size and shape directly from voxel masks and from reconstructed surface meshes.

1. Voxel-based features from nuclear segmentation masks were [extracted](./3_feature_extraction/1_extract_vox_features.ipynb) with `regionprops` from scikit-image library [4] and included: volume, bounding box volume, convex hull volume, lengths of major and minor axes, diameter of a sphere with the same volume, extent, solidity, and the eigenvalues of the inertia tensor.
2. To extract surface features, we reconstructed the surface of each binary mask and represented as triangulated mesh following the protocol from our previous study [3D shape modeling and morphometry](../2_3D_cell_nuclear_shape_modeling_morphometry#2-3d-shape-modeling-and-morphometry). This protocol uses Mask2Mesh tool from the MOCA framework [5] to robustly reconstruct surface meshes, followed by mesh translation, simplification, and subdivision using LONI ShapeTools [6].
3. Surface features [extracted](./3_feature_extraction/2_extract_trimesh_features.ipynb) using trimesh [7] library included: mesh volume, surface area, average mean curvature, convex hull and bounding primitive (box, oriented box, cylinder, sphere) volumes, convex hull surface area, inertia tensor eigenvalues, and principal axes lengths. We also computed surface-based extent and solidity from trimesh-derived measures as the ratio of the object volume to the bounding box volume and the ratio of the object volume to the convex hull volume, correspondingly. Sphericity of the nucleus was computed as the ratio of the surface area of a sphere with the same volume as the given nucleus to the surface area of the nucleus.
4. Additionally, we used LONI ShapeTools [6] to extract curvedness, shape index, and fractal dimension, same as in [3D shape modeling and morphometry](../2_3D_cell_nuclear_shape_modeling_morphometry#2-3d-shape-modeling-and-morphometry) study.


#### Required 3rd-party software:

[1] [PSFGenerator](https://bigwww.epfl.ch/algorithms/psfgenerator/)

[2] [DeconvolutionLab2](https://bigwww.epfl.ch/deconvolution/deconvolutionlab2/)

[3] [Farsight toolkit](http://farsight-toolkit.ee.uh.edu/wiki/Nuclear_Segmentation)

[4] [scikit-image](https://scikit-image.org)

[5] [MOCA](http://www.nitrc.org/projects/moca_2015/)

[6] [LONI ShapeTools](https://www.loni.usc.edu/research/software?name=ShapeTools)

[7] [trimesh](https://trimesh.org)
