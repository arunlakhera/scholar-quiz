package org.sairaa.scholarquiz;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class ChannelList {

    public String Moderator;
    private String ModeratorID;
    public String Name;
    public String channelId;
    String totalNumberOfQuiz;
    String totalUserQuiz;

    public ChannelList() {
        /*Blank default constructor essential for Firebase*/
    }

    public ChannelList(String Moderator, String ModeratorID, String Name, String channelId) {

        this.Moderator = Moderator;
        this.ModeratorID = ModeratorID;
        this.Name = Name;
        this.channelId = channelId;

    }

    public ChannelList(String Moderator, String ModeratorID, String Name, String channelId, String totalNumberOfQuiz, String totalUserQuiz) {

        this.Moderator = Moderator;
        this.ModeratorID = ModeratorID;
        this.Name = Name;
        this.channelId = channelId;
        this.totalNumberOfQuiz = totalNumberOfQuiz;
        this.totalUserQuiz = totalUserQuiz;
    }

    public String getModeratorName() {
        return Moderator;
    }
    public String getModeratorID() {
        return ModeratorID;
    }
    public String getChannelName() {
        return Name;
    }
    public String getChannelId() {
        return channelId;
    }

    public String getTotalNumberOfQuiz() {
        return totalNumberOfQuiz;
    }

    public String getTotalUserQuiz() {
        return totalUserQuiz;
    }
}
