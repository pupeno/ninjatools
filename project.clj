;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(defproject ninjatools "0.1.0-SNAPSHOT"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [selmer "0.8.7"]
                 [com.taoensso/timbre "4.0.2"]
                 [com.taoensso/tower "3.0.2"]
                 [markdown-clj "0.9.67"]
                 [environ "1.0.0"]
                 [compojure "1.4.0"]
                 [ring-webjars "0.1.1"]
                 [ring/ring-defaults "0.1.5"]
                 [ring-ttl-session "0.1.1"]
                 [ring "1.4.0"
                  :exclusions [ring/ring-jetty-adapter]]
                 [metosin/ring-middleware-format "0.6.0"]
                 [metosin/ring-http-response "0.6.3"]
                 [bouncer "0.3.3"]
                 [prone "0.8.2"]
                 [org.clojure/tools.nrepl "0.2.10"]
                 [org.webjars/bootstrap "3.3.5"]
                 [org.webjars/jquery "2.1.4"]
                 [buddy "0.6.0"]
                 [migratus "0.8.2"]
                 [clj-dbcp "0.8.1"]
                 [yesql "0.5.0-rc3"]
                 [instaparse "1.4.1"]
                 [org.clojure/java.jdbc "0.4.1"]
                 [to-jdbc-uri "0.2.0"]
                 [org.postgresql/postgresql "9.3-1102-jdbc41"]
                 [org.clojure/clojurescript "1.7.10" :scope "provided"]
                 [org.clojure/tools.reader "0.9.2"]
                 [reagent "0.5.0"]
                 [cljsjs/react "0.13.3-1"]
                 [reagent-forms "0.5.4"]
                 [reagent-utils "0.1.5"]
                 [secretary "1.2.3"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [cljs-ajax "0.3.14"]
                 [metosin/compojure-api "0.22.0"]
                 [metosin/ring-swagger-ui "2.1.1-M2"]
                 [org.immutant/web "2.0.2"]]

  :min-lein-version "2.0.0"
  :uberjar-name "ninjatools.jar"
  :jvm-opts ["-server"]

  :main ninjatools.core
  :migratus {:store :database}

  :plugins [[lein-environ "1.0.0"]
            [lein-ancient "0.6.5"]
            [migratus-lein "0.1.5"]
            [lein-cljsbuild "1.0.6"]]
  :clean-targets ^{:protect false} [:target-path [:cljsbuild :builds :app :compiler :output-dir] [:cljsbuild :builds :app :compiler :output-to]]
  :cljsbuild {:builds {:app {:source-paths ["src-cljs"]
                             :compiler     {:output-dir    "resources/public/js/out"
                                            :externs       ["react/externs/react.js"]
                                            :optimizations :none
                                            :output-to     "resources/public/js/app.js"
                                            :pretty-print  true}}}}

  :profiles {:uberjar       {:omit-source true
                             :env         {:production true}
                             :hooks       [leiningen.cljsbuild]
                             :cljsbuild   {:jar    true
                                           :builds {:app {:source-paths ["env/prod/cljs"]
                                                          :compiler     {:optimizations :advanced :pretty-print false}}}}

                             :aot         :all}
             :dev           [:project/dev :profiles/dev]
             :test          [:project/test :profiles/test]
             :project/dev   {:dependencies [[ring/ring-mock "0.2.0"]
                                            [ring/ring-devel "1.4.0"]
                                            [pjstadig/humane-test-output "0.7.0"]
                                            [org.clojure/tools.nrepl "0.2.10"]
                                            [lein-figwheel "0.3.7"]
                                            [mvxcvi/puget "0.8.1"]]
                             :plugins      [[lein-figwheel "0.3.7"]]
                             :cljsbuild    {:builds {:app {:source-paths ["env/dev/cljs"] :compiler {:source-map true}}}}
                             :figwheel     {:http-server-root "public"
                                            :server-port      3449
                                            :nrepl-port       7002
                                            :css-dirs         ["resources/public/css"]
                                            :ring-handler     ninjatools.handler/app}
                             :repl-options {:init-ns ninjatools.core}
                             :injections   [(require 'pjstadig.humane-test-output)
                                            (pjstadig.humane-test-output/activate!)]
                             ;;when :nrepl-port is set the application starts the nREPL server on load
                             :env          {:dev        true
                                            :port       3000
                                            :nrepl-port 7000}}
             :project/test  {:env {:test       true
                                   :port       3001
                                   :nrepl-port 7001}}
             :profiles/dev  {}
             :profiles/test {}})
