from __future__ import absolute_import
import argparse, logging
from typing import Any, Tuple, Dict, Iterator
import tensorflow as tf 
import apache_beam as beam
import subprocess
from apache_beam.options.pipeline_options import PipelineOptions
import tensorflow_transform as tft
import tensorflow_transform.beam as tft_beam
from tensorflow_transform.tf_metadata import dataset_metadata
from tensorflow_transform.tf_metadata import dataset_schema
from tensorflow_transform.coders import ExampleProtoCoder

class SplitPartitions(beam.DoFn):
   def process(self, element: dict) -> Iterator[Dict[(str, Any)]]:
      if element.get('ml_partition') == 'train':
        yield beam.pvalue.TaggedOutput('train', element)
      elif element.get('ml_partition') == 'test':
        yield beam.pvalue.TaggedOutput('test', element)
      elif element.get('ml_partition') == 'validation':
        yield beam.pvalue.TaggedOutput('validation', element)

def get_metadata() -> dataset_metadata.DatasetMetadata:
    return dataset_metadata.DatasetMetadata(dataset_schema.from_feature_spec({ #  
     'cash':tf.io.FixedLenFeature([], tf.int64), 
     'year_norm':tf.io.FixedLenFeature([], tf.float32), 
     'start_time_norm_midnight':tf.io.FixedLenFeature([], tf.float32), 
     'start_time_norm_noon':tf.io.FixedLenFeature([], tf.float32), 
     'pickup_lat_std':tf.io.FixedLenFeature([], tf.float32), 
     'pickup_long_std':tf.io.FixedLenFeature([], tf.float32), 
     'pickup_lat_centered':tf.io.FixedLenFeature([], tf.float32), 
     'pickup_long_centered':tf.io.FixedLenFeature([], tf.float32), 
     'day_of_week_MONDAY':tf.io.FixedLenFeature([], tf.float32), 
     'day_of_week_TUESDAY':tf.io.FixedLenFeature([], tf.float32), 
     'day_of_week_WEDNESDAY':tf.io.FixedLenFeature([], tf.float32), 
     'day_of_week_THURSDAY':tf.io.FixedLenFeature([], tf.float32), 
     'day_of_week_FRIDAY':tf.io.FixedLenFeature([], tf.float32), 
     'day_of_week_SATURDAY':tf.io.FixedLenFeature([], tf.float32), 
     'day_of_week_SUNDAY':tf.io.FixedLenFeature([], tf.float32), 
     'month_JANUARY':tf.io.FixedLenFeature([], tf.float32), 
     'month_FEBRUARY':tf.io.FixedLenFeature([], tf.float32), 
     'month_MARCH':tf.io.FixedLenFeature([], tf.float32), 
     'month_APRIL':tf.io.FixedLenFeature([], tf.float32), 
     'month_MAY':tf.io.FixedLenFeature([], tf.float32), 
     'month_JUNE':tf.io.FixedLenFeature([], tf.float32), 
     'month_JULY':tf.io.FixedLenFeature([], tf.float32), 
     'month_AUGUST':tf.io.FixedLenFeature([], tf.float32), 
     'month_SEPTEMBER':tf.io.FixedLenFeature([], tf.float32), 
     'month_OCTOBER':tf.io.FixedLenFeature([], tf.float32), 
     'month_NOVEMBER':tf.io.FixedLenFeature([], tf.float32), 
     'month_DECEMBER':tf.io.FixedLenFeature([], tf.float32)}))

def preprocessing_fn(input):
    return {'cash':input.get('cash'), 
     'year_norm':input.get('year_norm'), 
     'start_time_norm_midnight':input.get('start_time_norm_midnight'), 
     'start_time_norm_noon':input.get('start_time_norm_noon'), 
     'pickup_lat_std':input.get('pickup_lat_std'), 
     'pickup_long_std':input.get('pickup_long_std'), 
     'pickup_lat_centered':input.get('pickup_lat_centered'), 
     'pickup_long_centered':input.get('pickup_long_centered'), 
     'day_of_week_MONDAY':input.get('day_of_week_MONDAY'), 
     'day_of_week_TUESDAY':input.get('day_of_week_TUESDAY'), 
     'day_of_week_WEDNESDAY':input.get('day_of_week_WEDNESDAY'), 
     'day_of_week_THURSDAY':input.get('day_of_week_THURSDAY'), 
     'day_of_week_FRIDAY':input.get('day_of_week_FRIDAY'), 
     'day_of_week_SATURDAY':input.get('day_of_week_SATURDAY'), 
     'day_of_week_SUNDAY':input.get('day_of_week_SUNDAY'), 
     'month_JANUARY':input.get('month_JANUARY'), 
     'month_FEBRUARY':input.get('month_FEBRUARY'), 
     'month_MARCH':input.get('month_MARCH'), 
     'month_APRIL':input.get('month_APRIL'), 
     'month_MAY':input.get('month_MAY'), 
     'month_JUNE':input.get('month_JUNE'), 
     'month_JULY':input.get('month_JULY'), 
     'month_AUGUST':input.get('month_AUGUST'), 
     'month_SEPTEMBER':input.get('month_SEPTEMBER'), 
     'month_OCTOBER':input.get('month_OCTOBER'), 
     'month_NOVEMBER':input.get('month_NOVEMBER'), 
     'month_DECEMBER':input.get('month_DECEMBER')}

def write_tfrecords(transformed_dataset, location, step):
    transformed_data, transformed_metadata = transformed_dataset
    transformed_data | '{} - Write Transformed Data'.format(step) >> beam.io.tfrecordio.WriteToTFRecord(file_path_prefix=('{}/{}/{}'.format(location, step, step)),
      file_name_suffix='.tfrecords',
      coder=(ExampleProtoCoder(get_metadata().schema)))

def run(argv=None, save_main_session=True):
    parser = argparse.ArgumentParser()
    parser.add_argument('--dataset', dest='dataset',
      default='chicagotaxi',
      help='')
    parser.add_argument('--table', dest='table',
      default='finaltaxi_encoded_super_mini',
      help='')
    parser.add_argument('--output_bucket', dest='output_bucket',
      default='gcp-cert-demo-1',
      help='')
    parser.add_argument('--project', dest='project',
      default='ml-sandbox-1-191918',
      help='')
    parser.add_argument('--output_path', dest='output_path',
      default='data/tfrecord',
      help='')
    known_args, pipeline_args = parser.parse_known_args(argv)
    pipeline_args.extend([
     '--runner=DataflowRunner',
     '--project=ml-sandbox-1-191918',
     '--staging_location=gs://ntc-mls-dataflow-staging/python',
     '--temp_location=gs://ntc-mls-dataflow-tmp/python',
     '--job_name=gcp-demo1-tf-etl-10',
     '--setup_file=/Users/acarnevale/Projects/ntc-ml/setup.py',
     '--experiments=shuffle_mode=service',
     '--max_num_workers=4',
     '--worker_machine_type=n1-standard-4',
     '--service_account_email=261855689705-compute@developer.gserviceaccount.com',
     '--region=us-central1'])
    pipeline_options = PipelineOptions(pipeline_args).view_as(beam.options.pipeline_options.GoogleCloudOptions)
    with beam.Pipeline(options=pipeline_options) as p:
        with tft_beam.Context('/tmp'):
            training_data = p | 'ReadBigQuery training' >> beam.io.Read(beam.io.BigQuerySource(query=("SELECT * FROM `{}.{}.{}` WHERE ml_partition='train'".format(known_args.project, known_args.dataset, known_args.table)),
              use_standard_sql=True))
            test_data = p | 'ReadBigQuery test' >> beam.io.Read(beam.io.BigQuerySource(query=("SELECT * FROM `{}.{}.{}` WHERE ml_partition='test'".format(known_args.project, known_args.dataset, known_args.table)),
              use_standard_sql=True))
            validation_data = p | 'ReadBigQuery eval' >> beam.io.Read(beam.io.BigQuerySource(query=("SELECT * FROM `{}.{}.{}` WHERE ml_partition='validation'".format(known_args.project, known_args.dataset, known_args.table)),
              use_standard_sql=True))

            transformed_train_dataset, transform_fn = ( # pylint: disable=unused-variable
              training_data, get_metadata()) | '{} - Transform'.format('train') >> tft_beam.AnalyzeAndTransformDataset(preprocessing_fn)
            write_tfrecords(transformed_train_dataset, 'gs://{}/{}'.format(known_args.output_bucket, known_args.output_path), 'train')
            
            transformed_test_dataset, transform_fn = ( # pylint: disable=unused-variable
              test_data, get_metadata()) | '{} - Transform'.format('test') >> tft_beam.AnalyzeAndTransformDataset(preprocessing_fn)
            write_tfrecords(transformed_test_dataset, 'gs://{}/{}'.format(known_args.output_bucket, known_args.output_path), 'test')
            
            transformed_validation_dataset, transform_fn = ( # pylint: disable=unused-variable
              validation_data, get_metadata())| '{} - Transform'.format('validation') >> tft_beam.AnalyzeAndTransformDataset(preprocessing_fn)
            write_tfrecords(transformed_validation_dataset, 'gs://{}/{}'.format(known_args.output_bucket, known_args.output_path), 'validation')
            
if __name__ == '__main__':
    logging.getLogger().setLevel(logging.INFO)
    run()
