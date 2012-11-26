require 'dokidoki.module'
[[ background, debris, fish, background_fx, music_loader, game_flow, fade_in,
   fade_out, fighter, bomber, frigate, selection_factory, player_factory,
   easy_enemy_factory, laser, bomb, missile, countdown, splash ]]

local collision = require 'dokidoki.collision'
local game = require 'dokidoki.game'
local v2 = require 'dokidoki.v2'

background = game.make_blueprint('background',
  {'transform'},
  {'sprite', resource='background'})
  
debris = game.make_blueprint('debris',
  {'transform'},
  {'debris'},
  {'sprite'})
  
fish = game.make_blueprint('fish',
  {'transform'},
  {'fish'},
  {'sprite'})  
  
background_fx = game.make_blueprint('background_fx',
  {'background_fx'})

music_loader = game.make_blueprint('music_loader',
  {'transform', pos=v2(1024, 0)},
  {'sprite', resource='loading'},
  {'load_music', filename='audio/music.ogg'})

game_flow = game.make_blueprint('game_flow',
  {'game_flow'})

fade_in = game.make_blueprint('fade',
  {'fade', from=1, to=0, duration=60})

fade_out = game.make_blueprint('fade',
  {'fade', from=0, to=1, duration=60})
  
countdown = game.make_blueprint('countdown',
  {'countdown'},
  {'sprite'},
  {'transform', pos=v2(1024/2, 768/2)})

fighter = game.make_blueprint('fighter',
  {'transform'},
  {'sprite'},
  {'collision', collision_type='ship', poly=collision.make_rectangle(9, 6)},
  {'ship', turn_speed=0.025, accel=0.1, hit_points=40, sprites_table="fighter_sprites"},
  {'fighter_shooting'},
  {'fighter_ai'})

bomber = game.make_blueprint('bomber',
  {'transform'},
  {'sprite'},
  {'collision', collision_type='ship', poly=collision.make_rectangle(22, 14)},
  {'ship', turn_speed=0.03, accel=0.05, hit_points=250, sprites_table="bomber_sprites"},
  {'bomber_shooting'},
  {'bomber_ai'})

frigate = game.make_blueprint('frigate',
  {'transform'},
  {'sprite'},
  {'collision', collision_type='ship', poly=collision.make_rectangle(54, 36)},
  {'ship', turn_speed=0.01, accel=0.01, hit_points=1400, sprites_table="frigate_sprites"},
  {'frigate_shooting'},
  {'frigate_ai'})

selection_factory = game.make_blueprint('selection_factory',
  {'transform'},
  {'sprite', resource='factory_sprite', color={0.2, 0.2, 0.2}},
  {'selector'})
  
splash = game.make_blueprint('splash',
  {'splash'})
  
player_factory = game.make_blueprint('factory',
  {'transform'},
  {'sprite'},
  {'collision', collision_type='ship', poly=collision.make_rectangle(170, 100)},
  {'ship', turn_speed=0.00028, accel=0.002, hit_points=20000, sprites_table="factory_sprites"},
  {'factory_damage'},
  {'factory_ai'},
  {'resources'},
  {'production'},
  {'player_production'})
  
easy_enemy_factory = game.make_blueprint('factory',
  {'transform'},
  {'sprite', resource='factory_sprite'},
  {'collision', collision_type='ship', poly=collision.make_rectangle(170, 100)},
  {'ship', turn_speed=0.00028, accel=0.002, hit_points=20000, sprites_table="factory_sprites"},
  {'factory_damage'},
  {'factory_ai'},
  {'resources'},
  {'production'},
  {'easy_enemy_production'})
  
laser = game.make_blueprint('laser',
  {'transform'},
  {'sprite', resource='laser_sprite'},
  {'collision', collision_type='bullet', damage=10, poly=collision.make_rectangle(32, 1)},
  {'bullet'})
  
bomb = game.make_blueprint('bomb',
  {'transform'},
  {'sprite', resource='bomb_sprite'},
  {'collision', collision_type='bullet', damage=200, poly=collision.make_rectangle(4, 4)},
  {'bullet'})  
  
missile = game.make_blueprint('missile',
  {'transform'},
  {'sprite', resource='missile_sprite'},
  {'collision', collision_type='bullet', damage=40, poly=collision.make_rectangle(5, 2)},
  {'ship', turn_speed=0.055, accel=0.15, hit_points=1},
  {'heatseeking_ai'})

return get_module_exports()
