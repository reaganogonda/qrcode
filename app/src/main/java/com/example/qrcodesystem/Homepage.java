package com.example.qrcodesystem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Homepage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        SharedPreferences sharedpreferences = getSharedPreferences("QRCode", Context.MODE_PRIVATE);
        final String userType = sharedpreferences.getString("user","student");
//        final String userType = getIntent().getExtras().getString("usertype");
        findViewById(R.id.selectCourse).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Homepage.this,course.class);
                intent.putExtra("usertype",userType);
                startActivity(intent);
            }
        });

        if(userType.equals("student")){
            findViewById(R.id.Mystudents).setVisibility(View.GONE);
        }
        findViewById(R.id.Mystudents).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(Homepage.this,course.class));
                Intent intent = new Intent(
                        "com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                startActivityForResult(intent, 1);
            }
        });

        findViewById(R.id.Attendance).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Homepage.this, MyAttendance.class);
                intent.putExtra("usertype",userType);
                startActivity(intent);

            }
        });

        findViewById(R.id.Grades).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Homepage.this, YourGrades.class);
                intent.putExtra("usertype",userType);
                startActivity(intent);            }
        });

        findViewById(R.id.logoutBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(Homepage.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1)
            if (resultCode == Activity.RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");
                String format = data.getStringExtra("SCAN_RESULT_FORMAT");
                Toast.makeText(getApplicationContext(), contents, Toast.LENGTH_SHORT).show();
                DatabaseReference pushData = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("sign_ins");
                DatabaseReference push = pushData.push();
                push.setValue(contents);
            }// if result_ok
    }// onactivityresult
}
