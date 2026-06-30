package com.busalarm.app;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
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
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 화면 꺼짐/잠금 상태에서도 화면을 꽉 채우는 알람 화면.
 * 그라데이션 배경 + 큰 버스 아이콘(펄스) + 둥근 끄기 버튼 + 알람벨/진동 반복.
 */
public class AlarmActivity extends Activity {
    private Ringtone ringtone;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        if (title == null) title = "🚌 도착!";
        if (body  == null) body  = "곧 도착합니다";

        float d = getResources().getDisplayMetrics().density;
        int pad = (int)(28 * d);

        // 그라데이션 배경
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{ Color.parseColor("#ef4444"), Color.parseColor("#b91c1c") });
        root.setBackground(bg);
        root.setPadding(pad, pad, pad, pad);

        // 큰 버스 아이콘 (반투명 원형 배경)
        TextView icon = new TextView(this);
        icon.setText("🚌");
        icon.setTextSize(70);
        icon.setGravity(Gravity.CENTER);
        GradientDrawable cbg = new GradientDrawable();
        cbg.setShape(GradientDrawable.OVAL);
        cbg.setColor(Color.parseColor("#33FFFFFF"));
        icon.setBackground(cbg);
        int circle = (int)(150 * d);
        icon.setLayoutParams(new LinearLayout.LayoutParams(circle, circle));

        // 제목
        TextView tv = new TextView(this);
        tv.setText(title);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(32);
        tv.setGravity(Gravity.CENTER);
        tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
        LinearLayout.LayoutParams tvlp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tvlp.topMargin = (int)(28 * d);
        tv.setLayoutParams(tvlp);

        // 본문
        TextView bv = new TextView(this);
        bv.setText(body);
        bv.setTextColor(Color.parseColor("#FFE4E6"));
        bv.setTextSize(18);
        bv.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams bvlp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bvlp.topMargin = (int)(12 * d);
        bv.setLayoutParams(bvlp);

        // 둥근 흰색 끄기 버튼
        Button stop = new Button(this);
        stop.setText("알람 끄기");
        stop.setTextSize(20);
        stop.setAllCaps(false);
        stop.setTextColor(Color.parseColor("#b91c1c"));
        stop.setTypeface(stop.getTypeface(), Typeface.BOLD);
        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setColor(Color.WHITE);
        btnBg.setCornerRadius(50 * d);
        stop.setBackground(btnBg);
        int bw = (int)(44 * d), bh = (int)(16 * d);
        stop.setPadding(bw, bh, bw, bh);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        slp.topMargin = (int)(52 * d);
        stop.setLayoutParams(slp);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { stopAlarm(); finish(); }
        });

        root.addView(icon);
        root.addView(tv);
        root.addView(bv);
        root.addView(stop);
        setContentView(root);

        // 아이콘 펄스 애니메이션
        try {
            ObjectAnimator sx = ObjectAnimator.ofFloat(icon, "scaleX", 1f, 1.15f);
            ObjectAnimator sy = ObjectAnimator.ofFloat(icon, "scaleY", 1f, 1.15f);
            sx.setRepeatCount(ObjectAnimator.INFINITE); sx.setRepeatMode(ObjectAnimator.REVERSE);
            sy.setRepeatCount(ObjectAnimator.INFINITE); sy.setRepeatMode(ObjectAnimator.REVERSE);
            sx.setDuration(650); sy.setDuration(650);
            sx.setInterpolator(new LinearInterpolator());
            sx.start(); sy.start();
        } catch (Exception e) {}

        startAlarm();
    }

    private void startAlarm() {
        // 알람 스트림 볼륨을 최대로 (진동/무음 모드여도 알람은 울림)
        try {
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            int max = am.getStreamMaxVolume(AudioManager.STREAM_ALARM);
            am.setStreamVolume(AudioManager.STREAM_ALARM, max, 0);
        } catch (Exception e) {}

        try {
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (uri == null) uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            ringtone = RingtoneManager.getRingtone(getApplicationContext(), uri);
            // 알람 용도(STREAM_ALARM)로 재생 → 진동/무음 모드에서도 소리남
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ringtone.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build());
            }
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
