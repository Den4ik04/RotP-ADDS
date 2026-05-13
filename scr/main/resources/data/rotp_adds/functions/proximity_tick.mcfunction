# Логика срабатывает, если дистанция между игроками со стендами от 15 до 100 блоков
execute as @a[tag=stand_user] at @s run execute as @a[tag=stand_user,distance=15..100] run tellraw @s {"text":"Вражеский стендюзер поблизости!","color":"red","bold":true}
execute as @a[tag=stand_user] at @s run execute as @a[tag=stand_user,distance=15..100] run playsound entity.wither.spawn ambient @s ~ ~ ~ 0.5 1
# Частицы отключаются, если подойти ближе 15 блоков
execute as @a[tag=stand_user] at @s run execute as @a[tag=stand_user,distance=15..100] run particle minecraft:dust 0.5 0 1 1 ~ ~1 ~ 0.5 1 0.5 0.1 10
