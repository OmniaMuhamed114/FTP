package FTP;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
/**
 * The Server class implements a program that creates a client socket and sends a request to server to connect to him and waits the server to accept.
 */
public class Client {
    private static DataInputStream fileReadStream;
    private static Socket fileSocket;
    /**
     * This method Creates a new client socket and sends a request to server to connect to him and waits the server to accept.
     * When the server accepts the connection request the client will ask the user to enter his username and will send it to the server to check it.
     * If the username was wrong there is an exception will be thrown and the connection will ends.
     * If the username was right the client will ask the user to enter his password and will send it to the server to check it.
     * If the password was wrong there is an exception will be thrown and the connection will ends.
     * If the password was right the client will create another socket to be able to choose multiple directories.
     * If the server accepts this connection, user will be asked to enter his command.
     * If his command was show my directories, the server will reply with the user's directories.
     * Then user replies with a specific directory. If its name is wrong he will be asked to enter it again.
     * If his command was close the second connection will be terminated, if it was anything else it will asks him to re-enter if.
     * @throws IOException This is an exception which will be thrown if someThing wrong happened when the client connects with the server.
     * @param args command-line arguments.
     */
    public static void main(String[] args) throws IOException {
        InetAddress address = InetAddress.getLocalHost();
        Socket clientSocket = new Socket(address, 9000);
        System.out.println("Connecting to the server....");
        DataInputStream clientReadStream = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream clientWriteStream = new DataOutputStream(clientSocket.getOutputStream());
        Scanner input = new Scanner(System.in);
        String username;
        String password;
        boolean isFoundName;
        boolean isFoundPassword = false;
        System.out.println(clientReadStream.readUTF());
        username = input.nextLine();
        clientWriteStream.writeUTF(username);
        isFoundName = clientReadStream.readBoolean();
        System.out.println(clientReadStream.readUTF());
        if(isFoundName) {
            password = input.nextLine();
            clientWriteStream.writeUTF(password);
            isFoundPassword = clientReadStream.readBoolean();
            System.out.println(clientReadStream.readUTF());
        }
        if(isFoundPassword) {
            String anotherOperation = null;
            do{
                fileSocket = new Socket(address, 5000);
                Scanner input2 = new Scanner(System.in);
                fileReadStream = new DataInputStream(fileSocket.getInputStream());
                DataOutputStream fileWriteStream = new DataOutputStream(fileSocket.getOutputStream());
                String command;
                do{
                    System.out.println(fileReadStream.readUTF());
                    command = input2.nextLine();
                    fileWriteStream.writeUTF(command);
                }while(!command.equalsIgnoreCase("show my directories"));
                if(command.equalsIgnoreCase("show my directories")){
                    do{
                        showFiles();
                        String selectedFileName;
                        boolean rightName;
                        do{
                            selectedFileName = input2.nextLine();
                            fileWriteStream.writeUTF(selectedFileName);
                            rightName = fileReadStream.readBoolean();
                            if(!rightName)
                                System.out.println(fileReadStream.readUTF());
                        }while(!rightName);
                        String ext = Utils.getFileExtension(selectedFileName);
                        if(!ext.equals("")){
                            receiveFile(selectedFileName);
                            break;
                        }
                    } while(true);
                }
                System.out.println(clientReadStream.readUTF());
                anotherOperation = input2.nextLine();
                clientWriteStream.writeUTF(anotherOperation);
            } while(anotherOperation.equalsIgnoreCase("yes"));
        }
    }
    /**
     * This method shows the user's directories.
     * @throws IOException This is an exception which will be thrown if someThing wrong happened when the server was replying with the directories names.
     */
    private static void showFiles() throws IOException {
        System.out.println(fileReadStream.readUTF());
        int numberOfDirs = fileReadStream.readInt();
        for(int i = 0; i < numberOfDirs; i++)
            System.out.println("- " + fileReadStream.readUTF());
        System.out.println(fileReadStream.readUTF());
    }
    /**
     *This method receives the file which was sent by server and close the second connection.
     * @throws IOException This is an exception which will be thrown if someThing wrong happened when the server was sending the file.
     */
    private static void receiveFile(String selectedFileName) throws IOException {
        FileOutputStream fos = new FileOutputStream(selectedFileName);
        BufferedOutputStream out = new BufferedOutputStream(fos);
        InputStream is = fileSocket.getInputStream();
        byte[] buffer = new byte[1024];
        int count;
        while ((count = is.read(buffer)) >= 0) {
            fos.write(buffer, 0, count);
        }
        System.out.println("File " + selectedFileName + " downloaded");
        fos.close();
        fileSocket.close();
    }
}