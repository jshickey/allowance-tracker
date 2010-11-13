(ns  com.changeitupdesigns.allowancetracker.db
  "data persistence - using the Google App Engine Datastore"
  (:require 
	    [appengine.datastore.core :as ds]
	    [appengine.users :as users])
  (:import (com.google.appengine.api.datastore Query Query$FilterOperator)))

(defstruct ledger-entry :account-name :date :earned-spent :amount :reason :user)

(defstruct account :account-name :ledger)

;;
;; DATASTORE FUNCTIONS
;;

;; fetch entities

; get the user id for the currently logged in user
(defn get-user-info []
  (users/user-info))

(defn get-userid []
   (let [ui (users/user-info)
        user (:user ui)]
     (if user
       (.getEmail user))))


(defn get-all-ledger-entries []
  "Returns all ledger entries stored in the datastore."
  (ds/find-all (Query. "ledger-entry")))

(defn get-ledger-entries-by-account 
  ([account-name] (get-ledger-entries-by-account account-name (get-userid)))
  ([account-name userid]
     "Returns all ledger entries stored in the datastore."
     (ds/find-all (doto (Query. "ledger-entry")
		 (.addFilter "userid" Query$FilterOperator/EQUAL userid)
		 (.addFilter "account-name" Query$FilterOperator/EQUAL account-name)))))

;;
;; account functions
;;
(defn clear-accounts []
  (map ds/delete-entity (ds/find-all (doto (Query. "account")))))
			  

(defn get-all-accounts
  ([] (get-all-accounts (get-userid)))
  ([userid] (ds/find-all (doto (Query. "account")
			   (.addFilter "userid" Query$FilterOperator/EQUAL userid)))))

(defn get-account-by-name 
  ([account-name] (get-account-by-name account-name (get-userid)))
  ([account-name userid]
     (ds/find-all (doto (Query. "account")
		 (.addFilter "userid" Query$FilterOperator/EQUAL userid)
		 (.addFilter "account-name" Query$FilterOperator/EQUAL account-name)))))


(defn get-account
  ([key-str] (get-account (ds/string->key key-str) (get-userid)))
  ([key userid]
     (let [entity (ds/get-entity key)
	   key-str (ds/key->string (:key entity))]
       (assoc entity :key key-str))))

(defn create-account-from-params [params]
  "Stores a new account in the datastore and issues an HTTP Redirect to the main page."
  (println params)
  (let [ui (users/user-info)
        user (:user ui)]
    (ds/create-entity {:kind "account"
		       :account-name (params :account-name)})))

(defn save-account [params]
  (println params)
  (if (:key params)
    (ds/update-entity (ds/get-entity (ds/string->key (:key params))) (dissoc params :key))
    (ds/create-entity (assoc params :kind "account" :userid (get-userid)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; create ledger entries
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn get-ledger-entry
  ([key-str] (get-ledger-entry (ds/string->key key-str) (get-userid)))
  ([key userid]
     (let [entity (ds/get-entity key)
	   key-str (ds/key->string (:key entity))]
       (assoc entity :key key-str))))

(defn create-ledger-entry [params]
  "Stores a new ledger entry in the datastore and issues an HTTP Redirect to the main page."
  (println params)
  (let [ui (users/user-info)
        user (:user ui)]
    (ds/create-entity {:kind "ledger-entry"
		     :account-name (params "account-name")
                     :date (params "date")
                     :earned-spent (params "earned-spent")
                     :amount (Double/valueOf (params "amount"))
                     :reason (params "reason")
		     :userid (.getEmail user)})))

(defn update-ledger-entry [params]
  "Updates a ledger entry in the datastore and issues an HTTP Redirect to the main page."
  (println params)
  (let [ui (users/user-info)
        user (:user ui)]
    (ds/update-entity  (ds/string->key (params "key")) {
		     :acount-name (params "account-name")
  	             :date (params "date")
                     :earned-spent (params "earned-spent")
                     :amount (Double/valueOf (params "amount"))
                     :reason (params "reason")
		     :userid (.getEmail user)})))

(defn save-ledger-entry [params]
  (println params)
  (println (.getClass (params :amount)))
  (if (:key params)
    (ds/update-entity (ds/get-entity (ds/string->key (:key params))) (assoc (dissoc params :key) :amount params :amount)))
    (ds/create-entity (assoc params 
			:kind "ledger-entry" 
			:userid (get-userid) 
			:account-name "mike"
			:amount (Double/valueOf (str (params :amount))))))

(defn delete-ledger-entry-from-params [datastore-key]
  "deletes a ledger entry in the datastore and issues an HTTP Redirect to the main page."
  (println "deleting a ledger entry:" datastore-key)
  (let [ui (users/user-info)
        user (:user ui)]
    (ds/delete-entity  (ds/get-entity (ds/string->key datastore-key)))))

;; utility for creating a list of ledger entries from test data
;(defn save-ledger [ledger]
;  (map create-ledger-entry ledger))
