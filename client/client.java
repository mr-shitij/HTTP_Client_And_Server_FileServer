import java.net.*;
import java.io.*;
import java.util.*;

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
			catch (IOException e)
			{
				System.out.println("Invalid Command");
				//e.printStackTrace();
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
					Utility.convertByteToFile(System.getProperty("user.home")+ "/" + "GetShareAbleFromServer/" + data.getName(), data.getBytes());
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


public class client {
	private Socket socket = null;

	private InputStream inFromServer = null;
	private DataInputStream in = null;
	private ObjectInputStream inObj = null;

	private OutputStream toServer = null;
	private ObjectOutputStream out = null;

	public client(String serverName, int port) throws IOException{
		System.out.println("Connecting to " + serverName + " on port " + port);
		socket = new Socket(serverName, port);
		System.out.println("Just connected to " + socket.getRemoteSocketAddress());

	}
	public void sendDataRequestToServer(Request req) throws IOException {
		this.toServer = socket.getOutputStream();
		this.out = new ObjectOutputStream(toServer);
		this.out.writeObject(req);
	}

	public Responce getObjectDataFromServer() throws IOException, ClassNotFoundException {
		this.inFromServer = socket.getInputStream();
		this.inObj = new ObjectInputStream(inFromServer);
		return (Responce) this.inObj.readObject();
	}
	public ArrayList<DataToSend> getDataInArrayListForm(String method, String folder,String fileName) throws FileDoesNotExistException, FileNotFoundException{
		ArrayList<DataToSend> send = null;
		send = new ArrayList<DataToSend>();
		if(method.equals("post")){
			send.add(new Header("f"));
			FileData data = new FileData(fileName, System.getProperty("user.home")+ "/" + "GetShareAbleFromServer/" + folder + "/");
			send.add(data);	
		}else{
			send.add(new Header("t"));
			send.add(new TextData("fileName", fileName));
		}
		return send;
	}
	public Request getRequestFromString(String req) throws InvalidCommandInRequest, FileDoesNotExistException, FileNotFoundException{
		String[] ele = req.split(" ");
		String[] item = {"", "", ""};

		for(int i=0;i<ele.length;i++){
			for(int j=0;j<ele[i].length() - 1; j++){
				if(ele[i].charAt(j) == '\\' && ele[i].charAt(j + 1) != '\\'){
					StringBuilder myStr = new StringBuilder(ele[i]);
					myStr.setCharAt(j, ' ');
					ele[i] = myStr.toString();
				}
			}
			item[i]=ele[i];
		}
		return new Request(item[0], item[1], getDataInArrayListForm(item[0], item[1], item[2]));
	}

	public String getStringDataFromServer() throws IOException {
		this.inFromServer = socket.getInputStream();
		this.in = new DataInputStream(inFromServer);
		return (String) this.in.readUTF();
	}
	public void close() throws IOException{
		socket.close();
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		String serverName = "10.100.108.12";
		int port = 1978;
		try{
			client c = new client(serverName, port);
			System.out.println("Unable To Connect To server.");
			Scanner sc  = new Scanner(System.in);
			String inp = c.getStringDataFromServer();
			System.out.println(inp);


			while(true){
				System.out.print(":> ");
				inp = sc.nextLine();
				try{
					if(inp.equals("exit")){
						c.close();
						System.exit(-1);
					}
					c.sendDataRequestToServer(c.getRequestFromString(inp));
					Responce res = c.getObjectDataFromServer();
					System.out.println(res.toString());
					res.writeData();
				}catch(InvalidCommandInRequest i){
					System.out.println(i.getMessage());
				}catch(FileDoesNotExistException | FileNotFoundException e){
					System.out.println("File Does Not exist");
				}
				catch(Exception e){
					System.out.println("Invalid Command");
				}
			}
		}catch(IOException e){
			System.out.println("Unable to connect to server.");
		}
	}
}


