package com.xiaosu.lib.base.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Interpolator;


public class EaseElasticOutInterpolator implements Interpolator {

    private float mDuration;

    /**
     * @param durationMillis Duration in milliseconds. The duration cannot be negative.
     * @throws IllegalArgumentException if the duration is < 0
     */
    public EaseElasticOutInterpolator(float durationMillis) {
        if (durationMillis < 0) {
            throw new IllegalArgumentException("Animation duration cannot be negative");
        }
        mDuration = durationMillis / 1000f;
    }

    public EaseElasticOutInterpolator(Context context, AttributeSet attrs) {
    }

    public float getInterpolation(float input) {
        float s;
        float p = 0.0f;
        float a = 0.0f;
        if (input == 0) {
            return 0;
        }
        if (input == 1) {
            return 1;
        }
        if (p == 0) {
            p = mDuration * 0.3f;
        }
        if (a == 0 || (1 > 0 && a < 1) || (1 < 0 /*&& a < -1*/)) {
            a = 1;
            s = p / 4;
        } else {
            s = (float) (p / Math.PI * 2.0f * Math.asin(1 / a));
        }
        return (float) (a * Math.pow(2, -10 * input) * Math.sin((input * mDuration - s) * Math.PI * 2.0f / p) + 1 + 0);
    }
}
