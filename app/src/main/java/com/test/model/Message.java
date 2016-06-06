package com.test.model;

import android.os.Parcel;
import com.vk.sdk.api.model.VKApiMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Message extends VKApiMessage {

    private int chatId;
    private int usersCount;
    private ArrayList<Integer> chatActive = new ArrayList<>();
    private String photo;

    public Message() {}

    public Message(JSONObject from) throws JSONException {
        super(from);
        parse(from);
    }

    public Message(Parcel in) {
        super(in);
        this.chatId = in.readInt();
        this.chatActive = in.readParcelable(List.class.getClassLoader());
        this.photo = in.readString();
        this.usersCount = in.readInt();
    }

    public Message parse(JSONObject source) throws JSONException {
        super.parse(source);
        chatId = source.optInt("chat_id");
        JSONArray jsonChatActive = source.optJSONArray("chat_active");
        if (jsonChatActive != null) {
            for (int index = 0; index < jsonChatActive.length(); index++) {
                chatActive.add((Integer) jsonChatActive.get(index));
            }
        }
        photo = source.optString("photo_100");
        usersCount = source.optInt("users_count");
        return this;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.chatId);
        dest.writeSerializable(this.chatActive);
        dest.writeString(this.photo);
        dest.writeInt(this.usersCount);
    }


    public Integer getChatId() {
        return chatId;
    }

    public int getUsersCount() {
        return usersCount;
    }

    public ArrayList<Integer> getChatActive() {
        return chatActive;
    }

    public String getPhoto() {
        return photo;
    }


    public static Creator<Message> CREATOR = new Creator<Message>() {
        public Message createFromParcel(Parcel source) {
            return new Message(source);
        }

        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
}
