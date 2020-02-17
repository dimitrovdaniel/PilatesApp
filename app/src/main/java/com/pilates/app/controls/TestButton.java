package com.pilates.app.controls;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pilates.app.R;
import com.pilates.app.controls.listeners.OnTestButtonListener;
import com.pilates.app.model.MediaStats;

import androidx.annotation.Nullable;

public class TestButton extends FrameLayout {
    private CountDownTimer timer;
    private ProgressBar progress;
    private int iconSuccess;
    private int iconError;
    private int iconDefault;
    private ImageView testIcon;
    private FrameLayout framePreClick;
    private String failLabel;
    private String successLabel;
    private String testingLabel;
    private TextView testText;
    private String tapToTestLabel;
    private String dataTag;
    private OnTestButtonListener listener;

    public boolean testComplete;
    public MediaStats lastMediaStats;

    public enum TestButtonState {
        Default, Testing, Success, Error
    }

    private float msSinceStartTest = 0f;
    private TestButtonState state = TestButtonState.Default;

    public TestButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_test_button, this, true);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.TestButton, 0, 0);
        tapToTestLabel = a.getString(R.styleable.TestButton_tapToTestLabel);
        testingLabel = a.getString(R.styleable.TestButton_testingLabel);
        successLabel = a.getString(R.styleable.TestButton_successLabel);
        failLabel = a.getString(R.styleable.TestButton_failLabel);

        iconDefault = a.getResourceId(R.styleable.TestButton_iconDefault, 0);
        iconSuccess = a.getResourceId(R.styleable.TestButton_iconSuccess, 0);
        iconError = a.getResourceId(R.styleable.TestButton_iconError, 0);
        a.recycle();
    }

    public void setTestSuccess(boolean isSuccess) {
        state = isSuccess ? TestButtonState.Success : TestButtonState.Error;
        setLookForState();

        testComplete = isSuccess;
    }

    private void setLookForState() {
        if(state == TestButtonState.Default) {
            testText.setText(tapToTestLabel);
            testText.setTextColor(((Activity) getContext()).getResources().getColor(R.color.colorPrimary, null));
            framePreClick.setVisibility(View.VISIBLE);
            testIcon.setImageResource(iconDefault);
        }
        else if(state == TestButtonState.Testing) {
            testText.setText(testingLabel);
            testText.setTextColor(((Activity) getContext()).getResources().getColor(R.color.labelDark, null));
            framePreClick.setVisibility(View.GONE);
            testIcon.setImageResource(iconDefault);
        }
        else if(state == TestButtonState.Success) {
            testText.setText(successLabel);
            testText.setTextColor(((Activity) getContext()).getResources().getColor(R.color.labelDark, null));
            framePreClick.setVisibility(View.GONE);
            testIcon.setImageResource(iconSuccess);
            progress.setProgress(100);
        }
        else if(state == TestButtonState.Error) {
            testText.setText(failLabel);
            testText.setTextColor(((Activity) getContext()).getResources().getColor(R.color.labelError, null));
            framePreClick.setVisibility(View.GONE);
            testIcon.setImageResource(iconError);
            progress.setProgress(100);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        testText = (TextView) findViewById(R.id.tvTestText);
        framePreClick = (FrameLayout)findViewById(R.id.framePreClick);
        testIcon = (ImageView)findViewById(R.id.imgTestIcon);
        progress = (ProgressBar)findViewById(R.id.pbBgProgress);

        OnClickListener startTimerOnClick = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(state != TestButtonState.Default)
                    return;

                state = TestButtonState.Testing;

                msSinceStartTest = 0f;
                lastMediaStats = null;
                timer.start();

                if(listener != null)
                    listener.clicked(state);

                setLookForState();
            }
        };

        testText.setOnClickListener(startTimerOnClick);
        testIcon.setOnClickListener(startTimerOnClick);
        framePreClick.setOnClickListener(startTimerOnClick);


        timer = new CountDownTimer(10000, 50) {

            @Override
            public void onTick(long millisUntilFinished) {
                msSinceStartTest += millisUntilFinished;
                progress.setProgress((int)((millisUntilFinished / 10000.0) * 100));

                if(msSinceStartTest >= 2000)
                    listener.progressTick();
            }

            @Override
            public void onFinish() {
                if(listener != null)
                    listener.progressCompleted();
            }
        };
        progress.setProgress(100);
        setLookForState();

        //todo create new timer which requests stats each 1-2 sec
    }

    public void setListener(OnTestButtonListener listener) {
        this.listener = listener;
    }

    public void setLastMediaStats(MediaStats lastMediaStats) {
        this.lastMediaStats = lastMediaStats;
    }
}
