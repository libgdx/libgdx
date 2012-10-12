local v2 = require 'dokidoki.v2'

local stopping = false
local target_fuzzy_pos

local function retarget ()
  target = game.targeting.get_nearest_of_type(self, 'fighter') or
           game.targeting.get_nearest_of_type(self, 'frigate') or
           game.targeting.get_nearest_of_type(self, 'factory')
           
  if target then
    target_fuzzy_pos = target.transform.pos + v2.random() * 250
  end
end

function update()
  -- this math.random stuff is temporary
  if not target or target.dead or math.random() < 0.001 then
    retarget()
  end
  
  if target then      
    target_distance = v2.mag(target.transform.pos - self.transform.pos)
    speed_square = v2.sqrmag(self.ship.velocity)
    
    if self.frigate_shooting.is_ready_to_shoot() and speed_square > 0 then
      stopping = true
    elseif self.frigate_shooting.is_empty() then
      stopping = false
    end

    if not stopping then
      if target_distance < 200 then
        --not too close!
        self.ship.go_away(target_fuzzy_pos, true)
      else
        self.ship.go_towards(target_fuzzy_pos, true)
      end
    end
    
    -- Shoot when not moving and able to fire
    if not self.frigate_shooting.is_empty() and speed_square < 0.01 then
        self.frigate_shooting.shoot()
    end
  end
end
