package com.example.ssocial_app.Model;

public class ModelComment {
    String cid, comment, timestamp, uid, uemail, udp, uname;

    public ModelComment() {

    }

    public ModelComment(String cid, String comment, String timestamp, String uid, String uemail, String udp, String uname) {
        this.cid = cid;
        this.comment = comment;
        this.timestamp = timestamp;
        this.uid = uid;
        this.uemail = uemail;
        this.udp = udp;
        this.uname = uname;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUemail() {
        return uemail;
    }

    public void setUemail(String uemail) {
        this.uemail = uemail;
    }

    public String getUdp() {
        return udp;
    }

    public void setUdp(String udp) {
        this.udp = udp;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }
}