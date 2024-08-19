package ed.inf.adbs.minibase;

import ed.inf.adbs.minibase.base.*;
import ed.inf.adbs.minibase.base.operator.SelectStatement;
import ed.inf.adbs.minibase.parser.QueryParser;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

/**
 * Minibase class takes query, tables and output file and writes the result of the query to ouptput file
 *
 */
public class Minibase {

    /**
     * Main function for Minibase, reads in arguments, parses query, initialises DatabaseCatalogue, then calls
     * evaluateCQ
     * @param args database directory, input file and output file
     */
    public static void main(String[] args) {

        // Check correct number of arguments
        if (args.length != 3) {
            System.err.println("Usage: Minibase database_dir input_file output_file");
            return;
        }

        // Store arguments locally
        String dbDir = args[0];
        String inFile = args[1];
        String outFile = args[2];
        Query query;
        try {
            query = QueryParser.parse(Paths.get(inFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        DatabaseCatalogue catalogue = DatabaseCatalogue.getInstance();
        catalogue.setValues(dbDir, outFile);

        evaluateCQ(query);
    }

    /**
     * Given a query creates a SelectStatement instance then calls generateAndExecuteQueryPlan function
     * @param query query parsed from input file
     */
    public static void evaluateCQ(Query query){
            if (query != null){
                SelectStatement selectStatement = new SelectStatement(query);
                selectStatement.generateAndExecuteQueryPlan();
            }
    }

}
