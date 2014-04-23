#!/bin/bash
rm -r ./target
mkdir target
javac -d ./target/ schallplatten/wav/WavFile.java schallplatten/wav/WavFileException.java schallplatten/Constants.java schallplatten/SoundToImage.java schallplatten/ImageToSound.java

