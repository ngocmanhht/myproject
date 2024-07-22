package com.example.myproject;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {

  public String generateSalt() {
    return BCrypt.gensalt();
  }

  public String hashPassword(String plainPassword, String salt) {
    return BCrypt.hashpw(plainPassword, salt);
  }

  public boolean checkPassword(String plainPassword, String hashedPassword) {
    return BCrypt.checkpw(plainPassword, hashedPassword);
  }
}
