package trabalho1;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by iamfuzzeh on 10/13/16.
 * MetaServer. Keeps meta information about StorageServes, manages most client requests unless file operations are needed.
 */
public class MetaServer implements MetaInterface {

    private Map<String, Tree> serverBase = new HashMap<>();

    public String addStorageServer(String str, Tree tree){
        tree.printTree();
        serverBase.put(str, tree);
        System.out.println("Storage up: " + str);
        return (str + " has been set up");
    }

    public String delStorageServer(String topofsubtree){
        Map.Entry<String, Tree> entry;
        if( (entry = mapSearch(topofsubtree)) != null){
            serverBase.remove(entry.getKey());
            System.out.println("Storage removed: " + topofsubtree);
            return (topofsubtree + " has been removed.");
        }

        System.out.println("Storage removal attempt failed, no such storage: " + topofsubtree);
        return "No such server available.";
    }

    public String addStorageItem(String path, String type){

        String servername;
        String[] parse = path.split("/");

        if(path.charAt(0) == '/'){
            servername = parse[1];
        }else{
            servername = parse[0];
        }

        Map.Entry<String, Tree> entry;
        if( (entry = mapSearch(servername)) != null){
            Tree t =  entry.getValue();
            t.add(path, type);
            serverBase.put(servername, t);

            t.printTree();
            return path + " successfully created";
        }
        return "error creating " + path;
    }

    public String delStorageItem(String path){

        Map.Entry<String, Tree> entry;
        if( (entry = mapSearch(path)) != null){

            String[] parse = path.split("/");
            String servername = parse[0];

            Tree t = entry.getValue();
            t.del(path);
            serverBase.put(servername, t);

            t.printTree();
            return path + " successfully deleted";
        }

        return path + " not found";
    }

    public FindResponse find(String path){
        Map.Entry<String, Tree> entry;
        if( (entry = mapSearch(path)) != null){
           return ( new FindResponse(entry.getKey(), path, null));
        }
        return null;
    }

    public FindResponse lstat(String path){


        System.out.println("going lstat for path: " + path);

        Map.Entry<String, Tree> entry;
        if( (entry = mapSearch(path)) != null){
            Tree t =  entry.getValue();
            return ( new FindResponse(entry.getKey(), path, t.ls(path) ) ) ;
        }
        return null;
    }

    public String ls(String path){

        System.out.println("ls received path: " + path);

        Map.Entry<String, Tree> entry;
        if( (entry = mapSearch(path)) != null){
            Tree t =  entry.getValue();
            return (t.ls( path) ) ;
        }
        return "no such dir";
    }

    public String cd(String path){

        System.out.println("cd received path: " + path);

        if( mapSearch(path) != null){
            return path;
        }
        return "no such dir";
    }

    private Map.Entry<String, Tree> mapSearch(String name){
        for(Map.Entry<String, Tree> entry : serverBase.entrySet() ) {
            Tree tree = entry.getValue();
            if( tree.find(name)){
                return entry;
            }
        }
        return null;
    }

    public static void main(String[] args){

        try {
            MetaServer metaServer = new MetaServer();
            MetaInterface stub = (MetaInterface) UnicastRemoteObject.exportObject(metaServer, 0);

            Registry registry = LocateRegistry.getRegistry();
            registry.bind("metaserver", stub);

            System.out.println("Metaserver up, waiting...");
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

    }

}
