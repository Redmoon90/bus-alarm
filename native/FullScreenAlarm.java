package com.busalarm.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/**
 * 전체화면 알람 트리거 플러그인.
 * 화면 꺼짐/잠금 시 full-screen intent 로 AlarmActivity 를 화면 가득 띄운다.
 * (화면 켜짐+잠금해제 상태에서는 안드로이드 정책상 큰 헤드업 알림으로 표시됨)
 */
@CapacitorPlugin(name = "FullScreenAlarm")
public class FullScreenAlarm extends Plugin {

    private static final String CHANNEL_ID = "bus-fullscreen-alarm";

    @PluginMethod
    public void trigger(PluginCall call) {
        String title = call.getString("title", "🚌 하차 알림!");
        String body  = call.getString("body", "곧 도착합니다");
        Context ctx = getContext();

        NotificationManager nm =
            (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                CHANNEL_ID, "하차 전체화면 알람", NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("목적지 도착 시 전체화면 알람");
            ch.enableVibration(true);
            ch.setVibrationPattern(new long[]{0, 800, 400, 800, 400, 800});
            nm.createNotificationChannel(ch);
        }

        Intent fsIntent = new Intent(ctx, AlarmActivity.class);
        fsIntent.putExtra("title", title);
        fsIntent.putExtra("body", body);
        fsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int piFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            piFlags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pi = PendingIntent.getActivity(ctx, 0, fsIntent, piFlags);

        Notification.Builder b = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            ? new Notification.Builder(ctx, CHANNEL_ID)
            : new Notification.Builder(ctx);

        b.setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
         .setContentTitle(title)
         .setContentText(body)
         .setCategory(Notification.CATEGORY_ALARM)
         .setAutoCancel(true)
         .setFullScreenIntent(pi, true);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            b.setPriority(Notification.PRIORITY_MAX);
        }

        nm.notify(7777, b.build());

        // 잠금화면/화면꺼짐이면 full-screen intent 가 액티비티를 직접 띄움.
        // (잠금해제+다른앱 사용중이면 백그라운드 액티비티 시작이 막혀 헤드업으로 표시됨)
        try { ctx.startActivity(fsIntent); } catch (Exception e) {}

        call.resolve();
    }
}
