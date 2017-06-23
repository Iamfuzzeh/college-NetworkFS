package trabalho1;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by iamfuzzeh on 10/13/16.
 * MetaServer RMI Interface
 */
interface MetaInterface extends Remote {

    String addStorageServer(String str, Tree tree) throws RemoteException;

    String delStorageServer(String topofsubtree) throws RemoteException;

    String addStorageItem(String path, String type) throws RemoteException;

    String delStorageItem(String path) throws RemoteException;

    FindResponse find(String path) throws RemoteException;

    FindResponse lstat(String path) throws RemoteException;

    String ls(String path) throws RemoteException;

    String cd(String path) throws RemoteException;
}
