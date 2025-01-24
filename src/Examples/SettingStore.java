package Examples;

import java.util.ArrayList;
import java.util.List;

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
    @Override
    public List<String> getConfigHeader() {
        List<String> header = new ArrayList<>();
        header.add("This file contains settings that a user might wish to edit.");
        header.add("You can freely edit comments (lines starting with #) and values as you wish.");
        header.add("For each value declaration, make sure to keep a space on either side of the equals sign.");
        return header;
    }//end getConfigHeader()
    
    public double unsharpSigma = 1.5;
    public String unsharpSigmaCOMMENT = "The sigma value to use when performing an unsharp mask.";
    public boolean unsharpSkip = true;
    public String unsharpSkipCOMMENT = "If this is true, then the unsharp mask operation will be not be performed.";
    public int dpi = 600;
    public String suffixCOMMENT = "This suffix is added to each created image file.";
    public String suffix = "-test";
    public String suffixNAME = "image_file_suffix";
}//end class SettingStore
