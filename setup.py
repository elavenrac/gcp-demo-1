import setuptools

setuptools.setup(
              name='ml-package-import',
              version='0.0.1',
              install_requires=[
                  'apache-beam[gcp]==2.17.0',
                  'tensorflow==2.1.0',
                  'tensorflow-transform==0.21.0',
                  'workflow'
                  ],
              packages=setuptools.find_packages(),
              )