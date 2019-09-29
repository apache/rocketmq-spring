package org.apache.rocketmq.samples.springboot.consumer;

public class User{
        private String userName;
        private Byte userAge;

        public String getUserName() {
            return userName;
        }

        public User setUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public Byte getUserAge() {
            return userAge;
        }

        public User setUserAge(Byte userAge) {
            this.userAge = userAge;
            return this;
        }

        @Override
        public String toString() {
            return "User{" +
                    "userName='" + userName + '\'' +
                    ", userAge=" + userAge +
                    '}';
        }
    }