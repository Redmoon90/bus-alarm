package com.busalarm.app;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import com.getcapacitor.JSArray;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 목적지 주변에 안드로이드 OS 지오펜스를 등록.
 * 앱이 꺼져 있어도 OS가 도착을 감지해 GeofenceReceiver 를 호출 → 전체화면 알람.
 */
@CapacitorPlugin(name = "BusGeofence")
public class GeofencePlugin extends Plugin {

    private static final List<String> ALL_IDS =
        Arrays.asList("dest_300", "dest_100", "dest_50", "dest_10", "dest_200");

    @PluginMethod
    public void register(PluginCall call) {
        final Double lat = call.getDouble("lat");
        final Double lng = call.getDouble("lng");
        final String name = call.getString("name", "목적지");
        if (lat == null || lng == null) { call.reject("lat/lng 필요"); return; }

        List<Float> radii = new ArrayList<>();
        try {
            JSArray arr = call.getArray("radii");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    radii.add((float) arr.getDouble(i));
                }
            }
        } catch (Exception e) {}
        if (radii.isEmpty()) { radii.add(300f); radii.add(100f); }

        Context ctx = getContext();

        // 목적지 이름 저장 (수신기에서 사용)
        SharedPreferences sp = ctx.getSharedPreferences("bus_alarm_prefs", Context.MODE_PRIVATE);
        sp.edit().putString("dest_name", name).apply();

        GeofencingClient client = LocationServices.getGeofencingClient(ctx);

        // 기존 지오펜스 제거 후 새로 등록
        try { client.removeGeofences(ALL_IDS); } catch (Exception e) {}

        List<Geofence> fences = new ArrayList<>();
        for (Float r : radii) {
            fences.add(new Geofence.Builder()
                .setRequestId("dest_" + Math.round(r))
                .setCircularRegion(lat, lng, r)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setNotificationResponsiveness(0)   // 가능한 한 즉시 발동
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build());
        }

        GeofencingRequest req = new GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(fences)
            .build();

        Intent intent = new Intent(ctx, GeofenceReceiver.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) flags |= PendingIntent.FLAG_MUTABLE;
        PendingIntent pi = PendingIntent.getBroadcast(ctx, 0, intent, flags);

        try {
            client.addGeofences(req, pi)
                .addOnSuccessListener(a -> call.resolve())
                .addOnFailureListener(e -> call.reject("지오펜스 등록 실패: " + e.getMessage()));
        } catch (SecurityException e) {
            call.reject("위치 권한 필요(항상 허용): " + e.getMessage());
        }
    }

    @PluginMethod
    public void remove(PluginCall call) {
        try {
            LocationServices.getGeofencingClient(getContext()).removeGeofences(ALL_IDS);
        } catch (Exception e) {}
        call.resolve();
    }
}
