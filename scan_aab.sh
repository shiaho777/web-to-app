#!/bin/bash
PROTO_PATH="/Users/shiaho/Downloads/web-to-app-main 1/app/src/main/proto"
PROTO_FILE="/Users/shiaho/Downloads/web-to-app-main 1/app/src/main/proto/aapt/Resources.proto"

count=0
for f in /tmp/aab_scan/base/res/*.xml; do
  decoded=$(protoc --decode=aapt.pb.XmlNode --proto_path="$PROTO_PATH" "$PROTO_FILE" < "$f" 2>/dev/null)
  bad=$(echo "$decoded" | awk '
    /^[[:space:]]*attribute \{$/{ inAttr=1; depth=1; hasValue=0; hasCompiled=0; saved=""; next }
    inAttr {
      saved = saved "\n" $0
      if (/\{$/) depth++
      if (/^[[:space:]]*\}$/) {
        depth--
        if (depth == 0) {
          if (!hasValue && !hasCompiled) print "  bad attr: " saved
          inAttr = 0
        }
      }
      if (depth == 1) {
        if (/^[[:space:]]*value:/) hasValue = 1
        if (/^[[:space:]]*compiled_item \{/) hasCompiled = 1
      }
    }
  ')
  if [ -n "$bad" ]; then
    count=$((count + 1))
    echo "=== $f ==="
    echo "$bad" | head -10
    if [ $count -ge 5 ]; then break; fi
  fi
done
echo "Stopped after $count file(s)"
