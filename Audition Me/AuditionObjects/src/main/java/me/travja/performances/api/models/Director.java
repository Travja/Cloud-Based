package me.travja.performances.api.models;


public class Director extends Person {

    public Director() {
        super();
    }

    public Director(String name, String email, String phone, String password) {
        super(name, email, phone, password);
    }

    public Director(long id, String name, String email, String phone, String password) {
        super(id, name, email, phone, password);
    }
}
