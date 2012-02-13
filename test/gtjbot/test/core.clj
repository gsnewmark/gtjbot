;; ## Main tests namespace
;; Bunch of tests for the bot (mainly its parser part).
(ns gtjbot.test.core
  (:require [gtjbot.utils.parser :as parser])
  (:use [midje.sweet]))

;; ## Testing basic parser functionality

;; Tests for a help message.
(fact (parser/generate-answer "helloads") => parser/help-message)
(fact (parser/generate-answer "") => parser/help-message)

;; Tests for a greetings message.
(fact (parser/generate-answer "hi") => "Hello!")
(fact (parser/generate-answer "Hello, bot!! 1337") => "Hello!")
(fact (parser/generate-answer "Hail") => "Hello!")
(fact (parser/generate-answer "greetings... i think..") => "Hello!")

(tabular
 (future-fact "Meanings of HTTP status codes"
              (parser/generate-answer ?request) => ?meaning)
 ?request    ?meaning
 "200"       "OK\nStandard response for successful HTTP requests. The actual response will depend on the request method used. In a GET request, the response will contain an entity corresponding to the requested resource. In a POST request the response will contain an entity describing or containing the result of the action."
 "227"       "No such code."
 "301"       "Moved Permanently\nThis and all future requests should be directed to the given URI."
 "404"       "Not Found\nThe requested resource could not be found but may be available again in the future. Subsequent requests by the client are permissible."
 "502"       "Bad Gateway\nThe server was acting as a gateway or proxy and received an invalid response from the upstream server."
 "783"       "No such code.")