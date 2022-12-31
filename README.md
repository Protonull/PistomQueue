# PistomQueue

[![GitHub](https://img.shields.io/github/license/Protonull/PistomQueue?style=flat-square&color=b2204c)](https://github.com/Protonull/PistomQueue/blob/master/LICENSE)
[![GitHub Repo stars](https://img.shields.io/github/stars/Protonull/PistomQueue?style=flat-square)](https://github.com/Protonull/PistomQueue/stargazers)

PistomQueue is a standalone implementation of [PistonQueue](https://github.com/AlexProgrammerDE/PistonQueue) for
[Minestom](https://github.com/Minestom/Minestom). Since the queue logic is done on the proxy side, the server is only
there to host the players while they wait: they'll be whisked off the server again by the proxy. This is a great use
case for [Minestom](https://github.com/Minestom/Minestom). You should probably use this in tandem with
[ViaVersion](https://github.com/ViaVersion/ViaVersion).

## Install

You can either use the provided latest build [here](https://github.com/Protonull/PistomQueue/releases/tag/latest) or you
can compile it yourself with JDK 17 by doing:
```shell
git clone https://github.com/Protonull/PistomQueue.git
cd PistomQueue
./gradlew build
```
The resulting jar will be located at: `build/libs/PistomQueue-<VERSION>.jar`

## Usage

You'll need Java 17 or above to run PistomQueue. You need only execute it like so:
```shell
java -jar PistomQueue-<VERSION>.jar
```

## Options

PistomQueue permits the following options:
```shell
# don't try and copy paste this script, it wont work, it's more to give an idea

java -jar \

    # The server's hostname
    -Dhost="0.0.0.0" \
    
    # The server's port
    -Dport=25565 \
    
    # Whether players should be hidden from one another
    -DhidePlayers=false \
    
    # Whether chatting should be disabled
    -DdisableChat=false \
    
    # Whether to play a chime to players when they're close to the front of the queue
    -DplayXP=true \
    
    # What proxy to use, if any:
    # - NONE
    # - BUNGEE
    # - VELOCITY
    -Dproxy="NONE" \
    
    # The names of those, separated by commas, who should be exempt from restrictions
    -DexemptedPlayers="FirstPlayer,SecondPlayer,__Third-Player__" \

    # (OPTIONAL) Your Bungee tokens, assuming you've specified Bungee as your proxy, separated by commas
    -DbungeeTokens="Token1,Token2" \
    
    # (OPTIONAL) Your Velocity secret, assuming you've specified Velocity as your proxy
    -DvelocitySecret="YourSecret"
```
