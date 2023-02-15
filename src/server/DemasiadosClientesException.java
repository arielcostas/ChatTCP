package server;

public class DemasiadosClientesException extends Exception {
	public DemasiadosClientesException() {
		super("Hay demasiados clientes conectados");
	}
}
