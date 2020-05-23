package com.example.ssocial_app.Model;

public class ModelUsers {
    //users use name,email,search,phone, image, cover;
    String username,email,search,phone, image, cover,onlinestatus,typingto;
  // @SerializedName("id")
    String id;


    public ModelUsers() {
    }

    public ModelUsers(String username, String email, String search, String phone, String image, String cover, String onlinestatus, String typingto, String id) {
        this.username = username;
        this.email = email;
        this.search = search;
        this.phone = phone;
        this.image = image;
        this.cover = cover;
        this.onlinestatus = onlinestatus;
        this.typingto = typingto;
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getOnlinestatus() {
        return onlinestatus;
    }

    public void setOnlinestatus(String onlinestatus) {
        this.onlinestatus = onlinestatus;
    }

    public String getTypingto() {
        return typingto;
    }

    public void setTypingto(String typingto) {
        this.typingto = typingto;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
