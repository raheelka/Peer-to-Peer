import java.net.*;
import java.io.*;
import java.util.*;

class ClientHandler implements Runnable
{
	Socket client ;
	LinkedList lap;
	LinkedList lrfc;
	String client_host_name;
	String client_port_no;
		
	ClientHandler(Socket client,LinkedList lap,LinkedList lrfc,String client_host_name,String client_port_no)
	{
		this.client=client;	
		this.lap=lap;
		this.lrfc=lrfc;
		this.client_host_name=client_host_name;
		this.client_port_no=client_port_no;
	}
	
	public void run()
	{
		try
		{
			//Set Input OutPut Streams
			
			BufferedReader in=new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter out=new PrintWriter(client.getOutputStream(),true);
			
			
			String msg=null;
			while((msg=in.readLine())!=null)
			{
			
				//System.out.println(msg);
				boolean done=false;
				//Iterator<int []> li=l.iterator();
	
	
	//Remember to do out.println("nothing") if out queue is not used
				
				if(msg.equals("bye"))
				break;				
				else
				{
					String delimit_msg[]=msg.split(" ");
					
					if(delimit_msg[0].equals("ADD"))
					{
						//System.out.println("came in add " + delimit_msg[0]);
						add_rfc(lrfc,delimit_msg,client_host_name);
						display_msg(delimit_msg,client_port_no,client_host_name);
						out.println("nothing");
						done=true;
					}
					else if(delimit_msg[0].equals("LOOKUP"))
					{
						lookup_rfc(lrfc,lap,delimit_msg,out);
						display_msg(delimit_msg,client_port_no,client_host_name);
						done=true;
						out.flush();
					}
					else if(delimit_msg[0].equals("LIST"))
					{
						list_rfc(lrfc,lap,delimit_msg,out);
						display_msg(delimit_msg,client_port_no,client_host_name);
						done=true;
						out.flush();
					}
					
					else if(delimit_msg[0].equals("UPDATE"))
					{
						update_list(lrfc,delimit_msg,client_host_name);
						display_msg(delimit_msg,client_port_no,client_host_name);
						out.println("nothing");
						done=true;
					}	
					
					else if(delimit_msg[0].equals("DELETE"))
					{
						delete_peer_lrfc(lrfc,delimit_msg[1],client_host_name);
						delete_peer_lap(lap,delimit_msg[1],client_host_name);
						out.println("nothing");
						done=true;
					}
					else if(delimit_msg[0].equals("GET"))
					{
						display_msg(delimit_msg,client_port_no,client_host_name);
						out.println("nothing");
						done=true;
					}
					else if(delimit_msg[0].equals("NOUPDATE"))
					{
						out.println("nothing");
						done=true;
					}				
					
				}	
									
				if(!done)
				out.println("nothing");
				
		
				
			}
		
			out.close();
			//client.close();
		}
		catch(Exception e)
		{
			System.out.println("The client just exited");
			//e.printStackTrace();
		}
	}//Run ends
	
	
	void add_rfc(LinkedList lrfc,String delimit_msg[],String client_host_name)
	{
		String ar_lrfc[]=new String[4];
		ar_lrfc[0]=delimit_msg[2];	//RFC Number
		ar_lrfc[1]=delimit_msg[3];	//RFC Name
		ar_lrfc[2]=client_host_name;
		ar_lrfc[3]="true";			
		
		lrfc.add(ar_lrfc);
		//System.out.println("added Successfully");
	}
	
	void lookup_rfc(LinkedList lrfc,LinkedList lap,String delimit_msg[],PrintWriter out)
	{
		String temp="";
		Iterator<String []> li=lrfc.iterator();
		Boolean found_rfc=false;
		while(li.hasNext())
		{
			String temp_ar[]=li.next();						
			if(delimit_msg[2].equals(temp_ar[0]))
			{
				Iterator<String []> li2=lap.iterator();
				while(li2.hasNext())
				{
					String temp_ar2[]=li2.next();
					if(temp_ar2[0].equals(temp_ar[2]) && temp_ar2[2].equals("true"))
					{
						temp="P2P-CI/1.0 200 OK SSS RFC Number->"+temp_ar[0]+" Host name "+temp_ar[2]+" Port No-> "+temp_ar2[1];
						found_rfc=true;
					}
					//temp+="Host->"+temp_ar[2]+" RFC->"+temp_ar[0]+" Port No->"+temp_ar2[1]+" ||||" ; 
				}
				
			}
		}
		if(found_rfc==true)
		out.println(temp);	
		else
		out.println("P2P-CI/1.0  404 NotFound");
										
	}
	
	void list_rfc(LinkedList lrfc,LinkedList lap,String delimit_msg[],PrintWriter out)
	{
		String temp="";
		Iterator<String []> li=lrfc.iterator();
		
		while(li.hasNext())
		{
			String temp_ar[]=li.next();	
			Iterator<String []> li2=lap.iterator();
				while(li2.hasNext())
				{
					String temp_ar2[]=li2.next();
					if(temp_ar2[0].equals(temp_ar[2]) && temp_ar2[2].equals("true"))
					temp+="P2P-CI/1.0 200 OK SSS RFC Number ->"+temp_ar[0]+" Host name "+temp_ar[2]+" Port No->"+temp_ar2[1]+"SSS";
					//temp+="Host->"+temp_ar[2]+" RFC->"+temp_ar[0]+" Port No->"+temp_ar2[1]+ " |||| " ; 
				}
		
		}
		
		//System.out.println("The value of temp is "+ temp);
		//while(temp!=null)
		out.println(temp);	
	}
	
	void delete_peer_lrfc(LinkedList lrfc, String clients_port,String client_host_name)throws Exception
	{
		//System.out.println("came in delete lrfc");
		Iterator<String []> li=lrfc.iterator();
		int i=0;
		while(li.hasNext())
		{
		
			String temp_ar[]=li.next();
			if(client_host_name.equals(temp_ar[2]))
			{
				//System.out.println("Removing lrfc "+client_host_name + "found is "+temp_ar[2]);
				temp_ar[3]="false";
				//lrfc.remove(i);
			}
			i++;
		
		}
		
	}
	
	
	
	void delete_peer_lap(LinkedList lap, String clients_port,String client_host_name)throws Exception
	{
		
		int i=0;
		
		Iterator<String []> li2=lap.iterator();

		while(li2.hasNext())
		{
			
			String temp_ar2[]=li2.next();
			//System.out.println("moving here and String of "+temp_ar2[0]+" "+temp_ar2[1]);
			if(clients_port.equals(temp_ar2[1]))
			{
				//System.out.println("Removing lap "+client_host_name + "found is "+temp_ar2[1]);
				temp_ar2[2]="false";
				//lap.remove(i);
			}
			i++;
			
			
		}
	}
	
	
	
	
	
	
	void update_list(LinkedList lrfc,String delimit_msg[],String client_host_name)
	{
		add_rfc(lrfc,delimit_msg,client_host_name);
	}
	
	void display_msg(String delimit_msg[],String client_port_no,String client_host_name)
	{
		String rfc_no="";
		String rfc_title="";
		if(delimit_msg.length>2)
		{
			
			rfc_no=delimit_msg[2];
		}
		if(delimit_msg.length>3)
		{
			
			rfc_no=delimit_msg[2];
			rfc_title=delimit_msg[3];
		}
		
		
		String clients_server_port=client_port_no;
		if(delimit_msg[0].equals("ADD") || delimit_msg[0].equals("UPDATE") )
		System.out.println("\n \nADD RFC "+rfc_no+" P2P-CI/1.0 \nHost->"+client_host_name + "\nPort: "+clients_server_port+"\n");
		if(delimit_msg[0].equals("LOOKUP"))
		System.out.println("\n \nLOOKUP RFC "+rfc_no+" P2P-CI/1.0 \nHost->"+client_host_name +"\nPort: "+clients_server_port+"\n");
		if(delimit_msg[0].equals("LIST"))
		System.out.println("\n \nLIST ALL P2P-CI/1.0 \nHost->"+client_host_name +"\nPort: "+clients_server_port+"\n");
		if(delimit_msg[0].equals("GET"))
		System.out.println("\n \nGET RFC "+rfc_no+" P2P-CI/1.0 \nHost->"+client_host_name +"\nOperating System"+delimit_msg[5]+"\n");
	}
		
	
	
	
}//class ends