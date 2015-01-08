(ns photoshack.ui.controls)

(defn handle-change [state prop event]
  (reset!
   state
   (swap! state assoc-in [:editor prop] (int (-> event .-target .-value)))))

(defn input-range [state prop]
  [:input {:max 100
           :min -100
           :on-change #(handle-change state prop %)
           :step 1
           :type "range"
           :value (get-in @state [:editor prop])}])

(defn render [state]
  [:div
   [:label "Brightness" [input-range state :brightness]]
   [:label "Contrast" [input-range state :contrast]]])
