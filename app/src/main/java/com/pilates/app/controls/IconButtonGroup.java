package com.pilates.app.controls;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pilates.app.R;
import com.pilates.app.controls.listeners.OnButtonClickListener;

public class IconButtonGroup extends LinearLayout {
    private OnButtonClickListener buttonListener;

    public IconButtonGroup(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_HORIZONTAL);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.icon_button_group_layout, this, true);

    }

    void UpdateButtons(String tag) {
        for(int i = 0; i < getChildCount(); i++) {
            if(!(getChildAt(i) instanceof IconButton))
                continue;

            IconButton btn = (IconButton)getChildAt(i);
            btn.SetActive(btn.getDataTag().equals(tag));
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        for(int i = 0; i < getChildCount(); i++) {
            if(!(getChildAt(i) instanceof IconButton))
                continue;

            IconButton btn = (IconButton) getChildAt(i);
            btn.setOnButtonClickListener(new OnButtonClickListener() {
                @Override
                public void clicked(IconButton button) {
                    UpdateButtons(button.getDataTag());
                    buttonListener.clicked(button);
                }
            });
        }

        UpdateButtons("me_small");
    }

    public void setButtonListener(OnButtonClickListener listener) {
        buttonListener = listener;
    }
}
