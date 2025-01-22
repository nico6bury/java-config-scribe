package ConfigScribe;

/**
 * This interface is implemented by classes with public fields in order for those classes to be written to a config file.
 */
public interface ConfigStore {
    /**
     * @return Returns the name of the config file that holds this information. This should include the extension, but not the path.
     */
    public String getConfigFilename();
}//end interface ConfigStoreH
