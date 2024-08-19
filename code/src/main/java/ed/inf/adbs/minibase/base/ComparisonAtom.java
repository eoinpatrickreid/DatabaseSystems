package ed.inf.adbs.minibase.base;

/**
 * Comparison atom extends atom, used for atoms from a query that are comparisons, holds the comparisons terms and the
 * operator on which the terms are to be compared. Contains functions to return the terms and operator, to check if
 * this comparison atom is equal to another
 */
public class ComparisonAtom extends Atom {

    private final Term term1;

    private final Term term2;

    private final ComparisonOperator op;

    public ComparisonAtom(Term term1, Term term2, ComparisonOperator op) {
        this.term1 = term1;
        this.term2 = term2;
        this.op = op;
    }

    public Term getTerm1() {
        return term1;
    }

    public Term getTerm2() {
        return term2;
    }

    public ComparisonOperator getOp() {
        return op;
    }

    @Override
    public String toString() {
        return term1 + " " + op + " " + term2;
    }

    public boolean equals(Object object){
        if (!super.equals(object)) return false;
        ComparisonAtom comparisonAtom = (ComparisonAtom) object;
        return this.term1.equals(comparisonAtom.getTerm1()) && this.term2.equals(comparisonAtom.getTerm2()) && this.getOp().equals(comparisonAtom.getOp());
    }

}
