# [SOCR](http://socr.umich.edu/) 3D Cell Morphometry Project
## Workflows and instructions how to run them live

### Nuclear morphometric classification live demo:

This demo is prepared for classification of serum-starved Fibroblast cells
([SS, #160](http://www.socr.umich.edu/projects/3d-cell-morphometry/data.html)).
This workflow take as an input original 16 1024x1024xZ 3D TIFF images (sub-volumes) in DAPI channel (c0) and metadata.
It demonstrates nuclear binary mask preparation, 3D shape modeling, morphometric measure extraction,
and classification running in distributed mode on a cluster using LONI Pipeline guest mode.
It outputs .csv file with image-level output label, nucleus-level accuracy and average probability as well as
labels and probabilities for individual nuclear masks that were segmented out of 3D input sub-volume,
passed the curation, 3D shape modeling, feature extraction, and classification.

Instructions below describe how to use Pipeline in a guest mode. If you already have LONI Pipeline credentials
you can just download [Pipeline Client](http://pipeline.loni.usc.edu/products-services/pipeline-software/) 
and log in using your username and password.
 
1. Download and install [LONI Pipeline Client Web Start](http://pipeline.loni.usc.edu/files/webstart/pipeline.jnlp)
(requires Java<sup>[1](#myfootnote1)</sup>)
2. Create "Try-It-Now" connection by clicking Connections icon at the bottom-right corner of the client to connect
to the server without credentials (enter space for password)
3. Download workflow file 
[c0-classification-demo-run160.pipe](../demo/c0-classification-demo-run160.pipe) 
and open in the Pipeline client
4. [Click Run](http://pipeline.loni.usc.edu/learn/user-guide/execution/#Executing%20a%20workflow) button at the bottom of the client – after workflow validates the protocol, presence of input data,
and availability of free nodes in cluster, it will start running jobs
5. Running the workflow take 2-3 hours on average, depending on availability of computing nodes in the cluster
6. After workflow is completed, right-click on Calculate Accuracy module in Classification group and download or view
 the output file from [Output Files tab](http://pipeline.loni.usc.edu/learn/user-guide/execution/#Viewing%20output)

You can also double-click on group in the workflow at any moment to see individual modules inside.
You can disconnect while the workflow is running – under Connections you will be able to see your unique GUEST-ID
that you can use to reconnect later and check workflow status (enter space for password).
Having your GUEST-ID you should be able to use [LONI Pipeline Web App](http://pipeline.loni.usc.edu/webapp/)
to reconnect to the same sessions (web app is still in Beta and might not work as expected).
Workflow protocol can be ran multiple times to validate reproducibility of the morphometry results.
Pipeline documentation, including instructions module definition, modification, and execution,
is available on the [official website](http://pipeline.loni.usc.edu).

<a name="myfootnote1">1</a>: If you have problems accessing Java applications using Chrome, [Oracle recommends](https://www.java.com/en/download/faq/chrome.xml) using Internet Explorer (Windows) or Safari (Mac OS X) instead.
