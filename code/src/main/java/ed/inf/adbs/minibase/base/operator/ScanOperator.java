package ed.inf.adbs.minibase.base.operator;
import ed.inf.adbs.minibase.base.DatabaseCatalogue;
import ed.inf.adbs.minibase.base.IntegerConstant;
import ed.inf.adbs.minibase.base.StringConstant;
import ed.inf.adbs.minibase.base.Term;
import ed.inf.adbs.minibase.base.Tuple;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;




/**
 *
 * Operator that scans a table for data and saves the data in Tuple elements.
 *
 */
public class ScanOperator extends Operator {

    //table name to be scanned
    private String fileName;
    //original table
    private String originalFile;
    //path of the table's data file
    private String filePath;
    //buffer reader to read data from the tables
    private BufferedReader bufferedReader;

    /**
     * ScanOperator constructor  initialises the variables for the scan operator
     * @param name table name
     */
    public ScanOperator(String name) {
        try {
            this.fileName = name;
            this.originalFile = DatabaseCatalogue.getAlias(name);
            this.filePath = DatabaseCatalogue.getCsvPath(originalFile);
            bufferedReader = new BufferedReader(new FileReader(filePath));
        } catch (FileNotFoundException e) {
            System.out.println("Error when creating scan operator instance");
            e.printStackTrace();
        }
    }

    /**
     * Returns a tuple containing the next line in the given table, to do this we read the line and trim and split it
     * to give us the terms
     * @return tuple with the line's values
     */
    @Override
    public Tuple getNextTuple() {
        try {
            String buffer = bufferedReader.readLine();
            if (buffer !=null){
                // get the relevant variable types and schema for the table
                List<String> types = DatabaseCatalogue.getInitialTypes(originalFile);
                List<String> tableSchema = DatabaseCatalogue.getSchemaList(fileName);
                // Trim and split the line we have read in order to just get the terms as strings
                buffer = buffer.trim();
                String[] elements = buffer.split(",\\s+");
                // For each term, check the type it should be and then store the terms as its type in terms
                Term[] terms = new Term[elements.length];
                int i = 0;
                for (String t : types){
                    if (t.matches("string")){
                        terms[i] = new StringConstant(elements[i].replaceAll("'", ""));
                    } else if (t.matches("int")){
                        terms[i] = new IntegerConstant(Integer.valueOf(elements[i]));
                    }
                    i++;
                }
                // Create a new tuple with the terms we have collected and the schema
                return new Tuple(terms, tableSchema);
            } else {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Resets the reader
     */
    @Override
    public void reset() {
        try {
            bufferedReader.close();
            bufferedReader = new BufferedReader(new FileReader(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return table name
     * @return table name
     */
    public String getName() {
        return fileName;
    }
}
