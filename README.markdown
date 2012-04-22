# MongoDB Support for Judgr

This project adds MongoDB support for Judgr, is a
[naïve Bayes classifier](http://en.wikipedia.org/wiki/Naive_Bayes_classifier)
library written in Clojure.

## Getting Started

Add the following dependencies to your _project.clj_ file:

````clojure

[judgr "0.1.1"]
[judgr-mongodb "0.1.0"]
````

Then, require the `mongo-db` module and adjust the settings in order
to create your classifier:

````clojure

(ns your-ns
  (:use [judgr.core]
        [judgr.settings]
        [judgr-mongodb.db.mongo-db]))

(def new-settings
  (update-settings settings
                   [:database :type] :mongo
                   [:database :mongo :database] "your-db"
                   [:database :mongo :host]     "localhost"
                   [:database :mongo :port]     27017
                   [:database :mongo :auth?]    false
                   [:database :mongo :username] ""
                   [:database :mongo :password] ""))

(def classifier (classifier-from new-settings))

````

Doing this, all data will be stored in the specified MongoDB instance.

## License

Copyright (C) Daniel Fernandes Martins

Distributed under the New BSD License. See COPYING for further details.
