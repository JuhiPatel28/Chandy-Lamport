#!/bin/bash


# Change this to your netid
netid=jmp170130

#
# Root directory of your project
PROJDIR=$HOME/Project1

#
# Directory where the config file is located on your local system
CONFIGLOCAL=$PROJDIR/hi.txt

n=0

cat $CONFIGLOCAL | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
    read i
    echo $i
    totalNodes=$( echo $i | awk '{ print $1 }' )
	
    for ((a=1; a <= $totalNodes ; a++))
    do
		read line
		echo $line
        host=$( echo $line | awk '{ print $2 }' ) 
		suffixHost='.utdallas.edu'
		host=$host$suffixHost
		echo $host
		ssh $netid@$host killall -u $netid &		
		#echo $n
        sleep 1
        n=$(( n + 1 ))
    done
   
)


echo "Cleanup complete"
