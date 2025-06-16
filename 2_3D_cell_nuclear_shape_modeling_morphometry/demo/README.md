# [SOCR](http://socr.umich.edu/) 3D Cell Morphometry Project
## Instructions for running a demo Pipeline workflow

`[June 2, 2024] Update`: Previous version of this page suggested to use Pipeline Client Web Start or Web App. Because both of them do not appear to be supported anymore, we now recommend running workflows using the desktop native [Pipeline Client](https://pipeline.loni.usc.edu/products-services/pipeline-software/). To be able to run workflows on the LONI Pipeline Server, [apply to become a collaborator](https://pipeline.loni.usc.edu/get-started/become-a-collaborator/).

### Nuclear morphometric classification live demo:

This demo is prepared for classification of serum-starved Fibroblast cells
([SS, #160](http://www.socr.umich.edu/projects/3d-cell-morphometry/data.html)).
This workflow take as an input original 16 1024x1024xZ 3D TIFF images (sub-volumes) in DAPI channel (c0) and metadata.
It demonstrates nuclear binary mask preparation, 3D shape modeling, morphometric measure extraction,
and classification running in distributed mode on a cluster using LONI Pipeline guest mode.
It outputs .csv file with image-level output label, nucleus-level accuracy and average probability as well as
labels and probabilities for individual nuclear masks that were segmented out of 3D input sub-volume,
passed the curation, 3D shape modeling, feature extraction, and classification.

Instructions below describe how to use Pipeline if you already have LONI Pipeline credentials.
 
1. Download and install [Pipeline Client](https://pipeline.loni.usc.edu/products-services/pipeline-software/)
2. Click Connections icon at the bottom-right corner of the client to connect to the server with your LONI Pipeline credentials
3. Download workflow file 
[c0-classification-demo-run160.pipe](../demo/c0-classification-demo-run160.pipe) 
and open in the Pipeline client
4. [Click Run](http://pipeline.loni.usc.edu/learn/user-guide/execution/#Executing%20a%20workflow) button at the bottom of the client – after workflow validates the protocol, presence of input data,
and availability of free nodes in cluster, it will start running jobs
5. Running the workflow take 2-3 hours on average, depending on availability of computing nodes in the cluster
6. After workflow is completed, right-click on Calculate Accuracy module in Classification group and download or view
 the output file from [Output Files tab](http://pipeline.loni.usc.edu/learn/user-guide/execution/#Viewing%20output)

You can also double-click on group in the workflow at any moment to see individual modules inside.
You can disconnect while the workflow is running and reconnect later.
Workflow protocol can be ran multiple times to validate reproducibility of the morphometry results.
Pipeline documentation, including instructions module definition, modification, and execution,
is available on the [official website](http://pipeline.loni.usc.edu).
