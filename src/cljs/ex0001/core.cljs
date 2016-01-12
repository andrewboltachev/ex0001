(ns ex0001.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(enable-console-print!)



(defui App
  Object
  (render [this]
          (dom/div #js {:className "col-md-2"}
                   )
          (dom/div #js {:className "col-md-10"}
                   )
          )
  )


(defmulti readf om/dispatch)
(defmulti mutatef om/dispatch)


(def app-data)

(def parser
  (om/parser {:read readf :mutate mutatef}))

(def reconciler
  (om/reconciler
    {:state app-data
     :parser parser
     :pathopt true}
    )
  )

(om/add-root! reconciler App
              (gdom/getElement "app"))
