package com.official.ihome;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import com.official.ihome.Model.Property;

import java.util.HashMap;

public class EditPropertyActivity extends AppCompatActivity {

    ImageView image;
    TextView name,detail;
    EditText new_name,new_detail,new_price;
    Button update;

    ProgressDialog pd;
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference reference;

    private static final int IMAGE_REQUEST = 1;
    StorageReference storageReference;
    private Uri imageUri;
    private StorageTask uploadTask;
    String photoUrl = "default";
    String idPassed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_property);

        image = findViewById(R.id.edit_propertyImage);
        name = findViewById(R.id.edit_viewName);
        new_name = findViewById(R.id.edit_propertyName);
        new_detail = findViewById(R.id.edit_propertyDetail);
        new_price = findViewById(R.id.edit_propertyPrice);
        update = findViewById(R.id.updateProperty);

        Intent intent = getIntent();
        Bundle data = intent.getExtras();

        idPassed = data.getString("propertyID");
        storageReference = FirebaseStorage.getInstance().getReference("property");

        reference = FirebaseDatabase.getInstance().getReference("Property").child(firebaseUser.getUid()).child(idPassed);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Property property = dataSnapshot.getValue(Property.class);
                name.setText(property.getName());
                new_name.setHint(property.getName());
                new_detail.setHint(property.getDetail());
                new_price.setHint(property.getPrice());
                if (property.getImageUrl().equals("default")){
                    image.setImageResource(R.drawable.noimage);
                }else {
                    Glide.with(getApplicationContext()).load(property.getImageUrl()).into(image);
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

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd = new ProgressDialog(EditPropertyActivity.this);

                String update_name = new_name.getText().toString();
                String update_price = new_price.getText().toString();
                String update_detail = new_detail.getText().toString();

                HashMap<String,Object> hashMap = new HashMap<>();
                hashMap.put("name",update_name);
                hashMap.put("price",update_price);
                hashMap.put("detail",update_detail);

                if (TextUtils.isEmpty(update_name) || TextUtils.isEmpty(update_detail) || TextUtils.isEmpty(update_price)){
                    Toast.makeText(EditPropertyActivity.this,"All fields are required!",Toast.LENGTH_SHORT).show();
                }else {
                    pd.setMessage("Please wait...");
                    pd.setCanceledOnTouchOutside(false);
                    pd.show();

                    reference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                pd.dismiss();
                                Intent intent = new Intent(EditPropertyActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        }
                    });
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
        ContentResolver contentResolver = EditPropertyActivity.this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private String uploadImage(){
        final ProgressDialog pd = new ProgressDialog(EditPropertyActivity.this);
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
                            reference = FirebaseDatabase.getInstance().getReference("Property").child(firebaseUser.getUid()).child(idPassed);
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
            Toast.makeText(EditPropertyActivity.this,"No Image selected!",Toast.LENGTH_LONG).show();
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
                Toast.makeText(EditPropertyActivity.this,"Upload in progress...",Toast.LENGTH_LONG).show();
            }else {
                uploadImage();
            }
        }
    }
}
