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

public class LeaderboardChannelActivity extends AppCompatActivity {


    ListView leaderboardChannelListview;
    CustomLeaderboardChannelAdapter customAdapter;
    List<ChannelList> leaderboardChannelList;
    ChannelList leaderboardChannel = new ChannelList();
    String channelExist = "N";

    //Database Reference
    private DatabaseReference mDatabase;
    private DatabaseReference mChannelRef;
    private DatabaseReference mScoreChannelRef;
    private DatabaseReference mQuizListRef;
    private DatabaseReference mSubscriptionListRef;
    FirebaseUser user;

    // Variables to store information of user
    String channelId;
    String channelName;
    String moderatorName;
    String moderatorId;
    String totalNumberOfQuiz;
    String totalUserQuiz;

    Dialog menuDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard_channel_listview);

        menuDialog = new Dialog(this);

        // ListView to show list of channels available
        leaderboardChannelListview = findViewById(R.id.listView_leaderboard_Channel);
        leaderboardChannelList = new ArrayList<>();

        // To get User id of Current user so we can travel to Subscription List of User
        user = FirebaseAuth.getInstance().getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mSubscriptionListRef = mDatabase.child("SQ_Subscription/" + String.valueOf(user.getUid()));

        mChannelRef = mDatabase.child("SQ_ChannelList/");

        if (!isNetworkAvailable()) {
            Toast.makeText(LeaderboardChannelActivity.this, "To view your channels, Please Connect your Phone to Internet..", Toast.LENGTH_SHORT).show();
        }

        // Read all the channels name from Firebase
        mChannelRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot channelSnapshot) {

                // Check the count of Channels to subscribe to. If no channel, show message else show list of channels
                if (channelSnapshot.getChildrenCount() < 1) {
                    Toast.makeText(LeaderboardChannelActivity.this, "Not Subscribed to any Channel!!", Toast.LENGTH_LONG).show();

                } else {

                    // Loop through all channel lists
                    for (final DataSnapshot channelListSnapshot : channelSnapshot.getChildren()) {

                        // Read all the channels that user has subscribed to
                        mSubscriptionListRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot subscriptionSnapshot) {

                                for (DataSnapshot subscriptionListSnapshot : subscriptionSnapshot.getChildren()) {

                                    // If channel is already subscribed to by user set the channelExist to Y
                                    if (channelListSnapshot.getKey().equals(subscriptionListSnapshot.getKey())) {
                                        channelExist = "Y";
                                        break;
                                    } else {
                                        channelExist = "N";
                                    }

                                }

                                // If channelExist = Y i.e channel is  subscribed then add it to channel list adapter to show it to user else set it to N
                                if (channelExist.equals("Y")) {

                                    channelId = String.valueOf(channelListSnapshot.getKey());

                                    // Show channels available to user
                                    leaderboardChannel = channelListSnapshot.getValue(ChannelList.class);
                                    leaderboardChannelList.add(new ChannelList(leaderboardChannel.getModeratorName(), leaderboardChannel.getModeratorID(), leaderboardChannel.getChannelName(), channelId));

                                    customAdapter = new CustomLeaderboardChannelAdapter(getApplicationContext(), leaderboardChannelList);
                                    leaderboardChannelListview.setAdapter(customAdapter);

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                System.out.println("The read failed: " + databaseError.getMessage());
                            }
                        });
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getMessage());
            }

        });

        // When user selects a channel take user to the activity showing all the quizzes in that activity
        leaderboardChannelListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                // Variable to store selected item
                ChannelList itemSelected = leaderboardChannelList.get(i);

                // Variables to store Channel Information to pass to quizList Activity
                channelId = String.valueOf(itemSelected.getChannelId());
                channelName = String.valueOf(itemSelected.getChannelName());
                moderatorName = String.valueOf(itemSelected.getModeratorName());
                moderatorId = String.valueOf(itemSelected.getModeratorID());

                // quizList Activity Intent
                Intent quizListIntent = new Intent(LeaderboardChannelActivity.this, LeaderboardQuizListActivity.class);

                quizListIntent.putExtra("channelId", channelId);
                quizListIntent.putExtra("channelName", channelName);
                quizListIntent.putExtra("moderatorName", moderatorName);
                quizListIntent.putExtra("moderatorId", moderatorId);
                startActivity(quizListIntent);

            }
        });

        findViewById(R.id.button_Back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LeaderboardChannelActivity.this, HomeActivity.class));
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
            startActivity(new Intent(LeaderboardChannelActivity.this, SignInActivity.class));
            finish();
        } else {

            Toast.makeText(LeaderboardChannelActivity.this, "To Log Out, Please Connect your Phone to Internet..", Toast.LENGTH_SHORT).show();

        }
    }

    /**
     * 4. Function to execute when user presses MyChannel
     * */

    public void myChannelPressed(View view) {

        startActivity(new Intent(LeaderboardChannelActivity.this, UserChannelActivity.class));
        finish();
    }

    /**
     * 5. Function to execute when user presses AllChannel
     * */

    public void allChannelPressed(View view) {

        startActivity(new Intent(LeaderboardChannelActivity.this, AllChannelListActivity.class));
        finish();
    }

    /**
     * 6. Function to execute when user presses MyScorecard
     * */

    public void myScorecardPressed(View view) {

        startActivity(new Intent(LeaderboardChannelActivity.this, MyScorecardChannelActivity.class));
        finish();
    }

    /**
     * 7. Function to execute when user presses Leaderboard
     * */

    public void leaderboardPressed(View view) {

        startActivity(new Intent(LeaderboardChannelActivity.this, LeaderboardChannelActivity.class));
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