(ns main
    (:require [reagent.core :as r] 
              [reagent.dom :as rd]
              [cljs.pprint :as pprint]))

;Game Variables
  (def funds (r/atom 100))
  (def total-funds (r/atom 100))
  (def click-power (r/atom 1))
  (def price (r/atom 10))
  (def worker-power (r/atom 0))
  (def worker-price (r/atom 100))
  (def worker-upgrades (r/atom 1))
  (def click-upgrades (r/atom 1.2))
  (def worker-upgrades-cost (r/atom 400))
  (def click-upgrades-cost (r/atom 40))


;Game Functions
  (defn round [n]
    (pprint/cl-format nil "~,2f" n))

  (defn mine []
    (swap! funds + (* @click-power @click-upgrades))
    (swap! total-funds + (* @click-power @click-upgrades )))
  
  (defn upgrade-click []
    (when (>= @funds @price)
       (swap! click-power + 1) 
       (swap! funds - @price)
       (swap! price *  1.5)
       (js/console.log "Purchased upgrade")))

  (defn upgrade-worker []
    (when (>= @funds @worker-price)
       (swap! worker-power + 1) 
       (swap! funds - @worker-price)
       (swap! worker-price *  1.3)
       (js/console.log "Purchased upgrade")))
    

  (js/setInterval 
  (fn [] 
    (do 
      (swap! funds + (/ @worker-power 100)) 
      (swap! total-funds + (/ (* @worker-power @worker-upgrades)  100))))10)




;HTML
  (defn app []
    [:div
     [:h1 "Ore Miner: Powered by ClojureScript!"]
     [:p "Currnent Funds: " (round @funds)]
     [:button 
        {:on-click #(mine) 
         :style {:height "100px" :width "100px"}} "Mine!"]
     [:p "Click Power: " @click-power]
     [:p "Worker Power: " @worker-power]
     [:button 
        {:on-click #(upgrade-click)
         :style {:height "100px" :width "100px"}} "Upgrade! price: " (round @price)] 
     [:button 
        {:on-click #(upgrade-worker)
         :style {:height "100px" :width "100px"}} "Hire Worker! price: " (round @worker-price)]
     [:h2 "This mine has made: " (round @total-funds)]])
  
  ;Renderers
  (defn app-render[]
    (rd/render [app] (js/document.getElementById "app_container")))
  
  
  ;Site state handlers
  (defn main! []
    (app-render))

  (defn reload! [] 
  (app-render))
  
  (comment
    (defn max-ascensions-math [total-earned]
      (let [x (/ total-earned 1000000)
            a 0.55
            b 1.55
            c (- x)]
        (let [n (Math/floor (/ (- (- b) (Math/sqrt (- (* b b) (* 4 a c)))) (* 2 a)))]
          (max 0 (int n))))))