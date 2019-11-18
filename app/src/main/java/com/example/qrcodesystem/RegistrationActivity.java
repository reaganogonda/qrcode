package com.example.qrcodesystem;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RegistrationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);
        TextView login = (TextView)findViewById(R.id.lnkLogin);
        login.setMovementMethod(LinkMovementMethod.getInstance());
        findViewById(R.id.btnLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = "student";
                RadioButton btn = findViewById(R.id.lecturer);
                if(btn.isChecked())user = "lecturer";
                Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                intent.putExtra("usertype",user);

                startActivity(intent);
                finish();
            }
        });
    }
}