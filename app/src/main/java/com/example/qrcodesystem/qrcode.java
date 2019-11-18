package com.example.qrcodesystem;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.WriterException;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class qrcode extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);
        String courseName = getIntent().getExtras().getString("class");
        QRGEncoder qrgEncoder = new QRGEncoder(courseName, null, QRGContents.Type.TEXT, 400);

        try {
            // Getting QR-Code as Bitmap
            Bitmap bitmap = qrgEncoder.encodeAsBitmap();
            // Setting Bitmap to ImageView
            ImageView qrImage = findViewById(R.id.qrcodegen);
            qrImage.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Log.v("TAG", e.toString());
        }
    }
}
