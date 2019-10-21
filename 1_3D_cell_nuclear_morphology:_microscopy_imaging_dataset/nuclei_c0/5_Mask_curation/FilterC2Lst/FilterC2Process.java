
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author beagles
 */
public class FilterC2Process {
    
    FilterC2Process() {
    }
    
    void Process(String folderFileOut, String folderFileIn, String folderFileStats) {
        BufferedWriter fileWrite=null;      

        // read statistics file into a string array
        StringBuilder sb = new StringBuilder();
        ArrayList<String> statsList = new ArrayList<>();
        try {
            
            BufferedReader statsReader = new BufferedReader(new FileReader(folderFileStats));
            while(true) {
                String text = statsReader.readLine();
                if(text!=null) {
                    statsList.add(text);
                } else {
                    break;
                }
            }
            statsReader.close();
        } catch(IOException e) { 
            System.out.println("Fail_1: "+e.toString());
        } 

        // open the output text file
        try {
            fileWrite = new BufferedWriter(new FileWriter(folderFileOut));
        } catch(IOException e) {
            System.out.println("Fail_2: "+e.toString());
        }

        // process the input text file (prelim file)
        // - read each input line
        // - extract grid number and nucleoli number
        // - find entry in stats with matching grid and blob
        // - write to file if max value (from stats) is > 0
        try {
            BufferedReader br = new BufferedReader(new FileReader(folderFileIn));  
            while(true) {
                String line = br.readLine();
                if(line!=null) {
                    
                    int gindex = line.indexOf("c0_g");
                    String gstring = line.substring(gindex + 4, gindex +7);
                    int grid = Integer.parseInt(gstring);            // grid number in prelim file

                    int bindex = line.indexOf("nucleoli-");
                    String bstring = line.substring(bindex + 9, bindex +12);
                    int nucleoli = Integer.parseInt(bstring);        // nucleoli number in prelim file

                    int hits=0;
                    int miss=0;
                    boolean found = false;                     // true if matching file had fibrillarin
                    String t_stat;
                    int b, g, max;
                    for (String statLine : statsList ) {       // find grid and blob in stat_data
                        if (statLine.charAt(0) == 'G')         // skip header line
                            continue;
                        String[] tsplit = statLine.split("\t"); // split stats file
                        g = Integer.parseInt(tsplit[0]);        // grid number
                        b = Integer.parseInt(tsplit[1]);        // blob is nucleoli number
                        max = Integer.parseInt(tsplit[5]);      // amount of fibrillarin
                        if (max > 0 && g == grid && b == nucleoli) {    // tests max, grid, and blob in one line
                            found = true;                               // could test grid and blob, then hit, the break
                            break;
                        }
                    }
                    if (found) {                                // if found then write name
                        hits++;
                        fileWrite.write(line+"\n");
                    } else
                        miss++;
                }                                   
                else
                    break;
            }
            br.close();
            fileWrite.close();
        } catch(IOException e) {
            System.out.println("Fail_3: "+e.toString());
        }      
    }
    
}
