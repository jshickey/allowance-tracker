(ns com.changeitupdesigns.allowancetracker.core
  (:gen-class :extends javax.servlet.http.HttpServlet)

  ;; TODO - add :only to all libraries that are used
  (:use [compojure.core :only [defroutes GET POST]]
	ring.middleware.session
	[sandbar.core :only [get-param]]
	[sandbar.stateful-session :only [wrap-stateful-session
					 session-put!
					 flash-put!]]
	[sandbar.validation :only [build-validator
                                   non-empty-string
                                   add-validation-error]]
	[com.reasonr.scriptjure :only [js]]
	[ring.util.servlet  ];:only [defservice]]
	[ring.util.response] ; :only [redirect]]
	[hiccup.core :only [h html]]
	[hiccup.page-helpers :only [doctype include-css link-to xhtml-tag]]
	[hiccup.form-helpers :only [form-to text-area text-field drop-down select-options hidden-field]])

  (:require [compojure.route :as route]
	    [sandbar.forms :as forms]
	    [com.changeitupdesigns.allowancetracker.views :as views]
	    [com.changeitupdesigns.allowancetracker.db :as db]
	    [appengine.datastore.core :as ds]
	    [appengine.users :as users])

)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; MODEL functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO - change to defrecord
;; TODO - update ledger be part of account?
;; TODO - write tests for the model
;; TODO - move the model into its own namespace/file

;(defstruct ledger-entry :account-name :date :earned-spent :amount :reason :user)
;(defstruct account :account-name :ledger)

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

(defn list-of-property-values [p struct]
 (map #(%1 struct) p))

;; TODO - possibly move this to top of
(def ledger-properties '(:account-name :date :earned-spent :amount :reason))

(defn display-ledger-entry [ledger-entry]
  (map #(%1 ledger-entry) ledger-properties))

(defn display-ledger [ledger]
  "shows only the property values for each ledger entry" 
 (map #(display-ledger-entry %) ledger))

;;
;; DATASTORE FUNCTIONS
;;

  
(defn create-ledger-entry-from-params [params]
  (db/create-ledger-entry params)
  (redirect "/"))

(defn create-account-from-params [params]
  (db/create-account-from-params params)
  (redirect "/"))

;; update entities 
(defn create-account-from-params [params]
  (db/create-account-from-params params)
  (redirect "/"))

;; delete entities 
(defn delete-ledger-entry-from-params [datastore-key]
  (db/delete-ledger-entry-from-params datastore-key)
  (redirect "/"))

(defn update-ledger-entry-from-params [params]
  (db/update-ledger-entry params)
  (redirect "/"))
;;

;; VIEW HELPERS
;;
;(defn session-update-current-account [account]
;  (session-assoc :current-account account))

(defn get-default-user-account []
 (let [ui (users/user-info)]
     (if-let [user (:user ui)]
	(let [userid (.getEmail user)]
	  (first (map :account-name (db/get-all-accounts userid)))))))
;;
;; VIEWS
;;

(defn change-current-account [params] 
 (println "trying to put into the session " (params "account-list"))
 (session-put! "current-user" (params "account-list"))
  (redirect "/"))

(forms/defform change-account-form "/account/change"
  :fields [(forms/hidden :key)
	   (forms/select :account-list
                         (db/get-all-accounts)
                         {:id :value :prompt {"" "Select an Account"}})]
  :on-cancel "/"
  :on-success #(do
		 (session-put! "current-user" %)
		 (redirect "/"))
)


(forms/defform account-form "/account/edit"
  :fields [(forms/hidden :key)
           (forms/textfield "Account Name" :account-name)]
  :on-cancel "/"
  :on-success #(do
		 (db/save-account %)
		 (flash-put! :account-message "New account has been saved.")
		 "/")
  :load #(db/get-account %))

(forms/defform ledger-form "/ledger/edit"
  :fields [(forms/hidden :key)
	   (forms/hidden :userid)
	   (forms/hidden :account-name)
           (forms/textfield "Date:" :date)
	   (forms/textfield "Earned/Spent:" :earned-spent)
	   (forms/textfield "Amount:" :amount)
      	   (forms/textfield "Reason" :reason)]
  :on-cancel "/"
  :on-success #(do
		 (db/save-ledger-entry %)
		 (println "about to redirect to home j")
;		 "/")
		 (flash-put! :ledger-message "New ledger entry has been saved.")
		 "/")
  :load #(db/get-ledger-entry %))

(defroutes public-routes
  (account-form (fn [request form] (views/layout form)))
  (ledger-form (fn [request form] (views/layout form)))
  (change-account-form (fn [request form] (views/layout form)))
  (GET "/" [] (do
	       (println "getting /")
	       (views/home)))
;  (POST  "/edit" {params :params}
;	 (let [selected (params "ledger-list-box")
;	       button (params "ledger-crud-button")]
;	   (if (= button "Delete")
;	     (delete-ledger-entry-from-params selected)
;	     (render-page "Update Ledger Entry" (edit-form selected)))))
  (POST "/changecurrentaccount" {params :params} ( change-current-account params))
;  (GET "/dumpdata" [] (dump-data))
  (route/not-found "Not Found"))

(def app
    (-> public-routes
;        (with-security authenticate)
        (wrap-stateful-session)))

(defservice app)