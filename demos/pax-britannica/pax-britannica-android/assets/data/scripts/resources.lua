local gl = require 'gl'
local blueprints = require 'blueprints'
local v2 = require 'dokidoki.v2'

-- some resources to start with
amount = 60

harvest_rate = 0.75

function update()  
  -- build resources over time
  amount = amount + harvest_rate
end
