require 'dokidoki.module' [[ make ]]

local game = require 'dokidoki.game'
local kernel = require 'dokidoki.kernel'
local v2 = require 'dokidoki.v2'
require 'glfw'

local blueprints = require 'blueprints'

local music

local args = {}
for _, a in ipairs(arg) do
  args[a] = true
end

function make ()
  return game.make_game(
    {'update_setup', 'update', 'collision_registry', 'collision_check',
     'update_cleanup'},
    {'draw_setup', 'draw', 'draw_foreground', 'fade_draw'},
    function (game)
      math.randomseed(os.time())
      glfw.SetWindowTitle("Pax Britannica")

      game.init_component('exit_handler')
      game.init_component('keyboard')
      game.init_component('opengl_2d')
      game.opengl_2d.width = 1024
      game.opengl_2d.height = 768
      game.opengl_2d.background_color = {0, 0, 0}

      game.init_component('constants')
      game.init_component('collision')
      game.init_component('resources')
      game.init_component('targeting')
      game.init_component('tracing')
      game.init_component('log')
      game.init_component('fast_forward')
      game.init_component('the_one_button')
      game.init_component('debug_keys')

      game.exit_handler.trap_esc = true
      function game.exit_handler.on_close()
        game.log.print_stats()
        kernel.abort_main_loop()
      end

      local function init()
        game.actors.new(blueprints.background)
        game.actors.new(blueprints.background_fx)
        game.actors.new(blueprints.game_flow)
        game.init_component('particles')
      end

      if args['--no-music'] or music then
        init()
      else
        game.actors.new(blueprints.music_loader,
          {'load_music', filename='audio/music.ogg', callback=function(loaded)
             music = loaded
             music:play(1, 1, 0)
             init()
           end})
      end
    end)
end

return get_module_exports()
