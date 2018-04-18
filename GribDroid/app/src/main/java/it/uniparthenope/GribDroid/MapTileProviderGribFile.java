package it.uniparthenope.GribDroid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;

import net.sourceforge.jgrib.NoValidGribException;
import net.sourceforge.jgrib.NotSupportedException;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by pasqualecelardo on 09/01/2018.
 */

public class MapTileProviderGribFile extends MapTileProviderBase {
    public static final String LOG_TAG="MAPTILEPROVIDERGRIBFILE";
    private GribFileTileSource gribFileTileSource;
    private Context context;
    private int mode=0;
    private Worker worker;


    public MapTileProviderGribFile(Context context, GribFileTileSource gribFileTileSource) {
        super(gribFileTileSource);
        this.context=context;
        this.gribFileTileSource=gribFileTileSource;
    }


    @Override
    public Drawable getMapTile(MapTile pTile) {
        long time = System.currentTimeMillis();
        long x = System.nanoTime();
        Tile tile=new Tile(pTile);
        // this creates a MUTABLE bitmap
        Bitmap bitmap = Bitmap.createBitmap(tile.getSize(), tile.getSize(), Bitmap.Config.ARGB_8888);

        if (gribFileTileSource.getMinLon() < tile.getBoundingBox().getLonWest() &&
                gribFileTileSource.getMaxLon() > tile.getBoundingBox().getLonEast() &&
                gribFileTileSource.getMinLat() < tile.getBoundingBox().getLatNorth() &&
                gribFileTileSource.getMaxLat() > tile.getBoundingBox().getLatSouth()) {


            if (mode==0) {
                int size = tile.getSize();
                double minLat = tile.getBoundingBox().getLatSouth();
                double minLon = tile.getBoundingBox().getLonWest();
                double dLat = tile.getBoundingBox().getLatitudeSpan() / size;
                double dLon = tile.getBoundingBox().getLongitudeSpan() / size;

                int color;
                float u = Float.NaN;
                double lat, lon;
                try {

                    gribFileTileSource.setRecord(1);
                    for (int j = 0; j < size; j++) {
                        lat = minLat + j * dLat;
                        for (int i = 0; i < size; i++) {
                            lon = minLon + i * dLon;
                            u = gribFileTileSource.getValue(lat, lon);
                            color = gribFileTileSource.getColor(u);
                            bitmap.setPixel(i, size - j - 1, color);
                        }
                    }


                } catch (IOException e) {
                    //Log.d(LOG_TAG, e.getMessage());
                } catch (NotSupportedException e) {
                    //Log.d(LOG_TAG, e.getMessage());
                } catch (NoValidGribException e) {
                    //Log.d(LOG_TAG, e.getMessage());
                }
            } else if (mode==1) {
                 worker = new Worker(gribFileTileSource, tile,bitmap);
                try {
                    Scheduler.getInstance().submit(worker);
                    worker.join();
                } catch (InterruptedException e) {
                    Log.e(LOG_TAG, e.getMessage());
                }

            } else if (mode==2) {
                Canvas canvas = new Canvas(bitmap);
                BoundingBox boundingBox=tile.getBoundingBox();
                int color;
                float u = Float.NaN;
                double lat, lon;

                Paint paint=new Paint();


                // Check if the tile is inside a single grib cell
                int[] JI0=gribFileTileSource.getJIbyLatLon(tile.getBoundingBox().getLatSouth(),tile.getBoundingBox().getLonWest());
                int[] JI1=gribFileTileSource.getJIbyLatLon(tile.getBoundingBox().getLatNorth(),tile.getBoundingBox().getLonEast());

                Log.d(LOG_TAG,"Tile:"+pTile.toString()+" J0:"+JI0[0]+ " I0:"+ JI0[1]+" J1:"+JI1[0]+" I1:"+JI1[1]);
                int size=tile.getSize();
                double minLat=tile.getBoundingBox().getLatSouth();
                double minLon=tile.getBoundingBox().getLonWest();
                double dLat=tile.getBoundingBox().getLatitudeSpan()/size;
                double dLon=tile.getBoundingBox().getLongitudeSpan()/size;

                if (JI0[0]==JI1[0] && JI0[1]==JI1[1]) {

                    u = gribFileTileSource.getValue(JI0[0], JI0[1]);

                    color = gribFileTileSource.getColor(u);
                    paint.setColor(color);
                    Rect rect = new Rect(0, 0, size, size);
                    canvas.drawRect(rect, paint);
                    paint.setColor(Color.BLUE);
                    canvas.drawLine(0, 0, size, size, paint);
                    canvas.drawLine(0, size, size, 0, paint);
                } else {

                    // The tile matches more than a single on grib grid cell
                    for (int j=JI0[0]; j<=JI1[0]; j++) {
                        for (int i=JI0[1];i<=JI1[1];i++) {

                            u = gribFileTileSource.getValue(j, i);
                            color = gribFileTileSource.getColor(u);
                            paint.setColor(color);

                            double[] latLon=gribFileTileSource.getLatLonByJI(j,i);
                            int[] yX=gribFileTileSource.getJIbyLatLon(latLon[0],latLon[1]);
                            int top=yX[0];

                            int left=yX[1];

                            latLon=gribFileTileSource.getLatLonByJI(j+1,i+1);
                            yX=gribFileTileSource.getJIbyLatLon(latLon[0],latLon[1]);
                            int bottom=yX[0];
                            int right=yX[1];
                            top=0;
                            left=0;
                            right=256;
                            bottom=256;

                            Rect rect=new Rect(left,top,right,bottom);
                            canvas.drawRect(rect,paint);
                        }
                    }
                }
            }
        } else {
            Canvas canvas = new Canvas(bitmap);
            Paint paint=new Paint();
            paint.setColor(Color.BLUE);
            canvas.drawLine(0, 0, 256, 0, paint);
            canvas.drawLine(0, 0, 0, 256, paint);

        }

        Drawable drawable= new BitmapDrawable(context.getResources(), bitmap);
        long end = System.currentTimeMillis();

        long res = end-time;
        Log.d(LOG_TAG, "time: "+res);


        return drawable;

    }

    /*
    @Override
    public Drawable getMapTile(MapTile pTile) {
        Tile tile=new Tile(pTile);


        Bitmap bitmap = Bitmap.createBitmap(tile.getSize(), tile.getSize(), Bitmap.Config.ARGB_8888); // this creates a MUTABLE bitmap
        Canvas canvas = new Canvas(bitmap);

        // Check if the tile is inside the grib
        if (tile.isIn(gribFileTileSource.getMinLat(),gribFileTileSource.getMinLon(),gribFileTileSource.getMaxLat(), gribFileTileSource.getMaxLon())) {

            BoundingBox boundingBox=tile.getBoundingBox();
            int color;
            float u = Float.NaN;
            double lat, lon;

            Paint paint=new Paint();


            // Check if the tile is inside a single grib cell
            int[] JI0=gribFileTileSource.getJIbyLatLon(tile.getBoundingBox().getLatSouth(),tile.getBoundingBox().getLonWest());
            int[] JI1=gribFileTileSource.getJIbyLatLon(tile.getBoundingBox().getLatNorth(),tile.getBoundingBox().getLonEast());

            Log.d(LOG_TAG,"Tile:"+pTile.toString()+" J0:"+JI0[0]+ " I0:"+ JI0[1]+" J1:"+JI1[0]+" I1:"+JI1[1]);
            int size=tile.getSize();
            double minLat=tile.getBoundingBox().getLatSouth();
            double minLon=tile.getBoundingBox().getLonWest();
            double dLat=tile.getBoundingBox().getLatitudeSpan()/size;
            double dLon=tile.getBoundingBox().getLongitudeSpan()/size;

            if (JI0[0]==JI1[0] && JI0[1]==JI1[1]) {

                u = gribFileTileSource.getValue(JI0[0], JI0[1]);
                int mode=1;
                if (mode==0) {
                    color = gribFileTileSource.getColor(u);
                    paint.setColor(color);
                    Rect rect = new Rect(0, 0, size, size);
                    canvas.drawRect(rect, paint);
                    paint.setColor(Color.BLUE);
                    canvas.drawLine(0, 0, size, size, paint);
                    canvas.drawLine(0, size, size, 0, paint);
                } else {

                    // Draw the bitmap pixel by pixel
                    try {
                        for (int j = 0; j < size; j++) {
                            lat = minLat + j * dLat;
                            for (int i = 0; i < size; i++) {
                                lon = minLon + i * dLon;
                                //Log.d(LOG_TAG,"lon:"+lon+" lat:"+lat);
                                u = gribFileTileSource.getValue(lat, lon);
                                color = gribFileTileSource.getColor(u);
                                bitmap.setPixel(i, size - j - 1, color);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NotSupportedException e) {
                        e.printStackTrace();
                    } catch (NoValidGribException e) {
                        e.printStackTrace();
                    }
                }
            } else {

                // The tile matches more than a single on grib grid cell
                for (int j=JI0[0]; j<=JI1[0]; j++) {
                    for (int i=JI0[1];i<=JI1[1];i++) {

                        u = gribFileTileSource.getValue(j, i);
                        color = gribFileTileSource.getColor(u);
                        paint.setColor(color);

                        double[] latLon=gribFileTileSource.getLatLonByJI(j,i);
                        int[] yX=gribFileTileSource.getJIbyLatLon(latLon[0],latLon[1]);
                        int top=yX[0];
                        int left=yX[1];

                        latLon=gribFileTileSource.getLatLonByJI(j+1,i+1);
                        yX=gribFileTileSource.getJIbyLatLon(latLon[0],latLon[1]);
                        int bottom=yX[0];
                        int right=yX[1];
                        top=0;
                        left=0;
                        right=256;
                        bottom=256;

                        Rect rect=new Rect(left,top,right,bottom);
                        canvas.drawRect(rect,paint);
                    }
                }
            }
        }
        Drawable drawable= new BitmapDrawable(context.getResources(), bitmap);
        return drawable;
    }
    */
    @Override
    public int getMinimumZoomLevel() {
        return getTileSource().getMinimumZoomLevel();
    }

    @Override
    public int getMaximumZoomLevel() {
        return getTileSource().getMaximumZoomLevel();
    }

    @Override
    public IFilesystemCache getTileWriter() {
        return null;
    }

    @Override
    public long getQueueSize() {
        return 0;
    }








}
