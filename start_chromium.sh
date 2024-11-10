#!/bin/bash

DEFAULT_URL="http://localhost:5173/"
URL="${1:-$DEFAULT_URL}" # first argument ($1) is an input if you have your own, if not provided pick up default url as a variable instead (https://localhost:5173)

#kill all insecure chromiums
 ps --columns=4000 -e -o pid,args | grep '[c]hromium.*disable-web-security' | while read line #reads line from the output of previous command, save into line variable
 do
	echo "Detected chromium process: ${line}" #this is the line variable from the while loop
	pid=`echo ${line} | cut -d' ' -f1` #picks up the first column from the line, which would be the pid in this case
	test -z "$pid" || { #test if "pid" is an empty string, if otherwise (||) move onto the 12 and 13
		echo killing $pid
		kill -SIGKILL $pid #kills pid
	}	
done


echo "Starting dev chromium on $URL"
chromium --disable-web-security --user-data-dir=/tmp "$URL" &

echo "Running insecure chromium with ${URL}"
