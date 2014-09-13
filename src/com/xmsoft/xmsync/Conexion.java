package com.xmsoft.xmsync;

import java.io.Serializable;

public class Conexion implements Serializable{
	private static final long serialVersionUID = 1L;
	private String host;
	private int	port;
	private String user;
	private String pass;
	
	public Conexion() {
	}
	public Conexion(String host, int port, String user, String pass) {
		this.host = host;
		this.port = port;
		this.user = user;
		this.pass = pass;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPass() {
		return pass;
	}
	public void setPass(String pass) {
		this.pass = pass;
	}
}
