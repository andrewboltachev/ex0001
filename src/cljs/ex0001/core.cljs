(ns ex0001.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(enable-console-print!)

;; --- Helpers ---
(defn display [show]
  (if show
    #js {}
    #js {:display "none"}
    )
  )

;; --- Components ---
(defui Inbox
  Object
  (render [this]
          (dom/div nil "hello world")
          )
  )

(defui Categories
  Object
  (render [this]
          (dom/div nil "hello world")
          )
  )

(defui Calendar
  Object
  (render [this]
          (dom/div nil "hello world")
          )
  )

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

(def perspectives
  {:inbox Inbox
   :categories Categories
   :calendar Calendar
   :animals AnimalsList
   }
  )

(defui PerspectiveSwitch
  static om/IQuery
  (query [_]
         [:perspectives/list :perspectives/active]
         )

  Object
  (render [this]
          (let [{:keys [perspectives/active
                        perspectives/list
                        ]} (om/props this)]
            (println active list)
            (apply dom/ul nil
                    (map
                      (fn [perspective]
                        (dom/li #js {:style
                             (if (= (:key perspective) active)
                               #js {:backgroundColor "lime"}
                               #js {}
                               )
                             }
                                (dom/a #js {:href "javascript:void(0)"
                                            :onClick (fn [_] (om/transact! this `[(~'switch-perspective {:key ~(:key perspective)})]))
                                            }
                                   (:name perspective)
                                   )
                                )
                        )
                      list
                      )
                    )
            )
          )
  )


(defui App
  static om/IQuery
  (query [_]
          (concat
            (mapcat om/get-query
              (vals perspectives)
              )
            [
              :perspectives/list
              :perspectives/active
              ]
          )
         )
  Object
  (render [this]
          (let [{:keys [perspectives/active]} (om/props this)]
            (dom/div #js {:className "container-fluid"}
              (dom/div #js {:className "col-md-2"}
                    (dom/h1 nil "Tabs")
                       ((om/factory PerspectiveSwitch) (om/props this))
                    )
              (apply dom/div #js {:className "col-md-10"}
                    (dom/h1 nil (str
                                  "Hello world! "
                                  (name active)
                                  )
                                  )
                    (map
                      (fn [[k component]]
                        (dom/div #js {:style (display (= active k))}
                          ((om/factory component) (om/props this))
                          )
                        )
                        perspectives
                      )
                    )
                  )
            )
    )
  )

;; --- State ---

(def app-state
  (atom
    {:selected/animal nil
     :app/title "Animals"
     :animals/list
     [[1 "Ant"] [2 "Antelope"] [3 "Bird"] [4 "Cat"] [5 "Dog"]
      [6 "Lion"] [7 "Mouse"] [8 "Monkey"] [9 "Snake"] [10 "Zebra"]]
     :perspectives/list 
     [
      {:key :inbox
        :name "Inbox"
        }
      {:key :categories
        :name "Categories"
        }
      {:key :calendar
        :name "Calendar"
        }
      {:key :animals
        :name "Animals"
        }
      ]
    :perspectives/active :categories
     }))

;; --- Parser, reconciler and root ---

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


(defmethod mutatef 'switch-perspective
  [{:keys [state] :as env} _ {:keys [key]}]
  (println key)
  {:value {:keys [
                  ]}
   :action
   (fn []
     (swap! state update-in [:perspectives/active] (fn [_] key))
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

(om/add-root! reconciler
  App (gdom/getElement "app"))
