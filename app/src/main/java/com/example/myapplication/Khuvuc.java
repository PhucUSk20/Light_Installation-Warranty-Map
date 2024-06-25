package com.example.myapplication;


import java.util.List;

public class Khuvuc {
    public String name;
    public List<LightData> lights;

    public Khuvuc() {}

    public Khuvuc(String name, List<LightData> lights) {
        this.name = name;
        this.lights = lights;
    }
}

