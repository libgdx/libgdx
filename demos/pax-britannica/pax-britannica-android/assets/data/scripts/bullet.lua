local v2 = require 'dokidoki.v2'

assert(player, 'missing player argument')
assert(velocity, 'missing velocity argument')

local buffer = 500

function update()
  local screen_bounds = v2(game.opengl_2d.width, game.opengl_2d.height);

  self.transform.pos = self.transform.pos + velocity
  
  if self.transform.pos.x > screen_bounds.x + buffer
      or self.transform.pos.y > screen_bounds.y + buffer
      or self.transform.pos.x < -buffer
      or self.transform.pos.y < -buffer then
	   game.log.record_miss(self)
	   self.dead = true
  end
end
