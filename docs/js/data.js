/* RepVault in the browser: the exercise catalogue (ExerciseSeeder.kt), the templates (WorkoutTemplate.kt),
   a generated demo history, and ports of the dashboard maths in WorkoutRepository.kt and ExerciseStatsViewModel.kt. */
(function (root) {
  "use strict";
  var EXERCISES = [["Bench Press","Chest"],["Incline Bench Press","Chest"],["Decline Bench Press","Chest"],["Dumbbell Bench Press","Chest"],["Incline Dumbbell Press","Chest"],["Cable Fly","Chest"],["Pec Deck Fly","Chest"],["Push Up","Chest"],["Dips","Chest"],["Overhead Press","Shoulders"],["Arnold Press","Shoulders"],["Lateral Raise","Shoulders"],["Front Raise","Shoulders"],["Face Pull","Shoulders"],["Reverse Fly","Shoulders"],["Shrug","Shoulders"],["Pull Up","Back"],["Lat Pulldown","Back"],["Barbell Row","Back"],["Dumbbell Row","Back"],["T-Bar Row","Back"],["Seated Cable Row","Back"],["Deadlift","Back"],["Hyperextension","Back"],["Good Morning","Back"],["Squat","Quads"],["Front Squat","Quads"],["Leg Press","Quads"],["Bulgarian Split Squat","Quads"],["Leg Extension","Quads"],["Hack Squat","Quads"],["Lunge","Quads"],["Romanian Deadlift","Hamstrings"],["Leg Curl","Hamstrings"],["Nordic Curl","Hamstrings"],["Glute Ham Raise","Hamstrings"],["Hip Thrust","Glutes"],["Glute Bridge","Glutes"],["Cable Kickback","Glutes"],["Standing Calf Raise","Calves"],["Seated Calf Raise","Calves"],["Donkey Calf Raise","Calves"],["Barbell Curl","Biceps"],["Dumbbell Curl","Biceps"],["Hammer Curl","Biceps"],["Preacher Curl","Biceps"],["Cable Curl","Biceps"],["Concentration Curl","Biceps"],["Tricep Pushdown","Triceps"],["Overhead Tricep Extension","Triceps"],["Skull Crusher","Triceps"],["Close Grip Bench Press","Triceps"],["Tricep Kickback","Triceps"],["Crunch","Abs"],["Leg Raise","Abs"],["Plank","Abs"],["Cable Crunch","Abs"],["Russian Twist","Abs"],["Hanging Knee Raise","Abs"],["Ab Wheel Rollout","Abs"],["Wrist Curl","Forearms"],["Reverse Wrist Curl","Forearms"],["Farmer Walk","Forearms"]]
    .map(function (e, i) { return { id: i + 1, name: e[0], muscleGroup: e[1] }; });
  var byName = {}; EXERCISES.forEach(function (e) { byName[e.name] = e; });

  var TEMPLATES = [
    ["Push", ["Bench Press", "Incline Dumbbell Press", "Overhead Press", "Lateral Raise", "Tricep Pushdown"]],
    ["Pull", ["Pull Up", "Barbell Row", "Lat Pulldown", "Face Pull", "Barbell Curl"]],
    ["Legs", ["Squat", "Romanian Deadlift", "Leg Press", "Leg Extension", "Leg Curl", "Standing Calf Raise"]],
    ["Upper", ["Bench Press", "Pull Up", "Overhead Press", "Barbell Row", "Lateral Raise", "Barbell Curl"]],
    ["Lower", ["Squat", "Romanian Deadlift", "Leg Press", "Leg Curl", "Standing Calf Raise", "Hip Thrust"]],
    ["Custom", []]
  ].map(function (t) { return { name: t[0], exercises: t[1].map(function (n) { return byName[n]; }) }; });

  var BODYWEIGHT = /^(Push Up|Dips|Pull Up|Nordic Curl|Glute Ham Raise|Hyperextension|Crunch|Leg Raise|Plank|Russian Twist|Hanging Knee Raise|Ab Wheel Rollout)$/;
  var BARBELL = /(Bench Press|Squat|Deadlift|Barbell|Overhead Press|Hip Thrust|Good Morning|T-Bar Row|Shrug|Skull Crusher)/;
  function kind(name) {
    if (BODYWEIGHT.test(name)) return "bodyweight";
    if (/Dumbbell|Bulgarian|Hack Squat|Arnold|Lateral|Front Raise|Hammer|Concentration|Kickback|Lunge|Farmer/.test(name)) return "dumbbell";
    if (BARBELL.test(name)) return "barbell";
    return "machine";
  }

  /* starting weights (kg) used to generate the demo history */
  var START = { "Bench Press": 60, "Incline Dumbbell Press": 22, "Overhead Press": 37.5, "Lateral Raise": 8, "Tricep Pushdown": 25, "Pull Up": 0, "Barbell Row": 55, "Lat Pulldown": 50, "Face Pull": 20, "Barbell Curl": 25, "Squat": 80, "Romanian Deadlift": 70, "Leg Press": 140, "Leg Extension": 45, "Leg Curl": 35, "Standing Calf Raise": 60, "Hip Thrust": 90 };

  function rng(seed) { var s = seed >>> 0; return function () { s = (s + 0x6D2B79F5) >>> 0; var t = s; t = Math.imul(t ^ (t >>> 15), t | 1); t ^= t + Math.imul(t ^ (t >>> 7), t | 61); return ((t ^ (t >>> 14)) >>> 0) / 4294967296; }; }
  function dayStart(ms) { var d = new Date(ms); d.setHours(0, 0, 0, 0); return d.getTime(); }

  /* ~9 weeks of push / pull / legs with rest days, ending yesterday, so the streak bug is visible */
  function demoHistory(nowMs) {
    var r = rng(4217), today = dayStart(nowMs), sessions = [], sets = [], sid = 1, setId = 1, rot = ["Push", "Pull", "Legs"], k = 0;
    var weights = {}; Object.keys(START).forEach(function (n) { weights[n] = START[n]; });
    var pattern = [1, 1, 1, 0, 1, 1, 0]; // train, train, train, rest, train, train, rest
    for (var d = 63; d >= 1; d--) {
      var dayIdx = 63 - d;
      if (!pattern[dayIdx % 7] && d > 3) continue;
      if (r() < 0.08 && d > 3) continue; // the odd missed day
      var tpl = TEMPLATES.filter(function (t) { return t.name === rot[k % 3]; })[0]; k++;
      var date = today - d * 864e5 + (17 + Math.floor(r() * 3)) * 36e5 + Math.floor(r() * 50) * 6e4;
      sessions.push({ id: sid, date: date, workoutType: tpl.name });
      tpl.exercises.forEach(function (ex, xi) {
        var w = weights[ex.name] || 0, nSets = xi < 2 ? 4 : 3, reps0 = xi < 2 ? 5 + Math.floor(r() * 4) : 8 + Math.floor(r() * 5);
        for (var s = 0; s < nSets; s++) sets.push({ id: setId++, sessionId: sid, exerciseId: ex.id, weight: w, reps: Math.max(3, reps0 - (s > 1 ? Math.floor(r() * 2) : 0)) });
        if (w > 0 && r() < 0.55) weights[ex.name] = w + (w >= 60 ? 2.5 : w >= 20 ? 1.25 : 0.5) * (xi < 2 ? 1 : 0.5);
      });
      sid++;
    }
    return { sessions: sessions, sets: sets };
  }

  /* ---------- WorkoutRepository.kt ---------- */
  function ymd(ms) { var d = new Date(ms); return d.getFullYear() + "-" + String(d.getMonth() + 1).padStart(2, "0") + "-" + String(d.getDate()).padStart(2, "0"); }
  function sessionsDesc(db) { return db.sessions.slice().sort(function (a, b) { return b.date - a.date; }); }
  function setsFor(db, sid) { return db.sets.filter(function (s) { return s.sessionId === sid; }); }

  // As written: if the latest workout was yesterday it passes the first check, but the loop then expects today first and stops at 0.
  function currentStreak(db, nowMs) {
    var dates = Array.from(new Set(sessionsDesc(db).map(function (s) { return ymd(s.date); }))).sort().reverse();
    if (!dates.length) return 0;
    var today = ymd(nowMs), yesterday = ymd(nowMs - 864e5);
    if (dates[0] !== today && dates[0] !== yesterday) return 0;
    var streak = 0;
    for (var i = 0; i < dates.length; i++) { if (dates[i] === ymd(nowMs - i * 864e5)) streak++; else break; }
    return streak;
  }
  // What the yesterday rule was clearly meant to do: count back from the latest workout.
  function intendedStreak(db, nowMs) {
    var dates = Array.from(new Set(db.sessions.map(function (s) { return ymd(s.date); }))).sort().reverse();
    if (!dates.length) return 0;
    var start = dates[0] === ymd(nowMs) ? 0 : dates[0] === ymd(nowMs - 864e5) ? 1 : -1;
    if (start < 0) return 0;
    var n = 0; for (var i = 0; i < dates.length; i++) { if (dates[i] === ymd(nowMs - (i + start) * 864e5)) n++; else break; }
    return n;
  }
  function longestStreak(db) {
    var dates = Array.from(new Set(db.sessions.map(function (s) { return ymd(s.date); }))).sort();
    if (!dates.length) return 0;
    var longest = 1, cur = 1;
    for (var i = 1; i < dates.length; i++) {
      var diff = Math.round((Date.parse(dates[i] + "T00:00:00") - Date.parse(dates[i - 1] + "T00:00:00")) / 864e5);
      if (diff === 1) { cur++; longest = Math.max(longest, cur); } else cur = 1;
    }
    return longest;
  }
  function totals(db) {
    var vol = 0; db.sets.forEach(function (s) { vol += s.weight * s.reps; });
    var counts = {}; db.sets.forEach(function (s) { counts[s.exerciseId] = (counts[s.exerciseId] || 0) + 1; });
    var fav = null, best = -1; Object.keys(counts).forEach(function (k) { if (counts[k] > best) { best = counts[k]; fav = +k; } });
    // "Recent PR": the heaviest single set across every exercise, newest session first, strictly greater wins
    var maxW = 0, maxEx = null;
    sessionsDesc(db).forEach(function (sess) { setsFor(db, sess.id).forEach(function (s) { if (s.weight > maxW) { maxW = s.weight; maxEx = s.exerciseId; } }); });
    return { workouts: db.sessions.length, sets: db.sets.length, volume: vol, favorite: fav ? EXERCISES[fav - 1] : null, pr: maxW > 0 ? { exercise: EXERCISES[maxEx - 1], weight: maxW } : null };
  }
  function exerciseStats(db, exId) {
    var mine = db.sets.filter(function (s) { return s.exerciseId === exId; });
    var sessIds = Array.from(new Set(mine.map(function (s) { return s.sessionId; })));
    var sess = db.sessions.filter(function (s) { return sessIds.indexOf(s.id) >= 0; }).sort(function (a, b) { return a.date - b.date; });
    var history = sess.map(function (se) { var ss = mine.filter(function (s) { return s.sessionId === se.id; }); return { date: se.date, best: Math.max.apply(null, ss.map(function (s) { return s.weight; })), volume: ss.reduce(function (a, s) { return a + s.weight * s.reps; }, 0) }; });
    return {
      bestWeight: mine.length ? Math.max.apply(null, mine.map(function (s) { return s.weight; })) : 0,
      totalSessions: sessIds.length, totalSets: mine.length,
      totalVolume: mine.reduce(function (a, s) { return a + s.weight * s.reps; }, 0),
      avgReps: mine.length ? mine.reduce(function (a, s) { return a + s.reps; }, 0) / mine.length : 0,
      lastTrained: sess.length ? sess[sess.length - 1].date : null, history: history
    };
  }
  function previousFor(db, exId, excludeSession) {
    var sess = sessionsDesc(db).filter(function (s) { return s.id !== excludeSession; });
    for (var i = 0; i < sess.length; i++) { var ss = setsFor(db, sess[i].id).filter(function (s) { return s.exerciseId === exId; }); if (ss.length) return { date: sess[i].date, sets: ss }; }
    return null;
  }
  /* CalendarHeatmap.kt: from three months back (aligned to Monday) to today; every active day gets intensity 0.6 */
  function heatmap(db, nowMs) {
    var active = {}, vol = {}; db.sessions.forEach(function (s) { var k = ymd(s.date), ss = setsFor(db, s.id); active[k] = (active[k] || 0) + ss.length; vol[k] = (vol[k] || 0) + ss.reduce(function (a, x) { return a + x.weight * x.reps; }, 0); });
    var end = new Date(nowMs), cal = new Date(nowMs); cal.setMonth(cal.getMonth() - 3);
    var dow = (cal.getDay() + 6) % 7; cal.setDate(cal.getDate() - dow); cal.setHours(0, 0, 0, 0);
    var weeks = [];
    while (cal.getTime() <= end.getTime()) {
      var days = [], label = cal.toLocaleString(undefined, { month: "short" });
      for (var i = 0; i < 7; i++) { var k = ymd(cal.getTime()); days.push({ date: cal.getTime(), sets: active[k] || 0, volume: vol[k] || 0, future: cal.getTime() > end.getTime() }); cal.setDate(cal.getDate() + 1); }
      weeks.push({ label: label, days: days });
    }
    return weeks;
  }
  function plates(weight, bar) { // per side, greedy, standard kg plates
    var per = Math.max(0, (weight - bar) / 2), out = [];
    [25, 20, 15, 10, 5, 2.5, 1.25].forEach(function (p) { while (per >= p - 1e-9) { out.push(p); per -= p; } });
    return { plates: out, remainder: Math.round(per * 2 * 100) / 100 };
  }

  var api = { EXERCISES: EXERCISES, TEMPLATES: TEMPLATES, byName: byName, kind: kind, demoHistory: demoHistory, currentStreak: currentStreak, intendedStreak: intendedStreak, longestStreak: longestStreak, totals: totals, exerciseStats: exerciseStats, previousFor: previousFor, heatmap: heatmap, plates: plates, ymd: ymd, dayStart: dayStart };
  if (typeof module !== "undefined" && module.exports) module.exports = api; else root.RV = api;
})(typeof window !== "undefined" ? window : globalThis);
