#!/bin/bash
if [[(-n "$1") && (-n "$2")]]; then
    java -cp ./target schallplatten.SoundToImage $1 $2
else
    echo "Usage: encode <input-audio-wav> <output-image-png>"
fi
