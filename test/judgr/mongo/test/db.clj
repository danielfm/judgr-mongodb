(ns judgr.mongo.test.db
  (:use [judgr.core]
        [judgr.settings]
        [judgr.mongo.db]
        [judgr.mongo.test.util]
        [clojure.test])
  (:require [somnium.congomongo :as mongodb])
  (:import [judgr.mongo.db MongoDB]))

(def new-settings
  (update-settings settings
                   [:database :type] :mongo
                   [:database :mongo :database] "judgr-test"))

(def-fixture empty-db []
  (let [db (db-from new-settings)]
    (.clean-db! db)
    (test-body)))

(def-fixture basic-db []
  (let [db (db-from new-settings)]
    (.clean-db! db)
    (.add-item! db "Some message" :positive)
    (.add-item! db "Another message" :positive)
    (.add-feature! db "Some message" "message" :positive)
    (.add-feature! db "Another message" "message" :positive)
    (.add-feature! db "Another message" "another" :positive)
    (test-body)))

(deftest ensure-mongodb
  (with-fixture empty-db []
    (is (instance? MongoDB db))))

(deftest adding-items
  (with-fixture empty-db []
    (testing "if everything's ok"
      (let [data (.add-item! db "Some message" :positive)]
        (is (= "Some message" (:item data)))
        (is (= :positive (:class data)))))

    (testing "if class is invalid"
      (is (thrown? IllegalArgumentException
                   (.add-item! db "Some message" :some-class))))))

(deftest adding-features
  (with-fixture empty-db []
    (testing "if everything's ok"
      (let [data (.add-feature! db "Some message" "message" :positive)]
        (is (= "message" (:feature data)))
        (is (= '(:positive) (-> data :classes keys)))
        (is (= 1 (-> data :classes :positive)))
        (is (= 1 (:total data)))))

    (testing "if class is invalid"
      (is (thrown? IllegalArgumentException
                   (.add-feature! db "Some message" "message" :some-class))))))

(deftest updating-features
  (with-fixture basic-db []
    (let [data (.add-feature! db "Subliminar message" "message" :negative)]
      (is (= "message" (:feature data)))
      (is (= '(:negative :positive) (-> data :classes keys)))
      (is (= 2 (-> data :classes :positive)))
      (is (= 1 (-> data :classes :negative)))
      (is (= 3 (:total data))))))

(deftest counting-features
  (with-fixture basic-db []
    (is (= 2 (.count-features db))))

  (testing "when there's no features"
    (with-fixture empty-db []
      (is (zero? (.count-features db))))))

(deftest getting-feature
  (with-fixture basic-db []
    (let [data (.get-feature db "message")]
      (is (= "message" (:feature data)))
      (is (= '(:positive) (-> data :classes keys)))
      (is (= 2 (-> data :classes :positive)))
      (is (= 2 (:total data))))

    (testing "when feature doesn't exist"
      (is (nil? (.get-feature db "void"))))))

(deftest getting-items
  (with-fixture basic-db []
    (is (= '({:item "Some message" :class :positive}
             {:item "Another message" :class :positive})
           (.get-items db)))

    (testing "when there's no items"
      (with-fixture empty-db []
        (is (empty? (.get-items db)))))))

(deftest counting-items
  (with-fixture basic-db []
    (is (= 2 (.count-items db))))

  (testing "when there's no items"
    (with-fixture empty-db []
      (is (zero? (.count-items db))))))

(deftest counting-items-of-class
  (with-fixture basic-db []
    (is (= 2 (.count-items-of-class db :positive)))

    (testing "when there's no items in class"
      (is (zero? (.count-items-of-class db :negative))))))