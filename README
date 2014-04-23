# Schallplatten

The utilities to convert between WAV and "gramophone record" formats.
Supports round and polygonal records.

## Encoding
1. Prepare an audio file that you want to encode, e.g. input.wav.
2. Define constants in Constants.java. Default are probably fine for 5 to 50 seconds file.
3. Run "compile.sh".
4. Run "encode.sh input.wav image.png".
5. Mark the value of TURN_COUNT constant and the length of your audio file in seconds.
   The ratio of these numbers is the playback speed. 
   E.g. you have 40 turns and 20 seconds, which gives you 2 turns/second or 120 turns/minute.
   
## Decoding
6. Set the playback speed to TURNS_PER_SECOND in ImageToSound.java.
7. Run "compile.sh"
8. Run "decode.sh image.png output.wav".
9. Assuming that decoder is compiled with the same constants as encoder, and the playback 
   speed is set correctly, output.wav should contain understandable replay of input.wav.
   
