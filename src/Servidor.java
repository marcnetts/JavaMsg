import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Servidor extends JFrame {
	private JTextField area;
	private JTextArea display;
	private ObjectOutputStream saida;
	private ObjectInputStream entrada;
	private ServerSocket servidor;
	private Socket conexao;
	private int contador;
	
	public Servidor() {
		super("Servidor");
		Container container = getContentPane();
		area = new JTextField();
		area.setEnabled(false);
		area.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent evento) {
						enviaDado(evento.getActionCommand()); }
				});
		container.add(area, BorderLayout.NORTH);
		display = new JTextArea();
		container.add(new JScrollPane(display), BorderLayout.CENTER);
		setSize(300, 150);
		setVisible(true);
	}
	
	public void rodaServidor() {
		try {
			servidor = new ServerSocket(5000, 2);
			while(true) {
				contador++;
				esperaConexao();
				obtemFluxos();
				processoConexao();
				fechaConexao();
			}
		}
		catch(EOFException eofException) { System.out.println("Cliente terminou a conexão"); }
		catch(IOException ioException) { ioException.printStackTrace(); }
	}
	
	private void esperaConexao() throws IOException {
		display.setText("Esperando por conexão\n");
		conexao = servidor.accept();
		display.append("Conexão " + contador + " recebido de: " + conexao.getInetAddress().getHostName());
	}
	
	private void obtemFluxos() throws IOException{
		saida = new ObjectOutputStream(conexao.getOutputStream());
		saida.flush();
		entrada = new ObjectInputStream(conexao.getInputStream());
		display.append("\nFluxo de entrada/saída");
	}

	private void processoConexao() throws IOException{
		String mensagem = "Sucesso na conexão do servidor";
		saida.writeObject(mensagem); //usa tudo em uma linha kk
		saida.flush();
		area.setEnabled(true);
		
		do {
			try {
				try{
					mensagem = (String)entrada.readObject();
					display.append("\nCliente " + contador + ": " + mensagem);
					display.setCaretPosition(display.getText().length());}
				catch(SocketException socketException) {display.append("\nFalha ao ler mensagem; Cliente pode ter desconectado."); throw new IOException();}	
			}catch(ClassNotFoundException classNotFoundException) {display.append("\nTipo de objeto desconhecido");}
		}while(!mensagem.equals("Cliente terminou"));
	}

	private void fechaConexao() throws IOException{
		display.append("\nCliente" + contador + " terminou conexão");
		area.setEnabled(true);
		saida.close();
		entrada.close();
		conexao.close();
	}
	
	private void enviaDado(String mensagem) {
		try {
			saida.writeObject(mensagem);
			saida.flush();
			display.append("\nServidor: " + mensagem);
		}
		catch(IOException ioException) {display.append("Erro escrevendo objeto");}
	}
	
	public static void main(String[] args) {
		Servidor aplicacao = new Servidor();
		aplicacao.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		while(true)
			aplicacao.rodaServidor();
	}
}