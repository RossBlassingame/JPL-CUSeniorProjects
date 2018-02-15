package mars.map;

import mars.coordinate.Coordinate;
import mars.coordinate.Coordinate;
import org.geotools.coverage.grid.GridCoordinates2D;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.DirectPosition2D;
import mars.coordinate.*;

import java.awt.*;
import java.awt.image.PackedColorModel;
import java.awt.image.Raster;
import java.io.File;

/*
 * An implementation of a terrain map using the GeoTIFF format.
 */
public class GeoTIFF extends TerrainMap {
    private static GridCoverage2D grid; //grid coverage from input GeoTIFF, see http://docs.geotools.org/latest/javadocs/org/geotools/coverage/grid/GridCoverage2D.html
    private static Raster gridData; //image data from input GeoTIFF
    private String mapPath; //stores the path to this GeoTIFF


    //init function, takes in the file path to a target GeoTIFF
    public void initMap(String fileLocation) throws Exception {
        mapPath = fileLocation;
        initTif(fileLocation);
    }

    /**
     * Initialization function for GeoTIFF class. Loads full GeoTIFF into memory for use
     * Access to GeoTIFF data handled through other functions in this class using the created GeoTiffReader instance
     * @param fileLocation file path to GeoTIFF
     * @throws Exception exception generated by Geotools
     */
    public void initTif(String fileLocation) throws Exception {
        mapPath = fileLocation;
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

    public double getWidth() throws Exception {
        return gridData.getWidth();
    }

    public double getHeight() throws Exception {
        return gridData.getHeight();
    }

    /**
     * Returns the file path which initialized this GeoTIFF.
     */
    public String getMapPath() {
        return mapPath;
    }

    /**
     * Returns the elevations of pixels within a given
     * rectangular area, defined by an origin point and
     * a width and height from the origin
     * (width and height cannot be negative).
     *
     * @return a 2D array of the elevations at each pixel in the area
     * @param origin a Coordinate point which is the origin of the rectangular area
     * @param width the number of pixels to extend the area in the X direction from the origin
     * @param height the number of pixels to extend the area in the Y direction from the origin
     */
    public double[][] getElevationsInArea(Coordinate origin, int width, int height) {
        int x = origin.getX();
        int y = origin.getY();
        double[][] elevations = new double[height][width];
        //bounds checking
        int areaWidth = ((x+width) < gridData.getWidth()) ? width : gridData.getWidth();
        int areaHeight = ((y+height) < gridData.getHeight()) ? height : gridData.getHeight();

        for (int i = x; i < x+areaWidth; i++) {
            for (int j = y; j < y+areaHeight; j++) {
                try {
                    int row = (height-1) - (j-y);
                    int col = i - x;
                    elevations[row][col] = getValue(i, j);
                }
                catch (Exception e) {
                    //out of bounds; no-op
                }
            }
        }

        return elevations;
    }


	public Coordinate coordinate2LatLong(Coordinate coord){
    	if(coord.getUnits() != "pixels") {
			//return original cooridinate
			return coord;
		}

		//get x and y coordinate
		double x = coord.getX();
		double y = coord.getY();

		System.out.println(x);
		System.out.println(y);

		//declare lat/long variables
		double latitude;
		double longitude;

		//Mars Radius in meters
		int marsRadius = 3396200;

		//declare negative value degrees
		boolean isSouth = false;
		boolean isWest = false;

		double getEquator = gridData.getHeight()/2;
		double getPrimeMeridean = gridData.getWidth()/2;

		System.out.println(getEquator);
		System.out.println(getPrimeMeridean);

		double pixelDistanceLatitude = y - getEquator;
		double pixelDistanceLongitude = x - getPrimeMeridean;

		System.out.println(pixelDistanceLatitude);
		System.out.println(pixelDistanceLongitude);

		//Assuming that each pixel is 5mx5m using the viking phobos.
		/**
		 * TODO: Create a checks which map we are using and use correct scaling
		 */
		double arcLengthLatitude = pixelDistanceLatitude * 5;
		double arcLengthLongitude = pixelDistanceLongitude * 5;

		System.out.println(arcLengthLatitude);
		System.out.println(arcLengthLongitude);

		if(arcLengthLatitude < 0) {
			arcLengthLatitude = arcLengthLatitude * -1;
			isSouth = true;
		}

		if(arcLengthLongitude < 0) {
			arcLengthLongitude = arcLengthLongitude * -1;
			isWest = true;
		}

		double circumference = 2 * Math.PI * marsRadius;

		latitude = (360 * arcLengthLatitude) / circumference;
		longitude = (360 * arcLengthLongitude) / circumference;

		System.out.println(latitude);
		System.out.println(longitude);


		if(isSouth) {
			latitude = latitude * -1;
		}

		if(isWest) {
			longitude = longitude * -1;
		}

		int latitudeint = (int) latitude;
		int longitudeint = (int) longitude;

		Coordinate newCoord = new Coordinate(latitudeint, longitudeint);
		newCoord.setUnits("latLong");
		return newCoord;

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
