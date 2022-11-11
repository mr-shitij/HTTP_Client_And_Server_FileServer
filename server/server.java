import java.net.*;
import java.util.*;
import java.io.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;

interface DataToSend{
	public String getName();
	public String getMessage();
}

class Header implements DataToSend, Serializable{
	private String msg;
	Header(String str){
		this.msg = str;
	}
	public String getMessage(){
		return msg;
	}
	public String getName(){
		return null;
	}
	public boolean equals(String msg1){
		return this.msg.equals(msg1);
	}
}

class ServerManual{

	public static String getManual(){
		String manual ="									\n"+ 
		"				*** Welcome To Sever ***				\n"+
		"											\n"+
		"	# Available Commands  : 							\n"+
		"			1. Audio -(Donwload audio from server.) Ex : music etc.		\n"+
		"			2. Document - (Any Text Document.)				\n"+
		"			3. Video - (Any Video File.)					\n"+
		//"			4. Data of Sensor / Device (Available Data of Device.)		\n"+
		"											\n"+
		"	1.Audios : 									\n";
		
		for(String aud : new File(System.getProperty("user.home")+ "/" + "ShareAble/" + "audio").list())
		{
			manual += "\n\t\t" + aud + "\n";
		}
		manual += "\t2.Document";
		for(String doc : new File(System.getProperty("user.home")+ "/" + "ShareAble/" + "document").list()){
			manual += "\n\t\t" + doc + "\n";
		}
		manual += "\t3.Video";
		for(String vid : new File(System.getProperty("user.home")+ "/" + "ShareAble/" + "video").list()){
			manual += "\n\t\t" + vid + "\n";
		}
		return manual;
	}
}

class FileData implements Serializable, DataToSend {
	private String name;
	private int length;
	private byte[] bFile;
	public static final long serialVersionUID = 1261427;

	FileData(String name, String path) throws FileDoesNotExistException, FileNotFoundException{
		this.name = name;
		this.bFile = Utility.convertFileTOByte(path + name);
		this.length = bFile.length;
	}
	public String getName(){
		return name;
	}
	public int getLength(){
		return length;
	}
	public String getMessage(){
		return null;
	}
	public byte[] getBytes(){
		return bFile;
	}
	public void show(){
		for(byte i:bFile){
			System.out.print((char) i);
		}
	}
}

class TextData implements DataToSend, Serializable{
	private String message;
	private String name;
	public static final long serialVersionUID = 891427;
	
	TextData(String name, String message){
		this.name = name;
		this.message = message;
	}
	public String getMessage(){
		return message;
	}
	public String getName(){
		return name;
	}
	public String toString(){
		return "Name : " + getName() + " Message : " + getMessage();
	}
}


class FileDoesNotExistException extends Exception{
	String msg;
	FileDoesNotExistException(String msg){
		this.msg = msg;
	}
	public String getMessage(){
		return msg;
	} 
}
class InvalidCommandInRequest extends Exception{
	String msg;
	InvalidCommandInRequest(String msg){
		this.msg = msg;
	}
	public String getMessage(){
		return msg;
	} 

}
class Utility{
	public static byte[] convertFileTOByte(String path) throws FileDoesNotExistException, FileNotFoundException {
		File file = new File(path);
		if(file.exists()){
			FileInputStream fileInputStream = null;
			byte[] bFile = new byte[(int) file.length()];
			try
			{
				fileInputStream = new FileInputStream(file);
				fileInputStream.read(bFile);
				fileInputStream.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return bFile;	
		}
		else{
			throw new FileDoesNotExistException("File Doest not Exist");
		}
	}
	public static void convertByteToFile(String path, byte[] bFile){
		try { 
			OutputStream os= new FileOutputStream(path); 
			os.write(bFile); 
			os.close(); 
		} 
		catch (Exception e) { 
			System.out.println("Exception: " + e); 
		} 
	}
}

class Request implements Serializable{
	private String typeOfRequest;
	private String typeOfCommand;
	private ArrayList<DataToSend> command;
	public static final long serialVersionUID = 761427;
	
	Request(String typeOfRequest, String typeOfCommand, ArrayList<DataToSend> command) throws InvalidCommandInRequest {
		this.typeOfRequest = typeOfRequest;
		this.typeOfCommand = typeOfCommand;
		if(command.size() != 2){
			throw new InvalidCommandInRequest("Invalid request object creation."); 		
		}else{
			this.command = command;
		}
	}
	public String getTypeOfRequest(){
		return typeOfRequest;
	}
	public String getTypeOfCommand(){
		return typeOfCommand;
	}
	public FileData getData(){
		if(command.get(1) != null){
			return (FileData) command.get(1);
		}
		return null;
	}
	public String getHeader(){
		Header h = (Header) command.get(0);
		return h.getMessage();
	}
	public String getCommandName(){
		TextData data = (TextData) command.get(1);	// It name of command not a command 
		return data.getName();
	}
	
	public String getCommand(){
		if(getHeader().equals("t")){
			TextData data = (TextData) command.get(1);	// It Get checked in get command
			return data.getMessage();
		}
		FileData data = (FileData) command.get(1);	// It Get checked in get command
		return data.getName();
	}
}
class Responce implements Serializable {
	private String status = "";
	private String msg = "";
	private ArrayList<DataToSend> toSendData = null;
	public static final long serialVersionUID = 7129821;

	Responce(String status, String msg , ArrayList<DataToSend> data){
		this.status = status;
		if(this.status.equals("pass")){
			this.toSendData = data;
		}
		this.msg = msg;
	}
	public void writeData(){
		if(toSendData != null){
			try{
				Header h = (Header) toSendData.get(0);
				if(h.equals("f")){
					FileData data = (FileData) toSendData.get(1);
					Utility.convertByteToFile(data.getName(), data.getBytes());
					System.out.println("");
				}else if(h.equals("t")){
					TextData data = (TextData) toSendData.get(1);
					System.out.println(data.toString()); 
				}	
			}catch(NullPointerException e){
				System.out.println(e);
			}
		}
	}
	public String toString(){
		return "Status : " + this.status + " Msg : " + this.msg + " File-data : " + toSendData; 
	}
}


class RequestFullFiller{
	private static final List<String> folders = Arrays.asList("audio", "video", "document", "video");
	private static void verifyAllCommands(String folder, String file) throws FileDoesNotExistException{
		if(!folders.contains(folder)){
			throw new FileDoesNotExistException("file Does not exist");
		}	
		if(!Arrays.asList(new File(System.getProperty("user.home")+ "/" + "ShareAble/" + folder).list()).contains(file)){
			throw new FileDoesNotExistException("file Does not exist");
		}
	}
	public static Responce processRequest(Request r){
		ArrayList<DataToSend> send = null;
		if(r.getTypeOfRequest().equals("get") && !r.getTypeOfCommand().isEmpty() && r.getTypeOfCommand().equals("manual")){

			send = new ArrayList<DataToSend>();
			send.add(new Header("t"));
			send.add(new TextData("Manual",ServerManual.getManual()));
			return new Responce("pass", "Here is Your Manual", send);
			
		}else if(r.getTypeOfRequest().equals("get") && !r.getTypeOfCommand().isEmpty() &&!r.getCommand().isEmpty()){
			FileData file = null;
			String msg = "Success";
			String status = "pass";
			try{
				verifyAllCommands(r.getTypeOfCommand(), r.getCommand());
				file = new FileData(r.getCommand(), System.getProperty("user.home")+ "/" + "ShareAble/" + r.getTypeOfCommand() + "/");
			}catch(FileDoesNotExistException | FileNotFoundException e){
				msg = e.getMessage();
				status = "fail";
			}
			send = new ArrayList<DataToSend>();
			send.add(new Header("f"));
			send.add(file);
			return new Responce(status, msg, send);
			
		}else if(r.getTypeOfRequest().equals("post") && !r.getTypeOfCommand().isEmpty() &&!r.getCommand().isEmpty()){

			System.out.println("Setting The Data / Updating the data");
			
			try{
				Utility.convertByteToFile(System.getProperty("user.home")+ "/" + "ShareAble/"+"FromClientAdded/" + r.getCommand(), r.getData().getBytes());
			}catch(NullPointerException e){
				send = new ArrayList<DataToSend>();
				send.add(new Header("t"));
				send.add(new TextData("Msg","fail"));
				return new Responce("fail", "Invalid Request", send);
			}
			send = new ArrayList<DataToSend>();
			send.add(new Header("t"));
			send.add(new TextData("Msg","Done"));
			return new Responce("pass", "setting / updating data.", send);
		}
		else{
			send = new ArrayList<DataToSend>();
			send.add(new Header("t"));
			send.add(new TextData("Msg","fail"));
			return new Responce("fail", "Invalid Request", send);
		}
	}
}

class ClientHandler extends Thread {
	protected Socket socket;
	public ClientHandler(Socket clientSocket) {
		System.out.println("New Client connected");
		this.socket = clientSocket;
		try{
			OutputStream outToClient = socket.getOutputStream();
			DataOutputStream out = new DataOutputStream(outToClient);
			out.writeUTF(ServerManual.getManual());
		}catch(IOException e){
			System.out.println(e);
		}
		
	}
	public void sendData(Responce res){
		try{
			OutputStream outToClient = socket.getOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(outToClient);
			out.writeObject(res);
			
		}catch(IOException e){
			System.out.println(e);
		}
	}
	public void run() {
		try {
			while(true){
				InputStream inputStream = socket.getInputStream();
				ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
				Request req = (Request) objectInputStream.readObject();
				sendData(RequestFullFiller.processRequest(req));
			}
			//socket.close();
		} catch (IOException | ClassNotFoundException e) {
			return;
		}
	}
}
public class server {
	static final int PORT = 1978;
	public static void main(String args[]) {
		ServerSocket serverSocket = null;
		Socket socket = null;

		try {
			serverSocket = new ServerSocket(PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		while (true) {
			try {
				socket = serverSocket.accept();
			} catch (IOException e) {
				System.out.println("I/O error: " + e);
			}
			new ClientHandler(socket).start();
		}
	}
}

