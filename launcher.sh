#!/bin/bash

# Change this to your netid
netid=jmp170130

# Root directory of your project
PROJDIR=$HOME/Project1

# Directory where the config file is located on your local system
CONFIGLOCAL=$PROJDIR/hi.txt

# Directory your java classes are in
#BINDIR=$PROJDIR/bin

# Your main project class
PROG=Main

rm *.class *.out
javac -target 8 -source 8 *.java

n=0

cat $CONFIGLOCAL | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
    read i
    #echo $i
	totalNodes=$( echo $i | awk '{ print $1 }' )
    #echo $netId    
    for ((a=1; a <= $totalNodes ; a++))
    do
		read line 
		#echo $line
		nodeId=$( echo $line | awk '{ print $1 }' )
		host=$( echo $line | awk '{ print $2 }' )
		suffixHost='.utdallas.edu'
		host=$host$suffixHost
		echo $nodeId
		echo $host
		echo $netid
		echo $PROJDIR
		#ssh $netid@$host "cd $PROJDIR" &
		ssh -o StrictHostKeyChecking=no -l "$netid" "$host" "cd $PROJDIR;java $PROG $nodeId $1" &
    done   
)
