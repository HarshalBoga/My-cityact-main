package com.singhbrotherseducare.mycity_act;

import java.util.Date;

public class Comments extends CommentId {
    private String user_id, message;
    private Date timestamp;

    public Comments(){

    }


    public Comments(String user_id, String message, Date timestamp) {
        this.user_id = user_id;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
