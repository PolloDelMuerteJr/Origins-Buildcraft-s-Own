# Origin: BuildCraft&#39;s Own

This is an Origins Addon mod, current it add two Origins. The Giant! A realistic interpretation of a player scaled up to twice their normal size! And the Jumper, capable of teleporting themselves through the world. This mod was coded with AI assistance.

## Setup

This mod is a .jar place it in your mods folder like any other fabric mod. 
<br>This mod is dependent on the [Origins mod](https://modrinth.com/mod/origins) By Apace. The [Pehkui Mod](https://modrinth.com/mod/pehkui) by Virtuoel. And [Fabric API](https://modrinth.com/mod/fabric-api).
<br>The exact version of these dependencies will rely on the version of Origins: BuildCraft's Own you are using.

There is a config file in this mod, it can disable the Origins added by the mod. It will not remove the Origins from player who have already picked them, only prevent them from being picked in the Origins selection menu. You must restart the server for it to take effect.
<br>The config file is named "*origin-buildcrafts-own.json*" it lives at /origin-buildcrafts-own-template-1.20.2/run/config/

## License

This mod is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.

## Contact

This mod was made by PolloDelMuerteJr, with assistance from ChatGPT.
<br>Functional Builds of this mod can be downloaded at [Modrinth](https://modrinth.com/mod/origins-buildcrafts-own) or [CurseForge](https://www.curseforge.com/minecraft/mc-mods/origins-buildcrafts-own).
<br>The source code can be found on the [Github](https://github.com/PolloDelMuerteJr/Origins-Buildcraft-s-Own) project. This is also where issues, bugs, and suggestions can be submitted. This code may from an in development build of the mod, and may not be fully functional.

## Gameplay Information

Origin's BuildCraft's Own add two new Origins, the Giant and the Jumper. Both are intended to reshape the way the player interacts with Minecraft. As is the case with the default origins, you are probably better off just playing as a Human. However, that's less interesting, and if you are like me, the way Origins like these change the game is very enjoyable.

### Giant

The Giant is a very realistic take on a 'giant' player. They have no active ability or powers. Just changes and their consequences.

* The Giant is ~2x2x4 blocks in size

* The Giant has 40 max hp
* The Giant moves at 1.6 times the normal speed

* The Giant has a reduced attack speed
* The Giant deals 1.25 damage
* The Giant deals extra knockback, equivalent to one level of the knockback enchantment
* Giants take half as much knockback
* The Giant cannot use bows, crossbows, or tridents as ranged weapons

* Giants are 'resistant' to the levitation effect, only rising at 1/4 normal speed

* Giants can fall 6 blocks before taking fall damage, however they take `y = 2x + 2.5` fall damage

* Giants break blocks 1.5 times faster then normal

* Giants have an increased interaction range, ~7.5 blocks

* The Giant has a 0.25 chance to break a bed they wake up in

* The Giant will break 'weak' blocks like leaves and ice when stood on them. This process is slowed by the Feather Falling enchantment. This can be prevented by level 3 Feather Falling
* Some blocks will also break instantly, like vines and lily pads. This can also be prevented by level 3 Feather Falling
* The Giant will deal an increasing amount of damage to any entity they are mounted on every second

* The Giant has a very small, but constant food drain
* The Giant gains only half as much hunger/saturation from any eaten food

* Elytra take extra durability damage when in use by a Giant
* Giants fall out of the sky faster when using an elytra, and lose velocity faster
* Fireworks are much less effective in flight when used by a Giant
* When in flight Giants cannot rotate/turn instantly, they have a forced turning circle

* Giants will force open any non-iron trapdoors they land on when falling from a hight greater then 2.4 blocks

### Jumper

The Jumper Origin is inspired by the *Jumper* series by author Steven Gould.
<br>Although the Origin functions quite differently. This Jumper can teleport to any location pointed to on a compass. Or then can chose to charge a teleport directly forward.

* The Jumper is still heavily under development, and I will fill this out properly once I have finished it. Sorry, if you really need to know read the source code.