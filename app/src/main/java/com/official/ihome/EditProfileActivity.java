package com.official.ihome;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.official.ihome.Fragment.Profile;
import com.official.ihome.Model.User;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    TextView username,contact;
    EditText new_username,new_contact;
    Button submit;
    String profileID;
    CircleImageView image;

    ProgressDialog pd;
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference reference;

    private static final int IMAGE_REQUEST = 1;
    StorageReference storageReference;
    private Uri imageUri;
    private StorageTask uploadTask;
    String photoUrl = "default";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        SharedPreferences preferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        profileID = preferences.getString("profileID","none");

        username = findViewById(R.id.current_username);
        contact  = findViewById(R.id.current_contact);
        new_username = findViewById(R.id.new_username);
        new_contact = findViewById(R.id.new_contact);
        submit = findViewById(R.id.updateButton);
        image = findViewById(R.id.current_Image);

        storageReference = FirebaseStorage.getInstance().getReference("profile");
        reference = FirebaseDatabase.getInstance().getReference("Users").child(profileID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                contact.setText(user.getContact());
                if (user.getImageUrl().equals("default")){
                    image.setImageResource(R.drawable.noimage);
                }else {
                    Glide.with(getApplicationContext()).load(user.getImageUrl()).into(image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd = new ProgressDialog(EditProfileActivity.this);

                String update_username = new_username.getText().toString();
                String update_contact = new_contact.getText().toString();

                HashMap<String,Object> hashMap = new HashMap<>();
                hashMap.put("contact",update_contact);
                hashMap.put("username",update_username);

                if (TextUtils.isEmpty(update_username) || TextUtils.isEmpty(update_contact)){
                    Toast.makeText(EditProfileActivity.this,"All fields are required!",Toast.LENGTH_SHORT).show();
                }else {
                    pd.setMessage("Please wait...");
                    pd.show();
                    if (profileID.equals(firebaseUser.getUid())){
                        reference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    pd.dismiss();
                                    Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }
                            }
                        });
                    }
                }


            }
        });

    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = EditProfileActivity.this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private String uploadImage(){
        final ProgressDialog pd = new ProgressDialog(EditProfileActivity.this);
        pd.setMessage("Uploading..");
        pd.show();

        if (imageUri != null){
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    +"."+getFileExtension(imageUri));

            fileReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri downloadUrl) {
                            photoUrl = downloadUrl.toString();

                            reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("imageUrl",downloadUrl.toString());
                            reference.updateChildren(map);

                            pd.setMessage("Upload Successful!");
                            pd.dismiss();
                        }
                    });
                }
            });
        }else {
            Toast.makeText(EditProfileActivity.this,"No Image selected!",Toast.LENGTH_LONG).show();
        }
        return photoUrl;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() !=null){
            imageUri = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()){
                Toast.makeText(EditProfileActivity.this,"Upload in progress...",Toast.LENGTH_LONG).show();
            }else {
                uploadImage();
            }
        }
    }
}
