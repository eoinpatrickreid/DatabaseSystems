package ed.inf.adbs.minibase.base;

import ed.inf.adbs.minibase.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Relational atom class holds the name and terms of a relational atom, contains functions in order to get the name
 * and terms, and to check equality
 */
public class RelationalAtom extends Atom {
    private final String name;

    private final List<Term> terms;

    public RelationalAtom(String name, List<Term> terms) {
        this.name = name;
        this.terms = terms;
    }

    public String getName() {
        return name;
    }

    public List<Term> getTerms() {
        return terms;
    }

    public List<String> getTermsAsString() {
        List<String> list = new ArrayList<>();
        for (Term t : terms){
            list.add(t.toString());
        }
        return list;
    }

    @Override
    public String toString() {
        return name + "(" + Utils.join(terms, ", ") + ")";
    }

    /**
     * Returns true if this and another relational atom are equal
     * @param obj relational atom to compare to
     * @return boolean true if equal false otherwise
     */
    @Override
    public boolean equals(Object obj){
        if (this == obj){
            return true;
        }
        if (obj==null){
            return false;
        }
        if(!(obj instanceof RelationalAtom)){
            return false;
        }
        RelationalAtom relationalAtom = (RelationalAtom) obj;
        return relationalAtom.getName().equals(this.getName()) && relationalAtom.getTermsAsString().equals(this.getTermsAsString());
    }
}
