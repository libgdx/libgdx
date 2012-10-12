local gl = require 'gl'

local time = 0

function update()
  time = time + 1
end

function draw()
  -- ugh. . . sprite needs to be more flexible
  gl.glPushMatrix()
  gl.glTranslated(self.transform.pos.x, self.transform.pos.y, 0)
  local f = self.transform.facing
  gl.glRotated(180/math.pi * math.atan2(f.y, f.x), 0, 0, 1)

  local opacity = (self.sprite.color[4] or 1) * math.random()
  gl.glColor4d(1, 1, 1, opacity)
  local health = self.ship.health_percentage()
  if health < game.constants.low_health_threshold then
    game.resources.factory_heavy_damage_sprites[math.floor(time/4)%3+1]:draw()
  elseif health < game.constants.high_health_threshold then
    game.resources.factory_light_damage_sprites[math.floor(time/4)%3+1]:draw()
  end
  gl.glColor3d(1, 1, 1)

  gl.glPopMatrix()
end
