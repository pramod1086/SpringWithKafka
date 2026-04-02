package com.pramod.springwithkafka.model;

import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
public class User {
    private String name;
    private int age;

    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }

    /** Explicit accessors so builds work even if Lombok annotation processing is skipped. */
    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
}
