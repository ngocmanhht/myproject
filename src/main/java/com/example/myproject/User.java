package com.example.myproject;


import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {
  private String phoneNumber;
  private String name;
  private String password;

  void convertFromJson(JsonObject obj) {
    this.phoneNumber = obj.getString("phoneNumber");
    this.name = obj.getString("name");
    this.password = obj.getString("password");
  }

}
