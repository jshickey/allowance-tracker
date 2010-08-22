(ns com.changeitupdesigns.allowancetracker.core
  (:gen-class :extends javax.servlet.http.HttpServlet)

  (:use compojure.core
	ring.middleware.session
	(sandbar stateful-session)
    [ring.util.servlet  ];:only [defservice]]
    [ring.util.response] ; :only [redirect]]
    [hiccup.core :only [h html]]
    [hiccup.page-helpers :only [doctype include-css link-to xhtml-tag]]
    [hiccup.form-helpers :only [form-to text-area text-field drop-down select-options hidden-field]])

  (:require [compojure.route :as route]
    [appengine.datastore.core :as ds]
    [appengine.users :as users])

  (:gen-class :extends javax.servlet.http.HttpServlet)

  (:import (com.google.appengine.api.datastore Query Query$FilterOperator)))

(defstruct ledger-entry :account-name :date :earned-spent :amount :reason :user)
(defstruct account :account-name :ledger)

(def cams-ledger (list
  (struct ledger-entry "Cam" "2010-10-17" 'earned 19.75 "Initial Balance" "jscotthickey@gmail.com")
  (struct ledger-entry "Cam" "2010-10-19" 'earned 20.00 "Mowing" "jscotthickey@gmail.com")
  (struct ledger-entry "Cam" "2010-10-26" 'earned 30.00 "Report Cards - As" "jscotthickey@gmail.com")
  (struct ledger-entry "Cam" "2010-10-26" 'earned 20.00 "Mowing" "jscotthickey@gmail.com")
  (struct ledger-entry "Cam" "2010-11-09" 'spent 19.75 "Hellboy Book" "jscotthickey@gmail.com")))

;(def cams-account (struct account "Cam" cams-ledger))

(defn subtotal-old
  "Sum the amount property in the ledger that match the value passed for earned or spent.
   The expected values are 'earned 'spent"
  [ledger earned-spent]
  (reduce +
	  (map :amount
	       (filter #(=  (% :earned-spent)  earned-spent) 
ledger))))

;; uses thread last macro for expression threading
(defn subtotal  [ledger earned-spent]
  (->> ledger
       (filter #(=  (% :earned-spent)  earned-spent))
       (map :amount)
       (reduce + )))

(defn subtotal-earned [ledger] (subtotal ledger "earned"))
(defn subtotal-spent  [ledger] (subtotal ledger "spent"))

(defn balance [ledger]
  "calculate the current balance of the ledger subtracting spent from earned amounts"
   (- (subtotal-earned ledger) (subtotal-spent ledger)))


(defn list-of-propertys [p struct]
 (map #(%1 struct) p))

(def column-headings '(:account-name :date :earned-spent :amount :reason))

(defn display-ledger-entry [ledger-entry]
  (map #(%1 ledger-entry) column-headings))

(defn display-ledger [ledger]
 (map #(list-of-propertys column-headings %1) ledger))

;;
;; DATASTORE FUNCTIONS
;;

;; fetch entities
(defn get-all-ledger-entries []
  "Returns all ledger entries stored in the datastore."
  (ds/find-all (Query. "ledger-entry")))

(defn get-ledger-entries-by-account [account-name userid]
  "Returns all ledger entries stored in the datastore."
  (ds/find-all (doto (Query. "ledger-entry")
		 (.addFilter "userid" Query$FilterOperator/EQUAL userid)
		 (.addFilter "account-name" Query$FilterOperator/EQUAL account-name))))

(defn get-all-accounts [userid]
  (ds/find-all (Query. "account")))

(defn get-account [account-name userid]
  (ds/find-all (doto (Query. "account")
		 (.addFilter "userid" Query$FilterOperator/EQUAL userid)
		 (.addFilter "account-name" Query$FilterOperator/EQUAL account-name))))

;; create entities
(defn create-ledger-entry [ledger-entry]
  "Stores a new ledger entry in the datastore and issues an HTTP Redirect to the main page."
  (println ledger-entry)
  (let [ui (users/user-info)
        user (:user ui)]
    (ds/create-entity {:kind "ledger-entry"
		     :account-name (ledger-entry :account-name)
                     :date (ledger-entry :date)
                     :earned-spent (str (:earned-spent ledger-entry))
                     :amount (:amount ledger-entry)
                     :reason (:reason ledger-entry)
		     :userid (.getEmail user)})))

(defn create-ledger-entry-from-params [params]
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
		     :userid (.getEmail user)}))
  (redirect "/"))

;; update entities 
(defn update-ledger-entry-from-params [params]
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
		     :userid (.getEmail user)}))
  (redirect "/"))


(defn save-ledger [ledger]
  (map create-ledger-entry ledger))
;;
;; VIEW HELPERS
;;
;(defn session-update-current-account [account]
;  (session-assoc :current-account account))

(defn render-page [title & body]
  "Renders HTML around a given payload, acts as a template for all pages."
  (html
    (doctype :xhtml-strict)
    (xhtml-tag "en"
      [:head
       [:title title]
       (include-css "/css/main.css")]
      [:body
       [:h1 title]
       [:div#main body]
       ])))

(defn get-default-user-account []
 (let [ui (users/user-info)]
     (if-let [user (:user ui)]
	(let [userid (.getEmail user)]
	  (first (map :account-name (get-all-accounts userid)))))))
;;
;; VIEWS
;;
(defn main-page [account-in-session]
  (render-page "The Allowance Tracker"
	       (println "account-in-session:" account-in-session)
    ;(save-ledger cams-ledger)
    [:h3 "Current User"]
    (let [ui (users/user-info)]
      (if-let [user (:user ui)]
	(let [userid (.getEmail user)
	      current-account account-in-session
	      account-names (map :account-name (get-all-accounts userid))
	      selected-account (if current-account
				 current-account 
			         (first account-names))
	      ledger (get-ledger-entries-by-account selected-account userid) ]
	  (html 
	   [:ul
	    [:li "Logged in as " userid]
	    [:li "Selected Account: " selected-account]
	    [:li "Current Balance for " selected-account ": $" (balance ledger)]
	    [:li (link-to (.createLogoutURL (:user-service ui) "/") "Logout")]
	    [:li (link-to "/new" "Create new ledger entry")]]
	   [:form {:method "post" :action "/changecurrentaccount"}
	    (drop-down :account-list
		       (map :account-name (get-all-accounts (.getEmail user))) 
		       selected-account)
	    [:input.action {:type "submit" :value "Change Account"}]]
	   [:form {:method "post" :action "/edit"} 
	    (drop-down {:size 10} :ledger-list-box (map #(vector (apply str (interpose " " (display-ledger-entry %))) (ds/key->string (% :key))) ledger))
	    [:br]
	    [:input.action {:type "submit" :value "Edit"}]]))
	  [:ul
	   [:li "Not logged in"]
	   [:li (link-to (.createLoginURL (:user-service ui) "/") "Login")]]
        ))))

(defn dump-data []
  (render-page "Here's a dump of the data in the data store"
    (map #(html [:br] (str %) (:key %)) (get-all-ledger-entries))))

(def new-form
     (form-to [:post "/createledgerentry"]
              [:fieldset
               [:legend "Create a new ledger entry"]
               [:ol
                [:li
                 [:label {:for :account-name} "Account Name:"]
                 (text-field :account-name)]
                [:li
                 [:label {:for :date} "Date"]
                 (text-field :date)]
                [:li
                 [:label {:for :earned-spent} "earned-spent"]
                 (text-field :earned-spent)]
                [:li
                 [:label {:for :reason} "Reason"]
                 (text-field :reason)]
                [:li
                 [:label {:for :amount} "Amount"]
                 (text-field :amount)]]
               [:button {:type "submit"} "Save"]]))

(defn edit-form [datastore-key]
     (println datastore-key)
     (let [ledger-entry (ds/get-entity (ds/string->key datastore-key))]
       (form-to [:post "/updateledgerentry"]
	      (hidden-field :key (ds/key->string (ledger-entry :key)))
              [:fieldset
               [:legend "Edit a ledger entry"]
               [:ol
                [:li
                 [:label {:for :account-name} "Account Name:"]
                 (text-field :account-name (ledger-entry :account-name))]
                [:li
                 [:label {:for :date} "Date"]
                 (text-field :date (ledger-entry :date))]
                [:li
                 [:label {:for :earned-spent} "earned-spent"]
                 (text-field :earned-spent (ledger-entry :earned-spent))]
                [:li
                 [:label {:for :reason} "Reason"]
                 (text-field :reason (ledger-entry :reason))]
                [:li
                 [:label {:for :amount} "Amount"]
                 (text-field :amount (ledger-entry :amount))]]
               [:button {:type "submit"} "Save"]])))

(defn go-to-main [params] 
 (println "trying to put into the session " (params "account-list"))
 (session-put! "current-user" (params "account-list"))
  (redirect "/"))

(defroutes public-routes
  (GET "/" {session :session}  (main-page (session-get "current-user")))
  (GET  "/new"  [] (render-page "New Ledger Entry" new-form))
  (POST  "/edit" [ledger-list-box] (render-page "Update Ledger Entry" (edit-form ledger-list-box)))
  (POST "/createledgerentry" {params :params}  (create-ledger-entry-from-params params))
  (POST "/updateledgerentry" {params :params}   (update-ledger-entry-from-params params))
  (POST "/changecurrentaccount" {params :params} (go-to-main params))
  (GET "/dumpdata" [] (dump-data)))

(defroutes example
  public-routes
  ;;  (ANY "/admin/*" [] admin-routes)
  (route/not-found "Page not found"))

(def app
    (-> public-routes
;        (with-security authenticate)
        wrap-stateful-session))

(defservice app)