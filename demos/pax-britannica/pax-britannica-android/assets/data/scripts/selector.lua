local gl = require 'gl'
local v2 = require 'dokidoki.v2'

assert(player, 'missing player argument')

picked = false

local OFFSET_PIXELS = -4
local SEGMENTS = 16
local RADIUS = 32

local fade = 0.2

local pulse_time = 0

callback = callback or function () end

self.sprite.image = assert(game.resources.factory_sprites[player])

function update()
  pulse_time = pulse_time + 1

  if not picked and game.the_one_button.held(player) then
    picked = true
    callback()
  end
  if picked then
    fade = math.min(fade + 0.1, 1)
    self.sprite.color = {fade, fade, fade}
  end
end

function draw()
  local pos = self.transform.pos + OFFSET_PIXELS * self.transform.facing
  local pulse = (1 + math.cos(pulse_time/180*2*math.pi))/2

  local color = fade * pulse + 1 * (1-pulse)
  
  gl.glPushMatrix()
  gl.glTranslated(pos.x, pos.y, 0)
  
  -- dark section
  gl.glBegin(gl.GL_TRIANGLE_FAN)
    gl.glColor4d(0, 0, 0, 0.6)
    gl.glVertex2d(0, 0)
    for point = 0,SEGMENTS do
      local vert = v2.unit(math.pi/2 - point / SEGMENTS * math.pi * 2) * RADIUS
      gl.glVertex2d(vert.x, vert.y)
    end
    gl.glVertex2d(0, 0)
  gl.glEnd()      
  
  -- light section
  if picked then
    gl.glBegin(gl.GL_TRIANGLE_FAN)
      local bottom_highlight_angle = player * math.pi / 2 - math.pi
      gl.glColor4d(0.5, 1, 1, 0.7)
      gl.glVertex2d(0, 0)
      for point = 0,SEGMENTS do
        gl.glVertex2d(math.sin(point / SEGMENTS * math.pi * 0.5 + bottom_highlight_angle) * RADIUS, math.cos(point / SEGMENTS * math.pi * 0.5 + bottom_highlight_angle) * RADIUS)
      end
    gl.glEnd()   
  end

  gl.glColor3d(color, color, color)
  game.resources.a_button:draw()
  gl.glColor3d(1, 1, 1)
  gl.glPopMatrix()
end
