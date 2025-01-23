package Examples;

import java.util.ArrayList;
import java.util.List;

import ConfigScribe.ConfigStore;

/**
 * 
 */
public class FileIOStore implements ConfigStore {

    /**
     * 
     */
    @Override
    public String getConfigFilename() {
        return ".config";
    }//end getConfigFilename()
    /**
     * 
     */
    @Override
    public List<String> getConfigHeader() {
        List<String> header = new ArrayList<>();
        header.add("This file contains settings meant to be used by the program.");
        header.add("Please do not manually edit anything in this file.");
        return header;
    }//end getConfigHeader()
    
    public String lastOutputPath = "";
    public int timesOpened = 0;
}//end class FileIOStore
