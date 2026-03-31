# Paper Minecraft 2D - Development Roadmap

## Project Overview
Paper Minecraft is a 2D top-down voxel sandbox game inspired by Minecraft. Currently at version **0.13.0-alpha**, the game features infinite procedurally generated worlds, block breaking/placing mechanics, basic physics, and an inventory system. This roadmap outlines development priorities through to the full release.

**Release Timeline:** 25 Alpha versions → 15 Beta versions → 5 Pre-Release versions → 1-5 Release Candidate versions → v1.0.0

---

## Completed Features (v0.1.0 - v0.13.0)

### Core Systems
- **Block Registry System**: Extensible registry for block definitions with state properties
- **Infinite World Generation**: Chunk-based procedural generation with perlin noise and density functions
- **Mesh-Based Rendering**: GPU-optimized chunk rendering using mesh generation instead of framebuffers
- **Block State Properties**: Blocks can have directional properties (e.g., logs with facing direction)
- **Texture Atlas**: Single texture atlas for efficient block rendering with blockstate-specific UVs

### World Features
- **Terrain Generation**: Surface rules applying grass/dirt, deepslate layers, and bedrock foundation
- **Procedural Trees**: Oak logs and leaves generate in forest areas, respecting chunk boundaries
- **Water System**: Water blocks that generate below sea level, with water source block tracking
- **Caves**: Basic cave generation using noise-based air carving
- **Block Breaking**: Time-based block destruction with hardness calculations and tool efficiency

### Player & Interaction
- **Player Entity**: Physics-enabled player with collision detection and movement
- **Inventory System**: 32-slot inventory with hotbar support
- **Hotbar HUD**: Visual representation of hotbar with selected item indicator
- **Block Picking**: Raycasting-based block selection and placement
- **Item Entities**: Ground-based item entities that can be picked up and added to inventory
- **Block Breaking Animation**: Visual block breaking progress before destruction
- **Physics & Collision**: AABB-based collision detection for entities and blocks

### Rendering & UI
- **Debug Overlay**: Real-time performance metrics (FPS, TPS, memory usage)
- **Player Hitbox Rendering**: Visual debugging of entity collision boxes
- **Sky Rendering**: Dynamic sky color changes based on depth
- **Block State Tags**: Semantic tags for controlling block behavior (#can_place_through, #unbreakable, etc.)
- **Splash Screen**: Loading screen shown during initialization

### Technical
- **Multi-threaded World Generation**: 4 worker threads for chunk generation, 1 render thread
- **Memory Management**: Aggressive garbage collection and resource pooling
- **Graceful Crash Handling**: Dedicated crash screen when any thread fails
- **Texture Pre-loading**: Force-load critical textures for frame 1 availability
- **NBT Serialization**: Support for reading/writing structured block data

---

## Development Priorities (In Order)

### Priority 1: World Persistence
**Goal**: Save and load world state to disk, enabling persistent player progression

#### World Serialization (v0.14.0 - v0.16.0)
- Implement chunk data serialization format (likely region-based .mca files similar to Minecraft)
- Write chunk data to disk when chunks are unloaded from memory
- Store block data including state properties and custom data
- Implement chunk loading from disk for previously visited areas
- Add world metadata (seed, creation time, player position, game mode)
- Implement proper resource location caching to prevent memory leaks during save/load

#### Player Persistence (v0.17.0 - v0.18.0)
- Save player position, velocity, and rotation angle
- Persist player inventory state across sessions
- Track player spawn point and respawn mechanics
- Implement player statistics tracking (blocks placed/broken, distance traveled)

#### World Management (v0.19.0)
- Implement world list/selection screen to manage multiple worlds
- Add world creation dialog (world name, seed selection, game mode)
- Implement world deletion with confirmation
- Add world statistics display (creation date, playtime, size)

#### Data Migration & Backup (v0.20.0)
- Automated world backups on session start
- World repair/validation on load
- Migration system for updating save format in future versions

---

### Priority 2: User Interface Improvements
**Goal**: Create intuitive menus and settings systems for better UX

#### Main Menu (v0.21.0 - v0.22.0)
- Implement main menu screen with world selection
- Add "New World" button with world creation dialog
- Implement "Load World" with list of available saves
- Add "Settings" button for configuration options
- Implement "Exit Game" option
- Add version display and splash text randomization

#### Settings Menu (v0.23.0 - v0.24.0)
- Implement settings screen with categories (Video, Audio, Gameplay, Controls)
- **Video Settings**: Render distance slider, brightness control, particle effects toggle
- **Audio Settings**: Master volume, sound effects volume, music volume (prepare for music system)
- **Gameplay Settings**: Difficulty selection, damage toggle, creative mode option
- **Control Settings**: Key rebinding interface for WASD movement, block breaking, GUI navigation
- Implement settings persistence to config file
- Real-time preview of video settings changes

#### Pause Menu (v0.25.0)
- Pause game when ESC is pressed
- Implement pause menu with options: Resume, Settings, Return to Main Menu
- Continue world generation on background threads while paused
- Display current world name and playtime in pause menu

#### HUD Improvements (v0.26.0)
- Implement crosshair rendering for better targeting feedback
- Add block tooltip showing block name and properties when hovering
- Implement damage vignette effect when player takes damage
- Add item count indicator on hotbar items
- Display current game time and day/night cycle indicator
- Add compass or world position widget

---

### Priority 3: GUI Systems & Block Entities
**Goal**: Create interactive blocks and container systems for gameplay depth

#### Block Entities Foundation (v0.27.0 - v0.28.0)
- Implement block entity base system (separate from block data)
- Store additional data for blocks that need it (inventories, rotation, metadata)
- Implement block entity rendering pipeline (for multi-part blocks)
- Add proper serialization of block entity data to save files
- Create block entity registry system

#### Chest System (v0.29.0 - v0.30.0)
- Implement chest block with 27-slot inventory (3x3x3)
- Create chest GUI for opening/closing and item management
- Implement double chests that combine inventories
- Add chest opening animation (lid opening)
- Implement item dragging in GUI (single items, stacks, split stacking)
- Add visual feedback for inventory interactions

#### Furnace System (v0.31.0 - v0.32.0)
- Implement furnace block entity with input/output/fuel slots
- Create furnace smelting recipes (ore → ingot, logs → charcoal)
- Implement furnace fuel system (wood burns for specific duration)
- Create furnace GUI showing smelting progress
- Add fire texture inside furnace while burning
- Implement cooking time calculations with different fuel types

#### Workbench & Crafting (v0.33.0 - v0.34.0)
- Implement crafting table block entity
- Create crafting recipe system (shaped and shapeless recipes)
- Implement crafting GUI with recipe preview
- Add recipe discovery/unlock system as player crafts
- Support for complex recipes (tools, armor basics)
- Recipe validation and error handling for invalid combinations

#### Other Block Entities (v0.35.0)
- Implement sign block with text editing GUI
- Add hopper for item sorting and transportation
- Implement repeater for redstone-like signal delay
- Create trap door and other multi-state interactive blocks

---

### Priority 4: Real Entities & Mobs
**Goal**: Populate world with interactive creatures for combat and progression

#### Entity System Enhancement (v0.36.0)
- Extend entity base class with AI framework
- Implement entity behavior state machine (idle, walking, attacking, dying)
- Add entity animation system for movement and actions
- Implement entity aging (despawn after certain time if far from player)
- Create entity registry for mob definitions
- Add entity damage and health systems

#### Mob AI & Pathfinding (v0.37.0 - v0.38.0)
- Implement basic pathfinding (A* or simplified navigation)
- Create threat detection system (player detection radius)
- Implement simple melee attack behavior
- Add mob knockback physics
- Create mob spawning rules (spawn near player in dark areas)
- Implement mob despawn mechanics (too far from player)

#### Zombie Mob (v0.39.0)
- Implement zombie entity with basic green appearance
- Add zombie texture with damaged/rotting skin
- Implement zombie AI (wanders, chases player when in range, attacks)
- Add zombie death drops (some items on death)
- Implement zombie spawning during night time only

#### Creeper Mob (v0.40.0)
- Implement creeper entity with distinct sprite
- Add creeper AI (chases player, explodes on reach)
- Implement explosion physics (block destruction, damage radius)
- Add creeper spawning during night time
- Implement explosion light effect

#### Skeleton Mob (v0.41.0)
- Implement skeleton entity with bow
- Add skeleton AI (maintains distance, shoots arrows)
- Implement arrow projectiles with physics
- Add skeleton spawning in caves and at night
- Implement arrow collection after landing

#### Additional Mobs (v0.42.0 - v0.43.0)
- Implement passive mobs (sheep, cows for food sources)
- Add mob breeding mechanics
- Implement animal product harvesting (wool, leather, food)

---

### Priority 5: Day/Night Cycles & Spawn Systems
**Goal**: Create dynamic world with time progression and environmental effects

#### Day/Night System (v0.44.0 - v0.45.0)
- Implement game time ticker (in-game days tracked separately from real time)
- Create sun/moon position calculation based on time
- Implement sky color gradient based on day/night
- Add ambient lighting changes (darker at night, lighter during day)
- Implement sunrise/sunset transitions
- Display current time in HUD

#### Mob Spawning System (v0.46.0 - v0.47.0)
- Implement day spawn rules (passive mobs only during day)
- Implement night spawn rules (hostile mobs during night)
- Create spawn rate mechanics (more mobs as night progresses)
- Add spawn prevention near player spawn point
- Implement mob cap system (max mobs in loaded world)
- Create spawn chunk loading (chunks around player always loaded)

#### Environmental Effects (v0.48.0)
- Implement rain/thunderstorm weather system
- Add weather transitions with duration
- Create water level changes during rain
- Add thunder effects and sounds (prepare for audio system)
- Implement weather-based mob behavior changes

#### World Lighting Improvements (v0.49.0)
- Implement basic light propagation (light sources emit light)
- Create dark area detection for mob spawning
- Add torch/light source rendering
- Implement shadow casting based on light position

---

### Priority 6: Structure Generation
**Goal**: Create naturally occurring structures for exploration and resource gathering

#### Structure System Foundation (v0.50.0)
- Implement structure registry and placement system
- Create structure template system for storing block layouts
- Implement structure rotation and mirroring
- Add structure generation rules (where/when to place)
- Create structure-aware world generation (avoid overlaps)

#### Village Generation (v0.51.0 - v0.52.0)
- Implement basic village structure with houses
- Add village roads connecting buildings
- Create villager entity for trading
- Implement villager profession system
- Add village detection for player interaction

#### Dungeon Generation (v0.53.0 - v0.54.0)
- Implement dungeon structure with rooms and corridors
- Add dungeon spawner blocks for mob generation
- Create treasure chests with loot
- Implement dungeon difficulty scaling by depth
- Add environment hazards (lava, traps)

#### Temple & Structures (v0.55.0 - v0.56.0)
- Implement desert temple with traps and treasure
- Add jungle temple structure
- Create ocean structures (shipwrecks, underwater ruins)
- Implement loot tables for structure treasures

#### Biome-Specific Structures (v0.57.0)
- Add mountain peaks with specific structures
- Implement forest clearings with unique features
- Create swamp structures
- Add cave systems with landmark formations

---

## Features Deferred to Beta & Beyond

### Items & Crafting Depth (Beta Phase)
- Implement full tool progression (wood → stone → iron → diamond → netherite)
- Create armor system with protection tiers
- Add potion brewing and effects
- Implement enchantment system
- Create advancement/achievement system

### Combat & Status Effects (Beta Phase)
- Implement damage types (physical, fire, magic)
- Add player status effects (poison, weakness, strength)
- Create critical hit system
- Implement knockback mechanics
- Add particle effects for damage/effects

### Advanced Terrain (Beta Phase)
- Implement biome system with varied generation parameters
- Add ocean biomes with unique blocks
- Create desert biomes with sand dunes
- Implement mountain biome generation
- Add swamp biomes with unique flora

### Netherworld & Dimensions (Beta Phase)
- Implement nether dimension with unique terrain
- Create nether portal block mechanics
- Add nether-specific mobs and structures
- Implement end dimension and end game content

### Sound & Music (Pre-Release Phase)
- Implement audio system (currently rendering-only)
- Add block breaking/placing sound effects
- Create ambient cave sounds
- Implement background music system
- Add mob sound effects

### Performance Optimization (Throughout)
- Implement view frustum culling for rendering
- Add chunk streaming improvements
- Optimize mesh generation algorithms
- Implement LOD (Level of Detail) for distant chunks
- Add memory pooling for frequently allocated objects

---

## Milestones & Version Targets

### Alpha Phase (v0.14.0 - v0.38.0)
- **v0.14.0 - v0.20.0**: World persistence and management
- **v0.21.0 - v0.26.0**: User interface and menus
- **v0.27.0 - v0.35.0**: Block entities and GUIs
- **v0.36.0 - v0.43.0**: Mobs and entities
- **v0.44.0 - v0.49.0**: Day/night cycles and spawning
- **v0.50.0 - v0.57.0**: Structure generation

### Beta Phase (v0.58.0 - v0.72.0)
- Expand world generation variety
- Implement multiple biomes
- Add advanced crafting chains
- Create combat depth and abilities
- Implement complete enchantment system

### Pre-Release Phase (v0.73.0 - v0.77.0)
- Bug fixes and stability improvements
- Performance optimization
- Balance adjustments to mob spawning and difficulty
- Sound and music implementation
- UI polish and accessibility improvements

### Release Candidate Phase (v0.78.0 - v0.82.0)
- Final testing and bug squashing
- Community feedback integration
- Last-minute balance changes
- Optimization for target platforms

### Release (v1.0.0)
- Full feature-complete 2D Minecraft experience
- Stable world generation and saving
- Complete mob ecosystem
- Full crafting and progression system

---

## Technical Debt & Ongoing

### Throughout All Phases
- **Code Organization**: Continue separating concerns (rendering, logic, data)
- **Documentation**: Keep code commented and maintain wiki
- **Testing**: Implement unit tests for critical systems
- **Asset Management**: Organize textures and resources properly
- **Performance**: Profile and optimize bottlenecks as they appear
- **Refactoring**: Clean up legacy code from early development

### Known Issues to Address
- Chunk mesh generation may need optimization for complex terrain
- Memory management during rapid loading/unloading
- Floating-point precision loss at extreme distances
- Physics system scalability for many entities

---

## Design Principles

1. **2D Perspective**: All gameplay remains top-down 2D, no perspective shifts
2. **Minecraft Inspiration**: Follow Minecraft design philosophy while adapting to 2D
3. **Performance First**: Prioritize performance optimization alongside features
4. **Modular Systems**: Keep systems independent and testable
5. **Player Progression**: Ensure clear progression path and goals
6. **Accessibility**: Make game playable and enjoyable for various skill levels

---

## Notes

- **Save Format**: Consider implementing world backup/migration system early to prevent breaking changes
- **Modding**: Future considerations for modding API post-v1.0
- **Multiplayer**: Deferred to post-release expansion
- **Marketplace**: Consider cosmetic/convenience shop post-release
- **Cross-Platform**: Ensure code remains platform-agnostic (currently Java/LWJGL3)

This roadmap is subject to change based on development progress and design discoveries. Regular playtesting and community feedback should inform refinements to this plan.
