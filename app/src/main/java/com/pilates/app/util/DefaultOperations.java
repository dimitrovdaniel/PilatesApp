package com.pilates.app.util;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.pilates.app.MainActivity;
import com.pilates.app.PostTraineeRegisterActivity;
import com.pilates.app.UserRegistry;
import com.pilates.app.model.Action;
import com.pilates.app.model.ActionBody;
import com.pilates.app.model.ActionType;
import com.pilates.app.model.UserRole;
import com.pilates.app.model.UserSession;
import com.pilates.app.model.dto.UserDto;
import com.pilates.app.ws.SignalingWebSocket;

import java.util.Objects;

public class DefaultOperations {

    public static void loginRegisterFlow(final Activity activity) {
        final UserSession user = UserRegistry.getInstance().getUser();
        final ActionBody body = ActionBody.newBuilder()
                .withInfoId(user.getInfoId())
                .withName(user.getName())
                .withRole(user.getRole()).build();
        SignalingWebSocket.getInstance().sendMessage(new Action(ActionType.REGISTER, body));

        activity.runOnUiThread(() -> {
            System.out.println("USER ROLE: " + user.getRole());
            if (Objects.equals(user.getRole(), UserRole.TRAINER)) {
                hideKeyboard(activity);
                activity.startActivity(new Intent(activity, MainActivity.class));
            } else {
                activity.startActivity(new Intent(activity, PostTraineeRegisterActivity.class));
            }
        });
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
