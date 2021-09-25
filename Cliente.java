import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Cliente extends JFrame implements ActionListener {
	private JTextField area;
	private JTextArea display;
	private ObjectOutputStream saida;
	private ObjectInputStream entrada;
	private Socket cliente;
	private String mensagem = "";
	private String servidorconexao;
	private JButton B1; //Reset
	
	public Cliente (String host) {
		super("CLIENTE");
		servidorconexao = host;
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
		container.add(new JScrollPane(display),BorderLayout.CENTER);
		
		//nossa parte
		B1 = new JButton("Reset");
		B1.setBackground(new Color (137, 12, 12));
		B1.setFont(new Font(" ",Font.BOLD,16));
		B1.setForeground(Color.white);
		B1.addActionListener(this);
		container.add(B1,BorderLayout.SOUTH);
		setSize(300,200);
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == B1) {
			display.append("\nBotão pressionado");
//			//area.setText("");
			this.rodaCliente();
		}
	}
	
	public void rodaCliente() {
		try{
			if(cliente != null)
				cliente.close();
			conectaServidor();
			if(cliente != null)
			{
				obtemFluxos();
				processoConexao();
				fechaConexao();
			}
		}
		catch(EOFException eofException) { System.out.println("Cliente terminou a conexão"); eofException.printStackTrace(); }
		catch(IOException ioException) { ioException.printStackTrace(); }
	}
	
	//sem esperaConexao()
	
	private void obtemFluxos() throws IOException{
		saida = new ObjectOutputStream(cliente.getOutputStream());
		saida.flush();
		entrada = new ObjectInputStream(cliente.getInputStream());
		display.append("\nFluxo de entrada/saída");
	}
	
	private void conectaServidor() throws IOException{ //não presente em server
		display.setText("Tentando conexão\n");
		try { 
			cliente = new Socket(InetAddress.getByName(servidorconexao), 5000);
			display.append("Conecta para " + cliente.getInetAddress().getHostName());
		}
		catch(ConnectException connectException) {display.append("Erro criando conexão, servidor pode não estar ligado.\n");}
		}
	
	private void processoConexao() throws IOException{
		area.setEnabled(true);
		do{
			try{
				mensagem = (String)entrada.readObject();
				display.append("\nServidor: " + mensagem);
				display.setCaretPosition(display.getText().length());
			}catch(ClassNotFoundException classNotFoundException){
				display.append("\nTipo de objeto desconhecido");
				mensagem = "Servidor terminou";
			}
			catch(SocketException socketException) {
				display.append("\nErro ao receber mensagem; o servidor pode ter parado abrutamente. Pressione Reset para tentar novamente");
				mensagem = "Servidor terminou";	
			}
		}while(!mensagem.equals("Servidor terminou"));
	}
	
	private void fechaConexao() throws IOException{
		display.append("\nTerminou conexão");
		saida.close();
		entrada.close();
		cliente.close();
	}
	
	private void enviaDado(String mensagem) {
		try {
			saida.writeObject(mensagem);
			saida.flush();
			display.append("\nCliente: " + mensagem);
		}
		catch(IOException ioException) {display.append("\nErro escrevendo objeto");}
	}
	
	public static void main(String[] args){
		Cliente aplicacao;
		if(args.length == 0)
			aplicacao = new Cliente("127.0.0.1");
		else
			aplicacao = new Cliente(args[0]);
		aplicacao.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		aplicacao.rodaCliente();
	}
}
