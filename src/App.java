import ConfigScribe.ConfigScribe;
import Examples.FileIOStore;
import Examples.SettingStore;
import SimpleResult.SimpleResult;

/**
 * 
 */
public class App {
    public static void main(String[] args) throws Exception {
        FileIOStore conf1 = new FileIOStore();
        SettingStore conf2 = new SettingStore();

        SimpleResult<String> readRes1 = ConfigScribe.readConfig(conf1);
        SimpleResult<String> readRes2 = ConfigScribe.readConfig(conf2);

        conf1.timesOpened ++;

        SimpleResult<String> writeRes1 = ConfigScribe.writeConfig(conf1);
        SimpleResult<String> writeRes2 = ConfigScribe.writeConfig(conf2);

    }//end main method
}//end class App
