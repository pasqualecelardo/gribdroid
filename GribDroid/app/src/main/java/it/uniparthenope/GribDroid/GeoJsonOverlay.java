package it.uniparthenope.GribDroid;

import android.graphics.Canvas;

import com.cocoahero.android.geojson.Feature;
import com.cocoahero.android.geojson.FeatureCollection;
import com.cocoahero.android.geojson.GeoJSON;
import com.cocoahero.android.geojson.GeoJSONObject;

import org.json.JSONException;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by pasqualecelardo on 13/12/2017.
 */

public class GeoJsonOverlay extends Overlay {
    private GeoJSONObject geoJSONObject;

    public GeoJsonOverlay(String filename) throws IOException, JSONException{
        FileInputStream fileInputStream = new FileInputStream(filename);
        GeoJSONObject geoJSONObject = GeoJSON.parse(fileInputStream);
    }
    @Override
    public void draw(Canvas c, MapView osmv, boolean shadow) {

        switch (geoJSONObject.getType()) {
            case GeoJSON.TYPE_FEATURE_COLLECTION:
                FeatureCollection featureCollection = (FeatureCollection) geoJSONObject;
                for (Feature feature : featureCollection.getFeatures()) {
                    switch (feature.getType()) {
                        case GeoJSON.TYPE_POINT:
                            break;

                        case GeoJSON.TYPE_LINE_STRING:
                            break;

                        case GeoJSON.TYPE_POLYGON:
                            break;
                    }
                }
        }
    }
}
