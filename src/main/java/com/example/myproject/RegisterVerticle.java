package com.example.myproject;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public class RegisterVerticle extends AbstractVerticle {

  public void start(Promise<Void> startPromise) throws Exception {
    try {
      vertx.eventBus().consumer("register.vertX").handler(this::handleRegisterMessage);
      startPromise.complete();
    } catch (Exception e) {
      startPromise.fail(e);
    }
  }

  void handleRegisterMessage(Message<Object> event) {
    JsonObject userJson = (JsonObject) event.body();
    User user = new User();
    user.convertFromJson(userJson);
    if (user.getName() == null || user.getName().isEmpty() || user.getPassword() == null || user.getPassword().isEmpty() || user.getPhoneNumber() == null || user.getPhoneNumber().isEmpty()) {
      System.out.println("fail" + userJson);
      event.fail(400, "Missing Params");
    } else {
      System.out.println("message" + event.body());
      vertx.eventBus().request("db.insertUser", userJson, reply -> {
        if (reply.succeeded()) {
          event.reply("Oke");

        } else {
          event.fail(500, reply.cause().getMessage());
        }
      });
    }
  }
}
