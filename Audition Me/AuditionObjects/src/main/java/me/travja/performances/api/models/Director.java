package me.travja.performances.api.models;


import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;

import java.util.UUID;

public class Director extends Person {

    public Director() {
        super();
    }

    public Director(String name, String email, String phone, String password) {
        super(name, email, phone, password);
    }

    public Director(UUID id, String name, String email, String phone, String password) {
        super(id, name, email, phone, password);
    }

}
