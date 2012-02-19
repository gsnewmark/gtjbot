# gtjbot

An extensible XMPP bot written in Clojure using Noir framework.
Runs on Google App Engine (uses appengine-magic).
Deployed example: http://gtjbot.appspot.com

## Existing bot commands
**httpsc <args>** - returns definition of a given numeric HTTP status code.
**weather <args>** - returns short forecast for a given city 
                     (using Yahoo! Weather).

You can pass multiple arguments separated by commas to each bot command 
(e.g., **httpsc 100, 404, 501**).
