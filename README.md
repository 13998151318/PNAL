# PNAL
Source code for "P-NAL: a Simple, Effective and Interpretable Entity Alignment Method [Experiments, Datasets]"

## Source code references
The source code for some parts of our method has been adapted from the corresponding repositories. 
The repositories are:
- [BERT-INT](https://github.com/kosugi11037/bert-int) for BERT-INT.
- [PARIS](https://github.com/dig-team/PARIS) for PARIS.
- [PARIS+](https://github.com/epfl-dlab/entity-matchers) for entity-matchers(PARIS+).

## Installation process

Create a virtual environment with Anaconda to run PNALalignment and install the packages if needed. (fine for torch1.12 and CUDA11.6)
If you want to run the BERT unit, create an environment (fine for torch1.12 and CUDA11.6) to run experiments with it.

Download the datasets: you can find them following the link [https://drive.google.com/drive/folders/1x-8OonL8SMDpNyfGyBmwzsgQL_zVMojx?usp=sharing](https://drive.google.com/drive/folders/1x-8OonL8SMDpNyfGyBmwzsgQL_zVMojx?usp=sharing). 
  Extract the zip and place the datasets into the folder "datasets".

## Reproduction of results

cd PNALoverall

python run_experiment.py --dataset DBP15k_full_zh_en_2 --dataset_division 721_1folds --table_setting 0

