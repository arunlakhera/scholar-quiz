package org.sairaa.scholarquiz;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class QuizScoreActivity extends AppCompatActivity {

    String channelId;
    String channelName;
    String moderatorName;
    String quizListKey;
    String quizTitle;
    String totalQuestions;
    String correctAnswers;
    String totalNotAttemptedAnswers;
    String score;
    String userName;
    String userEmail;

    Dialog menuDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_score);

        menuDialog = new Dialog(this);

        // Declare variable to get values passed from channelActivity
        Bundle quizScoreBundle = getIntent().getExtras();

        channelId = quizScoreBundle.getString("channelId", "Channel ID Default");
        channelName = quizScoreBundle.getString("channelName", "Channel Name Default");
        moderatorName = quizScoreBundle.getString("moderatorName", "Moderator Name Default");
        quizListKey = quizScoreBundle.getString("quizListKey", "Quiz Key");
        quizTitle = quizScoreBundle.getString("quizTitle", "Quiz Title");
        totalQuestions = quizScoreBundle.getString("totalQuestions","Total Questions");
        correctAnswers = quizScoreBundle.getString("correctAnswers","Correct Answers");
        totalNotAttemptedAnswers = quizScoreBundle.getString("totalNotAttemptedAnswers", "Not Attempted");
        score = quizScoreBundle.getString("score","Score");
        userName = quizScoreBundle.getString("userName","");
        userEmail =quizScoreBundle.getString("userEmail","User Email");

        showScore();

    }

    public void showScore(){

        Integer totalQues = Integer.valueOf(totalQuestions);
        Integer correctAns = Integer.valueOf(correctAnswers);
        Integer totalNotAttempted = Integer.valueOf(totalNotAttemptedAnswers);
        Integer wrongAns = (totalQues - (correctAns + totalNotAttempted));

        // Set the Title of the Quiz
        TextView quizTitleTextView = findViewById(R.id.textView_QuizTitle);
        quizTitleTextView.setText(quizTitle.toUpperCase() + " SCORE");

        //Set Name of User
        TextView nameTextView = findViewById(R.id.name_TextView);

        if(!userName.isEmpty()) {
            nameTextView.setText(userName.toUpperCase());
        }else {
            nameTextView.setText(userEmail.toUpperCase());
        }

        //Set Total Questions
        TextView totalQuestionTextView = findViewById(R.id.total_questions_TextView);
        totalQuestionTextView.setText(totalQuestions);

        //Set Correct Answers
        TextView correctAnswersTextView = findViewById(R.id.correct_answers_TextView);
        correctAnswersTextView.setText(correctAnswers);

        //Set Wrong Answers
        TextView wrongAnswersTextView = findViewById(R.id.wrong_Answers_TextView);
        wrongAnswersTextView.setText(String.valueOf(wrongAns));

        //Set Not Attempted Answers
        TextView notAttemptedTextView = findViewById(R.id.notAttempted_TextView);
        notAttemptedTextView.setText(totalNotAttemptedAnswers);


        //Set Not Attempted Answers
        TextView scoreTextView = findViewById(R.id.score_TextView);
        //scoreTextView.setText(score + " Points");

        // Setting RatingBar
        RatingBar scoreRatingBar = (RatingBar) findViewById(R.id.score_RatingBar);

        Integer totalpercent = Integer.valueOf((correctAns * 100)/totalQues);
        scoreTextView.setText(score + " Points");

        if (totalpercent > 90) {
            // 5 star
            scoreRatingBar.setRating(5);

        }else if (totalpercent > 80) {
            // 4.5 star
            scoreRatingBar.setRating((float) 4.5);

        }else if (totalpercent > 70) {
            // 4 star
            scoreRatingBar.setRating(4);

        }else if (totalpercent > 60) {
            // 3.5 star
            scoreRatingBar.setRating((float) 3.5);

        }else if (totalpercent > 50) {
            // 3 star
            scoreRatingBar.setRating(3);

        }else if (totalpercent > 40) {
            // 2 star
            scoreRatingBar.setRating(2);
        }else {
            // 1 star
            scoreRatingBar.setRating(1);
        }


    }

    public void moreQuizButton (View view) {

        Intent moreQuizIntent = new Intent(QuizScoreActivity.this,QuizListActivity.class);
        moreQuizIntent.putExtra("channelId",channelId);
        moreQuizIntent.putExtra("channelName",channelName);
        moreQuizIntent.putExtra("moderatorName",moderatorName);
        startActivity(moreQuizIntent);
    }


    /**
     * Menu Functions
     * */

    /**
     * 1. Function to Show Popup Menu
     * */
    public void showPopUp(View view) {

        menuDialog.setContentView(R.layout.menupopup);
        menuDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        menuDialog.show();
    }

    /**
     * 2. Function to close Popup Menu
     * */
    public void closeMenu(View view) {
        menuDialog.dismiss();
    }

    /**
     * 3. Function to Show logout user
     * */

    public void logout(View view) {
        if (isNetworkAvailable()) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(QuizScoreActivity.this, SignInActivity.class));
            finish();
        } else {

            Toast.makeText(QuizScoreActivity.this, "To Log Out, Please Connect your Phone to Internet..", Toast.LENGTH_SHORT).show();

        }
    }

    /**
     * 4. Function to execute when user presses MyChannel
     * */

    public void myChannelPressed(View view) {

        startActivity(new Intent(QuizScoreActivity.this, UserChannelActivity.class));
        finish();
    }

    /**
     * 5. Function to execute when user presses AllChannel
     * */

    public void allChannelPressed(View view) {

        startActivity(new Intent(QuizScoreActivity.this, AllChannelListActivity.class));
        finish();
    }

    /**
     * 6. Function to execute when user presses MyScorecard
     * */

    public void myScorecardPressed(View view) {

        startActivity(new Intent(QuizScoreActivity.this, MyScorecardChannelActivity.class));
        finish();
    }

    /**
     * 7. Function to execute when user presses Leaderboard
     * */

    public void leaderboardPressed(View view) {

        startActivity(new Intent(QuizScoreActivity.this, LeaderboardChannelActivity.class));
        finish();
    }

    /**
     * Function to check if Device is connected to Internet
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }



}
