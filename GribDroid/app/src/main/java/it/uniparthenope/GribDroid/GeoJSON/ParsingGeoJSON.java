package it.uniparthenope.GribDroid.GeoJSON;

import com.cocoahero.android.geojson.Feature;
import com.cocoahero.android.geojson.FeatureCollection;
import com.cocoahero.android.geojson.GeoJSON;
import com.cocoahero.android.geojson.GeoJSONObject;
import com.cocoahero.android.geojson.LineString;
import com.cocoahero.android.geojson.Point;
import com.cocoahero.android.geojson.Polygon;
import com.cocoahero.android.geojson.Position;

import org.json.JSONException;
import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pasqualecelardo on 01/12/17.
 */

public class ParsingGeoJSON {

    private ArrayList<GeoPoint> mywaypoints;
    private ArrayList<GeoPoint> myPolygon;
    private ArrayList<GeoPoint> myLinestrings;

    private getInfoPolygonResults polyInfo;
    private getInfoLineStringResult liinestringInfo;

    public getInfoLineStringResult getLiinestringInfo() {
        return liinestringInfo;
    }

    public ArrayList<GeoPoint> getMywaypoints() {
        return mywaypoints;
    }

    public ArrayList<GeoPoint> getMyPolygon() {
        return myPolygon;
    }

    public ArrayList<GeoPoint> getMyLinestrings() {
        return myLinestrings;
    }

    public getInfoPolygonResults getPolyInfo() {
        return polyInfo;
    }

    public void openGeoJSON(InputStream is){
        try {
            System.out.print("*--- FILE APERTO CON SUCCESSO ---*\n");
            GeoJSONObject gj= GeoJSON.parse(is);
            String type = gj.getType();
            System.out.println("Type: "+type+"\n");
            ReadFromGeoJSON(gj);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void ReadFromGeoJSON(GeoJSONObject file) throws JSONException {
        switch (file.getType())
        {
            case GeoJSON.TYPE_FEATURE_COLLECTION:
                FeatureCollection featureCollection = (FeatureCollection) file;
                mywaypoints = new ArrayList<GeoPoint>();
                myPolygon = new ArrayList<GeoPoint>();
                myLinestrings = new ArrayList<GeoPoint>();
                int ii=0;
                for(Feature feature : featureCollection.getFeatures())
                {
                    switch(feature.getGeometry().getType())
                    {
                        case GeoJSON.TYPE_POINT:

                            Point point=(Point) feature.getGeometry();
                            System.out.print("Punto "+ii+"\n");
                            double lat = point.getPosition().getLatitude();
                            System.out.println(""+lat+"\n");
                            double lon = point.getPosition().getLongitude();
                            System.out.println(lon+"\n");
                            GeoPoint x = new GeoPoint(lat,lon);
                            mywaypoints.add(x);
                            ii++;
                            break;

                        case GeoJSON.TYPE_LINE_STRING:
                            System.out.print("6\n");
                            List<Position> line = (((LineString)feature.getGeometry()).getPositions());
                            int i=0;
                            for (Position p1 : line){
                                System.out.println("LINESTRING "+i+"\n");
                                System.out.println(p1.getLatitude()+" - "+p1.getLongitude());
                                GeoPoint linestring = new GeoPoint(p1.getLatitude(),p1.getLongitude());
                                i++;
                                myLinestrings.add(linestring);
                            }

                            String strokeLine = feature.getProperties().getString("stroke");
                            int swLine = feature.getProperties().getInt("stroke-width");
                            int soLine = feature.getProperties().getInt("stroke-opacity");
                            this.liinestringInfo = new getInfoLineStringResult(strokeLine,swLine,soLine);
                            System.out.println("STROKE: "+strokeLine+"\n");
                            System.out.println("S_WIDTH: "+swLine+"\n");
                            System.out.println("S_OPACITY: "+soLine+"\n");

                            break;
                        case GeoJSON.TYPE_POLYGON:
                            System.out.print("6\n");
                            List<Position> positions = ((Polygon)feature.getGeometry()).getRings().get(0).getPositions();
                            for (Position p : positions) {
                                System.out.println("LATITUDE: "+p.getLatitude()+"\n");
                                System.out.println("LONGITUDE: "+p.getLongitude()+"\n");
                                GeoPoint polygon = new GeoPoint(p.getLatitude(),p.getLongitude());
                                myPolygon.add(polygon);
                            }

                            String stroke = feature.getProperties().getString("stroke");
                            int s_width = feature.getProperties().getInt("stroke-width");
                            int s_opacity = feature.getProperties().getInt("stroke-opacity");
                            String fill = feature.getProperties().getString("fill");
                            double f_opacity = feature.getProperties().getDouble("fill-opacity");

                            polyInfo = new getInfoPolygonResults(stroke,s_width,s_opacity,fill,f_opacity);

                            System.out.println("STROKE: "+stroke+"\n");
                            System.out.println("S_WIDTH: "+s_width+"\n");
                            System.out.println("S_OPACITY: "+s_opacity+"\n");
                            System.out.println("FILL: "+fill+"\n");
                            System.out.println("F_OPACITY: "+f_opacity+"\n");
                            break;
                    }
                }
        }
    }

}
