package com.busalarm.app;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/**
 * JS에서 호출하는 전체화면 알람 트리거 + 전체화면 알림 권한 확인/요청.
 */
@CapacitorPlugin(name = "FullScreenAlarm")
public class FullScreenAlarm extends Plugin {

    @PluginMethod
    public void trigger(PluginCall call) {
        String title = call.getString("title", "🚌 도착 알림!");
        String body  = call.getString("body", "곧 도착합니다");
        AlarmNotifier.fire(getContext(), title, body);
        call.resolve();
    }

    // 전체화면 알림 권한이 켜져 있는지 (안드로이드 14+)
    @PluginMethod
    public void checkPermission(PluginCall call) {
        Context ctx = getContext();
        boolean granted = true;
        if (Build.VERSION.SDK_INT >= 34) {
            try {
                NotificationManager nm =
                    (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
                granted = nm.canUseFullScreenIntent();
            } catch (Exception e) { granted = false; }
        }
        JSObject ret = new JSObject();
        ret.put("granted", granted);
        call.resolve(ret);
    }

    // 전체화면 알림 권한 설정 화면 열기 (안드로이드 14+)
    @PluginMethod
    public void openPermissionSettings(PluginCall call) {
        Context ctx = getContext();
        try {
            Intent i;
            if (Build.VERSION.SDK_INT >= 34) {
                i = new Intent("android.settings.MANAGE_APP_USE_FULL_SCREEN_INTENT");
                i.setData(Uri.parse("package:" + ctx.getPackageName()));
            } else {
                i = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                i.setData(Uri.parse("package:" + ctx.getPackageName()));
            }
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(i);
        } catch (Exception e) {}
        call.resolve();
    }
}
