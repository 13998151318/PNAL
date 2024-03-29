package pnal;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import bak.pcj.benchmark.IntKeyMapFactory;
import bak.pcj.map.IntKeyDoubleMap;
import bak.pcj.map.IntKeyDoubleOpenHashMap;
import javatools.datatypes.Pair;
import javatools.datatypes.PeekIterator;
import pnal.nal.TruthValue;
import pnal.storage.FactStore;

/** This class is part of the PARIS ontology matching project at INRIA Saclay/France.
 * 
 * It is licensed under a Creative Commons Attribution Non-Commercial License
 * by the author Fabian M. Suchanek (http://suchanek.name). For all further information,
 * see http://webdam.inria.fr/paris
 *
 * This class stores an alignment between relations, using hashmaps.
 * Hence, it is slower than MatrixSubRelationStore but suitable for joins */

// TODO There is code duplication with HashSubThingStore

/* beware, this class is not thread-safe for writes */
public class HashSubRelationStore extends SubRelationStore implements Closeable {

    /** Maps ids to pairs */
    protected Map<Long, SubPair<JoinRelation>> primaryIndex;
    private long freshPairId;
    
    /** Maps subrel to pairs*/
    protected MultiMap<JoinRelation, SubPair<JoinRelation>> subIndex;

    /** Maps second arg to pairs*/
    protected MultiMap<JoinRelation, SubPair<JoinRelation>> superIndex;
    
    /** Maps first and second arg to pairs */
    protected HashMap<Integer, TruthValue> cross;
    

    public HashSubRelationStore(FactStore fs1, FactStore fs2) {
        super(fs1, fs2);
        primaryIndex = new HashMap<Long, SubPair<JoinRelation>>();
        subIndex = new MultiMap<JoinRelation, SubPair<JoinRelation>>(new MultiMap.HashSetFactory<SubPair<JoinRelation>>());
        superIndex = new MultiMap<JoinRelation, SubPair<JoinRelation>>(new MultiMap.HashSetFactory<SubPair<JoinRelation>>());
        cross = new HashMap<Integer, TruthValue>();
        freshPairId = 0;
    }
    
    public void clear() {
        primaryIndex.clear();
        subIndex.clear();
        superIndex.clear();
        cross.clear();
        freshPairId = 0;
    }
    
    public int code(JoinRelation sub, JoinRelation supr) {
        return sub.code() * fs2.maxJoinRelationCode() + supr.code();
    }
    
    public TruthValue getTruthValue(JoinRelation sub, JoinRelation supr) {
        return cross.get(code(sub, supr));
    }
    

    public void set(SubPair<JoinRelation> e) {
        long myId = freshPairId++;
        e.id = myId;
        primaryIndex.put(myId, e);
        subIndex.put(e.sub, e);
        superIndex.put(e.supr, e);
        cross.put(code(e.sub, e.supr), e.truthValue);  	
    }

    public void setTruthValue(JoinRelation sub, JoinRelation supr, TruthValue truthValue) {
        set(new SubPair<JoinRelation>(sub, supr, truthValue));
    }


    /* (non-Javadoc)
     * @see paris.SubThingStore#all()
     */
    public Iterable<SubPair<JoinRelation>> all() {
        return (primaryIndex.values());
    }

    public PeekIterator<SubPair<JoinRelation>> getSuperEntities(JoinRelation supr) {
        return new PeekIterator.SimplePeekIterator<SubPair<JoinRelation>>(superIndex.get(supr).iterator());
    }

    public Set<JoinRelation> superOf(JoinRelation sub) {
        Set<JoinRelation> result = new TreeSet<JoinRelation>();
        for (SubPair<JoinRelation> a : subIndex.getOrEmpty(sub)) {
            result.add(a.supr);
        }
        return (result);
    }

    public Collection<Pair<Object, TruthValue>> superOfScored(JoinRelation sub) {
        List<Pair<Object, TruthValue>> result = new ArrayList<Pair<Object, TruthValue>>();
        for (SubPair<JoinRelation> a : subIndex.getOrEmpty(sub)) {
            result.add(new Pair<Object, TruthValue>(a.supr, a.truthValue));
        }
        return (result);
    }

    public Set<JoinRelation> subOf(JoinRelation supr) {
        Set<JoinRelation> result = new TreeSet<JoinRelation>();
        for (SubPair<JoinRelation> a : superIndex.getOrEmpty(supr)) {
            result.add(a.sub);
        }
        return (result);
    }

    public Collection<Pair<Object, TruthValue>> subOfScored(JoinRelation supr) {
        List<Pair<Object, TruthValue>> result = new ArrayList<Pair<Object, TruthValue>>();
        for (SubPair<JoinRelation> a : superIndex.getOrEmpty(supr)) {
            result.add(new Pair<Object, TruthValue>(a.sub, a.truthValue));
        }
        return (result);
    }

    @Override
    public TruthValue getTruthValueCode(int sub, int supr) {
        return getTruthValue(new JoinRelation(fs1, sub), new JoinRelation(fs2, supr));
    }
    
}
