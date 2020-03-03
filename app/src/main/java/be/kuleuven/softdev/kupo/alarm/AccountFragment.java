package be.kuleuven.softdev.kupo.alarm;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.Serializable;

import static android.app.Activity.RESULT_OK;

// this fragment allows the user to change their preference settings

public class AccountFragment extends Fragment implements View.OnClickListener {

    private ImageView imageView;
    private TextView displayName, status;

    private Uri filePath, downloadUri;

    private final int PICK_IMAGE_REQUEST = 71;

    private StorageReference storageReference;
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Users");
    private User user;
    private String currentUserID = currentUser.getUid();

    private View view;
    private String picUri;

    public AccountFragment() {
        // required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate layout for this fragment
        view = inflater.inflate(R.layout.fragment_account, container, false);

        Button btnChangePic, btnUpload;
        Toolbar toolbar;
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // set toolbar
        toolbar = view.findViewById(R.id.tb_account_frag_toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
        toolbar.setTitle("Account");

        // find views
        btnChangePic = view.findViewById(R.id.bt_account_frag_change_picture);
        imageView = view.findViewById(R.id.iv_account_frag_image);
        btnUpload = view.findViewById(R.id.bt_account_frag_upload);

        // set a click listener to the buttons
        btnChangePic.setOnClickListener(this);
        btnUpload.setOnClickListener(this);

        // get current user info to display information on the main screen
        rootRef.orderByChild("userId").equalTo(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // get a user class
                for (DataSnapshot issue : dataSnapshot.getChildren()) {
                    user = issue.getValue(User.class);
                }

                // set text
                displayName = view.findViewById(R.id.tv_account_frag_name);
                status = view.findViewById(R.id.tv_account_frag_status);
                displayName.setText(user.getDisplayName());
                status.setText(user.getStatusMessage());

                // set image via uri
                Picasso.with(getContext()).load(user.getImage()).fit().into(imageView);

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return view;
    }

    // set the menu toolbar
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_account_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // set the menu toolbar on click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.tb_account_frag_setting :
                startActivity(new Intent(getContext(), SettingActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // set on click image buttons
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_account_frag_change_picture :
                chooseImage();
                break;
            case R.id.bt_account_frag_upload :
                uploadImage();
                break;
        }
    }

    // go to the activity for selecting a picture
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    // upload the image to firebase storage
    private void uploadImage() {

        if(filePath != null) {

            final ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child("pic_" + currentUser.getEmail());
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            downloadUri = taskSnapshot.getDownloadUrl();
                            picUri = downloadUri.toString();
                            rootRef.child(currentUserID).child("image").setValue(picUri);
                            updateImage();

                            Toast.makeText(getActivity(), "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }

    private void updateImage() {
        Picasso.with(getContext()).load(downloadUri).fit().into(imageView);
    }

    // set the image on screen
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null ) {

            filePath = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}
