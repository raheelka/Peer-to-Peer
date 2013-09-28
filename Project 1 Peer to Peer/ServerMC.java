import java.io.*;
import java.net.*;
import java.util.*;

class ServerMC
{
	LinkedList lap; //List of Active Peers
	LinkedList lrfc; //List of RFC names and host 
	
	ServerMC()
	{
		lap=new LinkedList<String []>();
		lrfc=new LinkedList<String []>();
		
	}
	
	
	public static void main(String args[]) throws Exception
	{
		int servers_port_no=7734; //Port to which server will listen
		
		
	
		ServerMC smc=new ServerMC();
		ServerSocket s=new ServerSocket(servers_port_no);
		
		String ServerHostName=InetAddress.getLocalHost().getHostAddress();
		System.out.println("here is the name "+ServerHostName);
		
		Socket client=null;
		int number=0; //For time being use this as the host name
		while(true)
		{
			
			
			number++;
			client=s.accept();
			
			String client_details=register_client(client,smc); //register the client ... Add him to list of active peers
			String client_port_name[]=client_details.split(",");
			
			String client_port_no=client_port_name[0];
			String client_host_name=client_port_name[1];	
		
		//Start a Thread to serve the client				
			Thread t=new Thread(new ClientHandler(client,smc.lap,smc.lrfc,client_host_name,client_port_no));
			t.start();
		}
		

			
	}
	
	
		static String register_client(Socket client,ServerMC smc)throws Exception
		{
			BufferedReader into=new BufferedReader(new InputStreamReader(client.getInputStream()));
			String gp=null;
			String client_host_name=null;
			String client_port_no=null;
			if((gp=into.readLine())!=null)
			{
				String client_detail[]=gp.split(",");
				client_host_name=client_detail[1];
				client_port_no=client_detail[0];
				String ar_lap[]=new String[3];
				ar_lap[0]=client_host_name;
				ar_lap[1]=client_port_no;
				ar_lap[2]="true";

				smc.lap.add(ar_lap);
								
			}
			return client_port_no+","+client_host_name;
			
		}
	
	
	
}