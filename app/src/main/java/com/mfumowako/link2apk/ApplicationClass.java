package com.mfumowako.link2apk;

import android.app.Application;
import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;

public class ApplicationClass extends Application {

    // Kitambulisho chako rasmi cha OneSignal
    private static final String ONESIGNAL_APP_ID = "54fbaf94-92dc-4d92-b2ee-7a7d3f1754d0";

    @Override
    public void onCreate() {
        super.onCreate();

        // Washa ripoti za kiufundi kwa ajili ya kuona makosa ya mfumo
        OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE);

        // Kuwasha rasmi mfumo wa OneSignal kwenye simu ya mteja
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID);
        
        // KUMBUKA: Ruhusa ya Notification tunaomba kitalaamu kule kwenye MainActivity.java
        // ili kuzuia fujo za compiler wa Java na Kotlin hapa.
    }
}
