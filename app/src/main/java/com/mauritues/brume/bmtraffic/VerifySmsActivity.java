package com.mauritues.brume.bmtraffic;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class VerifySmsActivity extends AppCompatActivity {

    private Button verifySmsButton;
    private EditText smsBox;

    public String TAG = "FragmentActivity";

    @Override
    protected void attachBaseContext(Context newBase) {
        String locale = Utils.getLang(newBase);
        Context ctx = Utils.changeLocale(newBase, locale);
        super.attachBaseContext(ctx);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_sms);

        getSupportActionBar().hide();

        smsBox = findViewById(R.id.smsTxtBx);

        View.OnClickListener mapClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Phone Verification Id " + SessionVariables.PhoneVerificationId);
                System.out.println("Sms code typed " + smsBox.getText().toString());
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(SessionVariables.PhoneVerificationId, smsBox.getText().toString());

                signInWithPhoneAuthCredential(credential);


            }
        };

        verifySmsButton = findViewById(R.id.verifySmsButton);
        verifySmsButton.setOnClickListener(mapClickListener);


    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithCredential:success");

                                    FirebaseUser user = task.getResult().getUser();
                                    SessionVariables.LoggedUser = user;
                                    SharedPreferences prefs = getSharedPreferences("traffic", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor edit = prefs.edit();
                                    edit.putBoolean("login", true);
                                    edit.apply();
                                    Intent intent = new Intent(getApplicationContext(), MainMapActivity.class);
                                    startActivity(intent);
                                    finish();
                                    // ...
                                } else {
                                    // Sign in failed, display a message and update the UI
                                    Log.w(TAG, "signInWithCredential:failure", task.getException());
                                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                        // The verification code entered was invalid
                                    }
                                }
                            }
                        }
                );
    }
}
