package com.mfumowako.link2apk;

import android.app.Application;
import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;

public class ApplicationClass extends Application {

    // Hapa tumeweka kile kitambulisho chako rasmi ulichonipa
    private static final String ONESIGNAL_APP_ID = "54fbaf94-92dc-4d92-b2ee-7a7d3f1754d0";

    @Override
    public void onCreate() {
        super.onCreate();

        // Washa ripoti za kiufundi kwa ajili ya kuona makosa (Unaweza kuifuta ikishakuwa sokoni)
        OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE);

        // Kuwasha rasmi mfumo wa OneSignal kwenye simu ya mteja
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID);

        // Kuomba ruhusa ya Notification kwa mfumo wa kisasa wa OneSignal v5
        OneSignal.getNotifications().requestPermission(true, continueWith -> {
            // Hapa inasubiri mtumiaji akubali au akatae
        });
    }
}
