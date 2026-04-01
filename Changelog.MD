# 0.1.0-alpha
Initial project setup

# 0.2.0-alpha
Setup gradle to build properly
Java compatibility set to Java 17 for gradle
Version.java included
Game launches with dynamic title "Paper Minecraft + version"

# 0.3.0-alpha
Added block registry.
Added textures: dirt, stone, grass
Game now renders all registered blocks to the screen
Game outputs all registered blocks to the console on load.
TextureManager allows for textures that dont exist to still render with a black/magenta square
BlockRenderer renders everything to the screen

# 0.4.0-alpha
Added constants: CHUNK_WIDTH and CHUNK_HEIGHT
Now a single chunk renders, 16x8 where 50% of the blocks are dirt, 50% are test blocks of a random state
New RenderableLifecycle which is a lifecycle with a spritebatch and camera
ChunkData is stored separately from the rendering
Fixed an issue where blocks wouldn't register their default state
Block `minecraft:test` has been renamed to `minecraft:test_block`

# 0.5.0-alpha
- Worlds are now infinite!
- Renamed BlockRenderer to ChunkRenderer
- The game runs on 5 threads - 4 world gen and 1 render thread
- World generation runs completely separately from the rendering. render() renders the available data, and nothing if there is none
- Inputs are polled 60 times per second, at a constant rate
- Keys WASD polled to move the camera around
- As the player moves around, the chunks are loaded and unloaded.
- The game has a render distance of 3 chunks, meaning 5 chunks are rendered
- When the game closes, the currently generating threads attempt to end
- Chunk height increased to 384 blocks tall
- Blocks off screen are no longer rendered

# 0.6.0-alpha
## Text Rendering
- Text can now render with formatting. Similar to the minecraft formatting codes like `Â§6`, the text can be styled in 16 colours, based on minecraft.wiki.

## Crash Screen
- When any thread crashes, a nice crash screen will show up, stopping the execution of ALL the threads, preventing it from existing in the background

## Performance & Stability
- When the game launches, after all the registries have been initialised, the garbage collector is invoked. This reduces the allocated memory from around 256mb to 20mb.
- Every 1000 ticks (2.7s)
- Thread crashes (not main rendering) will crash the entire game, rather than silently failing
- The `IndexOutOfBoundsException` thrown by `ChunkData` modifications now contains the position and the index attempted to write to.
## Gameplay
- Tick rate has been increased to 360tps, and the position is polled 1x per tick rather than 60.
- Chunks won't render until they have finished generating.
## Debug Screen
- New Renderer: `DebugScreenRenderer`. This contains an additional ShapeRenderer alongside a BitmapFont to draw an overlay behind the text. This supports the text colouring.
- Renders: Version, FPS, TPS, Camera position, Memory usage
## Sky
- New Renderer: `SkyRenderer`. This renders a blue rectangle.
## World Generation
- Added some noise classes, ImprovedNoise is used to make a simple "cave" to generate.
- Surface rule replaces stone with air above with grass, and dirt below that.
- World gen uses `Xoroshiro` rather than `java.util`'s
## Technical
- Camera is now protected rather than private so it can be accessed.
- BlockTags (unused though soon will be)
- Currently the world seed is fixed to 123

# 0.7.0-alpha
## Blocks
- Added Bedrock block. Bedrock generates at the bottom of the world
- Added Deepslate. This replaces all stone (with some slight fuzzing) below y63
- Blocks now store their default background block state, and they default to themselves.
## UI
- Added new layer: SelectorRenderer
This is used to render the block selector to the screen. It also calculates where the player is looking.

## Gameplay
- The camera moves 5 units per tick rather than 1.
- The + and - keys make the world zoom at half the size.
- Pressing left click now destroys the block.
- The camera starts at ground level rather than the bottom of the world

## Performance and Stability
- The render distance is now 16, rather than 3.
- Chunk framebuffer generation is one per few frames, rather than attempting all at once, harming performance.
- Chunk positions are cached in order to prevent some memory leaks
## Debug Screen
- Mouse X and Y are rendered to the debug screen
## Rendering
- Framebuffers are now used rather than individual draw calls. This optimises performance, though their generation in particularly complex scenes may be slightly hindered.
- Below deepslate level, the sky renders black.
### Known issues
- breaking a block causes a slight flicker
## Chunks
- Chunks now have a background layer, and use noise with a height bias in order to make nicer terrain. Caves are carved after the surface layer.


# 0.7.1-alpha
## Performance and Stability
- Fixed a critical issue causing excessive amounts of VRAM to be used when loading chunks, and the GPU memory not being deallocated. This would not cause the game to crash, though the pagefile and computer would slow down drastically. This was caused by only the heap memory being monitored (which remains at roughly 20-40mb), not the actual RAM allocated by LWJGL backend.
- Chunks now validate textures when rebuilding meshes, and pre-emptively load them to allow faster build times.
- Chunks share resources between them.
## Rendering
- Dirty chunks are allowed to render, fixing the flicker from 0.7.0-alpha
## Debug
- The debug screen now renders the actual block underneath the mouse pointer, along with its state and tags.


# 0.8.0-alpha
## Additions
### Splash Screen
When the game launches, a splash screen is shown to allow the game to immediately initialise
Textures such as this can now be force loaded, to ensure they are available on the first frame.
### Blocks
#### Water
Water is a new block. The player can right click to place it, and scroll to change its state.
Water does not currently flow. It exists on a separate layer to the rest of the terrain, and has an animated texture.
The water animation is framerate independent - it relies on the game time.

#### Solidity
Blocks have three levels of solidity now: AIR, WATER, SOLID, SCAFFOLD(unused). These are represented by the AABBRenderer as GREY, BLUE, CYAN, RED respectively.
### The Player
The player extends an entity class, and has collision. IJKL can be used to move the player super quickly (VERY fast with an acceleration of 10K blocks)
Arrow keys can be used to move the player quickly, at a more manageable pace.
WASD remain as before.
Left click to break blocks exists as before, and right click places water.
### Debug
New Debug Renderer: AABBRenderer. This is used to render the AABBs (collision boxes) that exist in the world.
Player position, speed, and the item in their hand along with its state is now in the debug screen.
New Debug Renderer: HitboxRenderer. Displays entities hitboxes.
### Physics
AABBs are used to calculate physics, and the collision of blocks. Blocks are given AABBs from a pool at runtime, and entities are given one to hold in their lifetime that they can modify as they wish.
### Tags
Both WATER and FLOWING_WATER are in a new FLUID tag.
## Changes
### World Generation
Water now generates anywhere below sea level (y=127) where there is air.
If there is an air block below y=5, water currently generates here as a placeholder for lava.
World generation has been abstracted out of the main chunk class.
Some placeholder world generation for caves has also been added, thanks to claude!
### Performance and Stability
Render distance has been reduced to 15.
Game initialisation now takes place on the background thread except for GPU tasks.
Chunk rebuilds are now tracked in a stack, rather than a queue. This means that new chunks will be built first.
### Gameplay
The player now spawns at 0, 128, rather than 16, 128.
### Chunks
ChunkData now stores a reference to its position.


# 0.9.0-alpha
## Additions
### Physics
- Pressing `F` now disables physics for the player
### Blocks
- Oak Logs and Leaves.
- Logs have attribute "DIRECTION"
- Leaves have unused attribute "persistent"
### World Generation
- Trees generate, and respect chunk boundaries **until*- they are unloaded, as there currently is no persistence of the world.
## Changes
### Debug Screen
- The text on the debug screen has been shrunken to 27, and isn't downscaled anymore.
- Debug Render Sections are now stored in the lifecycle, and is a wrapper of the render method, to track is now "draw" and not tracking is the original "render".
- FPS Cap 60 stored in SharedConstants
- Registry is now separate from the game (library)
- Removed queue from chunk building
### Gameplay
- Blocks now default to being 32px rather than 48px for zooming
- Chunk ticking separated to separate class.
## Bugs Fixed
- Block in player hand is now actually gotten from the game, not hardcoded.
- At large distances, players hitbox is rendered properly as a double, not a float .
- At large distances, block selector follows mouse properly, rather than getting stuck at float precision loss points.

# 0.10.0-alpha
## Additions
- Added basis for level serialisation
- Added block breaking animation and time. Each block currently is hardcoded to be 40 ticks.
- Mouse held time time is now tracked.
- Chunks are _written_ to disk, though not read yet.
## Changes
- Java version upgraded to 21.
- Replaced NBT implementation with library.
- Water now tracks if it is a source block
## Bugs Fixed
- Fixed the tick time not being counted into a frame, and the root time not counting to the entire duration.
- Fixed a crash where a framebuffer would not exist if the player was moving too fast.
- Fixed a bug where trees would always generate at the first position of the chunk due to the randomness being reinitialised for every chunk.

# 0.11.0-alpha
## Additions
### Item Entities
- These come in two variants: Item Item entities, and Block Item entities.
    - Block item entities use the texture of the default blockstate.
    - Item item entities do not currently exist in gameplay, therefore do not render.
### Items
- Due to the existence of item entities, items also now exist, and come in two variants: Item items, and Block items. These either represent themselves, or are linked to the blocks registry.
### Gameplay
- When the player breaks a block, an item entity is spawned at the position where the mouse was. The entity does not get deleted when the player gets close yet.
- Block breaking time is no loner hardcoded, and is calculated based on the hardness of the block. The tool speed is assumed to be 2 (wooden pick), and has efficiency 2 applied.
- The default block for the player to place is TEST
### Debug Screen
- Density calculated from x, y is now outputted to the debug screen for future use.
### Hitbox Renderer
- Now also renders any entity hitbox, not just the player.
### Technical
- Two new registries: NOISE and ITEM
## Changes
### World Generation
- Simplified world generation to use a single continentalness noise due to new density function system.
### World Serialisation
- Although unused, the ChunkWriter now writes what blocks and their properties to the saved chunk.
## Removals
- Removed some unnecessary debug outputs to clean up console.
## Bugs Fixed
- Fixed an issue where some labels in the debug chart would show their class name, not their string name.
- Fixed a crash caused by a null framebuffer when the player moves too fast.


# 0.12.0-alpha
This version contains a major rewrite to the rendering engine. Chunks now are meshes, rather than framebuffers, fixing several memory issues and bugs related to chunk rendering and management.
## Additions
- Textures now stored in a texture atlas rather than individual textures.
- Player now has an inventory, and can pick up items to insert it into it.
- ItemEntities that exist on the ground now can actually be picked up, and deleted from the world.
- Pressing left control and clicking now deletes all entities apart from the player fromt he world.
## Changes
- Item entities now cache their textures to fix performance issues with getting them every frame.
- Decreased sky colour brightness slightly.
- Block Item Entities are now considered ItemEntities, rather than entities, so inherit all item attributes.

## Removals
- Cleaned up debug screen, removing unnecessary information. These may be re-added in the future, but now there is performance gains due to the calculations being removed.
- AABB debug render now removed to combat memory leakage.
- Only the player's hitbox now renders.
## Bugs Fixed
- Fixed several memory churning related to entity and physics.
- Getting the block from the global context caches the chunk position, fixing a memory churn.
- Entities now release their bounding box.
- Fixed an issue where chunks sometimes would not gain a mesh on chunk generation.
- Fixed a crash where moving too fast would crash the game due to no frame buffer
- Fixed memory churn when looking up resource locations.


# 0.13.0-alpha
This version clears up some issues with the new Mesh based rendering system, and adds more interaction with the inventory.
- Added texture for hotbar, hotbar selected item.
## Additions
- Re-added the `test[test=fail]` block.
- The `[facing=]` block state property now actually influences the mesh.
- The player now spawns in with the following blocks in the inventory, that they would not be able to gain through normal gameplay:
    - `minecraft:bedrock`
    - `minecraft:test[test=false]`
    - `minecraft:test[test=true]`
- Scrolling the mouse now cycles through the hotbar, rather than cycling a specific property.
### GUI
- Added HUD of a hotbar. Items in the first 9 slots of the inventory (the hotbar) will now render there, and displays what item the player is about to place.
### Textures
- Blocks can now specify a uv coordinate for their blockstate.
## Changes
- Texture atlas now checks for the blockstate, not the block.
- The texture atlas is no longer upside down .
- Cleaned up logic related to block breaking. Stricter requirements on whether the renderer can display breaking texture, and if it can break the block now implemented through the `#unselectable` and `#unbreakable` block tags.
- Made item merging more strict, if a block is in the set `#ignore_state` then it will disregard the state when adding it to the inventory. Currently only `minecraft:deepslate` is in this category.
- Being able to replace a block with right click is now controlled through  a tag `#can_place_through`. This contains `minecraft:air` and `minecraft:water`.
### Stability and Performance
- Reduced the number of vertices allocated to the chunk mesh, saving 50% memory use.
## Removals
- Removed chunk serialisation. This will be reimplemented in the future in a more stable and performant way.
## Bugs Fixed
- Fixed some block variants not rendering the correct texture.
- Fixed item entities not displaying the block state, just the block
- Fixed the texture uvs being offset by 1 pixel.


# 0.14.0-alpha
## Additions
- Added a structure system with placement rules.
- Added inventory renderer with drag and drop handler (from old project). and inventory. Currently items can be moved around to any slot.
- Started to add player rendering. The players head and main body render.
- Added new block tags: `#minecraft:world_gen_replaceable`, and `#minecraft:world_gen_structure_replaceable`. These are used in world generation.
- The hotbar slots can now be cycled through with the number keys
- new block state property: `TOP`, which is used by the door block in order to gather if the door half is the top or not.
- Blocks can now choose if they change their state for the background block. Currently the `minecraft:test` block when breaking now reflects the background state properly, rather than defaulting to the original `[test=false]` state.
### Blocks
- Coal Ore
- Deepslate Coal ORe
- Iron Ore
- Deepslate Iron Ore
- Gold Ore
- Deepslate Gold Ore
- Diamond Ore
- Deepslate Diamond Ore
- Redstone Ore
- Deepslate Redstone Ore
- Cobblestone
- Mossy Cobblestone
- Stone Bricks
- Mossy Stone Bricks
- Torch
- Spawner
- Oak Planks
- Oak Door
### Structures
- Structures can now generate
- Currently a path structure (terrain following), dungeon structure (anywhere underground), and a house structure (anywhere on the surface)
### Features
- Trees no longer get cut off by the chunk borders

## Changes
- Moved the debug renderers to a specific debug renderer
- The smallest zoom size now is 8 pixels, rather than the 16 previously. This allows more of the world to be seen.
- Chunks now generate in stages. These are:
- EMPTY, NOISE, SURFACE, CARVERS, STRUCTURES, FEATURES, FULL
- Carvers is currently un used, but will be used when caves are added.
- The world seed is now randomly decided on game launch, rather than a fixed `123`.
- Chunks are loaded around the player centred on screen, rather left to right.
- Player's camera offset is now centred when inventory is open, rather than the mouse being able to interact outside.
- Decreased player render distance to 10
## Removals
- Removed some unnecessary logging from the inventory.
## Bugs Fixed
- Fixed a crash where a null chunk vertex buffer would lead to crashing.
- Fixed the MeshChunkRenderer's camera and spritebatch not being destroyed on exit.
- Features like trees are cut off on chunk borders.


# 0.15.0-alpha
## Additions
### World Generation
- Added `minecraft:temperature` and `minecraft:humidity` noises
### Biomes
- Biomes now exist in the registries
- Biome sampler added. This is currently unused.
Added the following biomes:
- PLAINS
- FOREST
- MUSHROOM_FIELDS
- FROZEN_OCEAN
- DEEP_FROZEN_OCEAN
- COLD_OCEAN
- DEEP_COLD_OCEAN
- OCEAN
- DEEP_OCEAN
- LUKEWARM_OCEAN
- DEEP_LUKEWARM_OCEAN
- WARM_OCEAN
- SNOWY_PLAINS
- SUNFLOWER_PLAINS
- ICE_SPIKES
- SNOWY_TAIGA
- TAIGA
- OLD_GROWTH_SPRUCE_TAIGA
- OLD_GROWTH_PINE_TAIGA
- OLD_GROWTH_BIRCH_FOREST
- DARK_FOREST
- BIRCH_FOREST
- FLOWER_FOREST
- SAVANNA
- JUNGLE
- SPARSE_JUNGLE
- BAMBOO_JUNGLE
- DESERT
## Fixes
- Items now respect their max stack size
- Added missing items for many blocks

# 0.16.0-alpha
## Additions
- Added biome into world generation
### Crafting System
- Added full 3x3 crafting system with recipe matching
- Added crafting table GUI with 3x3 crafting grid
- Added support for shaped crafting recipes
### Gameplay/UI
- Added ESC key support to close any open screen
- Added smart spawn position logic - player now spawns on land instead of in oceans (searches up to 16,000 blocks for valid land biome)
### Blocks
Added:
- Red, orange, lime, green, light blue, blue, cyan, purple, magenta, black, white, light grey, grey, pink: concrete, concrete powder, glazed terracotta, terracotta, wool
- Crafting table
- Furnace
- Tall Grass
- Bamboo, bamboo: planks, block, mosaic, door
- Barrel
- Sandstone
- Gold block
- Netherite block
- Ancient Debris
- Wheat
- Ladder
- Netherrack
- Iron block
- Diamond block
- Cobbled Deepslate
- Clay
- Flowers: Allium, Azure Bluet, Cornflower, Dandilion, Lily of the Valley, Oxeye Daisy, (purple, white, red, orange) tulip, Lilac, Peony, rose
- Jungle: sapling, logs, door, planks, leaves
- Cocoa beans
- Sand
- Short grass

### Items
- Sticks
- Dyes (all 16 colours)
- Netherite (ingot, scrap, tools, armour)
- Diamond (gem, tools, armour)
- Iron (ingot, nugget, tools, armour)
- Gold (ingot, nugget, tools, armour)
- Stone (pickaxe, axe, shovel, hoe, sword)
- Wood (pickaxe, axe, shovel, hoe, sword)
- Bread, empty bucket, bonemeal, bricks
- Cocoa beans (item), oak door (item), cookie, bone, clay ball
- Ink sac, water bucket, wheat (item), bamboo (item)

## Changes
- Player now animates when walking around properly
- Ocean biomes are now generated at C=0 rather than slightly lower.
- Changed inventory open check to generic screen open check
- Player hand item now syncs properly with inventory changes
- Hotbar and inventory now render non-block items correctly
- Added green tint rendering for leaf blocks in inventory/hotbar
### Debug Screen
- Added terrain density function information to the debug screen
- Added biome information display (current biome name)
- Added noise value display (continentalness, temperature, humidity, weirdness)
- Added terrain debug info (ground level, player depth relative to surface)
### World Generation
- Added weirdness noise
- Complete ChunkGenerator rewrite with biome-aware terrain density functions
- Terrain now varies by biome with proper height variation and roughness
- Tree density and type now varies by biome
- Ore placement now properly respects structure boundaries
- Improved deepslate generation transition
- Added biome-specific terrain generation with height variation, roughness, and base height parameters
- Added biome blending system for smooth transitions between biomes
- Added biome-specific tree generation (5 tree types: Oak, Spruce, Birch, Jungle, Acacia)
- Added biome-specific surface blocks (sand for deserts, clay for oceans, grass for other biomes)
- Added structure bounding box system to prevent features from spawning inside structures
- Added jungle tree generation with jungle logs and leaves
## Bugs Fixed
- Fixed some concurrency issues in the world
- Fixed tree placement crash when trees would exceed world height
- Fixed ore placement bug where ores could overwrite structures
- Fixed crafting output slot behaviour - properly consumes ingredients and updates preview
### Player Rendering
- Complete rewrite of player renderer with proper limb separation (head, body, arms, legs)
- Added walking animation with sine wave limb rotation
- Added directional flipping - player faces left/right based on movement
- Added head tracking - head rotates to follow mouse cursor


# 0.17.0-alpha
- Added bricks texture to texture atlas
- Texture atlas now supports 32\*32 textures (1024), from 16\*16. This _has_ doubled the memory usage by the texture, though this impact is nominal (only a few Kb)
- The player now renders properly on the inventory screen
- All hardcoded instances of the texture map size have been replaced. We now commit to the atlas based approach.
## Bugs Fixed
- Fixed the player rendering on top of all GUIs



# 0.18.0-alpha
## Additions
### Mining System
- Added proper tool-based mining with mining speeds and tool requirements
- Blocks now require specific tool tiers (fist, stone, iron, diamond)
- Mining progress bar now reflects actual mining time based on tool effectiveness
### Tools
- Added wooden tools: pickaxe, axe, shovel, hoe, sword
- Tools have durability that depletes with use
- Durability bars render on items in hotbar and inventory containers
### Block Tags System
- Complete rewrite of block tags to support JSON loading from assets
- New tool-related tags: `#mineable_axe`, `#mineable_pickaxe`, `#mineable_hoe`, `#mineable_sword`, `#mineable_fist`
- New tier tags: `#needs_fist_tool`, `#needs_stone_tool`, `#needs_iron_tool`, `#needs_diamond_tool`
- Added `#planks` tag
### Loot Tables
- New LootTableManager for loading loot tables from JSON files
- Loot tables loaded at game startup
### Item Entities
- Regular items (not just blocks) can now be dropped into the world
- Item entities render properly for both block and non-block items
### Blocks
- Oak Door with open/closed states and proper hitbox
- Oak Slab with top/bottom placement
- Oak Stairs with proper facing and corner rendering
- Mesh chunk rendering supports partial blocks (doors, slabs, stairs)
### Durability System
- ItemStack now tracks damage value
- Tools can be depleted and destroyed
- Durability bars render with colour gradient (green to red)
## Changes
- BlockTags now loads from JSON files in assets
- ItemStack constructors updated to support damage values
- Item merging now respects damage values for tools
- Hotbar and container renderers updated for durability display
- Mesh chunk handles special block rendering (doors, slabs, stairs)
## Bugs Fixed
- Fixed item rendering to handle non-block items in containers and hotbar
- Fixed block breaking calculations to use proper tool speeds



# 0.19.0-alpha
This version was heavily influenced by GPT-5.3-codex, Kimi-K2.5, and Minimax M2.5. Credit for the contents of the levelgen folder is no longer my own. I can take credit for any contents _outside_ of this folder.
## Additions
### Vanilla World Generation System
- Complete port of Minecraft's world generation using density functions
- New `VanillaNoiseStep` class implementing noise_router from JSON
- New `VanillaWorldgenConfig` loading worldgen settings from JSON
- New `SurfaceSystem` and `SurfaceRules` for surface layer generation
- New `FeatureRegistry` loading placed features from JSON
### Biome System
- Added erosion noise parameter to biome selection (critical for terrain-biome alignment)
- Biome sampling now uses 5 parameters: continentalness, temperature, humidity, weirdness, erosion
- Biome sampling now uses BLOCK coordinates for proper terrain alignment
New biomes added:
- Shore: Stony Shore, Snowy Beach
- Mountain: Frozen Peaks, Jagged Peaks, Stony Peaks, Snowy Slopes, Grove
- Badlands: Badlands, Eroded Badlands, Wooded Badlands
- Windswept: Windswept Hills, Windswept Gravelly Hills, Windswept Savanna
- Cave: Dripstone Caves
- Swamp: Mangrove Swamp
### Chunk Loading Screen
- New renderer displaying while world generates around player
- Shows dirt background pattern
- Displays current chunk generation status
- Spawn position now waits for chunks to fully generate (FEATURES status)
- Player starts at fixed position until chunks ready, then teleports to valid surface
### World Generation
- Added ore placement using VanillaWorldgenConfig
- Added biome feature placement (trees, flowers, grass, etc.)
- Surface rules now loaded from JSON
- Noise parameters now loaded from JSON
### Blocks
- TERRACOTTA
- COARSE_DIRT
- GRAVEL
- CALCITE
- MUD
- MYCELIUM
- PODZOL
- ICE
- PACKED_ICE
- POWDER_SNOW
- SNOW
- RED_SAND
- RED_SANDSTONE
### Debug Screen
- Debug screen now shows erosion noise value
- Added surface detection showing surface block and blocks below
- Added density value display
- Added generation timing per chunk status
- Fixed to use current player position instead of previous tick position
## Changes
- ChunkStatusOverlay enhanced with surface analysis and generation timing
- BiomeManager completely rewritten with erosion-aware selection
- Noises now uses VanillaWorldgenConfig for noise creation
- Changed humidity to use "vegetation" noise
- Changed weirdness to use "ridge" noise
- Chunks now need to be FULLY generated to be rendered.
## Bugs Fixed
- Fixed debug screen showing incorrect position data
- Fixed crashes due to buffers being destroyed while a chunk had been unloaded after it was loaded

# 0.20.0-alpha
## Additions
- All blocks now have their respective hardness values, so breaking them takes time.
- Added recipes for barrel, bonemeal, diamond chestplate, leggings, shovel, furnace, gold block, ingot, nugget, iron ace, iron hoe, iron nugget and ingot, iron pickaxe, red sandstone, sandstone, torch
- New block tag: mineaeble_shovel for blocks that find shovels faster (dirt, grass_block, sand, gravel)
- All entities now fall slower in water
- Players can now sprint
- Player can now step up blocks
- Shovels are now considered tools to speed up breaking blocks
- Wood is now considered a valid tier required to break blocks
## Changes
- (deepslate too) coal, diamond, gold, iron and redstone ores now drop as their raw items, rather than themselves. And they require a pickaxe of a minimum level.
- Loading screen now shows individual status for each chunk rather than the one of the lowest level
- Chunks are reqired to be at features rather than full to be rendered (there is no difference functionally)
- When closing the inventory, items are thrown into the world rather than remaining there.
- Blocks now on break can choose to not change their background block properly. This was previously not functional.
- Ore generation has been quartered

## Bugs Fixed
- Fixed stone axe and shovel being unobtainable
- Fixed breaking times for oak logs and crafting table and planks
- Fixed inventory items being left behind on close.
- Player could not step up slabs and stairs
- Fixed block property `doIChangeStateForBg()` not being respected.
- Fixed the player sinking slightly when pressing `f` to disable gravity.

# 0.21.0-alpha
## Additions
### Furnace

- Furnace block with smelting
- Furnace GUI with input, fuel, and output slots
- Smelting recipes for raw iron and raw gold into ingots
- Fuel system with burn time tracking, coal, charcoal, logs, planks, sticks
- Visual lit state when active, LIT block state property

### Barrel System
- Functional barrel block for storage
- Barrel GUI with 27 slots
- `BarrelBlockEntity` for inventory
- `BarrelRenderer` for GUI rendering

### Block Entities
- `BlockEntityManager` for managing block entities
- Block entities tick with chunks
- Block entities saved and loaded on placement and removal

### Recipes
- Furnace recipes, gold_ingot_smelting, iron_ingot_smelting
- Crafting recipes, iron_block, iron_ingot_from_nuggets

### Gameplay
- Right click furnace opens furnace GUI
- Right click barrel opens barrel GUI
- Furnace and barrel drop contents when broken
- Furnace fuel items in starting inventory for testing

### Technical
- `FURNACE` and `BARREL` screen states in `ScreenState`
- `LIT` block state property for furnace
- `FURNACE_FUELS` item tag for fuel items
- Updated block tags, mineable_fist and needs_fist_tool include barrel and crafting_table

## Changes
- Furnace and barrel require correct tools for efficient mining
- Block breaking handles block entity cleanup
- Starting inventory includes barrel, furnace, raw iron, coal


# 0.22.0-alpha
## Additions
### Title Screen
- New title screen on game launch with dirt background
- Play and Quit buttons
- Splash texts
- Game logo and version display

### Tooltip System
- Item tooltips in inventory containers
- Shows item registry name on hover
- Tooltips render at mouse position

### Loading State
- Step-by-step loading progress during bootstrap
- Loading steps: textures, registries, tags, recipes, loot tables, input
- Error handling with detailed logging for each step
- Splash screen displays current loading step
- World creation deferred until Play button clicked
- `TITLE` screen state as default
- `startGame()` method initiates world creation
- World generation happens asynchronously after title screen

### Thread Safety
- Custom ThreadFactory for worker threads with exception handling
- Uncaught exceptions in worker threads now logged properly
- Thread naming: "Minecraft-Worker"

## Changes
- Recipe fixed: stone_pickaxe now uses direct item instead of tag
- Bootstrap rewritten with 7 discrete steps and error recovery
- Renderer creation extracted to separate method
- Player reference changed from field to method to prevent null issues

## Removals
- Removed unnecessary logging on startup
## Bugs Fixed
- Fixed potential null pointer when accessing player before world creation


# 0.23.0-alpha

## Major Features

### World Save/Load System
- Implemented complete world persistence system using NBT format
- Created `GameSession` class to manage active world state
- Created `WorldSaveManager` for managing save folder structure and world list
- Created `WorldFile` for serialising/deserialising world data
- Created `ChunkFile` for saving/loading individual chunks
- Created `PlayerFile` for saving/loading player data
- Saves stored in `%appdata%/.paper-minecraft/saves/` folder
- Supports multiple saved worlds with metadata (name, last played, seed, game time)

### Menu Screens
- **World Selection Screen** - Lists saved worlds with preview info, allows select/delete/create
- **World Creation Screen** - Create new world with custom name and seed input
- **More World Options Screen** - Additional world configuration options
- **Pause Screen** - Pause menu accessible by pressing ESC during gameplay
- **Saving World Screen** - Shows "Saving world..." progress screen
- Added new ScreenState enums: `WORLD_SELECTION`, `WORLD_CREATION`, `MORE_WORLD_OPTIONS`, `PAUSE`, `SAVING_WORLD`

## Bug Fixes

### Surface Generation Fixes
- Fixed surface generation to not skip surface blocks when caves exist below the surface
- Removed overly strict `isConnectedToMainTerrain()` check that was causing bare stone
- Removed gap-tracking that would break surface generation near caves
- Surface rules now apply properly even when caves are near the surface
- Fixed biome sampling for negative X coordinates using `Math.floor()` instead of int cast

### Biome System Fixes
- Fixed biome-terrain alignment by removing conflicting manual terrain parameters
- Biome debug info now uses cached Chunk biome data instead of recalculating

### Entity System Fixes
- Fixed item entities throwing items too far (reduced from 0.2 to 0.1 velocity)
- Fixed null pointer exception when checking world in entity movement
- Entities now properly marked as removed when chunk unloads
- Fixed ConcurrentModificationException during entity iteration

## Refactoring

### Biome System Refactoring
- Removed manual terrain parameters from Biome class (baseHeight, heightVariation, roughness, hasSurface)
- Terrain now driven entirely by VanillaNoiseStep density functions
- Removed terrain parameter getters/setters and blended terrain code from ChunkGenerator
- Cleaned up Biomes.java - removed all unused terrain parameters
- Added `getFoliageColour()` method (fixed spelling from "Foiliage")

### Minecraft.java Refactoring
- Major refactor to delegate world/game management to GameSession
- Removed direct `world` and `player` fields from Minecraft class
- Added `getWorld()`, `getPlayer()`, `getSession()` accessor methods
- World renderers now managed by GameSession
- Added transition methods between screens: `transitionToTitle()`, `transitionToWorldSelection()`, `transitionToWorldCreation()`, etc.
- Added `saveAndTransition()` and `saveAndQuitToWorldSelection()` for save handling

### World System Refactoring
- Added gameTime tracking to World class
- Added world metadata: worldName, saveFolderName, isNewWorld
- Added `saveWorld()` and `saveChunk()` methods
- Chunk unloading now saves data before cleanup
- Added surfaceY and biome caching to Chunk class

### Timer Improvements
- Added tick skip detection - logs warning and skips when behind by 60+ ticks
- Timer now properly handles paused state
- Added cap for deltaNano to prevent overflow issues

## Gameplay Changes

### Spawn Height Fix
- Player now spawns on valid land surface instead of in oceans
- Spawn position waits for chunks to fully generate before setting

### UX Improvements
- ESC now opens pause menu
- Inventory opens with E key (when not in pause screen)
- Closing crafting table no longer requires immediate interaction

### Block Properties
- Crafting table, Furnace, and Barrel now have `solidity(Solidity.AIR).noCollision()` for proper non-collidable interaction

### Loot Table Changes
- Redstone Ore now drops 4 redstone dust (was 1)
- Deepslate Redstone Ore now drops 4 redstone dust (was 1)

### New Recipes
- Added diamond_axe crafting recipe
- Added diamond_axe_right (right-click) crafting recipe

### Debug Screen Improvements
- Shows timer state (paused/running)
- Shows total tick count
- Shows current game state

### Title Screen
- Logo animation speed reduced (rotation from 45 to 22.5 degrees)

### Checklist Items Completed
- Add block entities
- Add furnace
- Implement tools properly
- Biome surface blocks properly
- Data driven tags
- Implement proper splash screen
- Fix player spawn height
- Fix UX like opening crafting table immediately

## Technical Changes
- Updated version to 0.23.0-alpha
- Added DATA_VERSION = "100"
- All renderers and world access now use `Minecraft.getInstance().getWorld()` instead of direct field access
- Container rendering now properly uses session-based world reference
- InventoryGrid slot detection fixed to use mouse position parameters
- InventoryRenderer and CraftingTableRenderer changed from static to instance initialised flags
- Entities now have UUIDs


# 0.24.0-alpha
## Additions
- Can skip through time using `-` and `=` keys. This only influences the day night cycle, not the speed of the game.
- Added lighting system. Light flows in a breadth first way until it hits a block, at which point it will decrement by a new block property `filtersBy`, defaulting to 3.
- Entities will render at the correct brightness.
- Block light spreads in a diamond shape.
- Sky will change colour depending on the time of day
- Skylight strength will change on the time of day (purely visual).
- Sky and block light at the mouse position are now on the debug screen
- Blocks can emit light, or filter light
- Furnace blocks now emit light when on, and automatically stop when turned off
- Light updates update the neighbour chunks if needed.
- Light is a step in chunk generation. Will be recalculated on the neighbour chunks on a new chunk generation.
## Changes
- Changed splash text "Java 20!" to "Java 20 + 1!"
- World now starts at 5000 ticks, rather than the 0 it was previously to account for the new day night system.
- Moved items on debug screen around

## Bugs Fixed
- Fixed torch recipe using tags rather than blocks
- Fixed the 2x2 crafting grid being upside down (by slots)

# 0.25.0-alpha
## Additions
- Added farmland block, wheat seeds, hay bale, and oak saplings
- Oak leaves now decay
- Grass blocks now steadily grow in, and decay, rather than all at once
- oak leaves now need a hoe tool to drop themselves. otherwise they drop air.
- Added recipe for bread, bucket, hay bale
- Right clicking on `#drops_grass_on_hoe` with hoe drops seeds (`grass_block`)
- right clicking on `#farmable_blocks` with hoe turns them into farmland. (`grass_block`, `dirt`)
- Growing wheat and oak doors now need base support
- Random tick speed can be configured in more world options
## Changes
- Chunks must be at least of status LIGHT to render.
## Bugs Fixed
- Loading world screen no longer appears on generation and loading of world
- Block selector now respects light value
- Fixed debug screen not showing the mouse coords rather than blockx ,and blocky's light values

## 0.26.0-alpha

## Additions
### Mobs
- Added **Cow** mob with health (10 HP), AI goals (wander, panic, tempt), breeding mechanics, baby/adult variants, and death drops
- Added **Pig** mob with same features as Cow
- Added **Zombie** hostile mob (20 HP) with player tracking, attack behaviour, sunburn mechanic (burns in daylight), baby variant, and spawner spawning support

### Mob System
- Added `LivingEntity` base class with health, damage, healing, air/drowning, and health regeneration
- Added `Mob` class extending LivingEntity with AI goal system, movement, jumping, entity separation
- Added AI goals: `AIGoal` interface, `WanderGoal`, `FollowPlayerGoal`, `TemptGoal`, `CowTemptGoal`, `CowPanicGoal`
- Added `MobRenderer` generic class for rendering mobs with models
- Added mob models: `MobModel`, `CowModel`, `ZombieModel`, `PigModel`

### Mob Spawning System
- Added `SpawnRulesManager` loading spawn rules from JSON with configurable caps, intervals, conditions
- Added `PassiveMobSpawner` and `HostileMobSpawner` for natural spawning
- Added `SpawnGroup`, `SpawnCondition`, `GrassSurfaceSpawnCondition`, `SpawnLightUtil`
- Added spawn rules JSON files: `assets/spawn_rules/passive.json`, `assets/spawn_rules/hostile.json`

### Items
- Added spawn eggs: `COW_SPAWN_EGG`, `ZOMBIE_SPAWN_EGG`, `PIG_SPAWN_EGG`
- Spawn eggs spawn adults on blocks, babies when clicked on same mob type

### Loot Tables
- Added entity loot tables: `cow.json`, `pig.json`, `zombie.json`

### World Options
- Added configurable mob spawning settings in More World Options screen:
  - Passive/hostile mob spawning toggle
  - Spawn interval ticks for both types
  - Mob caps for both types
  - Starter inventory toggle (now functional)

### Rendering
- Added `getSkyLight()` and `getSkyLightAt()` methods to `RenderableEntity`
- Added multiline tooltip support for tool attributes

### Chunk Loading
- Added `TICKING_DISTANCE` (12) and `WORLDGEN_DISTANCE` (22) constants
- Added `amIInWorldgenDistance()` to Chunk class

## Changes
- **Render distance** increased from 10 to 16 chunks
- **Chunk generation** now uses separate distances for ticking (12), rendering (16), and worldgen (22)
- **Player class** refactored: starter inventory now optional via constructor parameter
- **Starter inventory** now includes spawn eggs and spawners for testing
- **ToolItem** enhanced with attack damage attribute system (`AttributeValue` class) and tool tooltips
- **WorldData** extended with mob spawning configuration fields
- **World.tick()** now respects ticking distance and calls mob spawners
- **Chunk.unload()** now calls `entity.destroy()` instead of just setting removed flag
- **MeshChunkRenderer** skips chunks outside render distance, added debug gamma override (G key)
- **PlayerRenderer** added try-catch for light brightness to prevent crashes
- **Player** can now attack mobs, interact with cows (feed wheat to breed), and use spawn eggs

## Bugs Fixed
- Fixed entity cleanup: removed entities now have `destroy()` called properly
- Fixed potential crash when player light data unavailable

# 0.27.0-alpha

## Additions
### Sound System
- Added complete sound system with `SoundLoader`, `SoundSystem`, and sound registry
- Sounds loaded during bootstrap with progress tracking
- Added `SOUND` registry to `Registry.java` for managing sound assets
- Press `P` key to play amethyst block placement sounds
- Entity footstep sounds play every ~1 block of horizontal distance when on ground

### Mob Sounds
- **Cow**: Added hurt sound (with variants) on damage, death sound
- **Pig**: Added hurt sound (with variants) on damage, death sound
- **Zombie**: Added attack hit sound, hurt sound (with variants), death sound

### Player Sounds
- Added damage hit sound when attacking mobs
- Added footstep sounds while walking (stone step sounds)
- Added block breaking/dig sounds when destroying blocks

### World Selection Screen
- Displays world version last played
- Displays world save file size

### World Save System
- Added `LastVersion` tag to world file saves
- Added `getWorldFolderSize()` and `calculateFolderSize()` methods to `WorldSaveManager`
- `WorldInfo` now stores `lastVersion` and `fileSize` fields

### Entity System
- Added `distanceTravelledSinceLastStep` tracking in Entity class for footstep sounds
- Added `playStepSound()` method to Entity base class
- Added null checks for `entity.bb` in `ItemEntityRenderer` and `MobRenderer` with error logging
- Added null check in `RenderableEntity.getLightBrightness()` for null bb

### Rendering
- Added bounds check in `SelectorRenderer` to prevent selector from rendering outside vertical world bounds

## Changes
### Bootstrap Process
- Sound loading now occurs during bootstrap with dedicated loading step
- Added `assetsQueued` atomic boolean and `bootStartTime` for timing measurements
- Changed rendering flow to load assets asynchronously in the render loop
- Loading screen now shows asset loading progress percentage

### Zombie Behaviour
- Increased attack cooldown from 20 to 40 ticks
- Fixed `tickSunburn()` to use session timer (`getSession().getTimer().totalTicks`) instead of world gameTime

### Entity Cleanup
- `Chunk.unload()` now removes entities from both chunk entities list AND world entities list
- Added duplicate entity prevention by UUID when loading chunks from save files

### World Info Display
- World selection screen now shows "Last played: [date]  |  Version: [version]"
- World selection screen now shows "Time played: [time]  |  Size: [size]"


## Bugs Fixed
- Fixed potential crashes in `ItemEntityRenderer` and `MobRenderer` when entity bb is null
- Fixed selector rendering outside world bounds when mouse Y is outside chunk height
- Fixed zombie sunburn timer using wrong time source
- Fixed entity cleanup not removing entities from world entities list on chunk unload
- Fixed duplicate entities appearing when loading world saves (now checks UUID)
- Fixed formatting issues for the splash text `public static void main(String[] args)`
- Fixed formatting issues for the splash text `This is rainbow text`


# 0.28.0-beta
## Additions
### Inventory & Containers
- Added shift-click quick move support across inventory, crafting table, furnace, and barrel screens.
- Added `F` offhand swap handling in both gameplay and container GUIs.

### Biomes
- Added `BiomeDataLoader` to load biome grass, foliage, and sky colours from biome JSON files.
- Added biome colour fallback presets when biome effect data is missing.

### Recipes & Tags
- Added recipe: `iron_ingot_from_block`.
- Added recipe: `stone_from_cobblestone_smelting`.
- Expanded block tags for mining/tool handling (`mineable_axe`, `mineable_fist`, `mineable_pickaxe`, `needs_fist_tool`, `planks`).

### Debug
- Added AABB pool stats to the debug screen (pool size, acquired, released, and net counters).

## Changes
- Updated game version to `0.28.0-beta`.
- Changed no-physics toggle from `F` to `C` so `F` can be used for offhand swaps.
- Player saves now serialise inventory slots `0` to `40` (includes armour and offhand).
- Container interaction base classes were refactored to share slot lookup, shift-click, armour routing, and offhand swap helpers.
- `SolidityAABB` pooling was disabled; new instances are created per query while AABBs continue using `AABBPool`.
- `AABBPool` max size increased to `50000` with warmup and debug counters.
- `ItemStack` merge now returns safely for incompatible stacks instead of throwing exceptions.
- Selector bounds checking now uses freshly calculated mouse world coordinates.
- Out-of-range Y light queries now return a safe brightness value.
- Reduced noisy console output during sound registration.
- Added/updated inline documentation comments across core runtime classes.

## Bugs Fixed
- Fixed offhand/armour inventory persistence by saving and loading extended player slots.
- Fixed potential crashes from invalid item stack merge attempts.
- Fixed selector world-bound checks using stale coordinates.
- Fixed collision/debug AABB release paths to avoid stale pooled bounding boxes.


# 1.0-rc-1
- This is the final version before 1.0.
- Added readme and contribution guidelines.
- Added better code docs.


# 1.0-rc-2
## Additions
- Added f3 toggle for debug infos
## Bugs Fixed
- Fixed chunks with too many blocks not building
- Fixed starter inventory being enabled by default

# 1.0-rc-3
- Fixed registry validation error when launching game if the .minecraft folder does not exist preventing the game from starting.
