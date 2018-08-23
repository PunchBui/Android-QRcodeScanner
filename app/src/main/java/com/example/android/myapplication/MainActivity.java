package com.example.android.myapplication;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.Toast;

import com.google.zxing.Result;

import java.security.PrivateKey;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission_group.CAMERA;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
//
    public static final int Request_Camera = 1;
    private ZXingScannerView scannerView;
    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scannerView = new ZXingScannerView(this);

        setContentView(scannerView);
        //if(Build.VERSION.SDK_INT == Build.VERSION_CODES.M){
            if(checkPermission()){
                Toast.makeText(MainActivity.this,"Permission is granted",Toast.LENGTH_LONG).show();
            }else{
                requestPermission();
            }
        //}
    }

    private  boolean checkPermission(){
        return (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED);
    }
    private  void requestPermission(){
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},Request_Camera);
    }
    public void onRequestPermissionResult(int requestCode,String permission[],int grantResult[]){
        switch (requestCode){
            case Request_Camera :
                if(grantResult.length > 0){
                    boolean cameraAccepted = grantResult[0] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted){
                        Toast.makeText(MainActivity.this,"Permission Granted",Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(MainActivity.this,"Permission Denied",Toast.LENGTH_LONG).show();
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                            if(shouldShowRequestPermissionRationale(CAMERA)){
                                displayAlertMessage("You need to allow access for both permission",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{Manifest.permission.CAMERA},
                                                            Request_Camera);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){*/
            if(checkPermission()){
                if(scannerView == null){
                    scannerView = new ZXingScannerView(this);
                    setContentView(scannerView);
                }
                scannerView.setResultHandler(this);
                scannerView.startCamera();
            }else{
                requestPermission();
            }
        /*}*/
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        scannerView.stopCamera();
    }

    public void displayAlertMessage(String message, DialogInterface.OnClickListener listener){
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK",listener)
                .setNegativeButton("Cancel",null)
                .create()
                .show();
    }
    @Override
    public void handleResult(final Result result) {
        final String scanResult = result.getText();
        Log.d("kuy",scanResult);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scan Result");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                scannerView.resumeCameraPreview(MainActivity.this);
            }
        });
        if(URLUtil.isHttpsUrl(scanResult)||URLUtil.isHttpUrl(scanResult)) {
        builder.setNeutralButton("Visit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(scanResult));
                    startActivity(browserIntent);
            }
        });}
        builder.setMessage(scanResult);
        AlertDialog alert = builder.create();
        alert.show();
    }
}
