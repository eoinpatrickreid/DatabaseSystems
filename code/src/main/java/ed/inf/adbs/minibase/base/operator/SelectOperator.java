package ed.inf.adbs.minibase.base.operator;

import java.util.List;

import ed.inf.adbs.minibase.base.*;


/**
 *
 * Operator for select queries, allows us to select tuples from a table based on a list of comparisons
 *
 */
public class SelectOperator extends Operator {
    //child scan operator
    private final ScanOperator scan;
    //list of conditions on which to select the tuples
    private final List<ComparisonAtom> comparisonAtoms;
    // table name
    String name;
    Tuple tuple;


    /**
     * SelectOperator constructor initialises variables
     * @param scanOperator child scan operator
     * @param comparisonAtomList list of conditions to select on
     */
    public SelectOperator(ScanOperator scanOperator, List<ComparisonAtom> comparisonAtomList) {
        scan = scanOperator;
        name = scanOperator.getName();
        comparisonAtoms = comparisonAtomList;

    }

    /**
     * Gets the next tuple using scan operator, then checks whether it meets all conditions, if it r=does then return
     * that tuple if not keep going till we find one that does and return it, or don't find one in which case return null
     * @return next tuple that fulfills all the given ComparisonAtom restrictions
     */
    @Override
    public Tuple getNextTuple() {
        try {
            Tuple nextTuple = scan.getNextTuple();
            while (nextTuple != null) {
                this.tuple = nextTuple;
                boolean accept = true;
                // for each condition check whether it is met, add the boolean result to accept, if it is true after all
                // comparison atoms then return the tuple, otherwise get the next tuple
                for(ComparisonAtom comparisonAtom : comparisonAtoms) {
                    accept = accept & evaluateAtom(comparisonAtom);
                }
                if (accept) {
                    return nextTuple;
                }
                nextTuple = scan.getNextTuple(); //else get the next tuple
            }
        } catch (Exception e) {
            System.out.println("Error in select operator when getting next tuple");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Evaluate a given condition to see if it holds true
     * @param comparisonAtom the condition to be evaluated
     * @return boolean true if the condition is satisfied and false otherwise
     */
    private boolean evaluateAtom(ComparisonAtom comparisonAtom) {
        // Get the terms and operator from the comparison atom
        Term first = comparisonAtom.getTerm1();
        Term second = comparisonAtom.getTerm2();
        ComparisonOperator op = comparisonAtom.getOp();
        // If the first term is a variable then get the relevant term value matching the variable from the tuple
        if(first instanceof Variable) {
            String name = ((Variable) first).getName();
            String attr = this.name + "." + name;
            int idx = DatabaseCatalogue.getAttrPos(attr);
            Term item = tuple.getPosition(idx);
            // If the second term is a constant then call compare constants to compare directly
            if (second instanceof Constant) {
                return compareConstants(op, (Constant) item, (Constant) second);
                // Otherwise if second term is a variable get the relevant value matching this variable and compare it
                // to the value we got above by calling compare constants on the values we obtain
            } else if (second instanceof Variable) {
                String name2 = ((Variable) second).getName();
                String attr2 = this.name + "." + name2;
                int idx2 = DatabaseCatalogue.getAttrPos(attr2);
                Term item2 = tuple.getPosition(idx2);
                return compareConstants(op, (Constant) item, (Constant) item2);
            }
            // If the first term is a constant
        } else if(first instanceof Constant) {
            // and so is the second then compare directly
            if (second instanceof Constant) {
                return compareConstants(op, (Constant) first, (Constant) second);
                // otherwise get the value associated with the second term and compare it to the constant first erm
            } else if (second instanceof Variable) {
                String name = ((Variable) second).getName();
                String attr = this.name + "." + name;
                int idx = DatabaseCatalogue.getAttrPos(attr);
                Term item = tuple.getPosition(idx);
                return compareConstants(op, (Constant) first, (Constant) item);
            }
        }
        //otherwise return false
        return false;
    }

    /**
     * Function compares two constant terms based on the operator provided and returns true if (term1 operator term2)
     * holds and false otherwise
     * @param operator the operator with which to compare the two terms
     * @param term1 the first term to be compared
     * @param term2 the second term to be compared
     * @return boolean value; true if the comparison is true and false otherwise
     */
    private boolean compareConstants(ComparisonOperator operator, Constant term1, Constant term2) {
        // If term 1 and term 2 are integers then get the respective values and compare them
        if((term1 instanceof IntegerConstant) & (term2 instanceof IntegerConstant)) {
            int val = ((IntegerConstant) term1).getValue();
            int val2 = ((IntegerConstant) term2).getValue();
            return compareVariables(operator, val, val2);
            //Otherwise if term1 is a string and term2 is a stirng compare the two strings and return the result
        } else if((term1 instanceof StringConstant) & (term2 instanceof StringConstant)){
            String val = ((StringConstant) term1).getValue();
            String val2 = ((StringConstant) term2).getValue();
            return compareVariables(operator, val, val2);
        }
        // Otherwise if the two items being compared aren't the same type return false
        return false;
    }

    /**
     * Compare variables strings, compares two string terms over a given operator and returns the result of the
     * comparison, true if it holds true false otherwise
     * @param operator the operator with which to compare terms 1 and 2
     * @param term1 the first term to be compared
     * @param term2 the second term to be compared
     * @return returns a boolean true if the condition holds over the two variables false otherwise
     */

    private boolean compareVariables(ComparisonOperator operator, String term1, String term2) {
        if(operator.toString().matches("=")){
            return term1.equals(term2);
        }else if(operator.toString().matches("!=")) {
            return !term1.equals(term2);
        }else if(operator.toString().matches(">=")) {
            return term1.compareTo(term2) >= 0;
        }else if(operator.toString().matches("<=")) {
            return term1.compareTo(term2) <= 0;
        }else if(operator.toString().matches("<")) {
            return term1.compareTo(term2) < 0;
        }else if(operator.toString().matches(">")) {
            return term1.compareTo(term2) > 0;
        }else {
            return false;
        }
    }

    /**
     * Compare variables ints, compares two int terms over a given operator and returns the result of the
     * comparison, true if it holds true false otherwise
     * @param operator the operator with which to compare terms 1 and 2
     * @param term1 the first term to be compared
     * @param term2 the second term to be compared
     * @return returns a boolean true if the condition holds over the two variables false otherwise
     */
    private boolean compareVariables(ComparisonOperator operator, int term1, int term2) {
        if(operator.toString().matches("=")){
            return term1==term2;
        }else if(operator.toString().matches(">")){
            return term1 > term2;
        }else if(operator.toString().matches("!=")){
            return term1 != term2;
        }else if(operator.toString().matches(">=")) {
            return term1 >= term2;
        }else if(operator.toString().matches("<=")) {
            return term1 <= term2;
        } else if(operator.toString().matches("<")) {
            return term1 < term2;
        }else {
            return false;
        }
    }


    /**
     * Reset calls child's reset
     */
    @Override
    public void reset() {
        scan.reset();
    }
}