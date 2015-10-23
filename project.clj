;;;; Copyright © 2015 Carousel Apps, Ltd. All rights reserved.

(defproject ninjatools "0.1.0-SNAPSHOT"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [selmer "0.9.2"]
                 [com.taoensso/timbre "4.1.4"]
                 [com.taoensso/tower "3.0.2"]
                 [markdown-clj "0.9.74"]
                 [environ "1.0.1"]
                 [compojure "1.4.0"]
                 [ring-webjars "0.1.1"]
                 [ring/ring-defaults "0.1.5"]
                 [jdbc-ring-session "0.3"]
                 [ring "1.4.0" :exclusions [ring/ring-jetty-adapter]]
                 [metosin/ring-middleware-format "0.6.0"]
                 [metosin/ring-http-response "0.6.5"]
                 [bouncer "0.3.3"]
                 [prone "0.8.2"]
                 [org.clojure/tools.nrepl "0.2.11"]
                 [org.webjars/jquery "2.1.4"]
                 [org.webjars/bootstrap "3.3.5"]
                 [buddy "0.7.2"]
                 [migratus "0.8.6"]
                 [clojurewerkz/mailer "1.2.0"]
                 [com.novemberain/validateur "2.4.2"]
                 [conman "0.2.0"]
                 [to-jdbc-uri "0.3.0"]
                 [org.postgresql/postgresql "9.3-1102-jdbc41"]
                 [org.clojure/clojurescript "1.7.48"]
                 [org.clojure/tools.reader "0.9.2"]
                 [reagent "0.5.1" :exclusions [cljsjs/react]]
                 [cljsjs/react-with-addons "0.14.0-0"]
                 [com.domkm/silk "0.1.1"]
                 [kibu/pushy "0.3.6"]
                 [com.carouselapps/re-frame "0.4.1"]
                 [com.carouselapps/free-form "0.1.1"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [cljs-ajax "0.5.0"]
                 [metosin/compojure-api "0.23.1"]
                 [metosin/ring-swagger-ui "2.1.3"]
                 [org.immutant/web "2.1.0"]
                 [com.carouselapps/prerenderer "0.1.0"]
                 [yeller-clojure-client "1.4.2"]
                 [com.lucasbradstreet/cljs-uuid-utils "1.0.2"]]

  :min-lein-version "2.0.0"
  :uberjar-name "ninjatools.jar"
  :jvm-opts ["-server"]
  :main ninjatools.core
  :migratus {:store :database}
  :plugins [[lein-environ "1.0.0"]
            [lein-ancient "0.6.5"]
            [migratus-lein "0.1.5"]
            [lein-cljsbuild "1.0.6"]
            [lein-npm "0.6.1"]]
  :clean-targets ^{:protect false} [:target-path [:clj :cljsbuild :builds :app :compiler :output-dir] [:clj :cljsbuild :builds :app :compiler :output-to]]
  :source-paths ["src/clj" "src/cljc"]
  :test-paths ["test/clj"]
  :cljsbuild {:builds {:app         {:source-paths ["src/cljs" "src/cljc"]
                                     :compiler     {:output-dir "resources/public/js/app"
                                                    :output-to  "resources/public/js/app.js"
                                                    :externs    ["react/externs/react.js"]}}
                       :server-side {:source-paths ["src/cljs" "src/cljc" "src/node"]
                                     :compiler     {:output-dir "target/js/server-side"
                                                    :output-to  "target/js/server-side.js"
                                                    :externs    ["react/externs/react.js"]
                                                    :main       "ninjatools.node"
                                                    :target     :nodejs}}}}


  :profiles {:uberjar       {:omit-source true
                             :env         {:production true}
                             :hooks       [leiningen.cljsbuild]
                             :prep-tasks  ["deps" "javac" "compile"]
                             :cljsbuild   {:jar    true
                                           :builds {:app         {:source-paths ["env/prod/cljs"]
                                                                  :compiler     {:optimizations :advanced
                                                                                 :pretty-print  false}}
                                                    :server-side {:compiler {:optimizations :none
                                                                             :source-map    true
                                                                             :pretty-print  true}}}}
                             :aot         :all}
             :dev           [:project/dev :profiles/dev]
             :test          [:project/test :profiles/test]
             :project/dev   {:dependencies [[ring/ring-mock "0.3.0"]
                                            [ring/ring-devel "1.4.0"]
                                            [pjstadig/humane-test-output "0.7.0"]
                                            [org.clojure/tools.nrepl "0.2.11"]
                                            [lein-figwheel "0.4.0"]
                                            [mvxcvi/puget "0.8.1"]]
                             :plugins      [[lein-figwheel "0.3.7"]]
                             :cljsbuild    {:builds {:app         {:source-paths ["env/dev/cljs"]
                                                                   :compiler     {:optimizations :none
                                                                                  :source-map    true
                                                                                  :pretty-print  true
                                                                                  :verbose       true}}
                                                     :server-side {:compiler {:optimizations :none
                                                                              :source-map    true
                                                                              :pretty-print  true
                                                                              :verbose       true}}}}
                             :figwheel     {:http-server-root "public"
                                            :server-port      3449
                                            :nrepl-port       7002
                                            :css-dirs         ["resources/public/css"]
                                            :ring-handler     ninjatools.handler/app
                                            :server-logfile   "tmp/logs/figwheel-logfile.log"}
                             :repl-options {:init-ns ninjatools.core}
                             :injections   [(require 'pjstadig.humane-test-output)
                                            (pjstadig.humane-test-output/activate!)]
                             ;;when :nrepl-port is set the application starts the nREPL server on load
                             :env          {:environment "dev"
                                            :dev         true
                                            :port        3000
                                            :nrepl-port  7000}}
             :project/test  {:env {:environment "test"
                                   :test        true
                                   :port        3001
                                   :nrepl-port  7001}}
             :profiles/dev  {}
             :profiles/test {}})
