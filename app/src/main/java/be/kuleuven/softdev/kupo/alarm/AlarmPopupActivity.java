package be.kuleuven.softdev.kupo.alarm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

// this activity displays the alarm sender's information to the receiver

public class AlarmPopupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_popup);

        Intent intent = getIntent();
        String displayName = intent.getExtras().getString("displayName");
        String wakeUpText = intent.getExtras().getString("wakeUpText");
        String image = intent.getExtras().getString("image");

        Button dismiss = findViewById(R.id.bt_alarm_popup_act_dismiss);
        TextView display = findViewById(R.id.tv_alarm_popup_act_display);
        TextView wakeText = findViewById(R.id.tv_alarm_popup_act_waketext);
        ImageView imageView = findViewById(R.id.iv_alarm_popup_act_image);

        display.setText("Tell " + displayName + " to stop the alarm!");
        wakeText.setText(wakeUpText);
        Picasso.with(this).load(image).fit().into(imageView);

        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
