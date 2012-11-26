local gl = require 'gl'
local blueprints = require 'blueprints'
local v2 = require 'dokidoki.v2'

function draw()
  gl.glPushMatrix()
    gl.glTranslated(1024/2, 768/2 + 768/4, 0)
    game.resources.title_sprite:draw()
  gl.glPopMatrix()
  
  gl.glPushMatrix()
    gl.glTranslated(1024/2 + 265, 768/2, 0)
    game.resources.credits_sprite:draw()
  gl.glPopMatrix()
  
  --gl.glPushMatrix()
    --gl.glTranslated(1024/2, 768/2 - 768/6, 0)
    --game.resources.press_a_sprite:draw()
  --gl.glPopMatrix()
end
