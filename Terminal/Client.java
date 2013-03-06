package Terminal;
import java.io.*;

import Accessory.*;

public class Client {

	static String signinName = null;
	static Operater operater = null;
	static Listener listener = null;
	
	public Client(String[] args) throws IOException, InterruptedException {
		
		// first check input arguments
		if (!Accessory.CheckArgument(args, "client")) {
			System.err.println("Error: arguments for client should be " +
					"like -s <server-ip> <server-port>\n");
			System.exit(-1);
		}
		
		operater = new Operater(args);
		listener = new Listener(args);
		System.out.print("sobs> ");
		
		operater.start();
		listener.start();
	}

}
