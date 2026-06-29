package com.busalarm.app;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/**
 * JS에서 호출하는 전체화면 알람 트리거. 실제 동작은 AlarmNotifier 가 처리.
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
}
