local graphics = require 'dokidoki.graphics'

background = graphics.sprite_from_image('sprites/background.png', nil, {0, 0})

loading = graphics.sprite_from_image('sprites/loading.png', nil, {113, -10})

fighter_sprites = {
  graphics.sprite_from_image('sprites/fighter_p1.png', nil, 'center'),
  graphics.sprite_from_image('sprites/fighter_p2.png', nil, 'center'),
  graphics.sprite_from_image('sprites/fighter_p3.png', nil, 'center'),
  graphics.sprite_from_image('sprites/fighter_p4.png', nil, 'center')
}

bomber_sprites = {
  graphics.sprite_from_image('sprites/bomber_p1.png', nil, 'center'),
  graphics.sprite_from_image('sprites/bomber_p2.png', nil, 'center'),
  graphics.sprite_from_image('sprites/bomber_p3.png', nil, 'center'),
  graphics.sprite_from_image('sprites/bomber_p4.png', nil, 'center')
}

frigate_sprites = {
  graphics.sprite_from_image('sprites/frigate_p1.png', nil, 'center'),
  graphics.sprite_from_image('sprites/frigate_p2.png', nil, 'center'),
  graphics.sprite_from_image('sprites/frigate_p3.png', nil, 'center'),
  graphics.sprite_from_image('sprites/frigate_p4.png', nil, 'center')
}

factory_sprites = {
  graphics.sprite_from_image('sprites/factory_p1.png', nil, 'center'),
  graphics.sprite_from_image('sprites/factory_p2.png', nil, 'center'),
  graphics.sprite_from_image('sprites/factory_p3.png', nil, 'center'),
  graphics.sprite_from_image('sprites/factory_p4.png', nil, 'center')
}

factory_light_damage_sprites = {
  graphics.sprite_from_image('sprites/factory_light_damage_1.png', nil, 'center'),
  graphics.sprite_from_image('sprites/factory_light_damage_2.png', nil, 'center'),
  graphics.sprite_from_image('sprites/factory_light_damage_3.png', nil, 'center'),
}

factory_heavy_damage_sprites = {
  graphics.sprite_from_image('sprites/factory_heavy_damage_1.png', nil, 'center'),
  graphics.sprite_from_image('sprites/factory_heavy_damage_2.png', nil, 'center'),
  graphics.sprite_from_image('sprites/factory_heavy_damage_3.png', nil, 'center'),
}

number_sprites = {
  graphics.sprite_from_image('sprites/1.png', nil, 'center'),
  graphics.sprite_from_image('sprites/2.png', nil, 'center'),
  graphics.sprite_from_image('sprites/3.png', nil, 'center'),
  graphics.sprite_from_image('sprites/4.png', nil, 'center'),
  graphics.sprite_from_image('sprites/5.png', nil, 'center')
}

a_button = graphics.sprite_from_image('sprites/a_button.png', nil, 'center')

production_layer_1 = graphics.sprite_from_image('sprites/production1.png', nil, 'center')
production_layer_2 = graphics.sprite_from_image('sprites/production2.png', nil, 'center')
production_layer_3 = graphics.sprite_from_image('sprites/production3.png', nil, 'center')

needle_sprite = graphics.sprite_from_image('sprites/needle.png', nil, 'center')
fighter_preview_sprite = graphics.sprite_from_image('sprites/fighter_outline.png', nil, 'center')
bomber_preview_sprite = graphics.sprite_from_image('sprites/bomber_outline.png', nil, 'center')
frigate_preview_sprite = graphics.sprite_from_image('sprites/frigate_outline.png', nil, 'center')
upgrade_preview_sprite = graphics.sprite_from_image('sprites/upgrade_outline.png', nil, 'center')

health_full = graphics.sprite_from_image('sprites/health_full.png', nil, 'center')
health_some = graphics.sprite_from_image('sprites/health_some.png', nil, 'center')
health_none = graphics.sprite_from_image('sprites/health_none.png', nil, 'center')

laser_sprite = graphics.sprite_from_image('sprites/laser.png', nil, 'center')
bomb_sprite = graphics.sprite_from_image('sprites/bomb.png', nil, 'center')
missile_sprite = graphics.sprite_from_image('sprites/missile.png', nil, 'center')

bubble_sprite = graphics.sprite_from_image('sprites/bubble.png', nil, 'center')
big_bubble_sprite = graphics.sprite_from_image('sprites/big_bubble.png', nil, 'center')
explosion_sprite = graphics.sprite_from_image('sprites/explosion.png', nil, 'center')
spark_sprite = graphics.sprite_from_image('sprites/spark.png', nil, 'center')

debris_sprites = {
  graphics.sprite_from_image('sprites/debris_large.png', nil, 'center'),
  graphics.sprite_from_image('sprites/debris_med.png', nil, 'center'),
  graphics.sprite_from_image('sprites/debris_small.png', nil, 'center')
}

fish_sprites = {
  graphics.sprite_from_image('sprites/fish1.png', nil, 'center'),
  graphics.sprite_from_image('sprites/fish2.png', nil, 'center'),
  graphics.sprite_from_image('sprites/fish3.png', nil, 'center'),
  graphics.sprite_from_image('sprites/fish4.png', nil, 'center'),
  graphics.sprite_from_image('sprites/fish5.png', nil, 'center'),
  graphics.sprite_from_image('sprites/fish6.png', nil, 'center'),
  graphics.sprite_from_image('sprites/fish7.png', nil, 'center'),
  graphics.sprite_from_image('sprites/fish8.png', nil, 'center')
}

title_sprite = graphics.sprite_from_image('sprites/title.png', nil, 'center')
credits_sprite = graphics.sprite_from_image('sprites/credits.png', nil, 'center')
--press_a_sprite = graphics.sprite_from_image('sprites/press_A.png', nil, 'center')

-- woot for hacks, this fixes some icky jittering when the factory moves
local gl = require 'gl'

local function smoothen(sprite)
  sprite.tex:enable()
  gl.glTexParameterf(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MIN_FILTER, gl.GL_LINEAR)
  gl.glTexParameterf(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MAG_FILTER, gl.GL_LINEAR)
  sprite.tex:disable()
end

local function tilen(sprite)
  sprite.tex:enable()
  gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_S, gl.GL_REPEAT)
  sprite.tex:disable()
end

for _, s in pairs(factory_sprites) do
  smoothen(s)
end
for _, s in ipairs(fish_sprites) do
  smoothen(s)
end
smoothen(production_layer_1)
smoothen(production_layer_2)
smoothen(production_layer_3)
smoothen(needle_sprite)
smoothen(fighter_preview_sprite)
smoothen(bomber_preview_sprite)
smoothen(frigate_preview_sprite)
smoothen(upgrade_preview_sprite)
smoothen(health_full)
smoothen(health_some)
smoothen(health_none)

tilen(health_full)
tilen(health_some)
tilen(health_none)
