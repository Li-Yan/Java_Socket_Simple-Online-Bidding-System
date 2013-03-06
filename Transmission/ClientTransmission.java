package Transmission;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;

import Accessory.Accessory;

public class ClientTransmission {
	public static enum SockType {ACK, REGISTER, DEREGISTER, BROADCAST, SELL, INFO, BID, SOLD, 
		BUY, DIRECT, PURCHASE, OFFLINE, ERROR};
	
	public static final int Max_Msg_Length = 1024;
	public static final int TimeOut = 500;
	public static final int Lower_Port = 1024;
	public static final int Upper_Port = 65535;
	public final int Max_Resend_Trial = 5;			// Can try resend 4 time if no ACK received

	public String serverIP = null;
	public int serverPort;
	public static int listenPort;
	public static HashMap<String, TransmissionAddress> addressHashMap = null;
    private byte recvByte[];
    private byte sendByte[];

	public ClientTransmission(String[] args) {
		addressHashMap = new HashMap<String, TransmissionAddress>();
		Set_IP_Port(args);
		listenPort = Accessory.Random_Port(Lower_Port, Upper_Port);
	}
	
	public void Set_IP_Port(String[] args) {
		int i = 0;
		// Get the port number from arguments
		for (String s : args) {
			if (s.equals("-c")) {
				if (!Accessory.CheckIP(args[i + 1])) {
					System.err.println("Not proper IP!");
					System.exit(-1);
				}
				serverIP = args[i + 1];
				int port = Accessory.CheckPort(args[i + 2]);
				if (port < 0) {
					System.err.println("Not proper port!");
					System.exit(-1);
				}
				serverPort = port;
				break;
			}
			i++;
		}
	}
	
	public static String SetTypeHeader(String sendString, SockType Type) {
		return String.valueOf(Type.ordinal()) + '#' + sendString;
	}
	
	public static SockType GetTypeHead(String recvString) {
		int index = recvString.indexOf('#');
		int typeIndex = Integer.parseInt(recvString.substring(0, index));
		for (SockType sockType : SockType.values()) {
			if (typeIndex == sockType.ordinal()) {
				return sockType;
			}
		}
		return null;
	}
	
	public static String RemoveTypeHeader(String recvString) {
		int index = recvString.indexOf('#');
		if (index == recvString.length() - 1) return "";
		else return recvString.substring(index + 1);
	}
	
	public synchronized String Send_By_Address(DatagramSocket socket, 
			String sendString, String IP, int Port) {
		sendByte = new byte[Max_Msg_Length];
		sendByte = sendString.getBytes();
		DatagramPacket sendPacket =null; 
		DatagramPacket recvPacket = null;
		try {
			sendPacket = new DatagramPacket(sendByte, sendByte.length, 
					InetAddress.getByName(IP), Port);
			socket.send(sendPacket);
			socket.setSoTimeout(TimeOut);
			recvByte = new byte[1024];
			recvPacket = new DatagramPacket(recvByte, recvByte.length);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		int m = 0;
		while (m < Max_Resend_Trial) {
			try {
				socket.receive(recvPacket);
			} catch (IOException e) {}
			if (recvPacket.getAddress() != null) {
				String recvString = new String(recvByte, 0, recvPacket.getLength());
				if (GetTypeHead(recvString) == SockType.ACK) {
					try {
						socket.setSoTimeout(0);
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return RemoveTypeHeader(recvString);
				}
			}
			else {
				m++;
				try {
					socket.send(sendPacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		try {
			socket.setSoTimeout(0);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public String Send_By_Address(DatagramSocket socket, String sendString, 
			TransmissionAddress address) {
		return Send_By_Address(socket, sendString, address.ip, address.port);
	}
	
	public void Send_By_Address_No_ACK(DatagramSocket socket, String sendString, 
			String IP, int Port) {
		sendByte = new byte[Max_Msg_Length];
		sendByte = sendString.getBytes();
		DatagramPacket sendPacket =null; 
		try {
			sendPacket = new DatagramPacket(sendByte, sendByte.length, 
					InetAddress.getByName(IP), Port);
			socket.send(sendPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void Send_By_Address_No_ACK(DatagramSocket socket, String sendString, 
			TransmissionAddress address) {
		Send_By_Address_No_ACK(socket, sendString, address.ip, address.port);
	}
	
	public void Send_ACK(DatagramSocket socket, String sendString, String IP, int Port) {
		Send_By_Address_No_ACK(socket, SetTypeHeader(sendString, SockType.ACK), IP, Port);
	}
	
	public void Send_ACK(DatagramSocket socket, String sendString, TransmissionAddress address) {
		Send_ACK(socket, sendString, address.ip, address.port);
	}
	
	public String Send_To_Server(DatagramSocket socket, String sendString) {
		return Send_By_Address(socket, sendString, serverIP, serverPort);
	}
	
	public String Read_Recv(DatagramSocket socket, TransmissionAddress address) {
		recvByte = new byte[1024];
		DatagramPacket recvPacket = new DatagramPacket(recvByte, recvByte.length);
		try {
			socket.receive(recvPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (recvPacket.getAddress() == null) {
			return null;
		}
		
		address.ip = recvPacket.getAddress().getHostAddress();
		address.port = recvPacket.getPort();
		return new String(recvByte, 0, recvPacket.getLength());
	}
	
	public void UpdateAddr(String recvString) {
		addressHashMap = new HashMap<String, TransmissionAddress>();
		
		while (true) {
			int index = recvString.indexOf(':');
			String name = recvString.substring(0, index);
			recvString = recvString.substring(index + 1);
			index = recvString.indexOf(',');
			String ip = recvString.substring(0, index);
			recvString = recvString.substring(index + 1);
			index = recvString.indexOf(';');
			int port = Integer.parseInt(recvString.substring(0, index));
			recvString = recvString.substring(index);
			addressHashMap.put(name, new TransmissionAddress(ip, port));
			if (recvString.equals(";")) {
				return;
			}
			else {
				recvString = recvString.substring(1);
			}
		}
	}
}
	