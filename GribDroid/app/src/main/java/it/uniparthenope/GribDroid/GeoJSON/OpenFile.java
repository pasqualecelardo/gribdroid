package it.uniparthenope.GribDroid.GeoJSON;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.uniparthenope.GribDroid.R;

public class OpenFile {

    public ParsingGeoJSON test = new ParsingGeoJSON();
    public MapView mapView;

    public OpenFile(String string,  Context context) throws IOException {
        InputStream is = context.getResources().getAssets().open(string);
        test.openGeoJSON(is);
        ArrayList<GeoPoint> geo = test.getMywaypoints();
        ArrayList<GeoPoint> pol = test.getMyPolygon();
        ArrayList<GeoPoint> line = test.getMyLinestrings();

    }


    public void setStartMarker(GeoPoint startPoint,MapView map){

        Marker startMarker = new Marker(map);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(startMarker);
        //startMarker.setIcon(getResources().getDrawable(R.drawable.start));
        startMarker.setTitle("Start point");
    }

    public void setMiddleMarker(GeoPoint startPoint,MapView map){
                Marker startMarker = new Marker(map);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(startMarker);
        //startMarker.setIcon(getDrawable(R.drawable.middle));
        startMarker.setTitle("Middle point");
    }

    public void setLastMarker(GeoPoint startPoint,MapView map){
        Marker startMarker = new Marker(map);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(startMarker);
        //startMarker.setIcon(getResources().getDrawable(R.drawable.end));
        startMarker.setTitle("Destination point");
    }


    public void getRoute(ArrayList<GeoPoint> waypoints, MapView map){
        setStartMarker(waypoints.get(0),map);
        drawRoute(waypoints.get(0),waypoints.get(1),map);
        for(int i=1; i < waypoints.size()-1;i++) {
            setMiddleMarker(waypoints.get(i),map);
            drawRoute(waypoints.get(i),waypoints.get(i+1),map);
        }
        setLastMarker(waypoints.get(waypoints.size()-1),map);
        //disegnaRotta(waypoints);
    }

    public void drawRoute(GeoPoint start, GeoPoint end,MapView map){

        //ParsingGeoJSON color = new ParsingGeoJSON();
        List<GeoPoint> geoPoints = new ArrayList<>();
        //add your points here
        geoPoints.add(start);
        geoPoints.add(end);
        Polyline line = new Polyline();
        //String c = test.getPolyInfo().getStroke();
        //line.setColor(Color.parseColor(c));
        System.out.println("");
        line.setOnClickListener(new Polyline.OnClickListener() {
            @Override
            public boolean onClick(Polyline polyline, MapView mapView, GeoPoint eventPos) {
                Toast.makeText(mapView.getContext(), "polyline with " + polyline.getPoints().size() + "pts was tapped", Toast.LENGTH_LONG).show();
                return false;
            }
        });
        //map.getOverlayManager().add(line);
        map.invalidate();
    }

}
