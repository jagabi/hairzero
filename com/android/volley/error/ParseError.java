package com.android.volley.error;

import com.android.volley.NetworkResponse;

public class ParseError extends VolleyError {
    public ParseError() {
    }

    public ParseError(NetworkResponse networkResponse) {
        super(networkResponse);
    }

    public ParseError(String exceptionMessage) {
        super(exceptionMessage);
    }

    public ParseError(Throwable cause) {
        super(cause);
    }
}
