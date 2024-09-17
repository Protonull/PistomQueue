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

latestPaper() {
    paperVersion=$1
    paperBuild=$(curl --proto "=https" --tlsv1.2 --silent --show-error --location "https://api.papermc.io/v2/projects/paper/versions/$paperVersion/" | jq --raw-output '.["builds"][-1]')
    echo "https://api.papermc.io/v2/projects/paper/versions/$paperVersion/builds/$paperBuild/downloads/paper-$paperVersion-$paperBuild.jar"
}

download "PaperMC" "paper.jar" "$(latestPaper "1.21.1")"

# Put server into offline-mode and port to 25570
echo "$(cat <<EOF
online-mode=false
server-port=25570
query.port=25570
EOF
)" > server.properties

# Make the config dir if it doesn't already exist
if [ ! -d "config/" ]; then
    mkdir "config"
fi

echo "$(cat <<EOF
proxies:
  velocity:
    enabled: true
    online-mode: true
    secret: "1234567890ABCDEF"
EOF
)" > config/paper-global.yml

java -Xms2G -Xmx2G \
    -XX:+UseG1GC \
    -XX:+ParallelRefProcEnabled \
    -XX:MaxGCPauseMillis=200 \
    -XX:+UnlockExperimentalVMOptions \
    -XX:+DisableExplicitGC \
    -XX:+AlwaysPreTouch \
    -XX:G1NewSizePercent=30 \
    -XX:G1MaxNewSizePercent=40 \
    -XX:G1HeapRegionSize=8M \
    -XX:G1ReservePercent=20 \
    -XX:G1HeapWastePercent=5 \
    -XX:G1MixedGCCountTarget=4 \
    -XX:InitiatingHeapOccupancyPercent=15 \
    -XX:G1MixedGCLiveThresholdPercent=90 \
    -XX:G1RSetUpdatingPauseTimePercent=5 \
    -XX:SurvivorRatio=32 \
    -XX:+PerfDisableSharedMem \
    -XX:MaxTenuringThreshold=1 \
    -Dusing.aikars.flags=https://mcflags.emc.gs \
    -Daikars.new.flags=true \
    -Dlog4j2.formatMsgNoLookups=true \
    -jar paper.jar nogui
