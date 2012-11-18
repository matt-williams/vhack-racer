#!/bin/sh
# Usage: jsontopng.sh <JSON map file>
# Converts JSON map file into PNG file for use in vHack Racer.
{
  echo P3
  echo 64 64
  echo 255
  grep data $1 | sed -e 's/.*"data":\[//g' | sed -e 's/],//g' > /tmp/jsontopng.sh.$$
  head -1 /tmp/jsontopng.sh.$$ | sed -e 's/, */\
/g' > /tmp/jsontopng.sh.terrain.$$
  tail -1 /tmp/jsontopng.sh.$$ | sed -e 's/, */\
/g' > /tmp/jsontopng.sh.specials.$$
  paste /tmp/jsontopng.sh.terrain.$$ /tmp/jsontopng.sh.specials.$$ | awk '/([0-9]+) ([0-9]+)/ { print ($1 - 1) % 40 " " int(($1 - 1) / 40) " " ($2 - 1600) }'
} | pnmtopng > $(echo $1 | sed -e 's/.json$//g').png