package com.aegismesh.activities;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class EmergencyContactRowView extends LinearLayout {

    public static View newInstance(Context context) {
        return new EmergencyContactRowView(context);
    }

    public EmergencyContactRowView(Context context) {
        super(context);
        init(context);
    }

    public EmergencyContactRowView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        // Placeholder implementation
        setOrientation(HORIZONTAL);
    }
}
