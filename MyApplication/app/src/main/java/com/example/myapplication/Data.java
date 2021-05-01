package com.example.myapplication;

public class Data {
    private int Ratio;
    private String Original_Text;

    public Data(int ratio, String original_Text) {
        this.Ratio = ratio;
        this.Original_Text = original_Text;
    }

    public Data(){

    }

    public int getRatio() {
        return Ratio;
    }

    public void setRatio(int ratio) {
        Ratio = ratio;
    }

    public String getOriginal_Text() {
        return Original_Text;
    }

    public void setOriginal_Text(String original_Text) {
        Original_Text = original_Text;
    }
}
