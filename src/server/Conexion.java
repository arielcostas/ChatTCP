package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * La clase conexión es un hilo que lee contínuamente del cliente (por red), y cuando recibe un mensaje
 * lo pasa al {@link Gestor}
 */
public class Conexion implements Runnable {
	private final Socket socket;
	private final Gestor gestor;
	private final int id;

	public Conexion(Socket socket, Gestor gestor, int id) {
		this.socket = socket;
		this.gestor = gestor;
		this.id = id;
	}

	@Override
	public void run() {
		this.gestor.enviarMensajesHastaAhora(socket);
		System.out.println("Escuchando " + this.id);

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			var fecha = new SimpleDateFormat("hh:mm:ss");
			while(!socket.isClosed()) {
				var linea = br.readLine().trim();
				if (linea.equals("*")) {
					gestor.desconectar(this.id);
					break;
				}

				System.out.printf("[%s] %s\n", fecha.format(new Date()), linea);
				System.out.println(this.id + ": " + linea);
				gestor.enviarMensaje(linea, this.id);
			}
		} catch (IOException e) {
			System.out.println("ROMPE " + this.id + ": " + e.getMessage());
			gestor.desconectar(id);
		}
		System.out.println("Ta luego " + this.id);

	}

	public Socket getSocket() {
		return socket;
	}

	public int getId() {
		return id;
	}
}
