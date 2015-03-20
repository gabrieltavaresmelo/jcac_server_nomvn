package br.com.gtmf.window;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.gtmf.controller.ServerController;
import br.com.gtmf.utils.FXOptionPane;
import br.com.gtmf.utils.NetworkUtils;
import br.com.gtmf.utils.FXOptionPane.Response;

public class MainLayoutWindow {

	private static final Logger LOG = LoggerFactory.getLogger(MainLayoutWindow.class);

	// Widgets
    @FXML private Label lbPorta;
    @FXML private Label lbInterfaces;
    @FXML private Label lbStatus;
    @FXML private ToggleButton tbStatus;

	// Referencia a Stage
	private Stage stage;
	
	
	public void setParams(Stage stage) {
		this.stage = stage;
	}
	
	/**
	 * Inicializa a classe controller. 
	 * Eh chamado automaticamente depois do fxml ter sido carregado 
	 */
	@FXML
	private void initialize() {
		String body = "";
		Map<String, String> ifaces = NetworkUtils.getInterfaces();
		
		Set<String> chaves = ifaces.keySet();
		for (Iterator<String> iterator = chaves.iterator(); iterator.hasNext();) {
			String key = iterator.next();
			String value = ifaces.get(key);
			
			body += key;
			body += ": ";
			body += value;
			body += "\n";
		}

		body += "\n";
		
		lbInterfaces.setText(body);
	}

	
	// ---------------------------------------------------
	// -- 
	// -- 	Metodos dos Eventos dos componentes da tela
	// --
	// ---------------------------------------------------

	
	@FXML
	private void handleExit() {
		finish();
	}
	
	@FXML
	private void handleAbout() {
		String body = "";

		body += "Curso:";
		body += "\n";
		body += "Engenharia de Computação";
		body += "\n";
		body += "\n";
		
		body += "Disciplina:";
		body += "\n";
		body += "Programação Paralela e Distribuída";
		body += "\n";
		body += "18/11/2013";
		body += "\n";
		body += "\n";

		body += "Trabalho:";
		body += "\n";
		body += "Jogo: Cara-a-Cara utilizando WebSockets";
		body += "\n";
		body += "\n";
		
		body += "Aluno:";
		body += "\n";
		body += "Gabriel Tavares";
		body += "\n";
		body += "gabrieltavaresmelo@gmail.com";
		body += "\n";
		body += "https://github.com/gabrieltavaresmelo";
		body += "\n";
		body += "\n";

		FXOptionPane.showMessageDialog(stage, body, "Sobre", "OK");
		
//		Dialogs.showInformationDialog(stage,
//				body,
//				"Jogo Cara-Cara", 
//				"Sobre");
//		Dialog.showInfo("Jogo Cara-Cara", "test");
//		FXOptionPane.showMessageDialog("This is an example of an error message dialog.", MessageType.ERROR);
	}
	
	@FXML
	private void handleSetPort() {
//		Dialogs.showInformationDialog(mainApp.getPrimaryStage(),
//				"Author: Marco Jakob\nWebsite: http://edu.makery.ch",
//				"AddressApp", "About");
		
		String porta = FXOptionPane.showInputDialog(stage, "Porta",
				"Configuração da Porta", "Confirmar", "Cancelar",
				lbPorta.getText());
		
		try {
			ServerController.PORT_SERVER = Integer.parseInt(porta);
			
			lbPorta.setText(String.valueOf(ServerController.PORT_SERVER));
			
		} catch (NumberFormatException e) {
			FXOptionPane.showMessageDialog(stage, "Erro",
					"Insira um número válido!", "OK");
		}
		
	}
	
	@FXML
	public void tbStatusItemChange() {
		if(tbStatus.isSelected()){
			turnOn();
		} else{			
			turnOff();			
		}
	}

	private void turnOn(){
		tbStatus.setText("Desconectar");

		lbStatus.setText("Conectado na porta " + ServerController.PORT_SERVER);
		ServerController.getInstance().start();
	}
	
	private void turnOff(){
		tbStatus.setText("Conectar");

		lbStatus.setText("Desconectado");
		ServerController.getInstance().stop();
	}


	/**
	 * Encerra a aplicacao
	 */
	public void finish() {		
		Response resp = FXOptionPane.showConfirmDialog(stage,
				"Deseja encerrar a Aplicação?", "Sair", "Sim", "Não");
		
		if(resp == Response.YES){
			turnOff();
			stage.close();
	//		Platform.exit();
			System.exit(0); // FIXME Procurar um metodo menos "agressivel"
		}
	}
	
}