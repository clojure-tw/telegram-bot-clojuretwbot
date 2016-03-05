# clojuretwbot - A telegram bot for clojure.tw channel
[![Build Status](https://travis-ci.org/clojure-tw/telegram-bot-clojuretwbot.svg?branch=master)](https://travis-ci.org/clojure-tw/telegram-bot-clojuretwbot)
[![Dependencies Status](https://jarkeeper.com/clojure-tw/telegram-bot-clojuretwbot/status.svg)](https://jarkeeper.com/clojure-tw/telegram-bot-clojuretwbot)
[![License](http://img.shields.io/badge/license-GPL-blue.svg?style=flat)](http://www.opensource.org/licenses/gpl-license.html)

This is a simple telegram bot designed for clojure.tw community.

## What this bot do ?

1. Fetch and send latest rss feed in: [Planet Clojure](http://planet.clojure.in/atom.xml) to telegram.
2. Fetch and send any news in [Clojure Mailing-list](https://groups.google.com/forum/#!forum/clojure) which start with *[ANN]* to telegram.

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

## License

Copyright © 2016 Yu-Fu, Wu <<wennynnewenny@gmail.com>>

Copyright © 2016 Yen-Chin, Lee <<coldnew.tw@gmail.com>>

Distributed under the GPLv3 or any later version.
