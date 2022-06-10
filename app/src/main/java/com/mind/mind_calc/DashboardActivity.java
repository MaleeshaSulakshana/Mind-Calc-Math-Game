package com.mind.mind_calc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DashboardActivity extends AppCompatActivity {

    boolean doubleBackToExitPressedOnce = false;
    private Dialog exitDialog, loadingDialog, leaderBoardDialog;

    private TextView playerScore, playerName;
    private Button btnEasy, btnMedium, btnHard;
    private ImageButton btnScoreBoard, btnLogout;

    private String name="", level="", userId="", won="", loss="", rounds="";

    private FirebaseAuth firebaseAuth;
    private DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //        Get layouts
        exitDialog = new Dialog(DashboardActivity.this);
        exitDialog.setContentView(R.layout.exit_dialog_box);

        loadingDialog = new Dialog(DashboardActivity.this);
        loadingDialog.setContentView(R.layout.loading_dialog_box);

        leaderBoardDialog = new Dialog(DashboardActivity.this);
        leaderBoardDialog.setContentView(R.layout.leader_board_dialog_box);

        playerScore = (TextView) this.findViewById(R.id.playerScore);
        playerName = (TextView) this.findViewById(R.id.playerName);

        btnEasy = (Button) this.findViewById(R.id.btnEasy);
        btnMedium = (Button) this.findViewById(R.id.btnMedium);
        btnHard = (Button) this.findViewById(R.id.btnHard);

        btnScoreBoard = (ImageButton) this.findViewById(R.id.btnScoreBoard);
        btnLogout = (ImageButton) this.findViewById(R.id.btnLogout);

        firebaseAuth = FirebaseAuth.getInstance();
        userId = firebaseAuth.getCurrentUser().getUid();

//        Show player details
        showPlayerDetails();

//        Button events
        btnEasy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToPlayActivity("LevelEasy");
            }
        });

        btnMedium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToPlayActivity("LevelMedium");
            }
        });

        btnHard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToPlayActivity("LevelHard");
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        btnScoreBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLeaderBoard();
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

    //    Method for show player details
    private void showPlayerDetails()
    {
        ref = FirebaseDatabase.getInstance().getReference("players/"+userId);

//        ref.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DataSnapshot> task) {
//                if (!task.isSuccessful()) {
//                    Log.e("firebase", "Error getting data", task.getException());
//                }
//                else {
//                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
//                }
//            }
//        });

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.child("name").exists()) {

                    loadingDialog.dismiss();
                    name = snapshot.child("name").getValue().toString();
                    playerName.setText(name);

                    if (snapshot.child("game").exists()) {
                        level = snapshot.child("game").child("level").getValue().toString();
                        won = snapshot.child("game").child("won").getValue().toString();
                        loss = snapshot.child("game").child("loss").getValue().toString();
                        rounds = snapshot.child("game").child("rounds").getValue().toString();
                    } else {
                        won = "0";
                        loss = "0";
                        rounds = "0";
                        level = "0";
                    }

                    playerScore.setText(level);

                } else {
                    firebaseAuth.signOut();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingDialog.dismiss();

                firebaseAuth.signOut();
                Toast.makeText(DashboardActivity.this, "Some error occur! Try again", Toast.LENGTH_SHORT).show();

            }
        });

    }

    //    Method for open game play activity
    private void navigateToPlayActivity(String mode)
    {
        if (name.equals("")){
            Toast.makeText(DashboardActivity.this, "Waiting for details!", Toast.LENGTH_SHORT).show();

        } else {
            Intent intent = new Intent(DashboardActivity.this, GamePlayActivity.class);
            intent.putExtra("mode", mode);

            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    //    Method for sign out user
    private void signOut()
    {
        firebaseAuth.signOut();
        Intent intent = new Intent(DashboardActivity.this, MainActivity.class);

        startActivity(intent);

        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void showLeaderBoard()
    {
        leaderBoardDialog.show();

        Button btnClose = (Button) leaderBoardDialog.findViewById(R.id.btnClose);
        ListView leaderList = (ListView) leaderBoardDialog.findViewById(R.id.leaderList);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaderBoardDialog.dismiss();
            }
        });

    }

}