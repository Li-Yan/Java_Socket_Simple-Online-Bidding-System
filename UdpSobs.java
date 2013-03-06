import java.io.IOException;

import Terminal.Client;
import Terminal.Server;


public class UdpSobs {

	public static void main(String[] args) throws ClassNotFoundException,
			IOException, InterruptedException {
		if (args[0].equals("-s")) {
			Server server = new Server(args);
		} else if (args[0].equals("-c")) {
			Client client = new Client(args);
		}
		else {
			System.err.println("Error: argument");
		}
	}

}
