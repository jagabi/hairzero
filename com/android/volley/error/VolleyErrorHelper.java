package com.android.volley.error;

import android.content.Context;
import androidx.constraintlayout.core.motion.utils.TypedValues;
import com.android.volley.C0109R;
import com.android.volley.NetworkResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.HashMap;
import java.util.Map;

public class VolleyErrorHelper {
    public static String getMessage(Object error, Context context) {
        if (error instanceof TimeoutError) {
            return context.getResources().getString(C0109R.string.generic_server_down);
        }
        if (isServerProblem(error)) {
            return handleServerError(error, context);
        }
        if (isNetworkProblem(error)) {
            return context.getResources().getString(C0109R.string.no_internet);
        }
        return context.getResources().getString(C0109R.string.generic_error);
    }

    public static String getErrorType(Object error, Context context) {
        if (error instanceof TimeoutError) {
            return context.getResources().getString(C0109R.string.generic_server_timeout);
        }
        if (error instanceof ServerError) {
            return context.getResources().getString(C0109R.string.generic_server_down);
        }
        if (error instanceof AuthFailureError) {
            return context.getResources().getString(C0109R.string.auth_failed);
        }
        if (error instanceof NetworkError) {
            return context.getResources().getString(C0109R.string.no_internet);
        }
        if (error instanceof NoConnectionError) {
            return context.getResources().getString(C0109R.string.no_network_connection);
        }
        if (error instanceof ParseError) {
            return context.getResources().getString(C0109R.string.parsing_failed);
        }
        return context.getResources().getString(C0109R.string.generic_error);
    }

    private static boolean isNetworkProblem(Object error) {
        return (error instanceof NetworkError) || (error instanceof NoConnectionError);
    }

    private static boolean isServerProblem(Object error) {
        return (error instanceof ServerError) || (error instanceof AuthFailureError);
    }

    private static String handleServerError(Object err, Context context) {
        VolleyError error = (VolleyError) err;
        NetworkResponse response = error.networkResponse;
        if (response == null) {
            return context.getResources().getString(C0109R.string.generic_error);
        }
        switch (response.statusCode) {
            case TypedValues.CycleType.TYPE_CURVE_FIT:
            case 404:
            case TypedValues.CycleType.TYPE_CUSTOM_WAVE_SHAPE:
                try {
                    HashMap<String, String> result = (HashMap) new Gson().fromJson(new String(response.data), new TypeToken<Map<String, String>>() {
                    }.getType());
                    if (result != null && result.containsKey("error")) {
                        return result.get("error");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return error.getMessage();
            default:
                return context.getResources().getString(C0109R.string.generic_server_down);
        }
    }
}
