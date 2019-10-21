/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.*;
import ij.*;
import ij.process.ImageProcessor;

/**
 *
 * @author beagles
 */
public class CurateImage {
    ImagePlus imagePlus;
    int imageMaxX;
    int imageMaxY;
    int imageMaxZ;
    int[][][] Image3D;
    int maxID;
    SegConnect Connect;
    SegmentFilter segmentFilter;
    int dummy=0;   
   
    // constructor prepares for processing
    // - saves filter
    // - gets image sizes
    // - loads image into 3D array
    // - finds the segment id (maxID)
    public CurateImage(ImagePlus image_plus, SegmentFilter filter) {
        int x,y,z;
        int[][] azt;
        ImageProcessor imageProcessor;
        
        segmentFilter=filter;
        
        imagePlus=image_plus;
        imageMaxX=imagePlus.getWidth();
        imageMaxY=imagePlus.getHeight();
        imageMaxZ=imagePlus.getNSlices();   

        Image3D = new int[imageMaxZ][imageMaxY][imageMaxX];
        maxID=0;
        for(z=0;z<imageMaxZ;z++)
        {
            imagePlus.setSlice(z+1);
            imageProcessor = imagePlus.getProcessor();
            azt = imageProcessor.getIntArray();
            for(y=0;y<imageMaxY;y++)
            {
                for(x=0;x<imageMaxX;x++)
                {
                    Image3D[z][x][y]=azt[x][y];
                    if(azt[x][y]>maxID)
                        maxID=azt[x][y];        // max segment id
                } 
            }   
        }
        maxID++;    // zero based index
    }
    
    // Fills Connect object and finds 26 connected neighbors
    // 1st loop is 3D
    // - updates each pixel
    // - tracks neighbors
    // - tracks surfeace
    // computes centroid
    // 2nd loop all segments
    // - track edges
    // 3rd loop, boundary and scan xyz for hollow
    // boundary has a pixel and image array boundary
    // we want to find voids
    // - here, voids are definded as a background pixel within "surface"
    // - no algorithm for surface from voxels has been identified
    // - we instead settle for hollows
    // - hollow is background betweeen targets
    // - hollow is exhasustively scanned along all axes, minimum is reported
    public void connectSegments() {
        int x,y,z;
        int xc,yc,zc;
        int pixel, connected;
        int targetCount=0;
        int so,si;
        int boundary,edges;
        int x1,x2,y1,y2,z1,z2;
        int hollow_x, hollow_y, hollow_z, hollow;
        int xl,xh, yl, yh, zl,zh;   
        
        // allocates object that will be filtered
        Connect = new SegConnect(maxID,imageMaxX,imageMaxY,imageMaxZ);
        
        // first loop is 3D
        // - updates each pixel
        // - tracks neighbors
        // - tracks surfeace
        // computes centroid
        for(z=0;z<imageMaxZ;z++) {
            for(y=0;y<imageMaxY;y++) {
                for(x=0;x<imageMaxX;x++) {

                    pixel = Image3D[z][x][y];
                    if(pixel>0) {
                        Connect.foundSegment(pixel,x,y,z);
                        targetCount++;
                        // search whole -1 0 +1 in 3 dimensions
                        // could also work in 0 +1 space and extend results?
                        for(yc=y-1;yc<=y+1;yc++) {
                            if(yc<0 || yc >=imageMaxY)   // test valid y index
                                continue;
                            for(xc=x-1;xc<=x+1;xc++) {
                                if(xc<0 || xc >=imageMaxX) // test valid x index
                                    continue;   
                                for(zc=z-1;zc<=z+1;zc++) {
                                    if(zc<0 || zc >=imageMaxZ) // test valid z index
                                        continue;
                                    connected = Image3D[zc][xc][yc];    // neighbor pixel
                                    if(connected>0)                             // test background
                                        Connect.foundConnect(pixel,connected);  // target is connected
                                    else {
                                        int xyz = Math.abs(yc-y)+ Math.abs(xc-x)+ Math.abs(zc-z);
                                        if(xyz==1)                          // surface is +/- 1 in on axis only
                                            Connect.foundSurface(pixel);    // target is surface
                                    }
                                }                                                                
                            }
                        }
                    }
                }
            }         
        }
        Connect.computeCentroid();
        
        // 2nd loop all segments
        // - track edges
        for(so=1;so<maxID;so++)
        {
            edges=0;
            for(si=0;si<maxID;si++)
            {
                if(Connect.Connected[so][si]>0)
                    edges++;
            }  
            Connect.Edges[so]=edges; 
        }
        
        // 3rd loop, boundary and scan xyz for hollow
        // boundary has a pixel and image array boundary
        // we want to find voids
        // - here, voids are definded as a background pixel within "surface"
        // - no algorithm for surface from voxels has been identified
        // - we instead settle for hollows
        // - hollow is background betweeen targets
        // - hollow is exhasustively scanned along all axes, minimum is reported
        for(so=0;so<maxID;so++)
        {
            // track boundaries
            boundary=0;
            if(Connect.X1[so]==0 || Connect.Y1[so]==0 || Connect.Z1[so]==0)
                boundary=1;
            if(Connect.X2[so]==Connect.MaxX-1 || Connect.Y2[so]==Connect.MaxY-1 || Connect.Z2[so]==Connect.MaxZ-1)
                boundary=1;
            Connect.Bounds[so]=boundary;             
            
            // track hollows (approximates "voids")
            if(Connect.Pixels[so]>0)
            {
                x1=Connect.X1[so];
                x2=Connect.X2[so];  
                y1=Connect.Y1[so];
                y2=Connect.Y2[so];  
                z1=Connect.Z1[so];
                z2=Connect.Z2[so];  

                hollow_x=hollow_y=hollow_z=0;
                // scan XYZ
                for(x=x1;x<=x2;x++)                      
                {
                    for(y=y1;y<=y2;y++)
                    {
                        zl=zh=-1;                   // set both to -1
                        for(z=z1;z<=z2;z++)         // scan from z1 to z2 at (x,y)
                        {
                            if(Image3D[z][x][y]>0)
                            {
                                zl=z;               // first target z1 to z2
                                break;
                            }
                        }
                        for(z=z2;z>=z1;z--)         // scan from z2 to z1 at (x,y)
                        {
                            if(Image3D[z][x][y]>0)
                            {
                                zh=z;              // first target z2 to z1
                                break;
                            }
                        }  
                        if(zl==-1 || zh==-1)
                            break;                  // didn't find two targets
                        for(z=zl;z<=zh;z++)         // scan between targets
                        {
                            if(Image3D[z][x][y]==0)
                            {
                                hollow_z++;         // count hollows for XYZ
                            }
                        } 
                    }
                }
                // scan YZX
                for(y=y1;y<=y2;y++)             
                {
                    for(z=z1;z<=z2;z++)
                    {
                        xl=xh=-1;
                        for(x=x1;x<=x2;x++)
                        {
                            if(Image3D[z][x][y]>0)
                            {
                                xl=x;
                                break;
                            }
                        }
                        for(x=x2;x>=x1;x--)
                        {
                            if(Image3D[z][x][y]>0)
                            {
                                xh=x;
                                break;
                            }
                        }  
                        if(xl==-1 || xh==-1)
                            break;
                        for(x=xl;x<=xh;x++)
                        {
                            if(Image3D[z][x][y]==0)
                            {
                                hollow_x++;         // count hollows for YZX
                            }
                        } 
                    }
                }
                // scan ZXY
                for(z=z1;z<=z2;z++)                    
                {
                    for(x=x1;x<=x2;x++)
                    {
                        yl=yh=-1;
                        for(y=y1;y<=y2;y++)
                        {
                            if(Image3D[z][x][y]>0)
                            {
                                yl=y;
                                break;
                            }
                        }
                        for(y=y2;y>=y1;y--)
                        {
                            if(Image3D[z][x][y]>0)
                            {
                                yh=y;
                                break;
                            }
                        }  
                        if(yl==-1 || yh==-1)
                            break;
                        for(y=yl;y<=yh;y++)
                        {
                            if(Image3D[z][x][y]==0)
                            {
                                hollow_y++;         // count hollows for ZXY
                            }
                        } 
                    }
                }     
                if(hollow_x<hollow_y)
                    hollow=hollow_x;
                else
                    hollow=hollow_y;
                if(hollow_z<hollow)
                    hollow=hollow_z;
                Connect.setHollowValue(so,hollow);  // report minimum
            }
        }
    }
    
    // apply the filter settings to the Connect resutls
    void applyFilter() {
        int so;
        boolean export_flag;
        
        for(so=1;so<Connect.maxSegments;so++)  {
            int pixels = Connect.Pixels[so];               // pixels in segment
            int voids = Connect.Hollows[so];                 // number of voids in segment
            int edges = Connect.Edges[so];                 // number of edges (touching segments)
            int bounds = Connect.Bounds[so];               // =0 if no pixel is touching an edge
            double cs = Connect.Cs[so];
            export_flag=false;
            if( pixels >= segmentFilter.voxelsMin && pixels <= segmentFilter.voxelsMax) {
                if(voids >= segmentFilter.voidsMin && voids <= segmentFilter.voidsMax) {
                    if(edges >= segmentFilter.edgesMin && edges <= segmentFilter.edgesMax) {
                        if(bounds >= segmentFilter.boundsMin && bounds <= segmentFilter.boundsMax) {
                            if(cs >= segmentFilter.csMin && cs <= segmentFilter.csMax) {
                                export_flag=true;
                            }
                        }
                    }
                }
            }
            Connect.Exported[so]=export_flag;
        }
    }
    
    // exports images names that passed all filters
    // - for c0 and c2, passes SegmentFilter
    // - for c2, corresponding c0 nucleus must also have been passed
    boolean exportList(String folder, String folderImageBase, String imageNameBase, boolean modeC0) {
        String folderFileLst;
        String folderFileC0;
        String linesC0=null;       
        FileWriter fileList;
        String folderImageNii="";
        String midName;
        int so;
        
        int grid_index = imageNameBase.indexOf("_g");
        String grid_text=imageNameBase.substring(grid_index,grid_index+6);        
        
        // for c0 and c2, get name of .LST file to write to
        // for c2, also load corresponding c0.lst file
        if(modeC0)
            folderFileLst=folderImageBase+".lst";       // c0 preps
        else {
            folderFileLst=folderImageBase+"_prelim.lst";    // c2 preps
            int c2Index = folderImageBase.indexOf("_c2");
            
            folderFileC0 = folderImageBase.substring(0,c2Index)+"_c0"+grid_text+"mask.lst";
            try {
                BufferedReader br = new BufferedReader(new FileReader(folderFileC0));
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();
                while (line != null) {              // read _c0.lst into linesC0
                    sb.append(line);
                    //sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                br.close();
                linesC0 = sb.toString();
            } catch(FileNotFoundException ex) {
                System.out.println("Fail_1: "+ex.toString());
            } catch(IOException ex) {
                System.out.println("Fail_2: "+ex.toString());
            }              
        }
        
        // open .LST file for writing
        try {
            fileList = new FileWriter(folderFileLst);
        } catch (IOException e) {
            System.out.println("Fail_3: "+e.toString());
            return false;
        }       
        int d=0;    // dummy variable
               
        //File root = new File( folder );         // list of all files
        //File[] list = root.listFiles();         // only c2 needs this
    
        
        // step through all segments and output names of valid files
        // - only consider files with Exported flag set
        // - c0's filenames are only filtered by Exported flag
        // - c2's filenames are also filtered against valid c0 (nucleus) filename
        String filename_nii="";
        for(so=1;so<Connect.maxSegments;so++)  {
            if(Connect.Exported[so]) {
                int mask_c0=1;               // preset this to pass c0 filenames 
                if(modeC0==true) {
                    midName = "-"+String.format("%03d",so);
                    folderImageNii = imageNameBase+midName+".nii";  // c0 is done here
                } else {                                            // c2 has more work to do
                    File root = new File( folder );         // list of all files
                    File[] list = root.listFiles();
                    int nf = list.length;
                    int dummy=0;
                    int f;

                    // test to see if the corresponding c0 nuclei file is present
                    // - at this point we know the grid and nucleoli numbers
                    // - we search through files on disk to find one with matching "grid" and "nucleoli" numbers
                    // - then extract the nuclei number and build the c0 nuclei name
                    // - then see if that name was exported in the c0.lst file
                    // .*gGGG_mask-...-nucleoli-NNN.nii.gz
                    String test = ".*"+grid_text+"mask-..."+"-nucleoli-"+String.format("%03d", so)+".tif";
                    //test = ".*"+grid_text+"mask-..."+".*";
                    //test = ".*"+grid_text+".*";
                    String name = "nucleoli-"+String.format("%03d", so)+".tif";
                    char fileSepChar = File.separatorChar;
                    String fname;
                    for(f=0;f<nf;f++) {
                        fname = list[f].toString();             // get trial filename for disk
                        boolean match = fname.matches(test);
                        if(match) {
                        //if(fname.contains(grid_text)) {         // does it contain grid number?
                            //if(fname.contains(name)) {          // does it contain nucleoli number?
                                 int slash = fname.lastIndexOf(fileSepChar);
                                 String justfile = fname.substring(slash+1);
                                 int len = justfile.length();
                                 filename_nii=justfile.substring(0,len-4)+".nii";
                                 folderImageNii = filename_nii;
                                 int mask = filename_nii.indexOf("mask-");          // get the nuclei mask from filename
                                 String mask_name = grid_text+filename_nii.substring(mask,mask+8);
                                 mask_c0 = linesC0.indexOf(mask_name);
                                 break;
                            //}
                        }   
                    }
                }                
                
                if(mask_c0==-1) {
                    AG.L.Log("NotInCO: "+folderImageNii);   // log show c2's not in c0 list
                } else {
                    AG.L.Log("Exported: "+folderImageNii+"  <--");  /// log shows filename exported
                    try {
                        fileList.write(folderImageNii+"\n");    // write to .LST file
                    } catch(IOException e) {
                        System.out.println("Fail_4: "+e.toString());
                        d++;
                    }
                }
            } else {
                 AG.L.Log("Rejected:"+so);      // log show rejected
            }
        }
        try {
            fileList.close();
        } catch(IOException e) {
            System.out.println("Fail_5: "+e.toString());
            d++;
        }        
      
        return true;
    }
    
    // Prints results to log file
    // - prints Connect segments data w/o adjacency matrix
    // - commented CSV file ops had same data with adjacency matrix
    // - text prep for CSV remains and full CSV could be restored
    
    //curate.printResults(imageFolder,imageNameBase);
    void printResults(String imageFolder,String imageNameBase) {     
        File CsvFile;
        FileWriter CsvStream=null;
        String text;
        int so,si;
        String log_text;
        
        try
        {
            //String folder = imageFolder.getText();
            //String pathfile = "test.csv"; //folder+FileBase+"_segments.csv";
            String pathfile = imageFolder+imageNameBase+"_segments.csv";
            CsvFile = new File(pathfile);
            CsvStream = new FileWriter(CsvFile);

            text="Xc,Yc,Zc,Voxels,Surface,Cs,Voids,Edges,Bounds,IDs";
            for(so=0;so<maxID;so++)
            {
                text=text+","+Integer.toString(so);
            }

            CsvStream.write(text+"\n");

            AG.L.Log(" -----------------------------------------------");
            AG.L.Log("   ID,  Xc,   Yc, Voxels, Surface,    Cs,  Voids,  Edges, Bounds");
            for(so=1;so<maxID;so++)
            {
                int xcent = (int) Math.round(Connect.Xcent[so]);
                int ycent = (int) Math.round(Connect.Ycent[so]);
                int zcent = (int) Math.round(Connect.Zcent[so]);

                //String test = String.format("%5d",so);
                log_text = " "+String.format("%4d",so)+","+String.format("%4d",xcent)+", "+String.format("%4d",ycent)+","
                        +String.format("%7d",Connect.Pixels[so])+","+String.format("%8d",Connect.Surface[so])+","+String.format("%6.3f",Connect.Cs[so])+","+String.format("%7d",Connect.Hollows[so])+","+String.format("%7d",Connect.Edges[so])+","+String.format("%7d",Connect.Bounds[so]);
                text = xcent+","+ycent+","+zcent+","+Connect.Pixels[so]+","+Connect.Surface[so]+","+String.format("%9.6f",Connect.Cs[so])+","+Integer.toString(Connect.Hollows[so])+","+Integer.toString(Connect.Edges[so])+","+Integer.toString(Connect.Bounds[so])+","+Integer.toString(so);
                for(si=0;si<maxID;si++)
                {
                    text = text + ","+Connect.Connected[so][si];
                }
                AG.L.Log(log_text);
                CsvStream.write(text+"\n");
            }  
            AG.L.Log(" -----------------------------------------------");
            CsvStream.close();
        }
        catch (Exception e) {
            System.out.println("Fail_6: "+e.toString());
        }        
    }
    
}
