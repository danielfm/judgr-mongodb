(ns judgr-mongodb.test.db.mongo-db
  (:use [judgr.core]
        [judgr.settings]
        [judgr-mongodb.db.mongo-db]
        [judgr-mongodb.test.util]
        [clojure.test])
  (:require [somnium.congomongo :as mongodb])
  (:import [judgr_mongodb.db.mongo_db MongoDB]))

(def new-settings
  (update-settings settings
                   [:database :type] :mongo
                   [:database :mongo :database] "judgr-test"))

(defn- clean-db!
  "Removes all documents from the database."
  [db]
  (mongodb/with-mongo (.get-connection db)
    (mongodb/destroy! :items {})
    (mongodb/destroy! :features {})))

(def-fixture empty-db []
  (let [db (db-from new-settings)]
    (clean-db! db)
    (test-body)))

(def-fixture basic-db []
  (let [db (db-from new-settings)]
    (clean-db! db)
    (.add-item! db "Some message" :ok)
    (.add-item! db "Another message" :ok)
    (.add-feature! db "Some message" "message" :ok)
    (.add-feature! db "Another message" "message" :ok)
    (.add-feature! db "Another message" "another" :ok)
    (test-body)))

(deftest ensure-mongodb
  (with-fixture empty-db []
    (is (instance? MongoDB db))))

(deftest adding-items
  (with-fixture empty-db []
    (testing "if everything's ok"
      (let [data (.add-item! db "Some message" :ok)]
        (is (= "Some message" (:item data)))
        (is (= :ok (:class data)))))

    (testing "if class is invalid"
      (is (thrown? IllegalArgumentException
                   (.add-item! db "Uma mensagem" :some-class))))))

(deftest adding-features
  (with-fixture empty-db []
    (testing "if everything's ok"
      (let [data (.add-feature! db "Some message" "message" :ok)]
        (is (= "message" (:feature data)))
        (is (= '(:ok) (-> data :classes keys)))
        (is (= 1 (-> data :classes :ok)))
        (is (= 1 (:total data)))))

    (testing "if class is invalid"
      (is (thrown? IllegalArgumentException
                   (.add-feature! db "Uma mensagem" "message" :some-class))))))

(deftest updating-features
  (with-fixture basic-db []
    (let [data (.add-feature! db "Subliminar message" "message" :offensive)]
      (is (= "message" (:feature data)))
      (is (= '(:offensive :ok) (-> data :classes keys)))
      (is (= 2 (-> data :classes :ok)))
      (is (= 1 (-> data :classes :offensive)))
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
      (is (= '(:ok) (-> data :classes keys)))
      (is (= 2 (-> data :classes :ok)))
      (is (= 2 (:total data))))

    (testing "when feature doesn't exist"
      (is (nil? (.get-feature db "void"))))))

(deftest getting-items
  (with-fixture basic-db []
    (is (= '("Some message"
             "Another message")
           (map :item (.get-items db))))

    (testing "when there's no items"
      (with-fixture empty-db []
        (is (= '() (.get-items db)))))))

(deftest counting-items
  (with-fixture basic-db []
    (is (= 2 (.count-items db))))

  (testing "when there's no items"
    (with-fixture empty-db []
      (is (zero? (.count-items db))))))

(deftest counting-items-of-class
  (with-fixture basic-db []
    (is (= 2 (.count-items-of-class db :ok)))

    (testing "when there's no items in class"
      (is (zero? (.count-items-of-class db :offensive))))))
