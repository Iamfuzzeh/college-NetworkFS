package trabalho1;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by iamfuzzeh on 10/13/16.
 * Client. Sends requests to MetaServer and StorageServer
 */
public class Client {

    static private String currentServer = "";
    static private String currentDir = "";
    static private String clientFolder="";
    static private StorageInterface serverconnection = null;
    static private Map<String, String> namespace = new HashMap<String, String>();


    private Client() {
    }

    public static void main(String args[]) {
        Runtime.getRuntime().addShutdownHook(
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, "Shutdown-thread"));

        Scanner in = new Scanner(System.in);
        String host = (args.length < 1) ? null : args[0];

        try {
            Registry registry = LocateRegistry.getRegistry(host);
            MetaInterface metaconnection = (MetaInterface) registry.lookup("metaserver");
            String response = null;
            String command;
            String[] input;

            input = reader(in.nextLine());
            command = input[0];

            while (command != null) {

                switch (command) {
                    case "find":
                        response = find(registry, metaconnection, input[1]);
                        break;
                    case "lstat":
                        response = lstat(registry, metaconnection, input[1]);
                        break;
                    case "init":
                        init(input);
                        break;
                    case "pwd":
                        System.out.println(currentDir);
                        break;
                    case "ls":
                        response = ls(metaconnection, response, input);
                        break;
                    case "cd":
                        response = cd(metaconnection, input[1]);
                        break;
                    case "mv":
                        response = mv(input);
                        break;
                    case "open":
                        open(input[1]);
                        break;
                    case "create":
                        response = create(input);
                        break;
                    case "del":
                        response = serverconnection.del(input[1]);
                        break;
                    case "get":
                        response = getString(response, input[1]);
                        break;
                    case "close":
                        close();
                        return;
                    case "help":
                        System.out.println("Always available commands:\n" +
                                "find\n" +
                                "lstat\n" +
                                "after connection estabilished:\n" +
                                "init\n" +
                                "pwd\n" +
                                "ls\n" +
                                "cd\n" +
                                "mv\n" +
                                "open\n" +
                                "del\n" +
                                "get\n" +
                                "help\n" +
                                "close");
                        break;
                }
                if (response != null && !response.equals("")) {
                    System.out.println(response);
                    response= "";
                }
                input = reader(in.nextLine());
                command = input[0];
            }

        } catch (RemoteException | NotBoundException e1) {
            e1.printStackTrace();
        }

    }

    private static String getString(String response, String path) {
        try{
            byte[] filebytes;
            if(isFullPath(path)){
                filebytes = serverconnection.get(path);
            }
            else{
                filebytes = serverconnection.get(currentDir + "/" + path);
            }
            if(filebytes == null){
                response = "no such file exists";
            }
            else{
                Path getpath = Paths.get(clientFolder + "/" + path);
                Files.createDirectories(getpath.getParent());
                Files.createFile(getpath);
                FileOutputStream fos = new FileOutputStream(clientFolder + "/" + path);
                fos.write(filebytes);
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    private static void init(String[] input) {
        //init

        if(input[1].equals("/")){
            URL url = Client.class.getResource("/trabalho1");
            clientFolder = url.getPath() + "/" + "clientfolder";
        }else{
            clientFolder = input[1];
        }
        //criar pasta
        File dir = new File(clientFolder);
        if(!dir.mkdir()){
            System.out.println("problem creating folder");
        }

        namespace.put(input[1], input[2]);
    }

    private static void close(){

        File file = new File(clientFolder);
        clearAndRemoveFolder(file);

    }

    private static String mv(String[] input) throws RemoteException {
        String response;
        if (input.length != 3) {
            response = "input error";
        } else {
            String p1;
            String p2;
            if(isFullPath(input[1])){
                p1 = input[1];
            }else{
                p1 = currentDir + "/" + input[1];
            }
            if(isFullPath(input[2])){
                p2 = input[2];
            }else{
                p2 = currentDir + "/" + input[2];
            }
            response = serverconnection.mv(p1, p2);
        }
        return response;
    }

    private static String create(String[] input) throws RemoteException {
        String response = "";
        if (input.length == 2) {
            if(isFullPath(input[1])){
                response = serverconnection.create(input[1]);
            }else{
                response = serverconnection.create(currentDir + "/" + input[1]);
            }
        } else if (input.length == 3) {
            if(isFullPath(input[1])){
                response = serverconnection.create(input[1], input[2]);
            }else{
                response = serverconnection.create(currentDir + "/" + input[1], input[2]);
            }
        }
        return response;
    }

    private static String cd(MetaInterface metaconnection, String path) throws RemoteException {
        String response;

        if(isFullPath(path)){
            response = metaconnection.cd(path);
        }else{
            response = metaconnection.cd(currentDir + "/" + path);
        }
        if (!response.equals("no such dir")) {
            currentDir = response;
            response = "";
        }
        return response;
    }

    private static String lstat(Registry registry, MetaInterface metaconnection, String path) throws RemoteException, NotBoundException {
        String response;
        FindResponse fr;
        if(isFullPath(path)){
            //full path
            fr = metaconnection.lstat(path);
        }else{
            //relative path, add current dir
            fr = metaconnection.lstat(currentDir + "/" + path);
        }
        if (fr != null) {
            currentServer = fr.getCurrentserver();
            currentDir = fr.getCurrentdir();
            System.out.println("establishing connection to : " + currentServer);
            response = fr.getLstat();
            serverconnection = (StorageInterface) registry.lookup(currentServer);
        } else {
            response = ("No such path could be found.");
        }
        return response;
    }

    private static String find(Registry registry, MetaInterface metaconnection, String path) throws RemoteException, NotBoundException {
        FindResponse fr;
        String response= "";
        if(isFullPath(path)){
            //full path
            fr = metaconnection.find(path);
        }else{
            //relative path, add current dir
            fr = metaconnection.find(currentDir + "/" + path);
        }
        if (fr != null) {
            currentServer = fr.getCurrentserver();
            currentDir = fr.getCurrentdir();
            System.out.println("establishing connection to : " + currentServer);
            serverconnection = (StorageInterface) registry.lookup(currentServer);
        } else {
            response = ("No such path could be found.");
        }
        return response;
    }

    private static String ls(MetaInterface metaconnection, String response, String[] input) throws RemoteException {
        if(input.length == 1){
            response = metaconnection.ls(currentDir);
        }else if (input.length == 2){
            if(isFullPath(input[1])){
                response = metaconnection.ls(input[1]);
            }
            else{
                response = metaconnection.ls(currentDir + "/" + input[1]);
            }
        }
        return response;
    }


    private static String[] reader(String line) {
        return line.split(" ");
    }

    private static boolean isFullPath(String path){
        if(path.charAt(0) == '/'){
            //full path
            return true;
        }
        //relative path, add current dir
        return false;
    }

    private static void open(String path){
        String ext = getExtension(path);
        if(ext.equals("")){
            System.out.println("no extension");
            return;
        }
        String app = getApp(ext);

        try {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(app +" "+ clientFolder + "/" + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getApp(String ext){
        try{
            InputStream is= Client.class.getResourceAsStream("/trabalho1/apps.conf");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while((line = br.readLine()) != null) {
                if(line.charAt(0) != '#'){

                    int firstslash = firstSlash(line);
                    String[] leftnright = new String[2];
                    leftnright[0] = line.substring(0, firstslash);
                    leftnright[1] = line.substring(firstslash, line.length());

                    leftnright[0] = leftnright[0].replace(" ", "");
                    leftnright[1] = leftnright[1].replace(" ", "");

                    String[] extensions = leftnright[0].split(",");

                    for(String e : extensions){
                        if(ext.equals(e)){
                            System.out.println(leftnright[1]);
                            return leftnright[1];
                        }
                    }
                }
            }
            return "";
        }catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String getExtension(String path) {
        String parse[] = path.split("/");
        String last = parse[parse.length - 1];
        if (last.contains(".")) {
            parse = last.split("\\.");
            last = parse[parse.length - 1];
            return last;
        } else {
            return "";
        }
    }

    private static int firstSlash(String path){
        for(int i=0;i<path.length()-1;i++){
            if(path.charAt(i) == '/'){
                return i;
            }
        }
        return -1;
    }

    private static void clearAndRemoveFolder(File folder) {
        String[] files = folder.list();
        if (files != null){
            for (String f : files) {
                File currentfile = new File(folder.getPath(), f);
                if (currentfile.isDirectory()) {
                    clearAndRemoveFolder(currentfile);
                } else {
                    if(!currentfile.delete()){
                        System.out.println("problem deleting file");
                    }
                }
            }
        }
        if(!folder.delete()){
            System.out.println("problem deleting folder");
        }
    }
}
