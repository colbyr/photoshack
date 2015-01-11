(ns photoshack.ui.canvas
  (:require [photoshack.utils.debounce :refer [debounce]]
            [reagent.core :as r]))

(def caman-render!
  (debounce (fn [caman state]
    (.revert caman false)
    (.brightness caman (:brightness state))
    (.contrast caman (:contrast state))
    (.render caman)) 100))

(defn- create-editor! [this]
  (let [caman (js/Caman "#editor" "img/kitteh.png")]
    (.log js/console "create editor" caman)
    (caman-render! caman (-> this r/props :editor-state :editor))
    (r/set-state this {:caman caman})))

(defn- update-editor! [this]
  (let [caman (-> this r/state :caman)
        props (-> this r/props :editor-state :editor)]
    (when-not (nil? caman)
      (caman-render! caman props))))

(defn- render [this]
  [:canvas {:id "editor"}])

(def canvas-component
  (r/create-class
   {:component-did-mount create-editor!
    :component-did-update update-editor!
    :get-initial-state (fn [] {:caman nil})
    :render render}))

(defn canvas [state]
  (fn []
    [:div {:style {:margin-bottom "15px"}}
     [:h2 (:name @state)]
     [canvas-component {:editor-state @state}]]))
