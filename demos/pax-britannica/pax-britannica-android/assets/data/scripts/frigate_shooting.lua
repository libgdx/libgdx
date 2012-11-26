local blueprints = require 'blueprints'

local missile_speed = 1
local shot_cooldown_time = 6
local shot_capacity = 8
local shot_reload_rate = 1.2 / 60

local shots = 0
local cooldown = 0

function is_empty()
  return shots < 1
end

function is_reloaded()
  return shots == shot_capacity
end

function is_cooled_down()
  return cooldown == 0
end

function is_ready_to_shoot()
  return is_cooled_down() and is_reloaded()
end

function update()
  cooldown = math.max(0, cooldown - 1)
  shots = math.min(shots + shot_reload_rate, shot_capacity)
end

function shoot()
  if cooldown == 0 and shots >= 1 then
    shots = shots - 1
    cooldown = shot_cooldown_time
    local start_vel = missile_speed * self.transform.facing + self.ship.velocity
    game.actors.new(blueprints.missile,
      {'transform', pos=self.transform.pos},
      {'ship', velocity=start_vel, player=self.ship.player})
  end
end
