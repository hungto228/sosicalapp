package com.example.ssocial_app.Model;

public class ModelChatlist {
    String id;     //get chat list ,sender receiver uid
    public  ModelChatlist()
    {

    }

    public ModelChatlist(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
