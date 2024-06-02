# [SOCR](http://socr.umich.edu/) 3D Cell Morphometry Project

This repository contains workflows, source code, and documentation associated with the [3D Cell Morphometry Project](http://www.socr.umich.edu/projects/3d-cell-morphometry).

3D microscopic imaging data and derived binary masks are available for downloading from the [project data webpage](http://www.socr.umich.edu/projects/3d-cell-morphometry/data.html).

## Structure of the repository:

Materials are provided in 3 parts corresponding to [publications](#Publications):

1. [3D cell nuclear morphology: microscopy imaging dataset](./1_3D_cell_nuclear_morphology:_microscopy_imaging_dataset)
2. [3D cell nuclear shape modeling, morphometry, and classification](./2_3D_cell_nuclear_shape_modeling_morphometry)
3. [4D nuclear morphometry of VPA-treated astrocytes](./3_4D_nuclear_morphometry_vpa_treated_astrocytes)
 
We also provide an example of how to run a workflow on LONI Pipeline Server:

[Nuclear morphometric classification demo workflow](./2_3D_cell_nuclear_shape_modeling_morphometry/demo)

## Publications

If you find this work useful please cite a corresponding publication(s):

1. Kalinin, A.A., Allyn-Feuer, A., Ade, A., Fon, G.V., Meixner, W., Dilworth, D., De Wet, J.R., Higgins, G.A., Zheng, G., Creekmore, A., Wiley, J.W., _et al_. 2018. 3D cell nuclear morphology: microscopy imaging dataset and voxel-based morphometry classification results. In _Proceedings of the IEEE Conference on Computer Vision and Pattern Recognition Workshops_ (pp. 2272-2280). [doi:10.1109/CVPRW.2018.00304](https://doi.org/10.1109/CVPRW.2018.00304)

 ```
@inproceedings{kalinin20183ddata,
  title={3D cell nuclear morphology: microscopy imaging dataset and voxel-based morphometry classification results},
  author={Kalinin, Alexandr A and Allyn-Feuer, Ari and Ade, Alex and Fon, Gordon-Victor and Meixner, Walter and Dilworth, David and De Wet, Jeffrey R and Higgins, Gerald A and Zheng, Gen and Creekmore, Amy and Wiley, John W and Verdone, James E and Veltri, Robert W and Pienta, Kenneth J and Coffey, Donald S and Athey, Brian D and Dinov, Ivo D},
  booktitle={Proceedings of the IEEE Conference on Computer Vision and Pattern Recognition Workshops},
  pages={2272--2280},
  year={2018}
}
```

2. Kalinin, A.A., Allyn-Feuer, A., Ade, A., Fon, G.V., Meixner, W., Dilworth, D., De Wet, J.R., Higgins, G.A., Zheng, G., Creekmore, A., Wiley, J.W., _et al_. 2018. 3D shape modeling for cell nuclear morphological analysis and classification. In _Scientific Reports_, 8(1), p. 13658. [doi:10.1038/s41598-018-31924-2](https://doi.org/10.1038/s41598-018-31924-2)

```
 @article{kalinin20183dshape,
  title={3D shape modeling for cell nuclear morphological analysis and classification},
  author={Kalinin, Alexandr A and Allyn-Feuer, Ari and Ade, Alex and Fon, Gordon-Victor and Meixner, Walter and Dilworth, David and Husain, Syed S and de Wett, Jeffrey R and Higgins, Gerald A and Zheng, Gen and and Creekmore, Amy and Wiley, John W and Verdone, James E and Veltri, Robert W and Pienta, Kenneth J and Coffey, Donald S and Athey, Brian D and Dinov, Ivo D},
  journal={Scientific reports},
  volume={8},
  year={2018},
}
```

3. Kalinin, A.A., Hou, X., Ade, A.S., Fon, G.V., Meixner, W., Higgins, G.A., Sexton, J.Z., Wan, X., Dinov, I.D., O’Meara, M.J. and Athey, B.D. 2021. Valproic acid-induced changes of 4D nuclear morphology in astrocyte cells. _Molecular Biology of the Cell_, 32(18), pp.1624-1633. [doi:10.1091/mbc.E20-08-0502](https://doi.org/10.1091/mbc.E20-08-0502)
 
 ```
@article{kalinin20214dvpa,
  title={Valproic Acid-Induced Changes of 4D Nuclear Morphology in Astrocyte Cells},
  author={Kalinin, Alexandr A and Hou, Xinhai and Ade, Alex S and Fon, Gordon-Victor and Meixner, Walter and Higgins, Gerald A and Sexton, Jonathan Z and Wan, Xiang and Dinov, Ivo D and O’Meara, Matthew J and Athey, Brian D},
  journal={Molecular Biology of the Cell},
  volume={32},
  number={18},
  pages={1624--1633},
  year={2021},
}
```

## License

Copyright (c) 2016-2024 Regents of the University of Michigan

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

See the GNU Lesser General Public License for more details http://opensource.org/licenses/LGPL-3.0.
