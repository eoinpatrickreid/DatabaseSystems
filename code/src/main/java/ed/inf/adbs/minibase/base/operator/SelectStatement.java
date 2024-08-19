package ed.inf.adbs.minibase.base.operator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ed.inf.adbs.minibase.base.DatabaseCatalogue;
import ed.inf.adbs.minibase.base.Atom;
import ed.inf.adbs.minibase.base.ComparisonAtom;
import ed.inf.adbs.minibase.base.ComparisonOperator;
import ed.inf.adbs.minibase.base.Constant;
import ed.inf.adbs.minibase.base.IntegerConstant;
import ed.inf.adbs.minibase.base.Query;
import ed.inf.adbs.minibase.base.RelationalAtom;
import ed.inf.adbs.minibase.base.StringConstant;
import ed.inf.adbs.minibase.base.Term;
import ed.inf.adbs.minibase.base.Tuple;
import ed.inf.adbs.minibase.base.Variable;

/**
 *
 * The select statement class has functions to build and run the query
 *
 */
public class SelectStatement {

    //variables to project
    private static List<Term> projectTerms;
    //all distinct variables
    private static List<Term> allVariables;
    //tables involved
    private static List<String> schema= new ArrayList<>();
    //only select conditions per table
    private static Map<String, List<ComparisonAtom>> selectConditions = new HashMap<>();
    //variables per table
    private static Map<String, List<Term>> selectItems = new HashMap<>();
    //only join conditions per table
    private static Map<String, List<ComparisonAtom>> joinConditions = new HashMap<>();
    //attribute position map
    private static Map<String, Integer> attributePositions = new HashMap<>();

    /**
     * SelectStatement constructor, initialises variables, initialises DatabaseCatalogue variables
     * @param query query to be evaluated
     */
    public SelectStatement(Query query) {
        RelationalAtom head = query.getHeadAsRelationalAtom();
        List<Atom> body = query.getBody();
        projectTerms = head.getTerms();
        schema = new ArrayList<>();
        joinConditions = new HashMap<>();
        selectConditions = new HashMap<>();
        selectItems = new HashMap<>();
        attributePositions = new HashMap<>();
        allVariables = new ArrayList<>();
        int id=0;
        for(Atom atom : body) {
            if(atom instanceof RelationalAtom) {
                String from = ((RelationalAtom) atom).getName();
                List<Term> terms = ((RelationalAtom) atom).getTerms();
                //Form then set the alias for the table
                String newAlias = from + id;
                DatabaseCatalogue.setAlias(newAlias, from);
                // Form then set the schema for the table
                List<String> newSchema = createTableSchema(newAlias, terms);
                id = id+1;
                DatabaseCatalogue.setSchemaList(newAlias, newSchema);
            }
        }
        //map attributes to their positions
        DatabaseCatalogue.setAttributePosition(attributePositions);
        // for each comparison atom in the body decide whether it is a selection or join condition
        for(Atom atom : body) {
            if(atom instanceof ComparisonAtom)
                decideConditionType((ComparisonAtom) atom);
        }
    }

    /**
     * Checks which table the terms belong to then adds ComparisonAtom to joinConditions if condition involves multiple
     * tables and to selectConditions otherwise
     * @param comparisonAtom ComparisonAtom to consider
     */
    private void decideConditionType(ComparisonAtom comparisonAtom) {
        Term first = comparisonAtom.getTerm1();
        Term second = comparisonAtom.getTerm2();
        for(String table : selectItems.keySet()) {
            List<Term> tableItems = selectItems.get(table);
            if((tableItems.contains(first) & !tableItems.contains(second)) || (!tableItems.contains(first) & tableItems.contains(second))) {
                if((first instanceof Constant) ^ (second instanceof Constant)) {
                    //add condition to selection list if only one variable is from table and the other is constant
                    addCond(selectConditions, table, comparisonAtom);
                } else {
                    //add condition to join list if one variable is from table and the other is from another
                    addCond(joinConditions, table, comparisonAtom);
                }
                continue;
            }
            addCond(selectConditions, table, comparisonAtom);
            }
        }

    /**
     * Checks if a list of comparison atoms exists for a given table, if it does add the comparison atom to that list
     * otherwise create a list and create a mapping from that table to the new list
     * @param tableToConditions map of each table name to the list of comparison atoms associated with that table
     * @param name table's name
     * @param comparisonAtom ComparisonAtom to add to the mapping
     */
    private static void addCond(Map<String, List<ComparisonAtom>> tableToConditions, String name, ComparisonAtom comparisonAtom) {
        if(tableToConditions.containsKey(name)) {
            List<ComparisonAtom> exps = tableToConditions.get(name);
            exps.add(comparisonAtom);
            tableToConditions.replace(name, exps);
        }
        else {
            List<ComparisonAtom> exps = new ArrayList<ComparisonAtom>();
            exps.add(comparisonAtom);
            tableToConditions.put(name, exps);
        }
    }

    /**
     * Creates a schema for the given table and terms, for each term, if the term is a variable add it to the table
     * schema, if it is a constant then create a variable representing the constant and add it to the schema
     * @param tableName name of table
     * @param termList list of terms
     * @return list of strings representing schema for table
     */
    private static List<String> createTableSchema(String tableName, List<Term> termList) {
        List<String> newSchema = new ArrayList<String>();
        selectItems.put(tableName, termList); //map table name to terms
        schema.add(tableName); //add table to list of tables involved in the query
        for (int i=0;i<termList.size();i++) {
            Term t = termList.get(i);
            if(t instanceof Variable) {//if Term is a variable, add its name to the table's schema
                String varName = ((Variable) t).getName();
                String name = tableName + "." + varName;
                newSchema.add(name);
                attributePositions.put(name, i);
                if(!allVariables.contains(t))
                    allVariables.add(t);
            }
            else { //if term is a Constant, construct variable for it and add it to the table's schema
                String name = "";
                Variable t1;
                if(t instanceof IntegerConstant) {
                    name = tableName + ".ci" + ((IntegerConstant) t).getValue();
                    attributePositions.put(name, i);
                    t1 = new Variable("ci" + ((IntegerConstant) t).getValue());
                }
                else {
                    name = tableName + ".cs" + ((StringConstant) t).getValue();
                    attributePositions.put(name, i);
                    t1 = new Variable("cs" + ((StringConstant) t).getValue());
                }
                newSchema.add(name);
                //create condition that values on this column are equal to Constant
                ComparisonAtom c = new ComparisonAtom(t1, t, ComparisonOperator.fromString("="));
                addCond(selectConditions, tableName, c);
            }
        }
        return newSchema;
    }


    /**
     * Constructs join conditions between two tables
     * @param firstConditions First tables join conditions
     * @param secondConditions Second tables conditions
     * @return list of join conditions between the two tables
     */
    private List<ComparisonAtom> getTablesJoinConditions(List<ComparisonAtom> firstConditions, List<ComparisonAtom> secondConditions) {
        List<ComparisonAtom> conditions = new ArrayList<ComparisonAtom>();
        if(firstConditions.size()==0 || secondConditions.size()==0) {
            return conditions;
        }
        for(ComparisonAtom firstCondition : firstConditions) {
            for(ComparisonAtom secondCondition : secondConditions) {
                if (firstCondition.equals(secondCondition)) {
                    conditions.add(firstCondition);
                    break;
                }
            }
        }
        return conditions;
    }

    /**
     * Constructs list of join conditions for table 2 that imply other tables than table 1
     * @param t1 list of join conditions for table 1
     * @param t2 list of join conditions for table 2
     * @return list of join conditions for table 2 that imply other tables than table 1
     */
    private List<ComparisonAtom> getRestOfJoinConds(List<ComparisonAtom> t1, List<ComparisonAtom> t2) {
        List<ComparisonAtom> joinConds = new ArrayList<ComparisonAtom>();
        if(t2.size()==0) return joinConds;
        for(ComparisonAtom cond2 : t2) {
            boolean ok = false;
            if(t1.size()==0) return t2;
            for(ComparisonAtom cond1 : t1) {
                if (cond2.equals(cond1)) {
                    ok = true;
                    break;
                }
            }
            if(!ok) joinConds.add(cond2);
        }
        return joinConds;
    }

    /**
     * Generates the operator tree and executes the query
     */
    public void generateAndExecuteQueryPlan() {
        // Create the root scan operator for the first table
        Operator root = new ScanOperator(schema.get(0));

        // Get the select conditions, if there are any then create a select operator with the scan operator as root
        List<ComparisonAtom> whereSelect = selectConditions.getOrDefault(schema.get(0), new ArrayList<>());
        if (whereSelect.size()>0) {
            root = new SelectOperator((ScanOperator) root, whereSelect);
        }

        //get join conditions for the first table
        List<ComparisonAtom> allJoinConds = joinConditions.getOrDefault(schema.get(0), new ArrayList<>());
        //get first tables schema
        List<String> table1Attr = new ArrayList<>(DatabaseCatalogue.getSchemaList(schema.get(0)));
        //For the rest of the tables, create scan operators then if there are select conditions for that table create
        //... select operators with the scan opperator as the root
        for (int i = 1; i < schema.size(); ++i) {
            String currentTable = schema.get(i);
            Operator root2 = new ScanOperator(currentTable);
            List<ComparisonAtom> whereSelect2 = selectConditions.getOrDefault(schema.get(i), new ArrayList<>());
            if (whereSelect2.size() > 0) {
                root2 = new SelectOperator((ScanOperator) root2, whereSelect2);
            }

            // Get current tables join conditions, then get the join conditions for the first table and this table
            List<ComparisonAtom> tableJoinConds = joinConditions.getOrDefault(schema.get(i), new ArrayList<>());
            List<ComparisonAtom> necessaryJoinConds = getTablesJoinConditions(allJoinConds, tableJoinConds);
            // Get schema for current table
            List<String> table2Attr = new ArrayList<>(DatabaseCatalogue.getSchemaList(currentTable));
            Map<String, Integer> attrPos1 = new HashMap<>();
            List<String> commonVars = new ArrayList<>();
            //check for common variables between the 2 tables
            for(int k=0;k<table2Attr.size();k++) {
                String[] splitLine = table2Attr.get(k).split("\\.");
                attrPos1.put(splitLine[1], k);
            }
            for (String string : table1Attr) {
                String[] splitLine = string.split("\\.");
                if (attrPos1.containsKey(splitLine[1])) {
                    commonVars.add(splitLine[1]);
                    table2Attr.remove(splitLine[1]);
                }
            }

            // Remove common join conditions, then add remaining join conditions before doing the join from first table
            //... to this table
            allJoinConds = getRestOfJoinConds(necessaryJoinConds, allJoinConds);
            allJoinConds.addAll(getRestOfJoinConds(necessaryJoinConds, tableJoinConds));
            root = new JoinOperator(root, root2, necessaryJoinConds, commonVars); //create a join operator for the first/last operator and the new one
            table1Attr.addAll(table2Attr);
        }

        // Check whether projection will be required
        boolean projectionNeeded = false;
        if(projectTerms.size()!= allVariables.size()) {
            projectionNeeded = true;
        } else {
            for(int i = 0; i< projectTerms.size(); i++)
                if (!projectTerms.get(i).equals(allVariables.get(i))) {
                    projectionNeeded = true;
                    break;
                }
        }
        // If projection needed, form procetion operator and output only the unique results
        if(projectionNeeded) {
            root = new ProjectOperator(projectTerms, root, schema.get(0));
            List<Tuple> t = root.getQueryResult();
            root = new DistinctOperator(t);
            root.dump(((DistinctOperator) root).getDistinctTuples());
        }
        else {
            //Output all results
            root.dump();
        }
    }

}