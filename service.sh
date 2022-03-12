#!/system/bin/sh
MODDIR=${0%/*}

sprop=/data/adb/modules/nexus/system.prop
nex_log=/storage/emulated/0/Nexus_Tweaks.log

# Universal GMS Doze Module Path
mmodule=/data/adb/modules/universal-gms-doze

# Detect whether Unlocked into System
while $(dumpsys window policy | grep mIsShowing | awk -F= '{print $2}')
do
sleep 1
done

# sleep extra 10s.
sleep 10

echo "- Hello, there! I'm NeX" > $nex_log

echo "" >> $nex_log

echo "- I am here to optimize your $(getprop ro.build.product)" >> $nex_log

echo "" >> $nex_log

# excute only once after flashing module
boot=$(getprop nex.boot)

if [ "$boot" == "0" ]; then

START=$(date +"%s")

echo "- Starting Art Optimization" >> $nex_log

echo "" >> $nex_log

# ART optimizer
su -c cmd package bg-dexopt-job

END=$(date +"%s")

DIFF=$(($END - $START))

echo "- Art Optimization took $(($DIFF / 60)) minute(s) and $(($DIFF % 60)) second(s)." >> $nex_log

echo "" >> $nex_log

echo nex.boot=1 >> $sprop

fi

if ! [ -d "$mmodule" ]; then

START1=$(date +"%s")

echo "- Starting DUS Optimization" >> $nex_log

echo "" >> $nex_log

# DUS optimizer
dus

END1=$(date +"%s")

DIFF1=$(($END1 - $START1))

echo "- DUS Optimization took $(($DIFF1 / 60)) minute(s) and $(($DIFF1 % 60)) second(s)." >> $nex_log

echo "" >> $nex_log

fi

su -lp 2000 -c "cmd notification post -S bigtext -t Nexus-Tweaks tag Activated"

# Where It All Begins
nice -n -9 nAi
common > /dev/null

exit 0
