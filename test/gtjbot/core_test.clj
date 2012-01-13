;; ## Main tests namespace
;; Bunch of tests for bot (mainly its parser part).
(ns gtjbot.core_test
  (:require [gtjbot.utils.parser :as parser])
  (:use [midje.sweet]))

;; ## Testing basic parser functionality
(fact (parser/generate-answer "helloads") => parser/help-message)
(fact (parser/generate-answer "") => parser/help-message)
(fact (parser/generate-answer "hi") => "Hello!")
(fact (parser/generate-answer "Hello, bot!! 1337") => "Hello!")
(fact (parser/generate-answer "Hail") => "Hello!")
(fact (parser/generate-answer "greetings... i think..") => "Hello!")
