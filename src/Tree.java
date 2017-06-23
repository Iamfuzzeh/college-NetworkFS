package trabalho1;

import java.io.Serializable;
import java.util.*;

/**
 * Created by iamfuzzeh on 10/18/16.
 * Tree, created as a way of storing metadata about storageservers
 */
public class Tree implements Serializable {

    private static final String FILE = "file";
    private static final String FOLDER = "folder";
    private  Node root = null;

    private class Node implements Serializable {
        String name;
        String type;

        List<Node> children;

        Node(String name, String type) {
            this.name = name;
            this.children = new ArrayList<>();
            this.type = type;
        }
    }

    public Tree(String name) {
        root = new Node(name, FOLDER);
    }

    public void add(String path, String type) {

        String[] parse = path.split("/");
        String name = parse[parse.length - 1];
        Node node;

        if (parse.length < 2) {
            node = followPath(path);
            if (node == null) {
                Node e = new Node(name, type);
                root.children.add(e);
            } else {
                System.out.println("couldn't add");
            }
        } else {

            String lastinparse = parse[parse.length - 1];
            path = path.substring(0, (path.length() - lastinparse.length() - 1));
            node = followPath(path);

            if (node == null) {
                System.out.println("couldn't add, might not exist folder in which to do so.");
            } else {
                if (node.name.equals(name)) {
                    System.out.println("Couldn't add '" + name + "', name already in use");
                    return;
                }
                if (node.type.equals(FILE)) {
                    System.out.println("Couldn't add '" + name + "', trying to add inside another file");
                    return;
                }
                Node e = new Node(name, type);
                node.children.add(e);
            }
        }

    }

    public void del(String path) {

        String[] parse = path.split("/");
        if (root.name.equals(parse[0])) {
            if (parse.length == 1) {
                root = null;
            } else {
                String[] newparse = Arrays.copyOfRange(parse, 1, parse.length);
                del(newparse, 0, root);
            }
        } else {
            del(parse, 0, root);
        }
    }

    boolean find(String path) {
        return (followPath(path) != null);
    }

    String ls(String path) {
        return (printNode(followPath(path)));
    }

    public void printTree() {
        printTree(root, 1);
    }

    private Node del(String[] path, int index, Node node) {
        if (node.children == null || index > path.length - 1) {
            System.out.println("return on 1");
            return node;
        }
        Node e = compareChildren(path[index], node);
        if (e != null) {
            if (index == path.length - 1) {
                System.out.println("removing: " + e.name);
                node.children.remove(e);
            } else {
                del(path, index + 1, e);
            }
        } else {
            System.out.println("e is null");
        }
        System.out.println("return on end");
        return node;
    }

    private Node followPath(String path) {

        if (path.charAt(0) == '/') {
            path = path.substring(1, path.length());
        }

        String[] parse = path.split("/");
        Node node = root;
        int i = 0;

        while (i < parse.length && (node = compareNodeAndChildren(parse[i], node)) != null) {
            i++;
        }
        return node;
    }

    private Node compareNodeAndChildren(String name, Node node) {

        if (node.name.equals(name)) {
            return node;
        }
        return compareChildren(name, node);
    }

    private Node compareChildren(String name, Node node) {

        for (Node child : node.children) {
            if (child.name.equals(name)) {
                return child;
            }
        }
        return null;
    }

    private void printTree(Node node, int i) {
        Queue<Node> mainqueue = new LinkedList<>();
        Queue<Node> folderqueue = new LinkedList<>();

        for (Node n : node.children) {
            if (n.type.equals(FILE)) {
                mainqueue.add(n);
            }
            if (n.type.equals(FOLDER)) {
                folderqueue.add(n);
            }
        }

        for (Node n : mainqueue) {
            System.out.println(generateString('-', i) + " " + n.name);
        }

        for (Node n : folderqueue) {
            System.out.println(generateString('+', i) + " " + n.name);
            printTree(n, i + 2);
        }
    }

    String printNode(Node node) {
        String response = "";
        for (Node n : node.children) {
            response += n.name + ", ";
            //System.out.println(n.name);
        }
        if (!response.equals("")) {
            response = response.substring(0, response.length() - 2);
        }
        return response;
    }

    private String generateString(char c, int i) {
        char[] chars = new char[i];
        Arrays.fill(chars, c);
        return new String(chars);
    }
}
