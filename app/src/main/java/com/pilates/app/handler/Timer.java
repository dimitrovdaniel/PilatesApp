package com.pilates.app.handler;

import android.os.CountDownTimer;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pilates.app.UserRegistry;
import com.pilates.app.model.Action;
import com.pilates.app.model.ActionType;
import com.pilates.app.model.UserRole;
import com.pilates.app.model.UserSession;
import com.pilates.app.ws.SignalingWebSocket;

import java.util.Objects;

public class Timer {
    private TextView progressText;
    private ProgressBar progressBar;
    private long startTime;
    private CustomTimer countDownTimer;

    public Timer(final ProgressBar progressBar) {
        this.progressBar = progressBar;

    }

    public Timer(final ProgressBar progressBar, final TextView tvProgressText) {
        this.progressBar = progressBar;
        this.progressText = tvProgressText;

    }
    public void start(long millisInFuture, long countDownInterval) {
        countDownTimer= new CustomTimer(millisInFuture, countDownInterval);
        startTime = millisInFuture;
        countDownTimer.start();
    }

    public void stop() {
        if (Objects.nonNull(countDownTimer)) {
            countDownTimer.cancel();
        }
    }

    private class CustomTimer extends CountDownTimer {

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public CustomTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            System.out.println("TIMER: " + millisUntilFinished);
            progressBar.setProgress((int) ((millisUntilFinished / startTime) * 100));

            int seconds = (int)(millisUntilFinished / 1000);
            int mins = seconds / 60;
            int secondsToMin = seconds - mins * 60;

            if(progressText != null)
                progressText.setText(String.format("00", mins) + ":" + String.format("00", secondsToMin));
        }

        @Override
        public void onFinish() {
            final UserSession user = UserRegistry.getInstance().getUser();
            if (Objects.equals(user.getRole(), UserRole.TRAINER)) {
                SignalingWebSocket.getInstance().sendMessage(new Action(ActionType.NEXT));
            }
            System.out.println("TIMER FINISHED");
        }
    }
}
