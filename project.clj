(defproject ex0001 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :jvm-opts ^:replace ["-Xmx1g" "-server"]

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [figwheel-sidecar "0.5.0-3" :scope "test"]
                 [org.omcljs/om "1.0.0-alpha22"]
                 [ring "1.4.0"]
                 [compojure "1.4.0"]
                 [com.datomic/datomic-free "0.9.5344" :exclusions [joda-time]]]

  :plugins [[lein-cljsbuild "1.1.2"]
            [lein-figwheel "0.5.0-3"]]


  :source-paths ["src/clj" "src/cljs"]
  :resource-paths ["resources"]
  :clean-targets ^{:protect false} ["resources/public/js/out"
                                    "resources/public/js/main.js"]

  :figwheel {:ring-handler ex0001.core/handler}

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src/clj" "src/cljs"]
                        :figwheel true
                        :compiler {:output-to "resources/public/js/main.js"
                                   :output-dir "resources/public/js/out"
                                   :main ex0001.core
                                   :asset-path "js/out"
                                   :optimizations :none
                                   :source-map true}}]})
