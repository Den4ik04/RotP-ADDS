scoreboard players set @s destiny_timer 0
playsound entity.lightning_bolt.thunder ambient @a ~ ~ ~ 10 1
particle minecraft:soul_fire_flame ~ ~1 ~ 1 5 1 0.1 100
summon item ~ ~2 ~ {Item:{id:"jojo:stand_arrow",Count:1b,tag:{display:{Name:'{"text":"Meteorite Arrow","color":"gold"}'}}}}
title @s title {"text":"Судьба настигла тебя!","color":"dark_red","bold":true}
