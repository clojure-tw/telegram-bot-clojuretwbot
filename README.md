# clojuretwbot - A telegram bot for clojure.tw channel
[![Circle CI](https://circleci.com/gh/clojure-tw/telegram-bot-clojuretwbot.svg?style=svg)](https://circleci.com/gh/clojure-tw/telegram-bot-clojuretwbot)
[![Dependencies Status](https://jarkeeper.com/clojure-tw/telegram-bot-clojuretwbot/status.svg)](https://jarkeeper.com/clojure-tw/telegram-bot-clojuretwbot)
[![License](http://img.shields.io/badge/license-GPL-blue.svg?style=flat)](http://www.opensource.org/licenses/gpl-license.html)
[![Stories in Ready](https://badge.waffle.io/clojure-tw/telegram-bot-clojuretwbot.png?label=ready&title=Ready)](http://waffle.io/clojure-tw/telegram-bot-clojuretwbot)

This is a special telegram bot designed for clojure.tw community.

## What this bot do ?

1. Fetch and send latest rss feed in: [Planet Clojure](http://planet.clojure.in/atom.xml) to telegram.
2. Fetch and send any news in [Clojure Mailing-list](https://groups.google.com/forum/#!forum/clojure) which start with *[ANN]* to telegram.
3. Fetch some Chinese/Taiwanese blog post about clojure/clojurescript
   - [coldnew's blog](https://coldnew.github.io)
   - [庄周梦蝶](http://blog.fnil.net/)

## Screenshots

So you want to know what this bot do in real-life example, here's the screenshot:

![Screenshot](https://raw.githubusercontent.com/clojure-tw/telegram-bot-clojuretwbot/master/screenshot/screenshot.png)

## Usage

To use this bot, you need to setup following environment variables: *TOKEN*, *CHAT_ID*, *DATABASE*

If you don't have any database, you need to run following command first

```bash
   TOKEN="telegram bot token" CHAT_ID="telegram room id" DATABASE="resources/database.db" lein run -- migrate
```

then start the bot

```bash
   TOKEN="telegram bot token" CHAT_ID="telegram room id" DATABASE="resources/database.db" lein run
```

## Deploy on OpenShift

Our bot is deploy on [openshift](https://openshift.redhat.com), to do so, you need to setup needed variables by *rhc* command:

```bash
   rhc env set TOKEN="TELEGRAM BOT TOKEN" CHAT_ID="TELEGRAM CHAT ID" DATABASE="Bot database position" -a App_Name
```

After environment variables setup, you can use following command to make sure all variables is ready

(NOTE: following info are just examples.)

```bash
$ rhc env list App_Name
CHAT_ID=-99999999
DATABASE=/var/lib/openshift/5708cdb089f5cfdacd000136/app-root/data/database.db
TOKEN=999992998:ABCDEfghijkl-mnnDi2HLgsdIT9Zc0NWTLz
```

For more info, please refer [openshift doc](https://developers.openshift.com/managing-your-applications/environment-variables.html)

## License

Copyright © 2016 Yu-Fu, Wu <<wennynnewenny@gmail.com>>

Copyright © 2016 Yen-Chin, Lee <<coldnew.tw@gmail.com>>

Distributed under the GPLv3 or any later version.
