package pnal;

import java.io.File;
import java.io.IOException;

import javatools.administrative.Announce;
import javatools.administrative.Parameters;
import javatools.filehandlers.FileSet;
import pnal.evaluation.GoldImdbYago;
import pnal.evaluation.GoldStandard;


/** This class is part of the PARIS ontology matching project at INRIA Saclay/France.
 * 
 * It is licensed under a Creative Commons Attribution Non-Commercial License
 * by the author Fabian M. Suchanek (http://suchanek.name). For all further information,
 * see http://webdam.inria.fr/paris
 *
 * This class holds a setting of two ontologies.*/
public class Setting {
  /** Folder where the computed equalities shall be stored*/
  public final File home;
  /** First ontology*/
  public final File ontology1;
  /** Third ontology. Just kidding. It's the second, of course...*/
  public final File ontology2;
  public File factstore1_1v1_assumption = null;
  public File factstore2_1v1_assumption = null;
  // there used to be several possible choices for types, now only one remains
  /** Type of the first ontology */
  public final String ontologyType1 = "memory";
  /** Type of the third ontology */
  public final String ontologyType2 = "memory";
  /** Folder where the TSV files shall be output*/
  public final File tsvFolder;
  /** Gold standard for evaluation*/
  public final GoldStandard gold;
  /** Name of the setting*/
  public final String name;
  /** End iteration*/
  public int endIteration;
  public int last_run;
  /** number of threads */
  public int nThreads;
  /** join length limit */
  public int joinLengthLimit;
  /** should we align both ways? */
  public boolean bothWays;
  /** should we takeMax? */
  public boolean takeMax;
  /** should we takeMaxMax? */
  public boolean takeMaxMax;
  /** do last pass with n best */
  public int lastPassThreshold;
  /** do we use interestingness thresholds on neighborhoods */
  public boolean interestingnessThreshold;
  /** do we use the new equality propagation formula */
  public boolean useNewEqualityProduct;
  /** should we use dense relation alignments */
  public boolean matrixSubRelationStores;
  /** Normalize strings to lowercase letters and numbers when loading RDF/N3 triples into the FactStore. 
   * Switch this on BEFORE YOU GENERATE THE ONTOLOGIES, if the ontologies that you want to match contain names and
   * strings in slight variations (Berlin=berlin). Default is FALSE.*/
  public boolean normalizeStrings = false;
  /** Normalize dates to the years. . 
   * Switch this on BEFORE YOU GENERATE THE ONTOLOGIES, if the ontologies that you want to match contain dates on one side
   * and years on the other side. Default is FALSE.*/
  public boolean normalizeDatesToYears = false;
  /** size of k-grams to index */
  public int shinglingSize;
  /** number of hash functions */
  public int shinglingFunctions;
  /** hash table size */
  public int shinglingTableSize = 10485760;
  //public static int shinglingTableSize = 65536;
  /** precompute shinglings */
  public boolean precomputeShinglings = false;
  /** divide approximate literal matches by this value (hacky) */
  public double penalizeApproxMatches;
  /** no approximate literal matches if an exact match exists */
  public boolean noApproxIfExact = true;
  /** parallelize the loading of each fact store */
  public boolean parallelFileLoad;
  /** number of threads for the shingling precomputation */
  public int shinglingThreads;
  /** if nonempty, print debug information for entities matching this string */
  public String debugEntity;
  /** report progress every time that many entities have been dealt with */
  public int reportInterval;
  /** number of entities on which we should search for join relation alignments at each iteration */
  public int sampleEntities;
  /** shuffle entities at each run */
  public boolean shuffleEntities;
  public double smoothNumerator;
  public double smoothDenominator;
  public double smoothNumeratorSampling;
  public double smoothDenominatorSampling;
  public boolean cleverMatching;
  public int sumJoinLengthLimit;
  public double postLiteralDistanceThreshold;
  public boolean shinglingSquare;
  public boolean allowLoops;
  public boolean printNeighborhoodsSampling;
  
  /** Use a special optimized findEqualsOf when no joins are made */
  public boolean optimizeNoJoins;
  
  public boolean debugSampling;

  public double joinThreshold;

  //xch2.0
  public int max_alignment_sentences;
  public int max_evidence;
  public int display_evidence_count;
  public int display_evidence_run_small;
  public int display_evidence_run_big;
  public float TRUTH_EPSILON;

  public float relation_triple_initial_frequency_fs1;
  public double relation_triple_initial_confidence_fs1;
  public float attribute_triple_initial_frequency_fs1;
  public double attribute_triple_initial_confidence_fs1;
  public float relation_triple_initial_frequency_fs2;
  public double relation_triple_initial_confidence_fs2;
  public float attribute_triple_initial_frequency_fs2;
  public double attribute_triple_initial_confidence_fs2;

  public double literal_equal_initial_confidence;
  // truth %0.0 , unaligned_entity_equal_initial_confidence%
  public double unaligned_entity_equal_initial_confidence;
  public double entity_emb_sim_confidence;
  public double attribute_value_emb_sim_confidence;
  public int literal_approximate_equal_count;
  //as original PARIS
  public boolean use_c_as_probability_value = false;
  public boolean add_evidence_remove_duplicate = false; 
  public int add_evidence_remove_duplicate_run;
  public double increse_relation_frequency;
  public double increse_attribute_frequency;
  public boolean use_entity_emb_sim;
  public boolean use_attribute_value_emb_sim;
  public File entity_emb;
  public File emb_entity_names;
  //public File attribute_value_emb;
  public File attribute_value_names;
  public File attribute_value_emb_sim_1;
  public File attribute_value_emb_sim_2;

  public int upper_attribute_value_num; 
  public double value_similarity_lower_bound;
  public int max_value_sim_count;

  public int matching_strategy;
  public int max_sparse_alignment;
  public boolean modify_matches;

  public boolean use_translate_emb;
  public File trans_entity_emb;
  public double trans_entity_emb_sim_confidence;

  public boolean all_revision;
  public boolean all_prob_revision;
  public boolean precompute_emb_sim;

  /** Types of string distance used in Computed.compareStrings()*/
  /* LEVENSHTEIN and SHINGLINGLEVENHSHTEIN are not guaranteed to work */
  public static enum LiteralDistance {
    IDENTITY, BAGOFCHARS, NORMALIZE, BAGOFWORDS, LEVENSHTEIN, SHINGLING, SHINGLINGLEVENSHTEIN
  };

  /** String distance used in Computed.compareStrings() for negative evidence.
   * Has an effect only if punish=TRUE. 
   * There is not much use tinkering with this value, leave it at the default value of IDENTITY.
   * If you need a string distance, use normalizeStrings=TRUE.*/
  /* if you use SHINGLING or SHINGLINGLEVENSHTEIN, make sure that the fact stores were generated with the literal indexes */
  public LiteralDistance literalDistance;
  
  /** Constructs a setting*/
  public Setting(String name, String homeFolder, String o1, String o2, String berkeley, String tsv,GoldStandard g) {
    this.name=name;
    home=new File(homeFolder);
    ontology1=new File(home,o1);
    ontology2=new File(home,o2);
    tsvFolder=new File(home,tsv);
    gold=g;
    
    // TODO: this is redundant with the default value of the settings below
    lastPassThreshold = 0;
    shinglingSize = 4;
    shinglingFunctions = 30;
    shinglingTableSize = 10485760;
    shinglingThreads = 4;
    endIteration=10;
    last_run = endIteration - 1;
    //xch1
    nThreads=Runtime.getRuntime().availableProcessors()*3/5;
    joinLengthLimit = 1;
    bothWays = true;
    interestingnessThreshold = false;
    takeMax = true;
    takeMaxMax = true;
    matrixSubRelationStores = false;
    useNewEqualityProduct = false;

    normalizeStrings = false;
    normalizeDatesToYears = false;
    precomputeShinglings = false;
    noApproxIfExact = true;
    parallelFileLoad = true;
    penalizeApproxMatches = 1.1;
    
    smoothNumerator = 0.;
    smoothDenominator = 10.;
    smoothNumeratorSampling = 0.;
    smoothDenominatorSampling = 1.;
    debugEntity = null;
    reportInterval = 5000;
    sampleEntities = 0;
    shuffleEntities = true;
    cleverMatching = false;
    sumJoinLengthLimit = 2*joinLengthLimit;
    postLiteralDistanceThreshold = 0.78;
    shinglingSquare = true;
    allowLoops = false;
    printNeighborhoodsSampling = false;
    optimizeNoJoins = true;
    joinThreshold = Config.IOTA;
    debugSampling = false;
    literalDistance = Setting.LiteralDistance.IDENTITY;

    //xch2.0
    max_alignment_sentences = 5;
    max_evidence = 5;
    TRUTH_EPSILON = 0.01f;
    relation_triple_initial_frequency_fs1 = 1.f;
    relation_triple_initial_confidence_fs1 = 0.90;
    attribute_triple_initial_frequency_fs1 = 1.f;
    attribute_triple_initial_confidence_fs1 = 0.90;
    relation_triple_initial_frequency_fs2 = 1.f;
    relation_triple_initial_confidence_fs2 = 0.90;
    attribute_triple_initial_frequency_fs2 = 1.f;
    attribute_triple_initial_confidence_fs2 = 0.90;

    literal_equal_initial_confidence = 1;
    
  }
  /** Constructs a setting from an ini file
   * @throws IOException */
  public Setting(File ini) throws IOException {
    Parameters.init(ini);
    name=FileSet.newExtension(ini.getName(),"");
    tsvFolder=Parameters.getOrRequestAndAddFile("resultTSV", "Enter the folder where the result shall be stored in TSV format:");
    gold=null;
    ontology1=Parameters.getOrRequestAndAddFile("factstore1", "Enter the folder where the first fact store lives:");
    ontology2=Parameters.getOrRequestAndAddFile("factstore2", "Enter the folder where the second fact store lives:");
    factstore1_1v1_assumption=Parameters.getFile("factstore1_1v1_assumption", null);
    factstore2_1v1_assumption=Parameters.getFile("factstore2_1v1_assumption", null);
    home=Parameters.getOrRequestAndAddFile("home", "Enter the folder where log information can be stored");

    use_attribute_value_emb_sim = Parameters.getBoolean("use_attribute_value_emb_sim", false);
    use_entity_emb_sim = Parameters.getBoolean("use_entity_emb_sim", false);
    entity_emb = Parameters.getFile("entity_emb", null);
    emb_entity_names = Parameters.getFile("emb_entity_names", null);
    //attribute_value_emb = Parameters.getFile("attribute_value_emb", null);
    attribute_value_names = Parameters.getFile("attribute_value_names", null);
    attribute_value_emb_sim_1 = Parameters.getFile("attribute_value_emb_sim_1", null);
    attribute_value_emb_sim_2 = Parameters.getFile("attribute_value_emb_sim_2", null);
    upper_attribute_value_num = Parameters.getInt("upper_attribute_value_num", 350000);

    lastPassThreshold=Parameters.getInt("lastPassThreshold", 0);
    shinglingSize=Parameters.getInt("shinglingSize", 4);
    shinglingFunctions=Parameters.getInt("shinglingFunctions", 30);
    shinglingTableSize=Parameters.getInt("shinglingTableSize", 10485760);
    shinglingThreads=Parameters.getInt("shinglingThreads", 4);
    endIteration=Parameters.getInt("endIteration", 13);
    last_run = endIteration - 1;
    //xch1
    nThreads=Parameters.getInt("nThreads", Runtime.getRuntime().availableProcessors()*3/5);
    joinLengthLimit=Parameters.getInt("joinLengthLimit", 1);
    bothWays=Parameters.getBoolean("bothWays", true);
    interestingnessThreshold=Parameters.getBoolean("interestingnessThreshold", false);
    takeMax=Parameters.getBoolean("takeMax", true);
    takeMaxMax=Parameters.getBoolean("takeMaxMax", true);
    matrixSubRelationStores=Parameters.getBoolean("matrixSubRelationStores", false);
    useNewEqualityProduct=Parameters.getBoolean("useNewEqualityProduct", false);
    normalizeStrings=Parameters.getBoolean("normalizeStrings", false);
    normalizeDatesToYears=Parameters.getBoolean("normalizeDatesToYears", false);
    precomputeShinglings=Parameters.getBoolean("precomputeShinglings", false);
    noApproxIfExact=Parameters.getBoolean("noApproxIfExact", true);
    parallelFileLoad=Parameters.getBoolean("parallelFileLoad", true);
    parallelFileLoad = false;
    penalizeApproxMatches=Parameters.getDouble("penalizeApproxMatches", 1.1);
    smoothNumerator=Parameters.getDouble("smoothNumerator", 0.);
    smoothDenominator=Parameters.getDouble("smoothDenominator", 10.);
    smoothNumeratorSampling=Parameters.getDouble("smoothNumeratorSampling", 0.);
    smoothDenominatorSampling=Parameters.getDouble("smoothDenominatorSampling", 1.);
    debugEntity=Parameters.get("debugEntity",null);
    reportInterval=Parameters.getInt("reportInterval", 5000);
    sampleEntities=Parameters.getInt("sampleEntities", 0);
    //shuffleEntities=Parameters.getBoolean("shuffleEntities", true);
    cleverMatching=Parameters.getBoolean("cleverMatching", false);
    sumJoinLengthLimit=Parameters.getInt("sumJoinLengthLimit", 2*joinLengthLimit);
    postLiteralDistanceThreshold=Parameters.getDouble("postLiteralDistanceThreshold", 0.78);
    shinglingSquare=Parameters.getBoolean("shinglingSquare", true);
    allowLoops=Parameters.getBoolean("allowLoops", false);
    printNeighborhoodsSampling=Parameters.getBoolean("printNeighborhoodsSampling", false);
    optimizeNoJoins=Parameters.getBoolean("optimizeNoJoins", true);
    joinThreshold=Parameters.getDouble("joinThreshold", Config.IOTA);
    debugSampling=Parameters.getBoolean("debugSampling", false);

    use_c_as_probability_value=Parameters.getBoolean("use_c_as_probability_value", false);
    add_evidence_remove_duplicate_run=Parameters.getInt("add_evidence_remove_duplicate_run", 20);
    
    //xch2.0
    entity_emb_sim_confidence = Parameters.getDouble("entity_emb_sim_confidence", 0.6);
    attribute_value_emb_sim_confidence = Parameters.getDouble("attribute_value_emb_sim_confidence", 0.6);
    literal_approximate_equal_count = Parameters.getInt("literal_approximate_equal_count", 2);
    value_similarity_lower_bound = Parameters.getDouble("value_similarity_lower_bound", 0.3); 
    max_value_sim_count = Parameters.getInt("max_value_sim_count", 50);
    shuffleEntities = false;
    increse_relation_frequency = Parameters.getDouble("increse_relation_frequency", 0.5); 
    increse_attribute_frequency = Parameters.getDouble("increse_attribute_frequency", 0.5); 
    max_alignment_sentences=Parameters.getInt("max_alignment_sentences", 5);

    matching_strategy=Parameters.getInt("matching_strategy", 1);
    max_sparse_alignment=Parameters.getInt("max_sparse_alignment", 40);
    modify_matches =Parameters.getBoolean("modify_matches", true);

    use_translate_emb =Parameters.getBoolean("use_translate_emb", false);
    trans_entity_emb = Parameters.getFile("trans_entity_emb", null);
    trans_entity_emb_sim_confidence = Parameters.getDouble("trans_entity_emb_sim_confidence", 0.5); 
    all_revision =Parameters.getBoolean("all_revision", false);
    all_prob_revision =Parameters.getBoolean("all_prob_revision", false);
    precompute_emb_sim =Parameters.getBoolean("precompute_emb_sim", false);
    max_evidence = 15;
    display_evidence_count = 10;
    TRUTH_EPSILON = 0.01f;
    relation_triple_initial_frequency_fs1 = 1f;
    relation_triple_initial_confidence_fs1 = 1;
    attribute_triple_initial_frequency_fs1 = 1f;
    attribute_triple_initial_confidence_fs1 = 1;
    relation_triple_initial_frequency_fs2 = 1f;
    relation_triple_initial_confidence_fs2 = 1;
    attribute_triple_initial_frequency_fs2 = 1f;
    attribute_triple_initial_confidence_fs2 = 1;

    literal_equal_initial_confidence = 1;
    unaligned_entity_equal_initial_confidence = 0.85;

    display_evidence_run_small = 3;
    display_evidence_run_big = last_run;//last_run - 1

    add_evidence_remove_duplicate = true;
    
    String dist=Parameters.get("literalDistance","identity");
    switch (dist.toLowerCase()) {
    case "identity":
    	literalDistance = Setting.LiteralDistance.IDENTITY;
    	break;
    case "shingling":
    	literalDistance = Setting.LiteralDistance.SHINGLING;
    	break;
    default:
    	Announce.message("bad choice of distance!");
    	System.exit(2);
    }
  }
  // Different settings
  public static final Setting restaurants=new Setting("Restaurants","c:/fabian/data/restaurant","restaurant1","restaurant2","eqv","eqvtsv",new GoldStandard(112));
  public static final Setting restaurantsnormalized=new Setting("RestaurantsNormalized","c:/fabian/data/restaurant_normalized","restaurant1","restaurant2","eqv","eqvtsv",new GoldStandard(112));
  public static final Setting persons =new Setting("Persons","c:/fabian/data/personA","person1","person2","eqv","eqvtsv",new GoldStandard(500));
  public static final Setting personsnormalized=new Setting("PersonsNormalized","c:/fabian/data/person_normalized","person1","person2","eqv","eqvtsv",new GoldStandard(500));
  public static final Setting yagodbpedia=new Setting("YagoDbpedia","/media/ssd/fabian/data","yago/berkeley","dbpedia/berkeley","eqv","eqvtsv",new GoldStandard(1429686,GoldStandard.yagoDbpediaRelations));
  public static final Setting yagodbpediaMoreFacts=new Setting("YagoDbpediaMoreFacts","/media/ssd/fabian/data","yago/berkeley","dbpedia/berkeley","eqv","eqvtsv",new GoldStandard(1049629,GoldStandard.yagoDbpediaRelations));
  public static final Setting dbpediaSelf=new Setting("DbpediaSelf","/media/ssd/fabian/data","dbpedia/berkeley","dbpedia/berkeley","eqv","eqvtsv",new GoldStandard(2365777,GoldStandard.yagoDbpediaRelations));
  public static final Setting yagodbpediaNew=new Setting("YagoDbpediaNew","/home/a3nm/DOCUMENTS/stage/paris","yago/memory","dbpedia/memory","eqv","eqvtsv",new GoldStandard(1484735,GoldStandard.yagoDbpediaRelations));
  public static final Setting imdbyago=new Setting("ImdbYago","/media/ssd/fabian/data","yago/berkeley","imdb/berkeley","eqv","eqvtsv",new GoldImdbYago());
}
