# Java Config Scribe

I created this repository in order to have one place for the config file system I tend to recreate in a number of java projects.

Here's a disordered list of details about how to use the thing

- For each configuration file, just create a class which:
  - contains public fields of supported types
    - as of writing, fields of types String, int, double, and boolean are supported
  - implements the ConfigStore interface
- This project uses reflection to look at each config class. Thus:
  - Any field whose name ends with "COMMENT" is treated as the default comment for another field
    - So, if you had a field named suffix, you could add a custom comment for it in a field called suffixCOMMENT.
  - Any field whose name ends with "NAME" is treated as the name of another field within the config file
    - So, if you had a field named suffix and a field named suffixNAME, then the value of suffixNAME would be the actual name used for suffix in the config file.
- Each line that starts with # is treated as a comment in the config file.
- As of writing, the current version of ConfigStore will ignore anything in the config file it doesn't recognize, so the comment syntax is more for the user to determine what is or is not a comment.
- Under certain circumstances, it's possible that ConfigScribe will print Exception information to System.err. It shouldn't actually throw any exceptions to the outside, however.
- For each config file, you can choose both a name for the file and the directory where that file will be located.
- When you configure a custom header or comments for your config file, that information will only be written if ConfigScribe can't find an older config file and makes a new one.
- when implementing ConfigStore, you can get safely set getConfigHeader and getDirectoryLocation to always return null; the config scribe will simple use the defaults of not including a header and placing the config file in the same directory as the jar file. However, you must always set the name for getConfigFilename.
