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
   - [ClojureTW Weekly](https://clojure.tw/weekly)

## Screenshots

So you want to know what this bot do in real-life example, here's the screenshot:

![Screenshot](https://raw.githubusercontent.com/clojure-tw/telegram-bot-clojuretwbot/master/screenshot/screenshot.png)

## Usage

We switch to use [docker](https://docs.docker.com) to host this bot on raspberry pi 2, you can add your `docker-compose.xml` file like below:

``` yaml
version: '2'
services:
  lein:
    build: ./docker
    container_name: lein
    restart: always
    volumes:
     - $PWD:/data
    environment:
      NODE_ENV: production
      TOKEN:    <YOUR TELEGRAM BOT TOKEN>
      CHAT_ID:  <YOUT TELEGRAM CHANNEL CHAT ID>
      DATABASE: <YOUR DATABASE FILE>
```

Then use `docker compose up -d` to make this bot running on background.

For more info, please refer [Docker Compose Doc](https://docs.docker.com/compose/)

## License

Copyright © 2016 Yu-Fu, Wu <<wennynnewenny@gmail.com>>

Copyright © 2016 Yen-Chin, Lee <<coldnew.tw@gmail.com>>

Distributed under the GPLv3 or any later version.
