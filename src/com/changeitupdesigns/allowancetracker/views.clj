(ns com.changeitupdesigns.allowancetracker.views
  "Common views which are used by all of the form examples."
  (:use [hiccup.core :only [html]]
        [hiccup.page-helpers :only [doctype link-to]]
        [sandbar.stateful-session :only [flash-get
					 session-get]]
        [sandbar.core :only [icon stylesheet]])
  (:require [com.changeitupdesigns.allowancetracker.db :as db]
	    [appengine.datastore.core :as ds]))

(defn layout [content]
  (html
   (doctype :html4)
   [:html
    [:head
     (stylesheet "sandbar.css")
     (stylesheet "sandbar-forms.css")
     (icon "icon.png")]
    [:body
     (println "skipping commented out flash message inside the bod tag")
     (if-let [m (flash-get :user-message)] [:div {:class "message"} m])
     [:h2 "Allowance Tracker Sandbar Form Example"]
     content]]))
     
(defn home []
  (println "inside of views/home")
  (let [userid (db/get-userid)]
    (layout
     (if userid
       [:div
	[:ul
	  [:li "Logged in as " userid]
	  [:li (link-to (.createLogoutURL (:user-service (db/get-user-info)) "/") "Logout")]
	  [:li "current account:" (session-get "current-user")]
	  [:li (link-to "/account/edit" "Add New Account")]
	  [:li (link-to "/ledger/edit" "Add New Ledger Entry")]]
	[:table
	 [:tr
	  [:th "Account Name:"]
	  [:th ""]
	  [:th ""]]
	 (map #(let [{:keys [account-name key]} %]
	       [:tr
		[:td account-name]
		[:td (link-to (str "/account/edit/" (ds/key->string key)) "Edit")]
		[:td (link-to "/account/change/" "Set Current")]])
	    (db/get-all-accounts))]
	[:table
	 [:tr
	  [:th "Date:"]
	  [:th "Earned/Spent:"]
	  [:th "Amount:"]
	  [:th "Description:"]
	  [:th ""]]
	 (map #(let [{:keys [date earned-spent amount reason user key]} %]
	       [:tr
		[:td date]
		[:td earned-spent]
		[:td amount]
		[:td reason]
		[:td (link-to (str "/ledger/edit/" (ds/key->string key)) "Edit")]])
	    (db/get-ledger-entries-by-account "mike"))]]
       [:ul
	[:li "Not logged in"]
	[:li (link-to (.createLoginURL (:user-service (db/get-user-info)) "/") "Login")]]))))


;(defn dump-data []
;  (render-page "Here's a dump of the data in the data store"
;    (map #(html [:br] (str %) (:key %)) (db/get-all-ledger-entries))))
