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

public class IconButton extends LinearLayout {
    private final int activeIcon;
    private final int inactiveIcon;
    private final String labelText;
    private final String dataTag;
    private OnButtonClickListener clickListener;

    public IconButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_HORIZONTAL);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.icon_button_layout, this, true);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.IconButton, 0, 0);
        activeIcon = a.getResourceId(R.styleable.IconButton_iconActive, 0);
        inactiveIcon = a.getResourceId(R.styleable.IconButton_iconInactive, 0);
        labelText = a.getString(R.styleable.IconButton_labelText);
        dataTag = a.getString(R.styleable.IconButton_dataTag);
        a.recycle();

        ((ImageView)findViewById(R.id.imgIcon)).setImageResource(inactiveIcon);
        ((TextView)findViewById(R.id.tvLabel)).setText(labelText);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SetActive(true);

                if(clickListener != null)
                    clickListener.clicked(getInstance());
            }
        });
    }

    IconButton getInstance() {
        return this;
    }

    public void setOnButtonClickListener(OnButtonClickListener listener) {
        clickListener = listener;
    }

    public String getDataTag() {
        return dataTag;
    }

    public void SetActive(boolean active) {
        ((ImageView)findViewById(R.id.imgIcon)).setImageResource(active ? activeIcon : inactiveIcon);
        ((TextView)findViewById(R.id.tvLabel)).setTextColor(getResources()
                .getColor(active ? R.color.colorPrimaryDark : R.color.labelDark, null));
    }
}
