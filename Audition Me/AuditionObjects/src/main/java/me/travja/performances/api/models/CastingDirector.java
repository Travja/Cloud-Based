package me.travja.performances.api.models;


import java.util.UUID;

public class CastingDirector extends Director {

    public CastingDirector() {
        super();
    }

    public CastingDirector(UUID id, String name, String email, String phone, String password) {
        super(id, name, email, phone, password);
    }

}
