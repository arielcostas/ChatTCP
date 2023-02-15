package cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Formatter;
import java.util.Scanner;

public class Cliente {
	private final Socket socket;

	public Cliente(int port) throws IOException {
		this.socket = new Socket("localhost", port);
	}

	public void start() throws IOException {
		var cw = System.out;
		var cr = new Scanner(System.in);

		var sw = new PrintWriter(socket.getOutputStream());
		var sr = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		Thread serverRead = new Thread(() -> {
			var fecha = new SimpleDateFormat("hh:mm:ss");
			while (!socket.isClosed()) {
				try {
					var serverLine = sr.readLine();
					cw.printf("[%s] %s\n", fecha.format(new Date()), serverLine);
					cw.flush();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});

		Thread clientRead = new Thread(() -> {
			while (!socket.isClosed()) {
				var clientLine = cr.nextLine();
				sw.println(clientLine);
				sw.flush();
			}
		});

		serverRead.start();
		clientRead.start();
	}

}
