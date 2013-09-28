import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;


public class SelectiveRepeatClient {
	
	private static final int MAX_ROUND_TRIP_TIME = 10000000;
	private static Socket clientSocket = null;
	private static int windowSize;
	private static int MSS;
	private static int serverPortNo;
	private static int currentWindow = 0;
	private static int spin = 0;
	private static int currentACK = -1;
	private static int prevACK = -1;
	private static ArrayList<Integer> negACK;
	
	private static Calendar C1;
	private static Calendar C2;
	
	public static void main(String args[]) throws Exception {
		
		String hostName = args[0];		
		try {
        	serverPortNo = Integer.parseInt(args[1]);
        	if( serverPortNo < 0 || serverPortNo > 65535 ) {
        		throw new IllegalArgumentException();
        	}
        } catch (NumberFormatException nfe) {
        	System.out.println("Error: Port No should be an integer");
        	nfe.printStackTrace();
        } catch (IllegalArgumentException iae) {
        	System.out.println("Error: Invalid Port No");
        	iae.printStackTrace();
        }
		
		clientSocket = new Socket(hostName, serverPortNo);
		windowSize = Integer.parseInt( args[3] );
		MSS = Integer.parseInt( args[4] );
		
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(),true);	
		try {
    		if( args[1].equals("") || args[1].equals(null)) {
    			clientSocket.close();
    			throw new IllegalArgumentException("Filename not valid");
    		}
    	} catch (IllegalArgumentException iae) {
    		System.out.println("Error: " + iae.getMessage());
    		iae.printStackTrace();
    	} 
	     
      	System.out.println("SEND(ftp or FTP to transfer file) ... \n");
       	outToServer.write(inFromUser.readLine() + "\n" );
       	outToServer.flush();
        	
       	// get response from the server
        String serverStatus = inFromServer.readLine();
       	if( serverStatus.equalsIgnoreCase("READY")) {
        	System.out.println("Sending File to the Server on port: " + serverPortNo + "\n");
        	String filename = args[2];
        	rdt_send( filename);
	        clientSocket.close();
		}   
	}
	
	@SuppressWarnings("deprecation")
	public static void rdt_send( String filename ) throws Exception {
		PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
		FileInputStream file = new FileInputStream( filename );
		// setting the sending buffer size of the packet to MSS
    	byte[] buffer = new byte[MSS];
    	TreeMap<Integer, String> clientBuffer = new TreeMap<Integer, String>();
    	int bytesRead = 0;
    	int seqNumber = 0;
    	
    	while( (bytesRead = file.read(buffer)) > 0) {
    		String temp = new String( buffer );
    		clientBuffer.put(seqNumber, temp);
    		seqNumber = seqNumber + bytesRead;
    		buffer = new byte[MSS];
    	}
    	
      	SelectiveRepeatClient ftpClient = new SelectiveRepeatClient();
    	Thread ackHandlerThread = new Thread( ftpClient.new ACKHandler(clientSocket) );
    	ackHandlerThread.start();
    	
    	C1 = Calendar.getInstance();
    	long t1 = C1.getTimeInMillis();
    	
    	seqNumber = 0;
    	String sendData = "";
    	Iterator<Map.Entry<Integer, String>> iter = clientBuffer.entrySet().iterator();
    	while( iter.hasNext() ) {
    		
    		Map.Entry<Integer, String> entry = (Map.Entry<Integer, String>) iter.next();
    		seqNumber = entry.getKey();
    		sendData = entry.getValue();
    		
    		int timer = 0;
    		while( true ) {
    			if( getCurrentWindow() < windowSize ) {
    				break;
    			}
    			timer++;
    			if( timer == MAX_ROUND_TRIP_TIME ) {
    				iter = clientBuffer.entrySet().iterator();
        			Map.Entry<Integer, String> iterEntry = null;
    				while( iter.hasNext() ) {
    					if( prevACK == -1 ) {
    						break;
    					}
    					iterEntry = (Map.Entry<Integer, String>) iter.next();
    					if( iterEntry.getKey() == prevACK ) {
    						break;
    					}
    				}
    				iterEntry = (Map.Entry<Integer, String>) iter.next();
    				seqNumber = iterEntry.getKey();
    	    		sendData = iterEntry.getValue();
    	    		break;
    			}
    		}
    		
    		String payload = "" + seqNumber + ":" + sendData;
    		int checksum = calculateChecksum(payload);
    		String data = payload + ":" + checksum;
    		//System.out.println("Sending packet, sequence number = " + seqNumber );
    		outToServer.println(data);
    		outToServer.println("nothing");
    		outToServer.flush();
    		updateWindow(0);

    	}
    	
    	while(  getCurrentWindow() != 0 ) {
    		
    		if( !negACK.isEmpty() ) {
    			int resendSeqNumber = negACK.remove(0);
    			iter = clientBuffer.entrySet().iterator();
    			Map.Entry<Integer, String> iterEntry = null;
    			while( iter.hasNext() ) {
				
    				iterEntry = (Map.Entry<Integer, String>) iter.next();
    				if( iterEntry.getKey() == resendSeqNumber ) {
    					break;
    				}
    			}
    			seqNumber = iterEntry.getKey();
    			sendData = iterEntry.getValue();
    			String payload = "" + seqNumber + ":" + sendData;
    			int checksum = calculateChecksum(payload);
    			String data = payload + ":" + checksum;
    			System.out.println("Sending packet, sequence number = " + seqNumber );
    			outToServer.println(data);
    			outToServer.println("nothing");
    			outToServer.flush();
    		}
    	}
    	
    	outToServer.println("DONE");
    	outToServer.println("nothing");
		outToServer.flush();
    	ackHandlerThread.stop();
    	file.close();
    	
    	C2 = Calendar.getInstance();
    	long t2 = C2.getTimeInMillis();
    	System.out.println("File Transfer time: " + (t2-t1) + " millisecs");
    	System.out.println("File Transfer Complete.");
	}
	
	
	public static synchronized int getCurrentWindow() {
		while( spin != 0 );
		return currentWindow;
	}
	
	
	public static synchronized void updateWindow( int threadId ) {
		spin = 1;
		if ( threadId == 0 ) {
			currentWindow = ( currentWindow % windowSize ) + 1;
		} else {
			currentWindow = currentWindow - 1;
		}
		spin = 0;
	}
	
	
	public static int calculateChecksum( String data ){
		int checksum = 0;
		for(int i=0; i< data.length(); i++ ) {
		    char c = data.charAt(i);
		    if( c != '\n' ) {
			    checksum = checksum + c;
			}
		}
		return( checksum );
	}
	
	class ACKHandler implements Runnable {
		
		private Socket socket = null;
		public ACKHandler( Socket socket ) {
			this.socket = socket;
		}
		
		public Socket getSocket() {
			return this.socket;
		}
		
		public void run() {
			negACK = new ArrayList<Integer>();
			try {
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
				while( true ) {
					String ACK = inFromServer.readLine();
					if( ACK.contains("NACK") ) {
						int pos = ACK.indexOf(':');
						int seqNumber = Integer.parseInt(ACK.substring(pos+1));
						System.out.println("\n\nReceived NACK, sequence number = " + seqNumber + "\n\n");
						negACK.add(seqNumber);
					} else {
						prevACK = currentACK;
						currentACK = Integer.parseInt(ACK);
						updateWindow(1);
					}
				}
			} catch (Exception e) {
				System.out.println("Error: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
}



