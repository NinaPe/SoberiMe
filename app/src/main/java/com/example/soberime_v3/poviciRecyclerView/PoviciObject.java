package com.example.soberime_v3.poviciRecyclerView;

public class PoviciObject {
    private String povikId;
    private String time;
    private String destinaion;

    public PoviciObject(String povikId, String destination, String time){
        this.povikId = povikId;
        this.destinaion = destination;
        this.time = time;
    }

    public String getPovikId(){
        return povikId;
    }
    public void setPovikId(String povikId) { this.povikId = povikId; }

    public String getDestinaion() {
        return  destinaion;
    }

    public void setDestinaion(String destinaion) {
        this.destinaion = destinaion;
    }

    public String getTime() {
        return time;
    }
    public void setTime(String time) { this.time = time; }
}
