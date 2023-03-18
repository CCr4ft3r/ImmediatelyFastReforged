<div align="center">
  <h1>ImmediatelyFast Reforged (Unofficial)</h1>
  <a ><img src="https://img.shields.io/badge/Mod%20Loader-Forge-red"></a>
  <img src="https://img.shields.io/badge/Enviroment-Client-green">

  <br />
  <p>ImmediatelyFast Reforged is a Forge port of the Fabric mod ImmediatelyFast. It improves the immediate mode rendering performance of the client.
  <br />It is designed to be lightweight and compatible with other mods. This makes it an ideal choice for modpacks.</p>
</div>

## Important Notes and Requests
The original copryright owner is RaphiMC who created the Fabric version of ImmediatelyFast.
I requested the permission to port their great mod to Forge and got the permission to do so.

Please do not report issues with this port to the original owner or to their GitHub repo. Please report it to [mine](https://github.com/CCr4ft3r/ImmediatelyFastReforged/issues).<br>
Please do not join their discord or post a comment on their mod page in order to get support for my port. Please post your questions here.

This port currently does not change any of the original optimizations. On code side the only differences are the following ones:
<li>I adapted the class references and mixin injection points to match it to SRG and Forge</li>
<li>I removed one of the original embedded libraries by using the default mixin annotation classes</li>
<li>I removed some compatibility modes for two mods that does not exist for Forge. </li>

This port is not going to support Optifine - Sorry. But you can use Rubidium and Oculus instead.

## Optimizations

ImmediatelyFast Reforged generally optimizes all immediate mode rendering by using a custom buffer implementation which batches
draw calls and uploads data to the GPU in a more efficient way.  
The following parts of the immediate mode rendering code are optimized:

- Entities
- Block entities
- Particles
- Text rendering
- GUI/HUD
- Immediate mode rendering of other mods (ImmersivePortals benefits a lot from this)

It also features targeted optimizations where vanilla rendering code is being changed in order to run faster.  
The following parts of the rendering code are optimized with a more efficient implementation:

- Map rendering
- HUD rendering
- Text rendering

## Performance

Performance should be equals to the original Fabric mod.
[Take a look at the original page](https://www.curseforge.com/minecraft/mc-mods/immediatelyfast)

## Compatibility

ImmediatelyFast Reforged is structured to interfere with mods as little as possible.
It should work fine with most if not all mods and modpacks.

Known incompatibilities:

- Optifine

If you encounter any issues, please report them on the [Issue Tracker](https://github.com/CCr4ft3r/ImmediatelyFastReforged/issues).