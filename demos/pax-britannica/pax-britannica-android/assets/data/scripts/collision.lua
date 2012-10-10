assert(poly, 'missing poly argument')
assert(collision_type, 'missing collision_type argument')

local body

function collision_registry()
  body = body or {
    poly = poly,
    type = collision_type,
    actor = self,
    player = self.ship and self.ship.player or self.bullet.player
  }

  body.pos = self.transform.pos
  body.facing = self.transform.facing
  game.collision.add_body(body)
end
