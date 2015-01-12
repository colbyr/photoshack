(ns photoshack.ui.canvas
  (:require [photoshack.utils.debounce :refer [debounce]]
            [reagent.core :as r]))

(def caman-render!
  (debounce (fn [caman state]
    (.revert caman false)
    (.brightness caman (:brightness state))
    (.contrast caman (:contrast state))
    (.render caman)) 100))

(defn- get-image-metrics [this]
  (let [image (js/Image.)]
    (.log js/console "getting image metrics")
    (set!
     (.-onload image)
     (fn [] (r/set-state this {:img-height (.-height image)
                               :img-width (.-width image)})))
    (set! (.-src image) (-> this r/props :editor-state :src))))

(defn- get-height [this]
  (-> this r/dom-node .-clientHeight))

(defn- get-width [this]
  (-> this r/dom-node .-clientWidth))

(defn resize! [this]
  (.log js/console "resize!" (r/dom-node this))
  (-> this r/set-state {:client-height (get-height this)
                        :client-width (get-width this)}))

(defn- create-editor! [this]
  (let [caman (js/Caman "#editor" (-> this r/props :editor-state :src))]
    (set! (.-onresize js/window) (partial resize! this))
    (caman-render! caman (-> this r/props :editor-state :editor))
    (get-image-metrics this)
    (r/set-state this {:caman caman
                       :client-height (get-height this)
                       :client-width (get-width this)})))

(defn- update-editor! [this]
  (let [caman (-> this r/state :caman)
        props (-> this r/props :editor-state :editor)
        src (-> this r/props :editor-state :src)]
    (when-not (nil? caman)
      (if (= (.-imageUrl caman) src)
        (caman-render! caman props)))))

(defn- is-ready [this]
  (and (-> this r/state :caman)
       (-> this r/state :client-height)
       (-> this r/state :img-height)))

(def container-style
  {:class "container"
   :style {:height "100%"
           :margin-top 30
           :margin-bottom 30}})

(defn- render [this]
  (let [ready (is-ready this)]
    (.log js/console "render" (-> this r/state str))
    [:div container-style
     (when-not ready [:div "loading..."])
     [:canvas {:id "editor"
               :style {:display (when-not ready "none")}}]]))

(def canvas-component
  (r/create-class
   {:component-did-mount create-editor!
    :component-did-update update-editor!
    :get-initial-state (fn [] {:caman nil
                               :client-height nil
                               :client-width nil
                               :img-width nil
                               :img-height nil})
    :render render}))

(defn canvas [state]
  [canvas-component {:editor-state @state}])
