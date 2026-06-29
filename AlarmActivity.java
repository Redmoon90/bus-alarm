package com.busalarm.app;

import android.app.Activity;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 화면 꺼짐/잠금 상태에서도 화면을 꽉 채우는 알람 화면.
 * 빨간 배경 + 큰 글씨 + 알람벨 반복 + 진동 반복 + "알람 끄기" 버튼.
 */
public class AlarmActivity extends Activity {
    private Ringtone ringtone;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 잠금화면 위에 표시 + 화면 켜기
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }
        getWindow().addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );

        String title = getIntent().getStringExtra("title");
        String body  = getIntent().getStringExtra("body");
        if (title == null) title = "🛌 하차 알람!";
        if (body  == null) body  = "곧 도착합니다";

        float d = getResources().getDisplayMetrics().density;
        int pad = (int)(24 * d);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.parseColor("#dc2626"));
        root.setPadding(pad, pad, pad, pad);

        TextView tv = new TextView(this);
        tv.setText(title);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(34);
        tv.setGravity(Gravity.CENTER);

        TextView bv = new TextView(this);
        bv.setText(body);
        bv.setTextColor(Color.WHITE);
        bv.setTextSize(20);
        bv.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams bvlp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bvlp.topMargin = pad;
        bv.setLayoutParams(bvlp);

        Button stop = new Button(this);
        stop.setText("알람 끄기");
        stop.setTextSize(22);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        slp.topMargin = pad * 2;
        stop.setLayoutParams(slp);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { stopAlarm(); finish(); }
        });

        root.addView(tv);
        root.addView(bv);
        root.addView(stop);
        setContentView(root);

        startAlarm();
    }

    private void startAlarm() {
        try {
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (uri == null) uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            ringtone = RingtoneManager.getRingtone(getApplicationContext(), uri);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ringtone.setLooping(true);
            }
            ringtone.play();
        } catch (Exception e) {}

        try {
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] pattern = {0, 800, 400, 800, 400, 800, 400};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
            } else {
                vibrator.vibrate(pattern, 0);
            }
        } catch (Exception e) {}
    }

    private void stopAlarm() {
        try { if (ringtone != null) ringtone.stop(); } catch (Exception e) {}
        try { if (vibrator != null) vibrator.cancel(); } catch (Exception e) {}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAlarm();
    }
}
