package Accessory;

import java.util.Random;

public class Accessory {
	static final int Max_Username_Length = 64;
	
	public static Boolean CheckArgument(String[] args, String mode) {
		// mode is "server" or "client"
		int i = 0;
		for (@SuppressWarnings("unused") String s : args) {
			i++;
		}
		if (mode.equalsIgnoreCase("server")) {
			if ((i != 2) || (!args[0].equals("-s")))
				return false;
		}
		else if (mode.equalsIgnoreCase("client")) {
			if ((i != 3) || (!args[0].equals("-c")))
				return false;
		}
		else {
			System.err.println("Mode error, mode should be \"server\" or \"client\"!");
		}
		return true;
	}
	
	public static int CheckPort(String Port) {
		// return the port number is valid, else return -1
		if (Port.length() > 5) return -1;
		for (int i = 0; i < Port.length(); i++) {
			if ((Port.charAt(i) < '0') || (Port.charAt(i) > '9')) {
				return -1;
			}
		}
		int portInt = Integer.parseInt(Port);
		if ((portInt < 1024) || (portInt > 65535)) {
			return -1;
		}
		return portInt;
	}
	
	public static Boolean CheckIP(String IP) {
		if (IP.equalsIgnoreCase("localhost")) {
			return true;
		}
		String s;
		int index;
		for (int i = 0; i < 3; i++) {
			index = IP.indexOf('.');
			if (index == -1) {
				return false;
			}
			s = IP.substring(0, index);
			if ((Integer.parseInt(s) > 255) || (Integer.parseInt(s) < 0)) {
				return false;
			}
			IP = IP.substring(index + 1);			
		}
		if ((Integer.parseInt(IP) > 255) || (Integer.parseInt(IP) < 0)) {
			
			return false;
		}
		return true;
	}
	
	public static Boolean is_Register(String input) {
		int index = input.indexOf(' ');
		if (index == -1) return false;
		String s = input.substring(0, index);
		if ((!s.equals("register")) && (!s.equals("Register"))) {
			return false;
		}
		return true;
	}
	
	public static Boolean is_Deregister(String input) {
		while (input.charAt(input.length() - 1) == ' ') input = input.substring(0, input.length() - 1);
		if (input.equals("deregister") || input.equals("Deregister")) return true;
		else {
			return false;
		}
	}
	
	public static Boolean is_Sell(String input) {
		int index = input.indexOf(' ');
		if (index == -1) return false;
		String s = input.substring(0, index);
		if ((!s.equals("sell")) && (!s.equals("Sell"))) {
			return false;
		}
		return true;
	}
	
	public static Boolean is_Info(String input) {
		int index = input.indexOf(' ');
		String s = input;
		if (index != -1) {
			s = input.substring(0, index);
		}
		if ((!s.equals("info")) && (!s.equals("Info"))) {
			return false;
		}
		return true;
	}
	
	public static Boolean is_Bid(String input) {
		int index = input.indexOf(' ');
		if (index == -1) return false;
		String s = input.substring(0, index);
		if ((!s.equals("bid")) && (!s.equals("Bid"))) {
			return false;
		}
		return true;
	}
	
	public static Boolean is_Buy(String input) {
		int index = input.indexOf(' ');
		if (index == -1) return false;
		String s = input.substring(0, index);
		if ((!s.equals("buy")) && (!s.equals("Buy"))) {
			return false;
		}
		return true;
	}
	
	public static String GetRegisterName(String input) {
		int index = input.indexOf(' ');
		String s = input.substring(index);
		while ((s.charAt(0) == ' ') && (s.length() >=2)) s = s.substring(1);
		
		if (s.equals(" ")) return null;
		
		if (s.length() > Max_Username_Length) {
			return s.substring(0, Max_Username_Length);
		}
		else {
			return s;
		}
	}
	
	public static Boolean GetSellInfo(String input, Sell sell) {
		int index = input.indexOf(' ');
		String s = input.substring(index);
		while ((s.charAt(0) == ' ') && (s.length() >=2)) s = s.substring(1);
		
		// for item name
		index = s.indexOf(' ');
		if (index == -1) {
			System.out.println("[Error: missing arguments]");
			return false;
		}
		String itemName = s.substring(0, index);
		if (itemName.length() > 64) itemName = itemName.substring(0, 64);
		sell.itemName = itemName;
		s = s.substring(index + 1);
		while ((s.charAt(0) == ' ') && (s.length() >=2)) s = s.substring(1);
		
		// for transction limit
		index = s.indexOf(' ');
		if (index == -1) {
			System.out.println("[Error: missing arguments]");
			return false;
		}
		String transLimitString = s.substring(0, index);
		if (!CheckIntString(transLimitString)) {
			System.out.println("[Error: transction limit is not an integer]");
			return false;
		}
		if ((sell.transLimit = Integer.parseInt(transLimitString)) <= 0) {
			System.out.println("[Error: transction limit is not positive]");
			return false;
		}
		s = s.substring(index + 1);
		while ((s.charAt(0) == ' ') && (s.length() >=2)) s = s.substring(1);
		
		// for start bid
		index = s.indexOf(' ');
		if (index == -1) {
			System.out.println("[Error: missing arguments]");
			return false;
		}
		String startBidString = s.substring(0, index);
		if (!CheckIntString(startBidString)) {
			System.out.println("[Error: start bid is not an integer]");
			return false;
		}
		if ((sell.startBid = Integer.parseInt(startBidString)) <= 0) {
			System.out.println("[Error: start bid is not positive]");
			return false;
		}
		s = s.substring(index + 1);
		while ((s.charAt(0) == ' ') && (s.length() >=2)) s = s.substring(1);
		
		// for buy now
		index = s.indexOf(' ');
		if (index == -1) {
			System.out.println("[Error: missing arguments]");
			return false;
		}
		String buyNowString = s.substring(0, index);
		if (!CheckIntString(buyNowString)) {
			System.out.println("[Error: buy now is not an integer]");
			return false;
		}
		if ((sell.buyNow = Integer.parseInt(buyNowString)) <= 0) {
			System.out.println("[Error: buy now is not positve]");
			return false;
		}
		s = s.substring(index + 1);
		while ((s.charAt(0) == ' ') && (s.length() >=2)) s = s.substring(1);
		
		// for description
		if (s.length() > 1024) s = s.substring(0, 1024);
		sell.desc = s;
		
		return true;
	}
	
	public static int GetItemNum(String input) {
		// 0 means there is no sepcific number, -1 means error
		while (input.charAt(input.length() - 1) == ' ') input = input.substring(0, input.length() - 1);
		int index = input.indexOf(' ');
		if (index == -1) return 0;
		String s = input.substring(index + 1);
		for (int i = 0; i < s.length(); i++) {
			if ((s.charAt(i) < '0') || (s.charAt(i) > '9')) {
				return -1;
			}
		}
		int itemNum = Integer.parseInt(s);
		if (itemNum < 0) return -1;
		return itemNum;
		
	}
	
	public static int GetBidInfo(String input, Bid bid) {
		// return -1: argument error; 0: negative bid;1: right
		int index = input.indexOf(' ');
		String s = input.substring(index);
		while ((s.charAt(0) == ' ') && (s.length() >=2)) s = s.substring(1);
		
		// for item-num
		index = s.indexOf(' ');
		if (index == -1) {
			System.out.println("[Error: missing arguments]");
			return -1;
		}
		String itemNumString = s.substring(0, index);
		if (!CheckIntString(itemNumString)) {
			System.out.println("[Error: item number is not an integer]");
			return -1;
		}
		if ((bid.itemNum = Integer.parseInt(itemNumString)) <= 0) {
			System.out.println("[Error: item number is not positive]");
			return -1;
		}
		s = s.substring(index);
		while ((s.charAt(0) == ' ') && (s.length() >= 2)) s = s.substring(1);
		
		// for amount
		String amountString = s;
		if (!CheckIntString(amountString)) {
			System.out.println("[Error: amount is not an integer]");
			return -1;
		}
		if ((bid.amount = Integer.parseInt(amountString)) <= 0) {
			System.out.println("[Error: amount is not positive]");
			return 0;
		}
		return 1;
	}
	
	public static Boolean GetBuyInfo(String input, Buy buy) {
		int index = input.indexOf(' ');
		String s = input.substring(index);
		while ((s.charAt(0) == ' ') && (s.length() >=2)) s = s.substring(1);
		
		// for seller name
		index = s.indexOf(' ');
		if (index == -1) {
			System.out.println("[Error: missing arguments]");
			return false;
		}
		buy.sellerName = s.substring(0, index);
		s = s.substring(index);
		while ((s.charAt(0) == ' ') && (s.length() >= 2)) s = s.substring(1);
		
		// for item number
		String itemNumString = s;
		if (!CheckIntString(itemNumString)) {
			System.out.println("[Error: item number is not an integer]");
			return false;
		}
		if ((buy.itemNum = Integer.parseInt(itemNumString)) <= 0) {
			System.out.println("[Error: item number is not positive]");
			return false;
		}
		return true;
	}
	
	public static int Random_Port(int lower, int upper) {
		Random rdm = new Random(System.currentTimeMillis());
		return Math.abs(rdm.nextInt()) % (upper - lower) + lower;
	}
	
	public static Boolean CheckIntString(String intString) {
		// return true if the string can be converted into and integer
		if (intString.length() == 0) return false;
		for (int i = 0; i < intString.length(); i++) {
			if ((i == 0) && (intString.charAt(0) == '-')) {
				continue;
			}
			if ((intString.charAt(i) < '0') || (intString.charAt(i) > '9')) {
				return false;
			}
		}
		return true;
	}
}
