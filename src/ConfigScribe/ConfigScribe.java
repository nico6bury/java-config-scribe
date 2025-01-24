package ConfigScribe;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
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
public class ConfigScribe {

    /** Constructs the class */
    public ConfigScribe() { }

    /**
     * This method writes information to a specified config file.<p>
     * This method is written in such a way as to keep any lines in an existing config
     * file that don't contain serialized information, so comments on separate lines are allowed.
     * @param <T> This is the type of the config class, which needs to implement ConfigStore.
     * @param store A config store to be written to the file.
     * @return Returns either a meaningless string, or an exception if something stopped execution from finishing.
     */
    public static <T extends ConfigStore> SimpleResult<String> writeConfig(T store) {
        String jarLocation;
        try {
            // figure out path to write file to
            jarLocation = new File(ConfigStore.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI()).getParentFile().toString();
            File configFilepath = new File(jarLocation + File.separator + store.getConfigFilename());
            // make sure file exists
            boolean addHeaderToConfig = false;
            if (!configFilepath.exists()) {configFilepath.createNewFile(); addHeaderToConfig = true;}
            // get all the lines from the config file to work with later
            List<String> configLines = Files.readAllLines(configFilepath.toPath());
            // if we're creating a new config file, add the header
            if (addHeaderToConfig && store.getConfigHeader() != null) {
                for (String headerLine : store.getConfigHeader()) {
                    configLines.add("# " + headerLine);
                }//end adding each header line to config file lines
                configLines.add("");
            }//end if we should and can add a header to the top of the config file
            // get list of fields to use for looking stuff up in match map
            Field[] fields = store.getClass().getFields();
            // find the lines at which things are written in existing config, if found at all
            HashMap<String,int[]> matchMap = matchConfigLines(configLines, fields, store);
            // update lines in config file with values from parameters
            for (int i = 0; i < fields.length; i++) {
                // skip comment and name fields in main loop
                if (fields[i].getName().endsWith("COMMENT") || fields[i].getName().endsWith("NAME")) {continue;}
                // get name of field, accounting for name changes
                String fName = checkFieldName(fields[i], fields, store);
                // get the formatting figured out beforehand since it will be the same
                String fLine = fName + " = " + fields[i].get(store);
                // check whether or not current field is already recorded in file
                if (matchMap.containsKey(fName)) {
                    // rewrite the line at index in matchMap[fields[i].getName()]
                    int index = matchMap.get(fName)[0];
                    configLines.set(index, fLine);
                }//end if we can rewrite the corresponding line
                else {
                    // Add customized comments for each potential field with one configured
                    String fieldComment = checkFieldComment(fields[i], fields, store);
                    if (fieldComment != null) {
                        configLines.add("# " + fieldComment);
                    }//end if there is in fact a field comment
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
     * based on the data parsed.<p>
     * This method is written in such a way as to ignore lines that don't contain
     * serialized information.
     * @param <T> This is the type of the config class, which needs to implement ConfigStore.
     * @param potentialStore An out parameter that holds the object which will be written to after reading the file.<p>
     * If this is null, then the method will return early, returning a NullPointerException explaining the issue.
     * @return Either a ConfigStore, or an exception that prevented the method from finishing.
     */
    public static <T extends ConfigStore> SimpleResult<String> readConfig(T potentialStore) {
        // exit early if potentialStore is null
        if (potentialStore == null)
        { return new SimpleResult<String>(new NullPointerException("The generic parameter potentialStore cannot be null for this work.")); }
        
        String jarLocation;
        try {
            // figure out path to write file to
            jarLocation = new File(ConfigScribe.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI()).getParentFile().toString();
            File configFilepath = new File(jarLocation + File.separator + potentialStore.getConfigFilename());
            // get all the lines from the config file
            List<String> configLines;
            if (!configFilepath.exists()) { configLines = new ArrayList<String>(); }
            else { configLines = Files.readAllLines(configFilepath.toPath()); }
            // get list of fields to use for looking stuff up in match map
            Field[] fields = potentialStore.getClass().getFields();
            // find the lines at which things are written in existing config, if found at all
            HashMap<String,int[]> matchMap = matchConfigLines(configLines, fields, potentialStore);
            // read data from config file into potentialStore
            for (int i = 0; i < fields.length; i++) {
                // skip comment and name fields in main loop
                if (fields[i].getName().endsWith("COMMENT") || fields[i].getName().endsWith("NAME")) {continue;}
                // get configured name of field
                String fName = checkFieldName(fields[i], fields, potentialStore);
                // check whether or not current field is recorded in file
                if (matchMap.containsKey(fName)) {
                    // read line at index in matchMap[fields[i].getName()]
                    int index = matchMap.get(fName)[0];
                    String thisLine = configLines.get(index); // name = value
                    String[] splitLine = thisLine.split(" = ");
                    if (splitLine.length == 2) {
                        // try and parse depending on type of field
                        if (fields[i].getType() == int.class) {
                            int val = Integer.parseInt(splitLine[1]);
                            fields[i].setInt(potentialStore, val);
                        }//end if it's an integer
                        if (fields[i].getType() == double.class) {
                            double val = Double.parseDouble(splitLine[1]);
                            fields[i].setDouble(potentialStore, val);
                        }//end if it's a double
                        if (fields[i].getType() == boolean.class) {
                            boolean val = Boolean.parseBoolean(splitLine[1]);
                            fields[i].setBoolean(potentialStore, val);
                        }//end if it's a boolean
                        if (fields[i].getType() == String.class) {
                            String val = splitLine[1];
                            fields[i].set(potentialStore, val);
                        }//end if it's a string
                    }//end if split line has expected length
                    else {
                        if (fields[i].getType() == String.class) {
                            String val = "";
                            fields[i].set(potentialStore, val);
                        }//end if value was an empty string
                        else {
                            System.err.println("Something went wrong with the config at line \"" + thisLine + "\", and I don't know how to parse it as " + fields[i].getName() + ".");
                        }//end else it actually was an error
                    }//end else we might not be able to parse it
                }//end if we found a line for this value
            }//end looping over fields, matching, and reading
            // return result wrapped around the config store we read
            return new SimpleResult<String>("Things seem to have been a success.");
        } catch (Exception e) { return new SimpleResult<String>(e); }
    }//end readConfig()

    /**
     * This helper method finds the index in the provided lines at which each property of ConfigStore starts a line.
     * @param <T> type parameter for whatever object we're applying things to
     * @param lines The lines of text from a config file.
     * @param fields A list of the fields we're looking to find in the config file.
     * @param exampleObj The object whose fields we're looking through. To be used in case of name specification.
     * @return Returns a hashmap, with keys being strings denoting the names of properties of ConfigStore, and values being the index they're found at
     */
    protected static <T extends ConfigStore> HashMap<String,int[]> matchConfigLines(List<String> lines, Field[] fields, T exampleObj) {
        HashMap<String,int[]> matchMap = new HashMap<>();

        // get list of the names of properties of config store
        for (int i = 0; i < fields.length; i++) {
            // if this field simply serves to specify the name of another field, skip it
            if (fields[i].getName().endsWith("NAME")) {continue;}
            // make sure we're looking for the right name for this field
            String thisFieldName = checkFieldName(fields[i], fields, exampleObj);
            boolean isCommentField = fields[i].getName().endsWith("COMMENT");
            for (int j = 0; j < lines.size(); j++) {
                String thisLine = lines.get(j);
                // change how we find the index/indices depending on whether we're looking for a value or a comment
                if (!isCommentField) {
                    String thisTrimmedLine = thisLine.substring(0, Math.min(thisLine.length(), thisFieldName.length()));
                    if (thisTrimmedLine.equals(thisFieldName)) {
                        matchMap.put(thisFieldName, new int[] {j});
                        break;
                    }//end if we found a match
                }//end if we're just looking for a value
                else {
                    // trim line to length of this field name - comment
                    String thisTrimmedLine = thisLine.substring(0,Math.min(thisLine.length(), thisFieldName.length() - 7));
                    // if we haven't yet navigated to the line with info matching field name, keep iterating through lines
                    if (!thisTrimmedLine.equals(thisFieldName)) {continue;}
                    // if the line above us doesn't contain a comment, then we
                    if (j <= 0 || !lines.get(j - 1).startsWith("#")) {continue;}
                    int highestIdx = j - 1;
                    for (int k = j; k > 0; k--) {
                        String thisThisLine = lines.get(k);
                        if (thisThisLine.startsWith("#")) {highestIdx = k;}
                        else {break;}
                    }//end figuring out how many lines the comment covers
                    // create and fill array with line index of each line that is a comment to the field
                    int[] commentLineIndices = new int[j - highestIdx];
                    for (int l = highestIdx; l <= j; l++) {commentLineIndices[l-highestIdx] = l;}
                    // update match map now that we've figure out what we need
                    matchMap.put(thisFieldName, commentLineIndices);
                }//end else we're looking for the location of a comment
            }//end looping over each line
        }//end looping over each field

        return matchMap;
    }//end matchConfigLines()

    /**
     * Helper function intended for matchConfigLines.<p>
     * Checks a given field against an array of fields.
     * If one of the fields' names is the given field's name plus "NAME",
     * then the value of that field in exampleObj is returned instead. <p>
     * If an IllegalArgumentException or IllegalAccessException occurs when
     * attempting to get the name from the value of that field in exampleObj,
     * then this method will simply return the original name and print the
     * offending exception to System.err.
     * @param <T> Generic type parameter due to upstream generic-ness.
     * @param field The field whose name is in question.
     * @param fields An array of all the fields of exampleObj, which will be searched to determine return.
     * @param exampleObj An object of the type which generated fields.
     * @return Returns a string which can be treated as the name of the given field.
     */
    protected static <T> String checkFieldName(Field field, Field[] fields, T exampleObj) {
        String defaultName = field.getName();
        for (Field thisField : fields) {
            String thisFieldName = thisField.getName();
            if (thisFieldName.equals(defaultName + "NAME")) {
                try {
                    return thisField.get(exampleObj).toString();
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    System.err.println(e.getCause() + "\n" + e.getMessage() + "\n" + e.getStackTrace());
                    break;
                }//end catching any issues that return
            }//end if we found one to use for name
        }//end checking each field for one that applies to the given
        return defaultName;
    }//end checkFieldName()

    /**
     * Helper function intended for matchConfigLines.<p>
     * Checks a given field against an array of fields.
     * If one of the fields' names is the given field's name plus "COMMENT",
     * then the value of that field in exampleObj is returned instead. <p>
     * If an IllegalArgumentException or IllegalAccessException occurs when
     * attempting to get the name from the value of that field in exampleObj,
     * then this method will simply return null and print the
     * offending exception to System.err.
     * @param <T> Generic type parameter due to upstream generic-ness.
     * @param field The field whose name is in question.
     * @param fields An array of all the fields of exampleObj, which will be searched to determine return.
     * @param exampleObj An object of the type which generated fields.
     * @return Returns either a comment for the given field or null.
     */
    protected static <T> String checkFieldComment(Field field, Field[] fields, T exampleObj) {
        String defaultFieldName = field.getName();
        for (Field thisField : fields) {
            String thisFieldName = thisField.getName();
            if (thisFieldName.equals(defaultFieldName + "COMMENT")) {
                try {
                    return thisField.get(exampleObj).toString();
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    System.err.println(e.getCause() + "\n" + e.getMessage() + "\n" + e.getStackTrace());
                    break;
                }//end catching any issues that return
            }//end if we found one to use for comment
        }//end checking each field for one that applies to the given
        return null;
    }//end checkFieldComment()
}//end class ConfigScribe
