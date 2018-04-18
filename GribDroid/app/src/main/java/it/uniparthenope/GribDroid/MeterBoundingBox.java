package it.uniparthenope.GribDroid;

/**
 * Created by pasqualecelardo on 09/01/2018.
 */

public class MeterBoundingBox {
    private MeterPoint lowerLeft;
    private MeterPoint upperRight;

    public MeterPoint getLowerLeft() {
        return lowerLeft;
    }

    public MeterPoint getUpperRight() {
        return upperRight;
    }

    public MeterBoundingBox(MeterPoint lowerLeft, MeterPoint upperRight) {
        this.lowerLeft=lowerLeft;
        this.upperRight=upperRight;
    }
}
