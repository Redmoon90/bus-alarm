package com.busalarm.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * 전체화면 알람을 띄우는 공용 헬퍼.
 * JS(FullScreenAlarm) 와 지오펜스 수신기(GeofenceReceiver) 둘 다 사용.
 * 화면 꺼짐/잠금 시 full-screen intent 로 AlarmActivity 를 화면 가득 띄운다.
 */
public class AlarmNotifier {
    public static final String CHANNEL_ID = "bus-fullscreen-alarm";

    public static void fire(Context ctx, String title, String body) {
        if (title == null) title = "🚌 도착 알림!";
        if (body == null) body = "곧 도착합니다";

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

        // 화면 켜짐+잠금해제(앱 포그라운드)면 직접 띄움. (백그라운드면 막혀서 위 full-screen intent 가 처리)
        try { ctx.startActivity(fsIntent); } catch (Exception e) {}
    }
}
