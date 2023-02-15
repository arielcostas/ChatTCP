package cliente;

import java.io.IOException;

public class Launcher {
	public static void main(String[] args) throws IOException {

		Cliente cli = new Cliente(9999);
		cli.start();

	}
}
