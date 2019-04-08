package com.txusballesteros.bubbles;

import android.view.animation.Interpolator;

/**
 * Created by lazarristic on 08/04/2019.
 * Copyright (c) 2019 bubbles-for-android. All rights reserved.
 */
public class BubbleBounceInterpolator implements Interpolator {

    private double amplitude = 1;
    private double frequency = 10;

    BubbleBounceInterpolator(double amplitude, double frequency) {
        this.amplitude = amplitude;
        this.frequency = frequency;
    }

    @Override
    public float getInterpolation(float time) {
        return (float) (-1 * Math.pow(Math.E, -time/ amplitude) * Math.cos(frequency + time) + 1);
    }
}
