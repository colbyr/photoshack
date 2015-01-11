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
  (let [caman (js/Caman "#editor" (-> this r/props :editor-state :src))]
    (caman-render! caman (-> this r/props :editor-state :editor))
    (r/set-state this {:caman caman})))

(defn- update-editor! [this]
  (let [caman (-> this r/state :caman)
        props (-> this r/props :editor-state :editor)
        src (-> this r/props :editor-state :src)]
    (when-not (nil? caman)
      (if (= (.-imageUrl caman) src)
        (caman-render! caman props)))))

(defn- render [this]
  [:div {:class "container"}
   [:canvas {:id "editor"}]])

(def canvas-component
  (r/create-class
   {:component-did-mount create-editor!
    :component-did-update update-editor!
    :get-initial-state (fn [] {:caman nil})
    :render render}))

(defn canvas [state]
  [canvas-component {:editor-state @state}])
