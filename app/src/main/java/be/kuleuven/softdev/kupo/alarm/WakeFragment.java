package be.kuleuven.softdev.kupo.alarm;

import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.support.constraint.Constraints.TAG;

// this fragment allows the user to cancel or look at the current alarm settings for his/her friends

public class WakeFragment extends Fragment {

    private List<User> userList;
    private String userId;
    private User user;
    private ArrayList<String> keys;

    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference().child("Users");

    private RecyclerView recyclerView;
    private WakeListAdapter adapter;
    private ProgressBar progressBar;

    public WakeFragment() {
        // required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_wake, container, false);

        keys = new ArrayList<>();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userList = new ArrayList<>();

        // set the toolbar
        Toolbar toolbar = view.findViewById(R.id.tb_wake_frag_toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
        toolbar.setTitle("Wake");

        // set the specifications of the recycler view
        recyclerView = view.findViewById(R.id.rv_wake_frag_recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        progressBar = view.findViewById(R.id.pb_wake_frag_progressbar);
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        // find users in the wake list
        mRootRef.child(userId).child("wakelist").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    // get the unique keys from each user and store them in the array list
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        keys.add(issue.getKey());
                    }

                    // get users from keys
                    mRootRef.addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            // get a user class from the database
                            for (DataSnapshot issue : dataSnapshot.getChildren()) {

                                user = issue.getValue(User.class);

                                // check which user belongs to the wake list
                                if (keys.contains(user.getUserId())) {

                                    // send user info to the adapter
                                    userList.add(user);
                                    adapter = new WakeListAdapter(getContext(), userList);
                                    recyclerView.setAdapter(adapter);

                                    progressBar.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.VISIBLE);

                                    // initiate the swipe gesture
                                    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN | ItemTouchHelper.UP) {

                                        @Override
                                        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                                            Toast.makeText(getActivity(), "Moved", Toast.LENGTH_SHORT).show();
                                            return false;
                                        }

                                        @Override
                                        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                                            Toast.makeText(getActivity(), "Deleted", Toast.LENGTH_SHORT).show();

                                            // remove swiped item from list and notify the recyclerView
                                            final int position = viewHolder.getAdapterPosition();
                                            User userToRemove = userList.get(position);
                                            userList.remove(position);

                                            // remove the user from that user's wake list
                                            mRootRef.child(userId).child("wakelist").child(userToRemove.getUserId()).removeValue();

                                            // set 'waking' and 'woken' for the selected user to false
                                            mRootRef.child(userToRemove.getUserId()).child("waking").setValue(false);
                                            mRootRef.child(userToRemove.getUserId()).child("woken").setValue(false);

                                            // notify the adapter that the item has been removed
                                            adapter.notifyItemRemoved(position);
                                        }
                                    };

                                    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
                                    itemTouchHelper.attachToRecyclerView(recyclerView);
                                }

                            }

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
}
