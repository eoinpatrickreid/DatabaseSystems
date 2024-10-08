package ed.inf.adbs.minibase.base;

import ed.inf.adbs.minibase.Utils;

import java.util.List;

public class SumAggregate extends Term {

    private final List<Term> productTerms;

    public SumAggregate(List<Term> terms) {
        this.productTerms = terms;
    }

    public List<Term> getProductTerms() {
        return productTerms;
    }

    @Override
    public String toString() {
        return "SUM(" + Utils.join(productTerms, " * ") + ")";
    }

    public SumAggregate deepCopy(){
    return new SumAggregate(this.productTerms);
    }
    @Override
    public boolean equals(Object obj){
        return true;
    }
}
