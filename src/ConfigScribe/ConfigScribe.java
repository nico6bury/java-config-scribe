package ConfigScribe;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import SimpleResult.SimpleResult;

/**
 * This class is used for serializing and deserializing config settings.
 * 
 * How to add new entries to the config file?
 * TODO: 
 * 
 */
public class ConfigScribe<T extends ConfigStore> {
    /**
     * A little helper field to get around type erasure and figure out the class
     * of T at runtime.
     */
    private final Class<T> typeT;

    /** Constructs the class */
    @SuppressWarnings("unchecked")
    public ConfigScribe() {
        /*
         * The nice way of saving the type of T was found at:
         * https://nautsch.net/2008/10/28/class-von-type-parameter-java-generics/
         */
        this.typeT = (Class<T>)
                ((ParameterizedType)getClass()
                .getGenericSuperclass())
                .getActualTypeArguments()[0];
    }//end constructor

    /**
     * This method writes information to a specified config file.
     * This method is written in such a way as to keep any lines in an existing config
     * file that don't contain serialized information, so comments on separate lines are allowed.
     * @param store A config store to be written to the file.
     * @return Returns either a meaningless string, or an exception if something stopped execution from finishing.
     */
    public SimpleResult<String> writeConfig(T store) {
        String jarLocation;
        try {
            // figure out path to write file to
            jarLocation = new File(this.getClass().getProtectionDomain()
                .getCodeSource().getLocation().toURI()).getParentFile().toString();
            File configFilepath = new File(jarLocation + File.separator + store.getConfigFilename());
            // make sure file exists
            boolean addHeaderToConfig = false;
            if (!configFilepath.exists()) {configFilepath.createNewFile(); addHeaderToConfig = true;}
            // get all the lines from the config file to work with later
            List<String> configLines = Files.readAllLines(configFilepath.toPath());
            // if we're creating a new config file, add the header
            if (addHeaderToConfig) {/* TODO: Add header functionallity */}
            // get list of fields to use for looking stuff up in match map
            Field[] fields = typeT.getClass().getFields();
            // find the lines at which things are written in existing config, if found at all
            HashMap<String,Integer> matchMap = matchConfigLines(configLines, fields);
            // update lines in config file with values from parameters
            for (int i = 0; i < fields.length; i++) {
                // get the formatting figured out beforehand since it will be the same
                String fLine = fields[i].getName() + " = " + fields[i].get(store);
                // check whether or not current field is already recorded in file
                if (matchMap.containsKey(fields[i].getName())) {
                    // rewrite the line at index in matchMap[fields[i].getName()]
                    int index = matchMap.get(fields[i].getName());
                    configLines.set(index, fLine);
                }//end if we can rewrite the corresponding line
                else {
                    // TODO: Add customized comments for each potential field
                    // add a new line for fields[i]
                    configLines.add(fLine);
                    // add extra line for spacing
                    configLines.add("");
                }//end else we'll have to add a new line for this field
            }//end looping over fields, matching, and writing
            // clear files of text
            new FileWriter(configFilepath, false).close();
            // write changes to files
            Files.write(configFilepath.toPath(), configLines);
        } catch (Exception e) { return new SimpleResult<String>(e); }

        return new SimpleResult<String>("No Exceptions Encountered.");
    }//end writeConfig()

    /**
     * This method reads from a specified config file and creates a ConfigStore
     * based on the data parsed.
     * This method is written in such a way as to ignore lines that don't contain
     * serialized information.
     * @param filename The filename of the config file you want to read from.
     * @return Either a ConfigStore, or an exception that prevented the method from finishing.
     */
    public SimpleResult<T> readConfig(String filename) {
        String jarLocation;
        try {
            // figure out path to write file to
            jarLocation = new File(this.getClass().getProtectionDomain()
                .getCodeSource().getLocation().toURI()).getParentFile().toString();
            File configFilepath = new File(jarLocation + File.separator + filename);
            // get all the lines from the config file
            List<String> configLines;
            if (!configFilepath.exists()) { configLines = new ArrayList<String>(); }
            else { configLines = Files.readAllLines(configFilepath.toPath()); }
            // get list of fields to use for looking stuff up in match map
            Field[] fields = typeT.getClass().getFields();
            // find the lines at which things are written in existing config, if found at all
            HashMap<String,Integer> matchMap = matchConfigLines(configLines, fields);
            //create store objects to read information into
            T store = typeT.getDeclaredConstructor().newInstance();
            // read data from config file into store
            for (int i = 0; i < fields.length; i++) {
                // check whether or not current field is recorded in file
                if (matchMap.containsKey(fields[i].getName())) {
                    // read line at index in matchMap[fields[i].getName()]
                    int index = matchMap.get(fields[i].getName());
                    String thisLine = configLines.get(index); // name = value
                    String[] splitLine = thisLine.split(" = ");
                    if (splitLine.length == 2) {
                        // try and parse depending on type of field
                        if (fields[i].getType() == int.class) {
                            int val = Integer.parseInt(splitLine[1]);
                            fields[i].setInt(store, val);
                        }//end if it's an integer
                        if (fields[i].getType() == double.class) {
                            double val = Double.parseDouble(splitLine[1]);
                            fields[i].setDouble(store, val);
                        }//end if it's a double
                        if (fields[i].getType() == boolean.class) {
                            boolean val = Boolean.parseBoolean(splitLine[1]);
                            fields[i].setBoolean(store, val);
                        }//end if it's a boolean
                        if (fields[i].getType() == String.class) {
                            String val = splitLine[1];
                            fields[i].set(store, val);
                        }//end if it's a string
                    }//end if split line has expected length
                    else {
                        if (fields[i].getType() == String.class) {
                            String val = "";
                            fields[i].set(store, val);
                        }//end if value was an empty string
                        else {
                            System.err.println("Something went wrong with the config at line \"" + thisLine + "\", and I don't know how to parse it as " + fields[i].getName() + ".");
                        }//end else it actually was an error
                    }//end else we might not be able to parse it
                }//end if we found a line for this value
            }//end looping over fields, matching, and reading
            // return result wrapped around the config store we read
            return new SimpleResult<>(store);
        } catch (Exception e) { return new SimpleResult<T>(e); }
    }//end readConfig()

    /**
     * This helper method finds the index in the provided lines at which each property of ConfigStore starts a line.
     * @param lines The lines of text from a config file.
     * @param fields A list of the fields we're looking to find in the config file.
     * @return Returns a hashmap, with keys being strings denoting the names of properties of ConfigStore, and values being the index they're found at
     */
    protected HashMap<String,Integer> matchConfigLines(List<String> lines, Field[] fields) {
        HashMap<String,Integer> matchMap = new HashMap<>();

        // get list of the names of properties of config store
        for (int i = 0; i < fields.length; i++) {
            String thisFieldName = fields[i].getName();
            for (int j = 0; j < lines.size(); j++) {
                String thisLine = lines.get(i);
                String thisTrimmedLine = thisLine.substring(0, Math.min(thisLine.length(), thisFieldName.length()));
                if (thisTrimmedLine.equalsIgnoreCase(thisFieldName)) {
                    matchMap.put(thisFieldName, j);
                    break;
                }//end if we found a match
            }//end looping over each line
        }//end looping over each field

        return matchMap;
    }//end matchConfigLines()
}//end class ConfigScribe
