#!/usr/bin/env bash

/wait

echo "Configuring expectations..."

curl -X PUT "http://localhost:1080/mockserver/expectation" -d '{
    "httpRequest": {
        "method": "GET",
        "path": "/percentage"
    },
    "httpResponse": {
        "statusCode": 200,
        "body": {
            "type": "SCRIPT",
            "script": {
                "type": "JAVASCRIPT",
                "script": "var Random = Java.type(\'java.util.Random\'); var random = new Random(); return \'\' + (1 + random.nextDouble() * 99);"
            }
        }
    }
}'