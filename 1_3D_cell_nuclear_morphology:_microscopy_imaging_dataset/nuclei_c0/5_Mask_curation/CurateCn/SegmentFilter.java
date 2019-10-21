/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author beagles
 */
import java.io.*;

public class SegmentFilter {
    public String pathName;
    public int voxelsMax;
    public int voxelsMin;
    public int voidsMax;
    public int voidsMin;
    public int edgesMax;
    public int edgesMin;
    public int boundsMax;
    public int boundsMin;  
    public double csMax;
    public double csMin;  
    SegmentFilter() {
        Clear();
    }
    
    void Clear() {
        pathName=null;
        voxelsMax=0; 
        voxelsMin=0; 
        voidsMax=0; 
        voidsMin=0; 
        edgesMax=0; 
        edgesMin=0; 
        boundsMax=0; 
        boundsMin=0;  
        csMax=0; 
        csMin=0;
    }
    
    double getDouble(String line) {
        String[] key_value = line.split(",");
        String key = key_value[0];
        double version = Double.parseDouble(key_value[1]);   
        return version;
    }
    
    String getString(String line) {
        String[] key_value = line.split(",");
        String key = key_value[0];
        String version = key_value[1];   
        return version;
    }
    
    int getInteger(String line) {
        String[] key_value = line.split(",");
        String key = key_value[0];
        int value = Integer.parseInt(key_value[1]);  
        return value;
    }
    
                    
    boolean loadFilter(String pathname) {
        boolean result=true;
        FileReader fileRead;
        BufferedReader buffRead;
        String line;
        String[] key_value;
        String key;
        int value;
        String version;
        
        Clear();
        pathName = pathname;
        
        try {
            fileRead = new FileReader(pathname);
            buffRead= new BufferedReader(fileRead);
            version = getString(buffRead.readLine());
            if(version.equals("1.0")) {
                buffRead.readLine();    // skip warning line
                voxelsMin = getInteger(buffRead.readLine());                
                voxelsMax = getInteger(buffRead.readLine());
                voidsMin = getInteger(buffRead.readLine());                
                voidsMax = getInteger(buffRead.readLine());                
                edgesMin = getInteger(buffRead.readLine());                
                edgesMax = getInteger(buffRead.readLine());    
                boundsMin = getInteger(buffRead.readLine());                
                boundsMax = getInteger(buffRead.readLine());  
                csMin = getDouble(buffRead.readLine());                
                csMax = getDouble(buffRead.readLine()); 
            }
            if(version.equals("1.1")) {
                buffRead.readLine();    // skip warning line
                voxelsMin = getInteger(buffRead.readLine());                
                voxelsMax = getInteger(buffRead.readLine());
                voidsMin = getInteger(buffRead.readLine());                
                voidsMax = getInteger(buffRead.readLine());                
                edgesMin = getInteger(buffRead.readLine());                
                edgesMax = getInteger(buffRead.readLine());    
                boundsMin = getInteger(buffRead.readLine());                
                boundsMax = getInteger(buffRead.readLine());  
                csMin = getDouble(buffRead.readLine());                
                csMax = getDouble(buffRead.readLine()); 
            } else {
                System.out.println("Unknown Filter File version "+ version);
                result=false;
            }           
        } catch(IOException e) {
            System.out.println("Fail_1: "+e.toString());
            result=false;
        }
        return result;        
    }
    void toLog()
    {
        AG.L.Log("Filter Parameters:");
        AG.L.Log(" Pathname,"+pathName);
        AG.L.Log(" Voxels, "+voxelsMin+","+voxelsMax);
        AG.L.Log(" Voids, "+voidsMin+","+voidsMax);
        AG.L.Log(" Edges, "+edgesMin+","+edgesMax);
        AG.L.Log(" Bounds, "+boundsMin+","+boundsMax);
        AG.L.Log(" Cs, "+csMin+","+csMax);
    }
}
