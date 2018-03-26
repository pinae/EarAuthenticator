package de.ct.earauthenticator;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ear data sets wrap 5 values: height, and two dimensional values to the right an left
 */

public class EarDataset {
    public float h = 500;
    public float minDw = 0;
    public float minDwDh = 0;
    public float maxDw = 0;
    public float maxDwDh = 0;

    public EarDataset(float h, float minDw, float minDwDh, float maxDw, float maxDwDh) {
        this.h = h;
        this.minDw = minDw;
        this.minDwDh = minDwDh;
        this.maxDw = maxDw;
        this.maxDwDh = maxDwDh;
    }

    public EarDataset(String serializedDataset) {
        Pattern p = Pattern.compile("^h:\\s*(-?\\d+\\.?(?:\\d*)?)\\s*," +
                "\\s+min d_w:\\s*(-?\\d+\\.?(?:\\d*)?)\\s*," +
                "\\s*min d_w_d_h:\\s*(-?\\d+\\.?(?:\\d*)?)\\s*," +
                "\\s*max d_w:\\s+(-?\\d+\\.?(?:\\d*)?)\\s*," +
                "\\s*max d_w_d_h:\\s*(-?\\d+\\.?(?:\\d*)?)\\s*$");
        Matcher m = p.matcher(serializedDataset);
        if (m.matches()) {
            this.h = Float.parseFloat(m.group(1));
            this.minDw = Float.parseFloat(m.group(2));
            this.minDwDh = Float.parseFloat(m.group(3));
            this.maxDw = Float.parseFloat(m.group(4));
            this.maxDwDh = Float.parseFloat(m.group(5));
        } else {
            Log.e("Loading ear data failed", serializedDataset);
        }
    }

    private float sq(float x) {
        return x*x;
    }

    public float squaredNormalizedError(EarDataset x) {
        return sq(h/900-x.h/900) +
                sq(minDw-x.minDw) + sq(minDwDh-x.minDwDh) +
                sq(maxDw-x.maxDw) + sq(maxDwDh-x.maxDwDh);
    }

    public String toString() {
        return "h: " + Double.toString(h) +
                " , min d_w: " + Double.toString(minDw) +
                " , min d_w_d_h: " + Double.toString(minDwDh) +
                " , max d_w: " + Double.toString(maxDw) +
                " , max d_w_d_h: " + Double.toString(maxDwDh);
    }
}
