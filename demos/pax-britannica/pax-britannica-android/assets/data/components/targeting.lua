local v2 = require 'dokidoki.v2'

function on_screen(pos)
  return pos.x >= game.constants.screen_left and
         pos.x <= game.constants.screen_right and
         pos.y >= game.constants.screen_bottom and
         pos.y <= game.constants.screen_top
end

-- returns the closest target of the given type
function get_nearest_of_type(source, ship_type)
  local ships = game.actors.get(ship_type)

  -- find the closest one!
  local closest_ship
  local closest_square_magnitude

  for _, ship in ipairs(ships) do
    local square_magnitude = v2.sqrmag(ship.transform.pos - source.transform.pos)
    
    if source.ship.player ~= ship.ship.player and on_screen(ship.transform.pos) and (not closest_ship or square_magnitude < closest_square_magnitude) then
      closest_ship = ship
      closest_square_magnitude = square_magnitude
    end
  end

  return closest_ship
end

-- return a random ship of the desired type that's in range
function get_type_in_range(source, ship_type, range)
  local ships = game.actors.get(ship_type)
  local ships_in_range = {}
  local range_squared = range * range

  for _, ship in ipairs(ships) do
    local square_magnitude = v2.sqrmag(ship.transform.pos - source.transform.pos)
    
    if source.ship.player ~= ship.ship.player and on_screen(ship.transform.pos) and (square_magnitude < range_squared) then
      ships_in_range[#ships_in_range+1] = ship
    end
  end

  if #ships_in_range > 0 then
    return ships_in_range[math.random(1, #ships_in_range)]
  else
    return false
  end
end
