
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author dilworth
 */
public class MergeFiles {
    
    /** List of mask.tif path/files to process */
    List<String> fileList;
    
    public MergeFiles(String mergefile, String folder, String mask) {
        
        BufferedWriter fileWrite=null;
        fileList= new ArrayList<>();
        walk(folder,mask);

        try {
            fileWrite = new BufferedWriter(new FileWriter(mergefile));
        } catch(IOException e) {
            System.out.println("Fail_1: "+e.toString());
        }

        for (String filename : fileList ) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(filename));  
                while(true) {
                    String line = br.readLine();
                    if(line!=null)
                        fileWrite.write(line+"\n");
                    else
                        break;
                }
                br.close();
            } catch(IOException e) {
                System.out.println("Fail_2: "+e.toString());
            }  
        }
        try {
            fileWrite.close();
        } catch(IOException e) {
            System.out.println("Failed: fileWrite.close()");
        }        
    }
             
    /**
     * Recursively walks the folder tree.
     * Files that contain that match file_mask are added to fileList
     * \sA fileList
     */
    public void walk( String path, String file_mask ) {

        File root = new File( path );
        File[] list = root.listFiles();

        String pathname;       

        if (list == null) return;

        // sift through files and find matches
        for ( File f : list ) {
            pathname = f.getAbsoluteFile().toString();
            if(pathname.contains(file_mask)) {
                fileList.add(pathname);
                System.out.println( "Found:" + pathname);
            }
        }
    }
} 
    
