package com.mind.mind_calc.Constructors;

public class UserCredentials {

    private String name, email, psw;

    public UserCredentials(String name, String email, String psw) {
        this.name = name;
        this.email = email;
        this.psw = psw;
    }

    public UserCredentials(String email, String psw) {
        this.email = email;
        this.psw = psw;
    }

    public UserCredentials(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPsw() {
        return psw;
    }

    public void setPsw(String psw) {
        this.psw = psw;
    }
}
