package it.uniparthenope.GribDroid.GeoJSON;

/**
 * Created by pasqualecelardo on 01/12/17.
 */

public class getInfoPolygonResults {

    private String stroke;
    private int stroke_width;
    private int stroke_opacity;
    private String fill ;
    private double fill_opacity;

    public getInfoPolygonResults(String stroke, int stroke_width, int stroke_opacity, String fill, double fill_opacity){
        this.stroke = stroke;
        this.stroke_width = stroke_width;
        this.stroke_opacity = stroke_opacity;
        this.fill = fill;
        this.fill_opacity = fill_opacity;
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

    public String getFill() {
        return fill;
    }

    public double getFill_opacity() {
        return fill_opacity;
    }
}
