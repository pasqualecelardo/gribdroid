package it.uniparthenope.GribDroid;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cocoahero.android.geojson.Feature;
import com.cocoahero.android.geojson.FeatureCollection;
import com.cocoahero.android.geojson.GeoJSON;
import com.cocoahero.android.geojson.GeoJSONObject;

import net.sourceforge.jgrib.GribFile;
import net.sourceforge.jgrib.NoValidGribException;
import net.sourceforge.jgrib.NotSupportedException;

import org.json.JSONException;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import it.uniparthenope.GribDroid.GeoJSON.OpenFile;

public class MainActivity extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE=222;
    public OpenFile openFileGeo ;
    public GribFileTileSource gribFileTileSource;
    public static final String LOG_TAG="TIMEEEEEEEEEEEEEEEEEe";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        final LinearLayout linearLayoutMap=findViewById(R.id.layout_map);
        final LinearLayout linearLayoutSplash=findViewById(R.id.layout_splash);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                showYouShouldGrant();
                askPermissions();

            } else {

                // No explanation needed, we can request the permission.

                askPermissions();

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    linearLayoutSplash.setVisibility(View.GONE);
                    linearLayoutMap.setVisibility(View.VISIBLE);
                }
            }, 5000);


            MapView map = (MapView) findViewById(R.id.map);
            map.setTileSource(TileSourceFactory.MAPNIK);
            map.setBuiltInZoomControls(true);
            map.setMultiTouchControls(true);

            GeoPoint startPoint = new GeoPoint(32.34, -64.79);


            IMapController mapController = map.getController();
            mapController.setZoom(4);
            mapController.setCenter(startPoint);

            String mygeo = "points.geojson";



            /*
            XYTileSource xyTileSource=new XYTileSource("Layer1",
                    5,25,
                    256,".png",
                    new String[] {"http://tiles.openseamap.org/seamark/"});
            MapTileProviderBasic tileProviderBasicOpenSeaMap=new MapTileProviderBasic(getBaseContext(),xyTileSource);
            TilesOverlay tilesOverlayOpenSeaMap=new TilesOverlay(tileProviderBasicOpenSeaMap,getBaseContext());
            map.getOverlays().add(tilesOverlayOpenSeaMap);
            */

            try {
                long start = System.currentTimeMillis();
                openFileGeo = new OpenFile(mygeo,getApplicationContext());
                InputStream is=getAssets().open("Mediterranean.wind.7days.grb");
                //InputStream is=getAssets().open("CentralPacific.grb");
                GribFile gribFile = new GribFile(is);
                gribFileTileSource = new GribFileTileSource("Layer2",gribFile);
                MapTileProviderGribFile tileProviderBasicCustom = new MapTileProviderGribFile(getBaseContext(), gribFileTileSource);
                TilesOverlay tilesOverlayCustom = new TilesOverlay(tileProviderBasicCustom, getBaseContext());
                map.getOverlays().add(tilesOverlayCustom);
                long end = System.currentTimeMillis();
                long res = end - start;
                //Log.d(LOG_TAG,"Time:"+res);

            } catch (IOException ex1) {
                throw new RuntimeException(ex1);
            } catch (NoValidGribException ex2) {
                throw new RuntimeException(ex2);
            } catch (NotSupportedException ex3) {
                throw new RuntimeException(ex3);
            }

            /*
            try {
            drawGeoJson(map,"sampledata/geojson.json");
            } catch (JSONException ex1) {
                throw new RuntimeException(ex1);
            } catch (IOException ex2) {
                throw new RuntimeException(ex2);
            }
            */
            /*
            try {
                GeoJsonOverlay geoJsonOverlay=new GeoJsonOverlay("sampledata/geojson.json");
                map.getOverlays().add(geoJsonOverlay);
            } catch (JSONException ex1) {
                throw new RuntimeException(ex1);
            } catch (IOException ex2) {
                throw new RuntimeException(ex2);
            }


            List<Overlay> list=map.getOverlays();
            for (Overlay item:list) {

            }
            */
        }
    }

    private void drawGeoJson(MapView map, String filename) throws JSONException, IOException{


            FileInputStream fileInputStream = new FileInputStream(filename);
            GeoJSONObject geoJSONObject = GeoJSON.parse(fileInputStream);
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


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    Intent i = getBaseContext().getPackageManager()
                            .getLaunchIntentForPackage( getBaseContext().getPackageName() );
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);


                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    showYouShouldGrant();

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void askPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
    }

    private void showYouShouldGrant() {
        Toast.makeText(MainActivity.this,"You should write external storage!",Toast.LENGTH_LONG).show();
    }

}
