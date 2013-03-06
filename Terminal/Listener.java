package Terminal;
import java.net.DatagramSocket;
import java.net.SocketException;

import Transmission.ClientTransmission;
import Transmission.TransmissionAddress;

public class Listener extends Thread {

	DatagramSocket listenSocket = null;
	ClientTransmission transmission;
	
	public Listener(String[] args) {
		try {
			transmission = new ClientTransmission(args);
			listenSocket = new DatagramSocket(ClientTransmission.listenPort);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void doUpdateAddr(String recvString) {
		transmission.UpdateAddr(recvString);
		System.out.println("[Client table updated.]");
		System.out.print("sobs> ");
	}
	
	private void doSold(String recvString) {
		System.out.println("[" + recvString + "]");
		System.out.print("sobs> ");
		transmission.Send_ACK(listenSocket, "", transmission.serverIP, transmission.serverPort);
	}
	
	private void doOffline(String recvString) {
		System.out.println("[" + recvString + "]");
		System.out.print("sobs> ");
		transmission.Send_ACK(listenSocket, "", transmission.serverIP, transmission.serverPort);
	}
	
	private void doBuy(String recvString, TransmissionAddress address) {
		// receiver format: buyername:item-num
		// send format: *#sellername:item-num,buyer-name
		String sendString = Client.signinName + " has received your request.";
		transmission.Send_ACK(listenSocket, sendString, address);
		int index = recvString.indexOf(':');
		String buyername = recvString.substring(0, index);
		int itemNum = Integer.parseInt(recvString.substring(index + 1));
		System.out.println("[" + buyername + " wants to buy your " + itemNum + ".]");
		System.out.print("sobs> ");
		
		// send to server
		sendString = ClientTransmission.SetTypeHeader(Client.signinName + ":" + itemNum + "," + buyername, 
				ClientTransmission.SockType.DIRECT);
		if ((transmission.Send_To_Server(listenSocket, sendString)) == null) {
			System.out.println("[Error: server is offline, please try reregister]");
			Client.signinName = null;
			System.out.print("sobs> ");
			return;
		}
	}
	
	private void doPurchase(String recvString, TransmissionAddress address) {
		System.out.println("[" + recvString + "]");
		System.out.print("sobs> ");
		transmission.Send_ACK(listenSocket, "", address);
	}
	
	private void doError(String recvString) {
		System.out.println("[" + recvString + "]");
		System.out.print("sobs> ");
	}
	
	public synchronized void run() {
		while (true) {
			TransmissionAddress address = new TransmissionAddress();
			String recvString = transmission.Read_Recv(listenSocket, address);
			if (recvString.length() == 0) continue;
			ClientTransmission.SockType type = ClientTransmission.GetTypeHead(recvString);
			recvString = ClientTransmission.RemoveTypeHeader(recvString);

			if (type == ClientTransmission.SockType.BROADCAST) {
				doUpdateAddr(recvString);
				continue;
			} else if (type == ClientTransmission.SockType.SOLD) {
				doSold(recvString);
				continue;
			} else if (type == ClientTransmission.SockType.OFFLINE) {
				doOffline(recvString);
				continue;
			} else if (type == ClientTransmission.SockType.BUY) {
				doBuy(recvString, address);
				continue;
			} else if (type == ClientTransmission.SockType.PURCHASE) {
				doPurchase(recvString, address);
				continue;
			} else if (type == ClientTransmission.SockType.ERROR) {
				doError(recvString);
				continue;
			}
		}
	}
}
