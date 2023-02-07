package com.android.volley.toolbox;

import com.android.volley.error.AuthFailureError;

public interface Authenticator {
    String getAuthToken() throws AuthFailureError;

    void invalidateAuthToken(String str);
}
