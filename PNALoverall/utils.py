import subprocess
import time
import os
import random


def turn_yg(triples, triple_type='rel'):
    predix_dict = {'dbp': 'http://dbpedia.org/ontology/',
                   'owl': 'http://www.w3.org/2002/07/owl#',
                   'rdf': 'http://www.w3.org/1999/02/22-rdf-syntax-ns#',
                   'rdfs': 'http://www.w3.org/2000/01/rdf-schema#',
                   'skos': 'http://www.w3.org/2004/02/skos/core#',
                   'xsd': 'http://www.w3.org/2001/XMLSchema#'}
    base_prefix = 'http://yago-knowledge.org/resource/'
    triples_new = set()
    for (s, p, o) in triples:
        s = s.lstrip('<').rstrip('>')
        p = p.lstrip('<').rstrip('>')
        o = o.lstrip('<').rstrip('>')
        s = base_prefix + s
        if ':' in p and 'EntityMatchers' not in p:
            p = p.split(':')
            p = predix_dict[p[0]] + p[1]
        elif 'EntityMatchers' not in p:
            p = base_prefix + p
        if triple_type == 'rel':
            o = base_prefix + o
        triples_new.add((s, p, o))
    return triples_new


def turn_and_write(rel_triples, attr_triples, seeds_triples, out_path):
    file = open(out_path, 'w', encoding='utf-8')
    for (s, p, o) in rel_triples:
        file.write('<' + s + '> <' + p + '> <' + o + '> .\n')
    for (s, p, o) in attr_triples:
        # FIX literal without quotes
        if not o.startswith('"'):
            mod_o = '"' + o + '"'
        else:
            mod_o = o
        file.write('<' + s + '> <' + p + '> ' + mod_o + ' .\n')
    for (s, p, l) in seeds_triples:
        file.write('<' + s + '> <' + p + '> ' + l + ' .\n')
    file.close()


def write_1v1(_1v1, out_path):
    file = open(out_path, 'w', encoding='utf-8')
    for s in _1v1:
        file.write(s + '\n')

    file.close()


def seed_triples(folder, dataset_division, fold_num):
    """
    Returns two lists of triples, with the seed for PARIS well formatted into
    ({resource}, {#label}, {label})
    Parameters
    ----------
    folder
    dataset_division
    fold_num

    Returns
    -------
    seed_triples_1
    seed_triples_2
    """
    root_fold = folder + dataset_division + "/" + fold_num + "/"
    seed_triples_1 = []
    seed_triples_2 = []
    with open(root_fold + "train_links",encoding='utf-8') as f:
        for l in f:
            e1, e2 = l.strip("\n").split("\t")
            label = e1.split("/")[-1]
            # Use label definition for N-Triples from W3C RDF recommendation. See https://www.w3.org/TR/n-triples/
            label_str = '{resource} EntityMatchers:label "{label}"'

            # Add the label
            seed_triples_1.append((e1, "EntityMatchers:label", '"{}"'.format(label)))
            seed_triples_2.append((e2, "EntityMatchers:label", '"{}"'.format(label)))
    return seed_triples_1, seed_triples_2


def create_nt(folder, dataset_division, fold_num, kg1: str, kg2: str, kg1_1v1_assumption, kg2_1v1_assumption, zero_seed, no_attr):

    #xch1
    if kg1_1v1_assumption and kg2_1v1_assumption:
        print(folder + 'ent_links')
        _1v1_left, _1v1_right = read_ent_links(folder + 'ent_links')
        random.shuffle(_1v1_left)
        random.shuffle(_1v1_right)
        #print("92")
        write_1v1(_1v1_left, kg1_1v1_assumption)
        write_1v1(_1v1_right, kg2_1v1_assumption)

    rel_triples_1 = read_triples(folder + 'rel_triples_1')
    attr_triples_1 = []
    if not no_attr:
        try:
            attr_triples_1 = read_triples(folder + 'attr_triples_1')
        except Exception:
            print("attr_triples_1 Exception")
            attr_triples_1 = []

    

    if 'YG' in folder:
        rel_triples_2 = turn_yg(read_triples(folder + 'rel_triples_2'))
        try:
            attr_triples_2 = turn_yg(read_triples(folder + 'attr_triples_2'), triple_type='attr')
        except Exception:
            print("attr_triples_2 Exception")
            attr_triples_2 = []
    else:
        rel_triples_2 = read_triples(folder + 'rel_triples_2')
        attr_triples_2 = []
        if not no_attr:
            try:
                attr_triples_2 = read_triples(folder + 'attr_triples_2')
            except Exception:
                print("attr_triples_2 Exception")
                attr_triples_2 = []
        
    if(not zero_seed):
        seed_triples_1, seed_triples_2 = seed_triples(folder, dataset_division, fold_num)
        if 'YG' in folder:
            seed_triples_2 = turn_yg(seed_triples_2, 'attr')
    else:
        seed_triples_1 = []
        seed_triples_2 = []
    turn_and_write(rel_triples_1, attr_triples_1, seed_triples_1, kg1)
    turn_and_write(rel_triples_2, attr_triples_2, seed_triples_2, kg2)


def read_triples(file_path):
    """
    read relation / attribute triples from file
    :param file_path: relation / attribute triples file path
    :return: relation / attribute triples
    """
    triples = set()
    with open(file_path, 'r', encoding='utf-8') as file:
        for line in file:
            line = line.strip('\n').split('\t')
            triples.add((line[0], line[1], line[2])) 
    file.close()
    tri = list(triples)
    tri.sort()
    return tri


def read_ent_links(file_path):

    _1v1_left = []
    _1v1_right = []
    with open(file_path, 'r', encoding='utf-8') as file:
        for line in file:
            line = line.strip('\n').split('\t')
            #print(line)
            _1v1_left.append(line[0])
            _1v1_right.append(line[1])
    file.close()
    #random.shuffle(_1v1_left)
    #random.shuffle(_1v1_right)
    return (_1v1_left, _1v1_right)


def run_paris(dataset_in, root_folder, name, ontology1, ontology2, kg1_1v1_assumption, kg2_1v1_assumption, _1v1, zero_seed, no_attr, table_setting, dataset_type):
    current_time = time.localtime()
    ontology1 = os.path.abspath(ontology1)
    ontology2 = os.path.abspath(ontology2)
    if(kg1_1v1_assumption == None or kg2_1v1_assumption == None):
        _1v1 = False
    if(_1v1):
        kg1_1v1_assumption = os.path.abspath(kg1_1v1_assumption)
        kg2_1v1_assumption = os.path.abspath(kg2_1v1_assumption)

    task_name = '%s_%02d%02d_%02d%02d%02d' % (name, current_time.tm_mon, current_time.tm_mday,
                                              current_time.tm_hour, current_time.tm_min, current_time.tm_sec)

    task_name = root_folder + "/" + task_name
    os.mkdir(task_name)
    os.mkdir('%s/output' % task_name)
    os.mkdir('%s/log' % task_name)

    with open(task_name + '/paris.ini', 'w') as ini_file:
        
        lang = name[-7:-5]
        excel_num = ""
        setting3_bootsrtap = 2  #False  True
        if table_setting == 0 or table_setting == 4:
            excel_num = ""
        else:
            if lang == "zh":
                if table_setting == 3:
                    if setting3_bootsrtap == 0:
                        excel_num = "301"
                    elif setting3_bootsrtap == 1:
                        excel_num = "342" #342
                    elif setting3_bootsrtap == 2:
                        excel_num = "351"#"351"
                else:
                    excel_num = "194"#194
            elif lang == "ja":
                if table_setting == 3:
                    if setting3_bootsrtap == 0:
                        excel_num = "334"
                    elif setting3_bootsrtap == 1:
                        excel_num = "343" #343
                    elif setting3_bootsrtap == 2:
                        excel_num = "352"#"352"
                else:
                    excel_num = "204"
            elif lang == "fr":
                if table_setting == 3:
                    if setting3_bootsrtap == 0:
                        excel_num = "335"
                    elif setting3_bootsrtap == 1:
                        excel_num = "410" #344
                    elif setting3_bootsrtap == 2:
                        excel_num = "411"#"353"
                else:
                    excel_num = "216"
            excel_num = excel_num + "_"
        
        #if excel_num != "":
        #    dataset_in_with_excel_num = dataset_in + str(excel_num) + "_"
        #else:
        #    dataset_in_with_excel_num = dataset_in

        if table_setting == 4:
            BASIC_BERT_UNIT_MODEL_EPOCH_NUM = 4 #14
        else:
            if lang == "fr":
                BASIC_BERT_UNIT_MODEL_EPOCH_NUM = 14
            else:
                BASIC_BERT_UNIT_MODEL_EPOCH_NUM = 14 #14

        entity_emb = dataset_in + str(excel_num) + "DBP15K_"+lang+"en_emb_"+str(BASIC_BERT_UNIT_MODEL_EPOCH_NUM)+".csv"
        trans_entity_emb = dataset_in + "trans_" +  str(excel_num) + "DBP15K_"+lang+"en_emb_"+str(BASIC_BERT_UNIT_MODEL_EPOCH_NUM)+".csv"
        emb_entity_names = dataset_in + str(excel_num) + "DBP15K_"+lang+"en_emb_"+str(BASIC_BERT_UNIT_MODEL_EPOCH_NUM)+"_entity_names"
        
        
        #attribute_value_emb = dataset_in + "attributeValue_embedding.csv"
        attribute_value_names = dataset_in + str(excel_num) + "attribute_value_list"
        #ini_file.write('attribute_value_emb = %s\n' % attribute_value_emb)
        trans_entity_emb_sim_confidence = 0
        all_revision = "false"
        all_prob_revision = "false" 
        if table_setting == 11:
            increse_relation_frequency = 0
            increse_attribute_frequency = 0
        else:
            increse_relation_frequency = 0.5# #0.85
            increse_attribute_frequency = 0.5
        

        if table_setting == -1:
            use_entity_emb_sim = "false"    #"true" "false"
            use_translate_emb = "false"
            use_attribute_value_emb_sim = "false"   #"true" "false"
        elif table_setting == 0:
            use_entity_emb_sim = "false"    #"true" "false"
            use_translate_emb = "false"
            use_attribute_value_emb_sim = "false"   #"true" "false"
        elif table_setting == 1 or table_setting == 2:
            use_entity_emb_sim = "false"    #"true" "false"
            use_translate_emb = "false"
            use_attribute_value_emb_sim = "true"   #"true" "false"
        elif table_setting == 3:
            if setting3_bootsrtap == 0:
                use_entity_emb_sim = "false"    #"true" "false"
            else:
                use_entity_emb_sim = "true"
            use_translate_emb = "true"
            use_attribute_value_emb_sim = "false"   #"true" "false"
        elif table_setting == 4 or table_setting == 5:
            use_entity_emb_sim = "true"    #"true" "false"
            use_translate_emb = "false"
            use_attribute_value_emb_sim = "true"   #"true" "false"
        elif table_setting == 6:
            use_entity_emb_sim = "false"    #"true" "false"
            use_translate_emb = "false"
            use_attribute_value_emb_sim = "true"   #"true" "false"
        elif table_setting == 7:
            use_entity_emb_sim = "true"    #"true" "false"
            use_translate_emb = "false"
            use_attribute_value_emb_sim = "true"   #"true" "false"
            all_revision = "true"
            add_evidence_remove_duplicate_run = 30
        elif table_setting == 8:
            use_entity_emb_sim = "true"    #"true" "false"
            use_translate_emb = "false"
            use_attribute_value_emb_sim = "true"   #"true" "false"
            all_prob_revision = "true"
        elif table_setting == 9:
            use_entity_emb_sim = "true"    #"true" "false"
            use_translate_emb = "false"
            use_attribute_value_emb_sim = "true"   #"true" "false"
        elif table_setting == 10:
            use_entity_emb_sim = "true"    #"true" "false"
            use_translate_emb = "false"
            use_attribute_value_emb_sim = "true"   #"true" "false"
        elif table_setting == 11:
            use_entity_emb_sim = "true"    #"true" "false"
            use_translate_emb = "false"
            use_attribute_value_emb_sim = "false"   #"true" "false"
        if dataset_type == "DW":
            use_entity_emb_sim = "false"    #"true" "false"
            use_translate_emb = "false"
            use_attribute_value_emb_sim = "false"   #"true" "false"

        
        if table_setting == 3:
            if lang == "fr":
                if setting3_bootsrtap == 0:
                    entity_emb_sim_confidence = 0.45 #0.6
                    trans_entity_emb_sim_confidence = 0.45
                elif setting3_bootsrtap == 1:
                    entity_emb_sim_confidence = 0.55 #0.6
                    trans_entity_emb_sim_confidence = 0.55
                elif setting3_bootsrtap == 2:
                    entity_emb_sim_confidence = 0.65 #0.6
                    trans_entity_emb_sim_confidence = 0.65
            else:
                if setting3_bootsrtap == 0:  # 0.6 c = 1.5 w; 0.75w = 0.42857 c      ; 0.8 c = 4 w; 2w = 0.666 cs
                    entity_emb_sim_confidence = 0.2 #0.6
                    trans_entity_emb_sim_confidence = 0.2
                elif setting3_bootsrtap == 1:
                    entity_emb_sim_confidence = 0.3 #0.6
                    trans_entity_emb_sim_confidence = 0.3
                elif setting3_bootsrtap == 2:
                    entity_emb_sim_confidence = 0.4 #0.6
                    trans_entity_emb_sim_confidence = 0.4
        else:
            if lang == "fr":
                entity_emb_sim_confidence = 0.8
            else:
                entity_emb_sim_confidence = 0.6
        
        
        attribute_value_emb_sim_confidence = 0.8
        literal_approximate_equal_count = 1
        value_similarity_lower_bound = 0.7
        ini_file.write(f'lang = {lang}\n')
        ini_file.write(f'table_setting = {table_setting}\n')
        ini_file.write(f'zero_seed = {zero_seed}\n')
        ini_file.write(f'no_attr = {no_attr}\n')
        ini_file.write(f'excel_num = {excel_num}\n')
        ini_file.write(f'increse_relation_frequency = {increse_relation_frequency}\n')
        ini_file.write(f'increse_attribute_frequency = {increse_attribute_frequency}\n')
        ini_file.write('use_entity_emb_sim = %s\n' % use_entity_emb_sim)
        ini_file.write('use_translate_emb = %s\n' % use_translate_emb)
        ini_file.write('setting3_bootsrtap = %s\n' % setting3_bootsrtap)
        ini_file.write('entity_emb_sim_confidence = %s\n' % entity_emb_sim_confidence)
        ini_file.write('trans_entity_emb_sim_confidence = %s\n' % trans_entity_emb_sim_confidence)
        ini_file.write('use_attribute_value_emb_sim = %s\n' % use_attribute_value_emb_sim)
        ini_file.write('attribute_value_emb_sim_confidence = %s\n' % attribute_value_emb_sim_confidence)
        ini_file.write('literal_approximate_equal_count = %s\n' % literal_approximate_equal_count)
        ini_file.write(f'value_similarity_lower_bound = {value_similarity_lower_bound}\n')
        ini_file.write(f'all_revision = {all_revision}\n')
        ini_file.write(f'all_prob_revision = {all_prob_revision}\n')
        ini_file.write('dataset_in_path = %s\n' % dataset_in)
        ini_file.write('entity_emb = %s\n' % entity_emb)
        ini_file.write('trans_entity_emb = %s\n' % trans_entity_emb)
        ini_file.write('emb_entity_names = %s\n' % emb_entity_names)
        ini_file.write('attribute_value_names = %s\n' % attribute_value_names)
        attribute_value_emb_sim_1 = dataset_in + str(excel_num) + "attribute_value_sim_left_to_right.csv"
        ini_file.write(f'attribute_value_emb_sim_1 = {attribute_value_emb_sim_1}\n')
        attribute_value_emb_sim_2 = dataset_in + str(excel_num) + "attribute_value_sim_right_to_left.csv"
        ini_file.write(f'attribute_value_emb_sim_2 = {attribute_value_emb_sim_2}\n')
        ini_file.write('resultTSV = %s/output\n' % task_name)
        ini_file.write('factstore1 = %s\n' % ontology1)
        ini_file.write('factstore2 = %s\n' % ontology2)
        if(_1v1):
            ini_file.write('factstore1_1v1_assumption = %s\n' % kg1_1v1_assumption)
            ini_file.write('factstore2_1v1_assumption = %s\n' % kg2_1v1_assumption)
        # matching_strategy:(1:xch's takeMaxMaxBothWays)(2:LAPJV for 1v1 + takeMaxMaxBothWays for non 1v1)
        matching_strategy = 1
        if table_setting == 10:
            modify_matches = "false"
        else:
            modify_matches = "true"
        max_sparse_alignment = 40
        ini_file.write(f'matching_strategy = {matching_strategy}\n')
        ini_file.write(f'modify_matches = {modify_matches}\n')
        ini_file.write(f'max_sparse_alignment = {max_sparse_alignment}\n')
        ini_file.write('home = %s/log\n' % task_name)
        if table_setting == 3:
            max_alignment_sentences = 400
        else:
            max_alignment_sentences = 400#80
        #从第几轮开始记录和显示证据
        endIteration = 22
        add_evidence_remove_duplicate_run = 15 #15
        display_evidence_run = 0
        nThreads = 8  #CPU 38
        use_c_as_probability_value = "false" #false  true
        ini_file.write('max_alignment_sentences = %s\n' % max_alignment_sentences)
        ini_file.write('endIteration = %s\n' % endIteration)
        ini_file.write(f'display_evidence_run = {display_evidence_run}\n')
        ini_file.write(f'nThreads = {nThreads}\n')
        ini_file.write(f'use_c_as_probability_value = {use_c_as_probability_value}\n')
        ini_file.write(f'add_evidence_remove_duplicate_run = {add_evidence_remove_duplicate_run}\n')

                                                       #PARIS_xch2.1.jar  paris.jar -Xmx26000m
    _ = subprocess.call(['java', '-Xmx45000m', '-Xss64m', '-jar', 'PNAL.jar', task_name + '/paris.ini'])
    return task_name, endIteration


def compute_prec_rec_f1(aligns, truth_links):
    """
    Note that aligns should have been pruned from the alignments already present in the
    seed.
    Truth link, hence, must contain only test and valid links.
    Parameters
    ----------
    aligns
    truth_links

    Returns
    -------

    """
    aligns = set(aligns)
    truth_links = set(truth_links)
    num_correct = len(aligns.intersection(truth_links))
    if num_correct == 0 or len(aligns) == 0:
        print("Got 0, 0, 0 in evaluation!!!")
        return 0, 0, 0
    precision = num_correct / len(aligns)
    recall = num_correct / len(truth_links)
    f1 = 2 * precision * recall / (precision + recall)
    return precision, recall, f1


def evaluate_paris(paris_out_folder, dataset_folder, dataset_division, fold_num, endIteration, zero_seed):
    run = 0
    res_list = []
    """
    while True:
        full_path = paris_out_folder + "/output/{run}_eqv.tsv".format(run=run)
        #print(full_path)
        if os.path.exists(full_path):
            # PARIS create an empty file at the last_iter+1. If we encountered it, we can break
            if os.stat(full_path).st_size == 0:
                break
        run += 1
    """
    fold_folder = dataset_folder + dataset_division + "/" + fold_num + "/"
    set_train = set()
    with open(fold_folder + "train_links", encoding='utf-8') as f:
        for l in f:
            (e1, e2) = l.rstrip("\n").split("\t")
            set_train.add((e1, e2))
    with open(paris_out_folder + f"/full_result_{fold_num}.log", "w",  encoding='utf-8') as f:
        f.write(f"使用数据集 fold_folder:{fold_folder}\n")
    test_links = []
    valid_links = []
    # Create the test alignments, which are the ones contained in test and valid.
    with open(fold_folder + "test_links", encoding='utf-8') as f:
        for l in f:
            (e1, e2) = l.rstrip("\n").split("\t")
            test_links.append((e1, e2))
    with open(fold_folder + "valid_links", encoding='utf-8') as f:
        for l in f:
            (e1, e2) = l.rstrip("\n").split("\t")
            valid_links.append((e1, e2))
    if(zero_seed):
        test_links = test_links + list(set_train)
        set_train = set()
    precisions = []
    recalls = []
    f1s = []
    len_res_no_trains = []
    for run in range(endIteration):
        full_path = paris_out_folder + f"/output/{run}_eqv.tsv"
        # Get PARIS result from the .tsv and elaborate it a bit to be compared with the same_list
        res_list = []
        with open(full_path) as f:
            for l in f:
                if(len(l.split("<->")) >= 2):
                    e1= l.split("<->")[0].strip()
                    e2= l.split("<->")[1].strip().split(" ")[0].strip()
                    #print(e1)
                    #print(e2)
                else:
                    (e1, e2, _) = l.split("\t")
                res_list.append((e1, e2))
        
        res_no_train = []
        for align in res_list:
            if align not in set_train:
                # Evaluate only the alignments that were not present in the training data (seed).
                res_no_train.append(align)
        
        
        #print("res_no_train")
        #print(res_no_train)
        #print(test_links + valid_links)
        precision, recall, f1 = compute_prec_rec_f1(res_no_train, test_links + valid_links)
        with open(paris_out_folder + f"/full_result_{fold_num}.log", "a",  encoding='utf-8') as f:
            f.write(f"run:{run} precision:{precision:.5f}   recall:{recall:.5f} f1:{f1:.5f} len(res_no_train):{len(res_no_train)}\n")
        precisions.append(precision)
        recalls.append(recall)
        f1s.append(f1)
        len_res_no_trains.append(len(res_no_train))
    
    #xch 使用最后一次结果
    return precisions, recalls, f1s, len_res_no_trains


def parse_stats_from_log(log_file):
    """
    Read statistics of precision recall and f1-score from log files of RDGCN and BootEA
    Parameters
    ----------
    log_file name of the log file

    Returns
    -------
    precision_no_csls
    precision_csls
    recall_no_csls
    recall_csls
    f1_no_csls
    f1_csls
    """
    with open(log_file) as log:
        log_str = log.read()

    f1_no_csls = float(log_str.split("Final test result:\n\t")[-1].split("F1: ")[1].split("\n")[0])
    precision_no_csls = float(log_str.split("Final test result:\n\t")[-1].split("Precision: ")[1].split("\n")[0])
    recall_no_csls = float(log_str.split("Final test result:\n\t")[-1].split("Recall: ")[1].split("\n")[0])

    if "Final test result with csls:" in log_str:
        f1_csls = float(log_str.split("Final test result with csls:\n\t")[-1].split("F1: ")[1].split("\n")[0])
        precision_csls = float(
            log_str.split("Final test result with csls:\n\t")[-1].split("Precision: ")[1].split("\n")[0])
        recall_csls = float(log_str.split("Final test result with csls:\n\t")[-1].split("Recall: ")[1].split("\n")[0])
    else:
        f1_csls, precision_csls, recall_csls = None, None, None

    train_time_seconds = float(log_str.split("Training ends. Total time = ")[-1].split(" s.")[0])
    test_time_seconds = float(log_str.split("Total run time = ")[-1].split(" s.")[0]) - train_time_seconds

    return precision_no_csls, precision_csls, recall_no_csls, recall_csls, f1_no_csls, \
           f1_csls, train_time_seconds, test_time_seconds
