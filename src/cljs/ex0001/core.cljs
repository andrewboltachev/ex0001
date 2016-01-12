(ns ex0001.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(enable-console-print!)






(defn f1 [])
(defn ^boolean f2 [])
(defn ^{:tag boolean1} f3 [])

(when (f1))
(when (f2))
(when (f3))













(def norm-tree-data
  {:tree {:id 0
          :node-value 1
          :children [{:id 1
                      :node-value 2
                      :children [{:id 2
                                  :node-value 3
                                  :children []}]}
                     {:id 3
                      :node-value 4
                      :children []}]}})

(declare norm-node)

(defn increment! [c id]
  (fn [e]
    (om/transact! c `[(tree/increment {:id ~id})])))

(defn decrement! [c id]
  (fn [e]
    (om/transact! c `[(tree/decrement {:id ~id})])))

(defui NormNode
  static om/Ident
  (ident [this {:keys [id]}]
    [:node/by-id id])
  static om/IQuery
  (query [this]
    '[:id :node-value {:children ...}])
  Object
  (render [this]
    (let [{:keys [id node-value children]} (om/props this)]
      (dom/li nil
        (dom/div nil
          (dom/label nil (str "Node value:" node-value))
          (dom/button #js {:onClick (increment! this id)} "+")
          (dom/button #js {:onClick (decrement! this id)} "-"))
        (dom/ul nil
          (map norm-node children))))))

(def norm-node (om/factory NormNode))


(defui PerspectiveItem
  static om/Ident
  (ident [this props]
         [:perspective/by-key (-> props :perspective :key)]
         )

  static om/IQuery
  (query [this]
         [{:perspective [:key :name]} :active
          :perspectives/active]
         )

  Object
  (render [this]
          (println (om/props this))
    #_(dom/li nil
            (:name (om/props this))
            )


          (let [{:keys [perspective active]} (om/props this)]
(dom/li #js {:style
                             (if (= (:key perspective) active)
                               #js {:backgroundColor "lime"}
                               #js {}
                               )
                             }
                                (dom/a #js {:href "javascript:void(0)"
                                            :onClick (fn [_]
                                                       ;(om/transact! this `[(~'switch-perspective {:key ~(:key perspective)})])
                                                       )
                                            }
                                   (:name perspective)
                                   )
                                )
            )
            )
  )

(def perspective-item (om/factory PerspectiveItem))

(defui Inbox
  static om/IQuery
  (query [this]
    [
     {:tree (om/get-query NormNode)}
     :perspectives/list
     :perspectives/active
     ])
  Object
  (render [this]
          (dom/div nil
    (let [{:keys [tree perspectives/list perspectives/active]} (om/props this)]
      (println
        (om/props this)
        )
      (dom/div nil
      (dom/ul nil
        (norm-node tree))
      
      (dom/ul nil
        (map
          #(perspective-item {:perspective %
                              :active active})
          list
          ))
               )
      ))))

























;; --- Helpers ---
(defn display [show]
  (if show
    #js {}
    #js {:display "none"}
    )
  )

;; --- Components ---
(defui Inbox1
  Object
  (render [this]
          (dom/div nil "hello world")
          )
  )


(declare category-node)


(defui CategoryNode
  static om/Ident
  (ident [this category]
         [:category/by-id (:db/id category)]
         )

  static om/IQuery
  (query [_]
         '[:db/id :category/name {:category/_parent ...}]
         )

  Object
  (render [this]
          (let [props (om/props this)]
          (dom/li nil
                  (when-let [icon (:category/icon props)]
                    (dom/button #js {:className "btn btn-xs btn-link"
                           :style #js {:marginRight "3px"}
                                     }
                    (dom/span
                      #js {:className icon
                           }
                      )
                                )
                    )
                  (dom/span nil (:category/name props))
                  (dom/button #js {:style #js {:marginLeft "10px"}
                                   :className "btn btn-xs"
                                   :onClick (fn [_]
                                              (om/transact! this `[(~'reverse-name {:id ~(:db/id props)})])
                                              )
                                   }
                              (dom/span
                                #js {:className "glyphicon glyphicon-edit"}
                                )
                              )
                  (when (:category/_parent props)
                  (apply dom/ul nil
                         (map (fn [category]
                                (category-node
                                 category
                                 )
                                )
                          (:category/_parent props)
                          )
                          )
                    )
                   )
            )
          )
  )

(def category-node (om/factory CategoryNode))

(defui Categories
  static om/IQuery
  (query [_]
         [{:categories/list (om/get-query CategoryNode)}]
         )

  Object
  (render [this]
          (dom/div #js {:className "container-fluid"}
            (dom/div #js {:className "row"}
                    (dom/div #js {:className "col-md-4"}



                              (dom/span nil "Categories")
                              (apply dom/ul nil
                                      (map (fn [category] ((om/factory CategoryNode)
                                            category)
                                           )
                                           (:categories/list (om/props this))
                                           )
                                      )
                              )
                    (dom/div #js {:className "col-md-8"}
                              (dom/span nil "foo")
                              )
                    )
            )
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
          (vec
            (mapcat om/get-query
              (filter identity (conj
                (vec (vals perspectives))
                    PerspectiveSwitch
                    ;             nil
                      ))
              )
          )
         )
  Object
  (render [this]
          (println
            "props"
            (om/props this)
            )
          (let [{:keys [perspectives/active]} (om/props this)]
            (dom/div #js {:className "container-fluid"}
              (dom/div #js {:className "col-md-2"}
                    (dom/h1 nil "Tabs")
                       ;((om/factory PerspectiveSwitch) (om/props this))
                    )
              (apply dom/div #js {:className "col-md-10"}
                    (dom/h1 nil (str
                                  "Hello world! "
                                  ;(name active)
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

(def app-data
    {:tree (:tree norm-tree-data)
     :categories/in-dialog nil
     :categories/list [
                       {:db/id 1
                        :category/name "Компьютерные дела"
                        :category/icon "icon-computer"
                        :category/_parent [
                                           {
                                            :db/id 10
                                            :category/name "Работа"
                                            }
                                           {
                                            :db/id 11
                                            :category/name "Языковые"
                                            }
                                           {
                                            :db/id 12
                                            :category/name "Другие свои"
                                            }
                                           {
                                            :db/id 13
                                            :category/name "Прочие"
                                            }
                                           ]
                        }
                       {:db/id 2
                        :category/name "Домашние дела"
                        :category/icon "icon-home"
                        :category/_parent [
                                           {
                                            :db/id 20
                                            :category/icon "glyphicons-cleaning"
                                            :category/name "Приборка"
                                            }
                                           {
                                            :db/id 12
                                            :category/name "Другие свои"
                                            :foo :bar
                                            }
                                           ]

                        }
                       {:db/id 3
                        :category/icon "glyphicons-briefcase"
                        :category/name "ИП"
                        }
                       {:db/id 4
                        :category/icon "glyphicons-medicine"
                        :category/name "Медицина"
                        }
                       ]
     :selected/animal nil
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
    :perspectives/active :inbox
     })

;; --- Parser, reconciler and root ---

(defmulti readf om/dispatch)

(defmethod readf :default
  [{:keys [state] :as env} key params]
  (let [st @state]
    (if-let [[_ v] (find st key)]
      {:value v}
      {:value :not-found})))

(defmethod readf :tree
  [{:keys [state query] :as env} _ _]
  (let [st @state]
    {:value (om/db->tree query (:tree st) st)}))

(defmethod readf :node/by-id
  [{:keys [state query query-root]} _ _]
  {:value (om/db->tree query query-root @state)})

(defmulti mutatef om/dispatch)

(defmethod mutatef 'tree/increment
  [{:keys [state]} _ {:keys [id]}]
  {:action
   (fn []
     (swap! state update-in [:node/by-id id :node-value] inc))})

(defmethod mutatef 'tree/decrement
  [{:keys [state]} _ {:keys [id]}]
  {:action
   (fn []
     (swap! state update-in [:node/by-id id :node-value]
       (fn [n] (max 0 (dec n)))))})

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
  {:value {:keys [
                  ]}
   :action
   (fn []
     (swap! state update-in [:perspectives/active] (fn [_] key))
     )
   }
  )


(defmethod mutatef 'reverse-name
  [{:keys [state] :as env} _ {:keys [id]}]
  {:value {:keys [
                  ]}
   :action
   (fn []
     (swap! state update-in [:category/by-id id :category/name]
            ;#(str % "!")
            identity
            )
     )
   }
  )


(def my-parser (om/parser {:read readf
                           :mutate mutatef
                           }))

(def reconciler
  (om/reconciler
    {:state app-data
     :parser my-parser
     :pathopt true}))


(println (om/get-query App))

(om/add-root! reconciler
  App (gdom/getElement "app"))

(println
         @reconciler)
