import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class FTPServer {
	
	private int serverPortNo;
	private static boolean packetLoss = false;
	
	public FTPServer( int serverPortNo) {
		this.serverPortNo = serverPortNo;
	}
	
	public static void main(String args[]) {
        ServerSocket serverSocket = null;
        Socket socket = null;
        int serverPortNo = 0;
        try {
        	serverPortNo = Integer.parseInt(args[0]);
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
        
        try {
        	FTPServer ftpServer = new FTPServer( serverPortNo );
        	serverSocket = new ServerSocket (ftpServer.serverPortNo);
        	System.out.println ("Server Waiting for client on port " + ftpServer.serverPortNo);

        	while(true) {
        		socket = serverSocket.accept();
        		System.out.println( " THE CLIENT ->" + socket.getInetAddress() + ":" + socket.getPort() + " IS CONNECTED ");
        		OutputStream output = socket.getOutputStream();
        		BufferedReader inFromClient = new BufferedReader(new InputStreamReader (socket.getInputStream()));
        		BufferedWriter outToClient = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        		
        		if( args[1].equals("") || args[1].equals(null)) {
   					throw new IllegalArgumentException("Filename not valid");
   				}        		
        		String fromClient = inFromClient.readLine();
        		System.out.println(" In From Client = " + fromClient);
        			
        		if( "ftp".equalsIgnoreCase(fromClient)) {
        			outToClient.write("READY\n");
        			outToClient.flush();
        			FileWriter fw = new FileWriter(args[1], true);
        			BufferedWriter bw = new BufferedWriter(fw);
        			
        			int seqNumber;
        			int lossPacketSeqNo = -1;
        			while( true ) {
        				String data = "";
        				String receivedData = "";
        				while( true ) {
        					data = inFromClient.readLine();
        					if (  "nothing".equals(data) || data == null) {
        						break;
        					}
        					receivedData = receivedData + data;
        				}
        				if (  receivedData == null || receivedData == "" ) {
        					break;
        				}
        				int pos = receivedData.indexOf(':');
        				int checksumPosition = receivedData.lastIndexOf(':');
        				if(pos == -1) {
        					break;
        				}
        				
        				seqNumber = Integer.parseInt(receivedData.substring(0, pos));
        				String fileData = receivedData.substring( pos+1, checksumPosition );
        				
        				int receivedChecksum = Integer.parseInt(receivedData.substring( checksumPosition+1 ));
        				int actualChecksum = calculateChecksum(""+seqNumber+":"+fileData);
        				double prob = Math.random();
        				double p = Double.parseDouble(args[2]);
        				//System.out.println("prob = " + prob + " , p = " + p );
        				//System.out.println("Actual Checksum , Received Checksum = " + actualChecksum + " , " + receivedChecksum );
        				if( prob < p ) {
        					if( !packetLoss ) {
        						packetLoss = true;
        						lossPacketSeqNo = seqNumber;
        						System.out.println("\nPacket loss, sequence number = " + lossPacketSeqNo + "\n\n" );
        					}
        				} else if( actualChecksum != receivedChecksum ) {
        					if( !packetLoss ) {
        						packetLoss = true;
        						lossPacketSeqNo = seqNumber;
        						System.out.println("\nPacket loss, sequence number = " + lossPacketSeqNo + "\n\n" );
        					}
        				} else if ( packetLoss && seqNumber == lossPacketSeqNo ){
        					packetLoss = false;
        				}
        				
        				if( !packetLoss ) {
        					bw.write(fileData);
        					int ACK = seqNumber;
        					//System.out.println("Sending ACK, sequence number: " + ACK + "\n");
        					outToClient.write("" + ACK);
        					outToClient.newLine();
        					outToClient.flush();
        				}
        			}
        			bw.close();
        		}
        		output.close();
        	}
        } catch (Exception e) {
        	System.out.println("Error: " + e.getMessage());
        	e.printStackTrace();
        } finally {
        	try {
        		serverSocket.close();
        	} catch ( IOException ioe ){
        		System.out.println("Error: Closing the socket, " + ioe.getMessage() );
        		ioe.printStackTrace();
        	}
        }
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
}
