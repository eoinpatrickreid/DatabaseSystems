package ed.inf.adbs.minibase.base;

import ed.inf.adbs.minibase.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * query class holds the head and body of a query, contains functions to retur the head and body in different formats
 * depending on how you plan to use them
 */
public class Query {
    private final Head head;

    private final List<Atom> body;

    public Query(Head head, List<Atom> body) {
        this.head = head;
        this.body = body;
    }

    public Head getHead() {
        return head;
    }

    /**
     * Returns the head as a relational atom
     * @return RelationalAtom representing the head of the query
     */
    public RelationalAtom getHeadAsRelationalAtom() {
        String name = this.head.getName();
        List<Variable> variables = this.head.getVariables();
        List<Term> terms = new ArrayList<>(variables);
        return new RelationalAtom(name, terms);
    }

    public List<Atom> getBody() {
        return body;
    }

    /**
     * Returns the body in the form of a list of relational atoms
     * @return list of relational atoms representing the body of the query
     */
    public List<RelationalAtom> getRelationalBody() {
        List <RelationalAtom> relationalBody = new ArrayList<>();
        for (Atom a : body) {
            if (a.getClass() == RelationalAtom.class){
                relationalBody.add((RelationalAtom) a);
            }
        }
        return relationalBody;
    }

    @Override
    public String toString() {
        return head + " :- " + Utils.join(body, ", ");
    }
}
