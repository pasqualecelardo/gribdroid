package it.uniparthenope.GribDroid;

import android.graphics.Point;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;

public class Tile {

    private int size;
    private double initialResolution;
    private double originShift;
    private int offset;

    private int Z;
    private int X;
    private int Y;

    private BoundingBox boundingBox;
    private MeterBoundingBox meterBoundingBox;

    public int getZoomLevel() { return Z; }
    public int getX() { return X; }
    public int getY() { return Y; }
    public int getSize() { return size; }
    public double getInitialResolution() { return initialResolution; }
    public double getOriginShift() { return originShift; }

    public Tile(MapTile mapTile) {
        this.Z=mapTile.getZoomLevel();
        this.X=mapTile.getX();
        this.Y=mapTile.getY();
        init();
    }

    public Tile(int Z, int X, int Y) {
        this.Z=Z;
        this.X=X;
        this.Y=Y;
        init();
    }

    private void init() {
        size=256;
        initialResolution = 2.0 * Math.PI * 6378137.0 / size;
        originShift = 2.0 * Math.PI * 6378137.0 / 2.0;
        offset = size << (Z-1);

        int x=X;
        int y=(int)((Math.pow(2,Z) - 1) - Y);
        Point llPoint=new Point(x*size,y*size);
        Point urPoint=new Point((x+1)*size, (y+1)*size);
        MeterPoint mpll=pixelToMeters(llPoint,Z);
        MeterPoint mpur=pixelToMeters(urPoint,Z);

        meterBoundingBox=new MeterBoundingBox(mpll,mpur);
        GeoPoint gpll=metersToLatLon(meterBoundingBox.getLowerLeft());
        GeoPoint gpur=metersToLatLon(meterBoundingBox.getUpperRight());
        boundingBox=new BoundingBox(gpur.getLatitude(),gpur.getLongitude(),gpll.getLatitude(),gpll.getLongitude());
    }

    public GeoPoint metersToLatLon(MeterPoint meterPoint) {
        double lon = (meterPoint.getX() / originShift) * 180.0;
        double lat = (meterPoint.getY() / originShift) * 180.0;

        lat = 180 / Math.PI * (2 * Math.atan( Math.exp( lat * Math.PI / 180.0)) - Math.PI / 2.0);
        return new GeoPoint(lat,lon);
    }

    public MeterPoint pixelToMeters(Point p, int zoom) {
        double res=getResolution(zoom);
        double mx = p.x * res - originShift;
        double my = p.y * res - originShift;

        return new MeterPoint(mx,my);
    }

/*
    public MeterBoundingBox getTileBounds() {
        int x=X;
        int y=(int)((Math.pow(2,Z) - 1) - Y);
        Point llPoint=new Point(x*tileSize,y*tileSize);
        Point urPoint=new Point((x+1)*tileSize, (y+1)*tileSize);
        MeterPoint ll=pixelToMeters(llPoint,Z);
        MeterPoint ur=pixelToMeters(urPoint,Z);
        return new MeterBoundingBox(ll,ur);
    }

    public BoundingBox getBoundingBox() {
        MeterBoundingBox bounds=getTileBounds();
        GeoPoint ll=metersToLatLon(bounds.getLowerLeft());
        GeoPoint ur=metersToLatLon(bounds.getUpperRight());
        return new BoundingBox(ur.getLatitude(),ur.getLongitude(),ll.getLatitude(),ll.getLongitude());
    }
    */

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public MeterBoundingBox getMeterBoundingBox() {
        return meterBoundingBox;
    }

    public double getResolution(int zoom) {

        return initialResolution / (Math.pow(2,zoom));
    }

    public boolean isIn(double minLat, double minLon, double maxLat, double maxLon) {
        if (
                boundingBox.getLatSouth()>=minLat &&
                boundingBox.getLonWest()>=minLon &&
                boundingBox.getLatNorth()<=maxLat &&
                boundingBox.getLonEast()<=maxLon) {
            return true;
        }
        return false;
    }

    public int[] yXByLatLon(double lat, double lon) {
        int[] result={0,0};
        int x0=(int)(offset+(offset*boundingBox.getLonWest()/180));
        int y0=(int)(offset - offset/Math.PI * Math.log(
                (1 + Math.sin(boundingBox.getLatSouth() * Math.PI / 180)) / (1 - Math.sin(boundingBox.getLatSouth() * Math.PI / 180))) / 2);
        int x=(int)(offset+(offset*lon/180));
        int y=(int)(offset - offset/Math.PI * Math.log(
                (1 + Math.sin(lat * Math.PI / 180)) / (1 - Math.sin(lat * Math.PI / 180))) / 2);
        result[0]=y-y0;
        result[1]=x-x0;
        return result;
    }

    public int[] latLngToTileXY(double lat, double lon, int zoom) {
        double minLat = -85.05112878;
        double minLon = -180;
        double maxLat = 85.05112878;
        double maxLon = 180;
        double mapSize = /*Math.pow(2, zoom) * */ 256;
        double longitude = clip(lon, minLon, maxLon);
        double latitude = clip(lat, minLat, maxLat);
        double x = (longitude + 180.0) / 360.0  *(1 << zoom);
        double y = (1.0 - Math.log(Math.tan(latitude * Math.PI / 180.0) + 1.0 / Math.cos(LatLonToRad(lat))) / Math.PI) / 2.0  *(1 << zoom);
        int pX = (int) x;
        int pY = (int) y;
        double pixelX = clipByRange((pX * 256) + ((x - pX) * 256), mapSize - 1);
        double pixelY = (/*256 - */mapSize - clipByRange((pY * 256) + ((y - pY) * 256), mapSize - 1));
        int coord_X = (int) pixelX;
        int coord_Y = (int) pixelY;
        return new int[]{coord_X, coord_Y};
    }

    public double clip(double n, double minvalue, double maxvalue) {
        return Math.min(Math.max(n, minvalue), maxvalue);
    }

    public double LatLonToRad(double x) {
        return x * Math.PI / 180;
    }

    public double clipByRange(double n, double range) {
        return (n % range);

    }
}
