package com.mind.mind_calc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mind.mind_calc.Constructors.GameRounds;
import com.mind.mind_calc.Question.LevelEasy;
import com.mind.mind_calc.Question.LevelHard;
import com.mind.mind_calc.Question.LevelMedium;

public class GamePlayActivity extends AppCompatActivity {

    boolean doubleBackToExitPressedOnce = false;
    private Dialog exitDialog;

    private TextView playerScore, playerName, textQuestion, textTime;
    private EditText textAnswer;
    private Button btnSubmitAnswer;
    private ImageButton btnDashboard;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference ref;

    private CountDownTimer countDownTimer;

    private String mode = "", playerId = "", name = "", level = "", won="", loss="", rounds="",
            question="", displayQuestionValue="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_play);


        //        Get put extra value
        Intent intent = getIntent();
        mode = intent.getStringExtra("mode");

        firebaseAuth = FirebaseAuth.getInstance();
        playerId = firebaseAuth.getCurrentUser().getUid();

//        Get layouts
        exitDialog = new Dialog(GamePlayActivity.this);
        exitDialog.setContentView(R.layout.exit_dialog_box);

        playerScore = (TextView) this.findViewById(R.id.playerScore);
        playerName = (TextView) this.findViewById(R.id.playerName);
        textQuestion = (TextView) this.findViewById(R.id.textQuestion);
        textTime = (TextView) this.findViewById(R.id.textTime);
        textAnswer = (EditText) this.findViewById(R.id.textAnswer);
        btnSubmitAnswer = (Button) this.findViewById(R.id.btnSubmitAnswer);
        btnDashboard = (ImageButton) this.findViewById(R.id.btnDashboard);

//        Show player details
        showPlayerDetails();

        String value = questionCreator(mode);

//        Button events
        btnDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showExitDialog();
            }
        });

        btnSubmitAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answerValidator();
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

    //    Tap to go back
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        showExitDialog();
    }

    //    Method for open dashboard activity
    private void showExitDialog()
    {
        exitDialog.show();

        Button btnExitYes = (Button) exitDialog.findViewById(R.id.btnYes);
        Button btnExitNo = (Button) exitDialog.findViewById(R.id.btnNo);
        TextView textView = (TextView) exitDialog.findViewById(R.id.textView);

        textView.setText("Do you want exit game?");

        btnExitYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToDashboard();
            }
        });

        btnExitNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitDialog.dismiss();
            }
        });

    }

    private void navigateToDashboard()
    {
        Intent intent = new Intent(GamePlayActivity.this, DashboardActivity.class);

        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    //    Method for show player details
    private void showPlayerDetails()
    {
        ref = FirebaseDatabase.getInstance().getReference("players/"+playerId);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.child("name").exists()) {

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

                firebaseAuth.signOut();
                Toast.makeText(GamePlayActivity.this, "Some error occur! Try again", Toast.LENGTH_SHORT).show();

            }
        });

    }

    private String questionCreator(String mode)
    {
        textAnswer.setText("");
        textAnswer.setFocusable(true);

        String value = "";
        String displayValue = "";

        if (mode.equals("LevelEasy")) {

            LevelEasy levelEasy = new LevelEasy();
            value = levelEasy.generateQuestion();
            countDownTimer(25000);

        } else if (mode.equals("LevelMedium")) {

            LevelMedium levelMedium = new LevelMedium();
            value = levelMedium.generateQuestion();
            countDownTimer(20000);

        } else if (mode.equals("LevelHard")) {

            LevelHard levelHard = new LevelHard();
            value = levelHard.generateQuestion();
            countDownTimer(15000);

        }

        question = value;

//        Replace unicode characters
        displayValue = value.replace("/","\u00F7");
        displayValue = displayValue.replace("*","\u00D7");

        displayQuestionValue = displayValue;

        textQuestion.setText(displayValue+" = ?");
        return value;
    }

    //    Method for count down
    private void countDownTimer(int maxTime)
    {
        countDownTimer = new CountDownTimer(maxTime+1000, 1000) {

            public void onTick(long millisUntilFinished) {

                if ((millisUntilFinished / 1000) < 6) {
                    textTime.setTextColor(getResources().getColor(R.color.red1));
                } else {
                    textTime.setTextColor(getResources().getColor(R.color.gray1));
                }

                if ((millisUntilFinished / 1000) < 10) {
                    textTime.setText("00:0" + millisUntilFinished / 1000);
                } else {
                    textTime.setText("00:" + millisUntilFinished / 1000);
                }

            }

            public void onFinish() {
                textAnswer.setText("");
                textAnswer.setFocusable(false);
                textTime.setText("Time Out!");

                showTimeOutDialogBox();

            }

        }.start();
    }

    //    Method for show show time out dialog box
    private void showTimeOutDialogBox()
    {
        Dialog timeOutDialog = new Dialog(GamePlayActivity.this);
        timeOutDialog.setContentView(R.layout.time_out_dialog_box);

        timeOutDialog.show();
        timeOutDialog.setCancelable(false);
        timeOutDialog.setCanceledOnTouchOutside(false);

        Button btnExit = (Button) timeOutDialog.findViewById(R.id.btnExit);
        Button btnTryAgain = (Button) timeOutDialog.findViewById(R.id.btnTryAgain);

        addScore(mode,"wrong");

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeOutDialog.dismiss();
                navigateToDashboard();
            }
        });

        btnTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeOutDialog.dismiss();
                questionCreator(mode);
            }
        });
    }

    //    Method for replace values
    private String replaceCharacters(String value)
    {
        String replaced = "";
        replaced = value.replace("/","%2F");
        replaced = replaced.replace("+","%2B");

        return replaced;
    }

    //    Method for get http response and validate answer
    private void answerValidator()
    {
        String answer = textAnswer.getText().toString();

        if (answer.isEmpty()) {
            Toast.makeText(GamePlayActivity.this,"Please enter your answer!", Toast.LENGTH_SHORT).show();
        } else {

            Dialog loadingDialog = new Dialog(GamePlayActivity.this);
            loadingDialog.setContentView(R.layout.loading_dialog_box);

            TextView textMsg = (TextView) loadingDialog.findViewById(R.id.textMsg);
            textMsg.setText("Answer Validating...");

            loadingDialog.show();
            loadingDialog.setCancelable(false);
            loadingDialog.setCanceledOnTouchOutside(false);

            countDownTimer.cancel();
            textTime.setTextColor(getResources().getColor(R.color.gray1));
            textTime.setText("00:00");

            answer = answer.replace(" ","");

            String replacedQuestion = replaceCharacters(question);

//            Get html response
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String getUrl = "https://api.mathjs.org/v4/?expr="+replacedQuestion;

            String finalAnswer = answer;
            StringRequest getRequest = new StringRequest(Request.Method.GET, getUrl, new Response.Listener<String>() {
                @Override
                public void onResponse (String response) {

                    if (finalAnswer.equals(response.toString())) {
                        addScore(mode,"correct");
                        loadingDialog.dismiss();
                        showAnswerCorrectDialog();

                    } else {
                        addScore(mode,"wrong");
                        loadingDialog.dismiss();
                        showAnswerWrongDialog(displayQuestionValue+" = "+ response.toString());
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse (VolleyError error) {
                    System.out.println(error.getMessage());
                }
            });
            requestQueue.add(getRequest);

        }
    }

    //    Method for answer correct dialog box
    private void showAnswerCorrectDialog()
    {
        Dialog correctAnswerDialog = new Dialog(GamePlayActivity.this);
        correctAnswerDialog.setContentView(R.layout.answer_correct_dialog_box);

        correctAnswerDialog.show();
        correctAnswerDialog.setCancelable(false);
        correctAnswerDialog.setCanceledOnTouchOutside(false);

        Button btnExit = (Button) correctAnswerDialog.findViewById(R.id.btnExit);
        Button btnNextRound = (Button) correctAnswerDialog.findViewById(R.id.btnNextRound);

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                correctAnswerDialog.dismiss();
                navigateToDashboard();
            }
        });

        btnNextRound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                correctAnswerDialog.dismiss();
                questionCreator(mode);
            }
        });
    }

    //    Method for answer wrong dialog box
    private void showAnswerWrongDialog(String value)
    {
        Dialog wrongAnswerDialog = new Dialog(GamePlayActivity.this);
        wrongAnswerDialog.setContentView(R.layout.answer_wrong_dialog_box);

        TextView textQuestion = (TextView) wrongAnswerDialog.findViewById(R.id.textQuestion);
        Button btnExit = (Button) wrongAnswerDialog.findViewById(R.id.btnExit);
        Button btnTryAgain = (Button) wrongAnswerDialog.findViewById(R.id.btnTryAgain);

        textQuestion.setText(value);

        wrongAnswerDialog.show();
        wrongAnswerDialog.setCancelable(false);
        wrongAnswerDialog.setCanceledOnTouchOutside(false);

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wrongAnswerDialog.dismiss();
                navigateToDashboard();
            }
        });

        btnTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wrongAnswerDialog.dismiss();
                questionCreator(mode);
            }
        });
    }

    //    Method for update firebase
    private void addScore(String mode, String status)
    {

        int wonCount = Integer.parseInt(won);
        int lossCount = Integer.parseInt(loss);
        int roundsCount = Integer.parseInt(rounds);
        int levelCount = Integer.parseInt(level);

        roundsCount = roundsCount + 1;

        if (status.equals("correct")) {
            wonCount = wonCount + 1;
        } else {
            lossCount = lossCount + 1;
        }

        if (mode.equals("LevelEasy")) {

            if (status.equals("correct")) {
                levelCount = levelCount + 1;
            } else {
                levelCount = levelCount - 1;
            }

        } else if (mode.equals("LevelMedium")) {

            if (status.equals("correct")) {
                levelCount = levelCount + 2;
            } else {
                levelCount = levelCount - 2;
            }

        } else if (mode.equals("LevelHard")) {

            if (status.equals("correct")) {
                levelCount = levelCount + 3;
            } else {
                levelCount = levelCount - 3;
            }

        }

        ref = FirebaseDatabase.getInstance().getReference("players/"+playerId);
        GameRounds gameRounds = new GameRounds(wonCount, lossCount, roundsCount, levelCount);

        int finalWonCount = wonCount;
        int finalLossCount = lossCount;
        int finalRoundsCount = roundsCount;
        int finalLevelCount = levelCount;

        ref.child("game").setValue(gameRounds).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    won = String.valueOf(finalWonCount);
                    loss = String.valueOf(finalLossCount);
                    rounds = String.valueOf(finalRoundsCount);
                    level = String.valueOf(finalLevelCount);

                    playerScore.setText(level);
                }
            }
        });
    }

}