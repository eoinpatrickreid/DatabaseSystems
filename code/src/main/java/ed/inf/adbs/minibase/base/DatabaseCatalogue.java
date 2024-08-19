package ed.inf.adbs.minibase.base;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The DatabaseCatalogue keeps track of file locations and schemas for different relations, as well as attribute positions
 * aliases etc
 */
public class DatabaseCatalogue {
    private static String schemaPath ="";
    private static String csvFilesPath = "";
    private static String outputPath = "";
    private static Map<String, Integer> attributePosition;
    private static Map<String, List<String>> schemaList;
    private static Map<String, String> aliases;
    private static final Map<String, List<String>> types = new HashMap<>();
    private static DatabaseCatalogue instance = null;
    private DatabaseCatalogue(){}

    public static DatabaseCatalogue getInstance(){
        if (instance != null) {
            return instance;
        }else {
            instance = new DatabaseCatalogue();
        }
        return instance;
    }

    /**
     * Given the database path and the output file path instantiate all of the required variables in DatabaseCatalogue
     * @param databasePath database file path
     * @param outputFilePath output file oath
     */
    public void setValues(String databasePath, String outputFilePath){
        csvFilesPath = databasePath + "/files";
        schemaPath = databasePath + "/schema.txt";
        outputPath = outputFilePath;
        aliases = new HashMap<>();
        attributePosition = new HashMap<>();
        schemaList = new HashMap<>();
        String buffer;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(schemaPath));
            while ((buffer = bufferedReader.readLine()) != null){
                List<String> entries = Arrays.asList(buffer.split("\\s+"));
                List<String> entriesTrimmed = entries.stream().filter(Objects::nonNull).map(String::trim).collect(Collectors.toList());
                types.put(entriesTrimmed.get(0), entriesTrimmed.subList(1, entriesTrimmed.size()));
            }
            File outputFile = new File(outputPath);
            outputFile.getParentFile().mkdirs();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputPath));
            bufferedWriter.write("");
            bufferedWriter.close();
            bufferedReader.close();
        } catch (IOException e) {
            System.out.println("Error when instantiating DBCatalogue");
            e.printStackTrace();
        }
    }

    public static String getCsvPath(String name) {
        return (csvFilesPath + "/"  + name + ".csv");
    }
    public static void setAlias(String aliasTable, String origTable){
        aliases.put(aliasTable, origTable);
    }

    public static List<String> getInitialTypes(String t){
        return types.get(t);
    }

    public static String getAlias(String table){
        if(!aliases.containsKey(table)) {
            return null;
        }
        return aliases.get(table);
    }

    public static String getOutputPath() {
        return outputPath;
    }

    public static int getAttrPos(String attr){
        return attributePosition.get(attr);
    }

    public static void setAttributePosition(Map<String, Integer> attributePosition){
        DatabaseCatalogue.attributePosition = attributePosition;
    }

    public static void setSchemaList(String name, List<String> types){
        schemaList.put(name,types);
    }

    public static List<String> getSchemaList(String t){
        return schemaList.get(t);
    }


}
