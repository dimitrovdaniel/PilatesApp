package com.pilates.app.controls.listeners;

import com.pilates.app.controls.IconButton;
import com.pilates.app.controls.TestButton;

public interface OnTestButtonListener {
    void clicked(TestButton.TestButtonState state);
    void progressCompleted();
    void progressTick();
}
