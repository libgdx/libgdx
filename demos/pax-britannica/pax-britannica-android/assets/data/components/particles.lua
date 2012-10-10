local v2 = require 'dokidoki.v2'
local particles = require 'particles'

local bubble_emitter = particles.make_emitter(
    game.resources.bubble_sprite.size[1],
    game.resources.bubble_sprite.size[2],
    game.resources.bubble_sprite.tex.name,
    300,
    1,
    0)
    
local big_bubble_emitter = particles.make_emitter(
    game.resources.big_bubble_sprite.size[1],
    game.resources.big_bubble_sprite.size[2],
    game.resources.big_bubble_sprite.tex.name,
    300,
    1,
    0)    

    
local big_explosion_emitter = particles.make_emitter(
    game.resources.explosion_sprite.size[1] / 64,
    game.resources.explosion_sprite.size[2] / 64,
    game.resources.explosion_sprite.tex.name,
    10,
    1,
    30)
    
local mid_explosion_emitter = particles.make_emitter(
    game.resources.explosion_sprite.size[1] / 64,
    game.resources.explosion_sprite.size[2] / 64,
    game.resources.explosion_sprite.tex.name,
    10,
    1,
    15)    
    
local small_explosion_emitter = particles.make_emitter(
    game.resources.explosion_sprite.size[1] / 96,
    game.resources.explosion_sprite.size[2] / 96,
    game.resources.explosion_sprite.tex.name,
    10,
    1,
    10)
    
local tiny_explosion_emitter = particles.make_emitter(
    game.resources.explosion_sprite.size[1] / 128,
    game.resources.explosion_sprite.size[2] / 128,
    game.resources.explosion_sprite.tex.name,
    10,
    1,
    5)    

local spark_emitter = particles.make_emitter(
    game.resources.spark_sprite.size[1] * 1.75,
    game.resources.spark_sprite.size[2] * 1.75,
    game.resources.spark_sprite.tex.name,
    40,
    0.95,
    0)

local emitters = { big_bubble_emitter, bubble_emitter, small_explosion_emitter, mid_explosion_emitter, big_explosion_emitter, spark_emitter }

local background_emitters = {big_bubble_emitter, bubble_emitter}
local foreground_emitters = {small_explosion_emitter, mid_explosion_emitter, big_explosion_emitter, spark_emitter}

function add_bubble(pos)
  bubble_emitter:add_particle(pos.x, pos.y, math.random() * 0.1 - 0.05, 0.01 + math.random() * 0.05)
end

local function explode_big(pos)
  big_explosion_emitter:add_particle(pos.x, pos.y, 0, 0)
  for i = 1, 20 do
    local vel = (v2.random() + v2.random())
    for i = 1, 20 do
      local vel = vel * i/20 * 2
      local offset = v2.random() * 10
      spark_emitter:add_particle(pos.x + offset.x, pos.y + offset.y, vel.x, vel.y)
    end
  end
  for i = 1, 50 do
    local vel = v2(math.random() * 2 - 1, math.random() * 2 - 1) * 5
    local offset = v2.random() * 3
    spark_emitter:add_particle(pos.x + offset.x, pos.y + offset.y, vel.x, vel.y)
  end
  for i = 1, 50 do
    local vel = v2(math.random() * 2 - 1, math.random() * 2 - 1) * 0.2
    local offset = v2.random() * 17
    big_bubble_emitter:add_particle(pos.x + offset.x, pos.y + offset.y, vel.x, vel.y)
  end  
end

local function explode_mid(pos)
  mid_explosion_emitter:add_particle(pos.x, pos.y, 0, 0)
  for i = 1, 10 do
    local vel = (v2.random() + v2.random())
    for i = 1, 10 do
      local vel = vel * i/20 * 2
      local offset = v2.random() * 10
      spark_emitter:add_particle(pos.x + offset.x, pos.y + offset.y, vel.x, vel.y)
    end
  end
  for i = 1, 15 do
    local vel = v2(math.random() * 2 - 1, math.random() * 2 - 1) * 4
    local offset = v2.random() * 3
    spark_emitter:add_particle(pos.x + offset.x, pos.y + offset.y, vel.x, vel.y)
  end
  for i = 1, 20 do
    local vel = v2(math.random() * 2 - 1, math.random() * 2 - 1) * 0.2
    local offset = v2.random() * 10
    big_bubble_emitter:add_particle(pos.x + offset.x, pos.y + offset.y, vel.x, vel.y)
  end  
end

local function explode_small(pos)
  small_explosion_emitter:add_particle(pos.x, pos.y, 0, 0)
  for i = 1, 5 do
    local vel = (v2.random() + v2.random())
    for i = 1, 5 do
      local vel = vel * i/20 * 2
      local offset = v2.random() * 10
      spark_emitter:add_particle(pos.x + offset.x, pos.y + offset.y, vel.x, vel.y)
    end
  end
  for i = 1, 10 do
    local vel = v2(math.random() * 2 - 1, math.random() * 2 - 1) * 3
    local offset = v2.random() * 3
    spark_emitter:add_particle(pos.x + offset.x, pos.y + offset.y, vel.x, vel.y)
  end
  for i = 1, 10 do
    local vel = v2(math.random() * 2 - 1, math.random() * 2 - 1) * 0.2
    local offset = v2.random() * 5
    big_bubble_emitter:add_particle(pos.x + offset.x, pos.y + offset.y, vel.x, vel.y)
  end  
end

local function explode_tiny(pos)
  small_explosion_emitter:add_particle(pos.x, pos.y, 0, 0)
  for i = 1, 2 do
    local vel = (v2.random() + v2.random())
    for i = 1, 5 do
      local vel = vel * i/20 * 2
      local offset = v2.random() * 10
      spark_emitter:add_particle(pos.x + offset.x, pos.y + offset.y, vel.x, vel.y)
    end
  end
  for i = 1, 5 do
    local vel = v2(math.random() * 2 - 1, math.random() * 2 - 1) * 2
    local offset = v2.random() * 3
    spark_emitter:add_particle(pos.x + offset.x, pos.y + offset.y, vel.x, vel.y)
  end
  for i = 1, 5 do
    local vel = v2(math.random() * 2 - 1, math.random() * 2 - 1) * 0.2
    local offset = v2.random() * 5
    big_bubble_emitter:add_particle(pos.x + offset.x, pos.y + offset.y, vel.x, vel.y)
  end  
end

function explode(actor, forced_pos)
  local pos = forced_pos or actor.transform.pos
  local name = actor.blueprint.name

  if name == 'factory' or name == 'frigate' then
    explode_big(pos)
  elseif name == 'bomber' then
    explode_mid(pos)
  else
    explode_small(pos)
  end
end

local function laser_hit(pos, vel)
  for i = 1, 10 do
    local vel = vel + v2.random()
    spark_emitter:add_particle(pos.x, pos.y, vel.x, vel.y)
  end
end

function bullet_hit(ship, bullet)
  local pos = bullet.transform.pos

  -- ugh. . .
  local bullet_vel =
    bullet.bullet and bullet.bullet.velocity or bullet.ship.velocity

  local bullet_dir
  if v2.sqrmag(bullet_vel) == 0 then
    bullet_dir = v2.zero
  else
    bullet_dir = v2.norm(bullet_vel)
  end
  local vel = ship.ship.velocity - bullet_dir * 1.5

  if bullet.blueprint.name == 'laser' then
    laser_hit(pos, vel)
  elseif bullet.blueprint.name == 'bomb' then
    explode_mid(pos)
  elseif bullet.blueprint.name == 'missile' then    
    explode_tiny(pos)
  end
end

game.actors.new_generic('particles', function ()
  function draw ()
    for _, emitter in ipairs(background_emitters) do
      emitter:draw()
    end
  end
  function draw_foreground ()
    for _, emitter in ipairs(foreground_emitters) do
      emitter:draw()
    end
  end
  function update ()
    if game.debug_keys.key_held(string.byte('P')) then
      for i = 1, 10 do
        local vel = v2.random() * 3
        bubble_emitter:add_particle(300, 300, vel.x, vel.y)
      end
    end
    for _, emitter in ipairs(foreground_emitters) do emitter:update() end
    for _, emitter in ipairs(background_emitters) do emitter:update() end
  end
end)
