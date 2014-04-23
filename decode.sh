#!/bin/bash
if [[(-n "$1") && (-n "$2")]]; then
    java -cp ./target schallplatten.ImageToSound $1 $2
else
    echo "Usage: decode <input-image-png> <output-audio-wav>"
fi
