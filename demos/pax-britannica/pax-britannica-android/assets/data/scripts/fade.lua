local gl = require 'gl'

assert(from, 'missing from argument')
assert(to, 'missing to argument')
assert(duration, 'missing duration argument')
callback = callback or function () end

local counter = 0;

function update()
  counter = counter + 1

  if counter > duration then
    callback()
    self.dead = true
  end
end

function fade_draw()
  local t = counter / duration
  local opacity = from * (1 - t) + to * t

  gl.glColor4d(0, 0, 0, opacity)
  gl.glBegin(gl.GL_QUADS)
  gl.glVertex2d(game.constants.screen_left, game.constants.screen_bottom)
  gl.glVertex2d(game.constants.screen_right, game.constants.screen_bottom)
  gl.glVertex2d(game.constants.screen_right, game.constants.screen_top)
  gl.glVertex2d(game.constants.screen_left, game.constants.screen_top)
  gl.glEnd()
  gl.glColor3d(1, 1, 1)
end
