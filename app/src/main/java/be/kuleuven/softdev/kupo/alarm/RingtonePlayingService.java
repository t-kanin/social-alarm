package be.kuleuven.softdev.kupo.alarm;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

// this class is used as a service for keeping the ringtone playing in the background once the alarm has rung

public class RingtonePlayingService extends Service {

    private MediaPlayer mediaPlayer;
    private DatabaseReference rootRef;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        rootRef = FirebaseDatabase.getInstance().getReference().child("Users");

        // get info from the previous intent
        String displayName = intent.getExtras().getString("displayName");
        String wakeUpText = intent.getExtras().getString("wakeUpText");
        String state = intent.getExtras().getString("extra");
        String image = intent.getExtras().getString("image");

        // send info to next intent
        Intent toPopupActivity = new Intent(this.getApplicationContext(), AlarmPopupActivity.class);
        toPopupActivity.putExtra("displayName", displayName);
        toPopupActivity.putExtra("wakeUpText", wakeUpText);
        toPopupActivity.putExtra("image", image);

        assert state != null;
        switch (state) {
            case "no":
                // stop the sound
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                }
                break;
            case "yes":
                // create an alarm sound
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                }
                mediaPlayer = MediaPlayer.create(this, R.raw.morning_flower);
                mediaPlayer.start();
                mediaPlayer.setLooping(true);

                // vibrate the phone
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                long[] pattern = {0, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000}; // wait 0, vibrate 1000, stop 1000 repeat 4 times
                vibrator.vibrate(pattern, -1); // don't repeat the sequence

                // set 'woken' to true
                rootRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("woken").setValue(true);

                // open up pop up activity when service is called
                toPopupActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(toPopupActivity);
                break;
        }

        // don't restart the service
        return START_NOT_STICKY;
    }

}
