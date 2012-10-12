local v2 = require 'dokidoki.v2'

callback = callback or function () end

local counter = 5

function reset_counter()
  counter = 5
end

function update()
  counter = counter - 1/60
  self.sprite.color = {1, 1, 1, math.sin(math.mod(counter, 1) * math.pi)}
  
  if counter <= 0 then
    self.dead = true 
    callback()
  else
    self.sprite.image = game.resources.number_sprites[math.ceil(counter)]
  end
end
