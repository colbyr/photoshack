(ns photoshack.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [photoshack.ui.canvas :refer [canvas]]
              [photoshack.ui.controls :refer [controls]])
    (:import goog.History))

;; -------------------------
;; Views

(def state (atom {:editor {:brightness 0
                           :contrast 0}
                  :name "kitteh.png"
                  :src "img/kitteh.png"}))

(defn home-page []
  [:div
   [controls state]
   [canvas state]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page home-page))

;; -------------------------
;; Initialize app
(defn init! []
  (reagent/render-component [current-page] (.getElementById js/document "app")))

;; -------------------------
;; History
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))
;; need to run this after routes have been defined
(hook-browser-navigation!)
