local mixer = require 'mixer'

assert(callback, 'missing callback parameter')
assert(filename, 'missing filename parameter')

-- skip first update to allow the loading sprite to draw
local skipped

function update()
  if skipped then
    callback(assert(mixer.load_ogg('audio/music.ogg')))
    self.dead = true
  else
    skipped = true
  end
end
