package ed.inf.adbs.minibase.base;

import ed.inf.adbs.minibase.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Head class stores the data for the head of a query, this includes the name of the query and the variables in the
 * query head
 */
public class Head {
    private final String name;

    private final List<Variable> variables;

    private final SumAggregate agg;

    public Head(String name, List<Variable> variables, SumAggregate agg) {
        this.name = name;
        this.variables = variables;
        this.agg = agg;
    }

    public String getName() {
        return name;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    /**
     * Returns a set containing only unique variables by string
     * @return set of string representing unique variables
     */
    public Set<String> getVariablesAsStringSet() {
        Set<String> vars = new HashSet<>();
        for (Variable v : variables){
            vars.add(v.toString());
        }
        return vars;
    }

    public SumAggregate getSumAggregate() {
        return agg;
    }

    @Override
    public String toString() {
        if (agg == null) {
            return name + "(" + Utils.join(variables, ", ") + ")";
        }
        if (variables.isEmpty()) {
            return name + "(" + agg + ")";
        }
        return name + "(" + Utils.join(variables, ", ") + ", " + agg + ")";
    }


}
