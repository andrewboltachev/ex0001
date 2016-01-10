(ns ex0001.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(enable-console-print!)


(def app-state
  (atom
    {:selected/animal nil
     :app/title "Animals"
     :animals/list
     [[1 "Ant"] [2 "Antelope"] [3 "Bird"] [4 "Cat"] [5 "Dog"]
      [6 "Lion"] [7 "Mouse"] [8 "Monkey"] [9 "Snake"] [10 "Zebra"]]}))

(defui AnimalsList
  static om/IQuery
  (query [this]
         '[:app/title (:animals/list) :selected/animal])

  Object
  (render [this]
    (let [{:keys [app/title animals/list selected/animal]} (om/props this)]
      (dom/div nil
        (dom/h2 nil title)
        (apply dom/ul nil
          (map
            (fn [[i name]]
                (dom/li nil
                        (dom/span #js {:style
                             (if (= animal i)
                               #js {:backgroundColor "lime"}
                               #js {}
                               )
                             } (str i ". " name))
                        (dom/button #js {:onClick (fn [_]
                                                    (om/transact! this `[(select/animal {:id ~i})])
                                                    )} "Select")
                        ))
            list)))))
  )


(defn readf
  [{:keys [state] :as env} key params]
  (let [st @state]
    (if-let [[_ v] (find st key)]
      {:value v}
      {:value :not-found})))

(defmulti mutatef om/dispatch)

(defmethod mutatef 'select/animal
  [{:keys [state] :as env} _ {:keys [id]}]
  {:value {:keys [
                  ;:selected/animal
                  ]}
   :action
   (fn []
     (swap! state update-in [:selected/animal] (fn [_] id))
     )
   }
  )


(def my-parser (om/parser {:read readf
                           :mutate mutatef
                           }))

(def reconciler
  (om/reconciler
    {:state app-state
     :parser my-parser}))

(def hello (om/factory AnimalsList))


(defui App
  Object
  (render [this]
          (dom/div #js {:className "container-fluid"}
                   (dom/h1 nil "Hello world!")
                   ((om/factory AnimalsList)
                    (om/props this)
                    )
                   )
    )
  )

(om/add-root! reconciler
  App (gdom/getElement "app"))
