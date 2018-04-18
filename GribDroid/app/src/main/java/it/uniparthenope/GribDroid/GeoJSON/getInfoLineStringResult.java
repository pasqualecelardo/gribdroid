package it.uniparthenope.GribDroid.GeoJSON;

/**
 * Created by pasqualecelardo on 01/12/17.
 */

public class getInfoLineStringResult {

    String stroke;
    int stroke_width;
    int stroke_opacity;

    public getInfoLineStringResult(String stroke, int stroke_width, int stroke_opacity)
    {
        this.stroke=stroke;
        this.stroke_opacity=stroke_opacity;
        this.stroke_width=stroke_width;
    }

    public String getStroke() {
        return stroke;
    }

    public int getStroke_width() {
        return stroke_width;
    }

    public int getStroke_opacity() {
        return stroke_opacity;
    }
}
