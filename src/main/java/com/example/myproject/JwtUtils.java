package com.example.myproject;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class JwtUtils {


  private String SECRET_KEY = "-----BEGIN PRIVATE KEY-----\n" +
    "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDX9ZuYlaY1FGTb\n" +
    "QYBDcG3ZNx0sX0H14cx05K0C5BZOTASC1tOdK/vvauXt0dnjeJL0B2EZSQPUD2sa\n" +
    "cs0iWtxIYbzEsTQb+u8OIEDXfYy0PZzUX7Hdrqs2dGJ9j0ef6yJN0cOSMbO6Dkl4\n" +
    "+d8mOtmat6W9tMWMylfs7JikpwaiO9YP/UzKGThpbetL3ZIsFcf7p/GeNC2vmErw\n" +
    "2B4ByQIGrOJ7AKZalbKxqV5homSGGUGORrNZLYHgc9cz83S7g/6caN+gHxOrII6h\n" +
    "7FwYp68Ha5DTc8teBx1+WJ72BFhrB7v8EIG1FXBi4c7IVjAP4NpwS3XgWaSXu7kE\n" +
    "7i+hUmThAgMBAAECggEAETsSgBkomsGWznuqbTcSPKuOE1VCYQj0FDuOplVmhjbA\n" +
    "gWP8nGS/BDpGQDRnKyQarYfNMoRF3uygAgs43M3dF5Vcx2DQJI9f9YOTSN5GlLb5\n" +
    "xWQhP+vI015z4UrGTzcmlIjxDsASc/zh1dox+M3nQNX4TmuW+4XFgCzcfP6qo9T2\n" +
    "1lIrqMQgtQqhU2Iu+GR+jKy6goWJEnhBwKLwF0m6lYhfx5xQ3Q7a/e5Nw4D8+fRa\n" +
    "6unJu2pM9R71argylkLJtfNakYvi7QCTKRlCxiVFfZSVjbEnFJCVk/jHaW8uYbDz\n" +
    "/27708A4RiaYSJbn9D4FYnD1yhvoJ9WeTUmTwd4fdQKBgQDwy7wSM2yVnjBotL5n\n" +
    "iq/fKZ9PJcB1f4q1edBwdcXBKFTqnYbej7dhNeOA60mbPnYgOeZ5rbOc2Y7uJdso\n" +
    "wDsulwNCMrc944lUFCdB+yXmIsV8lx222YQgnxrprnZqd8TA3H5EPeJDW1RVWhgV\n" +
    "8Gs/A7yofEJwG3Be1Vtge1cKbwKBgQDlmGmrPlDqNwdYSkg5iMxuBrLaBnnodbQt\n" +
    "F6Bj88/aocjyh4zZymSCTggrN3LCYS1LkSWXXW+YYy8BjLfu123Tdf8e+XOP0xJA\n" +
    "ZU9p8gfAPCHvgP1ILo7tLadjm4lvNHT7Zeym+4fGDaE7ZGSOY+dOJWfFjepYxp+N\n" +
    "jA+AmS9trwKBgQDWBpBcsR6Dgf5PIs/WYlveBDXYeJqvFTUqBmLxgozKGdoYSvnU\n" +
    "mnMJUYesT/W1qff+vtgPMQhjkrBLFpTMjMhNqeY4kbFzremjNOKL7/oIqsFT81Fy\n" +
    "87VP9XtLV0ljap8UOSd16ndRHT1BO/oKjg4VDXJDY2b+FDV15Tf58sczuwKBgB1G\n" +
    "6yqX/q9vJvOePUmA5TieiA3/R2paIAij+6LjQz0I5lvu5woaoehv1ODV6D9bLvB0\n" +
    "6Ms9ce6Hr0XOUOIW/H07jbXAb3kGnEwz3wWOhGiCAn3M//9FWJpr+O1dtw/EK6qn\n" +
    "G59LccvSjx3Itn3lxWgUov/xEdISOeRN2Og4IHEnAoGBAJ0zPj0SvqdKbRgd/trb\n" +
    "wT2UJIlDpWrrwWr1tLkmedr+lP8W/eCHx7cQF6ccvlyaVBSVZKd7A/c99r7NzIN+\n" +
    "9iAiNBNhv0XY8OK0xhMnwicuW9mD3U24posJ27zYZoM71gStoMJ/YxIecdUns42C\n" +
    "Jwdq+96qhMifoFAusWwUgev3\n" +
    "-----END PRIVATE KEY-----\n";


  public String generateToken(String subject) {
    try {
      return Jwts.builder()
        .setSubject(subject)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 7200))
        .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
        .compact();
    } catch (Exception e) {
      System.out.println(e);
      return "";
    }

  }

  public Claims getClaims(String token) {
    return Jwts.parser()
      .setSigningKey(SECRET_KEY)
      .parseClaimsJws(token)
      .getBody();
  }

  public long getExpiration(String token) {
    Claims claims = Jwts.parser()
      .setSigningKey(SECRET_KEY)
      .parseClaimsJws(token)
      .getBody();
    return claims.getExpiration().getTime();
  }

  public boolean isTokenValid(String token) {
    try {
      Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

}
