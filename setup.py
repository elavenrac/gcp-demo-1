import setuptools

setuptools.setup(
              name='ml-package-import',
              version='0.0.1',
              install_requires=[
                  'apache-beam[gcp]==2.11.*',
                  'tensorflow-transform==0.13.*',
                  'tensorflow==1.13.*',
                  'python-snappy==0.5.4x'
                  ],
              packages=setuptools.find_packages(),
              )