package be.kuleuven.softdev.kupo.alarm;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

// a fragment that allows the user to look at their friends

public class ContactFragment extends Fragment {

    private ArrayList<String> keys = new ArrayList<>();
    private List<User> userList;

    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    private DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Users");
    private User user;

    public ContactFragment() {
        // required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_contact, container, false);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userList = new ArrayList<>();

        progressBar = view.findViewById(R.id.pb_contact_frag_progressbar);
        recyclerView = view.findViewById(R.id.rv_contact_frag_recyclerview);
        Toolbar toolbar = view.findViewById(R.id.tb_contact_frag_toolbar);

        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        // set the toolbar
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
        toolbar.setTitle("Contacts");

        // find the friends of the current user
        rootRef.child(userId).child("friendlist").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    // get unique key id from each friend and store it in 'keys'
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        keys.add(issue.getKey());
                    }

                    // get users from keys
                    rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot issue : dataSnapshot.getChildren()) {
                                user = issue.getValue(User.class);
                                if (keys.contains(user.getUserId())) {
                                    userList.add(user);
                                }
                            }

                            // initiate the recycler view
                            initRecyclerView(view);

                            progressBar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return view;
    }

    // set adapter to the recycler view
    private void initRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.rv_contact_frag_recyclerview);
        ContactListAdapter adapter = new ContactListAdapter(this.getContext(), userList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
    }

}
