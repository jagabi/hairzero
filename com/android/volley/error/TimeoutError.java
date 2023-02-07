package com.android.volley.error;

import com.android.volley.NetworkResponse;
import java.util.Map;

public class TimeoutError extends VolleyError {
    public TimeoutError() {
        super(new NetworkResponse(-1, (byte[]) null, (Map<String, String>) null, false));
    }
}
