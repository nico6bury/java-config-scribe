package ConfigScribe;

import java.lang.reflect.Field;
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
    /** Constructs the class */
    public ConfigScribe() {}

    /**
     * This method writes information to a specified config file.
     * This method is written in such a way as to keep any lines in an existing config
     * file that don't contain serialized information, so comments on separate lines are allowed.
     * @param store A config store to be written to the file.
     * @return Returns either a meaningless string, or an exception if something stopped execution from finishing.
     */
    public SimpleResult<String> writeConfig(T store) {
        // TODO
        return new SimpleResult<String>(new Exception("Not Yet Implemented"));
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
        // TODO
        return new SimpleResult<T>(new Exception("Not Yet Implemented"));
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
