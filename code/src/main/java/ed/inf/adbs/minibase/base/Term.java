package ed.inf.adbs.minibase.base;

/**
 * Term class represents term objects used throughout, contains a function to check equality
 */
public class Term {
    @Override
    public boolean equals(Object object){
        if (this == object) return true;
        return object != null && getClass() == object.getClass();
    }

}


