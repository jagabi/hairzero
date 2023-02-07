package com.android.volley.error;

import com.android.volley.NetworkResponse;

public class NetworkError extends VolleyError {
    public NetworkError() {
    }

    public NetworkError(Throwable cause) {
        super(cause);
    }

    public NetworkError(NetworkResponse networkResponse) {
        super(networkResponse);
    }

    public NetworkError(NetworkResponse networkResponse, Throwable reason) {
        super(networkResponse, reason);
    }
}
