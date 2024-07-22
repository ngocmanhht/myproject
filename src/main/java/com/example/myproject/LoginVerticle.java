package com.example.myproject;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class LoginVerticle extends AbstractVerticle {
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    try {
      vertx.eventBus().consumer("login.vertX").handler(this::handleLogin);
      startPromise.complete();
    } catch (Exception e) {
      startPromise.fail(e);
    }
  }

  void handleLogin(Message<Object> event) {
    JsonObject loginJson = (JsonObject) event.body();
    User user = new User();
    user.convertFromJson(loginJson);
    if (user.getPassword() == null || user.getPassword().isEmpty() || user.getPhoneNumber() == null || user.getPhoneNumber().isEmpty()) {
      System.out.println("fail" + loginJson);
      event.fail(400, "Missing Params");
    } else {
      System.out.println("login" + event.body());
      vertx.eventBus().request("db.loginUser", loginJson, reply -> {
        if (reply.succeeded()) {
          event.reply(reply.result().body());

        } else {
          event.fail(500, reply.cause().getMessage());
        }
      });
    }
  }
}
