package Examples;

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
    public String lastOutputPath = "";
    /**
     * 
     */
    public int timesOpened = 0;
}//end class FileIOStore
