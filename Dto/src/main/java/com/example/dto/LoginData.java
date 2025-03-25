package com.example.dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginData {
 private String username;
 private String status;
 private String ip;
 private int port;
 private String sessionId;
 
 public LoginData (String username,String status,String ip ,int port,String sessionId) {
	 this.username = username;
	 this.status = status;
	 this.ip=ip;
	 this.port= port;
	 this.sessionId = sessionId;
 }
 //Getters
 public String getUsername() {return username;}
 public String getStatus() {return status;}
 public String getIp() {return ip;}
 public int getPort() {return port;}
 public String getSessionId() {return sessionId;}
}
