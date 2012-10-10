local kernel = require 'dokidoki.kernel'

game.actors.new_generic('fast_forward', function ()
  function update()
    if game.debug_keys.key_held(string.byte(" ")) then
      kernel.set_fps(300)
      kernel.set_max_frameskip(60)
    else
      kernel.set_fps(60)
      kernel.set_max_frameskip(6)
    end
  end
end)
