dokidoki_disable_debug = true
require 'dokidoki.module' [[]]

local kernel = require 'dokidoki.kernel'

local the_game = require 'the_game'

local args = {}
for _, a in ipairs(arg) do
  args[a] = true
end

kernel.set_ratio(4/3)

if args['--windowed'] then
  kernel.set_video_mode(1024, 768)
else
  kernel.set_fullscreen(true)
end

kernel.start_main_loop(the_game.make())
