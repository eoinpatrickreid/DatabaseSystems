package ed.inf.adbs.minibase.base.operator;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import ed.inf.adbs.minibase.base.Tuple;

/**
 *
 * Distinct operator, purpose is to give a set of unique tuples given a list of tuples
 *
 */
public class DistinctOperator extends Operator {

    //original tuples
    private final List<Tuple> tuples;
    //set of unique tuples
    private final TreeSet<Tuple> distinctT;
    //unique tuples in order
    private final List<Tuple> distinctTuplesOrdered;

    /**
     * DistinctOperator constructor
     * @param tuples sorted tuples
     */
    public DistinctOperator(List<Tuple> tuples) {
        this.tuples = tuples;
        distinctT = new TreeSet<>();
        distinctTuplesOrdered = new ArrayList<>();
    }

    /**
     * Returns ordered list of unique tuples
     * @return list of unique tuples
     */
    public List<Tuple> getDistinctTuples() {
        for(Tuple tuple : tuples) {
            if(distinctT.contains(tuple)){
                continue;
            }
            distinctTuplesOrdered.add(tuple);
            distinctT.add(tuple);
        }
        return distinctTuplesOrdered;
    }


    @Override
    public Tuple getNextTuple() {
        return null;
    }

    @Override
    public void reset() {

    }
}