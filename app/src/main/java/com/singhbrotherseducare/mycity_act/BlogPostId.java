package com.singhbrotherseducare.mycity_act;

import com.google.firebase.firestore.Exclude;

import io.reactivex.annotations.NonNull;

public class BlogPostId {
    @Exclude
    public String BlogPostId;
    public <T extends BlogPostId> T withId(@NonNull String id){
        this.BlogPostId=id;
        return (T) this;
    }
}
