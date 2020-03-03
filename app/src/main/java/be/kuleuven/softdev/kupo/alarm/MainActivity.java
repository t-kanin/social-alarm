package be.kuleuven.softdev.kupo.alarm;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

// this activity contains all fragments

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {

                // if alarm button is clicked, go to alarm fragment
                case R.id.navigation_alarm:
                    AlarmFragment alarmFragment = new AlarmFragment();
                    android.app.FragmentManager manAlarm = getFragmentManager();
                    manAlarm.beginTransaction().replace(R.id.fl_main_act_contentlayout, alarmFragment, alarmFragment.getTag()).commit();
                    return true;

                // if search button is clicked, go to search fragment
                case R.id.navigation_find:
                    SearchFragment searchFragment = new SearchFragment();
                    android.app.FragmentManager manSearch = getFragmentManager();
                    manSearch.beginTransaction().replace(R.id.fl_main_act_contentlayout, searchFragment, searchFragment.getTag()).commit();
                    return true;

                // if contact button is clicked, go to contact fragment
                case R.id.navigation_contacts:
                    ContactFragment contactFragment = new ContactFragment();
                    android.app.FragmentManager manContact = getFragmentManager();
                    manContact.beginTransaction().replace(R.id.fl_main_act_contentlayout, contactFragment, contactFragment.getTag()).commit();
                    return true;

                // if wake button is clicked, go to wake fragment
                case R.id.navigation_notifications:
                    WakeFragment wakeFragment = new WakeFragment();
                    android.app.FragmentManager manWake = getFragmentManager();
                    manWake.beginTransaction().replace(R.id.fl_main_act_contentlayout, wakeFragment, wakeFragment.getTag()).commit();
                    return true;

                // if account button is clicked, go to account fragment
                case R.id.navigation_account:
                    AccountFragment accountFragment = new AccountFragment();
                    android.app.FragmentManager manAccount = getFragmentManager();
                    manAccount.beginTransaction().replace(R.id.fl_main_act_contentlayout, accountFragment, accountFragment.getTag()).commit();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set the layout
        setContentView(R.layout.activity_main);

        // set the layout for the navigation bar
        BottomNavigationView navigation = findViewById(R.id.bnv_main_act_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BottomNavigationViewHelper.disableShiftMode(navigation);

        // set the default fragment to be alarm fragment
        AlarmFragment alarmFragment = new AlarmFragment();
        android.app.FragmentManager manager1 = getFragmentManager();
        manager1.beginTransaction().replace(R.id.fl_main_act_contentlayout, alarmFragment, alarmFragment.getTag()).commit();
    }
}
