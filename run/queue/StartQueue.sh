#!/bin/sh
cd "$(realpath "$(dirname "$0")")" || exit

if [ ! -f "../../build/libs/PistomQueue.jar" ]; then
    if ! (
        cd ../../
        ./gradlew clean build
    ); then
        echo "Could not build PistomQueue"
    fi
fi

java -Xms1G -Xmx1G \
    -XX:+UseG1GC \
    -XX:G1HeapRegionSize=4M \
    -XX:+UnlockExperimentalVMOptions \
    -XX:+ParallelRefProcEnabled \
    -XX:+AlwaysPreTouch \
    -Dlog4j2.formatMsgNoLookups=true \
    -Dport=25571 \
    -Dproxy="VELOCITY" \
    -DvelocitySecret="1234567890ADCDEF" \
    -jar ../../build/libs/PistomQueue.jar
