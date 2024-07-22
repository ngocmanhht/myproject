package com.example.myproject;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class WebVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    configRoutes()
      .onSuccess(this::startHttpServer)
      .onComplete(http -> {
        if (http.succeeded()) {
          startPromise.complete();
          System.out.println("HTTP server started on port 8080");
        } else {
          startPromise.fail(http.cause());
        }
      });

  }

  Future<Router> configRoutes() {
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    router.get("/api/v1/hello").handler(ctx -> {
      ctx.response().end("Hello World1");
    });
    router.post("/api/v1/register").handler(this::registerHandler);
    router.post("/api/v1/login").handler(this::loginHandler);
    return Future.succeededFuture(router);
  }

  Future<HttpServer> startHttpServer(Router router) {
    Promise<HttpServer> promise = Promise.promise();
    vertx.createHttpServer().requestHandler(router).listen(8080).onSuccess(promise::complete).onFailure(promise::fail);
    return promise.future();
  }

  void registerHandler(RoutingContext ctx) {
    HttpServerRequest request = ctx.request();
    String name = request.getFormAttribute("name");
    String phoneNumber = request.getFormAttribute("phoneNumber");
    String password = request.getFormAttribute("password");
    JsonObject userJson = new JsonObject();
    userJson.put("name", name);
    userJson.put("phoneNumber", phoneNumber);
    userJson.put("password", password);
    System.out.println("userJson: " + userJson);
    vertx.eventBus().request("register.vertX", userJson).onComplete(reply -> {
      if (reply.succeeded()) {
        ctx.response().end(reply.result().body().toString());
      } else {
        Throwable cause = reply.cause();
        handleException(ctx, cause);
      }
    });
  }

  void loginHandler(RoutingContext ctx) {
    HttpServerRequest request = ctx.request();
    String phoneNumber = request.getFormAttribute("phoneNumber");
    String password = request.getFormAttribute("password");
    JsonObject loginJson = new JsonObject();
    loginJson.put("phoneNumber", phoneNumber);
    loginJson.put("password", password);
    vertx.eventBus().request("login.vertX", loginJson).onComplete(reply -> {
      if (reply.succeeded()) {
        ctx.response().setStatusCode(200).end(reply.result().body().toString());
      } else {
        Throwable cause = reply.cause();
        handleException(ctx, cause);
      }
    });
  }

  void handleException(RoutingContext ctx, Throwable cause) {
    if (cause instanceof ReplyException) {
      ReplyException replyException = (ReplyException) cause;
      int statusCode = replyException.failureCode();
      String message = replyException.getMessage();
      System.out.println("Failed with code: " + statusCode);
      System.out.println("Error message: " + message);
      ctx.response().setStatusCode(statusCode).end(message);

    } else {
      cause.printStackTrace();
    }
  }
}
