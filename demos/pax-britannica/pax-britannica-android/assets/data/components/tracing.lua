local gl = require 'gl'

local tracers = {}

local function in_debug_mode()
  return game.debug_keys.key_held(string.byte('`'))
end

function trace_line(from, to)
  if in_debug_mode() then
    table.insert(tracers, game.actors.new_generic('line_trace', function ()
      function draw()
        gl.glBegin(gl.GL_LINES)
        gl.glColor4d(1, 1, 1, 0.3)
        gl.glVertex2d(from.x, from.y)
        gl.glColor4d(0, 0, 0, 0)
        gl.glVertex2d(to.x, to.y)
        gl.glEnd()
        gl.glColor3d(1, 1, 1)
      end
    end))
  end
end

function trace_bar(pos, progress)
  if in_debug_mode() then
    table.insert(tracers, game.actors.new_generic('arrow_trace', function ()
      function draw()
        local left = -10 + pos.x
        local right = 10 + pos.x
        local bottom = -1 + pos.y
        local top = 1 + pos.y
        local mid = left + (right - left) * progress
        gl.glBegin(gl.GL_QUADS)
        gl.glColor3d(1, 0, 0)
        gl.glVertex2d(left, bottom)
        gl.glVertex2d(right, bottom)
        gl.glVertex2d(right, top)
        gl.glVertex2d(left, top)
        gl.glColor3d(0, 1, 0)
        gl.glVertex2d(left, bottom)
        gl.glVertex2d(mid, bottom)
        gl.glVertex2d(mid, top)
        gl.glVertex2d(left, top)
        gl.glEnd()
        gl.glColor3d(1, 1, 1)
      end
    end))
  end
end

game.actors.new_generic('tracer_cleanup', function ()
  function update_setup()
    for i = 1, #tracers do
      tracers[i].dead = true
      tracers[i] = nil
    end
  end
end)
