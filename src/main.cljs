(ns main
    (:require [reagent.core :as r] 
              [reagent.dom :as rd]
              [cljs.pprint :as pprint]))

;Game Variables

  ;Inner variables
  (def notification (r/atom nil))

  ;Stats
  (def funds (r/atom 0))
  (def total-funds (r/atom 0))
  (def click-power (r/atom 1))
  (def worker-power (r/atom 0))
  (def worker-upgrades (r/atom 1))
  (def click-upgrades (r/atom 1))
  (def ascension (r/atom 1))

  ;Prices
  (def price (r/atom 10))
  (def worker-price (r/atom 100))
  (def team-price (r/atom 1000))
  (def worker-upgrades-cost (r/atom 400))
  (def click-upgrades-cost (r/atom 40))

  
  
;Game Functions
  (defn round [n]
    (pprint/cl-format nil "~,2f" n))

  (defn max-ascensions-math [total-earned]
      (let [x (/ total-earned 1000000)
            a 0.55
            b 1.55
            c (- x)]
        (let [n (Math/floor (/ (- (- b) (Math/sqrt (- (* b b) (* 4 a c)))) (* 2 a)))]
          (max 0 (int n)))))

  (defn mine []
    (swap! funds + (* @click-power @click-upgrades))
    (swap! total-funds + (* @click-power @click-upgrades )))
  
  (defn add-click []
    (when (>= @funds @price)
       (swap! click-power + 1) 
       (swap! funds - @price)
       (swap! price *  1.5)
       (js/console.log "Purchased upgrade")))

  (defn get-worker []
    (when (>= @funds @worker-price)
       (swap! worker-power + 1) 
       (swap! funds - @worker-price)
       (swap! worker-price *  1.3)
       (js/console.log "Purchased upgrade")))

  (defn get-team []
   (when (>= @funds @team-price)
       (swap! worker-power + 5)
       (swap! funds - @team-price)
       (swap! team-price *  1.35)))
    
(defn upgrade-worker []
  (when (>= @funds @worker-upgrades-cost)
       (swap! worker-upgrades + 0.1) 
       (swap! funds - @worker-upgrades-cost)
       (swap! worker-upgrades-cost *  1.4)
       (js/console.log "Purchased upgrade")))

(defn upgrade-click []
  (when (>= @funds @click-upgrades-cost)
       (swap! click-upgrades + 0.1) 
       (swap! funds - @click-upgrades-cost)
       (swap! click-upgrades-cost *  1.4)
       (js/console.log "Purchased upgrade")))

(defn save-game []
  (let [game-state {:funds @funds
                    :total-funds @total-funds
                    :click-power @click-power
                    :worker-power @worker-power
                    :worker-upgrades @worker-upgrades
                    :click-upgrades @click-upgrades
                    :price @price
                    :worker-price @worker-price
                    :worker-upgrades-cost @worker-upgrades-cost
                    :click-upgrades-cost @click-upgrades-cost
                    :ascension @ascension}]
    (js/localStorage.setItem "gameState" (js/JSON.stringify (clj->js game-state)))
    (reset! notification "Game saved!")
    (js/setTimeout #(reset! notification nil) 5000)))

(defn load-game []
  (when-let [saved (js/localStorage.getItem "gameState")]
    (let [parsed (js->clj (js/JSON.parse saved) :keywordize-keys true)]
      (reset! funds (:funds parsed))
      (reset! total-funds (:total-funds parsed))
      (reset! click-power (:click-power parsed))
      (reset! worker-power (:worker-power parsed))
      (reset! worker-upgrades (:worker-upgrades parsed))
      (reset! click-upgrades (:click-upgrades parsed))
      (reset! price (:price parsed))
      (reset! worker-price (:worker-price parsed))
      (reset! worker-upgrades-cost (:worker-upgrades-cost parsed))
      (reset! click-upgrades-cost (:click-upgrades-cost parsed))
      (reset! ascension (:ascension parsed))
      (js/console.log "Game loaded!"))))

(defn reset-game []
(if (js/confirm "Are you sure you want to reset the game?")
(do
  (js/localStorage.removeItem "gameState")
  (reset! funds 0)
  (reset! total-funds 0)
  (reset! click-power 1)
  (reset! worker-power 0)
  (reset! worker-upgrades 1)
  (reset! click-upgrades 1)
  (reset! price 10)
  (reset! worker-price 100)
  (reset! worker-upgrades-cost 400)
  (reset! click-upgrades-cost 40)
  (reset! ascension 1))))

  (defn format-number [n]
  (cond
    (>= n 1e18) (str (round (/ n 1e18)) "Qi")   ;; Quintillions
    (>= n 1e15) (str (round (/ n 1e15)) "Q")    ;; Quadrillions
    (>= n 1e12) (str (round (/ n 1e12)) "T")    ;; Trillions
    (>= n 1e9)  (str (round (/ n 1e9)) "B")     ;; Billions
    (>= n 1e6)  (str (round (/ n 1e6)) "M")     ;; Millions
    (>= n 1e3)  (str (round (/ n 1e3)) "K")     ;; Thousands
    :else       (round n)))                     ;; Normal number

(defn save-last-timestamp []
  (when-not (.-hidden js/document)  ;; Only update if tab is visible
     (js/localStorage.setItem "lastActiveTime" (str (js/Date.now)))))

(defn load-last-timestamp []
  (some-> (js/localStorage.getItem "lastActiveTime") js/parseInt))

(defn apply-offline-progress []
  (let [last-time (load-last-timestamp)
        now (js/Date.now)]
    (when (and last-time (> now last-time))
      (let [elapsed-seconds (/ (- now last-time) 1000)
            offline-gain (* @worker-power @worker-upgrades elapsed-seconds)]
        (swap! funds + offline-gain)
        (swap! total-funds + offline-gain)
        (js/console.log (str "Offline earnings: " (round offline-gain)))
        (js/console.log (str "Time away: " (round elapsed-seconds)))
        (js/console.log (str "last here: " (round last-time)))
        (js/console.log (str "now: " (round now)))))))

(defn export-save []
  (let [game-state {:funds @funds
                    :total-funds @total-funds
                    :click-power @click-power
                    :worker-power @worker-power
                    :worker-upgrades @worker-upgrades
                    :click-upgrades @click-upgrades
                    :price @price
                    :worker-price @worker-price
                    :worker-upgrades-cost @worker-upgrades-cost
                    :click-upgrades-cost @click-upgrades-cost}
        json-str (js/JSON.stringify (clj->js game-state))
        blob (js/Blob. (clj->js [json-str]) #js {:type "application/json"})
        url (js/URL.createObjectURL blob)
        a (.createElement js/document "a")]
    (set! (.-href a) url)
    (set! (.-download a) "savegame.json")
    (.click a)
    (js/URL.revokeObjectURL url)))

(defn import-save [event]
  (let [file (-> event .-target .-files (aget 0))
        reader (js/FileReader.)]
    (set! (.-onload reader)
          (fn [e]
            (let [json-data (js->clj (js/JSON.parse (.-result e.target))
                                     :keywordize-keys true)]
              (reset! funds (:funds json-data))
              (reset! total-funds (:total-funds json-data))
              (reset! click-power (:click-power json-data))
              (reset! worker-power (:worker-power json-data))
              (reset! worker-upgrades (:worker-upgrades json-data))
              (reset! click-upgrades (:click-upgrades json-data))
              (reset! price (:price json-data))
              (reset! worker-price (:worker-price json-data))
              (reset! worker-upgrades-cost (:worker-upgrades-cost json-data))
              (reset! click-upgrades-cost (:click-upgrades-cost json-data))
              (js/console.log "Game imported!"))))
    (.readAsText reader file)))



;Timed actions

;;update funds with partial total worker power at a rate of every 25ms (1/40th of a second)
;;where the partial total power is also 1/40 to make 1 whole per second
(js/setInterval
   (fn []
     (do
       (swap! funds + (/ (* @worker-power @worker-upgrades) 40))
       (swap! total-funds + (/ (* @worker-power @worker-upgrades) 40)))) 
 25)

(js/setInterval save-game 20000)

(js/setInterval save-last-timestamp 1000)

(.addEventListener js/document "visibilitychange"
                   (fn []
                     (when (= js/document.visibilityState "visible")
                       (apply-offline-progress)
                       (save-last-timestamp)))) ;; Reset timestamp when user comes back



;HTML

(defn notification-box []
  (when @notification
    [:div {:style {:position "fixed"
                   :top "50px"
                   :right "10px"
                   :background "rgba(0, 0, 0, 0.8)"
                   :color "white"
                   :padding "10px 20px"
                   :border-radius "8px"
                   :z-index "1000"
                   :transition "opacity 0.5s"}}
     @notification]))

(defn reset-button []
  [:button#reset-button {:on-click reset-game}
   "Reset Game"])

(defn save-button []
  [:button#save-button {:on-click save-game}
   "Save Game"])

(defn export-button []
  [:button#export-button {:on-click export-save}
   "Export save"])

(defn import-button []
  [:button#import-button {:on-click #(-> js/document
                                         (.getElementById "file-import")
                                         .click)}
   "Import save"])

(defn file-input []
  [:input {:type "file"
           :accept ".json"
           :style {:display "none"} ;; Hide it
           :id "file-import"
           :on-change import-save}])

  (defn app []
    [:div 
     [file-input]
     [reset-button]
     [save-button]
     [export-button]
     [import-button]
     [notification-box]
     [:h1 "Ore Miner: Powered by ClojureScript!"]
     [:p "Currnent Funds: " (format-number @funds)]
     [:p "Funds per second: " (format-number (* @worker-power @worker-upgrades))]
     [:p "Funds per click: " (format-number (* @click-power @click-upgrades))]
     [:button 
      {:on-click #(mine)} "Mine!"]
     [:p "Click Power: " @click-power]
     [:p "Click Upgrades: " (format-number @click-upgrades)]
     [:p "Worker Power: " @worker-power]
     [:p "Worker Upgrades: " (format-number @worker-upgrades)]
     [:button 
      {:on-click #(add-click)} "Sharpen Equipment! price: " (format-number @price)] 
     [:button 
      {:on-click #(get-worker)} "Hire Worker! price: " (format-number @worker-price)] 
     [:button 
      {:on-click #(upgrade-worker)} "Upgrade Workers! price: " (format-number @worker-upgrades-cost)]
     [:button 
      {:on-click #(upgrade-click)} "Upgrade Clicking! price: " (format-number @click-upgrades-cost)]
     [:h2 "This mine has made: " (format-number @total-funds)]])
  


  ;Renderers
  (defn app-render[]
    (load-game)
    (rd/render [app] (js/document.getElementById "app_container")))
  
  
  ;Site state handlers
  (defn main! []
    (app-render))

  (defn reload! [] 
  (app-render))