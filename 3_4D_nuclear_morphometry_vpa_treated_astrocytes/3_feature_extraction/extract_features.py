# Author: Alexandr Kalinin
# Copyright (c) 2016-2024 Regents of the University of Michigan
# Citation:
# Kalinin, A.A., Hou, X., Ade, A.S., Fon, G.V., Meixner, W., Higgins, G.A., Sexton, J.Z., Wan, X., Dinov, I.D., O’Meara, M.J. and Athey, B.D. 2021. Valproic acid-induced changes of 4D nuclear morphology in astrocyte cells. Molecular Biology of the Cell, 32(18), pp.1624-1633. doi:10.1091/mbc.E20-08-0502

import warnings

import numpy as np
import trimesh
from skimage.measure import label, marching_cubes, regionprops
from trimesh.curvature import (
    discrete_gaussian_curvature_measure,
    discrete_mean_curvature_measure,
)


def voxel_features():
    return [
        "area",
        "bbox_area",
        "convex_area",
        "equivalent_diameter",
        "euler_number",
        "extent",
        "inertia_tensor_eigvals",
        "major_axis_length",
        "minor_axis_length",
        "solidity",
    ]


def extract_voxel_features(mask_3d, feature_list=None):
    feature_list = voxel_features() if feature_list is None else feature_list
    props = regionprops(label(mask_3d))

    features_dict = {feature: [] for feature in feature_list}

    for prop in props:
        for feature in feature_list:
            if hasattr(prop, feature):
                features_dict[feature].append(getattr(prop, feature))
            else:
                raise ValueError(
                    f"Feature '{feature}' is not available in skimage.measure.regionprops"
                )

    return features_dict


def mask2mesh(mask_3d, marching_cubes_kwargs=None):
    marching_cubes_kwargs = marching_cubes_kwargs or {}
    verts, faces, _, _ = marching_cubes(mask_3d, **marching_cubes_kwargs)
    mesh = trimesh.Trimesh(vertices=verts, faces=faces)
    trimesh.repair.fix_normals(mesh)
    return mesh


def ellipsoid_sphericity(v, sa):
    return np.power(np.pi, 1 / 3) * np.power(6 * v, 2 / 3) / sa


def extract_mesh_features(mesh):
    axis_len = sorted(mesh.extents)
    # convex_hull_axis_len = sorted(mesh.convex_hull.extents)
    intertia_pcs = mesh.principal_inertia_components
    volume = mesh.volume
    surface_area = mesh.area

    try:
        bounding_volume = mesh.bounding_cylinder.volume
    except Exception as e:
        bounding_volume = 0
        warnings.warn(
            f"Unable extract bounding cylinder volume. Returning 0. Exception: {e}"
        )

    return {
        "Area": surface_area,
        "Volume": volume,
        "Min Axis Length": axis_len[0],
        "Med Axis Length": axis_len[1],
        "Max Axis Length": axis_len[2],
        "Scale": mesh.scale,
        "Inertia PC1": intertia_pcs[0],
        "Inertia PC2": intertia_pcs[1],
        "Inertia PC3": intertia_pcs[2],
        "Bounding Box Volume": mesh.bounding_box.volume,
        "Oriented Bounding Box Volume": mesh.bounding_box_oriented.volume,
        "Bounding Cylinder Volume": bounding_volume,
        "Bounding Sphere Volume": mesh.bounding_sphere.volume,
        "Convex Hull Volume": mesh.convex_hull.volume,
        "Convex Hull Area": mesh.convex_hull.area,
        "Sphericity": ellipsoid_sphericity(volume, surface_area),
        "Extent": mesh.volume / mesh.bounding_box.volume,
        "Solidity": mesh.volume / mesh.convex_hull.volume,
        "Avg Gaussian Curvature": discrete_gaussian_curvature_measure(
            mesh, mesh.vertices, 1
        ).mean(),
        "Avg Mean Curvature": discrete_mean_curvature_measure(
            mesh, mesh.vertices, 1
        ).mean(),
    }
