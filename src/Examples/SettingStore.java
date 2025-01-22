package Examples;

import ConfigScribe.ConfigStore;

/**
 * 
 */
public class SettingStore implements ConfigStore {

    /**
     * 
     */
    @Override
    public String getConfigFilename() {
        return "settings.conf";
    }//end getConfigFilename()
    
    /**
     * 
     */
    public double unsharpSigma = 1.5;
    /**
     * 
     */
    public boolean unsharpSkip = true;
    /**
     * 
     */
    public int dpi = 600;
    /**
     * 
     */
    public String suffix = "-test";
}//end class SettingStore
