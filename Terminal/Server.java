package Terminal;
import java.io.*;
import java.net.DatagramSocket;
import java.sql.ResultSet;
import java.sql.SQLException;

import Transmission.*;
import Accessory.*;
import Database.*;

public class Server {
	
	static ServerDB serverDB;
	static ServerTransmission transmission;
	static DatagramSocket serverSocket = null;
	
	public static void doRegister(String recvString, TransmissionAddress address) {
		int index = recvString.indexOf(';');
		String name = recvString.substring(0, index);
		recvString = recvString.substring(index + 1);
		transmission.Send_ACK(serverSocket, "Welcome " + name
				+ ", you have successfully signed in", address);
		address.port = Integer.parseInt(recvString);
		transmission.addressHashMap.put(name, address);
		try {
			Thread.sleep(73);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		transmission.BroadcastAddr(serverSocket);
		
		// send offline message to this user
		ResultSet rs = serverDB.sqlite_Query("SELECT msg FROM messages WHERE username = \'" + name + "\'");
		try {
			String sendString = ServerTransmission.SetTypeHeader("", ServerTransmission.SockType.OFFLINE);
			while (rs.next()) {
				transmission.Send_By_Address(serverSocket, sendString + rs.getString(1), address);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		serverDB.sqlite_Execute("DELETE FROM messages WHERE username = \'" + name + "\'");
	}
	
	public static void doDeregister(String recvString, TransmissionAddress address) {
		transmission.Send_ACK(serverSocket, "You have successfully signed out. Bye!", address);
		String name = recvString;
		transmission.addressHashMap.remove(name);
		try {
			Thread.sleep(73);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		transmission.BroadcastAddr(serverSocket);
	}
	
	public static void doSell(String recvString, TransmissionAddress address) {
		// Format:sellername:sell <item-name> <transaction-limit> <starting-bid> <buy-now> <description>
		Sell item = new Sell();
		int index = recvString.indexOf(':');
		String sellerName = recvString.substring(0, index);
		recvString = recvString.substring(index + 1);
		if (!Accessory.GetSellInfo(recvString, item)) {
			transmission.Send_ACK(serverSocket, "Error: arguments", address);
			return;
		}
		
		String queryString ="INSERT INTO items (sellername, itemname, translimit, bidby, " +
				"currentbid, buynow, desc) ";
		queryString += "VALUES (\'" + sellerName + "\', \'" + item.itemName + "\', " + item.transLimit +
				", \'\', " + item.startBid + ", " + item.buyNow + ", \'" + item.desc + "\');";
		serverDB.sqlite_Execute(queryString);
		ResultSet rs = serverDB.sqlite_Query("SELECT MAX(id) FROM items;");
		try {
			rs.next();
			String sendString = item.itemName + " added with number " + rs.getString(1);
			transmission.Send_ACK(serverSocket, sendString, address);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private static void doInfo(String recvString, TransmissionAddress address) {
		// Send to server format: item-number
		int itemNum = Integer.parseInt(recvString);
		String queryString ="SELECT id, itemname, sellername, currentbid, buynow, desc FROM items";
		if (itemNum > 0) {
			queryString += " WHERE id = " + itemNum;
		}
		ResultSet rs = serverDB.sqlite_Query(queryString);
		try {
			if (!rs.next()) {
				if (itemNum == 0) {
					transmission.Send_ACK(serverSocket, "Error: empty", address);
					return;
				}
				else {
					transmission.Send_ACK(serverSocket, "Error: " + itemNum + " not found", address);
					return;
				}
			}
			else {
				if (itemNum == 0) {
					int m =0;
					String sendString = "";
					while (true) {
						m++;
						sendString += '\n';
						for (int i = 1; i < 6; i++) {
							sendString += rs.getString(i) + ' ';
						}
						sendString += rs.getString(6);
						if (!rs.next())
							break;
					}
					if (m == 1) sendString = sendString.substring(1);
					transmission.Send_ACK(serverSocket, sendString, address);
				}
				else {
					String sendString = "";
					for (int i = 1; i < 6; i++) {
						sendString += rs.getString(i) + ' ';
					}
					sendString += rs.getString(6);
					transmission.Send_ACK(serverSocket, sendString, address);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void doBid(String recvString, TransmissionAddress address) {
		// Format:username:bid <item-num> <amount>
		Bid bid = new Bid();
		int index = recvString.indexOf(':');
		String userName = recvString.substring(0, index);
		recvString = recvString.substring(index + 1);
		
		int check = Accessory.GetBidInfo(recvString, bid);
		if (check == -1) {
			transmission.Send_ACK(serverSocket, "Error: arguments", address);
			return;
		}
		else if (check == 0) {
			transmission.Send_ACK(serverSocket, "Error: negative bid", address);
			return;
		}
		else {
			String queryString = "SELECT sellername, itemname, translimit, bidby, currentbid " +
					"FROM items WHERE id = " + bid.itemNum;
			ResultSet rs = serverDB.sqlite_Query(queryString);
			try {
				if (!rs.next()) {
					transmission.Send_ACK(serverSocket, "Error: " + bid.itemNum + " not found", address);
					return;
				}
				else {
					if (rs.getString(1).equals(userName)) {
						transmission.Send_ACK(serverSocket, "Error: owner", address);
						return;
					}
					else if (rs.getString(4).equals(userName)) {
						transmission.Send_ACK(serverSocket, "Error: duplicate bid", address);
						return;
					}
					else {
						int translimit = Integer.parseInt(rs.getString(3));
						if (translimit > 1) {
							// Tell bidder and update the database
							String sendString = bid.itemNum + " " + rs.getString(2) + " " + 
									(Integer.parseInt(rs.getString(5)) + bid.amount);
							queryString = "UPDATE items SET translimit = translimit - 1, bidby = \'" + userName +
									"\', currentbid = currentbid + " + bid.amount + " WHERE id = " + bid.itemNum;
							serverDB.sqlite_Execute(queryString);
							transmission.Send_ACK(serverSocket, sendString, address);
							return;
						}
						else { // translimit == 1, it should be bought
							// Tell bidder and update the database
							String sellerName = rs.getString(1);
							String sendString = bid.itemNum + " " + rs.getString(2) + " " + 
									(Integer.parseInt(rs.getString(5)) + bid.amount);
							queryString = "DELETE FROM items WHERE id = " + bid.itemNum;
							serverDB.sqlite_Execute(queryString);
							transmission.Send_ACK(serverSocket, "purchased " + sendString, address);
							
							// Tell seller
							sendString = "sold " + sendString;
							sendString = ServerTransmission.SetTypeHeader(sendString, ServerTransmission.SockType.SOLD);
							if ((address = transmission.addressHashMap.get(sellerName)) == null) {
								// seller is not in address book
								System.out.println(sellerName + " is not in address book!");
								queryString = "INSERT INTO messages VALUES (\'" + sellerName + "\', \'" +
										ServerTransmission.RemoveTypeHeader(sendString) + "\')";
								serverDB.sqlite_Execute(queryString);
								return;
							} else if (transmission.Send_By_Address(serverSocket, sendString, 
									transmission.addressHashMap.get(sellerName)) == null) {
								// seller is offline
								transmission.TreatLostUser(serverSocket, sellerName);
								System.out.println(sellerName + " is offline!");
								queryString = "INSERT INTO messages VALUES (\'" + sellerName + "\', \'" +
										ServerTransmission.RemoveTypeHeader(sendString) + "\')";
								serverDB.sqlite_Execute(queryString);
							}
							return;
						}
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void doDirect(String recvString, TransmissionAddress address) {
		// Format from buyer: item-num,buyer-name
		// Format from seller: sellername:item-num,buyer-name
		transmission.Send_ACK(serverSocket, "", address);
		int index = recvString.indexOf(':');
		String senderName = null;
		if (index != -1) {
			senderName = recvString.substring(0, index);
			recvString = recvString.substring(index + 1);
		}
		index = recvString.indexOf(',');
		int itemNum = Integer.parseInt(recvString.substring(0, index));
		String buyerName = recvString.substring(index + 1);
		ResultSet rs = serverDB.sqlite_Query("SELECT itemname, buynow, sellername FROM items WHERE " +
				"id = " + itemNum);
		
		String sendString = null;
		try {
			if (!rs.next()) {
				// no such item
				sendString = "Error: " + itemNum + " not found";
				sendString = ServerTransmission.SetTypeHeader(sendString, ServerTransmission.SockType.ERROR);
				// if send from seller, if from buyer need to tell him/her
				if (senderName != null) {
					address = transmission.addressHashMap.get(senderName);
					transmission.Send_By_Address_No_ACK(serverSocket, sendString, address);
				}
				try {
					Thread.sleep(73);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// tell buyer
				if ((address = transmission.addressHashMap.get(buyerName)) != null) {
					transmission.Send_By_Address_No_ACK(serverSocket, sendString, address);
				}
				return;
			} else {
				if (senderName !=null) {
					// check if send from seller, whether is his/her item, if not stop this trade
					if (!senderName.equals(rs.getString(3))) {
						// not the sender's item
						sendString = "Error: " + itemNum + " is not yours";
						sendString = ServerTransmission.SetTypeHeader(sendString, ServerTransmission.SockType.ERROR);
						transmission.Send_By_Address_No_ACK(serverSocket, sendString, address);
						try {
							Thread.sleep(73);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						// tell buyer
						sendString = "Error: " + itemNum + " is not " + senderName + "\'s";
						sendString = ServerTransmission.SetTypeHeader(sendString, ServerTransmission.SockType.ERROR);
						if ((address = transmission.addressHashMap.get(buyerName)) != null) {
							transmission.Send_By_Address_No_ACK(serverSocket, sendString, address);
						}
						return;
					}
				}
				
				if ((senderName == null) && (buyerName.equals(rs.getString(3)))) {
					// buyer own's the item
					try {
						Thread.sleep(73);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					address = transmission.addressHashMap.get(buyerName);
					sendString = ServerTransmission.SetTypeHeader("Error: owner", ServerTransmission.SockType.ERROR);
					transmission.Send_By_Address_No_ACK(serverSocket, sendString, address);
					return;
				}
				
				// buy succeed
				String sellerName = rs.getString(3);
				String queryString = null;
				sendString = itemNum + " " + rs.getString(1) + " " + rs.getString(2);
				String soldString = "sold " + sendString;
				String purchaseString = "purchased " + sendString;
				
				// deal with the databse
				serverDB.sqlite_Execute("DELETE FROM items WHERE id = " + itemNum);
				
				// first tell seller
				if ((address = transmission.addressHashMap.get(sellerName)) == null) {
					// seller is not in address book
					System.out.println(sellerName + " is not in address book!");
					queryString = "INSERT INTO messages VALUES (\'" + sellerName + "\', \'" +
							soldString + "\')";
					serverDB.sqlite_Execute(queryString);
				} else {
					soldString = ServerTransmission.SetTypeHeader(soldString, ServerTransmission.SockType.SOLD);
					if (transmission.Send_By_Address(serverSocket, soldString, address) == null) {
						// seller is offline
						transmission.TreatLostUser(serverSocket, sellerName);
						System.out.println(sellerName + " is offline!");
						queryString = "INSERT INTO messages VALUES (\'" + sellerName + "\', \'" +
								ServerTransmission.RemoveTypeHeader(soldString) + "\')";
						serverDB.sqlite_Execute(queryString);
					}
				}
				
				
				// Tell buyer, first sleep a while for seller tell buyer receive
				try {
					Thread.sleep(73);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if ((address = transmission.addressHashMap.get(buyerName)) == null) {
					// seller is not in address book
					System.out.println(buyerName + " is not in address book!");
					queryString = "INSERT INTO messages VALUES (\'" + buyerName + "\', \'" +
							purchaseString + "\')";
					serverDB.sqlite_Execute(queryString);
				} else {
					purchaseString = ServerTransmission.SetTypeHeader(purchaseString, ServerTransmission.SockType.PURCHASE);
					if (transmission.Send_By_Address(serverSocket, purchaseString, address) == null) {
						// seller is offline
						transmission.TreatLostUser(serverSocket, buyerName);
						System.out.println(buyerName + " is offline!");
						queryString = "INSERT INTO messages VALUES (\'" + buyerName + "\', \'" +
								ServerTransmission.RemoveTypeHeader(purchaseString) + "\')";
						serverDB.sqlite_Execute(queryString);
					}
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void ListenLoop () {
		while (true) {
			TransmissionAddress address = new TransmissionAddress();
			String recvString = transmission.Read_Recv(serverSocket, address);
			ServerTransmission.SockType type = ServerTransmission.GetTypeHead(recvString);
			recvString = ServerTransmission.RemoveTypeHeader(recvString);

			if (type == ServerTransmission.SockType.REGISTER) {
				doRegister(recvString, address);
			} else if (type == ServerTransmission.SockType.DEREGISTER) {
				doDeregister(recvString, address);
			} else if (type == ServerTransmission.SockType.SELL) {
				doSell(recvString, address);
			} else if (type == ServerTransmission.SockType.INFO) {
				doInfo(recvString, address);
			} else if (type == ServerTransmission.SockType.BID) {
				doBid(recvString, address);
			} else if (type == ServerTransmission.SockType.DIRECT) {
				doDirect(recvString, address);
			}
		}
	}
	
	public Server(String[] args) throws ClassNotFoundException, IOException {
		
		// first check input arguments
		if (!Accessory.CheckArgument(args, "server")) {
			System.err.println("Error: arguments for server should " +
					"be like -s <server-port>\n");
			System.exit(-1);
		}
		
		serverDB = new ServerDB();
		//serverDB.DB_Reset();
		transmission = new ServerTransmission(args);
		serverSocket = new DatagramSocket(ServerTransmission.serverPort);
		
		ListenLoop();
	}

}
