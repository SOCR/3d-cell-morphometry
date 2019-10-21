
/**
 * Tracks connections between segments.
 * @author dilworth
*/
public class SegConnect {

    /** Maximum number of segments found in image */
    public int maxSegments;

    /** Maximum X coordinate in image */
    public int MaxX;

    /** Maximum Y coordinate in image */
    public int MaxY;

    /** Maximum Z coordinate in image */
    public int MaxZ;

    /** Number of the target pixels in a segment */
    public int[] Pixels;
    
    /** Number of the surface pixels in a segment */
    public int[] Surface;

    /** Number of hollow points in a segment */
    public int[] Hollows;
    
    /** Number of edges from a segment */
    public int[] Edges;
    
   /** =1 if segment box is at image bounds */
    public int[] Bounds;    

    /** Minimum x coordiante for a segment */
    public int[] X1;

    /** Maximum x coordinate for a segment */
    public int[] X2;

    /** Minimum y coordinate for a segment */
    public int[] Y1;

   
    public int[] Y2;

    /** Minimum z coordinate for a segment */
    public int[] Z1;

    /** Maximum z coordinate for a segment */
    public int[] Z2;

    /** Sum of x coordinates for a segment */
    public int[] Xsum;

    /** Sum of y coordinates for a segment */
    public int[] Ysum;

   /** Sum of z coordinates for a segment */
    public int[] Zsum;

    /** X centroid for a segment */
    public double[] Xcent;

    /** Y centroid for a segment */
    public double[] Ycent;

    /** z centroid for a segment */
    public double[] Zcent;
    
    /** Spherical compactness, convert to Cd later */
    public double[] Cs;
    
    public boolean[] Exported;
    


    /** Number of connections between different segments.
     * A weighted adjancency matrix.  */
    public int[][] Connected;

    /** Constructe with image dimensions */
    SegConnect(int max_segments, int width, int height, int slices) {
        maxSegments = max_segments;
        MaxX=width;
        MaxY=height;
        MaxZ=slices;
        Pixels = new int[maxSegments];
        Surface = new int[maxSegments];
        Cs = new double[maxSegments];
        Connected = new int[maxSegments][maxSegments];
        Hollows = new int[maxSegments];
        Edges = new int[maxSegments];
        Bounds = new int[maxSegments];
        X1 = new int[maxSegments];
        X2 = new int[maxSegments];
        Y1 = new int[maxSegments];
        Y2 = new int[maxSegments];
        Z1 = new int[maxSegments];
        Z2 = new int[maxSegments];
        Xsum = new int[maxSegments];
        Ysum = new int[maxSegments];
        Zsum = new int[maxSegments];
        Xcent = new double[maxSegments];
        Ycent = new double[maxSegments];
        Zcent = new double[maxSegments];
        Exported = new boolean[maxSegments];
        Clear();
    }
    /** Clears tracked values, not dimensions */
    void Clear() {
        for(int so=0;so<maxSegments;so++) {
            Pixels[so]=0;
            Hollows[so]=0;
            Pixels[so]=0;
            Surface[so]=0;
            Cs[so]=0;
            Bounds[so]=0;
            X1[so]=MaxX;
            X2[so]=0;
            Y1[so]=MaxY;
            Y2[so]=0;            
            Z1[so]=MaxZ;
            Z2[so]=0;
            Xsum[so]=0;
            Ysum[so]=0;
            Zsum[so]=0;
            Xcent[so]=0;
            Ycent[so]=0;
            Zcent[so]=0;
            Exported[so]=false;
            for(int si=0;si<maxSegments;si++) {
                Connected[so][si]=0;
            }
        }
    }
    /** Tracks results for a pixel(x,y,z) in a segment */
    void foundSegment(int segment, int x, int y, int z) {
        if(x<X1[segment])
            X1[segment]=x;
        if(x>X2[segment])
            X2[segment]=x;  
        if(y<Y1[segment])
            Y1[segment]=y;
        if(y>Y2[segment])
            Y2[segment]=y;         
        if(z<Z1[segment])
            Z1[segment]=z;
        if(z>Z2[segment])
            Z2[segment]=z;        
        Pixels[segment]++;
        Xsum[segment]+=x;
        Ysum[segment]+=y;
        Zsum[segment]+=z;
    }
    /* Tracks that two segments were in connected */
    void foundConnect(int base, int connect) {
        if(base!=connect)
            Connected[base][connect]++;
    }
    /* Tracks that two segments were in connected */
    void foundSurface(int base) {
        Surface[base]++;
    }
     /* Tracks that a hollow values was found in a segment. */
    void setHollowValue(int segment, int value) {
        Hollows[segment]=value;
    }
     /* Computes centroid (call after image is processed). */
    void computeCentroid() {
        double size;
        double sa;
        for(int so=0;so<maxSegments;so++) {
            size = (double)Pixels[so];
            if(size>0) {
                Xcent[so]=Xsum[so]/size;
                Ycent[so]=Ysum[so]/size;
                Zcent[so]=Zsum[so]/size;
            } else {
                Xcent[so]=0;
                Ycent[so]=0;
                Zcent[so]=0;
            }
            sa = (double) Surface[so];
            if(sa>0)
                Cs[so] = (36*Math.PI*size*size)/(sa*sa*sa);
            else
                Cs[so]=0;
        }
    }
}
