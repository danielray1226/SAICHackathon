#!/bin/bash
REQUEST='{"hello" : "world", "daniel" : 2}'

echo "submitting : ${REQUEST}"
echo ${REQUEST} | curl -i -v -X POST -d @- http://localhost:8000/TestAPI/