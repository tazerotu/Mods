# Trait system: shell and implementation guide

The shell lives in `src/main/java/com/skillsmod/traits/`. It implements the design from the trait reference (tiers, stat curve, perks, abilities, cooldowns, two ability keys) as empty hooks you fill in one trait at a time. The Minecraft side of it compiles against your 26.3 jar. The Fabric API calls mentioned in the advanced part were not checked, so confirm their names in the Fabric docs.

## What is in the shell

| File | What it is | State |
|---|---|---|
| `UpgradeTier` | Which tier a level is (big, medium, small, very small) | working |
| `StatCurve` | Main stat from the cap: 0.5% of the cap per very small level, 6% per small level, exactly the cap at level 100 | working |
| `Perk` | One named upgrade: level, id, name, description | working |
| `Ability` | Every perk with a cooldown: key (auto, combat, movement), duration, cooldown | working (numbers from the document) |
| `Cooldowns` | When each ability is ready again, per player | working, memory only |
| `Trait` | Base class of every trait, with empty hooks | shell |
| `SturdyPicks` | Example trait: the full perk list is filled in, the hooks are TODOs | example |
| `TraitRegistry` | Holds all traits and passes events to them. `SkillsMod` already calls `init()` | shell |
| `AbilityManager` | Checks cooldown, applies the effect, starts the cooldown | shell |

## Basic: adding a trait

1. **Copy `SturdyPicks.java`** and rename it. Fill in the constructor: id, skill, the stat cap from the document, the unlock level (1, or 20 for Vein Miner, Tree Feller, Prospector, Double Harvest and Growth Aura), and the ten perks.
2. **Register it** in `TraitRegistry.init()` (the commented lines show where).
3. **Override only the hooks it needs.** Every hook receives the player's level in the trait's skill, so you never look it up yourself.
   - `onTick`: attribute traits (Brute Force, Iron Skin, Light Feet, Fall Training, Mining Haste, Treasure Hunter, Sea Legs) and passive effects (Growth Aura).
   - `onBlockBroken`: Sturdy Picks/Axes, Prospector, Double Harvest, Vein Miner, Tree Feller.
   - `onKill`: Bloodlust, Rampage.
   - `onAbilityKey`: only for key abilities.
4. **Read stat and perks from the helpers.** `stat(level)` gives the main stat (0 while locked), and `hasPerk(level, "perk_id")` says whether a perk is unlocked. Never hardcode a level in an effect.
5. **Test the pure logic first.** Tier counts (80 / 10 / 5 / 5) and `StatCurve.at(100) == cap` are already correct, so a bug is in your effect, not the numbers.

### Moving from the current code

`Traits.java` and `Scaling.java` still work. Migrate one trait at a time: build its class, move the effect code from `Traits.java` into the hook, then change `SkillEvents` to call `TraitRegistry.blockBroken(...)`, `kill(...)` and `tick(...)` instead of calling `Traits` directly. Do this when the first two or three traits are converted, not before. Delete `Scaling` and `Traits` last.

### Three patterns that cover almost everything

- **Attribute trait.** In `onTick`, set a transient modifier on the right attribute with an id of your own, only when the amount changed (the current `Traits.set(...)` already does this). Light Feet, Fall Training, Iron Skin, Brute Force and Sea Legs are all this.
- **Event-roll trait.** In a hook, roll `stat(level)` and apply a result. Sturdy Picks, Prospector and Double Harvest are this.
- **Ability.** Hook into the trigger, call `AbilityManager.tryStart(player, Ability.X)`, and let the manager handle the cooldown.

## Advanced

### Ability keys (two keys)

Keys exist on the client, but abilities must run on the server, so a key press needs a message to the server.

1. **Client:** register two key mappings, one for the combat key (Warlord) and one for the movement key (Sprint Burst, Wind Runner, Marathoner). Check them in a client tick, as your menu key already does.
2. **Packet:** a small client-to-server payload carrying which key was pressed (`Ability.Key` as a byte). Register its type and codec on the common side, send it from the client tick, and receive it on the server.
3. **Server:** in the receiver, call `AbilityManager.onKeyPressed(player, key)` on the server thread. The movement key will start every unlocked, ready movement ability, as designed.
4. **Never trust the client.** The server checks the unlock and the cooldown, which `tryStart` already does.

The payload and registration API changes between Minecraft versions, so follow the current Fabric networking docs for the exact names.

### Timed effects

Abilities with a duration (Sprint Burst, Marathoner, Stoneskin) need something that ends them. Two ways:
- **Status effect or attribute modifier** with a duration. Simplest: apply it and let the game remove it.
- **Your own timer.** Store an "active until" tick next to the cooldown and check it in `onTick`. Needed for effects the game has no status effect for (Marathoner's "no hunger from sprinting").

### Saving and syncing

`SkillData` and `Cooldowns` are in memory. Move both to Fabric's data attachment API on the player (a codec-backed object that persists with the player and can sync to the owning client). Do this before building many perks, because every perk state (perk uses left, discovered biomes) belongs there too.

### Per-tool state (Last Gasp, 3 uses per tool)

State that belongs to one item cannot live on the player. Store the remaining uses in a custom data component on the item stack, or in the item's custom data tag. Read it when the tool would break, subtract one, and stop granting the ability at zero.

### Hooks that need more than an event

| Perk | How |
|---|---|
| Last Stand (survive a lethal hit) | A Fabric event that runs before death and can cancel it: cancel, set health to half a heart, start the cooldown |
| Iron Skin perks, Stoneskin, Battle Cry | An event that runs when a living entity takes damage; reduce or react there. Protection-II equivalents can also be applied as damage reduction |
| Pearl Landing | Same damage event, checking for the ender pearl damage type |
| Mining Haste cap and derivative wood share | The mining-efficiency attribute works for the +56 and +26 flat bonuses. The "derivatives never go above 98% of instant" rule needs the per-block speed calculation, which needs a mixin on the player's break-speed method |
| Anvil, Mending and crafting-grid repair perks | Mixins (or the item-repair events if your Fabric version has them). These are the hardest perks, so do them last |
| Field Repair | A use-item event that checks sneak, the off-hand repair material and the cooldown |

### Order that keeps it easy

1. Attribute traits (Brute Force, Iron Skin, Light Feet, Fall Training, Treasure Hunter, Sea Legs, Mining Haste, Woodcutting Haste): `onTick` only.
2. Event-roll traits (Sturdy Picks/Axes, Prospector, Double Harvest) and their easy perks.
3. Vein Miner, Tree Feller, Growth Aura (you already have working versions in `Traits.java` and `VeinMiner.java`).
4. Abilities with cooldowns, starting with the automatic ones, then the key packet and the two keys.
5. Saving and syncing.
6. Mixin-based perks last.
