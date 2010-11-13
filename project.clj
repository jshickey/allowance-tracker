(defproject allowance-tracker "0.1.0"
  :description "Simple Clojure app for using Compojure on Google App Engine"
 
  ; namespaces will drive what get compiled AOT, required for deploying to Google App Engine
  :namespaces [com.changeitupdesigns.allowancetracker.core]

  :dependencies [[org.clojure/clojure "1.2.0"]
		 [org.clojure/clojure-contrib "1.2.0"]
		 [compojure "0.5.0"]
                 [ring/ring "0.2.5"]
                 [hiccup "0.2.6"]
                 [appengine "0.2"]
                 [scriptjure "0.1.17"]
		 [sandbar/sandbar "0.3.0-SNAPSHOT"]
                 [com.google.appengine/appengine-api-1.0-sdk "1.3.4"]
                 [com.google.appengine/appengine-api-labs "1.3.4"]
		 [clj-time "0.1.0-RC1"]]

  :dev-dependencies [[swank-clojure "1.2.0"]
		     [jline "0.9.94"]
                     [ring/ring-jetty-adapter "0.2.5"]
                     [com.google.appengine/appengine-local-runtime "1.3.4"]
                     [com.google.appengine/appengine-api-stubs "1.3.4"]
		     [mycroft/mycroft "0.0.2"]]

 ; :repositories [["maven-gae-plugin" "http://maven-gae-plugin.googlecode.com/svn/repository"]]

  :compile-path "war/WEB-INF/classes"
  :library-path "war/WEB-INF/lib")
