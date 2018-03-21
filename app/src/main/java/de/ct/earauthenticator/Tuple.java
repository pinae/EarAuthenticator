package de.ct.earauthenticator;

/**
 * Created by pmk on 13.03.18.
 */

class Tuple {
    public final float x;
    public final float y;
    public Tuple(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public double getDistance(Tuple p) {
        return Math.sqrt((x-p.x)*(x-p.x)+(y-p.y)*(y-p.y));
    }
}