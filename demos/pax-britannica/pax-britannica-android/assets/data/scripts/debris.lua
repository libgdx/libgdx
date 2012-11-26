local v2 = require 'dokidoki.v2'

local SPEED = 0.2
local LIFETIME = 8
local FADE_TIME = 2

local random_direction = v2.unit(math.random() * 2 * math.pi)
local random_scale = math.random() * 0.75 + 0.5
local random_speed = math.random() + 0.5
local random_opacity = math.random() * 0.25 + 0.4

local since_alive = 0

function update()
  since_alive = since_alive + 1/60

  self.transform.scale_x = random_scale
  self.transform.scale_y = random_scale
  
  self.transform.facing = random_direction
  
  self.transform.pos = self.transform.pos + v2.rotate(random_direction, math.sin(since_alive) * 0.5) * SPEED * random_speed
  
  if since_alive < FADE_TIME then
    self.sprite.color[4] = since_alive / FADE_TIME * random_opacity
  else
    self.sprite.color[4] = math.min(1 - (since_alive - LIFETIME + FADE_TIME) / FADE_TIME, 1) * random_opacity
  end
  if since_alive > LIFETIME then self.dead = true end
end
