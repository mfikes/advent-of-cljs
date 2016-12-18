(ns advent-2016.day-01.part-2
  (:require
    [clojure.set :as set]
    [clojure.spec :as s]))

(s/def ::turn #{::right ::left ::none})

(s/def ::blocks int?)

(s/def ::direction #{::north ::east ::south ::west})

(s/def ::move (s/keys :req [::turn ::blocks]))

(s/def ::blocks-east ::blocks)

(s/def ::blocks-north ::blocks)

(s/def ::location (s/keys :req [::blocks-east ::blocks-north]))

(s/def ::state (s/keys :req [[::direction ::location]]))

(defn parse-turn
  [s]
  (case s
    "R" ::right
    "L" ::left))

(defn parse-blocks
  [s]
  (js/parseInt s))

(defn parse-move
  [move]
  (let [move-str (str move)]
    {::turn   (parse-turn (first move-str))
     ::blocks (parse-blocks (subs move-str 1))}))

(def directions-clockwise [::north ::east ::south ::west])

(def right-turns (zipmap directions-clockwise (concat (rest directions-clockwise) [(first directions-clockwise)])))

(def left-turns (set/map-invert right-turns))

(def no-turns (zipmap directions-clockwise directions-clockwise))

(defn update-direction
  [direction turn]
  (let [turns (case turn
                ::right right-turns
                ::left left-turns
                ::none no-turns)]
    (get turns direction)))

(s/fdef update-direction
  :args (s/cat :direction ::direction :turn ::turn)
  :ret ::direction)

(defn update-location
  [location direction blocks]
  (case direction
    ::north (update location ::blocks-north + blocks)
    ::east (update location ::blocks-east + blocks)
    ::south (update location ::blocks-north - blocks)
    ::west (update location ::blocks-east - blocks)))

(s/fdef update-location
  :args (s/cat :location ::location :direction ::direction :blocks ::blocks)
  :ret ::location)

(defn update-state
  [{:keys [::direction ::location]}
   {:keys [::turn ::blocks]}]
  (let [direction (update-direction direction turn)
        location (update-location location direction blocks)]
    {::direction direction
     ::location  location}))

(s/fdef update-state
  :args (s/cat :state ::state :move ::move)
  :ret ::state)

(defn trail
  [state moves]
  (->> (reductions update-state state moves)
    (map ::location)))

(s/fdef trail
  :args (s/cat :state ::state :moves (s/coll-of ::move))
  :ret (s/coll-of ::location))

(defn distance
  [{:keys [::blocks-east ::blocks-north]}]
  (+ (Math/abs blocks-east) (Math/abs blocks-north)))

(s/fdef distance
  :args (s/cat :location ::location)
  :ret int?)

(defn first-repeat
  [coll]
  (loop [so-far #{}
         to-go coll]
    (let [f (first to-go)]
      (when f
        (if (so-far f)
          f
          (recur (conj so-far f) (rest to-go)))))))

(s/fdef first-repeat
  :args (s/coll-of any?))

(defn calcluate-solution
  [initial-state moves]
  (-> (trail initial-state moves)
    first-repeat
    distance))

(defn expand-move
  [move]
  (cons (assoc move ::blocks 1)
    (repeat (dec (::blocks move))
      {::turn ::none, ::blocks 1})))

(s/fdef expand-move
  :args (s/cat :move ::move)
  :ret (s/coll-of ::move))

(defn solve
  []
  (let [raw-moves '[R3, L2, L2, R4, L1, R2, R3, R4, L2, R4, L2, L5, L1, R5,
                    R2, R2, L1, R4, R1, L5, L3, R4, R3, R1, L1, L5, L4, L2,
                    R5, L3, L4, R3, R1, L3, R1, L3, R3, L4, R2, R5, L190,
                    R2, L3, R47, R4, L3, R78, L1, R3, R190, R4, L3, R4, R2,
                    R5, R3, R4, R3, L1, L4, R3, L4, R1, L4, L5, R3, L3, L4,
                    R1, R2, L4, L3, R3, R3, L2, L5, R1, L4, L1, R5, L5, R1,
                    R5, L4, R2, L2, R1, L5, L4, R4, R4, R3, R2, R3, L1, R4,
                    R5, L2, L5, L4, L1, R4, L4, R4, L4, R1, R5, L1, R1, L5,
                    R5, R1, R1, L3, L1, R4, L1, L4, L4, L3, R1, R4, R1, R1,
                    R2, L5, L2, R4, L1, R3, L5, L2, R5, L4, R5, L5, R3, R4,
                    L3, L3, L2, R2, L5, L5, R3, R4, R3, R4, R3, R1]
        moves (map parse-move raw-moves)
        expanded-moves (mapcat expand-move moves)
        initial-state {::direction ::north
                       ::location  {::blocks-east  0
                                    ::blocks-north 0}}]
    (calcluate-solution initial-state expanded-moves)))