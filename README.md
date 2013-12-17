cnpa_three
==========

Computer networks PA3, to be done by 12/15/2013

Marcellin Nshimiyimana
Programming Assignement 3
Extended to 12/15/13

Implementing A routing protocol using the Distributed Bellman-Ford algorithm.
My program may present some race condition, but it works as described in the homeworks

Features:

Stable distributed routing table with a network of size 5 with few of the clients on same machine. 
  -
Use of SHOWRT. 30
  - works!
Use of LINKDOWN command on one or more clients to show its correct effect 15
  - works
Use of LINKUP command on one or more terminals to show its correct effect 15
  - works
Use of CLOSE command and show its correct effect 15
  - works!
  
To run the clients.

compile with make and then 
type
java BFclient localport timeout [ipaddress1 port1 weight1 ...]	

ex: java BFclient 20000 5 160.39.193.188 20002 2 160.39.193.188 20003 7






