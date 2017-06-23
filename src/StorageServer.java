package trabalho1;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;


/**
 * Created by iamfuzzeh on 10/18/16.
 * StorageServer. Manages a file system, receives client requests and propagates changes to MetaServer
 */
public class StorageServer implements StorageInterface {
    static private final int COLUMNTYPE = 0;
    static private final int COLUMNNAME = 8;
    static private final String FILE = "file";
    static private final String FOLDER = "folder";
    static private final String SERVERNAME = "SDtrabalho1.server_teste.com";
    static private String localpath = "";
    static private String systempath = "";
    static private Tree metatree;
    static private Registry registry;

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            close();
                        } catch (RemoteException | NotBoundException e) {
                            e.printStackTrace();
                        }
                    }
                }, "Shutdown-thread"));

        try {
            registry = getRegistry();
            Scanner in = new Scanner(System.in);
            System.out.println("localpath?");
            localpath = in.nextLine();
            if(localpath.isEmpty()){
                //default
                URL url = StorageServer.class.getResource("/trabalho1");
                localpath = url.getPath() + "/" + "serverfolder/";
            }
            System.out.println("systempath?");
            systempath=in.nextLine();
            if(systempath.isEmpty()){
                systempath = "server_teste";
            }

            init();

        } catch (Exception e) {
            System.out.println("exception main:" + e.getMessage());
        }

    }

    private static void init() throws RemoteException, AlreadyBoundException, NotBoundException {

        System.out.println("StorageServer initiating, please init(local_path,system_path)");

        if (!validateDir()) {
            runCommand(new String[]{"mkdir", systempath}, localpath);
        }
        metatree = new Tree(systempath);
        fillDirTree(localpath + systempath, "");
        addStorageServer(SERVERNAME, metatree);
    }

    private static void close() throws RemoteException, NotBoundException {
        delStorageServer(SERVERNAME);
    }

    private static Registry getRegistry() throws RemoteException {
        return LocateRegistry.getRegistry();
    }

    private static void addStorageServer(String servername, Tree topofsubtree) throws RemoteException, AlreadyBoundException, NotBoundException {
        StorageServer storageServer = new StorageServer();
        StorageInterface stub = (StorageInterface) UnicastRemoteObject.exportObject(storageServer, 0);
        registry.bind(servername, stub);

        System.out.println("Trying to set up: " + servername);

        MetaInterface metaconnection = (MetaInterface) registry.lookup("metaserver");
        String response = metaconnection.addStorageServer(servername, topofsubtree);

        System.out.println("response:" + response);
    }

    private static void delStorageServer(String servername) throws RemoteException, NotBoundException {
        MetaInterface metaconnection = (MetaInterface) registry.lookup("metaserver");
        String response = metaconnection.delStorageServer(servername);
        registry.unbind(servername);

        System.out.println("response:" + response);
    }

    private static void addStorageItem(String path, String type) throws RemoteException, NotBoundException {
        MetaInterface metaconnection = (MetaInterface) registry.lookup("metaserver");
        String response = metaconnection.addStorageItem(path, type);

        System.out.println("response:" + response);
    }

    private static void delStorageItem(String path) throws RemoteException, NotBoundException {
        MetaInterface metaconnection = (MetaInterface) registry.lookup("metaserver");
        String response = metaconnection.delStorageItem(path);

        System.out.println("response:" + response);
    }

    private static boolean validateDir() {
        String servers = runCommand(new String[]{"ls"}, localpath);
        String parsed[] = servers.split(" ");

        for (String str : parsed) {
            if (str.equals(systempath)) {
                return true;
            }
        }
        return false;
    }

    private static void fillDirTree(String prefix, String path) {
        String content = runCommand(new String[]{"ls", "-l"}, prefix + "/" + path);
        String lines[] = content.split("\\r?\\n");
        for (String str : lines) {
            String[] columns = str.split("\\s+");
            if (!columns[0].equals("total")) {
                if (columns[COLUMNTYPE].charAt(0) == 'd') {
                    fillDirTreeFolder(columns[COLUMNNAME], path, prefix);
                } else {
                    fillDirTreeFile(columns[COLUMNNAME], path);
                }
            }
        }
    }

    private static void fillDirTreeFolder(String name, String path, String prefix) {
        if (path.equals("")) {
            metatree.add(name, FOLDER);
            fillDirTree(prefix, name);
        } else {
            metatree.add(path + "/" + name, FOLDER);
            fillDirTree(prefix, path + "/" + name);
        }
    }

    private static void fillDirTreeFile(String name, String path) {
        if (path.equals("")) {
            metatree.add(name, FILE);
        } else {
            metatree.add(path + "/" + name, FILE);
        }
    }

    private static String runCommand(String[] cmd, String path) {
        String result = "";

        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.directory(new File(path));
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }
            result = builder.toString();
            return result;
        } catch (Exception e) {
            System.out.println("exception runCommand:" + e.getMessage());
        }
        return result;
    }

    //Client commands
    public String create(String path) throws RemoteException {
        String response = "";
        String abspath = localpath + path;

        System.out.println("abspath: " + abspath);

        try {
            //check if folder/file exists
            System.out.println("checking if file/folder exists");
            if (fileExists(abspath)) {
                return (path + " already exists.");
            }

            System.out.println("doesnt exist, checking extension");
            //if not
            //check if folder or file, via .extension
            //folder
            if (getExtension(path).equals("")) {
                System.out.println("folder, trying to create");
                //create
                File dir = new File(abspath);
                if(!dir.mkdir()){
                    System.out.println("problem creating dir");
                }
                //add to tree
                System.out.println("adding to metatree");
                metatree.add(path, FOLDER);

                metatree.printTree();

                //sync tree
                System.out.println("attempting to sync tree.");
                addStorageItem(path, FOLDER);
            }
            //file
            else {
                System.out.println("file, trying to create");
                //create
                File file = new File(abspath);
                if(file.createNewFile()){
                    System.out.println("file already existed");
                }
                //add to tree
                System.out.println("adding to metatree");
                metatree.add(path, FILE);

                metatree.printTree();

                //sync tree
                System.out.println("attempting to sync tree.");
                addStorageItem(path, FILE);
            }

        } catch (NotBoundException | IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public String create(String path, String blob) throws RemoteException {
        String abspath = localpath + path;

        try {
            //check if file exists
            System.out.println("checking if file/folder exists");
            if (fileExists(abspath)) {
                return (path + " already exists.");
            }
            if (getExtension(path).equals("")) {
                return "missing extension on file";
            }
            //file
            else {
                System.out.println("trying to create: " + path);
                //create
                File file = new File(abspath);
                file.createNewFile();
                List<String> content = Arrays.asList(blob);
                Path p = Paths.get(file.getPath());
                Files.write(p, content, Charset.forName("UTF-8"));
                //add to tree
                System.out.println("adding to metatree");
                metatree.add(path, FILE);
                //sync tree
                System.out.println("attempting to sync tree.");
                addStorageItem(path, FILE);
            }
        } catch (IOException | NotBoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String del(String path) throws RemoteException {
        String abspath = localpath + "/" + systempath + "/" + path;

        try {
            //check if file/folder exists
            System.out.println("checking if file/folder exists");
            if (!fileExists(abspath)) {
                return (path + " doesn't exist.");
            }

            File file = new File(abspath);

            //remove locally
            if (isFile(abspath)) {
                if(!file.delete()){
                    System.out.println("problem deleting file");
                }
            } else {
                clearAndRemoveFolder(file);
            }

            //remove from tree
            System.out.println("removing: " + systempath + "/" + path);
            metatree.del(path);
            System.out.println("tree after del attempt:");
            metatree.printTree();

            //sync tree
            System.out.println("attempting to sync tree.");

            //syncTree(SERVERNAME, metatree);
            delStorageItem(path);

        } catch (NotBoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String mv(String file1path, String file2path) {
        String abspathfile1 = localpath + file1path;
        String abspathfile2 = localpath + file2path;
        File file1;
        File file2;
        try {
            System.out.println("checking if file/folder exists");
            file1 = new File(abspathfile1);
            file2 = new File(abspathfile2);

            if (!fileExists(abspathfile1)) {
                return (file1path + " doesn't exist.");
            }
            if (!fileExists(abspathfile2)) {
                if(!file2.createNewFile()){
                    System.out.println("problem creating file");
                }
                metatree.add(file2path, FILE);
                addStorageItem(file2path, FILE);
            }

            Path pathfile1 = Paths.get(file1.getPath());
            Path pathfile2 = Paths.get(file2.getPath());

            Files.copy(pathfile1, pathfile2, REPLACE_EXISTING);

        } catch (IOException | NotBoundException e) {
            e.printStackTrace();
        }

        return "";
    }

    public byte[] get(String path) {
        String abspath = localpath + path;

        try {
            //check if file exists and isn't a folder
            if (fileExists(abspath) && isFile(abspath)) {
                File file = new File(abspath);
                Path pathfile1 = Paths.get(file.getPath());
                return Files.readAllBytes(pathfile1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean fileExists(String path) {
        File varTmpDir = new File(path);
        return varTmpDir.exists();
    }

    private boolean isFile(String path) {
        File varTmpDir = new File(path);
        return varTmpDir.isFile();
    }

    private void clearAndRemoveFolder(File folder) {
        String[] files = folder.list();
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
        if(!folder.delete()){
            System.out.println("problem deleting folder");
        }
    }

    private String getExtension(String path) {
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
}
