# Chandy-Lamport

A program that simulates the Chandy-Lamport Protocol

Uses a config file to create a network of concurrent nodes that would represent processes in an operating system. 

The nodes are tasked to send a certain amount of messages to eachother through socket programming.
Every defined period, the program attempts to take a snapshot of the system. If the Chandy-Lamport requirements for a snapshot are met, the program takes a snapshot of the current system state (State of each node, how many messages were recieved and sent by each node, clock time)
