package ConfigScribe;

import java.io.File;
import java.util.List;

/**
 * This interface is implemented by classes with public fields in order for those classes to be written to a config file.
 */
public interface ConfigStore {
    /**
     * @return Returns the name of the config file that holds this information. This should include the extension, but not the path.<p>
     * If this returns null, it will cause issues.
     */
    public String getConfigFilename();

    /**
     * @return Returns the header to put at the top of the config file whenever it is regenerated. <p>
     * Each element of the list will be trated as a separate line. <p>
     * If you do not wish to include a header, simply return null.
     */
    public List<String> getConfigHeader();

    /**
     * Set this file to the directory you want the config file to be
     * written and read from. <p>
     * This also needs to be a directory, as tested by
     * File.isDirectory(). If it doesn't exist, it will be created.<p>
     * Alternatively, if you wish to just save the file in the default location,
     * you can just leave this as null.
     */
    public File getDirectoryLocation();
}//end interface ConfigStoreH
