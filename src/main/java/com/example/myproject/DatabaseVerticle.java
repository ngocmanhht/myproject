package com.example.myproject;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgBuilder;
import io.vertx.pgclient.PgConnection;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.*;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DatabaseVerticle extends AbstractVerticle {

  private Pool pool;

  @Override
  public void start(Promise<Void> startPromise) {
    PgConnectOptions connectOptions = new PgConnectOptions()
      .setPort(5432)
      .setHost("localhost")
      .setDatabase("demo")
      .setUser("postgres")
      .setPassword("12345678");


    PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

    pool = PgBuilder
      .pool()
      .with(poolOptions)
      .connectingTo(connectOptions)
      .using(vertx)
      .build();

    // Test connection
    testConnection().onComplete(result -> {
      if (result.succeeded()) {
        System.out.println("Successfully connected to PostgreSQL");
        this.setupEventBusHandler();
        startPromise.complete();

      } else {
        System.out.println("Failed to connect to PostgreSQL: " + result.cause());
        startPromise.fail(result.cause());
      }
    });
  }

  private Future<Void> testConnection() {
    Promise<Void> promise = Promise.promise();

    pool.getConnection().onSuccess(conn -> {
      conn.query("SELECT 1").execute().onComplete(res -> {

        conn.close();  // Always close the connection after use
        if (res.succeeded()) {
          promise.complete();
        } else {
          promise.fail(res.cause());
        }
      });
    }).onFailure(promise::fail);

    return promise.future();
  }

  private void setupEventBusHandler() {
    vertx.eventBus().consumer("db.insertUser", this::onInsertUser);
    vertx.eventBus().consumer("db.loginUser", this::onLoginUser);

  }

  private void onInsertUser(Message<Object> message) {
    JsonObject user = (JsonObject) message.body();
    String phoneNumber = user.getString("phoneNumber");
    String password = user.getString("password");
    String name = user.getString("name");
    userExists(phoneNumber).onComplete(res -> {
      if (res.succeeded()) {
        boolean exists = res.result();
        if (exists) {
          message.fail(400, "User already exists");
        } else {
          insertUser(phoneNumber, password, name).onComplete(response -> {
            if (response.succeeded()) {
              message.reply("User inserted successfully");
            } else {
              System.out.println("Failed to insert user: " + response.cause().getMessage());
              message.fail(500, response.cause().getMessage());
            }
          });
        }
      } else {
        message.fail(500, res.cause().getMessage());
      }
    });

  }

  public Future<Void> insertUser(String phoneNumber, String password, String name) {
    Promise<Void> promise = Promise.promise();
    PasswordUtils passwordUtils = new PasswordUtils();
    String salt = passwordUtils.generateSalt();
    String hashedPassword = passwordUtils.hashPassword(password, salt);
    String insertSql = "INSERT INTO public.users (phone_number, password, name, salt) VALUES ($1, $2, $3, $4)";

    Tuple params = Tuple.of(phoneNumber, hashedPassword, name, salt);

    pool.getConnection().onSuccess(conn -> {
      conn.preparedQuery(insertSql).execute(params).onComplete(res -> {
        conn.close();
        if (res.succeeded()) {
          promise.complete();
        } else {
          promise.fail(res.cause());
        }
      });
    }).onFailure(promise::fail);

    return promise.future();
  }


  private Future<Boolean> userExists(String phoneNumber) {
    Promise<Boolean> promise = Promise.promise();
    String sql = "SELECT COUNT(*) FROM public.users WHERE phone_number = $1";
    Tuple params = Tuple.of(phoneNumber);

    pool.getConnection().onSuccess(conn -> {
      conn.preparedQuery(sql).execute(params).onComplete(res -> {
        conn.close();
        if (res.succeeded()) {
          int count = res.result().iterator().next().getInteger(0);
          promise.complete(count > 0);
        } else {
          promise.fail(res.cause());
        }
      });
    }).onFailure(promise::fail);

    return promise.future();
  }

  void onLoginUser(Message<Object> message) {
    JsonObject user = (JsonObject) message.body();
    String phoneNumber = user.getString("phoneNumber");
    String password = user.getString("password");
    JwtUtils jwtUtils = new JwtUtils();
    isPasswordMatch(phoneNumber, password).onComplete(res -> {
      if (res.succeeded()) {
        boolean isMatch = res.result();
        if (isMatch) {
          System.out.println("Successfully logged in");
          String token = jwtUtils.generateToken(phoneNumber);
          loginUser(phoneNumber, token).onComplete(success -> {
              JsonObject jsonObject = new JsonObject();
              jsonObject.put("token", token);
              jsonObject.put("expired", "7200");
              message.reply(jsonObject);
            })
            .onFailure(fail -> {
              message.fail(500, "Server Error");
            });

        } else {
          message.fail(400, "Password does not match");

        }
      } else {
        message.fail(500, res.cause().getMessage());
      }
    });
  }

  public Future<Void> loginUser(String phoneNumber, String token) {
    Promise<Void> promise = Promise.promise();
    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));

    ZonedDateTime futureTime = now.plus(Duration.ofSeconds(7200));
    String formattedDate = futureTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    String updateSql = "UPDATE users SET token = $1 , expired_at = $2 WHERE phone_number = $3";
    Tuple params = Tuple.of(token, formattedDate, phoneNumber);

    pool.getConnection().onSuccess(conn -> {
      conn.preparedQuery(updateSql).execute(params).onComplete(res -> {
        conn.close();
        if (res.succeeded()) {
          promise.complete();
        } else {
          promise.fail(res.cause());
        }
      });
    }).onFailure(promise::fail);

    return promise.future();
  }

  public Future<Boolean> isPasswordMatch(String phoneNumber, String password) {
    Promise<Boolean> promise = Promise.promise();
    PasswordUtils passwordUtils = new PasswordUtils();
    String sql = "SELECT * FROM public.users  WHERE phone_number = $1";

    Tuple params = Tuple.of(phoneNumber);

    pool.getConnection().onSuccess(conn -> {
      conn.preparedQuery(sql).execute(params).onComplete(res -> {
        conn.close();
        if (res.succeeded()) {
          RowSet<Row> rows = res.result();
          if (rows.size() > 0) {
            String hashedPassword = rows.iterator().next().getString("password");
            Boolean isMatch = passwordUtils.checkPassword(password, hashedPassword);
            promise.complete(isMatch);
          } else {
            promise.fail("No user found with phone number: " + phoneNumber);
          }
        } else {
          promise.fail(res.cause());
        }
      });
    }).onFailure(promise::fail);

    return promise.future();
  }


  public Future<Void> updateUserToken(String phoneNumber, String token) {
    Promise<Void> promise = Promise.promise();

    String updateSql = "UPDATE public.users SET token = $1 WHERE phone_number = $2";

    Tuple params = Tuple.of(token, phoneNumber);

    pool.getConnection().onSuccess(conn -> {
      conn.preparedQuery(updateSql).execute(params).onComplete(res -> {
        conn.close();
        if (res.succeeded()) {
          promise.complete();
        } else {
          promise.fail(res.cause());
        }
      });
    }).onFailure(promise::fail);

    return promise.future();
  }


}

