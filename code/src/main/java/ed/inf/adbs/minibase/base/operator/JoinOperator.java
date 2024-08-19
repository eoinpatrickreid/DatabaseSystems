package ed.inf.adbs.minibase.base.operator;

import java.util.*;

import ed.inf.adbs.minibase.base.*;


/**
 *
 * Join operator has left and right child. Contains implementation of simple tuple nested loop join algorithm
 *
 */
public class JoinOperator extends Operator {
    // Left and right child operators
    private final Operator leftOperator;
    private final Operator rightOperator;
    // List of conditions
    private final List<ComparisonAtom> comparisonAtoms;
    // Tuples for the left and right tables
    private Tuple leftTuple;
    private Tuple rightTuple;
    // List of strings representing the common variables in the tuples
    private final List<String> commonVariables;
    // maps for the int positions of terms in each tuple
    Map<String, Integer> termPositions1 = new HashMap<>();
    Map<String, Integer> termPositions2 = new HashMap<>();

    /**
     * JoinOperator constructor; reads in arguments and then initialises the join by getting the first tuple of both the
     * left and right operators
     * @param left operator for outer table
     * @param right operator for inner table
     * @param atoms join conditions list
     * @param variables Common variable names list
     */
    public JoinOperator (Operator left, Operator right, List<ComparisonAtom> atoms, List<String> variables) {
        leftOperator = left;
        rightOperator = right;
        this.comparisonAtoms = atoms;
        this.commonVariables = variables;
        leftTuple = leftOperator.getNextTuple();
        rightTuple = rightOperator.getNextTuple();
    }

    /**
     * Returns the next tuple
     * @return next tuple resulting of the join condition
     */
    @Override
    public Tuple getNextTuple() {
        try {
            Tuple tuple = null;
            //while there are still tuples in the left operator
            while (leftTuple != null) {
                List<String> leftTupleSchema = leftTuple.getSchema();
                List<String> rightTupleSchema = rightTuple.getSchema();
                // while i is less than the size of both of the tuples
                for (int i=0; i<leftTupleSchema.size() || i<rightTupleSchema.size();i++){
                    // If there's still attributes in the first tuple add their position to relevant position map
                    if(i<leftTupleSchema.size()){
                        String[] splitLine = leftTupleSchema.get(i).split("\\.");
                        termPositions1.put(splitLine[1], i);
                    }
                    if(i<rightTupleSchema.size()){
                        String[] splitLine = rightTupleSchema.get(i).split("\\.");
                        termPositions2.put(splitLine[1], i);
                    }
                }
                // Evaluate the conditions and the common variables
                boolean compareBool = true;
                if(comparisonAtoms.size()>0)
                    for(ComparisonAtom a : comparisonAtoms) {
                        compareBool = compareBool & evaluateAtom(a);
                    }
                if(commonVariables.size()>0) {
                    for(String var : commonVariables) {
                        compareBool = compareBool & evaluateAtom(var);
                    }
                }
                //If all join conditions are met, then combine the tuples
                if (compareBool)
                    tuple = combineTuples(leftTuple, rightTuple);
                // If not at the last tuple for inner table then get next tuple
                if (rightTuple != null) //get next tuple for the inner table
                    rightTuple = rightOperator.getNextTuple();

                //If at the last inner table tuple then reset inner table and get next tuple of outer table and
                // first tuple of inner table
                if (rightTuple == null) {
                    rightOperator.reset();
                    leftTuple = leftOperator.getNextTuple();
                    rightTuple = rightOperator.getNextTuple();
                }
                // Return the combined tuple (if there is one)
                if (tuple != null)
                    return tuple;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Evaluates whether a comparison condition is met for a given comparison atom
     * @param comparisonAtom condition to be tested
     * @return returns true if the condition is met and false otherwise
     */
    public boolean evaluateAtom(ComparisonAtom comparisonAtom) {
        //Get the operator and terms for the condition
        ComparisonOperator operator = comparisonAtom.getOp();
        String term1Name = ((Variable) comparisonAtom.getTerm1()).getName();
        String term2Name = ((Variable) comparisonAtom.getTerm2()).getName();

        // Get the positions of the first and second terms from the first and second tuples
        Term term1 = leftTuple.getPosition(termPositions1.get(term1Name));
        Term term2 = rightTuple.getPosition(termPositions2.get(term2Name));
        //if both terms are strings then do string comparison
        if((term1 instanceof StringConstant) & (term2 instanceof StringConstant)) {
                return evaluateComparison(operator, ((StringConstant) term1).getValue(), ((StringConstant) term2).getValue());
            //if both values are integers, return expression evaluation
        } else if((term1 instanceof IntegerConstant) & (term2 instanceof IntegerConstant)){
            return evaluateComparison(operator, ((IntegerConstant) term1).getValue(), ((IntegerConstant) term2).getValue());
        }
        return false;

    }

    /**
     * Evaluates whether two variables are equal
     * @param variable variable name
     * @return returns true if condition is met and false otherwise
     */
    public boolean evaluateAtom(String variable) {
        // Get the position of the variable in left and right tuples
        Term term1 = leftTuple.getPosition(termPositions1.get(variable));
        Term term2 = rightTuple.getPosition(termPositions2.get(variable));
        // if both terms are strings then do string equality check
        if ((term1 instanceof StringConstant) & (term2 instanceof StringConstant)) {
            return ((StringConstant) term1).getValue().equals(((StringConstant) term2).getValue());
            // if both terms are strings then do string equality check
        } else if ((term1 instanceof IntegerConstant) & (term2 instanceof IntegerConstant)) {
            return Objects.equals(((IntegerConstant) term1).getValue(), ((IntegerConstant) term2).getValue());
        }
        return false;
    }

    /**
     * If the two terms to be compared are integers then this function is called, it checks whether the terms satisfy
     * the given operator.
     * @param operator operator with which to compare the two terms
     * @param first first int term to be compared
     * @param second second int term to be compared
     * @return boolean returns true if the comparison is satisfied and false otherwise
     */
    private boolean evaluateComparison(ComparisonOperator operator, int first, int second) {
        if(operator.toString().matches("=")) {
            return first==second;
        }else if(operator.toString().matches("<=")){
            return first<=second;
        }else if(operator.toString().matches(">=")) {
            return first>=second;
        }else if(operator.toString().matches("<")) {
            return first<second;
        }else if(operator.toString().matches(">")) {
            return first>second;
        } else if(operator.toString().matches("!=")) {
            return first!=second;
        }else return false;
    }

    /**
     * If the two terms to be compared are strings then this function is called, it checks whether the terms satisfy
     * the given operator.
     * @param operator operator with which to compare the two terms
     * @param first first string term to be compared
     * @param second second string term to be compared
     * @return boolean returns true if the comparison is satisfied and false otherwise
     */
    private boolean evaluateComparison(ComparisonOperator operator, String first, String second) {
        if(operator.toString().matches("=")) {
            return first.equals(second);
        }else if(operator.toString().matches(">=")) {
            return first.compareTo(second)>=0;
        }else if(operator.toString().matches("<=")) {
            return first.compareTo(second)<=0;
        }else if(operator.toString().matches(">")) {
            return first.compareTo(second)>0;
        }else if(operator.toString().matches("<")) {
            return first.compareTo(second)<0;
        }else if(operator.toString().matches("!=")) {
            return !first.equals(second);
        } else return false;
    }

    /**
     * Given two tuples,create the schema for a new combined tuple, and full the new tuple with attributes from both
     * tuples
     * @param first first tuple to be combined
     * @param second second tuple to be combined
     * @return returns the combined tuple with new attributes and new schema
     */
    public Tuple combineTuples(Tuple first, Tuple second){
        List<String> schema;
        Term[] tuple;
        List<String> initialSchema = second.getSchema();
        List<String> finalSchema = new ArrayList<>();
        String table = first.getSchema().get(0).split("\\.")[0];
        //modify the schema of the second tuple to add to the new schema
        for(String col : initialSchema) {
            String [] splitCol = col.split("\\.");
            String ans = table + "." + splitCol[1];
            finalSchema.add(ans);
        }
        // create new schema adding the first tuples schema followed by the second tuples
        schema = new ArrayList<>();
        schema.addAll(first.getSchema());
        schema.addAll(finalSchema);
        int firstLength = first.getLength();
        int secondLength = second.getLength();
        tuple = new Term[firstLength + secondLength];
        //Add the terms from both tuples to the new tuple
        for (int i = 0; i < tuple.length; i++) {
            if (i < firstLength)
                tuple[i] = first.getPosition(i);
            else tuple[i] = second.getPosition(i - firstLength);
        }
        return new Tuple(tuple, schema);
    }

    @Override
    public void reset() {

    }

}
