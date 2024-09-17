#!/bin/sh
cd "$(realpath "$(dirname "$0")")" || exit

download() {
    name=$1
    path=$2
    link=$3
    if ! [ -f "$path" ]; then
        echo Downloading "$name"
        curl --proto "=https" --tlsv1.2 --silent --show-error --output "$path" --location "$link"
        sleep 2
    else
        echo Skipping "$name"
    fi
}

latestVelocity() {
    velocityVersionGroup=$(curl --proto "=https" --tlsv1.2 --silent --show-error --location "https://api.papermc.io/v2/projects/velocity/" | jq --raw-output '.["version_groups"][-1]')
    velocityVersion=$(curl --proto "=https" --tlsv1.2 --silent --show-error --location "https://api.papermc.io/v2/projects/velocity/version_group/$velocityVersionGroup/" | jq --raw-output '.["versions"][-1]')
    velocityBuild=$(curl --proto "=https" --tlsv1.2 --silent --show-error --location "https://api.papermc.io/v2/projects/velocity/versions/$velocityVersion/" | jq --raw-output '.["builds"][-1]')
    echo "https://api.papermc.io/v2/projects/velocity/versions/$velocityVersion/builds/$velocityBuild/downloads/velocity-$velocityVersion-$velocityBuild.jar"
}

download "Velocity" "velocity.jar" "$(latestVelocity)"
download "PistonQueue" "plugins/PistonQueue-3.0.0.jar" "https://github.com/AlexProgrammerDE/PistonQueue/releases/download/3.0.0/PistonQueue-3.0.0.jar"

java -Xms1G -Xmx1G \
    -XX:+UseG1GC \
    -XX:G1HeapRegionSize=4M \
    -XX:+UnlockExperimentalVMOptions \
    -XX:+ParallelRefProcEnabled \
    -XX:+AlwaysPreTouch \
    -Dlog4j2.formatMsgNoLookups=true \
    -jar velocity.jar
