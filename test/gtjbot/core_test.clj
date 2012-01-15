;; ## Main tests namespace
;; Bunch of tests for the bot (mainly its parser part).
(ns gtjbot.tests.core
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
