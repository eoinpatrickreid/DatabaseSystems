package ed.inf.adbs.minibase.base.operator;

import ed.inf.adbs.minibase.base.Tuple;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract Operator class, contains the basic functions to be inherited by all other operator classes
 */

public abstract class Operator {
    public abstract Tuple getNextTuple();

    public abstract void reset();

public void dump() {
        Tuple current = getNextTuple();
        do {
            current.dump();
            current = getNextTuple();
        } while (current!=null);
}

    /**
     * For queries with projection use this dump
     * @param result list of the resulting tuples
     */
    public void dump(List<Tuple> result) {
        for (Tuple t:result)
            t.dump();
    }


    /**
     * Returns the result of the query
     * @return List of tuples representing the result of the query
     */
    public List<Tuple> getQueryResult() {
        List<Tuple> result =  new ArrayList<>();
        try {
            Tuple t = getNextTuple();
            while (t != null) {
                result.add(t);
                t = getNextTuple();
            }
        } catch (NullPointerException ignored) {
        }
        return result;
    }
}
