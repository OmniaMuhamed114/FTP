package FTP;
/**
 * The Utils class implements a program that handle the inputs which taken from user.
 */
public class Utils {
    /**
     * This method takes name of file and checks if this an file or directory by getting its extension.
     * @param name This is the name of the file.
     * @return String This is the extension of the file.
     */
    public static String getFileExtension(String name) {
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return name.substring(lastIndexOf);
    }
}
