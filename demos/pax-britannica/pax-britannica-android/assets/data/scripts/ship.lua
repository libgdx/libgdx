local v2 = require 'dokidoki.v2'

assert(player, 'missing player argument')
assert(turn_speed, 'missing turn_speed argument')
assert(accel, 'missing accel argument')
assert(hit_points, 'missing hit_points argument')

local max_hit_points = hit_points
self.sprite.color = {1.0, 1.0, 1.0}

if sprites_table then
  self.sprite.image = assert(game.resources[sprites_table][player])
end

velocity = v2(0, 0)

-- 1 for left, -1 for right
function turn(direction, amount)
  amount = amount or 1
  self.transform.facing =
    v2.norm(v2.rotate(self.transform.facing, turn_speed * direction * amount))
end

function thrust(amount)
  amount = amount or 1
  velocity = velocity + self.transform.facing * accel * amount
end

local function random_point_on_ship()
  -- assumes the ship is a rectangle
  local a = self.collision.poly.vertices[1]
  local ab = self.collision.poly.vertices[2] - a
  local ac = self.collision.poly.vertices[4] - a
  return self.transform.pos +
         v2.rotate_to(self.transform.facing,
                      a + ab * math.random() + ac * math.random())
end

function update()
  velocity = velocity * 0.97
  self.transform.pos = self.transform.pos + velocity
  if hit_points <= 0 then destruct() end

  if math.random() < v2.mag(velocity) / 10 then
    game.particles.add_bubble(random_point_on_ship())
  end

  -- debug
  game.tracing.trace_bar(self.transform.pos + v2(0, 10),
                         hit_points / max_hit_points)
end

local function go_towards_or_away(target_pos, force_thrust, is_away)
  local target_direction = target_pos - self.transform.pos
  if is_away then target_direction = -target_direction end

  if v2.cross(self.transform.facing, target_direction) > 0 then
    turn(1)
  else
    turn(-1)
  end
  if force_thrust or v2.dot(self.transform.facing, target_direction) > 0 then
    thrust()
  end
end

function health_percentage()
  return math.max(hit_points / max_hit_points, 0)
end

function damage(amount)
  hit_points = math.max(hit_points - amount, 0)
end

function destruct()
  if self.factory_ai then
    factory_destruct()
  else
    game.particles.explode(self)
    game.log.record_death(self.blueprint)
    self.dead = true
  end
end

local death_counter = 100
local next_explosion = 10
local opacity = 0.6

function factory_destruct()
  if death_counter > 0 then
    self.production.halt_production = true
    self.sprite.color = {self.sprite.color[1], self.sprite.color[2], self.sprite.color[3], math.max(0, opacity)}
    opacity = opacity - 0.006
    if death_counter % next_explosion == 0 then
      game.particles.explode(self, random_point_on_ship())
      next_explosion = math.random(6,15)
    end
    death_counter = death_counter - 1
  else
    for i = 1,5 do
      random = (v2.random() + v2.random()) * 20
      game.particles.explode(self, self.transform.pos + random)
    end
    game.log.record_death(self.blueprint)
    self.dead = true
  end
end

-- automatically thrusts and turns according to the target
function go_towards(target_pos, force_thrust)
  go_towards_or_away(target_pos, force_thrust, false)
end

function go_away(target_pos, force_thrust)
  go_towards_or_away(target_pos, force_thrust, true)
end
