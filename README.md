# Network File-System

This assignment requires the implementation of a networked filesystem using Java RMI. The
systemmust have exactlythree components, as depicted below:

## Meta-data Server
This server is responsible for all the meta-data information from available storage nodes. This server
must not store any actual files, but instead only information regarding the localization of the avail-
able files across the network file system. It can be seen as a portal, or directory, that offers central
yet dynamical lookups for directories and files.


### Mandatory features:
- All data must be indexed at the meta-data server, i.e., storage servers must propagate any modifications to the storage pool to the meta-data server; and it must properly initialize its local share within meta-data server.
- Offer a lookup service so clients can discover item locations, .i.e., files and directories.
- An API call to list a directory, i.e., all the files inside a directory.

### Suggested application programming interface (API):

Calls from storage server:

+ add_storage_server(host, top_of_the_subtree)
  - Example: addstorageserver(machine1.dcc.fc.up.pt, /courses)
+ del_storage_server(top_of_the_subtree)
  - Example: delstorageserver(/courses)
+ add_storage_item(item)
  - Example: addstorageitem(/courses/video1.avi)
+ del_storage_item(item)
  - Example: delstorageitem(/courses/video1.avi)

Calls from client:

+ find(path)
  - Example: find(/courses) ->machine1.dcc.fc.up.pt
+ lstat(path)
  - Example: lstat(/courses) ->{machine1.dcc.fc.up.pt,{afile.txt, a2file.txt, ...}}

## Storage Server
Holds the actual data, i.e., the actual files and structure. It is responsible for part of the file-system,
so it must enforce the proper management over its sub-tree. So any changes performed locally must
be propagated into the meta-data server. For example, upon receiving a delete request from a client,
the storage server must reflect that action into the meta-data server and calldelstorageitem.

### Mandatory features:

- An API call to allow a client to create files and directories
- A call to delete file and directories
- A call to proper initialize a local share
- A call to download a file

Calls from clients:

+ create(path)
+ create(path, blob)
  - Examples:
  - create(/courses) # creates a directory
  - create(/courses/file1.txt, ”A line in a text”) # creates a file
+ del(path)
  - Examples:
  - del(/courses/file1.txt) # removes file
  - del(/courses) # removes sub-tree
+ get(path)
  - Examples:
  - get(/courses/file1.txt) # downloads the file

On startup:
+ init(local_path, filesystem_path)
  - Example: init(/home/student/courses, /courses)
  - Local dir maps into global namespace. Must call addstorageserver on the metadata server

On close:
+ close(local_path)
  - Example: close(/home/student/courses)
  - Closes Local share. Must call delstorageserver on the metadata server

## Clients
The client issues commands that operate over the filesystem. It must implement the following commands:

```
1.pwd: Prints the current directory
```
```
2.ls: Lists the current directory
```
```
3.cd<dir>: Changes the current directory todir
```
```
4.mv<file1><file2>: Copies filefile1tofile2, overwriting if the latter exists.
```
```
5.open<file>: Opens thefilewith the proper application, accordingly to its extension.
```
The association between applications and file extensions are defined in apps.conf file, with the following format:

#file extension application
jpg, jpeg, png <path_to_gimp> # e.g., /usr/local/bin/gimp
txt, tex, c, java <path_to_emacs> # e.g., /usr/bin/emacs
pdf <path_to_evince> # e.g., /usr/local/bin/evince

Please note that bothdirandfilecan be either a simple name, e.g., figure.jpg, or absolute or relative paths, e.g., ../figures/landscapes/geres.jpg.

### Notes

1. No two storage servers should have the same sub-tree (for regular assignment, please check the
    bonus evaluation for extra information). This would imply replication, and thus consistency
    management.
2. Is left to the students the proper treatment for the root directory (/).

## How to

1. Compile and prepare workspace with: `$ ./compile_loadtest.sh`
1. Run RMI: `$ rmiregistry` use of & is not recommended for logging porpuses
1. Run MetaServer: `$ java trabalho1.MetaServer`
    - MetaServer will wait for communication from StorageServer or Clients
1. Run MetaServer: `$ java trabalho1.StorageServer`
    - Note that two pathes are required, localpath and systempath, both can be skipped and a default is used.
    - localpath default: 'dir_da_class'
    - systempath default: 'server_teste'
1. Run Client: `$ java trabalho1.Client`
    - For a list of commands use `help`
    - We recommend the following:
        ```$ find /server_teste
        $ ls
        $ init / /server_teste (/ is treated as default, which is 'dir_da_class')
        $ get file1.txt
        $ open file1.txt
        ```



