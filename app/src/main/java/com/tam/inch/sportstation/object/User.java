package com.tam.inch.sportstation.object;

/**
 * Created by MyPC on 12/04/2016.
 */
public class User {
    public String name;
    public String email;
    public String connecttion;
    public String cratedAt;

    public User() {
    }

    public User(String name, String email, String connecttion, String cratedAt) {
        this.name = name;
        this.email = email;
        this.connecttion = connecttion;
        this.cratedAt = cratedAt;
    }
}
