local kernel = require 'dokidoki.kernel'
local v2 = require 'dokidoki.v2'

local blueprints = require 'blueprints'
local the_game = require 'the_game'

local LEFT = game.constants.screen_left
local RIGHT = game.constants.screen_right
local BOTTOM = game.constants.screen_bottom
local TOP = game.constants.screen_top
local CENTER = v2((LEFT + RIGHT) / 2, (TOP + BOTTOM) / 2)
local RADIUS = 300

local POSITIONS = {
  false,
  {CENTER + v2.unit(math.pi) * RADIUS,
   CENTER + v2.unit(0) * RADIUS},
  {CENTER + v2.unit(math.pi*5/6) * RADIUS,
   CENTER + v2.unit(math.pi*1/6) * RADIUS,
   CENTER + v2.unit(math.pi*9/6) * RADIUS},
  {CENTER + v2.unit(math.pi*3/4) * RADIUS,
   CENTER + v2.unit(math.pi*1/4) * RADIUS,
   CENTER + v2.unit(math.pi*5/4) * RADIUS,
   CENTER + v2.unit(math.pi*7/4) * RADIUS}
}

local state = 'init'
local selectors
local splash

local game_over_timer = 0

local function generate_positions(n)
  local positions = {}
  for i = 1, n do
    positions[#positions+1] = v2(math.cos(i/n), math.sin(i/n)) * 200
  end
  return positions
end

local function start_game()
  local players = {}
  for i, selector in ipairs(selectors) do
    if selector.selector.picked then
      players[#players+1] = i
    end
  end

  if #players > 0 then
    game.actors.new(blueprints.fade_out, {'fade', callback=function ()
      for i, selector in ipairs(selectors) do
        selector.dead = true
      end
      splash.dead = true

      local cpu_player

      if #players == 1 then
        cpu_player = players[1] == 1 and 2 or 1
        players[#players+1] = cpu_player
      end

      local positions = generate_positions(#players)
      for i, p in ipairs(players) do
        local pos = POSITIONS[#players][i]
        local facing = v2.norm(v2.rotate90(pos - CENTER))
        if p == cpu_player then
          game.actors.new(blueprints.easy_enemy_factory,
            {'transform', pos=pos, facing=facing},
            {'ship', player=p})
        else
          game.actors.new(blueprints.player_factory,
            {'transform', pos=pos, facing=facing},
            {'ship', player=p})
        end
      end

      state = 'in_game'
      game.actors.new(blueprints.fade_in)
    end})

    state = nil
  end
end

function update()
  if state == 'init' then
    local dist = 130
    local offset = v2(-75, 0)
    selectors = {
      game.actors.new(blueprints.selection_factory,
        {'transform', pos=offset+v2(1024/2 - dist/2 - dist, 768/2), facing=v2(0, 1)},
        {'selector', player=1}),
      game.actors.new(blueprints.selection_factory,
        {'transform', pos=offset+v2(1024/2 - dist/2, 768/2), facing=v2(0, 1)},
        {'selector', player=2}),
      game.actors.new(blueprints.selection_factory,
        {'transform', pos=offset+v2(1024/2 + dist/2, 768/2), facing=v2(0, 1)},
        {'selector', player=3}),
      game.actors.new(blueprints.selection_factory,
        {'transform', pos=offset+v2(1024/2 + dist + dist/2, 768/2), facing=v2(0, 1)},
        {'selector', player=4})
    }
    splash = game.actors.new(blueprints.splash)

    game.actors.new(blueprints.fade_in)

    state = 'player_select'
  elseif state == 'player_select' then
    local player_selected = false
    for _, s in ipairs(selectors) do
      if s.selector.picked then
        player_selected = true
      end
    end

    if player_selected then
      local c = game.actors.new(blueprints.countdown, {'countdown', callback=start_game}, {'transform', pos=v2(1024/2, 768/2 - 200)})
      for _,s in ipairs(selectors) do
        s.selector.callback = c.countdown.reset_counter
      end
      state = nil
    end
  elseif state == 'in_game' then
    if #game.actors.get('factory') < 2 then
      game_over_timer = game_over_timer + 1
      if game_over_timer > 300 then
        game.actors.new(blueprints.fade_out, {'fade', callback=function ()
          game.log.print_stats()
          kernel.switch_scene(the_game.make())
        end})
      end
    end
  else
    -- no state
  end
end
