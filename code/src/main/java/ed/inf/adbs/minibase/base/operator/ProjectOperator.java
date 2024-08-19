package ed.inf.adbs.minibase.base.operator;

import java.util.ArrayList;
import java.util.List;

import ed.inf.adbs.minibase.base.Term;
import ed.inf.adbs.minibase.base.Tuple;
import ed.inf.adbs.minibase.base.Variable;


/**
 *
 * Project Operator, extends operator, has one child operator. Allows queries that contain projection e.g. Q(x):-R(x,y), y>10
 *
 */
public class ProjectOperator extends Operator {
    private final List<Term> attributes;
    //child operator can be select or scan
    private final Operator operator;
    //table on which projection is done
    private final String tableName;

    /**
     * ProjectOperator constructor initialises variables
     * @param attribs the attributes in select clause
     * @param op child operator
     * @param table table name
     */
    public ProjectOperator(List<Term> attribs, Operator op, String table) {
        attributes = attribs;
        operator = op;
        tableName = table;
    }

    /**
     * Get next tuple, returns tuple with only the terms in attributes
     * @return tuple meeting out projection requirements
     */
    @Override
    public Tuple getNextTuple() {
        Tuple nextTuple = operator.getNextTuple();
        List<String> tupleSchema = new ArrayList<>();
        List<Integer> tupleAttributes = new ArrayList<>();

        //For each term store the position in the table and the new tuples schema
        for (Term attribute: attributes) {
            String table = tableName + "." + ((Variable)attribute).getName();
            tupleAttributes.add(nextTuple.getAttrPos(table));
            tupleSchema.add(table);
        }
        Term[] newTerms;
        newTerms = new Term[attributes.size()];
        int i = 0;
        for (Integer attribute: tupleAttributes){
            newTerms[i] = nextTuple.getPosition(attribute);
            i++;
        }
        return new Tuple(newTerms, tupleSchema);
    }

    /**
     * Calls the child operator's reset method
     */
    @Override
    public void reset() {
        operator.reset();
    }
}