package ed.inf.adbs.minibase.base;

/**
 * Variable is an instance of a term which is a variable representing a value
 */
public class Variable extends Term {
    private final String name;

    public Variable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object object) {
        if (!super.equals(object)) return false;
        return ((Variable) object).getName().equals(this.getName());
    }
}
