#!/bin/sh
# Long unattended population run on a dev server, in a fresh throwaway world ("poprun").
# Builds a glass pen with berry bushes, a water cauldron and two frogs, releases 3 females and 3 males,
# speeds up growth and ageing, and prints /flygenes stats every 5 minutes.
# Usage: headless/poprun.sh [minutes] [frogs: 0 or 2]   Log: docs/poprun/server.log
cd "$(dirname "$0")/.." || exit 1
MIN=${1:-55}
FROGS=${2:-0}
nap() { timeout "$1" tail -f /dev/null; }
rm -rf run/poprun
sed -i 's/^level-name=.*/level-name=poprun/' run/server.properties
(
  nap 100
  cat <<CMDS
forceload add -16 -16 16 16
gamerule doDaylightCycle false
gamerule doWeatherCycle false
gamerule doMobSpawning false
gamerule randomTickSpeed 20
time set noon
fill -11 -61 -11 11 -54 11 glass hollow
fill -10 -61 -10 10 -61 10 grass_block
setblock 0 -60 0 water_cauldron[level=3]
setblock -6 -60 -6 sweet_berry_bush[age=3]
setblock 6 -60 -6 sweet_berry_bush[age=3]
setblock -6 -60 6 sweet_berry_bush[age=3]
setblock 6 -60 6 sweet_berry_bush[age=3]
setblock 0 -60 -7 sweet_berry_bush[age=3]
setblock 0 -60 7 sweet_berry_bush[age=3]
setblock -7 -60 0 sweet_berry_bush[age=3]
setblock 7 -60 0 sweet_berry_bush[age=3]
setblock -3 -60 3 sweet_berry_bush[age=3]
setblock 3 -60 -3 sweet_berry_bush[age=3]
flylab brains 3
flygenes set growth_speed 60
flygenes set adult_life_minutes 8
flygenes set cap 12
summon connectomefly:connectome_fly -2 -60 -2 {Male:0b}
summon connectomefly:connectome_fly 2 -60 2 {Male:0b}
summon connectomefly:connectome_fly -2 -60 2 {Male:0b}
summon connectomefly:connectome_fly 2 -60 -2 {Male:1b}
summon connectomefly:connectome_fly 3 -60 0 {Male:1b}
summon connectomefly:connectome_fly -3 -60 0 {Male:1b}
setblock -4 -60 -8 melon
setblock 4 -60 8 melon
setblock 8 -60 -4 melon
setblock -8 -60 4 melon
flygenes set
CMDS
  if [ "$FROGS" != 0 ]; then echo "summon frog 8 -60 8"; echo "summon frog -8 -60 -8"; fi
  nap 10; echo "flygenes full"
  i=0
  while [ $i -lt "$MIN" ]; do
    nap 60; i=$((i + 1))
    echo "say MINUTE $i"
    echo "execute as @e[type=connectomefly:connectome_fly,nbt={Male:0b}] run data get entity @s Hunger"
    if [ $((i % 5)) -eq 0 ]; then
      echo "flygenes stats"; echo "flylab brains"
      echo "execute if entity @e[type=connectomefly:connectome_fly,x=-11,y=-62,z=-11,dx=22,dy=9,dz=22] run say some flies are inside the pen"
    fi
    [ $((i % 10)) -eq 0 ] && echo "flygenes full"
  done
  echo "flygenes full"; nap 3; echo stop
) | ./gradlew runServer --console=plain --args=nogui > docs/poprun/server.log 2>&1
sed -i 's/^level-name=.*/level-name=devworld/' run/server.properties
