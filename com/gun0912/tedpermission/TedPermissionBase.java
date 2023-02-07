package com.gun0912.tedpermission;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.List;

public abstract class TedPermissionBase {
    private static final String PREFS_IS_FIRST_REQUEST = "IS_FIRST_REQUEST";
    private static final String PREFS_NAME_PERMISSION = "PREFS_NAME_PERMISSION";
    public static final int REQ_CODE_REQUEST_SETTING = 2000;

    public static boolean isGranted(Context context, String... permissions) {
        for (String permission : permissions) {
            if (isDenied(context, permission)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isDenied(Context context, String permission) {
        return !isGranted(context, permission);
    }

    private static boolean isGranted(Context context, String permission) {
        if (permission.equals("android.permission.SYSTEM_ALERT_WINDOW")) {
            if (Build.VERSION.SDK_INT >= 23) {
                return Settings.canDrawOverlays(context);
            }
            return true;
        } else if (ContextCompat.checkSelfPermission(context, permission) == 0) {
            return true;
        } else {
            return false;
        }
    }

    public static List<String> getDeniedPermissions(Context context, String... permissions) {
        List<String> deniedPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (isDenied(context, permission)) {
                deniedPermissions.add(permission);
            }
        }
        return deniedPermissions;
    }

    public static boolean canRequestPermission(Activity activity, String... permissions) {
        if (isFirstRequest((Context) activity, permissions)) {
            return true;
        }
        for (String permission : permissions) {
            boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
            if (isDenied(activity, permission) && !showRationale) {
                return false;
            }
        }
        return true;
    }

    private static boolean isFirstRequest(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (!isFirstRequest(context, permission)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isFirstRequest(Context context, String permission) {
        return getSharedPreferences(context).getBoolean(getPrefsNamePermission(permission), true);
    }

    private static String getPrefsNamePermission(String permission) {
        return "IS_FIRST_REQUEST_" + permission;
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME_PERMISSION, 0);
    }

    public static void startSettingActivityForResult(Activity activity) {
        startSettingActivityForResult(activity, 2000);
    }

    public static void startSettingActivityForResult(Activity activity, int requestCode) {
        activity.startActivityForResult(getSettingIntent(activity), requestCode);
    }

    public static Intent getSettingIntent(Context context) {
        return new Intent("android.settings.APPLICATION_DETAILS_SETTINGS").setData(Uri.parse("package:" + context.getPackageName()));
    }

    public static void startSettingActivityForResult(Fragment fragment) {
        startSettingActivityForResult(fragment, 2000);
    }

    public static void startSettingActivityForResult(Fragment fragment, int requestCode) {
        fragment.startActivityForResult(getSettingIntent(fragment.getActivity()), requestCode);
    }

    static void setFirstRequest(Context context, String[] permissions) {
        for (String permission : permissions) {
            setFirstRequest(context, permission);
        }
    }

    private static void setFirstRequest(Context context, String permission) {
        getSharedPreferences(context).edit().putBoolean(getPrefsNamePermission(permission), false).apply();
    }
}
