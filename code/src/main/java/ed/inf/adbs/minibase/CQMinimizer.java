package ed.inf.adbs.minibase;

import ed.inf.adbs.minibase.base.*;
import ed.inf.adbs.minibase.parser.QueryParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * Minimization of conjunctive queries
 *
 */
public class CQMinimizer {

    /**
     * Main function call, takes as input the input and output file paths and calls minimizeCQ function on them
     * @param args input and output file paths respectively
     */

    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("Usage: CQMinimizer input_file output_file");
            return;
        }

        String inputFile = args[0];
        String outputFile = args[1];

        minimizeCQ(inputFile, outputFile);
    }

    /**
     * CQ minimization procedure
     * Given an input file path, parse the query at the file,then minimize the query by removing each atom
     * and seeing if a homomorphism exists from the body to the body without the atom, if it does remove the given
     * atom. Print the minimized query to the output file
     *
     * Assume the body of the query from inputFile has no comparison atoms
     * but could potentially have constants in its relational atoms.
     * @param inputFile file path for input
     * @param outputFile file path for output
     */
    public static void minimizeCQ(String inputFile, String outputFile) {
        //Parse the query from the input file
        Query query = null;
        try {
            query = QueryParser.parse(Paths.get(inputFile));
        } catch (Exception e) {
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
        }
        assert query != null;
        // obtain the head and the body from the query, call removeDuplicates in order to remove duplicate atoms
        Head head = query.getHead();
        List<RelationalAtom> body = query.getRelationalBody();
        body = removeDuplicates(body);

        // While a change has been made, try to remove each atom from the body, if when removing an atom the resulting body; body \ {a}...
        //... contains all terms in the query head and  a homomorphism exists from the original body to the new body...
        //... then remove that atom
        boolean changeMade;
        do {
            Atom atomToRemove;
            changeMade = false;
            for (RelationalAtom atom: body) {
                List<RelationalAtom> body2 = new ArrayList<>(body);
                body2.remove(atom);
                if (!headTermsPreserved(head, body2)){
                    continue;
                }
                if (homomorphism(body, body2)) {
                    changeMade = true;
                    atomToRemove = atom;
                    if (atomToRemove != null) {
                        body.remove(atomToRemove);
                        break;
                    }
                }
            }
        } while (changeMade);

        // Form the minimized query and write it to the outputfile
        List<Atom> finalBody = new ArrayList<>(body);
        Query finalQuery = new Query(head, finalBody);
        try {
            queryToFile(finalQuery, outputFile);
        } catch (IOException e) {
            System.err.println("Error occurred when writing query to file");
            e.printStackTrace();
        }
    }

    /**
     * Take two bodies; body and body \{a}, form all mappings from the first to the second, then for each mmapping
     * check if the logical relationship has been preserved, if so a homomorphism exists - return true, otherwise return
     * false
     * @param baseQuery the body of the original query
     * @param targetQuery the body with an atom removed
     * @return boolean true if a homomorphism exists from base to target query, false otherwise
     */
    public static boolean homomorphism(List<RelationalAtom> baseQuery, List<RelationalAtom> targetQuery) {
        for(Map<String, Term> variableMapping : getMappings(baseQuery, targetQuery)){
            List<RelationalAtom> newQuery = formQuery(baseQuery, variableMapping);
            if (preservedRelationships(newQuery, targetQuery)){
                return true;
            }
        }
        return false;
    }

    /**
     * Get mappings function
     * Get all possible mappings from terms in the baseQuery to terms in the targetQuery
     * @param baseQuery the body of the original query
     * @param targetQuery the body with an atom removed
     * @return return a list containing each possible mapping from baseQuery terms to TargetQuery terms
     */

    private static List<Map<String, Term>> getMappings(List<RelationalAtom> baseQuery, List<RelationalAtom> targetQuery) {
        Set<Term> targetTerms = new HashSet<>();
        Set<String> visited = new HashSet<>();
        //get all unique terms in the base query as variables
        for (RelationalAtom baseAtom : baseQuery){
            List<Term> aTerms = baseAtom.getTerms();
            for (Term t : aTerms) {
                if (t instanceof Variable) {
                    visited.add(t.toString());
                }
            }
        }
        for (RelationalAtom targetAtom : targetQuery){
            List<Term> tTerms = targetAtom.getTerms();
            targetTerms.addAll(tTerms);
        }
        String[] stringVars = visited.toArray(new String[0]);
        Term[] targetT = targetTerms.toArray(new Term[0]);

        return generateMappings(stringVars, targetT);
    }

    /**
     * Generate Mappings
     * given an array of strings (representing terms) and an array of terms, call helper function returning all possible
     * mappings from first array to second. I used array of strings as had problems where terms with same value
     * would appear multiple times
     * @param arr1 List of terms in string form
     * @param arr2 list of terms
     * @return List of mappings from first array to second
     */
    public static List<Map<String, Term>> generateMappings(String[] arr1, Term[] arr2){
        List<Map<String, Term>> mappings = new ArrayList<>();
        generateMappingsHelper(arr1, arr2, new HashMap<>(), mappings);
        return mappings;
    }

    /**
     * Generate Mappings Helper
     * Function that recursively adds mappings from arr1 to arr2 to the mappings list provided to it, resulting in the
     * complete list of all mappings
     * @param arr1 Array of strings representing terms in the first body
     * @param arr2 Array of terms from the second body
     * @param mapping current mapping being built to add to mappings when complete
     * @param mappings overall list of all mappings
     */
    private static void generateMappingsHelper(String[] arr1, Term[] arr2, Map<String, Term> mapping, List<Map<String, Term>> mappings){
        if (arr1.length == 0){
            mappings.add(mapping);
            return;
        }
        String c1 = arr1[0];
        String[] rest1 = getRest(arr1);
        for (Term c2 : arr2) {
            Map<String, Term> newMapping = new HashMap<>(mapping);
            newMapping.put(c1, c2);
            generateMappingsHelper(rest1, arr2, newMapping, mappings);
        }
    }

    /**
     * Get Rest
     * Function that returns the rest of the array of strings without the first element for use in recursion
     * @param arr array to get subarray from
     * @return string array containing the rest of the array
     */
    private static String[] getRest(String[] arr) {
        String[] rest = new String[arr.length - 1];
        System.arraycopy(arr, 1, rest, 0, arr.length - 1);
        return rest;
    }


    /**
     * Form Query
     * Given a list of relational atoms and a mapping, apply the mapping to each atom and return the resulting list of
     * all new atoms
     * @param baseQ the original query to be changed
     * @param mapping the mapping to apply to the base query
     * @return a list of relational atoms representing the original atoms with the mapping applied
     */

    private static List<RelationalAtom> formQuery(List<RelationalAtom> baseQ, Map<String, Term> mapping){
        List<RelationalAtom> newQuery = new ArrayList<>();
        for(RelationalAtom currAtom: baseQ){
            String name = currAtom.getName();
            List<Term> terms = currAtom.getTerms();
            List<Term> newTerms = new ArrayList<>();
            for(Term t: terms){
                if (t instanceof Variable) {
                    newTerms.add(mapping.get(t.toString()));
                } else {
                    newTerms.add(t);
                }
            }
            RelationalAtom newAtom = new RelationalAtom(name, newTerms);
            newQuery.add(newAtom);
        }
        return newQuery;
    }

    /**
     * Preserved Relationships
     * given the query formed with a given mapping (mapQuery) and the query we want to achieve (targetQuery), check each
     * atom in mapQuery is equivalent to at least one atom in targetMapping. If each atom has an equivalent atom return
     * then this is a valid mapping return true, otherwise return false
     * @param mapQuery the list of relational atoms formed for a given mapping
     * @param targetMapping the list of relational atoms we want to achieve with the mapping
     * @return boolean, true if the mapping preserves the relationship false otherwise
     */
    private static boolean preservedRelationships(List<RelationalAtom> mapQuery, List<RelationalAtom> targetMapping) {
        boolean preserved = false;
        for(RelationalAtom mapAtom: mapQuery){
            preserved = false;
            for(RelationalAtom targetAtom: targetMapping){
                if (mapAtom.equals(targetAtom)) {
                    preserved = true;
                    break; //here
                }
            }
            if (!preserved){
                return false;
            }
        }
        return preserved;
    }


    /**
     * Given a list of RelationalAtoms remove any duplicate atoms and return the resulting list
     * @param query the list of relational atoms we want to remove duplicates from
     * @return query without any duplicates
     */
    private static List<RelationalAtom> removeDuplicates(List<RelationalAtom> query){
        if (query.size() <= 1){
            return query;
        }
       boolean tracker = false;
        List<RelationalAtom> finalBody = new ArrayList<>();
        List<RelationalAtom> bodyHolder;
        bodyHolder = new ArrayList<>(query);
        RelationalAtom head = query.get(0);
        bodyHolder.remove(head);
        if (bodyHolder.isEmpty()){
            return finalBody;
        }
        for (RelationalAtom atom: bodyHolder){
            if (head.equals(atom)) {
                tracker = true;
                break;
            }
        }
        if (tracker){
            finalBody = removeDuplicates(bodyHolder);
        } else {
            List<RelationalAtom> newList = new ArrayList<>();
            newList.add(head);
            finalBody = Stream.concat(newList.stream(), removeDuplicates(bodyHolder).stream()).collect(Collectors.toList());
        }
        return finalBody;
    }

    /**
     * Check head terms are in new body. Given the head and the proposed new body, return true if all terms in the head
     * are present in the new body,and false otherwise
     * @param head query head
     * @param newBody proposed body
     * @return boolean, true if head terms are in new body false otherwise
     */
    private static boolean  headTermsPreserved(Head head, List<RelationalAtom> newBody){
        Set<String> hTerms = head.getVariablesAsStringSet();
        Set<String> bTerms = new HashSet<>();
        List<Term> termList = new ArrayList<>();
        boolean found = true;
        for (RelationalAtom atom: newBody){
            termList.addAll(atom.getTerms());
        }
        for (Term term: termList){
            bTerms.add(term.toString());
        }
        for (String hString: hTerms){
            if (!bTerms.contains(hString)) {
                found = false;
                break;
            }
        }
        return found;
    }

    /**
     * Write the new query to the output file
     * @param query the minimized query to be written
     * @param fileName the path of the output file
     * @throws IOException if error writing to file
     */
    public static void queryToFile(Query query, String fileName) throws IOException {
        File outputFile = Paths.get(fileName).toFile();
        outputFile.createNewFile();
        try (FileWriter writer = new FileWriter(outputFile)){
            writer.write(query.toString());
        }
    }




}
