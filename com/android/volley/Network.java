package com.android.volley;

import com.android.volley.error.VolleyError;

public interface Network {
    NetworkResponse performRequest(Request<?> request) throws VolleyError;
}
