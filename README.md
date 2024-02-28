# PNAL
Source code for "P-NAL: a Simple, Effective and Interpretable Entity Alignment Method [Experiments, Datasets]"

## Source code references
The source code for some parts of our method has been adapted from the corresponding repositories. 
The repositories are:
- [BERT-INT](https://github.com/kosugi11037/bert-int) for BERT-INT.
- [PARIS](https://github.com/dig-team/PARIS) for PARIS.
- [PARIS+](https://github.com/epfl-dlab/entity-matchers) for entity-matchers(PARIS+).

## Installation process

Create a virtual environment with Anaconda to run PNALalignment and install the imported python packages if needed. (fine for python3.8 and numpy)

If you want to run the BERT unit, create an environment (fine for python3.8, torch1.12 and CUDA11.6) to run experiments with it.

Download the datasets, pretrained BERT model and some experiment results (with evidence log file): you can find them following the link [https://drive.google.com/file/d/1fikMMw_DSs7dreNnp27NSQXnK1ZVZHxu/view?usp=sharing](https://drive.google.com/file/d/1fikMMw_DSs7dreNnp27NSQXnK1ZVZHxu/view?usp=sharing). 
  Extract the zip and place the datasets into the folder "datasets".

The DBP15k and D_W_15K_V2 datasets are consistent with other studies, with entities and relations' urls abbreviated for simplicity.
## Reproduction of results

cd PNALoverall

python run_experiment.py --dataset DBP15k_full_zh_en_2 --dataset_division 721_1folds --table_setting 0

(change the argument "table_setting" for different configuration groups (1-5) and ablation studies (6-11))

