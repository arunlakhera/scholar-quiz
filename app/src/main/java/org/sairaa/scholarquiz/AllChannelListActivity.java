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

public class AllChannelListActivity extends AppCompatActivity {

    ListView channelList;

    //Databas Reference
    private DatabaseReference mChannelRef;
    private DatabaseReference mChannelListRef;
    private DatabaseReference mSubscriptionListRef;

    DatabaseReference mDatabase;

    CustomAllChannelAdapter customAdapter;
    List<ChannelList> AllChannelList;
    ChannelList channel = new ChannelList();

    String channelExist = "N";
    String channelId;

    Dialog menuDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_channel_listview);

        menuDialog = new Dialog(this);

        // ListView to show list of channels available
        channelList = findViewById(R.id.listView_Channel);
        AllChannelList = new ArrayList<>();

        // To get User id of Current user so we can travel to Subscription List of User
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mSubscriptionListRef = mDatabase.child("SQ_Subscription/" + String.valueOf(user.getUid()));

        mChannelRef = mDatabase.child("SQ_ChannelList/");

        if (!isNetworkAvailable()) {
            Toast.makeText(AllChannelListActivity.this,"To view available channels, Please Connect your Phone to Internet..",Toast.LENGTH_SHORT).show();
        }

        /**
         * Code to Read All Channel List from Firebase and show them in Channel List Activity
         */

        // Read all the channels name from Firebase
        mChannelRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot channelSnapshot) {

                // Clear the Channel List View
                AllChannelList.clear();

                // Check the count of Channels to subscribe to. If no channel, show message else show list of channels
                if (channelSnapshot.getChildrenCount() < 1) {
                    Toast.makeText(AllChannelListActivity.this, "No Channel to Subscribe", Toast.LENGTH_LONG).show();

                } else {

                    // Loop through all channel lists
                    for (final DataSnapshot channelListSnapshot : channelSnapshot.getChildren()) {

                        // Read all the channels that user has subscribed to
                        mSubscriptionListRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot subscriptionSnapshot) {

                                for (DataSnapshot subscriptionListSnapshot : subscriptionSnapshot.getChildren()) {
                                    // If channel is already subscribed to by user ser the channelExist to Y
                                    if (channelListSnapshot.getKey().equals(subscriptionListSnapshot.getKey())) {
                                        channelExist = "Y";
                                        break;
                                    }

                                }

                                // If channelExist = N i.e channel is not subscribed then add it to channel list adapter to show it to user else set it to N
                                if (channelExist.equals("N")) {

                                    channelId = String.valueOf(channelListSnapshot.getKey());

                                    // Show channels available to user
                                    channel = channelListSnapshot.getValue(ChannelList.class);
                                    AllChannelList.add(new ChannelList(channel.getModeratorName(),channel.getModeratorID(), channel.getChannelName(), channelId));
                                    customAdapter = new CustomAllChannelAdapter(getApplicationContext(), AllChannelList);
                                    channelList.setAdapter(customAdapter);

                                } else {
                                    channelExist = "N";
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

    }

    public void goBackButton(View view) {
        startActivity(new Intent(AllChannelListActivity.this, UserChannelActivity.class));

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
            startActivity(new Intent(AllChannelListActivity.this, SignInActivity.class));
            finish();
        } else {

            Toast.makeText(AllChannelListActivity.this, "To Log Out, Please Connect your Phone to Internet..", Toast.LENGTH_SHORT).show();

        }
    }

    /**
     * 4. Function to execute when user presses MyChannel
     * */

    public void myChannelPressed(View view) {

        startActivity(new Intent(AllChannelListActivity.this, UserChannelActivity.class));
        finish();
    }

    /**
     * 5. Function to execute when user presses AllChannel
     * */

    public void allChannelPressed(View view) {

        startActivity(new Intent(AllChannelListActivity.this, AllChannelListActivity.class));
        finish();
    }

    /**
     * 6. Function to execute when user presses MyScorecard
     * */

    public void myScorecardPressed(View view) {

        startActivity(new Intent(AllChannelListActivity.this, MyScorecardChannelActivity.class));
        finish();
    }

    /**
     * 7. Function to execute when user presses Leaderboard
     * */

    public void leaderboardPressed(View view) {

        startActivity(new Intent(AllChannelListActivity.this, LeaderboardChannelActivity.class));
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
