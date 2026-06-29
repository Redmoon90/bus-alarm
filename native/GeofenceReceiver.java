package com.busalarm.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

/**
 * OS가 지오펜스 진입을 감지하면 호출됨 (앱이 꺼져 있어도).
 * → 전체화면 알람을 띄운다.
 */
public class GeofenceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event == null || event.hasError()) return;
        if (event.getGeofenceTransition() != Geofence.GEOFENCE_TRANSITION_ENTER) return;

        // 진입한 지오펜스 중 가장 작은 반경(가까운 것) 찾기
        int minR = Integer.MAX_VALUE;
        List<Geofence> triggering = event.getTriggeringGeofences();
        if (triggering != null) {
            for (Geofence g : triggering) {
                try {
                    int r = Integer.parseInt(g.getRequestId().replace("dest_", ""));
                    if (r < minR) minR = r;
                } catch (Exception e) {}
            }
        }

        SharedPreferences sp = context.getSharedPreferences("bus_alarm_prefs", Context.MODE_PRIVATE);
        String name = sp.getString("dest_name", "목적지");

        String title = (minR <= 100) ? "🚨 곧 도착! 내릴 준비!" : "🚌 하차 " + minR + "m 전";
        String body = name + ((minR != Integer.MAX_VALUE) ? " — 약 " + minR + "m 남음" : " 근처 도착");

        AlarmNotifier.fire(context, title, body);
    }
}
