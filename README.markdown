# MongoDB Support for Judgr

[![Build Status](https://secure.travis-ci.org/danielfm/judgr-mongodb.png?branch=master)](http://travis-ci.org/danielfm/judgr-mongodb)

This project adds [MongoDB](http://mongodb.org) support for
[Judgr](http://danielfm.github.com/judgr/), a naïve Bayes classifier
library written in Clojure.

## Getting Started

Add the following dependencies to your _project.clj_ file:

````clojure

[judgr/mongodb "0.1.2"]
````

Then, require the `judgr.mongo.db` module and adjust the settings in
order to create your classifier:

````clojure

(ns your-ns
  (:use [judgr.core]
        [judgr.settings]
        [judgr.mongo.db]))

(def new-settings
  (update-settings settings
                   [:database :type] :mongo
                   [:database :mongo] {:database "your-db"
                                       :host     "localhost"
                                       :port     27017
                                       :auth?    false
                                       :username ""
                                       :password ""}))

(def classifier (classifier-from new-settings))

````

Doing this, all data will be stored in the specified MongoDB instance.

## License

Copyright (C) Daniel Fernandes Martins

Distributed under the New BSD License. See COPYING for further details.
