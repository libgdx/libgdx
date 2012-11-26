local SCRIPTED_ACTIONS = { { 'fighter', 'fighter', 'fighter', 'fighter', 'bomber' },
                           { 'fighter', 'fighter', 'fighter', 'bomber' },
                           { 'fighter', 'fighter', 'bomber' },
                           { 'fighter', 'fighter', 'fighter', 'frigate' } }

local action_index = 0
local frames_to_hold = 0
local accumulated_frames = 0
local frames_to_wait = 0
local script_index = 0

self.resources.harvest_rate = self.resources.harvest_rate * 0.8

local function next_script()  
  script_index = math.ceil(math.random() * table.getn(SCRIPTED_ACTIONS))
  --print ('player ' .. self.ship.player .. ' picked script #' .. script_index)  
end

local function next_action()
  accumulated_frames = 0  
 
  action_index = action_index + 1
  if action_index > table.getn(SCRIPTED_ACTIONS[script_index]) then 
    action_index = 1 
    next_script()
  end
  
  -- how much time to hold the button
  frames_to_hold = (self.production.UNIT_COSTS[SCRIPTED_ACTIONS[script_index][action_index]] - self.production.UNIT_COSTS['fighter']) / self.production.BUILDING_SPEED + 2 + math.random() * 60
  
  -- how much time to wait before actually performing the scripted action?
  if not self.resources then return end
  local missing_resources = (self.production.UNIT_COSTS[SCRIPTED_ACTIONS[script_index][action_index]] - self.resources.amount) / self.resources.harvest_rate
  frames_to_wait = math.max(missing_resources + 1, 0) + math.random() * 60
  
  --print('producing ' .. SCRIPTED_ACTIONS[action_index] .. ' : ' .. frames_to_hold .. ' held, ' .. frames_to_wait .. ' waited')
end

-- take the first action on init
next_script()
next_action()

function update()
  -- make sure we still have enemies
  local found_enemy = false
  for _, actor in ipairs(game.actors.get('factory')) do
    if actor.ship and actor.ship.player ~= self.ship.player then found_enemy = true end
  end
  if not found_enemy then 
    self.production.button_held = false
    return 
  end

  accumulated_frames = accumulated_frames + 1
  
  self.production.button_held = accumulated_frames > frames_to_wait and accumulated_frames < frames_to_hold + frames_to_wait
  if accumulated_frames > frames_to_hold + frames_to_wait then
    next_action()
  end
end