package ConfigScribe;

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
}//end interface ConfigStoreH
