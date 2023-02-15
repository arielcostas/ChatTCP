package cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Cliente {
	public void start() {
		var partes = obtenerConexion().split("--", 2);

		try (var socket = new Socket(partes[0], Integer.parseInt(partes[1]))) {
			System.out.println("Conectando a " + socket.getRemoteSocketAddress());
			// Flujos de cliente
			var cw = System.out;
			var cr = new Scanner(System.in);

			// Flujos de servidor
			var sw = new PrintWriter(socket.getOutputStream());
			var sr = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// Lee mensajes del servidor y los imprime por consola
			Thread serverRead = new Thread(() -> {
				System.out.println("Leyendo mensajes del servidor");
				var fecha = new SimpleDateFormat("hh:mm:ss");
				while (true) {
					try {
						var serverLine = sr.readLine();
						cw.printf("[%s] %s\n", fecha.format(new Date()), serverLine);
						cw.flush();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			});

			// Lee mensajes del usuario por teclado y los envía al servidor
			Thread clientRead = new Thread(() -> {
				System.out.println("Leyendo mensajes del teclado");
				while (true) {
					var clientLine = cr.nextLine();
					sw.println(clientLine);
					sw.flush();
				}
			});

			// Inicializa los hilos
			serverRead.start();
			clientRead.start();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	private String obtenerConexion() {
		System.out.println("Intentando obtener conexion");
		try (var sock = new DatagramSocket()) {
			var bufenv = new byte[]{
					'H', 'O', 'L', 'A'
			};

			var mensaje = new DatagramPacket(bufenv, bufenv.length, InetAddress.getLocalHost(), 42069);
			sock.send(mensaje);

			// Recibir mensaje de vuelta
			var bufrec = new byte[1024];
			var recibido = new DatagramPacket(bufrec, bufrec.length);
			sock.receive(recibido);

			String strrec = new String(recibido.getData());
			strrec = strrec.substring(0, recibido.getLength());
			System.out.println("Recibido del servidor: " + strrec);

			return strrec;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
