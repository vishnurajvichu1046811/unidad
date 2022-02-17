package com.utracx.util.helper;

import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

public final class ViewAnimator {

    private static final Animation ROTATE_ANIMATION = new RotateAnimation(
            0.0f,
            360.0f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
    );

    private ViewAnimator() {
    }

    public static Animation getRotatingAnimation(int repeatCount, long durationMillis) {
        ROTATE_ANIMATION.setInterpolator(new AccelerateDecelerateInterpolator());
        ROTATE_ANIMATION.setRepeatCount(repeatCount);
        ROTATE_ANIMATION.setDuration(durationMillis);

        return ROTATE_ANIMATION;
    }
}
