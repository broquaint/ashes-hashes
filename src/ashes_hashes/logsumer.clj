(ns ashes-hashes.logsumer
  (:require [yesql.core :refer [defqueries]]
            [medley.core :refer [map-vals map-keys]]
            [com.stuartsierra.component :as component]
            [clojurewerkz.elastisch.rest :as esr]
            [clojurewerkz.elastisch.rest.index :as esi]
            [clojurewerkz.elastisch.rest.document :as esd]))

(defqueries "ashes_hashes/logsumer/logrecord.sql")

(def ^:private index-name "scratch")
(def ^:private mapping-type "game")

;; Take the logrecord fields and turn them into something I can make
;; sense of at a glance/
(def ^:private from-to
  {
 :absdepth :deepest_level, ; 2
 :ac :armor_class, ; 5 ; :armour_class reads weird
 ; :alpha false ; internal field
 ; :aut 1234 ; :arbitary_units_of_time is awkward
 ; :banisher "" ; fine as is
 :bmmp :base_magic_points,
 :br :branch, ; "D"
 ; :charabbrev ; fine as is
 :cls :class ; "Berserker",
 :crace :canonicalised_race, ; "Human"
 :cv :canonicalised_version, ; "0.15",
 :dam :damage, ; -9999,
 :dur :duration, ; 46,
 :ev :evasion, ; 12,
 :explbr :experimental_branch_name, ; "",
 ; :file "cdo/allgames-0.15.txt", ; internal field
 ; :file_offset 0, ; internal field
 :game_key :id ; "Napkin:cdo:20140728184556S" ; least worst ID for now
 ; :god "Trog", ; fine as is
 ; :gold 10, ; fine as is
 :goldfound :gold_found, ; 10,
 :goldspent :gold_spent ; 0,
 :hp :hit_points ; 18,
 ; :id 1, ; internal field
 :kaux :killer_weapon_desc ; "",
 ; :killer "", ; fine as is
 ; :kills 0, ; ditto
 :kmod :kind_of_undead, ; "",
 :kpath :death_blame_chain, ; "",
 :ktyp :ending_type, ; "quitting",
 :ltyp :level_type, ; "",
 ; :lv "0.1", logfile format version, internal field
 :lvl :most_recent_depth, ; 1,
 :mapdesc :map_description, ;  "",
 :mapname :map_name, ; "scummos arrival star",
 :maxskills :maxed_out_skills, ; "",
 :mhp :affected_max_hit_points, ; 18,
 :mmhp :real_max_hit_points, ;  18,
 :mmp :max_magic_points, ; 0,
 :mp :magic_points, ; 0,
 :nrune :rune_count, ; 0,
 ; :ntv 0, ; internal field, number of times it has been requested for FooTV
 :pen :penance, ; 0,
 ; :piety 35, ; fine as is
 ; :place "D:1", ; fine as is
 :pname :player_name, ; "Napkin",
 :potionsused :potions_used, ; 0
 ; :race "Human", ;fine as is
 ; :rend "20140728184700S", ; internal field, raw end
 ; :rstart "20140728184556S", ; as above, raw start
 :sc :score, ; 0,
 :scrollsused :scrolls_used, ; 0,
 :sdam :damage_from_source, ; 0,
 :sdex :dexterity ; 12, ; 
 :sh :shields, ; 0,
 :sint :intelligence, ; 7,
 :sk :best_skill, ; "Long Blades",
 :sklev :best_skill_level, ; 3,
 ; :src "cdo", ; internal field, will be useful for other things
 :sstr :strength ; 17,
 ; :status "", ; fine as is
 :tdam :damage_from_all_sources, ; 0,
 :tend :end ; #inst "2014-08-28T17:47:00.000000000-00:00",
 ; :tiles false, ; internal field
 ; :title "Slasher", ; fine as is
 :tmsg :terse_msg, ; "quit the game",
 :tstart :start, ; #inst "2014-08-28T17:45:56.000000000-00:00",
 ; :turn 7, ; fine as is
 :urune :unique_rune_count, ; 0,
 :v :version, ; "0.15.0",
 :vlong :full_version, ; "0.15.0",
 ; :vsav "", ; internal field, save file version
 ; :vsavrv "", ; internal field, most recently used version
 ; :wiz false, ; internal field, wiz mode
 :xl :experience_level, ; 1
 :zigdeepest :deepest_ziggurat_depth, ; 0,
 :zigscompleted :number_of_ziggurats_completed, ; 0,
 })

;; Turn CITEXT types into strings.

;; Turn a row out of Postgres into Clojure data, mostly just turn
;; CITEXT -> string.
(defn- pgrow->clj [row]
  (map-vals (fn [v]
              (if (isa? (class v) org.postgresql.util.PGobject)
                (.getValue v)
                v))
            row))

;; Take a row out of the DB and turn it into something that could be
;; used by ES.
(defn- row->game [row]
  (map-keys #(get from-to % %) (pgrow->clj row)))

(defn get-game-from-db [id db]
  (row->game
   (or
    (first (get-logrecord {:id id} {:connection db}))
    {})))

(defn games-after-from-db [offset db]
  (map row->game
       (take 3 (games-since-offset {:offset offset} {:connection db}))))

(defn add-game-to-es [game conn]
  (esd/put conn index-name mapping-type (:id game) game))

(defn really-follow-logrecords [component offset]
  (let [db (:spec (:db component))
        es (:conn (:es component))
        me (:ls component)
        games (games-after-from-db offset db)]
    (doseq [game games]
      (println "Adding " (:id game))
      (add-game-to-es game es))
    (println "Added " (count games) " games to ES")
    (Thread/sleep 1000)
    (when (deref (:should-keep-running me))
     (recur component (:file_offset (last games))))))

(defn follow-logrecords [component]
  (future (really-follow-logrecords component 0)) ;; XXX Don't always start from the start!
  component)

(defrecord Logsumer []
  component/Lifecycle
  (start [component]
    (if (:logsumer component)
      component
      (let [conn (:conn (:es component))
            logsumer {:should-keep-running (atom true)}]
        (when-not (esi/exists? conn index-name)
          (esi/create conn index-name))
        (follow-logrecords (assoc component :logsumer logsumer)))))
  (stop [component]
    (future (reset! (:should-keep-running (:logsumer component)) false))
    (dissoc component :logsumer)))

(defn logsumer-component [options]
  (map->Logsumer options))
