package com.singhbrotherseducare.mycity_act;

import com.google.firebase.firestore.Exclude;

import io.reactivex.annotations.NonNull;

public class CommentId {
    @Exclude
    public String CommentId;
    public <T extends CommentId> T withId(@NonNull String id){
        this.CommentId=id;
        return (T) this;
    }
}
