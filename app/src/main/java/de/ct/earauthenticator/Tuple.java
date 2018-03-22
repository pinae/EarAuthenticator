package de.ct.earauthenticator;

/**
 * Created by pmk on 13.03.18.
 */

class Tuple {
    public float x;
    public float y;
    public Tuple(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getDistance(Tuple p) {
        return (float) Math.sqrt((x-p.x)*(x-p.x)+(y-p.y)*(y-p.y));
    }
}