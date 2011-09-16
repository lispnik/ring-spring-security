(defproject ring-spring-security "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [ring/ring-core "0.3.11"]
                 [ring/ring-jetty-adapter "0.3.11"]
                 [org.springframework.security/spring-security-web "3.0.5.RELEASE"]
                 [org.springframework.security/spring-security-config "3.0.5.RELEASE"]
                 [hiccup "0.3.6"]
                 [compojure "0.6.4"]
                 [org.slf4j/slf4j-api "1.6.1"]
                 [org.slf4j/jcl-over-slf4j "1.6.1"]
                 [ch.qos.logback/logback-classic "0.9.29"]]
  :exclusions [commons-logging/commons-logging]
  :dev-dependencies [[ring/ring-devel "0.3.11"]])