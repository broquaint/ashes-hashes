-- name: get-logrecord
SELECT logrecord.id, logrecord.file_offset, alpha, sc, xl, sklev, lvl, absdepth, hp, mhp, mmhp, mp, mmp, bmmp, dam, sdam, tdam, sstr, sint, sdex, piety, pen, wiz, tstart, tend, dur, turn, urune, nrune, rstart, rend, ntv, tiles, gold, goldfound, goldspent, zigscompleted, zigdeepest, scrollsused, potionsused, kills, ac, ev, sh, aut, maxskills, status, game_key, file, src, explbr, v, cv, vlong, vsav, vsavrv, lv, pname, race, crace, cls, charabbrev, sk, title, ktyp, killer, kpath, kmod, kaux, place, br, ltyp, god, tmsg, mapname, mapdesc, banisher
FROM logrecord
LEFT JOIN l_game_key ON logrecord.game_key_id = l_game_key.id
LEFT JOIN l_file ON logrecord.file_id = l_file.id
LEFT JOIN l_src ON logrecord.src_id = l_src.id
LEFT JOIN l_explbr ON logrecord.explbr_id = l_explbr.id
LEFT JOIN l_version ON logrecord.v_id = l_version.id
LEFT JOIN l_cversion ON logrecord.cv_id = l_cversion.id
LEFT JOIN l_vlong ON logrecord.vlong_id = l_vlong.id
LEFT JOIN l_savever ON logrecord.vsav_id = l_savever.id
LEFT JOIN l_savercsversion ON logrecord.vsavrv_id = l_savercsversion.id
LEFT JOIN l_lv ON logrecord.lv_id = l_lv.id
LEFT JOIN l_name ON logrecord.pname_id = l_name.id
LEFT JOIN l_race ON logrecord.race_id = l_race.id
LEFT JOIN l_crace ON logrecord.crace_id = l_crace.id
LEFT JOIN l_cls ON logrecord.cls_id = l_cls.id
LEFT JOIN l_char ON logrecord.charabbrev_id = l_char.id
LEFT JOIN l_sk ON logrecord.sk_id = l_sk.id
LEFT JOIN l_title ON logrecord.title_id = l_title.id
LEFT JOIN l_ktyp ON logrecord.ktyp_id = l_ktyp.id
LEFT JOIN l_killer ON logrecord.killer_id = l_killer.id
LEFT JOIN l_kpath ON logrecord.kpath_id = l_kpath.id
LEFT JOIN l_kmod ON logrecord.kmod_id = l_kmod.id
LEFT JOIN l_kaux ON logrecord.kaux_id = l_kaux.id
LEFT JOIN l_place ON logrecord.place_id = l_place.id
LEFT JOIN l_br ON logrecord.br_id = l_br.id
LEFT JOIN l_ltyp ON logrecord.ltyp_id = l_ltyp.id
LEFT JOIN l_god ON logrecord.god_id = l_god.id
LEFT JOIN l_msg ON logrecord.tmsg_id = l_msg.id
LEFT JOIN l_map ON logrecord.mapname_id = l_map.id
LEFT JOIN l_mapdesc ON logrecord.mapdesc_id = l_mapdesc.id
LEFT JOIN l_banisher ON logrecord.banisher_id = l_banisher.id
LEFT JOIN l_maxskills ON logrecord.maxskills_id = l_maxskills.id
LEFT JOIN l_status ON logrecord.status_id = l_status.id
WHERE logrecord.id = :id

-- name: games-since-offset
SELECT logrecord.id, logrecord.file_offset, alpha, sc, xl, sklev, lvl, absdepth, hp, mhp, mmhp, mp, mmp, bmmp, dam, sdam, tdam, sstr, sint, sdex, piety, pen, wiz, tstart, tend, dur, turn, urune, nrune, rstart, rend, ntv, tiles, gold, goldfound, goldspent, zigscompleted, zigdeepest, scrollsused, potionsused, kills, ac, ev, sh, aut, maxskills, status, game_key, file, src, explbr, v, cv, vlong, vsav, vsavrv, lv, pname, race, crace, cls, charabbrev, sk, title, ktyp, killer, kpath, kmod, kaux, place, br, ltyp, god, tmsg, mapname, mapdesc, banisher
FROM logrecord
LEFT JOIN l_game_key ON logrecord.game_key_id = l_game_key.id
LEFT JOIN l_file ON logrecord.file_id = l_file.id
LEFT JOIN l_src ON logrecord.src_id = l_src.id
LEFT JOIN l_explbr ON logrecord.explbr_id = l_explbr.id
LEFT JOIN l_version ON logrecord.v_id = l_version.id
LEFT JOIN l_cversion ON logrecord.cv_id = l_cversion.id
LEFT JOIN l_vlong ON logrecord.vlong_id = l_vlong.id
LEFT JOIN l_savever ON logrecord.vsav_id = l_savever.id
LEFT JOIN l_savercsversion ON logrecord.vsavrv_id = l_savercsversion.id
LEFT JOIN l_lv ON logrecord.lv_id = l_lv.id
LEFT JOIN l_name ON logrecord.pname_id = l_name.id
LEFT JOIN l_race ON logrecord.race_id = l_race.id
LEFT JOIN l_crace ON logrecord.crace_id = l_crace.id
LEFT JOIN l_cls ON logrecord.cls_id = l_cls.id
LEFT JOIN l_char ON logrecord.charabbrev_id = l_char.id
LEFT JOIN l_sk ON logrecord.sk_id = l_sk.id
LEFT JOIN l_title ON logrecord.title_id = l_title.id
LEFT JOIN l_ktyp ON logrecord.ktyp_id = l_ktyp.id
LEFT JOIN l_killer ON logrecord.killer_id = l_killer.id
LEFT JOIN l_kpath ON logrecord.kpath_id = l_kpath.id
LEFT JOIN l_kmod ON logrecord.kmod_id = l_kmod.id
LEFT JOIN l_kaux ON logrecord.kaux_id = l_kaux.id
LEFT JOIN l_place ON logrecord.place_id = l_place.id
LEFT JOIN l_br ON logrecord.br_id = l_br.id
LEFT JOIN l_ltyp ON logrecord.ltyp_id = l_ltyp.id
LEFT JOIN l_god ON logrecord.god_id = l_god.id
LEFT JOIN l_msg ON logrecord.tmsg_id = l_msg.id
LEFT JOIN l_map ON logrecord.mapname_id = l_map.id
LEFT JOIN l_mapdesc ON logrecord.mapdesc_id = l_mapdesc.id
LEFT JOIN l_banisher ON logrecord.banisher_id = l_banisher.id
LEFT JOIN l_maxskills ON logrecord.maxskills_id = l_maxskills.id
LEFT JOIN l_status ON logrecord.status_id = l_status.id
WHERE logrecord.file_offset > :offset
