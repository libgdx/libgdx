local v2 = require 'dokidoki.v2'
local blueprints = require 'blueprints'

local speed = 3.5

function shoot(approach_sign)
  local facing = approach_sign * v2.rotate90(self.transform.facing)
  local start_vel = speed * facing -- + self.ship.velocity
  game.actors.new(blueprints.bomb,
    {'transform', pos=self.transform.pos, facing=facing},
    {'bullet', player=self.ship.player, velocity=start_vel})
end
