(ns photoshack.ui.controls)

(defn handle-change [state prop event]
  (.log js/console "change" (str prop) (-> event .-target .-value))
  (reset!
   state
   (swap! state assoc-in [:editor prop] (int (-> event .-target .-value)))))

(defn input-range [state prop]
  [:input {:max 100
           :min -100
           :on-mouse-up #(handle-change state prop %)
           :step 1
           :type "range"}])

(defn controls [state]
  [:ul {:class "side-nav fixed"}
   [:li
    [:label "Brightness" [input-range state :brightness]]]
   [:li
    [:label "Contrast" [input-range state :contrast]]]])
