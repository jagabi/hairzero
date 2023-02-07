package com.android.volley.error;

import com.android.volley.NetworkResponse;

public class ServerError extends VolleyError {
    public ServerError(NetworkResponse networkResponse) {
        super(networkResponse);
    }

    public ServerError() {
    }
}
