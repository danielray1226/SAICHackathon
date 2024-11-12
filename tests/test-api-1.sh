#!/bin/bash
REQUEST=\
`cat <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<note>
  <to>Tove</to>
  <from>Jani</from>
  <heading>Reminder</heading>
  <body>Don't forget me this weekend!</body>
</note>
EOF`
echo "submitting : ${REQUEST}"
echo ${REQUEST} | curl -i -v -X POST -d @- http://localhost:8000/hack/api