package com.guster.androidgeofence;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

/**
 * Created by Gusterwoei on 11/25/15.
 */
public class BehaviorLayout extends CoordinatorLayout.Behavior<View> {

    public BehaviorLayout() {
    }

    public BehaviorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof Button || dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        float y = Math.min(0, dependency.getTranslationY() - dependency.getHeight());

        child.findViewById(R.id.btn_start_stop).setTranslationY(y);
        child.findViewById(R.id.lyt_placeholder).setTranslationY(y);

        return true;
    }
}
