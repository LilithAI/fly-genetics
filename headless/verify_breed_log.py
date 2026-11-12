#!/usr/bin/env python3
"""Checks the logs of the in-game breeding test: verify_breed_log.py breed.log [reload.log]
 - every child gene equals its mother's or father's value, except at most `mutations` genes
 - generation = max(parents) + 1, parents named
 - (with reload.log) every fly has the same genome after the world is saved and reopened; a fly that
   hatched after the reload must carry the genome of an egg saved before it
"""
import re, sys
def parse(path):
    out = {}
    for line in open(path, errors='replace'):
        m = re.search(r'(fly|egg) ([0-9a-f]{8})(.*?) gen (\d+) mother (\w+) father (\w+) mutations (\d+).*?: (tau_m=.*)$', line)
        if m:
            genes = dict((k, float(v)) for k, v in (kv.split('=') for kv in m.group(8).split()))
            out[m.group(2)] = dict(kind=m.group(1), gen=int(m.group(4)), mother=m.group(5), father=m.group(6), mut=int(m.group(7)), genes=genes, line=m.group(3))
    return out
a = parse(sys.argv[1]); fails = 0
print('%d genomes in %s' % (len(a), sys.argv[1]))
kids = 0
for i, x in a.items():
    if x['gen'] == 0: continue
    kids += 1
    mo, fa = a.get(x['mother']), a.get(x['father'])
    if not mo or not fa: print('FAIL', i, 'parent not found'); fails += 1; continue
    fm = ff = both = other = 0
    for k, v in x['genes'].items():
        em, ef = abs(v - mo['genes'][k]) < 6e-4, abs(v - fa['genes'][k]) < 6e-4
        if em and ef: both += 1
        elif em: fm += 1
        elif ef: ff += 1
        else: other += 1
    ok = other <= x['mut'] and x['gen'] == max(mo['gen'], fa['gen']) + 1 and len(x['genes']) == 29
    print('%s %s %s gen %d: %d genes from mother, %d from father, %d same in both, %d changed (mutations recorded: %d)' % ('PASS' if ok else 'FAIL', x['kind'], i, x['gen'], fm, ff, both, other, x['mut']))
    fails += not ok
if kids == 0: print('FAIL no children found'); fails += 1
if len(sys.argv) > 2:
    b = parse(sys.argv[2])
    print('%d genomes after reload' % len(b))
    if not b: print('FAIL nothing after reload'); fails += 1
    for i, x in b.items():
        same = lambda y: y['genes'] == x['genes'] and y['gen'] == x['gen'] and y['mother'] == x['mother'] and y['father'] == x['father']
        if i in a:
            ok = same(a[i]); what = 'identical after save and reload'
        else:  # an egg that was saved, reloaded and has hatched since: the fly has a new id but must carry the egg's genome
            src = [j for j, y in a.items() if y['kind'] == 'egg' and same(y)]
            ok = len(src) == 1; what = 'hatched after reload from saved egg %s with the same genome' % (src[0] if ok else '?')
        print('%s %s %s %s' % ('PASS' if ok else 'FAIL', x['kind'], i, what)); fails += not ok
sys.exit(1 if fails else 0)
