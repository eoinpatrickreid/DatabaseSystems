package ed.inf.adbs.minibase.base;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Tuple class holds tuple objects, used to store lines from given tables, stores the list of terms for a tuple and
 * the schema for those terms
 */
public class Tuple implements Comparable<Tuple> {

    //tuple values
    private final Term[] tuple;
    //tuple schema
    private final List<String> schema;

    /**
     * Tuple constructor
     * @param t terms
     * @param s schema
     */
    public Tuple(Term[] t, List<String> s) {
        this.tuple = t;
        this.schema = s;
    }

    /**
     * Returns value of tuple at index i
     * @param i index
     * @return value at i
     */
    public Term getPosition(int i) {
        return tuple[i];
    }

    /**
     * Returns the tuple values
     * @return tuple values
     */
    public Term[] getTuple() {
        return tuple;
    }

    /**
     * Returns the tuple size
     * @return tuple length
     */
    public int getLength() {
        return tuple.length;
    }

    /**
     * Returns the values in the form of a string
     * @return String of tuple values separated by ", "
     */
    @Override
    public String toString() {
        StringBuilder t = new StringBuilder();
        for (int i = 0; i < tuple.length; ++i) {
            if(tuple[i] instanceof IntegerConstant)
                t.append(tuple[i].toString().replaceAll("'", ""));
            else
                t.append(tuple[i].toString());
            if (i != tuple.length - 1) t.append(", ");
        }
        return t.toString();
    }

    /**
     * Wtites the tuple to the output file
     */
    public void dump() {
        try {
            String outputPath = DatabaseCatalogue.getOutputPath();
            String t = this + "\n";
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath, true));
            bw.write(t);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the tuple's schema
     * @return tuple's schema
     */
    public List<String> getSchema() {
        return schema;
    }

    /**
     * Returns the position of an attribute in the tuple
     * @param attr attribute of the tuple
     * @return position of an attribute in the tuple
     */
    public int getAttrPos(String attr) {
        if (attr.equals("*")) return -1;
        return schema.indexOf(attr);
    }

    /**
     * Returns 0 if tuples are equal and -1 otherwise
     * @param t2 tuple to compare to
     * @return int representing whether they're equal or not
     */
    @Override
    public int compareTo(Tuple t2) {
        Term[] t2_elems = t2.getTuple();
        if(tuple.length == t2_elems.length) {
            boolean areEqual = true;
            for(int i = 0; i<tuple.length; i++) {
                if (!tuple[i].equals(t2_elems[i])) {
                    areEqual = false;
                    break;
                }
            }
            if(areEqual) return 0;
            return -1;
        }
        return -1;
    }

}
