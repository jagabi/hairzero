package com.android.volley.error;

import com.android.volley.NetworkResponse;

public class NoConnectionError extends NetworkError {
    public NoConnectionError() {
    }

    public NoConnectionError(NetworkResponse networkResponse) {
        super(networkResponse);
    }

    public NoConnectionError(NetworkResponse networkResponse, Throwable reason) {
        super(networkResponse, reason);
    }

    public NoConnectionError(Throwable reason) {
        super(reason);
    }
}
