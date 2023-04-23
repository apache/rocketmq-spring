package org.apache.rocketmq.samples.springboot.domain;

/**
 * @author Akai
 */
public class UserMessage {
    int id;
    private String userName;
    private Byte userAge;

    public int getId() {
        return id;
    }

    public UserMessage setId(int id) {
        this.id = id;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public UserMessage setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public Byte getUserAge() {
        return userAge;
    }

    public UserMessage setUserAge(Byte userAge) {
        this.userAge = userAge;
        return this;
    }
}
