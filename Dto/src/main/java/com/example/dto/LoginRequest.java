package com.example.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
  private String username;
  private String password;
  private String ip;
  private int port;
public String getUsername() {
	return username;
}
public String getPassword() {
	return password;
}
public LoginRequest(String username , String password) {
	this.username = username;
	this.password = password;
}
public  String getIp() {
	return ip;
}
public void setIp(String ip) {
	this.ip=ip;
}
public int getPort() {
	return port;
}
public void setPort(int port){
	this.port = port;
}

}
