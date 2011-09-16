
(ns ring-spring-security.core
  (:require [ring.util.response :as response])
  (:use ring.util.servlet
        ring.middleware.reload
        ring.middleware.stacktrace
        ring.handler.dump)
  (:use compojure.core)
  (:require [compojure.route :as route]
            [compojure.handler :as handler])
  (:require [ring.adapter.jetty :as jetty])
  (:use hiccup.core)
  (:use hiccup.page-helpers))

(declare index-html example-html)

(defroutes main-routes
  (GET "/"      req (index-html req))
  (GET "/admin" req (example-html req))
  (GET "/user"  req (example-html req))
  (GET "/other" req (example-html req))
  (route/resources "/")
  (route/not-found "Page not found"))

(defn application-context
  ([req]
     (org.springframework.web.context.support.WebApplicationContextUtils/getWebApplicationContext
      (:servlet-context req)))
  ([req name]
     (org.springframework.web.context.support.WebApplicationContextUtils/getWebApplicationContext
      (:servlet-context req) name)))

(defn security-context []
  (org.springframework.security.core.context.SecurityContextHolder/getContext))

(def app
  (-> (handler/site main-routes)
      (wrap-reload '(ring-spring-security.core))
      (wrap-stacktrace)))

(defn layout [& contents]
  (html5
   [:head [:title "Spring Security with Clojure, Ring and Compojure"]
    (include-css "/default.css")]
   [:body
    [:p (link-to "/j_spring_security_logout" "Logout")]
    contents]))

(defn example-html [req]
  (layout
    [:dl
     [:dt "Application Context"]
     [:dd (escape-html (application-context req))]
     [:dt "Security Context"]
     [:dd (escape-html (security-context))]]))

(defn index-html [req]
  (layout
    [:p "Welcome, here you can try:"]
    [:ul
     [:li (link-to "/admin" "/admin")]
     [:li (link-to "/user" "/user")]
     [:li (link-to "/other" "/other")]]))

(defn- boot-spring
  "Initialize a Jetty server for Spring and also Spring Security"
  ([server handler context-config-location]
    (let [filter (doto (org.mortbay.jetty.servlet.FilterHolder. org.springframework.web.filter.DelegatingFilterProxy)
            (.setName "springSecurityFilterChain"))
          servlet (doto (org.mortbay.jetty.servlet.ServletHolder. (ring.util.servlet/servlet handler))
            (.setName "default"))
          context (doto (org.mortbay.jetty.servlet.Context. server "/"
                          (bit-or org.mortbay.jetty.servlet.Context/SESSIONS
                            org.mortbay.jetty.servlet.Context/SECURITY))
            (.addFilter filter "/*" 0)
            (.addServlet servlet "/")
            (.addEventListener (org.springframework.web.context.ContextLoaderListener.)))]
      (when context-config-location
        (.setInitParams context {"contextConfigLocation" context-config-location}))
      (.addHandler server context)))
  ([server handler]
    (boot-spring server handler nil)))

(defn boot [join?]
  (jetty/run-jetty #'app
    {:port 9090
     :join? join?
     :configurator (fn [server] (boot-spring server #'app "classpath:security.xml"))
     :default-handler? false}))

(comment
  (def jetty (boot true))
  (.stop jetty))
