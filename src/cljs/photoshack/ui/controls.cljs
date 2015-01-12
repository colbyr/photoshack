(ns photoshack.ui.controls
  (:require [clojure.string :refer [capitalize]]))

(defn handle-change [state prop event]
  (reset!
   state
   (swap! state assoc-in [:editor prop] (int (-> event .-target .-value)))))

(defn input-range [state prop]
  [:li
   [:label
   (-> prop name capitalize)
   [:input {:max 100
            :min -100
            :on-mouse-up #(handle-change state prop %)
            :step 1
            :type "range"}]]])

(def inputs [:brightness :contrast :saturation :vibrance :exposure])



(defn controls [state]
  [:ul {:class "side-nav fixed full"}
   (map (partial input-range state) inputs)])
