package com.example.qrcodesystem;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import android.os.Bundle;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = SignUpActivity.class.getSimpleName();
    private ProgressDialog mAuthProgressDialog;
    private Context mContext;
    private String mKey = "";

    @Bind(R.id.SignUpCard)
    CardView mCreateUserButton;
    @Bind(R.id.nameEditText) EditText mNameEditText;
    @Bind(R.id.emailEditText) EditText mEmailEditText;
    @Bind(R.id.passwordEditText) EditText mPasswordEditText;
    @Bind(R.id.confirmPasswordEditText) EditText mConfirmPasswordEditText;
    @Bind(R.id.loginTextView) TextView mLoginTextView;
    @Bind(R.id.signUpRelative) RelativeLayout mRelative;
    @Bind(R.id.progressBarSignUp) ProgressBar mProgressBarSignUp;
    @Bind(R.id.creatingAccountLoadingText) TextView mLoadingText;
    @Bind(R.id.ConfirmEmailLayout) LinearLayout mConfirmEmailLayout;
    @Bind(R.id.textLink) TextView mPrivPol;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String mName;
    private DatabaseReference mRef1;
    private DatabaseReference mRef2;
    private int mClusterID;

    private List<String> easyPasswords = new ArrayList<>(Arrays.asList("123456789", "987654321","qwertyuio","asdfghjkl","zxcvbnm12","123456abc","123456qwe",
            "987654qwe", "987654asd",""));

    Handler h = new Handler();
    Runnable r;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        createAuthStateListener();
        mContext = this.getApplicationContext();

        mLoginTextView.setOnClickListener(this);
        mCreateUserButton.setOnClickListener(this);
        mConfirmPasswordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                        (actionId == EditorInfo.IME_ACTION_DONE) ||
                        (actionId == EditorInfo.IME_ACTION_NEXT) ||
                        (actionId == EditorInfo.IME_ACTION_GO)) {
                    try{
                        mCreateUserButton.performClick();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    Log.i(TAG, "Enter pressed");
                }
                return false;
            }
        });

        mPasswordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b) findViewById(R.id.dontForgetStuff).setVisibility(View.VISIBLE);
                else findViewById(R.id.dontForgetStuff).setVisibility(View.GONE);
            }
        });

        String sourceString = "By clicking SIGN UP, you agree to our <b>End User License Agreement.</b>";
        mPrivPol.setText(Html.fromHtml(sourceString));
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) mAuth.removeAuthStateListener(mAuthListener);
    }

    @Override
    public void onClick(View v) {
        if(v == mLoginTextView){
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        if(v == mCreateUserButton){
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            createNewUser();
        }
    }

    private void createNewUser() {
        final String name = mNameEditText.getText().toString().trim();
        final String email = mEmailEditText.getText().toString().trim();

        final String password = mPasswordEditText.getText().toString().trim();
        String confirmPassword = mConfirmPasswordEditText.getText().toString().trim();

        boolean validEmail = isValidEmail(email);
        boolean validPassword = isValidPassword(password,confirmPassword);
        boolean validName = isValidName(name);
        if(!validEmail || !validName || !validPassword)return;

        if(!isOnline(mContext)){
//            Snackbar.make(findViewById(R.id.SignUpCoordinatorLayout), R.string.SignUpNoConnection,
//                    Snackbar.LENGTH_LONG).show();
            Toast.makeText(mContext, R.string.SignUpNoConnection,Toast.LENGTH_SHORT).show();
        }else{
//            mAvi.setVisibility(View.VISIBLE);
            mProgressBarSignUp.setVisibility(View.VISIBLE);
            mLoadingText.setVisibility(View.VISIBLE);
            mRelative.setVisibility(View.GONE);
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Log.d(TAG,"authentication successful");
                        createFirebaseUserProfile(task.getResult().getUser());
                    }else {
                        mRelative.setVisibility(View.VISIBLE);
//                        mAvi.setVisibility(View.GONE);
                        mProgressBarSignUp.setVisibility(View.GONE);
                        mLoadingText.setVisibility(View.GONE);
                        showFailedSignUp();
                    }
                }
            });
        }

    }

    private boolean isValidName(String name) {
        if (name.equals("")) {
            mNameEditText.setError("Please enter your name");
            return false;
        }
        if(name.length()>16){
            mNameEditText.setError("That name is too long");
            return false;
        }
        return true;
    }

    private void showFailedSignUp(){
        Toast.makeText(mContext, "Failed to sign up",Toast.LENGTH_SHORT).show();
    }

    private void createFirebaseUserProfile(final FirebaseUser user) {
        UserProfileChangeRequest addProfileName = new UserProfileChangeRequest.Builder().setDisplayName(mName).build();

        user.updateProfile(addProfileName).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d(TAG,"Created new username");
                }
            }
        });
    }


    private void createAuthStateListener() {
        mAuthListener = new FirebaseAuth.AuthStateListener(){

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    startMainActivity();
//                    user.sendEmailVerification();
                }
            }
        };
    }

    private void startMainActivity(){
        String user = "k";
        RadioButton btn = findViewById(R.id.lecturer);
        if(btn.isChecked())user = "lecturer";

        SharedPreferences sharedpreferences = getSharedPreferences("QRCode", Context.MODE_PRIVATE);
        sharedpreferences.edit().putString("user", user).apply();

//        mAvi.setVisibility(View.GONE);
        mProgressBarSignUp.setVisibility(View.GONE);
        mLoadingText.setVisibility(View.GONE);
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
        intent.putExtra("usertype",user);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean isValidPassword(String password, String confirmPassword) {
        if(password.equals("")){
            mPasswordEditText.setError("We need a password.");
            return false;
        }else if (password.length() < 9) {
            mPasswordEditText.setError("Please create a password containing at least 6 characters");
            return false;
        } else if (!password.equals(confirmPassword)) {
            mPasswordEditText.setError("Passwords do not match");
            return false;
        }else if(easyPasswords.contains(password)){
            mPasswordEditText.setError("Please, put a strong password!");
            return false;
        }
        return true;
    }

    private boolean isValidEmail(String email) {
        if(email.equals("")){
            mEmailEditText.setError("We need your email.");
            return false;
        }

        boolean isGoodEmail = (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches());

        if(!email.contains("@")){
            mEmailEditText.setError("That's not an email address.");
            return false;
        }

        int counter = 0;
        for( int i=0; i<email.length(); i++ ) {
            if(email.charAt(i) == '.' ) {
                counter++;
            }
        }
        if(counter!=1 && counter!=2 && counter!=3){
            mEmailEditText.setError("We need your actual email address.");
            return false;
        }

        int counter2 = 0;
        boolean continueIncrement = true;
        for( int i=0; i<email.length(); i++ ) {
            if(email.charAt(i) == '@' ) {
                continueIncrement = false;
            }
            if(continueIncrement)counter2++;
        }
        if(counter2<=3){
            mEmailEditText.setError("That's not a real email address");
            return false;
        }

        if(!isGoodEmail){
            mEmailEditText.setError("We need your actual email address please");
            return false;
        }
        return isGoodEmail;
    }


    public boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }
}
