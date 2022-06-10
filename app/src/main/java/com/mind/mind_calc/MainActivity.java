package com.mind.mind_calc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mind.mind_calc.Constructors.UserCredentials;
import com.mind.mind_calc.Utiles.EmailValidator;

import java.net.InetAddress;

public class MainActivity extends AppCompatActivity {

    boolean doubleBackToExitPressedOnce = false;
    private Button btnEnter;
    private Dialog exitDialog, loginDialog, regDialog, loadingDialog;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //        Get layouts
        exitDialog = new Dialog(MainActivity.this);
        exitDialog.setContentView(R.layout.exit_dialog_box);

        loginDialog = new Dialog(MainActivity.this);
        loginDialog.setContentView(R.layout.login_dialog_box);

        regDialog = new Dialog(MainActivity.this);
        regDialog.setContentView(R.layout.registraion_dialog_box);

        loadingDialog = new Dialog(MainActivity.this);
        loadingDialog.setContentView(R.layout.loading_dialog_box);

//        Get components
        btnEnter = (Button) this.findViewById(R.id.btnEnter);

        firebaseAuth = FirebaseAuth.getInstance();

//        Enter button command
        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterToGame();
            }
        });

    }

    //    Hide status bar and navigation bar
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    //    Tap to close app
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        exitDialog.show();

        Button btnExitYes = (Button) exitDialog.findViewById(R.id.btnYes);
        Button btnExitNo = (Button) exitDialog.findViewById(R.id.btnNo);

        btnExitYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
                System.exit(0);
            }
        });

        btnExitNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitDialog.dismiss();
            }
        });

    }

    //    Method for enter to game
    private void enterToGame() {

//        Check is connect network
        if (isNetworkConnected() == false) {
            Toast.makeText(MainActivity.this, "Please turn on wifi or mobile data!", Toast.LENGTH_SHORT).show();

//            Check internet connection
        } else if (isInternetAvailable() == false){
            Toast.makeText(MainActivity.this, "Please  check your internet connection!", Toast.LENGTH_SHORT).show();

        } else {

            showLoading("Checking authentications...");

            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

//                        Check which user logged in
                        String userId = firebaseAuth.getCurrentUser().getUid();
                        ref = FirebaseDatabase.getInstance().getReference("players/"+userId);

                        ref.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                if (snapshot.child("name").exists()) {
                                    loadingDialog.dismiss();
                                    navigateToDashboard();
                                } else {

                                    firebaseAuth.signOut();
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                loadingDialog.dismiss();

                                firebaseAuth.signOut();
                                Toast.makeText(MainActivity.this, "Some error occur! Try again", Toast.LENGTH_SHORT).show();

                            }
                        });

                    }
                },5);

            }
            else {

                loadingDialog.dismiss();
                showLogin();

            }

        }
    }

    //    Method for show loading dialog
    private void showLoading(String msg) {

        TextView textMsg = (TextView) loadingDialog.findViewById(R.id.textMsg);
        textMsg.setText(msg);
        loadingDialog.show();
        loadingDialog.setCancelable(false);
        loadingDialog.setCanceledOnTouchOutside(false);
    }


    //    Method for show login dialog
    private void showLogin() {
        loginDialog.show();

        Button btnLogin = (Button) loginDialog.findViewById(R.id.btnLogin);
        Button btnClose = (Button) loginDialog.findViewById(R.id.btnClose);
        TextView textReg = (TextView) loginDialog.findViewById(R.id.textReg);
        EditText textLoginEmail = (EditText) loginDialog.findViewById(R.id.textLoginEmail);
        EditText textLoginPsw = (EditText) loginDialog.findViewById(R.id.textLoginPsw);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = textLoginEmail.getText().toString();
                String psw = textLoginPsw.getText().toString();

                if (email.isEmpty() || psw.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Text fields are empty", Toast.LENGTH_SHORT).show();
                } else if (!EmailValidator.isValidEmail(email)) {
                    Toast.makeText(MainActivity.this, "Please check your email address", Toast.LENGTH_SHORT).show();
                } else {

                    UserCredentials userCredentials = new UserCredentials(email, psw);
                    textLoginEmail.setText("");
                    textLoginPsw.setText("");

                    loginDialog.dismiss();
                    showLoading("Waiting for login...");

//                   Login
                    firebaseAuth.signInWithEmailAndPassword(userCredentials.getEmail(), userCredentials.getPsw())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {

                                        String userId = firebaseAuth.getCurrentUser().getUid();
                                        ref = FirebaseDatabase.getInstance().getReference("players/"+userId);

                                        ref.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                if (snapshot.child("name").exists()) {
                                                    UserCredentials userCredentials = new UserCredentials(
                                                            snapshot.child("name").getValue().toString());

                                                    loadingDialog.dismiss();
                                                    navigateToDashboard();
                                                } else {

                                                    firebaseAuth.signOut();
                                                }

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                                firebaseAuth.signOut();
                                                loadingDialog.dismiss();
                                                Toast.makeText(MainActivity.this, "Some error occur! Try again", Toast.LENGTH_SHORT).show();

                                            }
                                        });

                                    } else {
                                        loadingDialog.dismiss();
                                        Toast.makeText(MainActivity.this, "Login Failed!", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                }

            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginDialog.dismiss();
            }
        });

        textReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginDialog.dismiss();
                showRegistration();
            }
        });
    }

    //    Method for show registration dialog
    private void showRegistration() {
        regDialog.show();

        Button btnReg = (Button) regDialog.findViewById(R.id.btnReg);
        Button btnClose = (Button) regDialog.findViewById(R.id.btnClose);
        TextView textLogin = (TextView) regDialog.findViewById(R.id.textLogin);
        EditText textRegName = (EditText) regDialog.findViewById(R.id.textRegName);
        EditText textRegEmail = (EditText) regDialog.findViewById(R.id.textRegEmail);
        EditText textRegPsw = (EditText) regDialog.findViewById(R.id.textRegPsw);

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = textRegName.getText().toString();
                String email = textRegEmail.getText().toString();
                String psw = textRegPsw.getText().toString();

                if (name.isEmpty() || email.isEmpty() || psw.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Text fields are empty", Toast.LENGTH_SHORT).show();
                } else if (!EmailValidator.isValidEmail(email)) {
                    Toast.makeText(MainActivity.this, "Please check your email address", Toast.LENGTH_SHORT).show();
                } else if (psw.length() < 6) {
                    Toast.makeText(MainActivity.this, "Please enter at least 6 characters for password", Toast.LENGTH_SHORT).show();
                } else {

                    UserCredentials userCredentials = new UserCredentials(name, email, psw);
                    regDialog.dismiss();
                    showLoading("Waiting for registration...");

                    textRegName.setText("");
                    textRegEmail.setText("");
                    textRegPsw.setText("");


//                    Create account with firebase authentication
                    firebaseAuth.createUserWithEmailAndPassword(userCredentials.getEmail(), userCredentials.getPsw())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()){

//                                       Get user id
                                        String userId = firebaseAuth.getCurrentUser().getUid();
                                        ref = FirebaseDatabase.getInstance().getReference("players/"+userId);

//                                       Insert data to firebase real time database
                                        ref.child("name").setValue(userCredentials.getName());
                                        ref.child("email").setValue(userCredentials.getEmail());
                                        loadingDialog.dismiss();

//                                       Navigate to patient dashboard
                                        navigateToDashboard();

                                    } else {

                                        String msg = task.getException().toString();
                                        Toast.makeText(MainActivity.this, "Error" + msg, Toast.LENGTH_SHORT).show();
                                        loadingDialog.dismiss();

                                    }

                                }
                            });

                }

            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                regDialog.dismiss();
            }
        });

        textLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                regDialog.dismiss();
                showLogin();
            }
        });
    }

    //    Method for open dashboard activity
    private void navigateToDashboard()
    {
        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);

        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    //    Check network connection
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    //    Check internet connection
    private boolean isInternetAvailable() {
        try {
            if (android.os.Build.VERSION.SDK_INT > 9) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                InetAddress ipAddr = InetAddress.getByName("www.google.com");
                return !ipAddr.equals("");
            }
            else {
                return false;
            }

        } catch (Exception e) {
            return false;
        }
    }

}




//    //    Method for show player details
//    private void showPlayerDetails()
//    {
//        ref = FirebaseDatabase.getInstance().getReference("players/IqMxekFZMIPVzrguWscz7SMt3Xt1");
//
////        ref.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
////            @Override
////            public void onComplete(@NonNull Task<DataSnapshot> task) {
////                if (!task.isSuccessful()) {
////                    Log.e("firebase", "Error getting data", task.getException());
////                }
////                else {
////                    Log.d("firebase***", String.valueOf(task.getResult().getValue()));
////                }
////            }
////        });
//
//        ref.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                if (snapshot.child("name").exists()) {
//
//                    System.out.println("****+++ "+ snapshot);
//
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
////                loadingDialog.dismiss();
////
////                firebaseAuth.signOut();
////                Toast.makeText(DashboardActivity.this, "Some error occur! Try again", Toast.LENGTH_SHORT).show();
//
//            }
//        });
//
//    }

//}