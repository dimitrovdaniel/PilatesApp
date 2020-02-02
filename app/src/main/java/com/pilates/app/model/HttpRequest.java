package com.pilates.app.model;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.pilates.app.model.dto.Request;

import java.nio.charset.StandardCharsets;

public class HttpRequest extends StringRequest {
    private Request request;

    public HttpRequest(int method, String url,
                       Response.Listener<String> listener,
                       @Nullable Response.ErrorListener errorListener,
                       final Request request) {
        super(method, url, listener, errorListener);
        this.request = request;
    }

    public HttpRequest(String url, Response.Listener<String> listener, @Nullable Response.ErrorListener errorListener) {
        super(url, listener, errorListener);
    }

    @Override
    public String getBodyContentType() {
        return "application/json; charset=utf-8";
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        final String body = request.toString();
        System.out.println("SENDING REQUEST: " + body);
        return body.getBytes(StandardCharsets.UTF_8);
    }
}
