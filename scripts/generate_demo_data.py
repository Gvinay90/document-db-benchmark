#!/usr/bin/env python3
"""
Generate JSON arrays compatible with POST /insertData and /insertDataParallel.

Each element matches the Demo document: {"id": int, "name": str, "value": float}

Uses only the Python standard library. Streams output so large files stay memory-safe.
"""

from __future__ import annotations

import argparse
import json
import random
import sys
from pathlib import Path


def parse_args() -> argparse.Namespace:
    p = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    p.add_argument(
        "-n",
        "--count",
        type=int,
        default=10_000,
        help="Number of documents (default: 10000)",
    )
    p.add_argument(
        "-o",
        "--output",
        type=Path,
        default=Path("data/benchmark/demo.json"),
        help="Output file (default: data/benchmark/demo.json)",
    )
    p.add_argument(
        "--id-start",
        type=int,
        default=1,
        help="First id value (default: 1)",
    )
    p.add_argument(
        "--seed",
        type=int,
        default=None,
        help="Random seed for reproducible name/value fields",
    )
    p.add_argument(
        "--name-prefix",
        default="item",
        help="Name field prefix; becomes item-0, item-1, ... by default pattern",
    )
    p.add_argument(
        "--pretty",
        action="store_true",
        help="Pretty-print JSON (larger files; default is compact)",
    )
    return p.parse_args()


def main() -> int:
    args = parse_args()
    if args.count < 1:
        print("error: --count must be at least 1", file=sys.stderr)
        return 1

    rng = random.Random(args.seed)

    args.output.parent.mkdir(parents=True, exist_ok=True)

    sep = (", ", ": ") if args.pretty else (",", ":")

    with args.output.open("w", encoding="utf-8") as f:
        f.write("[")
        for i in range(args.count):
            if i == 0:
                if args.pretty:
                    f.write("\n ")
            else:
                f.write("," + ("\n " if args.pretty else ""))
            doc_id = args.id_start + i
            rec = {
                "id": doc_id,
                "name": f"{args.name_prefix}-{doc_id}",
                "value": round(rng.uniform(-1_000_000, 1_000_000), 6),
            }
            json.dump(rec, f, separators=sep, ensure_ascii=False)
        if args.pretty:
            f.write("\n")
        f.write("]\n")

    print(f"Wrote {args.count} documents to {args.output.resolve()}", file=sys.stderr)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
