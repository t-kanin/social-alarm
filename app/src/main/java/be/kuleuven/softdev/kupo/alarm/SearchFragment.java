package be.kuleuven.softdev.kupo.alarm;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

// this fragment allows the user to search for friends

public class SearchFragment extends Fragment implements View.OnClickListener {

    RelativeLayout mainDisplay;
    Button add, cancel;
    EditText searchText;
    ImageButton searchButton;
    DatabaseReference rootRef;
    ImageView image;
    TextView displayName, username;
    User user;
    String userId;
    private FirebaseAuth auth;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        rootRef = FirebaseDatabase.getInstance().getReference().child("Users");
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();

        // find views
        add = view.findViewById(R.id.bt_search_frag_add);
        cancel = view.findViewById(R.id.bt_search_frag_cancel);
        mainDisplay = view.findViewById(R.id.cl_search_frag_view);
        searchText = view.findViewById(R.id.et_search_frag_searchbar);
        searchButton = view.findViewById(R.id.ib_search_frag_search);
        image = view.findViewById(R.id.iv_search_frag_image);
        displayName = view.findViewById(R.id.tv_search_frag_display_name);
        username = view.findViewById(R.id.tv_search_frag_username);

        mainDisplay.setVisibility(View.INVISIBLE);

        // set a listener to each button
        add.setOnClickListener(this);
        cancel.setOnClickListener(this);
        searchButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()) {

            // if add button is clicked, add that user to the database
            case R.id.bt_search_frag_add :

                // check if you don't add yourself
                if (!user.getEmail().equals(auth.getCurrentUser().getEmail())) {

                    // add to the current user's friend list
                    rootRef.child(userId).child("friendlist").child(user.getUserId()).setValue(true);

                    // add yourself as a friend to your friend
                    rootRef.child(user.getUserId()).child("friendlist").child(userId).setValue(true);

                    Toast.makeText(getActivity(),"Added!",Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getActivity(),"You cannot add yourself!",Toast.LENGTH_SHORT).show();
                }

                break;

            // if cancel button is clicked, remove the main display
            case R.id.bt_search_frag_cancel :
                mainDisplay.setVisibility(View.INVISIBLE);
                break;

            // if search button is clicked, search a user in the database for that specified username
            case R.id.ib_search_frag_search:

                final SharedPreferences prfAllowSearch = PreferenceManager.getDefaultSharedPreferences(getContext());

                rootRef.orderByChild("username").equalTo(searchText.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        // if username is found
                        if (dataSnapshot.exists()) {

                            // if that user allows other people to search, then show the info
                            if (prfAllowSearch.getBoolean("key_allow_add", true)) {

                                for (DataSnapshot issue : dataSnapshot.getChildren()) {
                                    user = issue.getValue(User.class);
                                }

                                username.setText(user.getUsername());
                                displayName.setText(user.getDisplayName());
                                Picasso.with(getContext()).load(user.getImage()).into(image);

                                mainDisplay.setVisibility(View.VISIBLE);

                            } else {
                                Toast.makeText(getActivity(),"User not found",Toast.LENGTH_SHORT).show();
                            }

                        // if username is not found, then show toast
                        } else {
                            Toast.makeText(getActivity(),"User not found",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        }
    }
}
