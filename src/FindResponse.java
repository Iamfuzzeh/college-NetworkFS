package trabalho1;

import java.io.Serializable;

/**
 * Created by iamfuzzeh on 11/10/16.
 * Tipo de resposta, criado como teste de alternativa de comunicacao entre classes
 */
public class FindResponse implements Serializable{

    private String currentserver;
    private String currentdir;
    private String lstat;

    public FindResponse(String systemdir, String currentdir, String lstat) {
        this.currentserver = systemdir;
        this.currentdir = currentdir;
        this.lstat = lstat;
    }

    String getCurrentserver() {
        return currentserver;
    }

    String getCurrentdir() {
        return currentdir;
    }

    String getLstat() {
        return lstat;
    }
}
