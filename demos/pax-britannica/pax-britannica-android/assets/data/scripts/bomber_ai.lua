local v2 = require 'dokidoki.v2'

local APPROACH_DISTANCE = 275
local COOLDOWN_DURATION = 40
local MAX_SHOTS = 4

local state = 'approach' -- 'turn', 'shoot', 'move_away'

local cooldown_timer = COOLDOWN_DURATION
local shots_counter = MAX_SHOTS

local target = false
local approach_sign = 1

local function retarget()
  target = game.targeting.get_nearest_of_type(self, 'frigate') or
           game.targeting.get_nearest_of_type(self, 'factory') or
           game.targeting.get_nearest_of_type(self, 'bomber') or
           game.targeting.get_nearest_of_type(self, 'fighter')
end

local function revise_approach()
  approach_sign = math.random() < 0.5 and 1 or -1
end

function update()
  if not target or math.random() < 0.005 or target.dead then
    local old_target = target
    retarget()
    if old_target ~= target then revise_approach() end
  end

  if target then   
    local target_position = target.transform.pos
    target_distance = v2.mag(target_position - self.transform.pos)
    target_direction = v2.norm(target_position - self.transform.pos)
    
    local unit_factor = target.blueprint.name == 'frigate' and 0.6 or 1
    
    if target_distance > (APPROACH_DISTANCE+50) * unit_factor then
      state = 'approach'
    end
    
    if state == 'approach' then
      self.ship.go_towards(target_position, true)
      if target_distance < APPROACH_DISTANCE * unit_factor then
        revise_approach()
        state = 'turn'
      end
      
    elseif state == 'turn' then
      self.ship.turn(-approach_sign)
      self.ship.thrust(unit_factor * 0.75)
      if v2.dot(target_direction, self.transform.facing) < 0.5 then 
        state = 'shoot' 
      end
      
    elseif state == 'shoot' then
      self.ship.turn(approach_sign * 0.05)
      self.ship.thrust(unit_factor * 0.75)
      
      cooldown_timer = cooldown_timer - 1
      if cooldown_timer == 0 then
        self.bomber_shooting.shoot(approach_sign)
        cooldown_timer = COOLDOWN_DURATION
        shots_counter = shots_counter - 1
        
        if shots_counter == 0 then
          shots_counter = MAX_SHOTS
          state = 'move_away'
        end
      end
    
    elseif state == 'move_away' then
      self.ship.go_away(target_position, true)
    end    
  end
end
