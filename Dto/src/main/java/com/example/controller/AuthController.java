package com.example.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.LoginData;
import com.example.dto.LoginRequest;
import com.example.dto.LogoutRequest;

@RestController
@RequestMapping("/")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
	private final Map<String, LoginData>activeSessions = new HashMap<>();
	@PostMapping("/login")
	public Map<String,String>login(@RequestBody LoginRequest request) {
		String sessionId = UUID.randomUUID().toString();
		LoginData loginData = new LoginData (
				request.getUsername(),
				"LOGIN",
				request.getIp(),
				request.getPort(),
				sessionId
		);
		activeSessions.put(sessionId, loginData);
	    logger.info("User {} logged in with session ID{}",request.getUsername(),sessionId);
		
		Map<String,String>response = new HashMap<>();
		response.put("username",request.getUsername());
		response.put("status","LOGIN SUCCESS");
		response.put("ip",request.getIp());
		response.put("username",String.valueOf(request.getPort()));
		response.put("sessionId",sessionId);
		
		return response;
	}
	@PostMapping("/logout")
	public String logout(@RequestBody LogoutRequest request) {
		if(!activeSessions.containsKey(request.getSessionId())) {
			return "Invalid session";
		}
		activeSessions.remove(request.getSessionId());
		return "Logout successful";
	}
	@PostMapping(value ="/get",produces = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<String>get(@RequestBody Map<String,String> request) {
		return processRequest(request,"get");
	}
	@PostMapping(value ="/get-config",produces = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<String>getConfig(@RequestBody Map<String,String> request) {
		return processRequest(request,"get-config");
	}
	@PostMapping(value ="/edit-config",produces = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<String>editConfig(@RequestBody Map<String,String> request) {
		return processRequest(request,"edit-config");
	}
	
	private ResponseEntity<String>processRequest(Map<String,String>request,String operation){
		String sessionId = request.get("sessionId");
		String messageId = request.get("messageId");
		
		if(!activeSessions.containsKey(sessionId)) {
			return ResponseEntity.badRequest().body("Invalid session");
		}
		logger.info("Session ID {}-{} operation invoked",sessionId,operation);
		String response = generateXmlResponse(operation,messageId);
		return ResponseEntity.ok(response);
	}
	private String generateXmlResponse (String operation,String messageId) {
		switch(operation) {
		case "get":
			return    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
		              "<rpc messageId = \"" + messageId + "\">\n" +
					   "<get>\n" +
		              "<filter type =\"subtree\"></filter>\n" +
					   "</get>\n" +
		              "</rpc>";
		case "get-config":
			return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
		              "<rpc messageId = \"" + messageId + "\">\n" +
					   "<get-config>\n" +
		              "<source>\n" +
					   "<running/>\n" +
		               "</source>\n" +
					   "</get-config>\n" +
		              "</rpc>";
			
		case "edit-config":
			 return   "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
		              "<rpc messageId = \"" + messageId + "\">\n" +
					   "<edit-config>\n" +
		              "<target>\n"+
					   "<running/>\n" +
		               "</target>\n" +
					   "<config>\n" +
		               "<data>\n" +
					   "    "+  "" +"\n"+
		               "</data>\n" +
					   "</edit-config>\n" +
		              "</rpc>";
			 default:
				 return "<error>Invalid operation</error>";
		}
	}
}
