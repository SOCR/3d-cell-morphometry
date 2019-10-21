import java.io.*;
import java.util.Date;

/**
 * Logs messages to IJ.log() and a file in the current image folder.
 * @author dilworth
 */
public class DiagLogger {
    /** Underlying Log file */
   //File LogFile;
    /** Stream that writes to the file */
    FileWriter LogStream=null;

    /** 
     *
     */
    public DiagLogger()     {
        //LogFile=null;
        LogStream=null;
    }

    /**
     * Closes old (if opne) and opens new log file
     * (previously this class appended to log file)
     * Prints a delimiter lines with date/time and version
     * @param pathfilename full path and file to log file
     * @param version version number for plugin 
     * @return true if file opened  */
    public boolean Append(String pathfilename,String version) {
        try {
            if(LogStream!=null)
                LogStream.close();
        } catch(IOException e) { }
            
        // LogFile doesn't have a close() method, or equiv?
        try {
            //LogFile = new File(pathfilename);
            LogStream = new FileWriter(pathfilename,false); // overwrite old log
            LogStream.write("\n");
            LogStream.write("= - = - = - = - = - = - ="+"\n");
            LogStream.write("\n");
            Date date = new Date();
            LogStream.write(date.toString()+", "+version+"\n");
        } catch(IOException e) { 
            return false;
        }
        return true;
    }

    /**
     * Prints message to IJ.log and current log file
     * @param message the text to log
     */
    public void Log(String message) {
        //IJ.log(message);
        System.out.println(message);
        if(LogStream!=null) {
            try {
                LogStream.write(message+"\n");
                LogStream.flush();
            } catch(IOException e) { }
        }
    }
}
