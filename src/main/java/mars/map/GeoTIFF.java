package mars.map;

import org.geotools.coverage.grid.GridCoordinates2D;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.DirectPosition2D;

import java.awt.image.Raster;
import java.io.File;

/*
 * An implementation of a terrain map using the GeoTIFF format.
 */
public class GeoTIFF extends TerrainMap {
    private static GridCoverage2D grid; //grid coverage from input GeoTIFF, see http://docs.geotools.org/latest/javadocs/org/geotools/coverage/grid/GridCoverage2D.html
    private static Raster gridData; //image data from input GeoTIFF


    //init function, takes in the file path to a target GeoTIFF
    public void initMap(String fileLocation) throws Exception {
        initTif(fileLocation);
    }

    /**
     * Initialization function for GeoTIFF class. Loads full GeoTIFF into memory for use
     * Access to GeoTIFF data handled through other functions in this class using the created GeoTiffReader instance
     * @param fileLocation file path to GeoTIFF
     * @throws Exception exception generated by Geotools
     */
    public void initTif(String fileLocation) throws Exception {
        File tiffFile = new File(fileLocation); //get the tiff
        GeoTiffReader reader = new GeoTiffReader(tiffFile); //make a GeoTiffReader (a apache geotools class)

        grid = reader.read(null); //read in the tiff file
        gridData = grid.getRenderedImage().getData(); //and its data
    }

    /**
     * Function to get an elevation at a certain point in the elevation map.
     * @param x x-coordinate (in pixels) of the desired elevation in the elevation map
     * @param y y-coordinate (in pixels) of the desired elevation in the elevation map
     * @return elevation at the given point
     * @throws Exception exception generated by Geotools
     */
    public double getValue(double x, double y) throws Exception { //take in x,y and return elevation
        GridGeometry2D gg = grid.getGridGeometry();

        DirectPosition2D posWorld = new DirectPosition2D(x,y);
        GridCoordinates2D posGrid = gg.worldToGrid(posWorld);

        //envelope is the size in the target projection
        double[] pixel = new double[1];
        if(x > gridData.getWidth() || x < 0 || y > gridData.getHeight() || y < 0){ //if x or y out of bounds, error
            throw new Exception("Bad getValue");
        }
        double[] data = gridData.getPixel(posGrid.x,posGrid.y,pixel);
        return data[0];
    }

    /**
     * Function to get maximum value of a GeoTIFF.
     * @return maximum value
     * @throws Exception exception generated by Geotools
     */
    public double getMaxValue() throws Exception {
        double maxElevation = Double.MIN_VALUE; //use minimum
        double currentElevation;
        for(int i = 0; i < gridData.getWidth(); i++)
        {
            for(int j = 0; j < gridData.getHeight(); j++)
            {
                currentElevation = getValue(i,j);
                if(currentElevation > maxElevation){
                    maxElevation = currentElevation;
                }
            }
        }
        System.out.println("Maximum Elevation:  "+ maxElevation);
        return maxElevation;
    }

    /**
     * Function to get minimum value of a GeoTIFF.
     * @return minimum value
     * @throws Exception exception generated by Geotools
     */
    public double getMinValue() throws Exception {
        double minElevation = Double.MAX_VALUE; //use maximum
        double currentElevation;
        for(int i = 0; i < gridData.getWidth(); i++)
        {
            for(int j = 0; j < gridData.getHeight(); j++)
            {
                currentElevation = getValue(i,j);
                if(currentElevation < minElevation){
                    minElevation = currentElevation;
                }
            }
        }
        System.out.println("Minimum Elevation:  "+ minElevation);
        return minElevation;
    }

    /* leftover function from Route. Keeping here for now
    public void getLine(double x) throws Exception {
        double lastStat = -1;
        double newStat = -1;
        int lastY = 0;
        int maxY = 46080;
        for(int i=0; i<maxY; i++){
            newStat = terrain.getValue(x,i);
            if(newStat != lastStat){
                System.out.println(Double.toString(lastStat) + " (" + Integer.toString(lastY) + " - " + Integer.toString(i) + ")");
                lastStat = newStat;
                lastY = i;
            }
        }
    }
     */
}