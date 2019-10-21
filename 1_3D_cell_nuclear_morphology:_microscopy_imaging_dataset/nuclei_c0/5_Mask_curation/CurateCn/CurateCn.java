/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author dilworth
 */
public class CurateCn {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
       if(args.length==2) {
           CurateExec main = new CurateExec();
           main.Execute((args[0]), args[1]);
            System.out.println("CurateCn "+ args[0] + " "+args[1]);
        } else System.out.println("usage: CurateCn folderImage.tif folderFilter.flt");  
    }
    
}
