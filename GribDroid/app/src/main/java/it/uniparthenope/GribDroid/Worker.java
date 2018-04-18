package it.uniparthenope.GribDroid;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import net.sourceforge.jgrib.NoValidGribException;
import net.sourceforge.jgrib.NotSupportedException;

import org.osmdroid.tileprovider.MapTile;

import java.io.IOException;

public class Worker extends Thread {
    public static final String LOG_TAG="WORKER";

    private Bitmap bitmap;
    private GribFileTileSource gribFileTileSource;
    private Tile tile;

    public Worker(GribFileTileSource gribFileTileSource, Tile tile, Bitmap bitmap) {
        this.gribFileTileSource=gribFileTileSource;
        this.tile=tile;
        this.bitmap=bitmap;
    }



    @Override
    public void run() {
        Log.d(LOG_TAG,"Tile:"+tile);
        int size=tile.getSize();
        double minLat=tile.getBoundingBox().getLatSouth();
        double minLon=tile.getBoundingBox().getLonWest();
        double dLat=tile.getBoundingBox().getLatitudeSpan()/size;
        double dLon=tile.getBoundingBox().getLongitudeSpan()/size;

        int color;
        float u=Float.NaN;
        double lat,lon;
        try {
            gribFileTileSource.setRecord(1);
            for (int j=0;j<size;j++) {
                lat=minLat+j*dLat;
                for (int i=0;i<size;i++) {
                    lon=minLon+i*dLon;
                    u=gribFileTileSource.getValue(lat,lon);
                    color=gribFileTileSource.getColor(u);
                    bitmap.setPixel(i,size-j-1,color);
                }
            }
        } catch (IOException e) {
            Log.d(LOG_TAG,e.getMessage());
        } catch (NotSupportedException e) {
            Log.d(LOG_TAG,e.getMessage());
        } catch (NoValidGribException e) {
            Log.d(LOG_TAG,e.getMessage());
        }

    }
}
