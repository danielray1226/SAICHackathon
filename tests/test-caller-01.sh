#!/bin/bash


REQUEST=`cat <<EOF
		{
			"path" : "/artifact-fields",
			"method" : "get",
			"parameters" : [
				{
					"name": "project",
					"value": "FooBar"
				},
				{
					"name": "type",
					"value": "Some Type"
				}
			]
		}
EOF
`

echo "submitting : ${REQUEST}"
echo ${REQUEST} | curl -i -v -X POST -d @- http://localhost:8000/APICaller