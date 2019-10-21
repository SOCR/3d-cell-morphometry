/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author beagles
 */
public class FilterC2Lst {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        FilterC2Process process = new FilterC2Process();
        String folderFileIn = args[0];
        int connected_index = folderFileIn.indexOf("_connected");
        String folderFileOut = folderFileIn.substring(0,connected_index)+"_connected.lst";
        String folderFileStats = folderFileIn.substring(0,connected_index)+"_statistics.txt";
        System.out.println("folderFileIn: "+folderFileIn);
        System.out.println("folderFileOut: "+folderFileOut);
        System.out.println("folderFileStats: "+folderFileStats);
        process.Process(folderFileOut, folderFileIn, folderFileStats);
        // TODO code application logic here
    }
    
}
