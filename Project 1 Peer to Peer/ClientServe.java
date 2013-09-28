import java.net.*;
import java.io.*;
import java.util.*;

class ClientServe implements Runnable
{
	int clients_server_port;
	String to_test;
	
	ClientServe(int clients_server_port,String to_test)
	{
		 this.clients_server_port=clients_server_port;
		 this.to_test=to_test;
	}
	
	public void run()
	{
		try
		{
			ServerSocket s=new ServerSocket(clients_server_port);
			Socket client=null;
		
			while(true)
			{
				client=s.accept();		
			
				Thread t=new Thread(new ClientServeHandler(client,to_test));
				t.start();
			}
		
		}
		catch(Exception e)
		{
			System.out.println("Exception in ClientServer");
			e.printStackTrace();
		}
	}
	
	
}
