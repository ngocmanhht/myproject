package com.example.myproject;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    vertx.deployVerticle(new DatabaseVerticle())
      .compose(id -> vertx.deployVerticle(new WebVerticle()))
      .compose(id -> vertx.deployVerticle(new RegisterVerticle()))
      .compose(id -> vertx.deployVerticle(new LoginVerticle()))
      .onComplete(result -> {
        if (result.succeeded()) {
          System.out.println("All Verticles deployed successfully");
          startPromise.complete();
        } else {
          System.out.println("Failed to deploy Verticles: " + result.cause());
          startPromise.fail(result.cause());
        }
      });
  }
}
