#!/bin/bash


REQUEST=`cat <<EOF
		{
			"path" : "/artifact-fields/{field-id}/options",
			"method" : "get",
			"parameters" : [
				{
					"name": "field-id",
					"value" : 123
				}, 
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