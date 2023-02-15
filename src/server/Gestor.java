package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Gestiona las conexiones/desconexiones, los mensajes entre usuarios y demás.
 */
public class Gestor {
	private final Conexion[] conexiones;
	private final List<String> mensajes;
	private final AtomicInteger id = new AtomicInteger();

	public Gestor(int maximo) {
		if (maximo <= 1) {
			throw new IllegalArgumentException("Deben permitirse al menos dos conexiones");
		}
		System.out.println("Inicializando chat con hasta " + maximo + " conexiones");
		this.conexiones = new Conexion[maximo];
		this.mensajes = new ArrayList<>();
	}

	public synchronized Conexion conectar(Socket socket) throws DemasiadosClientesException {
		Integer posicionDisponible = this.findPrimeraPosicionDisponible();
		if (posicionDisponible == null) {
			throw new DemasiadosClientesException();
		}

		var con = new Conexion(socket, this, id.addAndGet(1));
		this.conexiones[posicionDisponible] = con;

		return con;
	}

	public void desconectar(int idCliente) {
		for (int i = 0; i < conexiones.length; i++) {
			try {
				var c = conexiones[i];
				if (c.getId() == idCliente) {
					c.getSocket().close();
					conexiones[i] = null;
					break;
				}
			} catch (IOException e) {
				System.out.println("Error cerrando conexion " + idCliente + ": " + e.getMessage());
				conexiones[i] = null;
			}
		}
	}

	public void enviarMensaje(String mensaje, int idEmisor) {
		for (Conexion c : this.conexiones) {
			if (c == null) {
				continue;
			}

			if (c.getId() == idEmisor) {
				continue;
			}

			PrintWriter pw;
			try {
				pw = new PrintWriter(c.getSocket().getOutputStream());
			} catch (IOException e) {
				System.out.println("Error escribiendo a " + c.getId() + ": " + e.getMessage());
				this.desconectar(c.getId());
				return;
			}

			pw.println(idEmisor + ": " + mensaje);
			pw.flush();
		}

		synchronized (mensajes) {
			mensajes.add(idEmisor + ": " + mensaje);
		}
	}

	public void enviarMensajesHastaAhora(Socket socket) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(socket.getOutputStream());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		this.mensajes.forEach(pw::println);
		pw.println("===== BIENVENIDO AL CHAT =====");
		pw.flush();
	}

	public boolean hayPosicionesDisponibles() {
		return this.findPrimeraPosicionDisponible() != null;
	}

	private synchronized Integer findPrimeraPosicionDisponible() {
		for (int i = 0; i < conexiones.length; i++) {
			if (this.conexiones[i] == null) {
				return i;
			}
		}
		return null;
	}
}
