(ns ex0001.core
 ; (:require-macros [devcards.core :refer [defcard deftest dom-node]])
  (:require [goog.dom :as gdom]
            [cljs.test :refer-macros [is async]]
            [cljs.pprint :as pprint]
            ;[om.devcards.utils :as utils]
            ;[om.devcards.tutorials]
            ;[om.devcards.bugs]
            ;[om.devcards.autocomplete]
            ;[om.devcards.shared-fn-test]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(enable-console-print!)


;; -----------------------------------------------------------------------------
;; Recursive Query Syntax with Mutations

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

(defmulti norm-tree-read om/dispatch)

(defmethod norm-tree-read :tree
  [{:keys [state query] :as env} _ _]
  (let [st @state]
    {:value (om/db->tree query (:tree st) st)}))

(defmethod norm-tree-read :node/by-id
  [{:keys [state query query-root]} _ _]
  {:value (om/db->tree query query-root @state)})

(defmulti norm-tree-mutate om/dispatch)

(defmethod norm-tree-mutate 'tree/increment
  [{:keys [state]} _ {:keys [id]}]
  {:action
   (fn []
     (swap! state update-in [:node/by-id id :node-value] inc))})

(defmethod norm-tree-mutate 'tree/decrement
  [{:keys [state]} _ {:keys [id]}]
  {:action
   (fn []
     (swap! state update-in [:node/by-id id :node-value]
       (fn [n] (max 0 (dec n)))))})

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

(defui NormTree
  static om/IQuery
  (query [this]
    [{:tree (om/get-query NormNode)}])
  Object
  (render [this]
    (let [{:keys [tree]} (om/props this)]
      (dom/ul nil
        (norm-node tree)))))

(def norm-tree-parser
  (om/parser {:read   norm-tree-read
              :mutate norm-tree-mutate}))

(def norm-tree-reconciler
  (om/reconciler
    {:state   norm-tree-data
     :parser  norm-tree-parser
     :pathopt true}))


(defui NormRoot
  Object
  (render [this]
          (dom/div nil
                   ((om/factory NormTree)
                    (om/props this)
                    )
                   )
          )
  )

(om/add-root! norm-tree-reconciler NormRoot
              (gdom/getElement "app")
              )

