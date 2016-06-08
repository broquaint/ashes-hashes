(ns ashes-hashes.logsumer
  (:require [yesql.core :refer [defqueries]]
            [medley.core :refer [map-vals map-keys]]
            [com.stuartsierra.component :as component]
            [clojurewerkz.elastisch.rest.index :as es-index]
            [clojurewerkz.elastisch.rest.bulk :as es-bulk]
            [clojurewerkz.elastisch.rest.document :as es-doc]))

(defqueries "ashes_hashes/logsumer/logrecord.sql")

(def ^:private index-name "scratch")
(def ^:private mapping-type "game")
;; Generated by indexing some games then pulling out the mapping (+ local tweaks)
(def ^:private mappings
  {(keyword mapping-type)
   {:properties
    {:affected_max_hit_points {:type "long"}
     :base_magic_points {:type "long"}
     :evasion {:type "long"}
     :kills {:type "long"}
     :most_recent_depth {:type "long"}
     :real_max_hit_points {:type "long"}
     :deepest_ziggurat_depth {:type "long"}
     :vsav {:type "string" :index "not_analyzed"}
     :banisher {:type "string" :index "not_analyzed"}
     :gold_found {:type "long"}
     :best_skill {:type "string" :index "not_analyzed"}
     :place {:type "string" :index "not_analyzed"}
     :full_version {:type "string" :index "not_analyzed"}
     :experience_level {:type "long"}
     :canonicalised_version {:type "string" :index "not_analyzed"}
     :damage_from_all_sources {:type "long"}
     :deepest_level {:type "long"}
     :race {:type "string" :index "not_analyzed"}
     :scrolls_used {:type "long"}
     :file {:type "string" :index "not_analyzed"}
     :intelligence {:type "long"}
     :number_of_ziggurats_completed {:type "long"}
     :start {:format "strict_date_optional_time||epoch_millis", :type "date"}
     :maxed_out_skills {:type "string" :index "not_analyzed"}
     :penance {:type "long"}
     :god {:type "string" :index "not_analyzed"}
     :armor_class {:type "long"}
     :duration {:type "long"}
     :src {:type "string" :index "not_analyzed"}
     :piety {:type "long"}
     :level_type {:type "string" :index "not_analyzed"}
     :rstart {:type "string" :index "not_analyzed"}
     :title {:type "string" :index "not_analyzed"}
     :ntv {:type "long"}
     :aut {:type "long"}
     :max_magic_points {:type "long"}
     :gold {:type "long"}
     :turn {:type "long"}
     :killer {:type "string" :index "not_analyzed"}
     :strength {:type "long"}
     :damage {:type "long"}
     :best_skill_level {:type "long"}
     :status {:type "string" :index "not_analyzed"}
     :id {:type "string" :index "not_analyzed"}
     :kind_of_undead {:type "string" :index "not_analyzed"}
     :class {:type "string" :index "not_analyzed"}
     :score {:type "long"}
     :dexterity {:type "long"}
     :player_name {:type "string" :index "not_analyzed"}
     :rune_count {:type "long"}
     :alpha {:type "boolean"}
     :unique_rune_count {:type "long"}
     :wiz {:type "boolean"}
     :tiles {:type "boolean"}
     :canonicalised_race {:type "string" :index "not_analyzed"}
     :experimental_branch_name {:type "string" :index "not_analyzed"}
     :magic_points {:type "long"}
     :end {:format "strict_date_optional_time||epoch_millis", :type "date"}
     :version {:type "string" :index "not_analyzed"}
     :branch {:type "string" :index "not_analyzed"}
     :gold_spent {:type "long"}
     :map_name {:type "string" :index "not_analyzed"}
     :ending_type {:type "string" :index "not_analyzed"}
     :rend {:type "string" :index "not_analyzed"}
     :hit_points {:type "long"}
     :lv {:type "string" :index "not_analyzed"}
     :file_offset {:type "long"}
     :terse_msg {:type "string" :index "not_analyzed"}
     :death_blame_chain {:type "string" :index "not_analyzed"}
     :damage_from_source {:type "long"}
     :killer_weapon_desc {:type "string" :index "not_analyzed"}
     :charabbrev {:type "string" :index "not_analyzed"}
     :vsavrv {:type "string" :index "not_analyzed"}
     :potions_used {:type "long"}
     :map_description {:type "string" :index "not_analyzed"}
     :shields {:type "long"}}}})

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
 :id :db_row_id, ; 1
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


(defn ensure-es-state [conn]
  (when-not (es-index/exists? conn index-name)
   (es-index/create conn index-name :mappings mappings)))

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

(def batch-limit 100)

(defn games-after-from-db [offset db]
  (let [binds {:offset offset
               :batchlimit batch-limit}
        query-options {:connection db}]
   (map row->game (games-since-offset binds query-options))))

(defn add-games-to-es [conn games]
  (let [bulk-doc (es-bulk/bulk-index games)]
    (es-bulk/bulk-with-index-and-type conn index-name mapping-type bulk-doc)))

(defn catch-up-on-games [offset es db]
  (loop [game-batch (games-after-from-db offset db)
         final-db-row-id offset]
    (cond (empty? game-batch)
          ;; This is where we will resume from next.
          final-db-row-id
          ;; But if games were found then process them.
          :else
          (let [last-row-id (:db_row_id (last game-batch))]
            (add-games-to-es es game-batch)
            (println (str "Added " (count game-batch) " games [" last-row-id "] ..."))
            (recur (games-after-from-db last-row-id db) last-row-id)))))

(defn really-follow-logrecords [component offset]
  (let [db (:spec (:db component))
        es (:conn (:es component))
        me (:logsumer component)]
    (let [last-row-id (catch-up-on-games offset es db)]
      (Thread/sleep 1000) ;; Lazy polling FTW.
      (when (deref (:should-keep-running me))
        (recur component last-row-id)))))

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
        (ensure-es-state conn)
        (follow-logrecords (assoc component :logsumer logsumer)))))
  (stop [component]
    (future (reset! (:should-keep-running (:logsumer component)) false))
    (dissoc component :logsumer)))

(defn logsumer-component [options]
  (map->Logsumer options))
