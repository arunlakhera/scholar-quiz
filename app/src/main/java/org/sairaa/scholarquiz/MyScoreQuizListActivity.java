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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyScoreQuizListActivity extends AppCompatActivity {

    // Variable for Channel Information
    String channelId;
    String channelName;
    String moderatorName;
    String moderatorId;
    String quizListId;
    String quizTitle;

    ListView quizListView;
    CustomMyScorecardQuizAdapter customAdapter;
    List<QuizList> channelQuizList;
    QuizList quizList = new QuizList();

    // Variable to store Score data
    String correct;
    String notAttempted;
    String score;
    String totalQuestions;
    String wrong;

    String userName;
    String userEmail;

    //Database Reference
    private DatabaseReference mDatabase;
    private DatabaseReference mScoreRef;
    private DatabaseReference mQuizListRef;
    private DatabaseReference mUserRef;

    //private DatabaseReference mSubscriptionListRef;
    private FirebaseUser user;
    String userId;

    Bundle channelBundle;
    Dialog menuDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_score_quiz_listview);

        menuDialog = new Dialog(this);

        // Declare variable to get values passed from channelActivity
        channelBundle = getIntent().getExtras();

        channelId = channelBundle.getString("channelId","Channel ID Default");
        channelName = channelBundle.getString("channelName","Channel Name Default");
        moderatorName = channelBundle.getString("moderatorName","Moderator Name Default");
        moderatorId = channelBundle.getString("moderatorId", "Moderator ID");

        // Put the Title as Channel name
        TextView myQuizTitleTextView = findViewById(R.id.textView_MyQuizTitle);
        myQuizTitleTextView.setText(String.valueOf(channelName));

        // ListView to show list of Quiz User has taken in the channel
        quizListView = findViewById(R.id.listView_Score_Quiz);
        channelQuizList = new ArrayList<>();

        // To get User id of Current user so we can travel to Score of User
        user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();

        // Set Database Reference
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Set Database reference to Score
        mScoreRef = mDatabase.child("SQ_Score/").child(channelId);

        // Set Database Reference to Quiz List
        mQuizListRef = mDatabase.child("SQ_QuizList/").child(channelId);

        // Set Database reference to Users
        mUserRef = mDatabase.child("SQ_Users").child(String.valueOf(user.getUid()));

        mUserRef.child("Name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userName = String.valueOf(dataSnapshot.getValue());
                userEmail = String.valueOf(dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Read all the Scores of Quizzes for this channel by user

        mScoreRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(final DataSnapshot quizScoreSnapshot : dataSnapshot.getChildren() ) {

                    quizListId = String.valueOf(quizScoreSnapshot.getKey());

                    mQuizListRef.child(quizListId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot quizListSnapshot) {

                            quizTitle = String.valueOf(quizListSnapshot.child("Title").getValue());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            System.out.println("The read failed: " + databaseError.getMessage());
                        }
                    });

                    mScoreRef.child(quizListId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot scoreSnapshot) {

                            for(DataSnapshot scoreSnap : scoreSnapshot.getChildren()) {

                                if (userId.equals(String.valueOf(scoreSnap.getKey()))) {

                                    correct = String.valueOf(scoreSnap.child("Correct").getValue());
                                    notAttempted = String.valueOf(scoreSnap.child("NotAttempted").getValue());
                                    score = String.valueOf(scoreSnap.child("Score").getValue());
                                    totalQuestions = String.valueOf(scoreSnap.child("TotalQuestions").getValue());
                                    wrong = String.valueOf(scoreSnap.child("Wrong").getValue());

                                    // Show channels available to user
                                    channelQuizList.add(new QuizList(channelId,moderatorName,moderatorId,quizTitle,quizListId,correct,notAttempted,score,totalQuestions,wrong));
                                    customAdapter = new CustomMyScorecardQuizAdapter(getApplicationContext(), channelQuizList);
                                    quizListView.setAdapter(customAdapter);
                                }

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            System.out.println("The read failed: " + databaseError.getMessage());
                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getMessage());
            }
        });


        // When user selects a channel take user to the activity showing all the quizzes in that activity
        quizListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                // Variable to store selected item
                QuizList itemSelected = channelQuizList.get(i);

                // Variables to store Channel Information to pass to quizList Activity
                quizTitle = String.valueOf(itemSelected.getQuizTitle());
                correct = String.valueOf(itemSelected.getCorrect());
                notAttempted = String.valueOf(itemSelected.getNotAttempted());
                score = String.valueOf(itemSelected.getScore());
                totalQuestions = String.valueOf(itemSelected.getTotalQuestions());
                wrong = String.valueOf(itemSelected.getWrong());

                // quizList Activity Intent
                Intent quizListIntent = new Intent(MyScoreQuizListActivity.this, MyScorecardActivity.class);

                quizListIntent.putExtra("channelId", channelId);
                quizListIntent.putExtra("channelName", channelName);
                quizListIntent.putExtra("moderatorName", moderatorName);
                quizListIntent.putExtra("moderatorId", moderatorId);
                quizListIntent.putExtra("quizTitle", quizTitle);
                quizListIntent.putExtra("correct", correct);
                quizListIntent.putExtra("notAttempted", notAttempted);
                quizListIntent.putExtra("score", score);
                quizListIntent.putExtra("totalQuestions", totalQuestions);
                quizListIntent.putExtra("wrong", wrong);
                quizListIntent.putExtra("userName", userName);
                quizListIntent.putExtra("userEmail", userEmail);
                startActivity(quizListIntent);

            }
        });



        findViewById(R.id.button_Back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyScoreQuizListActivity.this, MyScorecardChannelActivity.class));
            }
        });

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
            startActivity(new Intent(MyScoreQuizListActivity.this, SignInActivity.class));
            finish();
        } else {

            Toast.makeText(MyScoreQuizListActivity.this, "To Log Out, Please Connect your Phone to Internet..", Toast.LENGTH_SHORT).show();

        }
    }

    /**
     * 4. Function to execute when user presses MyChannel
     * */

    public void myChannelPressed(View view) {

        startActivity(new Intent(MyScoreQuizListActivity.this, UserChannelActivity.class));
        finish();
    }

    /**
     * 5. Function to execute when user presses AllChannel
     * */

    public void allChannelPressed(View view) {

        startActivity(new Intent(MyScoreQuizListActivity.this, AllChannelListActivity.class));
        finish();
    }

    /**
     * 6. Function to execute when user presses MyScorecard
     * */

    public void myScorecardPressed(View view) {

        startActivity(new Intent(MyScoreQuizListActivity.this, MyScorecardChannelActivity.class));
        finish();
    }

    /**
     * 7. Function to execute when user presses Leaderboard
     * */

    public void leaderboardPressed(View view) {

        startActivity(new Intent(MyScoreQuizListActivity.this, LeaderboardChannelActivity.class));
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