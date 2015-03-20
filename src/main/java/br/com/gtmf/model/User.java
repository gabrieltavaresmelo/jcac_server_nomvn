package br.com.gtmf.model;

import org.java_websocket.WebSocket;

/**
 * Guarda as informacoes do usuario conectado
 * 
 * @author Gabriel Tavares
 *
 */
public class User {

	private String name;
	private String colorRGB;
	private WebSocket conn;
	private boolean isRequestNewGame = false;
	private String persona;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getColorRGB() {
		return colorRGB;
	}

	public void setColorRGB(String colorRGB) {
		this.colorRGB = colorRGB;
	}

	public WebSocket getConn() {
		return conn;
	}

	public void setConn(WebSocket conn) {
		this.conn = conn;
	}

	public boolean isRequestNewGame() {
		return isRequestNewGame ;
	}
	
	public void setRequestNewGame(boolean isRequestNewGame) {
		this.isRequestNewGame = isRequestNewGame;
	}
	
	public String getPersona() {
		return persona;
	}

	public void setPersona(String persona) {
		this.persona = persona;
	}

}