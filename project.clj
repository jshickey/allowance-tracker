(defproject allowance-tracker "0.1.0"
  :description "Simple Clojure app for using Compojure on Google App Engine"
 
  ; namespaces will drive what get compiled AOT, required for deploying to Google App Engine
  :namespaces [com.changeitupdesign.allowancetracker.core]

  :dependencies [[compojure "0.4.1"]
                 [ring/ring "0.2.5"]
                 [hiccup "0.2.4"]
                 [appengine "0.2"]
		 [sandbar/sandbar-session "0.2.3"]
                 [com.google.appengine/appengine-api-1.0-sdk "1.3.4"]
                 [com.google.appengine/appengine-api-labs "1.3.4"]]

  :dev-dependencies [[swank-clojure "1.2.0"]
                     [ring/ring-jetty-adapter "0.2.5"]
                     [com.google.appengine/appengine-local-runtime "1.3.4"]
                     [com.google.appengine/appengine-api-stubs "1.3.4"]]

  :repositories [["maven-gae-plugin" "http://maven-gae-plugin.googlecode.com/svn/repository"]]

  :compile-path "war/WEB-INF/classes"
  :library-path "war/WEB-INF/lib")