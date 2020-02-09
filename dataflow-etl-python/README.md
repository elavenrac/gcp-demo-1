# Demo 1 data pipeline

ETL is done by a Google Cloud Dataflow job in `./dataflow-etl-python`. The job will read data from the `bigquery-public-data:chicago_taxi_trips.taxi_trips` public dataset and prepare data for both training and predictions. The job will output data as TFRecords into a GCS bucket. TFRecords will enable more efficient ML training


## ETL prerequisites

You will need to create a Bigquery dataset in your project. If you use the default job options, create a datased named `chicagotaxi`. It can be named anything and customized in job options. If you do not specifiy a service account for the Dataflow job to use (`--serviceAccount`), it will use the project's default Compute Engine service account. Either the default GCE or the provided service account for the workers must be have read and write access to the dataset.

## Using the shuffle service

If you want to use the Dataflow shuffle service (`--experiments=shuffle_mode=service`), you will need to run your job in a GCP region that supports the service: https://cloud.google.com/dataflow/docs/guides/deploying-a-pipeline#cloud-dataflow-shuffle


## Python virtual environment

> NOTE: It is recommended to run on `python2`.
> Support for using the Apache Beam SDK for Python 3 is in a prerelease state [alpha](https://cloud.google.com/products/?hl=EN#product-launch-stages) and might change or have limited support.

Install a [Python virtual environment](https://packaging.python.org/guides/installing-using-pip-and-virtual-environments).

Run the following to set up and activate a new virtual environment:
```bash
python2.7 -m virtualenv env
source env/bin/activate
```

Once you are done with the environment, you can deactivate the virtual environment by running `deactivate`.


## Instance types and worker counts

The job processes more than 250 GB of internal data. Using 6 n1-highmem-4 instances takes the job about an hour to finish.

## Google Cloud Setup 
If you have never created application default credentials (ADC), you can create it by `gcloud` [command](https://cloud.google.com/sdk/gcloud/reference/projects/list). 

```bash
gcloud auth application-default login
```
### Google Cloud Storage GCS setup
To run on Google Cloud Platform, all the files must reside in [Google Cloud Storage](https://cloud.google.com/storage/docs/creating-buckets), so you will have to specify a Google Cloud Storage path as the working directory.

> NOTE: this will incur charges on your Google Cloud Platform project. See [Storage pricing](https://cloud.google.com/storage/pricing).

## Run Preprocessing
This is an [Apache Beam](https://beam.apache.org/) pipeline that will do all the preprocessing necessary to train a Machine Learning model.
It uses [tft_beam.AnalyzeAndTransformDataset](https://github.com/tensorflow/transform/blob/master/docs/api_docs/python/tft_beam/AnalyzeAndTransformDataset.md) to do any processing over the dataset.

After preprocessing our dataset, we also want to split it into a training and evaluation dataset.
The training dataset will be used to train the model.
The evaluation dataset contains elements that the training has never seen, and since we also know the "answers", we'll use these to validate that the training accuracy roughly matches the accuracy on unseen elements.

### Setting Environment Variables
Environment variables are referenced in the pipeline and needs to be set before running the pipeline. Use the [set_env](set_env) file to set all the required environment variables

```bash
source set_env
```
The environment variables necessary are listed below
```
PROJECT: GCP Project to run the ETL process 
REGION: GCP Region to run the pipeline
GCS_OUTPUT_BUCKET: GCS output bucket
GCS_STAGING_BUCKET: GCS staging bucket
GCS_TEMP_BUCKET=: GCS temp bucket
JOB_NAME: Dataflow job name
NUM_WORKERS: Number of workers for the pipeline
MACHINE_TYPE: Machine type to runt he pipeline on
SERVICE_ACCOUNT: Service account used
```

With the python virtual environment active, run the processing step with 
```bash
python preprocess.py
  ```
