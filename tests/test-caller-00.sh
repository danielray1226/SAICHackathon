#!/bin/bash


REQUEST=`cat <<EOF
		{
			"path" : "/",
		}
EOF
`

echo "submitting : ${REQUEST}"
echo ${REQUEST} | curl -i -v -H "Authorization: Bearier myToken" -d @- http://localhost:8000/APICaller