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
import android.widget.Button;
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

public class QuizListActivity extends AppCompatActivity {

    // Variable for Channel Information
    String channelId;
    String channelName;
    String moderatorName;
    String moderatorId;
    String quizListKey;
    String quizTitle;

    /*
    // To Add Quiz
    private DatabaseReference mQuizRef;
    DatabaseReference mDatabase;
    private DatabaseReference mChannelListRef;
    */

    ListView quizListView;
    DatabaseReference mDatabase;
    CustomQuizListAdaptor customAdapter;
    List<QuizList> channelQuizList;
    QuizList quizList = new QuizList();

    //Database Reference
    private DatabaseReference mChannelRef;
    private DatabaseReference mQuizListRef;
    private DatabaseReference mSubscriptionListRef;
    private FirebaseUser user;
    String userId;

    Dialog menuDialog;

    Bundle channelBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_list_listview);

        menuDialog = new Dialog(this);

        // Declare variable to get values passed from channelActivity
        channelBundle = getIntent().getExtras();

        channelId = channelBundle.getString("channelId","Channel ID Default");
        channelName = channelBundle.getString("channelName","Channel Name Default");
        moderatorName = channelBundle.getString("moderatorName","Moderator Name Default");
        moderatorId = channelBundle.getString("moderatorId", "Moderator ID");

        TextView title_TextView = findViewById(R.id.textView_Title);
        title_TextView.setText(String.valueOf(channelName));

        // ListView to show list of channels available
        quizListView = findViewById(R.id.listView_QuizList);
        channelQuizList = new ArrayList<>();

        // To get User id of Current user so we can travel to Subscription List of User
        user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();

        Button createQuizButton = findViewById(R.id.button_CreateQuiz);

        /**
         *  If User is also the Moderator of the Channel selected then only show Create Quiz button else
         *  hide the Create Quiz button for that channel
         * */

        if(userId.equals(moderatorId)) {
            createQuizButton.setVisibility(View.VISIBLE);
        }else {
            createQuizButton.setVisibility(View.INVISIBLE);
        }

        // Set Database Reference to Channel List
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mChannelRef = mDatabase.child("SQ_ChannelList/" + channelId);

        // Set Database Reference to Quiz List
        mQuizListRef = mDatabase.child("SQ_QuizList/" + channelId + "/");

        // Read all the quizzes available in the channel
        mQuizListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot quizSnapshot) {

                // Check the count of Channels to subscribe to. If no channel, show message else show list of channels
                if (quizSnapshot.getChildrenCount() < 1) {
                    Toast.makeText(QuizListActivity.this, "No Quiz Available right now. Please try back later!!", Toast.LENGTH_LONG).show();

                } else {

                    String quizChannelId;

                    // Loop through all the quiz
                    for ( DataSnapshot quizListSnapshot : quizSnapshot.getChildren()) {

                        quizChannelId = String.valueOf(quizListSnapshot.child("ChannelID").getValue());

                        if (quizChannelId.equals(channelId)) {

                            quizListKey = String.valueOf(quizListSnapshot.getKey());
                            quizTitle = String.valueOf(quizListSnapshot.child("Title").getValue());

                            quizList = quizListSnapshot.getValue(QuizList.class);

                            // Show channels available to user
                            channelQuizList.add(new QuizList(channelId,moderatorName,moderatorId,quizTitle,quizListKey));
                            customAdapter = new CustomQuizListAdaptor(getApplicationContext(), channelQuizList);
                            quizListView.setAdapter(customAdapter);

                        }
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(QuizListActivity.this, "Database Error", Toast.LENGTH_LONG).show();
            }
        });

        // When user selects a channel take user to the activity showing all the quizzes in that activity
        quizListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if(userId.equals(moderatorId)) {
                    Toast.makeText(QuizListActivity.this, "You cannot participate in this quiz as you have created it!! Please try other quiz.", Toast.LENGTH_LONG).show();
                }else {

                    // Variable to store selected item
                    QuizList itemSelected = channelQuizList.get(i);

                    quizListKey = String.valueOf(itemSelected.getquizListId());
                    quizTitle = String.valueOf(itemSelected.getQuizTitle());

                    // quizList Activity Intent
                    Intent quizListToQuizIntent = new Intent(QuizListActivity.this, QuizActivity.class);

                    quizListToQuizIntent.putExtra("channelId", channelId);
                    quizListToQuizIntent.putExtra("channelName", channelName);
                    quizListToQuizIntent.putExtra("moderatorName", moderatorName);
                    quizListToQuizIntent.putExtra("moderatorId", moderatorId);
                    quizListToQuizIntent.putExtra("quizListKey", quizListKey);
                    quizListToQuizIntent.putExtra("quizTitle", quizTitle);

                    startActivity(quizListToQuizIntent);
                }
            }
        });

        if (!isNetworkAvailable()) {
            Toast.makeText(QuizListActivity.this,"To view Quizzes available, Please Connect your Phone to Internet..",Toast.LENGTH_SHORT).show();
        }

        /**
         * Action to perform when back button is clicked
         * */
        findViewById(R.id.button_Back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent quizListToUserChannelIntent = new Intent(QuizListActivity.this,UserChannelActivity.class);

                quizListToUserChannelIntent.putExtra("channelId",channelId);
                quizListToUserChannelIntent.putExtra("channelName",channelName);
                quizListToUserChannelIntent.putExtra("moderatorName",moderatorName);
                quizListToUserChannelIntent.putExtra("moderatorId",moderatorId);


                startActivity(quizListToUserChannelIntent);
                finish();
            }
        });


        findViewById(R.id.button_CreateQuiz).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createQuizIntent = new Intent(QuizListActivity.this, CreateQuizActivity.class);
                createQuizIntent.putExtra("channelId",channelId);
                createQuizIntent.putExtra("channelName",channelName);
                createQuizIntent.putExtra("moderatorName",moderatorName);
                createQuizIntent.putExtra("moderatorId",moderatorId);

                startActivity(createQuizIntent);
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
            startActivity(new Intent(QuizListActivity.this, SignInActivity.class));
            finish();
        } else {

            Toast.makeText(QuizListActivity.this, "To Log Out, Please Connect your Phone to Internet..", Toast.LENGTH_SHORT).show();

        }
    }

    /**
     * 4. Function to execute when user presses MyChannel
     * */

    public void myChannelPressed(View view) {

        startActivity(new Intent(QuizListActivity.this, UserChannelActivity.class));
        finish();
    }

    /**
     * 5. Function to execute when user presses AllChannel
     * */

    public void allChannelPressed(View view) {

        startActivity(new Intent(QuizListActivity.this, AllChannelListActivity.class));
        finish();
    }

    /**
     * 6. Function to execute when user presses MyScorecard
     * */

    public void myScorecardPressed(View view) {

        startActivity(new Intent(QuizListActivity.this, MyScorecardChannelActivity.class));
        finish();
    }

    /**
     * 7. Function to execute when user presses Leaderboard
     * */

    public void leaderboardPressed(View view) {

        startActivity(new Intent(QuizListActivity.this, LeaderboardChannelActivity.class));
        finish();
    }
    /**
     * Function to check if Device is connected to Internet
     * */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}