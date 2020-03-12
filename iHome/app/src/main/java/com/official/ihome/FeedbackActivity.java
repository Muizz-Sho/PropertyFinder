package com.official.ihome;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.official.ihome.Model.Feedback;

public class FeedbackActivity extends AppCompatActivity {

    TextView subject, message;
    Button submit;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        subject = findViewById(R.id.subject);
        message = findViewById(R.id.messageBox);
        submit = findViewById(R.id.submitFeed);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reference = FirebaseDatabase.getInstance().getReference("Feedback").child(firebaseUser.getUid()).push();
                String feedId = reference.getKey();
                String feedSubject = subject.getText().toString();
                String feedMessage = message.getText().toString();

                if (TextUtils.isEmpty(feedSubject)) {
                    Toast.makeText(FeedbackActivity.this, "Enter Your Subject", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(feedMessage)) {
                    Toast.makeText(FeedbackActivity.this, "Enter Your Message!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Feedback feedback = new Feedback(feedId, feedSubject, feedMessage);
                reference.setValue(feedback).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(FeedbackActivity.this, "Feedback Submitted!", Toast.LENGTH_SHORT).show();
                            Intent backToProfile = new Intent(FeedbackActivity.this, MainActivity.class);
                            startActivity(backToProfile);
                        } else
                            Toast.makeText(FeedbackActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
