(ns photoshack.ui.canvas
  (:require [photoshack.utils.debounce :refer [debounce]]
            [reagent.core :as r]))

(defn- get-ratio [metrics]
  (let [{:keys [img-height img-width client-height client-width]} metrics]
    (if (or (> img-width client-width) (> img-height client-height))
      (/ (min client-height client-width) (min img-height img-width))
      1)))

(defn- get-relative-size [metrics]
  (let [{:keys [img-height img-width]} metrics
        ratio (get-ratio metrics)]
    {:height (* img-height ratio)
     :width (* img-width ratio)}))

(defn- caman-set! [caman pair]
  (js-invoke
   caman
   (name (first pair))
   (second pair)))

(def caman-render!
  (debounce
   (fn [this caman state]
    (let [size (get-relative-size (r/state this))]
      (if (= (.-height caman) (:height size))
        (.revert caman false)
        (let []
          (.reset caman)
          (.resize caman (clj->js size))))
      (mapv (partial caman-set! caman) state)
      (.render caman)))
   100
   false))

(defn- get-image-metrics [this]
  (let [image (js/Image.)]
    (set!
     (.-onload image)
     (fn [] (r/set-state this {:img-height (.-height image)
                               :img-width (.-width image)})))
    (set! (.-src image) (-> this r/props :editor-state :src))))

(defn- get-height [this]
  (-> this r/dom-node .-clientHeight))

(defn- get-width [this]
  (-> this r/dom-node .-clientWidth))

(def resize!
  (debounce
   (fn [this]
    (r/set-state this {:client-height (get-height this)
                       :client-width (get-width this)}))
   100
   false))

(defn- create-editor! [this]
  (let [caman (js/Caman "#editor" (-> this r/props :editor-state :src))]
    (set! (.-onresize js/window) (partial resize! this))
    (caman-render! this caman (-> this r/props :editor-state :editor))
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
        (caman-render! this caman props)
        (create-editor! this)))))

(defn- is-ready [this]
  (and (-> this r/state :caman)
       (-> this r/state :client-height)
       (-> this r/state :img-height)))

(def container-style
  {:style {:height "100%"
           :position "fixed"
           :top 0
           :left 240
           :bottom 0
           :right 0}})

(defn- render [this]
  (let [ready (is-ready this)]
    [:div container-style
     (when-not ready [:div "loading..."])
     [:canvas {:id "editor"
               :style {:display (if ready "block" "none")
                       :position "relative"
                       :transform "translate(-50%,-50%)"
                       :left "50%"
                       :top "50%"}}]]))

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
