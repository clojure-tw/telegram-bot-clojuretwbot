#!/bin/bash

# -----------------------------------------------------
# Start bot
# -----------------------------------------------------

cd /data

# make surce database migrate to latest config
lein run -- migrate

echo ""
echo ""
echo "------------------------------------------------------------"
echo "    Start Telegram Bot ClojureTW"
echo "------------------------------------------------------------"
echo ""

# start the bot
lein run
