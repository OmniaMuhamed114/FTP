package FTP;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
/**
 * The MultipleClients class extends thread class and creates another server socket to send files.
 */
public class MultipleClients  extends Thread {
    private static Socket clientSocket;
    private static Socket fileSocket;
    ServerSocket serverSocket;
    private static DataInputStream fileReadStream;
    private static DataOutputStream fileWriteStream;
    String username = "";
    String password = "";
    /**
     * This is a parameterize constructor of the class.
     * @param socket This is the client socket which the server accepts.
     */
    public MultipleClients(Socket socket) {
        clientSocket = socket;
    }
    /**
     * This method reads the file of clients information to check the username and the password.
     * then it replies to the client to enter the username.
     * then it read the username from client and checks if it in the file or no.
     * If the username was wrong it will terminate the connection.
     * If the username was right, it will ask the client to enter the password.
     * Then if reads the password from the client and checks if it the password of the username or no.
     * If the password was wrong it will terminate the connection.
     * If the password was right if replies to the client that login successfully and open ner socket to send files.
     */
    public void run(){
        try{
            DataInputStream serverReadStream = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream serverWriteStream = new DataOutputStream(clientSocket.getOutputStream());
            String usernameFromFile;
            String passwordFromFile;
            boolean findUserName = false;
            File usersData = new File("C:\\Users\\omnia mohamed\\IdeaProjects\\network project\\users.txt");
            FileInputStream fileInput = new FileInputStream(usersData);
            Scanner fromFile = new Scanner(fileInput);
            serverWriteStream.writeUTF("Server: Please enter your username: ");
            username = serverReadStream.readUTF();
            while(fromFile.hasNext()){
                usernameFromFile = fromFile.nextLine();
                passwordFromFile = fromFile.nextLine();
                if(username.equalsIgnoreCase(usernameFromFile)){
                    findUserName = true;
                    serverWriteStream.writeBoolean(true);
                    serverWriteStream.writeUTF("Server: Please enter your password: ");
                    password = serverReadStream.readUTF();
                    if(password.equalsIgnoreCase(passwordFromFile)){
                        serverWriteStream.writeBoolean(true);
                        serverWriteStream.writeUTF("Server: Login Successfully.");
                        String anotherOperation;
                        do{
                            openFileSocket();
                            serverWriteStream.writeUTF("Server: do you want to do another operation?(yes/no)");
                            anotherOperation = serverReadStream.readUTF();
                        }while("yes".equalsIgnoreCase(anotherOperation));
                    }else{
                        serverWriteStream.writeBoolean(false);
                        serverWriteStream.writeUTF("wrong password, Login Failed and the connection will terminate..");
                        System.out.println("Server: Connection with this client [" + clientSocket + "] is terminated.");
                        clientSocket.close();
                    }
                }
            }
            if(!findUserName){
                serverWriteStream.writeBoolean(false);
                serverWriteStream.writeUTF("Server: wrong username, Login Failed and the connection will terminate");
                System.out.println("Server: Connection with this client [" + clientSocket + "] is terminated.");
                clientSocket.close();
            }
        }catch(IOException e){
            System.out.println("Server: Connection with this client [" + clientSocket + "] is terminated.");
            e.printStackTrace();
        }
    }
    /**
     * This method accepts the new socket to send the files by it.
     * It sends to the client to enter the command.
     * If the client reply with show my directories it will open file of clients paths and get its path and open it.
     * It reply with names of files in this path.
     * If the client replied with close it will close the connection between the client and server.
     * If the client replied with anything else the server will reply with this is wrong and asks it to enter the command again.
     */
    private void openFileSocket() throws IOException{
        serverSocket = new ServerSocket(5000);
        fileSocket = serverSocket.accept();
        System.out.println("the second TCP connection [" + fileSocket + "] is connected to the server.");
        fileReadStream = new DataInputStream(fileSocket.getInputStream());
        fileWriteStream = new DataOutputStream(fileSocket.getOutputStream());
        fileWriteStream.writeUTF("Server: enters the command (show my directories)");
        String command;
        do{
            command = fileReadStream.readUTF();
            if(command.equalsIgnoreCase("show my directories")){
                File clients = new File("C:\\Users\\omnia mohamed\\IdeaProjects\\network project\\clients\\clients paths.txt");
                Scanner myReader = new Scanner(clients);
                while (myReader.hasNextLine()){
                    String data = myReader.nextLine();
                    if (data.contains(username)){
                        showClientFiles(data);
                    }
                }
                myReader.close();
                closeFileSocket();
            }else {
                fileWriteStream.writeUTF("Server: invalid command, please enters the command “show my directories“");
            }
        }while(!command.equalsIgnoreCase("show my directories"));
    }
    /**
     * This method close two connections of client when the user enter close.
     * @throws IOException This is an exception which will be thrown if something wrong happened when the connection was terminated.
     */
    private void closeFileSocket() throws IOException{
        fileReadStream.close();
        fileWriteStream.close();
        fileSocket.close();
        serverSocket.close();
        System.out.println("Server: Connection with this client [" + fileSocket + "] is terminated.");
    }
    /**
     * This method takes the path of this client and open it and return the names of files which in it to client to choose from them.
     * It asks client to enter the name of the file he wants.
     * Then it checks if the name which the client reply with is true or false, if the client reply with wrong name the server will asks him to re-enter it.
     * If this selected file was file the server will download it.
     * If this selected file was directory the server will reply with the names of files in it.
     * @throws IOException This is an exception which will be thrown if something wrong happened.
     * @param path This is the path of the client.
     */
    private void showClientFiles(String path) throws IOException{
        File dirs = new File(path);
        File[] allSubFiles = dirs.listFiles();
        fileWriteStream.writeUTF("Server: your files are:");
        assert allSubFiles != null;
        int numberOfDirs = allSubFiles.length;
        fileWriteStream.writeInt(numberOfDirs);
        for(File folder : allSubFiles){
            fileWriteStream.writeUTF(folder.getName());
        }
        fileWriteStream.writeUTF("Server: enter the name of the file you want:");
        String selectedFileName;
        boolean rightName = true;
        do{
            selectedFileName = fileReadStream.readUTF();
            for(File folder : allSubFiles){
                if(folder.getName().equalsIgnoreCase(selectedFileName)){
                    rightName = true;
                    break;
                } else
                    rightName = false;
            }
            fileWriteStream.writeBoolean(rightName);
            if(!rightName)
                fileWriteStream.writeUTF("Server: file name isn't found, please enter right name: ");
        }while(!rightName);
        File selectedFile = null;
        for(File folder : allSubFiles){
            if (folder.getName().equalsIgnoreCase(selectedFileName))
                selectedFile = new File(folder.getAbsolutePath());
        }
        assert selectedFile != null;
        if(selectedFile.isDirectory()){
            showClientFiles(selectedFile.getAbsolutePath());
        }else{
            sendFile(selectedFile.getAbsolutePath());
        }
    }
    /**
     * This method sends the file which was chosen by the user to the client by dividing it into bytes.
     * @param selectedFilePath This is the path of the file which the user selected.
     * @throws IOException This is an exception which will be thrown if something wrong happened when the server was sending the file.
     */
    private static void sendFile(String selectedFilePath) throws IOException{
        File myFile = new File (selectedFilePath);
        int count, totalCount = 0;
        byte[] buffer = new byte[1024];
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(myFile));
        while ((count = in.read(buffer)) >= 0) {
            fileWriteStream.write(buffer, 0, count);
            fileWriteStream.flush();
            totalCount += count;
            System.out.println("Sending " + selectedFilePath + "(" + totalCount + " bytes)");
        }
        System.out.println("Done.");
    }
}