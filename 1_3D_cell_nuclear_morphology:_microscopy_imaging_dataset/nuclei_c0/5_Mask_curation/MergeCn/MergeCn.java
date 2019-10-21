
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author beagles
 */
public class MergeCn {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
       char fileSepChar = File.separatorChar;
       String folderFile = args[0];
             
        // path to image folder without trailing slash
       int lastSlashIndex = folderFile.lastIndexOf(fileSepChar);
       String folder=folderFile.substring(0,lastSlashIndex);       
       String mask = args[1];
       MergeFiles merge = new MergeFiles(folderFile, folder ,mask);
    }    
}
