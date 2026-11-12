#!/usr/bin/env python3
"""Step 1 analysis. Usage: analyze.py seeds.csv single.csv [random.csv ...]
Verdict rules (ours, not the mod's):
  SILENT  rest or food or threat firing below half of baseline, or threat leg-motor response below 30% of baseline
  SEIZING any phase above 3x its baseline rate, or activity 1.5 s after the threat still above 2x resting baseline,
          or leg motor neurons above the mod's 0.05 floor with no input at all
  ok      otherwise
"""
import csv, sys, statistics as st, collections

def rows(p): return list(csv.DictReader(open(p)))
seeds = rows(sys.argv[1])
F = lambda r, k: float(r[k])
base = {k: st.mean(F(r, k) for r in seeds) for k in seeds[0] if k not in ('label',)}
sd = {k: st.pstdev([F(r, k) for r in seeds]) for k in base}
KEYS = ['rest_hz', 'food_hz', 'threat_hz', 'recover_hz', 'court_hz', 'rest_dn', 'threat_mn_leg_front', 'threat_dn_giant_fiber',
        'food_mn_proboscis', 'court_dn_courtship', 'rest_mbon_avoid', 'punish_memApproach', 'reward_memAvoid']

def verdict(r):
    v = []
    if F(r, 'rest_hz') < .5 * base['rest_hz'] or F(r, 'food_hz') < .5 * base['food_hz'] or F(r, 'threat_hz') < .5 * base['threat_hz'] \
       or F(r, 'threat_mn_leg_front') < .3 * base['threat_mn_leg_front']: v.append('SILENT')
    if any(F(r, p + '_hz') > 3 * base[p + '_hz'] for p in ('rest', 'food', 'threat', 'court')) or F(r, 'recover_hz') > 2 * base['rest_hz'] \
       or F(r, 'rest_mn_leg_front') > .05: v.append('SEIZING')
    return '+'.join(v) or 'ok'

print('## Baseline (mean of %d seeds, sd in brackets)' % len(seeds))
for k in KEYS: print('- %s: %.4f (%.4f)' % (k, base[k], sd[k]))

if len(sys.argv) > 2:
    single = rows(sys.argv[2])
    groups = collections.OrderedDict()
    for r in single:
        name, m = r['label'].rsplit(' x', 1)
        groups.setdefault(name, []).append((float(m), r))
    print('\n## One at a time (values are ratios to baseline, except mem columns which are the raw mean weight; 1.0 = nothing learned)')
    for name, lst in groups.items():
        print('\n### ' + name)
        print('| x | rest | food | threat | recover | court | legs(threat) | proboscis(food) | memApproach after punish | memAvoid after reward | verdict |')
        print('|---|---|---|---|---|---|---|---|---|---|---|')
        for m, r in sorted(lst + [(1.0, None)], key=lambda t: t[0]):
            if r is None:
                print('| **1** | 1 | 1 | 1 | %.2f | 1 | 1 | 1 | %.3f | %.3f | baseline |' % (base['recover_hz'] / base['rest_hz'], base['punish_memApproach'], base['reward_memAvoid'])); continue
            q = lambda k: F(r, k) / base[k] if base[k] else float('nan')
            print('| %g | %.2f | %.2f | %.2f | %.2f | %.2f | %.2f | %.2f | %.3f | %.3f | %s |' % (m, q('rest_hz'), q('food_hz'), q('threat_hz'),
                  F(r, 'recover_hz') / base['rest_hz'], q('court_hz'), q('threat_mn_leg_front'), q('food_mn_proboscis'),
                  F(r, 'punish_memApproach'), F(r, 'reward_memAvoid'), verdict(r)))
    print('\n## Widest multiplier range tested with verdict ok on both sides of 1')
    for name, lst in groups.items():
        lo = hi = 1.0
        for m, r in sorted([t for t in lst if t[0] < 1], key=lambda t: -t[0]):
            if verdict(r) != 'ok': break
            lo = m
        for m, r in sorted([t for t in lst if t[0] > 1], key=lambda t: t[0]):
            if verdict(r) != 'ok': break
            hi = m
        print('- %s: %g to %g' % (name, lo, hi))

for p in sys.argv[3:]:
    rs = rows(p)
    vs = collections.Counter(verdict(r) for r in rs)
    print('\n## Random brains: %s (%d brains)' % (p, len(rs)))
    for k, n in vs.most_common(): print('- %s: %d' % (k, n))
    for k in ['rest_hz', 'food_hz', 'threat_hz', 'recover_hz', 'court_hz', 'threat_mn_leg_front', 'punish_memApproach']:
        x = sorted(F(r, k) / base[k] for r in rs)
        print('- %s ratio to baseline: min %.2f, 10%% %.2f, median %.2f, 90%% %.2f, max %.2f' % (k, x[0], x[len(x) // 10], x[len(x) // 2], x[-len(x) // 10 - 1], x[-1]))
