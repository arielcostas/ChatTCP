package server;

import javax.net.ServerSocketFactory;
import java.io.IOException;

public class Server {
	public static void main(String[] args) {
		var ssf = ServerSocketFactory.getDefault();
		var MAXIMO_CONEXIONES = 100;
		var conexiones = new Gestor(MAXIMO_CONEXIONES);

		try(var ss = ssf.createServerSocket(9999)) {
			while(!ss.isClosed() && conexiones.hayPosicionesDisponibles()) {
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

	}
}
