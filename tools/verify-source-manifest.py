#!/usr/bin/env python3
"""Verify the archived source files without contacting production services."""
import hashlib
import json
from pathlib import Path
import sys
root = Path(__file__).resolve().parents[1]
manifest = json.loads((root / 'docs/source-manifest.json').read_text())
errors = []
for item in manifest['files']:
    path = root / item['path']
    if not path.is_file():
        errors.append(item['path'] + ': missing')
    elif hashlib.sha256(path.read_bytes()).hexdigest() != item['sha256']:
        errors.append(item['path'] + ': changed')
if errors:
    print('\n'.join(errors))
    sys.exit(1)
print(f"Verified {len(manifest['files'])} archived files")
