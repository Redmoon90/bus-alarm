package com.busalarm.app;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(FullScreenAlarm.class);
        registerPlugin(GeofencePlugin.class);
        super.onCreate(savedInstanceState);
    }
}
