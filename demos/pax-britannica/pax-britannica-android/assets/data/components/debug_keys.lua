local debug = false

for _, a in ipairs(arg) do
  enabled = enabled or a == '--debug'
end

function key_pressed(key)
  return enabled and game.keyboard.key_pressed(key)
end

function key_released(key)
  return enabled and game.keyboard.key_released(key)
end

function key_held(key)
  return enabled and game.keyboard.key_held(key)
end
