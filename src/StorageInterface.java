package trabalho1;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by iamfuzzeh on 10/13/16.
 * StorageServer Interface
 */
public interface StorageInterface extends Remote {

        String create(String path) throws RemoteException;
        String create(String path, String blob) throws RemoteException;
        String del(String path) throws RemoteException;
        String mv(String path, String path2) throws RemoteException;
        byte[] get(String path) throws RemoteException;
}
