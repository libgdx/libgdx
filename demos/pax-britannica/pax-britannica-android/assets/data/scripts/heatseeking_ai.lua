local v2 = require 'dokidoki.v2'

local MAX_LIFETIME = 60*5 -- 5 seconds to auto-destruct

local turn_speed = 0.15
local accel = 0.15
local age = 0

local target

local function self_destruct()
  --EXPLODE!
  game.log.record_miss(self)
  self.dead = true
end

local function retarget()
  target = game.targeting.get_type_in_range(self, 'fighter', 600) or
           game.targeting.get_type_in_range(self, 'bomber', 600) or
           game.targeting.get_type_in_range(self, 'frigate', 600) or
           game.targeting.get_nearest_of_type(self, 'fighter') or
           game.targeting.get_nearest_of_type(self, 'factory')
end
retarget()

local function predict(target)
  local relative_vel = self.ship.velocity - target.ship.velocity
  local to_target = target.transform.pos - self.transform.pos
  if v2.dot(self.ship.velocity, to_target) ~= 0 then
    local time_to_target =
      v2.sqrmag(to_target) / v2.dot(relative_vel, to_target)
    return target.transform.pos - relative_vel * math.max(0, time_to_target)
  else
    return target.transform.pos
  end
end

function update()
  age = age + 1

  if not target or age > MAX_LIFETIME then
    self_destruct()
  elseif target.dead then
    retarget()
  else
    game.tracing.trace_line(self.transform.pos, target.transform.pos)
    self.ship.go_towards(predict(target), true)
  end
end

