package be.kuleuven.softdev.kupo.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.support.constraint.Constraints.TAG;

// this is the first activity which will get fired when the user opens the app
// before the user goes to main activity, the app will first check if someone has already set the alarm to you
// if you are supposed to be woken up, then the system will set the alarm

public class LoginActivity extends AppCompatActivity {

    private EditText email, password;
    private FirebaseAuth auth;
    private ProgressBar progressBar;

    private DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Users");
    private User user;

    private PendingIntent pending_intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button signUp, login, reset;
        auth = FirebaseAuth.getInstance();

        // if auto login is true, then proceed directly to main activity
        SharedPreferences prfAutoLogin = PreferenceManager.getDefaultSharedPreferences(this);
        if (auth.getCurrentUser() != null) {
            if (prfAutoLogin.getBoolean("key_auto_login", true)) {
                setAlarmThenGoToMain();
            }
        }

        // set the layout for this activity
        setContentView(R.layout.activity_login);

        // find views
        email = findViewById(R.id.et_login_act_email);
        password = findViewById(R.id.et_login_act_password);
        progressBar = findViewById(R.id.pb_login_act_progressbar);
        signUp = findViewById(R.id.bt_login_act_signup);
        login = findViewById(R.id.bt_login_act_login);
        reset = findViewById(R.id.bt_login_act_resetpassword);

        // when sign up button is clicked, go to sign up activity
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        });

        // when reset password button is clicked, go to reset password activity
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
            }
        });

        // when login button is clicked, go to main activity
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = LoginActivity.this.email.getText().toString();
                // final because accessed from inner class
                final String password = LoginActivity.this.password.getText().toString();

                // if email is empty, show toast
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // if password is empty, show toast
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //authenticate user
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                progressBar.setVisibility(View.INVISIBLE);

                                // if no error, go to main activity
                                if (!task.isSuccessful()) {
                                    // if password is too short
                                    if (password.length() < 6) {
                                        LoginActivity.this.password.setError(getString(R.string.minimum_password));
                                    } else {
                                        Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    setAlarmThenGoToMain();
                                }
                            }
                        });
            }
        });
    }

    // set the alarm if 'waking' is true, otherwise go directly to main activity
    private void setAlarmThenGoToMain() {

        String userId = auth.getCurrentUser().getUid();

        // final because accessed from inner class
        final Calendar calendar = Calendar.getInstance();
        final Intent intent = new Intent(LoginActivity.this, AlarmReceiver.class);
        final AlarmManager alarmManager = (AlarmManager) LoginActivity.this.getSystemService(ALARM_SERVICE);

        // get info from current user
        rootRef.orderByKey().equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // get a user class
                for (DataSnapshot issue : dataSnapshot.getChildren()) {
                    user = issue.getValue(User.class);
                }

                // if someone wants to wake you, and the alarm hasn't rung yet, then set the alarm
                if (user.getWaking() && !user.getWoken()) {

                    // extract int from string 'timeDisplay'
                    String[] timeDisplay = user.getTimeDisplay().split(":");
                    int hour = Integer.parseInt(timeDisplay[0]);
                    int minute = Integer.parseInt(timeDisplay[1]);

                    // set the time to the calender
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);

                    // send info to another activity
                    intent.putExtra("extra", "yes");
                    intent.putExtra("wakingName", user.getWakingName());
                    intent.putExtra("wakeUpText", user.getWakeUpText());
                    intent.putExtra("wakingImage", user.getWakingImage());

                    // set the intent to use when the alarm rings
                    pending_intent = PendingIntent.getBroadcast(LoginActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    // set the alarm to ring at a specific time from the calender
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pending_intent);

                // if someone wants to wake you, but the alarm has already rung, then let the alarm continue to ring
                } else if (user.getWaking() && user.getWoken()) {
                    intent.putExtra("extra", "yes");
                    intent.putExtra("wakingName", user.getWakingName());
                    intent.putExtra("wakeUpText", user.getWakeUpText());
                    intent.putExtra("wakingImage", user.getWakingImage());
                    getApplicationContext().sendBroadcast(intent);

                } else {
                    // end the alarm if it is ringing
                    intent.putExtra("extra", "no");
                    getApplicationContext().sendBroadcast(intent);

                    // cancel the current alarm pending intent
                    pending_intent = PendingIntent.getBroadcast(LoginActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager.cancel(pending_intent);
                }

                // go to main activity
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}