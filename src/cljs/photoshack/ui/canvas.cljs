(ns photoshack.ui.canvas
  (:require [reagent.core :as r]))

(def caman-instance (r/atom nil))
(def is-processing (r/atom false))

(defn debounce [func wait immediate]
  (let [timeout (atom nil)]
    (fn []
      (this-as this
               (let [context this
                     args js/arguments
                     later (fn []
                             (reset! timeout nil)
                             (when-not immediate
                               (.apply func context args)))]
                 (if (and immediate (not @timeout))
                   (.apply func context args))
                 (js/clearTimeout @timeout)
                 (reset! timeout (js/setTimeout later wait)))))))

(def update-caman
  (debounce (fn [caman state]
    (reset! is-processing true)
    (.revert caman)
    (.brightness caman (:brightness state))
    (.contrast caman (:contrast state))
    (.render caman (fn [] (reset! is-processing false)))) 100))

(defn handle-create-editor [editor-state]
  (js/Caman "#editor" "img/kitteh.png"
   (fn []
     (this-as
      caman
      (let []
        (reset! caman-instance caman)
        (update-caman caman editor-state))))))

(defn handle-update-editor [editor-state]
  (update-caman @caman-instance editor-state)
  false)

(defn render [state]
  (let [render-caman
        (with-meta
          (fn [] [:canvas {:id "editor" :data-update-hook (:editor @state)}])
          {:component-did-mount #(handle-create-editor (:editor @state))
           :should-component-update #(handle-update-editor (:editor @state))})]
    (fn []
      [:div {:style {:margin-bottom "15px"}}
       [:h2 (:name @state)]
       [render-caman]])))
