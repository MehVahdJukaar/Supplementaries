Additions:
- Pulleys can now pull down multiple blocks continuously, just like pistons. Experimental
- Pistons can now push tile entities
- Cooperative Pistons: pistons and pulleys can push blocks together. meaning that 2 or more pistons pushing on same contraption at the same time will have double the max push limit.


Bugs:
- added back farmers delight rope tomato and tomato sticks blocks
- fixed a recent worldgen issue
- fixes #2008 - updated quark non_double_door tag for iron/gold gates (renamed from non_double_doors)
- fixes #1567 - books in piles now properly consolidate when one is removed, preventing books from disappearing
- fixes #1856 - restore depth test after cannon HUD renders to prevent breaking Jade/Spyglass integration
- fixes #1915 - clicking on a rope with rope item now walks down to the bottom of the chain and redirects placement as if the player clicked that rope's bottom face, so the new rope is placed below it naturally
- fixed rope knot held block not rendering after world reload (missing requestModelReload on load, surfaced by moonlight 3.0.17)
- fixes #2032 - buntings now properly register for cauldron washing interactions, preserving custom names
- misc fixes