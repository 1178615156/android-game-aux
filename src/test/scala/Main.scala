object MainTest {
  def main(args: Array[String]): Unit = {
    import better.files._
    File("F:\\software\\android\\res-decrypt\\codes\\shared\\conftable").list
      .map(e => e.name -> e.size)
      .toList
      .sortBy(_._2)
      .map(_._1)
      .reverse
      .take(10)
    val s = List("achievement.lua", "activity.lua", "activity_mission.lua", "activity_mission_group.lua", "arena_content.lua", "arena_k.lua", "arena_list.lua", "arena_npc.lua", "arena_reward_rank.lua", "arena_reward_stage.lua", "arena_rules.lua", "arena_stage_score.lua", "battle_expression.lua", "buff.lua", "build.lua", "condition.lua", "current_bottle_activity.lua", "cv.lua", "dialogue.lua", "drop_lib.lua", "effect.lua", "enums.lua", "equipment.lua", "equipment_build.lua", "equipment_synthesis.lua", "error_code.lua", "event_bottle.lua", "event_buff.lua", "event_fight.lua", "event_immediate.lua", "event_resource.lua", "event_special.lua", "event_text.lua", "event_transfer.lua", "explore.lua", "explore_event.lua", "explore_mission.lua", "explore_subevent.lua", "explore_word.lua", "fulcrum.lua", "fulcrum_condition.lua", "fulcrum_event.lua", "fulcrum_mission.lua", "game_buff.lua", "goods.lua", "guide.lua", "guide_elf.lua", "guide_text.lua", "handbook.lua", "help.lua", "item.lua", "line.lua", "mail_model.lua", "medal.lua", "monster.lua", "monster2.lua", "monster_activity.lua", "monster_handbook.lua", "monster_special.lua", "mood.lua", "name.lua", "paint_info.lua", "patrol.lua", "pay.lua", "personal_buff.lua", "personal_mission.lua", "personal_mission_word.lua", "player_level.lua", "player_mission.lua", "player_style.lua", "real_name_school_mail.lua", "refresh_time.lua", "server_push.lua", "sign.lua", "sign_text.lua", "skill.lua", "skill_change.lua", "skill_skin1.lua", "skin.lua", "skin_gacha.lua", "stage.lua", "stage_dialogue.lua", "stage_plot.lua", "store.lua", "strength_level.lua", "target.lua", "theater.lua", "training.lua", "training_fight.lua", "training_fulcrum.lua", "unit.lua", "unit_level.lua", "unit_lib.lua", "unit_time_limit.lua", "unit_whereabouts.lua", "update_cartoon.lua", "vocabulary_entry.lua", "wardialogue.lua", "wishing_buff.lua", "wishing_effect.lua", "wishing_gift.lua", "wishing_well.lua", "wishing_word.lua", "worktime_limit.lua", "__enums.lua", "__validate_all.lua")

    println(s.map { s =>

      s"""__text_write_to_file("${s.replace(".lua", "")}")"""
    }.mkString("\n"))
  }
}
