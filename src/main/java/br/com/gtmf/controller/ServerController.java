package br.com.gtmf.controller;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.java_websocket.WebSocket;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.gtmf.model.Bundle;
import br.com.gtmf.model.User;
import br.com.gtmf.utils.CollectionUtils;

/**
 * Classe que implementa a logica do Servidor (WebSocketServer)
 * 
 * Todas as mensagens sao interceptadas pelo Servidor e 
 * redirecionadas para os destinatarios.
 * 
 * @author Gabriel Tavares
 *
 */
public class ServerController extends WebSocketServer {

	private static final Logger LOG = LoggerFactory.getLogger(ServerController.class);

	public static int PORT_SERVER = 9009; 
	
	private Collection<User> usersOn = new ArrayList<User>();

	private String firstLogged = "";

	
	// Singleton
	private static ServerController instance = null;
	
	public static ServerController getInstance() {
		if (instance == null) {
			try {
				instance = new ServerController(PORT_SERVER);
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
		return instance;
	}

	public ServerController(int port) {
		super(new InetSocketAddress(port));
	}

	public ServerController(InetSocketAddress address) {
		super(address);
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
//		this.sendToAll("nova conexao: " + handshake.getResourceDescriptor());
		LOG.debug(conn.getRemoteSocketAddress().getAddress().getHostAddress() + " fez login!");
	}
	
	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		try {
//			this.sendToAll(conn + " saiu do chat!");
			LOG.debug(conn.getRemoteSocketAddress().getAddress().getHostAddress() + " fez logout!");
			
			User user = CollectionUtils.search(usersOn, conn);
			
			if(user != null){
//				System.out.println(user.getName() + " saiu ");
				usersOn.remove(user);
				
				Map<String, String> map = CollectionUtils.toMap(usersOn);
				
				// Envia new bundle com a lista de todos os usuarios
				Bundle bundle = new Bundle(Bundle.LIST_USERS_ON, map);				
				this.sendToAll(bundle.toJson());
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
//			e.printStackTrace();
		}
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		try {
			Bundle bundle = new Bundle(message);
			
			switch (bundle.getHead()) {
			case Bundle.USER_IN:
				
				boolean isEmpty = usersOn.size() == 0 ? true : false;
								
				String username = bundle.getUser();
				
				if(CollectionUtils.search(usersOn, bundle.getUser()) != null){ // nome ja existe
					username = username + "1";
				}
				
				// Adiciona o usuario na lista
				User newUser = new User();
				newUser.setName(username);
				newUser.setConn(conn);
				
				usersOn.add(newUser);

				// Envia a confirmacao do username para o usuario recem logado
				bundle = new Bundle(Bundle.USER_IN, username);
				this.send(conn, bundle.toJson());
				
				// Envia new bundle com a lista de todos os usuarios
				bundle = new Bundle(Bundle.LIST_USERS_ON, CollectionUtils.toMap(usersOn));				
				this.sendToAll(bundle.toJson());
				
				if(isEmpty){
					firstLogged  = username;
				}

				bundle = new Bundle(Bundle.USER_FIRST, firstLogged);
				this.sendToAll(bundle.toJson());
				
				break;
				
			case Bundle.USER_OUT:
				User user = CollectionUtils.search(usersOn, conn);
				
				if(user != null){
					usersOn.remove(user);
					
					// Envia new bundle com a informacao de encerrar o jogo
					bundle = new Bundle(Bundle.USER_OUT, user.getName());
					this.sendToAll(bundle.toJson(), conn);	
					
					// Envia new bundle com a lista de todos os usuarios
					bundle = new Bundle(Bundle.LIST_USERS_ON, CollectionUtils.toMap(usersOn));				
					this.sendToAll(bundle.toJson());
				}
				
				break;
				
			case Bundle.QUESTION_SEND:
				bundle.setHead(Bundle.QUESTION_RECEIVE);

				// Envia new bundle com a pergunta para o destinatario
				this.sendToAll(bundle.toJson(), conn);				
				break;
				
			case Bundle.ANSWER_SEND:
				bundle.setHead(Bundle.ANSWER_RECEIVE);

				// Envia new bundle com a resposta para o destinatario
				this.sendToAll(bundle.toJson(), conn);				
				break;
				
			case Bundle.CHAT_MSG_SEND:
				bundle.setHead(Bundle.CHAT_MSG_RECEIVE);

				// Envia new bundle com a mensagem do chat para o destinatario
				this.sendToAll(bundle.toJson());				
				break;
				
			case Bundle.NEW_GAME_SEND:
				
				String split [] = bundle.getUser().split(";");
				username = split[0];
				String persona = split[1];
//				System.out.println(persona);
				
				// Diz que o usuario esta pronto para comecar o jogo
				for (User tmp : usersOn) {
					if(tmp.getName().equals(username)){
						tmp.setRequestNewGame(true);
						tmp.setPersona(persona);
					}
				}
				
				// Verifica se todos os usuarios estao prontos para comecar
				boolean canStartGame = true;
				
				for (User tmp : usersOn) {
					if(!tmp.isRequestNewGame()){
						canStartGame = false;
					}
				}
				
				// Se todos estiverem prontos, sera dado o inicio
				if(canStartGame && usersOn.size() > 1){
					bundle.setHead(Bundle.NEW_GAME_RECEIVE);
	
					// Envia new bundle mandando iniciar o jogo
					this.sendToAll(bundle.toJson());
				}
				
				break;
				
			case Bundle.RIDDLE_PERSONA_SEND:
				
				persona = bundle.getPersona();				
				User userMe = CollectionUtils.search(usersOn, conn);
				
				boolean isWin = false;
				
				for (User tmp : usersOn) {
					if (!userMe.getName().equals(tmp.getName())
							&& persona.equalsIgnoreCase(tmp.getPersona())) {
						isWin = true;
					}
				}
				
				bundle.setHead(Bundle.RIDDLE_PERSONA_RECEIVE);
				bundle.setPersona(userMe.getName() + "=" +isWin);

				// Envia new bundle com a mensagem do chat para o destinatario
				this.sendToAll(bundle.toJson());
				
				break;
			}
			
		} catch (Exception e) {
			LOG.error(e.getMessage());
//			e.printStackTrace();
		}
		
//		this.sendToAll(message);
//		LOG.debug(conn + ": " + message);
	}

	// @Override
	public void onFragment(WebSocket conn, Framedata fragment) {
		LOG.debug("received fragment: " + fragment);
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		LOG.debug(ex.getMessage(), ex);
		
		if (conn != null) {
			// some errors like port binding failed may not be assignable to a
			// specific websocket
		}
	}

	/**
	 * Envia para todos os clientes conectados
	 * 
	 * @param text
	 *            The String to send across the network.
	 * @throws InterruptedException
	 *             When socket related I/O errors occur.
	 */
	public void sendToAll(String text) {
		Collection<WebSocket> con = connections();
		
		synchronized (con) {
			for (WebSocket c : con) {
				c.send(text);
			}
		}
	}

	/**
	 * Envia para todos os clientes conectados, exceto para conRestriciton
	 * 
	 * @param text
	 *            The String to send across the network.
	 * @throws InterruptedException
	 *             When socket related I/O errors occur.
	 */
	public void sendToAll(String text, WebSocket conRestriciton) {
		Collection<WebSocket> con = connections();
		
		synchronized (con) {
			for (WebSocket c : con) {
				if(!c.equals(conRestriciton)){
					c.send(text);
				}
			}
		}
	}

	private void send(WebSocket con, String text) {
		synchronized (con) {
			con.send(text);
		}
	}

	public void close() {
		try {
			Bundle bundle = new Bundle(Bundle.SERVER_OUT, "");
			String message = bundle.toString();
			sendToAll(message);
			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}		
	}
	
	@Override
	public void stop() {
		try {
			close();
			
			super.stop();
			instance = null;
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}