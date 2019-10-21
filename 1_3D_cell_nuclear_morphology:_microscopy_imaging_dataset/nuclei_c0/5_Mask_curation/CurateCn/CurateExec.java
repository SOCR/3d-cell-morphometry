
import java.io.File;
import ij.*;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author beagles
 */
public class CurateExec {
    String version="v0.1.0.0";
    String imageDotLog;       
    String imageFolder; 
    String filterDotFlt;
    char fileSepChar;
    String regExSep;
    DiagLogger logger;


    boolean Execute(String folderImageTif, String folderFilterFlt) {
        
        fileSepChar = File.separatorChar;

        // test if c0 or c2 type of image
        boolean modeC0= folderImageTif.contains("c0");
        
        // create logger from folder_image_tif (replace .tif with .log) 
        //String reg = "\\"+fileSepChar+"\\.tif";
        //String[] splitDot = folderImageTif.split(reg);    // split regex
        String[] splitDot = folderImageTif.split("\\.tif");    // split regex
        String folderImageBase = splitDot[0];               // folder/image w/o extension
        imageDotLog = splitDot[0]+".log";
        logger = new DiagLogger();
        logger.Append(imageDotLog,version);
        AG.L = logger;                       // logger is accessed by AG.L
        
        
        // path to image folder with trailing slash
        int lastSlashIndex = folderImageTif.lastIndexOf(fileSepChar);
        imageFolder=folderImageTif.substring(0,lastSlashIndex+1);
        
        // path to folder and partial list file name
        //String temp = folderImageTif.substring(0,lastSlashIndex);
        //int folderSlash = temp.lastIndexOf(fileSepChar);
        //String folderLstBase = temp+temp.substring(folderSlash);
        
        // name of image file w/o extension
        String imageNameBase = folderImageBase.substring(lastSlashIndex+1);
                
        // save the filter name
        filterDotFlt=folderFilterFlt;
        
        AG.L.Log("MaskPath, "+folderImageTif);
        AG.L.Log("LogPath,  "+imageDotLog);
        
        // load the filter file
        SegmentFilter filter = new SegmentFilter();
        filter.loadFilter((folderFilterFlt));
        filter.toLog();

        // load the c0 or c2 TIF image
        ImagePlus imagePlus = IJ.openImage(folderImageTif);  
        
        // curation sequence
        CurateImage curate = new CurateImage(imagePlus,filter);
        curate.connectSegments();
        curate.applyFilter();
        curate.exportList(imageFolder,folderImageBase,imageNameBase,modeC0);
        curate.printResults(imageFolder,imageNameBase);
        return true;
    }
    
}
