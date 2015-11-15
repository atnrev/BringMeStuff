package com.bringmestuff.bringmestuff;

/**
 * Created by Antoine on 05/11/2015.
 */
public class Membre {
    public String email;
    public String password;

    public Membre(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
