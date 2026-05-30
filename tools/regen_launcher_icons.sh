#!/usr/bin/env bash
# Regenerate launcher icons from a single source image.
# Usage: ./tools/regen_launcher_icons.sh <source_image>
set -eu

SRC="${1:-031205b2e4eccaee6ee9230747441677.jpg}"
RES_DIR="app/src/main/res"

if [ ! -f "$SRC" ]; then
  echo "Source not found: $SRC" >&2
  exit 1
fi

# density:size pairs (one per line so we don't need bash 4 associative arrays)
PAIRS="mdpi:48
hdpi:72
xhdpi:96
xxhdpi:144
xxxhdpi:192"

while IFS=: read -r density size; do
  [ -z "$density" ] && continue
  dir="$RES_DIR/mipmap-$density"
  mkdir -p "$dir"

  # Square legacy icon
  magick "$SRC" -resize "${size}x${size}^" -gravity center -extent "${size}x${size}" \
    -define png:format=png32 "$dir/ic_launcher.png"

  # Round legacy icon: crop the same square to a circle (transparent outside).
  half=$((size / 2))
  magick "$SRC" -resize "${size}x${size}^" -gravity center -extent "${size}x${size}" \
    \( +clone -threshold -1 -negate -fill white -draw "circle ${half},${half} ${half},0" \) \
    -alpha off -compose CopyOpacity -composite \
    -define png:format=png32 "$dir/ic_launcher_round.png"

  echo "  wrote $dir/ic_launcher.png + ic_launcher_round.png (${size}x${size})"
done <<EOF_PAIRS
$PAIRS
EOF_PAIRS

# Adaptive-icon foreground: image centered in the 66dp safe zone (264 px) of a 432 px canvas.
FG="$RES_DIR/drawable/ic_launcher_foreground.png"
mkdir -p "$(dirname "$FG")"
magick "$SRC" -resize 264x264^ -gravity center -extent 264x264 \
  -background none -gravity center -extent 432x432 \
  -define png:format=png32 "$FG"
echo "  wrote $FG (432x432, transparent safe zone)"

# About-screen avatar: same image filled into a 432x432 square (no safe zone),
# so the AboutScreen circular clip does not leave transparent borders.
ABOUT="$RES_DIR/drawable-nodpi/about_avatar.png"
mkdir -p "$(dirname "$ABOUT")"
magick "$SRC" -resize 432x432^ -gravity center -extent 432x432 \
  -define png:format=png32 "$ABOUT"
echo "  wrote $ABOUT (432x432, full bleed)"
