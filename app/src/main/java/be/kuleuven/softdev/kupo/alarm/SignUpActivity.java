package be.kuleuven.softdev.kupo.alarm;

import android.content.Intent;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Array;
import java.util.ArrayList;

// this activity allows the user to create his/her account

public class SignUpActivity extends AppCompatActivity {

    private EditText email, password, username, displayName;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private String wakeUpText, timeDisplay, userId;
    private String image;
    private DatabaseReference databaseUser;
    private String statusMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button signIn, signUp, resetPassword;

        auth = FirebaseAuth.getInstance();
        databaseUser = FirebaseDatabase.getInstance().getReference("Users");

        // set the layout
        setContentView(R.layout.activity_sign_up);

        // find views
        signIn = findViewById(R.id.bt_signup_act_login);
        signUp = findViewById(R.id.bt_signup_act_register);
        username = findViewById(R.id.et_signup_act_username);
        displayName = findViewById(R.id.et_signup_act_displayname);
        email = findViewById(R.id.et_signup_act_email);
        password = findViewById(R.id.et_signup_act_password);
        progressBar = findViewById(R.id.pb_signup_act_progressbar);
        resetPassword = findViewById(R.id.bt_signup_act_resetpassword);

        // when reset password button is clicked, go to reset password activity
        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, ResetPasswordActivity.class));
            }
        });

        // when sign in button is clicked, go back to login activity
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // when sign up button is clicked:
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String password = SignUpActivity.this.password.getText().toString().trim();
                final String email = SignUpActivity.this.email.getText().toString().trim();
                final String username = SignUpActivity.this.username.getText().toString().trim();
                final String displayName = SignUpActivity.this.displayName.getText().toString().trim();

                // if email is empty
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // if password is empty
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // if password is too short
                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                // create a user
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Toast.makeText(SignUpActivity.this, "Account created", Toast.LENGTH_SHORT).show();

                                progressBar.setVisibility(View.INVISIBLE);

                                if (!task.isSuccessful()) {
                                    Toast.makeText(SignUpActivity.this, "Login failed" + task.getException(), Toast.LENGTH_SHORT).show();
                                } else {
                                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));

                                    // initialize parameters to be put in the current user class
                                    wakeUpText = "Wake up!";
                                    timeDisplay = "00:00";
                                    image = "https://firebasestorage.googleapis.com/v0/b/alarm-2ab6e.appspot.com/o/member-default.jpg?alt=media&token=19724b3b-f022-4a94-b449-c86534e8a3fe";
                                    statusMessage = "It's a nice day!";
                                    userId = auth.getCurrentUser().getUid();
                                    String wakingName = "";
                                    String wakingImage = image;
                                    ArrayList<String> friendlist = new ArrayList<>();
                                    ArrayList<String> wakelist = new ArrayList<>();

                                    User user = new User(username, displayName, statusMessage, wakeUpText, timeDisplay,
                                            email, image, userId, wakingName, wakingImage, false, false, friendlist, wakelist);

                                    // set value to the current user in the database
                                    databaseUser.child(userId).setValue(user);
                                    finish();
                                }
                            }
                        });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.INVISIBLE);
    }
}
