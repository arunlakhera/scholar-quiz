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
import android.widget.ListView;
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

public class LeaderScoreboardActivity extends AppCompatActivity {

    // Variable for Channel Information
    String channelId;
    String channelName;
    String moderatorName;
    String moderatorId;
    String quizListId;
    String quizTitle;

    String userName;

    // Variable to store Score data
    String correct;
    String notAttempted;
    String score;
    String totalQuestions;
    String wrong;
    Bundle channelBundle;

    ListView quizListView;
    CustomLeaderScoreboardAdapter customAdapter;
    List<QuizList> channelQuizList;
    QuizList quizList = new QuizList();

    //Database Reference
    DatabaseReference mDatabase;
    private DatabaseReference mScoreListRef;
    private DatabaseReference mQuizScoreRef;
    private DatabaseReference mUserRef;
    private FirebaseUser user;

    String userId;
    Dialog menuDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_scoreboard_listview);

        menuDialog = new Dialog(this);

        // ListView to show list of Quiz User has taken in the channel
        quizListView = findViewById(R.id.listView_Scoreboard);
        channelQuizList = new ArrayList<>();

        // Declare variable to get values passed from channelActivity
        channelBundle = getIntent().getExtras();

        channelId = channelBundle.getString("channelId","Channel ID Default");
        channelName = channelBundle.getString("channelName","Channel Name Default");
        moderatorName = channelBundle.getString("moderatorName","Moderator Name Default");
        moderatorId = channelBundle.getString("moderatorId", "Moderator ID");
        quizListId = channelBundle.getString("quizListKey", "Quiz List ID");
        quizTitle = channelBundle.getString("quizTitle", "Quiz Title");

        // Set Database Reference to Channel List
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mScoreListRef = mDatabase.child("SQ_Score/").child(channelId).child(quizListId);
        // mQuizScoreRef = mDatabase.child("SQ_Score/");
        mUserRef = mDatabase.child("SQ_Users/");


        mScoreListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot scoreUserListSnapshot) {

                for(final DataSnapshot scoreUserSnapshot : scoreUserListSnapshot.getChildren()) {

                    userId = String.valueOf(scoreUserSnapshot.getKey());

                    mUserRef.child(userId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            userName = String.valueOf(dataSnapshot.child("Name").getValue());

                            correct = String.valueOf(scoreUserSnapshot.child("Correct").getValue());
                            notAttempted = String.valueOf(scoreUserSnapshot.child("NotAttempted").getValue());
                            score = String.valueOf(scoreUserSnapshot.child("Score").getValue());
                            totalQuestions = String.valueOf(scoreUserSnapshot.child("TotalQuestions").getValue());
                            wrong = String.valueOf(scoreUserSnapshot.child("Wrong").getValue());

                            // Show channels available to user
                            channelQuizList.add(new QuizList(channelId, moderatorName, moderatorId, quizTitle, quizListId, correct, notAttempted, score, totalQuestions, wrong, userName));
                            customAdapter = new CustomLeaderScoreboardAdapter(getApplicationContext(), channelQuizList);
                            quizListView.setAdapter(customAdapter);
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

        /**
         * Action to perform when back button is clicked
         * */
        findViewById(R.id.button_Back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent quizListToUserChannelIntent = new Intent(LeaderScoreboardActivity.this,LeaderboardQuizListActivity.class);

                quizListToUserChannelIntent.putExtra("channelId",channelId);
                quizListToUserChannelIntent.putExtra("channelName",channelName);
                quizListToUserChannelIntent.putExtra("moderatorName",moderatorName);
                quizListToUserChannelIntent.putExtra("moderatorId",moderatorId);

                startActivity(quizListToUserChannelIntent);
                finish();
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
            startActivity(new Intent(LeaderScoreboardActivity.this, SignInActivity.class));
            finish();
        } else {

            Toast.makeText(LeaderScoreboardActivity.this, "To Log Out, Please Connect your Phone to Internet..", Toast.LENGTH_SHORT).show();

        }
    }

    /**
     * 4. Function to execute when user presses MyChannel
     * */

    public void myChannelPressed(View view) {

        startActivity(new Intent(LeaderScoreboardActivity.this, UserChannelActivity.class));
        finish();
    }

    /**
     * 5. Function to execute when user presses AllChannel
     * */

    public void allChannelPressed(View view) {

        startActivity(new Intent(LeaderScoreboardActivity.this, AllChannelListActivity.class));
        finish();
    }

    /**
     * 6. Function to execute when user presses MyScorecard
     * */

    public void myScorecardPressed(View view) {

        startActivity(new Intent(LeaderScoreboardActivity.this, MyScorecardChannelActivity.class));
        finish();
    }

    /**
     * 7. Function to execute when user presses Leaderboard
     * */

    public void leaderboardPressed(View view) {

        startActivity(new Intent(LeaderScoreboardActivity.this, LeaderboardChannelActivity.class));
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