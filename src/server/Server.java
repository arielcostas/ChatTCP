package server;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.*;

public class Server {
	public static final int PUERTO_CHAT = 9999;
	public static final int MAXIMO_CONEXIONES = 100;

	public static void main(String[] args) throws UnknownHostException {
		var host = InetAddress.getByName("0.0.0.0");
		var ssf = ServerSocketFactory.getDefault();
		var conexiones = new Gestor(MAXIMO_CONEXIONES);

		var conectorTcp = new Thread(() -> {
			System.out.println("Escuchando TCP en " + host.getHostAddress() + ":" + PUERTO_CHAT);
			try (ServerSocket ss = ssf.createServerSocket(PUERTO_CHAT, 0, host)) {
				while (!ss.isClosed() && conexiones.hayPosicionesDisponibles()) {
					var sock = ss.accept();
					var conexion = conexiones.conectar(sock);
					System.out.println("Conectado " + conexion.getId());
					new Thread(conexion).start();
				}
			} catch (IOException e) {
				System.out.println("Excepción creando socket de servidor " + e.getMessage());
			} catch (DemasiadosClientesException e) {
				System.out.println("Se rechazó la conexión de un cliente porque hay demasiados conectados.");
			}
		});

		var anuncioUdp = new Thread(() -> {
			System.out.println("Escuchando UDP en " + host.getHostAddress() + ":" + 42069);
			try (DatagramSocket s = new DatagramSocket(42069)) {
				while (true) {
					byte[] bufrec = new byte[1024];
					DatagramPacket recibido = new DatagramPacket(bufrec, bufrec.length);
					s.receive(recibido);

					var mensaje = new String(recibido.getData());
					mensaje = mensaje.substring(0, recibido.getLength());
					System.out.println("<< UDP <<: " + mensaje);

					var strenv = InetAddress.getLocalHost().getHostAddress() + "--" + PUERTO_CHAT;
					var bufenv = strenv.getBytes();

					System.out.println(">> UDP >> " + strenv);
					DatagramPacket respuesta = new DatagramPacket(bufenv, bufenv.length, recibido.getAddress(), recibido.getPort());
					s.send(respuesta);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});

		anuncioUdp.start();
		conectorTcp.start();
	}
}
