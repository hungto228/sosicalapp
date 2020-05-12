package com.example.ssocial_app.Model;

public class ModelPost {
    //when use  pid,ptitle,pimage,ptime,uid,uemail,udp,uname;
    String pid,ptitle,pdescr,plikes,pimage,ptime,uid,uemail,udp,uname;
    public ModelPost()
    {

    }

    public ModelPost(String pid, String ptitle, String pdescr, String plikes, String pimage, String ptime, String uid, String uemail, String udp, String uname) {
        this.pid = pid;
        this.ptitle = ptitle;
        this.pdescr = pdescr;
        this.plikes = plikes;
        this.pimage = pimage;
        this.ptime = ptime;
        this.uid = uid;
        this.uemail = uemail;
        this.udp = udp;
        this.uname = uname;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getPtitle() {
        return ptitle;
    }

    public void setPtitle(String ptitle) {
        this.ptitle = ptitle;
    }

    public String getPdescr() {
        return pdescr;
    }

    public void setPdescr(String pdescr) {
        this.pdescr = pdescr;
    }

    public String getPlikes() {
        return plikes;
    }

    public void setPlikes(String plikes) {
        this.plikes = plikes;
    }

    public String getPimage() {
        return pimage;
    }

    public void setPimage(String pimage) {
        this.pimage = pimage;
    }

    public String getPtime() {
        return ptime;
    }

    public void setPtime(String ptime) {
        this.ptime = ptime;
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
