package be.kuleuven.softdev.kupo.alarm;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

// this class is responsible for the interaction of each card view in the recycler view

public class WakeListAdapter extends RecyclerView.Adapter<WakeListAdapter.UserViewHolder> {

    private Context context;
    private List<User> userList;

    public WakeListAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    // create the layout for each view in the recycler view
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.list_wake, null);
        return new UserViewHolder(view);
    }

    @Override
    // for each view, do something...
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {

        User user = userList.get(position);

        // set display name, wake up text and wake up time of each user to each view
        holder.displayName.setText(user.getDisplayName());
        holder.wakeUpText.setText(user.getWakeUpText());
        holder.timeDisplay.setText(user.getTimeDisplay());
        Picasso.with(context).load(user.getImage()).fit().into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    // find views
    class UserViewHolder extends RecyclerView.ViewHolder {

        TextView displayName, wakeUpText, timeDisplay;
        ImageView imageView;

        public UserViewHolder(View itemView) {
            super(itemView);

            displayName = itemView.findViewById(R.id.tv_wake_list_name);
            wakeUpText = itemView.findViewById(R.id.tv_wake_list_text);
            timeDisplay = itemView.findViewById(R.id.tv_wake_list_time);
            imageView = itemView.findViewById(R.id.iv_wake_list_image);
        }
    }
}
