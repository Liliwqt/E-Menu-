#!/usr/bin/env python3
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
EXTS = {'.kt', '.java', '.kts', '.gradle.kts'}
SKIP_DIRS = {'/build/', '/.gradle/', '/buildSrc/', '/gradle/'}

kdoc_re = re.compile(r"(?P<indent>^[ \t]*)/\*\*(?P<body>.*?)(?P<end>\*/)", re.DOTALL | re.MULTILINE)

def clean_block(body: str) -> str:
    lines = body.splitlines()
    # Strip leading '*' and whitespace
    cleaned = []
    for ln in lines:
        ln = ln.lstrip(' \t')
        if ln.startswith('*'):
            ln = ln[1:]
        ln = ln.strip()
        if ln:
            cleaned.append(ln)
    if not cleaned:
        return ''
    # Take first non-empty line as title
    title = cleaned[0]
    # Remove leading/trailing asterisks or slashes accidentally included
    title = title.strip('*/ ')
    return ' ' + title + ' '


def process_text(text: str) -> (str, bool):
    changed = False
    def repl(m):
        nonlocal changed
        indent = m.group('indent')
        body = m.group('body')
        end = m.group('end')
        title = clean_block(body)
        if title == ' ':
            # empty -> keep original
            return m.group(0)
        new = f"{indent}/**{title}*/"
        if new != m.group(0):
            changed = True
        return new
    out = kdoc_re.sub(repl, text)
    return out, changed


def main(dry=False):
    files_changed = []
    for p in ROOT.rglob('*'):
        sp = str(p)
        if any(sd in sp for sd in SKIP_DIRS):
            continue
        if p.is_file() and (p.suffix in EXTS or sp.endswith('.gradle.kts')):
            try:
                text = p.read_text(encoding='utf-8')
            except Exception:
                continue
            newtext, changed = process_text(text)
            if changed:
                files_changed.append(p)
                if not dry:
                    p.write_text(newtext, encoding='utf-8')
    for f in files_changed:
        print(f"MODIFIED: {f}")
    print(f"Total modified: {len(files_changed)}")

if __name__ == '__main__':
    dry = '--apply' not in sys.argv
    if dry:
        print('Running dry-run (no files will be changed). Use --apply to modify files.')
    main(dry=not dry)
