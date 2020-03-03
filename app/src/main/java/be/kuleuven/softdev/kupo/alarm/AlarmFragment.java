package be.kuleuven.softdev.kupo.alarm;

import android.app.AlarmManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import static android.content.Context.ALARM_SERVICE;
import static android.support.constraint.Constraints.TAG;

// this fragment allows the user to set the alarm to other people

public class AlarmFragment extends Fragment implements View.OnClickListener {

    private TimePicker timePicker;
    private NumberPicker numberPicker;
    private ProgressBar progressBar;

    private ArrayList<String> keys, names;
    private String userId, wakeTime;

    private PendingIntent pending_intent;

    private DatabaseReference rootRef;
    private User user;
    private String wakingName, wakingImage;

    public AlarmFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate layout for this fragment
        View view = inflater.inflate(R.layout.fragment_alarm, container, false);

        Button save;
        rootRef = FirebaseDatabase.getInstance().getReference().child("Users");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        names = new ArrayList<>();
        keys = new ArrayList<>();

        // find views
        timePicker = view.findViewById(R.id.tpk_alarm_frag_timepicker);
        save = view.findViewById(R.id.bt_alarm_frag_save);
        numberPicker = view.findViewById(R.id.np_alarm_frag_numberpicker);
        progressBar = view.findViewById(R.id.pb_alarm_frag_progressbar);

        numberPicker.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        // if save button is clicked...
        save.setOnClickListener(this);

        // get the unique keys from each user in the friend list
        rootRef.child(userId).child("friendlist").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        keys.add(issue.getKey());
                    }
                }

                // add yourself to 'names'
                rootRef.orderByKey().equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot issue : dataSnapshot.getChildren()) {
                            user = issue.getValue(User.class);
                        }
                        names.add(user.getDisplayName());

                        // for later use
                        wakingName = user.getDisplayName();
                        wakingImage = user.getImage();

                        // get users from keys and add them into 'names'
                        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot issue : dataSnapshot.getChildren()) {
                                    user = issue.getValue(User.class);
                                    if (keys.contains(user.getUserId())) {
                                        names.add(user.getDisplayName());
                                    }
                                }
                                Collections.sort(names);

                                // display friends in number picker
                                // convert the list into an array
                                String[] data = names.toArray(new String[0]);
                                numberPicker.setDisplayedValues(null); // init
                                numberPicker.setMinValue(0);
                                numberPicker.setMaxValue(names.size()-1);
                                numberPicker.setWrapSelectorWheel(false); // prevent over scrolling

                                numberPicker.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.INVISIBLE);

                                numberPicker.setDisplayedValues(data);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return view;
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()) {

            // when save button is clicked, update the value in the database
            case R.id.bt_alarm_frag_save:

                // pop up dialog
                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

                final EditText edittext = new EditText(getActivity());
                alert.setTitle("Wake up text");
                alert.setView(edittext);

                // if submitted...
                alert.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        final String YouEditTextValue = edittext.getText().toString();
                        final int hour = timePicker.getHour();
                        final int minute = timePicker.getMinute();

                        // change the time format to HH:mm and put in 'wakeTime'
                        String hourToString = "";
                        String minuteToString = "";

                        if (hour < 10) hourToString += ("0");
                        if (minute < 10) minuteToString += ("0");

                        hourToString += Integer.toString(hour);
                        minuteToString += Integer.toString(minute);

                        wakeTime = hourToString + ":" + minuteToString;

                        // Get name from number picker
                        final String name = names.get(numberPicker.getValue());

                        // set wake up info to the specified user
                        rootRef.orderByChild("displayName").equalTo(name).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot issue : dataSnapshot.getChildren()) {
                                    user = issue.getValue(User.class);
                                }
                                rootRef.child(user.getUserId()).child("timeDisplay").setValue(wakeTime);
                                rootRef.child(user.getUserId()).child("wakeUpText").setValue(YouEditTextValue);
                                rootRef.child(user.getUserId()).child("waking").setValue(true);
                                rootRef.child(user.getUserId()).child("woken").setValue(false);
                                rootRef.child(user.getUserId()).child("wakingName").setValue(wakingName);
                                rootRef.child(user.getUserId()).child("wakingImage").setValue(wakingImage);

                                // add this user to your wake list
                                rootRef.child("wakelist").orderByKey().equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            Toast.makeText(getContext(), "This user is already in your wake list.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            rootRef.child(userId).child("wakelist").child(user.getUserId()).setValue(true);
                                            Toast.makeText(getContext(),"User added to wakelist!",Toast.LENGTH_SHORT).show();

                                            setAlarm();
                                        }

                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });

                // if cancelled, do nothing
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });

                alert.show();

                break;
        }
    }

    private void setAlarm() {

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final Calendar calendar = Calendar.getInstance();
        final Intent intent = new Intent(getActivity(), AlarmReceiver.class);
        final AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);

        // retrieve info from the current user
        rootRef.orderByKey().equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // get a user class
                for (DataSnapshot issue : dataSnapshot.getChildren()) {
                    user = issue.getValue(User.class);
                }

                // if 'waking' is true, then set the alarm
                if (user.getWaking()) {

                    // split the string to each int value
                    String[] timeDisplay = user.getTimeDisplay().split(":");
                    int hour = Integer.parseInt(timeDisplay[0]);
                    int minute = Integer.parseInt(timeDisplay[1]);

                    // set the calendar at the specified time
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);

                    // pass some info to the alarm receiver class
                    intent.putExtra("extra", "yes");
                    intent.putExtra("wakingName", user.getWakingName());
                    intent.putExtra("wakeUpText", user.getWakeUpText());
                    intent.putExtra("wakingImage", user.getWakingImage());

                    // set the alarm at the specified time
                    pending_intent = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pending_intent);

                // if 'waking' is false, then cancel the alarm or do nothing
                } else {
                    intent.putExtra("extra", "no");
                    getContext().sendBroadcast(intent);
                    pending_intent = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager.cancel(pending_intent);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
