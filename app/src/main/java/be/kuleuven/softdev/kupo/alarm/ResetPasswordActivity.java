package be.kuleuven.softdev.kupo.alarm;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

// this activity allows the user to change his/her password by sending a reset email

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText email;
    private FirebaseAuth auth;
    private ProgressBar progressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button reset, back;
        auth = FirebaseAuth.getInstance();

        // set the layout
        setContentView(R.layout.activity_reset_password);

        // find views
        email = findViewById(R.id.et_reset_password_act_email);
        reset = findViewById(R.id.bt_reset_password_act_reset);
        back = findViewById(R.id.bt_reset_password_act_back);
        progressbar = findViewById(R.id.pb_reset_password_act_progressbar);

        // when click back, go back to login activity
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // when click reset, send password reset email
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            String email = ResetPasswordActivity.this.email.getText().toString().trim();

            // if email is empty, show toast
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getApplication(), "Enter your registered email id", Toast.LENGTH_SHORT).show();
                return;
            }

            progressbar.setVisibility(View.VISIBLE);

            // send password reset email
            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ResetPasswordActivity.this, "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ResetPasswordActivity.this, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                            }
                            progressbar.setVisibility(View.INVISIBLE);
                        }
                    });
            }
        });
    }
}