import java.net.*;
import java.io.*;
import java.util.*;

class ClientServeHandler implements Runnable
{
	Socket client;
	String to_test;

	ClientServeHandler(Socket client, String to_test)
	{
		this.client=client;
		this.to_test=to_test;
	}
	
	public void run()
	{
		try
		{
			
			BufferedReader in=new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter out=new PrintWriter(client.getOutputStream(),true); 
			
			String get_msg=in.readLine();
			String get_msg_delimit[]=get_msg.split(" ");
			
			String rfc=null;
			//System.out.println("came to read rfc");
			File f=new File("RFCFile"+get_msg_delimit[2]+".txt");
			if(f.isFile())	
			rfc=read_rfc_from_file(get_msg_delimit[2]);
			else
			rfc="Bad 404 File Not Found";
			
			
			//out.write(to_test);
			out.println(rfc);
			out.close();
			in.close();	
			client.close();
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("whoops something went wrong");
		}
	
	}	
	
	String read_rfc_from_file(String get_msg_delimit)throws Exception
	{
	
		String to_test="";
		//System.out.println("came to read");
		FileReader f=null;
		
		f=new FileReader("RFCFile"+get_msg_delimit+".txt");
	
		
		BufferedReader fr=new BufferedReader(f);
		String r;

		while((r=fr.readLine())!=null)
		to_test+=r+"\n";


		
		return to_test;
	}

}