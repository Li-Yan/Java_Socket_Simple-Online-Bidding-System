package Terminal;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.SocketException;

import Accessory.*;
import Transmission.*;


public class Operater extends Thread {
	
	DatagramSocket opSocket = null;
	ClientTransmission transmission = null;
	
	public Operater(String[] args) {
		try {
			transmission = new ClientTransmission(args);
			opSocket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void doRegister(String inputString) {
		// Send to server format: *#username;listenport
		String userNameString = Accessory.GetRegisterName(inputString);
		if (Client.signinName == null) {
			if (userNameString == null) {
				System.out.println("[Error! haven\'t signin]");
				System.out.print("sobs> ");
				return;
			} else {
				String sendString = ClientTransmission.SetTypeHeader(
						userNameString, ClientTransmission.SockType.REGISTER);
				sendString = sendString + ';' + ClientTransmission.listenPort;
				String ackString = null;
				if ((ackString = transmission.Send_To_Server(opSocket, sendString)) == null) {
					System.out.println("[Error: server is offline, please try reregister]");
					Client.signinName = null;
					System.out.print("sobs> ");
					return;
				} else {
					System.out.println("[" + ackString + "]");
					Client.signinName = userNameString;
					System.out.print("sobs> ");
					return;
				}
			}
		} else {
			if (userNameString.equals(Client.signinName)) {
				String sendString = ClientTransmission.SetTypeHeader(
						userNameString, ClientTransmission.SockType.REGISTER);
				sendString = sendString + ';' + ClientTransmission.listenPort;
				String ackString = null;
				if ((ackString = transmission.Send_To_Server(opSocket, sendString)) == null) {
					System.out.println("[Error: server is offline, please try reregister]");
					Client.signinName = null;
					System.out.print("sobs> ");
					return;
				} else {
					System.out.println("[" + ackString + "]");
					Client.signinName = userNameString;
					System.out.print("sobs> ");
					return;
				}
			}
			else {
				System.out.println("[Error! cannot signin with another name before signout]");
				System.out.print("sobs> ");
				return;
			}
		}
	}
	
	private void doDeregister(String inputString) {
		if (Client.signinName == null) {
			System.out.println("[Error! haven\'t signin]");
			System.out.print("sobs> ");
			return;
		}
		else {
			String sendString = ClientTransmission.SetTypeHeader(
					Client.signinName, ClientTransmission.SockType.DEREGISTER);
			String ackString = null;
			if ((ackString = transmission.Send_To_Server(opSocket, sendString)) == null) {
				System.out.println("[Error: server is offline, please try reregister]");
				Client.signinName = null;
				System.out.print("sobs> ");
				return;
			} else {
				System.out.println("[" + ackString + "]");
				Client.signinName = null;
				System.out.print("sobs> ");
				return;
			}
		}
	}
	
	private void doSell(String inputString) {
		// Send to server format: *#username:input
		if (Client.signinName == null) {
			System.out.println("[Error! haven\'t signin]");
			System.out.print("sobs> ");
			return;
		}
		else {
			String sendString = ClientTransmission.SetTypeHeader(
					Client.signinName + ':', ClientTransmission.SockType.SELL);
			sendString = sendString + inputString;
			String ackString = null;
			if ((ackString = transmission.Send_To_Server(opSocket, sendString)) == null) {
				System.out.println("[Error: server is offline, please try reregister]");
				Client.signinName = null;
				System.out.print("sobs> ");
				return;
			} else {
				System.out.println("[" + ackString + "]");
				System.out.print("sobs> ");
				return;
			}
		}
	}
	
	private void doInfo(String inputString) {
		// Send to server format: *#item-number
		if (Client.signinName == null) {
			System.out.println("[Error! haven\'t signin]");
			System.out.print("sobs> ");
			return;
		}
		else {
			int itemNum = Accessory.GetItemNum(inputString);
			if (itemNum == -1) {
				System.out.println("[Error: arguments]");
				System.out.print("sobs> ");
				return;
			}
			String sendString = ClientTransmission.SetTypeHeader(String.valueOf(itemNum), 
					ClientTransmission.SockType.INFO);
			String ackString = null;
			if ((ackString = transmission.Send_To_Server(opSocket, sendString)) == null) {
				System.out.println("[Error: server is offline, please try reregister]");
				Client.signinName = null;
				System.out.print("sobs> ");
				return;
			} else {
				System.out.println("[" + ackString + "]");
				System.out.print("sobs> ");
				return;
			}
		}
	}
	
	private void doBid(String inputString) {
		// Send to server format: *#username:input
		if (Client.signinName == null) {
			System.out.println("[Error! haven\'t signin]");
			System.out.print("sobs> ");
			return;
		}
		else {
			String sendString = ClientTransmission.SetTypeHeader(
					Client.signinName + ':', ClientTransmission.SockType.BID);
			sendString = sendString + inputString;
			String ackString = null;
			if ((ackString = transmission.Send_To_Server(opSocket, sendString)) == null) {
				System.out.println("[Error: server is offline, please try reregister]");
				Client.signinName = null;
				System.out.print("sobs> ");
				return;
			} else {
				System.out.println("[" + ackString + "]");
				System.out.print("sobs> ");
				return;
			}
		}
	}
	
	private void doBuy(String inputString) {
		// Send to seller format: *#buyername:item-num
		// Send to server format: *#item-num,buyer-name
		if (Client.signinName == null) {
			System.out.println("[Error! haven\'t signin]");
			System.out.print("sobs> ");
			return;
		}
		Buy buy = new Buy();
		if (!Accessory.GetBuyInfo(inputString, buy)) {
			System.out.print("sobs> ");
			return;
		}
		if (buy.sellerName.equals(Client.signinName)) {
			System.out.println("[Error: seller name is yourself]");
			System.out.print("sobs> ");
			return;
		}
		
		TransmissionAddress address = ClientTransmission.addressHashMap.get(buy.sellerName);
		String sendString = null;
		String ackString = null;
		if (address != null) {
			// seller is in address book
			sendString = ClientTransmission.SetTypeHeader(Client.signinName + ":" + buy.itemNum, 
					ClientTransmission.SockType.BUY);
			if ((ackString = transmission.Send_By_Address(opSocket, sendString, address)) != null) {
				System.out.println("[" + ackString + "]");
				System.out.print("sobs> ");
				return;
			}
		}
		
		// seller is either not in the address bood or offline
		sendString = ClientTransmission.SetTypeHeader(buy.itemNum + "," + Client.signinName,
				ClientTransmission.SockType.DIRECT);
		if ((ackString = transmission.Send_To_Server(opSocket, sendString)) == null) {
			System.out.println("[Error: server is offline, please try reregister]");
			Client.signinName = null;
			System.out.print("sobs> ");
			return;
		} else {
			System.out.println("[" + buy.sellerName + " is currently offline. Request forwarded " +
					"to the server.]");
			System.out.print("sobs> ");
			return;
		}
	}
	
	public synchronized void run() {
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		
		while (true) {
			String inputString = null;
			try {
				inputString = input.readLine();
				System.out.print("sobs> ");
				int i;
				for (i = 0; i < inputString.length(); i++) {
					if (inputString.charAt(i) != ' ') break;
				}
				if (i == inputString.length()) continue;
				inputString = inputString.substring(i);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (Accessory.is_Register(inputString)) {
				doRegister(inputString);
				continue;
			}
			else if (Accessory.is_Deregister(inputString)) {
				doDeregister(inputString);
				continue;
			}
			else if (Accessory.is_Info(inputString)) {
				doInfo(inputString);
				continue;
			}
			else if (Accessory.is_Sell(inputString)) {
				doSell(inputString);
				continue;
			}
			else if (Accessory.is_Bid(inputString)) {
				doBid(inputString);
				continue;
			}
			else if (Accessory.is_Buy(inputString)) {
				doBuy(inputString);
				continue;
			}
			else {
				System.out.println("[Error : unrecognized input]");
				System.out.print("sobs> ");
			}
		}
	}
}
