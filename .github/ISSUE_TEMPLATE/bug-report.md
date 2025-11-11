---
name: Bug report
about: A general issue with the mod / compatibility with other mods
title: ''
labels: bug
assignees: ''

---

**Describe the bug**
Is is a crash?
How can you reproduce it?
What other mods is this issue happening with?
Does this happen when only this mod is installed?
If not, please, try to find out what mod is also causing the issue with my mod.

**Other information**
Mod version: 
Minecraft version: 

**Screenshots/Videos**
If you think that it would help if I saw a screenshot or a video, you can attach it here.

**Log file**
Generally i don't need a log, but if it is a crash, please do provide the log file. (from .minecraft/logs/latest.log)

**Tips for reporter**
A binary search can be used to quickly find a specific mod causing trouble, which can be especially useful when logs don't give a conclusive answer to your issue.

Start by removing or disabling half of your mods, then test if the problem still occurs. If it does, remove half of the remaining mods and test again. If it doesn't, add back half of the mods you just removed.

Keep in mind you don't have to stick strictly to halves each time, and may have to enable some library mods like Fabric API out of order.

By repeating this on an increasingly smaller set of mods, you'll find the problematic mod within a few iterations.
