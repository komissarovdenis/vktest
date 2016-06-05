package com.test.model;

import android.os.Parcel;
import com.vk.sdk.api.model.VKApiMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Message extends VKApiMessage {

    private Integer chatId;
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
        return this;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.chatId);
        dest.writeSerializable(this.chatActive);
        dest.writeString(this.photo);
    }


    public Integer getChatId() {
        return chatId;
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
