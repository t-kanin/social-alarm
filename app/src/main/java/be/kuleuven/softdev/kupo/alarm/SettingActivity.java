package be.kuleuven.softdev.kupo.alarm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// this activity keeps all of setting preferences

public class SettingActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // load setting fragment
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new MainSettingFragment()).commit();
    }

    public static class MainSettingFragment extends PreferenceFragment {

        User user;
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Users");

        SharedPreferences settings;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // set preference layout
            addPreferencesFromResource(R.xml.pref_main);

            // get main preference
            settings = PreferenceManager.getDefaultSharedPreferences(getContext());

            // check database for current user
            rootRef.orderByKey().equalTo(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // get a user class
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        user = issue.getValue(User.class);
                    }

                    // overwrite old preferences
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("key_display_name", user.getDisplayName());
                    editor.putString("key_status_message", user.getStatusMessage());
                    editor.apply();

                    // rewrite summary value if changed
                    bindSummaryValue(findPreference("key_display_name"));
                    bindSummaryValue(findPreference("key_status_message"));

                    // find preferences
                    Preference prfDisplayName = findPreference("key_display_name");
                    Preference prfStatusMessage = findPreference("key_status_message");
                    Preference btnUsername = findPreference("key_username");
                    Preference btnSignOut = findPreference("key_sign_out");
                    Preference btnEmail = findPreference("key_email");
                    Preference prfDelete = findPreference("key_delete_account");

                    // set default summary value
                    prfDisplayName.setSummary(user.getDisplayName());
                    prfStatusMessage.setSummary(user.getStatusMessage());
                    btnUsername.setSummary(user.getUsername());
                    btnEmail.setSummary(currentUser.getEmail());

                    // if username button is clicked, show toast
                    btnUsername.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            Toast.makeText(getContext(), "You cannot change your username!", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                    });

                    // if sign out button is clicked, go to login activity
                    btnSignOut.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            auth.signOut();
                            startActivity(new Intent(getActivity(), LoginActivity.class));
                            return true;
                        }
                    });

                    // if email button is clicked, show toast
                    btnEmail.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            Toast.makeText(getContext(), "You cannot change your email!", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                    });

                    // if delete button is clicked, delete the account then go to sign up activity
                    prfDelete.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            currentUser.delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getActivity(), "Your profile is deleted:( Create a account now!", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(getActivity(), SignUpActivity.class));
                                                getActivity().finish();
                                            } else {
                                                Toast.makeText(getActivity(), "Failed to delete your account!", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            return true;
                        }
                    });
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    // when a preference is changed, set the summary value immediately
    private static void bindSummaryValue(Preference preference) {
        preference.setOnPreferenceChangeListener(listener);
        listener.onPreferenceChange(preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                .getString(preference.getKey(), ""));
    }

    private static Preference.OnPreferenceChangeListener listener = new Preference.OnPreferenceChangeListener() {

        @Override
        public boolean onPreferenceChange(final Preference preference, Object newValue) {

            // this value is when a user inputs a string into a preference
            String stringValue = newValue.toString();

            // if the preference is edit text
            if (preference instanceof EditTextPreference) {

                // set the value to the database of my account
                if (preference.getKey().equals("key_display_name")) {
                    FirebaseDatabase.getInstance().getReference().child("Users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child("displayName").setValue(stringValue);

                    // set the new summary value
                    preference.setSummary(stringValue);
                }
                if (preference.getKey().equals("key_status_message")) {
                    FirebaseDatabase.getInstance().getReference().child("Users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child("statusMessage").setValue(stringValue);
                    preference.setSummary(stringValue);
                }

            // if not, then just set the new value
            } else if (preference != null) {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };
}
