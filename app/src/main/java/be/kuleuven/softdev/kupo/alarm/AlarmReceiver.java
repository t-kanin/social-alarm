package be.kuleuven.softdev.kupo.alarm;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import static android.content.Context.KEYGUARD_SERVICE;
import static android.content.Context.POWER_SERVICE;

// this is the class which will get fired when the alarm rings
// receive broadcast from alarm manager

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        // get info from previous intent
        String state = intent.getExtras().getString("extra");
        String displayName = intent.getExtras().getString("wakingName");
        String wakeUpText = intent.getExtras().getString("wakeUpText");
        String image = intent.getExtras().getString("wakingImage");

        // disable lock
        KeyguardManager km = (KeyguardManager) context.getSystemService(KEYGUARD_SERVICE);
        final KeyguardManager.KeyguardLock kl=km.newKeyguardLock("My_App");
        kl.disableKeyguard();

        // force open the screen
        PowerManager pm = (PowerManager) context.getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wl=pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK, "My_App");
        wl.acquire();
        wl.release();

        Intent serviceIntent = new Intent(context, RingtonePlayingService.class);

        serviceIntent.putExtra("extra", state);
        serviceIntent.putExtra("displayName", displayName);
        serviceIntent.putExtra("wakeUpText", wakeUpText);
        serviceIntent.putExtra("image", image);

        context.startService(serviceIntent);
    }

}
