local blueprints = require 'blueprints'

local bullet_speed = 20
local shot_cooldown_time = 6
local shot_capacity = 5
local shot_reload_rate = 2/60

local shots = shot_capacity
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
  return is_cooled_down() and not is_empty()
end

function update()
  cooldown = math.max(0, cooldown - 1)
  shots = math.min(shots + shot_reload_rate, shot_capacity)
end

function shoot()
  if cooldown == 0 and shots >= 1 then
    shots = shots - 1
    cooldown = shot_cooldown_time

    local start_vel = bullet_speed * self.transform.facing + self.ship.velocity
    game.actors.new(blueprints.laser,
      {'transform', pos=self.transform.pos, facing=self.transform.facing},
      {'bullet', player=self.ship.player, velocity=start_vel})
  end
end
