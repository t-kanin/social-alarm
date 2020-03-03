package be.kuleuven.softdev.kupo.alarm;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

// this class is responsible for the layout and interaction of each view in the recycler view

public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ViewHolder> {

    private List<User> userList;
    private Context context;

    ContactListAdapter(Context context, List<User> userList) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    // create the layout for each view in the recycler view
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    // for each view, do something...
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        final User user = userList.get(position);

        // set display name, username, status message and image of each user to each view
        holder.displayName.setText(user.getDisplayName());
        holder.username.setText(user.getUsername());
        holder.status.setText(user.getStatusMessage());
        Picasso.with(context).load(user.getImage()).fit().into(holder.image);

        // when the view is clicked, show toast
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, user.getDisplayName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    // find views
    class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView image;
        TextView username, displayName, status;
        RelativeLayout parentLayout;

        ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.iv_contact_list_image);
            username = itemView.findViewById(R.id.tv_contact_list_username);
            displayName = itemView.findViewById(R.id.tv_contact_list_displayname);
            parentLayout = itemView.findViewById(R.id.rl_contact_list_relativelayout);
            status = itemView.findViewById(R.id.tv_contact_list_status);
        }
    }
}
