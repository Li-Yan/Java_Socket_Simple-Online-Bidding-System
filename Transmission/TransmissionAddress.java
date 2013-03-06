package Transmission;

public class TransmissionAddress {
	public String ip;
	public int port;
	
	public TransmissionAddress() {}
	
	public TransmissionAddress(String IP, int Port) {
		ip = IP;
		port = Port;
	}
	
	public void Print() {
		System.out.println("IP: " + ip + "; Port: " + port);
	}
}
