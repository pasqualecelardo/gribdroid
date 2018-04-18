package it.uniparthenope.GribDroid;


import android.graphics.Color;
import android.util.Log;

import net.sourceforge.jgrib.GribFile;
import net.sourceforge.jgrib.GribRecord;
import net.sourceforge.jgrib.NoValidGribException;
import net.sourceforge.jgrib.NotSupportedException;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase;
import org.osmdroid.util.BoundingBox;

import java.io.IOException;
import java.util.Random;

/**
 * Created by pasqualecelardo on 09/01/2018.
 */

public class GribFileTileSource extends BitmapTileSourceBase {
    public static final String LOG_TAG="GRIBFILETILESOURCE";
    private GribFile gribFile;
    private int nRows,nCols;
    private double minLon,maxLon,dLon;
    private double minLat,maxLat,dLat;
    private float[] values;
    private int index;

    public int getRows() { return nRows; }
    public int getCols() { return nCols; }
    public double getMinLon() { return minLon; }
    public double getMinLat() { return minLat; }
    public double getMaxLon() { return maxLon; }
    public double getMaxLat() { return maxLat; }
    public double getDLon() { return dLon; }
    public double getDLat() { return dLat; }
    public double getLonEx() { return maxLon-minLon; }
    public double getLatEx() { return maxLat-minLat; }


    public GribFileTileSource(String aName, GribFile gribFile) throws NoValidGribException, NotSupportedException, IOException {
        super(aName, 0, 20, 256, null);
        this.gribFile=gribFile;
        init();
    }

    public GribFileTileSource(String aName, GribFile gribFile, String aCopyrightNotice) throws NoValidGribException, NotSupportedException, IOException {
        super(aName, 0, 20, 256, null, aCopyrightNotice);
        this.gribFile=gribFile;
        init();
    }

    private void init() throws NoValidGribException, NotSupportedException, IOException {
        index=1;
        setRecord(index);
    }


    public void setRecord(int index) throws NoValidGribException, NotSupportedException, IOException {
        this.index=index;
        GribRecord gribRecord=gribFile.getRecord(index);
        nRows=gribRecord.getGDS().getGridNY();
        nCols=gribRecord.getGDS().getGridNX();
        minLon=gribRecord.getGDS().getGridLon1();
        if (minLon>180) {
            minLon=minLon-360;
        }
        maxLon=gribRecord.getGDS().getGridLon2();
        if (maxLon>180) {
            maxLon=maxLon-360;
        }
        if (minLon>maxLon) {
            double tmp=maxLon;
            maxLon=minLon;
            minLon=tmp;
        }
        dLon=gribRecord.getGDS().getGridDX();
        minLat=gribRecord.getGDS().getGridLat1();
        maxLat=gribRecord.getGDS().getGridLat2();
        dLat=gribRecord.getGDS().getGridDY();
        values=gribRecord.getValues();
    }

    public double[] getLatLonByJI(int j, int i) {
        double _minLat=minLat+j*dLat;
        double _minLon=minLon+i*dLon;
        double _maxLat=_minLat+dLat;
        double _maxLon=_minLon+dLon;
        return new double[] { _minLat, _minLon, _maxLat,_maxLon};
    }
    public int[] getJIbyLatLon(double lat, double lon) {
        double I=(lon-minLon)/dLon;
        double J=(lat-minLat)/dLat;

        int i=(int)Math.round(I);
        if (i==nCols) {
            i--;
        }
        if (i==-1) {
            i=0;
        }
        int j=(int)Math.round(J);
        if (j==nRows) {
            j--;
        }
        if (j==-1) {
            j=0;
        }
        return new int[] { j, i };
    }

    public float getValue(int j, int i) {
        if (i >= 0 && i < nCols && j >= 0 && j < nRows) {
            int index=j * nCols + i;
            return values[index];
        }
        return Float.NaN;
    }

    public float getValue(double lat, double lon) throws IOException,NotSupportedException,NoValidGribException {
        float value=Float.NaN;
        if (lon>=minLon && lon<=maxLon && lat>=minLat && lat<=maxLat) {
            int J=(int)((lat-minLat)/dLat);
            int I=(int)((lon-minLon)/dLon);

            /*
            if ((J==nRows)||(I==nCols)) {
                return value;
            }
            */
            if (J>=1 && J<nRows-1 && I>=1 && I<nCols-1) {
                double y1 = minLat + J * dLat;
                double x1 = minLon + I * dLon;
                double y2 = y1 + dLat;
                double x2 = x1 + dLon;



                double CC = values[J * nCols + I];
                double Q11 = (CC + values[(J - 0) * nCols + (I - 1)] + values[(J - 1) * nCols + (I - 0)] + values[(J - 1) * nCols + (I - 1)]) * .25;
                double Q12 = (CC + values[(J + 1) * nCols + (I - 1)] + values[(J + 1) * nCols + (I - 0)] + values[(J + 0) * nCols + (I - 1)]) * .25;
                double Q21 = (CC + values[(J + 0) * nCols + (I + 1)] + values[(J - 1) * nCols + (I + 1)] + values[(J - 1) * nCols + (I - 0)]) * .25;
                double Q22 = (CC + values[(J + 1) * nCols + (I + 0)] + values[(J + 1) * nCols + (I + 1)] + values[(J + 0) * nCols + (I + 1)]) * .25;

                double R1 = ((x2 - lon) / (x2 - x1)) * Q11 + ((lon - x1) / (x2 - x1)) * Q21;
                double R2 = ((x2 - lon) / (x2 - x1)) * Q12 + ((lon - x1) / (x2 - x1)) * Q22;
                value = (float) (((y2 - lat) / (y2 - y1)) * R1 + ((lat - y1) / (y2 - y1)) * R2);

            }
        }
        return value;
    }

    public int getColor(float value) {
        if (Float.isNaN(value)) {
            return Color.TRANSPARENT;
        }
        int color=Color.TRANSPARENT;
        if (value<-4.91) color=Color.parseColor("#90fff5f0");
        else if (value>=-4.91 && value<-2.52) color=Color.parseColor("#90fff5f0");
        else if (value>=-2.52 && value<-0.119) color=Color.parseColor("#90ffe3d7");
        else if (value>=-0.119 && value<2.28) color=Color.parseColor("#90fdc6af");
        else if (value>= 2.28 && value<4.67) color=Color.parseColor("#90fca487");
        else if (value>= 4.67 && value<7.07) color=Color.parseColor("#90fc8161");
        else if (value>= 7.07 && value<9.47) color=Color.parseColor("#90f85d42");
        else if (value>= 9.47 && value<11.9) color=Color.parseColor("#90eb362a");
        else if (value>=11.9 && value<14.3) color=Color.parseColor("#90cc181d");
        else if (value>=14.3 && value<16.7) color=Color.parseColor("#90a90f15");
        else if (value>=16.7) color= Color.parseColor("#9067000d");
        return color;
    }



}
