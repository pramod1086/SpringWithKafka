package com.pramod.springwithkafka.kafka;

import java.util.Objects;

/**
 * Key for Kafka partitioning by username, id, and age.
 * Same key → same partition (ordering and grouping per user).
 */
public class UserPartitionKey {
    private final String username;
    private final int id;
    private final int age;

    public UserPartitionKey(String username, int id, int age) {
        this.username = username;
        this.id = id;
        this.age = age;
    }

    public String getUsername() {
        return username;
    }

    public int getId() {
        return id;
    }

    public int getAge() {
        return age;
    }

    /** Composite key string for partitioner hashing. */
    public String toPartitionKey() {
        return username + ":" + id + ":" + age;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPartitionKey that = (UserPartitionKey) o;
        return id == that.id && age == that.age && Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, id, age);
    }
}
