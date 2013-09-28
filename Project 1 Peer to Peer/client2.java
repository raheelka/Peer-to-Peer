import java.io.*;
import java.net.*;

class Client2
{
	public static void main(String arg[]) throws Exception
	{
		
		if(arg.length<1)
		{
			System.err.println("Wrong arguments -- Usage is java ClientClass Servername "); 
			System.exit(-1);
		}
	    int clients_server_port=generate_random_port();
	    String to_test="hello this is server>> "+clients_server_port + "replying \n";
	    int main_servers_port=7734;
		Socket server=null;
		
		
		server=new Socket(arg[0],main_servers_port); //Establish connection with Server
		
			String client_host_name=InetAddress.getLocalHost().getHostAddress();
		
		send_server_myPortNo(server,clients_server_port,client_host_name);
		
		/*FileReader f=new FileReader("RFCFile.txt");
		BufferedReader fr=new BufferedReader(f);
		String r;
		while((r=fr.readLine())!=null)
		to_test+=r+"\n";*/
		//System.out.println(r);
		
	
		
		
		Thread t=new Thread(new ClientServe(clients_server_port,to_test));
		t.start();		//Start its own upload Server
		
		

		
		PrintWriter out=new PrintWriter(server.getOutputStream(),true);
		BufferedReader in=new BufferedReader(new InputStreamReader(server.getInputStream()));
		BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
		
		String msg="";
		String temp=null;
		int flag=0;
		boolean done=false;
		
		
		while(true)
		{
			if(flag!=0)
			{
				
				temp=in.readLine();
				
				if(!temp.equals("nothing"))
				{

					String represent[]=temp.split("SSS");
					for(int i=0;i<represent.length;i++)
					System.out.println(represent[i]);
				}
			}
			
			display_option();
			
			msg=br.readLine();
			
			if(msg.equals("bye"))
			{
				//System.out.println("came in bye");
				delete_me_server(out,clients_server_port);
				server.close();
				System.exit(-1);
				break;
			}
			
			String interpreted_msg=interpret_msg(msg,clients_server_port,client_host_name);
			
			
			
				String msg_ar[]=interpreted_msg.split(" ");
			
				if(msg_ar[0].equals("GET"))
				{
					String data=null;
					Socket s2=null;
					try{
					s2=new Socket(msg_ar[4],Integer.parseInt(msg_ar[3]));
					
					
					PrintWriter pw=new PrintWriter(s2.getOutputStream(),true);
					pw.println(interpreted_msg);//For the time being not necessary ... later will b required
					BufferedReader brs2=new BufferedReader(new InputStreamReader(s2.getInputStream()));
					
					int flag_print=0;
					String to_write="";
					while((data=brs2.readLine())!=null)
					{
						if(flag_print==0 && !data.startsWith("Bad"))
						{
							System.out.println("P2P-CI/1.0 200 OK \nOS:"+System.getProperty("os.name")+"\nContent Type:text/text\n");
							flag_print=1;
						}
						to_write+=data+"\n";
						System.out.println(data);
					}
					String bad[]=to_write.split(" ");
					boolean update=true;
					
					if(bad[0].equals("Bad"))
					update=false;
					
					if(update)
					{
						FileWriter write_rfc = new FileWriter("RFCFile"+msg_ar[2]+".txt",true);
						BufferedWriter buf_write=new BufferedWriter(write_rfc);
					//System.out.println(" here");
						buf_write.write(to_write);
						buf_write.close();
					//System.out.println("and here");
						update_me_server(out,msg_ar[2]);
					}
					else
					do_not_update_me_server(out);
					
					}
					catch(Exception e)
					{
						System.out.println("STATUS 400 REQUEST WAS BAD");
						do_not_update_me_server(out);
					}
					
				}
				else if(msg!=null)
				{
					out.println(interpreted_msg);
				}
			
			
			
				flag=1;
			
			
		
		}	
		System.out.println("closing");
		out.close();
		server.close();
	}
	
	static int generate_random_port()
	{
		return(4000 + (int)(Math.random() * ((1000) + 1)));
	}
	
	static void send_server_myPortNo(Socket server,int clients_server_port,String client_host_name)throws Exception
	{
		PrintWriter mstr=new PrintWriter(server.getOutputStream(),true);
		mstr.println(""+clients_server_port+","+client_host_name);
	}
	
	static void update_me_server(PrintWriter out,String rfc_number)
	{
		out.println("UPDATE RFC "+rfc_number+" DummyTitle");
	}
	
	static void do_not_update_me_server(PrintWriter out)
	{
		out.println("NOUPDATE RFC");
	}
	
	static void display_option()
	{
		System.out.println("\n \n What do you want to do ? \n 1.ADD 2.LOOKUP 3.LIST_ALL 4.GET ");
	}
	
	static void delete_me_server(PrintWriter out,int clients_server_port)
	{
		out.println("DELETE "+clients_server_port);
	}
	
	static String interpret_msg(String msg,int clients_server_port,String client_host_name)throws IOException
	{
		
		
		String interpreted_msg=null;
		
		int num=Integer.parseInt(msg);
		BufferedReader interpreter=new BufferedReader(new InputStreamReader(System.in));
		if(num==1)
		{
			
			System.out.println("Enter the RFC number to add");
			String rfc_no=interpreter.readLine();
			System.out.println("Enter the RFC title");
			String rfc_title=interpreter.readLine();
			interpreted_msg="ADD RFC "+rfc_no+" Dummy Title";
			String msg_to_display="\n \n ADD RFC "+rfc_no+" P2P-CI/1.0 \n Host-> "+client_host_name+"\n Port: "+clients_server_port+"\n Title:"+rfc_title+" \n "; 
			System.out.println(msg_to_display);
		}
		if(num==2)
		{
			
			System.out.println("Enter the RFC number to LOOKUP");
			String rfc_no=interpreter.readLine();
			interpreted_msg="LOOKUP RFC "+rfc_no;
			String msg_to_display="\n \n LOOKUP RFC "+rfc_no+" P2P-CI/1.0 \n Host-> "+client_host_name+ "\n Port: "+clients_server_port+"\n ";  
			System.out.println(msg_to_display);
		}
		if(num==3)
		{
		
			interpreted_msg="LIST RFC";
			String msg_to_display="\n \n LIST ALL P2P-CI/1.0 \n  Host-> "+client_host_name+ " \n Port: "+clients_server_port+"\n  "; 
			System.out.println(msg_to_display);
		}
		if(num==4)
		{
			String osName= System.getProperty("os.name");
			System.out.println("Enter the RFC number to GET ");
			String rfc_no=interpreter.readLine();
			System.out.println("Enter the host name  from whom you want the RFC");
			String host_name_of_client=interpreter.readLine();
			System.out.println("Enter the Peers Port from whom you want the RFC");
			String port_of_remote_peer= interpreter.readLine();
			String msg_to_display="\n \nGET RFC "+rfc_no+" P2P-CI/1.0 \n  Host-> "+client_host_name+"Operating system:"+osName+"\n  ";
			System.out.println(msg_to_display);
			interpreted_msg="GET RFC "+rfc_no+" "+port_of_remote_peer+" "+host_name_of_client+" "+osName;
		}
		
		if(num>4 || num<1)
		{
			System.out.println("P2P-CI/1.0 Bad Request 400");
			//interpreted_msg="Wrong Entry";
		}
		return interpreted_msg;
	}
	
}