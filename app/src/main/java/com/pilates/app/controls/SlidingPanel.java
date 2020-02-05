package com.pilates.app.controls;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.pilates.app.R;
import com.pilates.app.controls.listeners.OnButtonClickListener;
import com.pilates.app.controls.listeners.OnSlidingPanelEventListener;

import androidx.annotation.NonNull;

public class SlidingPanel extends FrameLayout {
    private OnSlidingPanelEventListener listener;

    public SlidingPanel(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_slide_panel, this, true);

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int height = displayMetrics.heightPixels;
        getThisView().setY(height * 1.2f);

        findViewById(R.id.btnClosePanel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getThisView().animate().y(height * 1.2f);
            }
        });

        ((IconButtonGroup)findViewById(R.id.ibgLayout)).setButtonListener(new OnButtonClickListener() {
            @Override
            public void clicked(IconButton button) {
                listener.changeLayout(button.getDataTag());
            }
        });
    }

    private FrameLayout getThisView() {
        return this;
    }

    public void setListener(OnSlidingPanelEventListener listener) {
        this.listener = listener;
    }

    public void showPanel() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;

        getParent().requestTransparentRegion(this);

        FrameLayout fds = findViewById(R.id.frameDisplaySettings);
        fds.animate().y(height * 0.2f).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                getParent().requestTransparentRegion(getThisView());
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }
}
